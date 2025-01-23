package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.controllers.Constants;
import com.myservice.employeetestingservice.domain.*;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.exception.AccessDeniedException;
import com.myservice.employeetestingservice.exception.AuthenticationDataRetrievalException;
import com.myservice.employeetestingservice.exception.UserNotFoundException;
import com.myservice.employeetestingservice.mapper.UserMapper;
import com.myservice.employeetestingservice.mapper.UserStorageMapper;
import com.myservice.employeetestingservice.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;
import java.util.stream.Collectors;

import static com.myservice.employeetestingservice.controllers.Constants.PASSWORD_NEW;

@Service
@Data
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final UserMapper userMapper;
    private final UserStorageMapper userStorageMapper;
    private final ServiceWorkingUserAndStorage serviceWorkingUserAndStorage;
    private final ValidationService validationService;

    /**
     * Удаляет пользователя из базы данных.
     *
     * @param id                 ID пользователя для удаления
     * @param userAuthentication текущий аутентифицированный пользователь
     * @throws JsonProcessingException если возникает ошибка при записи лога
     * @throws UserNotFoundException   если пользователь с указанным ID не найден
     */
    public void deleteUser(long id, User userAuthentication) throws JsonProcessingException {
        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        logService.writeUserLog(userAuthentication, "администратор удалил пользователя - \"" + userFromDb.getUsername() + "\"");
        userRepository.delete(userFromDb);
    }

// Получение списка пользователей в зависимости от роли администратора--------------------------------------------------

    /**
     * Получает список пользователей, доступных для текущего администратора.
     * Если пользователь имеет роль MAIN_ADMIN, возвращаются все пользователи.
     * Если пользователь имеет роль ADMIN, возвращаются пользователи, связанные с его хранилищем.
     *
     * @param adminUser текущий пользователь с ролью ADMIN или MAIN_ADMIN
     * @return список пользователей в формате DTO, доступных для текущего администратора
     */
    public List<UserDTO> getAllUsersForRoleAdmin(User adminUser) {
        User fullAdminUser = getUserByIdWithUserStorage(adminUser);

        List<User> users = fullAdminUser.isMainAdmin() ?
                findAllUsersSorted() :
                getUsersForStorage(fullAdminUser.getUserStorage());

        return userMapper.convertToDTOList(users);
    }

    /**
     * Получает всех пользователей из базы данных и сортирует их.
     * Сортировка выполняется сначала по роли, затем по имени пользователя.
     *
     * @return отсортированный список всех пользователей
     */
    private List<User> findAllUsersSorted() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing((User user) -> user.getRoles().contains(Role.MAIN_ADMIN) ? 0 :
                                user.getRoles().contains(Role.ADMIN) ? 1 : 2)
                        .thenComparing(User::getUsername))
                .toList();
    }

    /**
     * Получает пользователей для указанного хранилища.
     * Включает пользователей текущего хранилища и вложенных хранилищ, сортирует их по роли и имени.
     *
     * @param storage хранилище, для которого необходимо получить пользователей
     * @return отсортированный список пользователей хранилища
     */
    private List<User> getUsersForStorage(UserStorage storage) {
        return storage.getAllNestedStorageUsers(storage).stream()
                .sorted(Comparator.comparing((User user) -> user.getRoles().contains(Role.MAIN_ADMIN) ? 0 :
                                user.getRoles().contains(Role.ADMIN) ? 1 : 2)
                        .thenComparing(User::getUsername))
                .toList();
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Сортирует список пользователей по приоритету ролей и имени.
     * Приоритет ролей:
     * 1. MAIN_ADMIN — самый высокий приоритет.
     * 2. ADMIN — средний приоритет.
     * 3. Остальные пользователи — низший приоритет.
     *
     * @param userList список пользователей для сортировки
     * @return отсортированный список пользователей
     */
    public List<User> sortingListByRoleByName(List<User> userList) {
        return userList.stream()
                .sorted(Comparator.comparing((User user) -> {
                            // Присваиваем приоритет в зависимости от роли пользователя
                            if (user.getRoles().contains(Role.MAIN_ADMIN)) {
                                return 0; // Высший приоритет
                            } else if (user.getRoles().contains(Role.ADMIN)) {
                                return 1; // Средний приоритет
                            } else {
                                return 2; // Низший приоритет
                            }
                        })
                        .thenComparing(User::getUsername)) // Дополнительно сортируем пользователей по имени (username) в алфавитном порядке
                .toList();
    }

// получение списка пользователей для конкретного хранилища ------------------------------------------------------------

    /**
     * Получает список пользователей, связанных с указанным хранилищем.
     * Включает пользователей из текущего хранилища и всех вложенных хранилищ.
     * Результат сортируется по приоритету ролей и имени пользователя.
     *
     * @param userStorage объект хранилища, для которого необходимо получить пользователей
     * @return список пользователей в формате DTO, связанных с указанным хранилищем
     */
    public List<UserDTO> getUsersByStorageId(UserStorage userStorage) {
        // Получаем список всех пользователей из текущего хранилища и всех вложенных хранилищ
        List<User> filteredSortedUsers = sortingListByRoleByName(userStorage.getAllNestedStorageUsers(userStorage));

        // Преобразуем список пользователей в DTO и возвращаем
        return userMapper.convertToDTOList(filteredSortedUsers);
    }

// получение профиля пользователя --------------------------------------------------------------------------------------

    /**
     * Получает данные профиля пользователя и проверяет доступ текущего пользователя к этим данным.
     * Если доступ разрешён, данные профиля добавляются в модель и возвращается имя страницы профиля.
     *
     * @param userAuthentication текущий аутентифицированный пользователь
     * @param id                 ID пользователя, профиль которого необходимо просмотреть
     * @param model              модель для передачи данных в представление
     * @return имя страницы профиля
     * @throws IllegalArgumentException если пользователь с указанным ID не найден
     * @throws IllegalStateException    если информация о текущем пользователе не получена
     * @throws SecurityException        если доступ к профилю пользователя запрещён
     */
    public String getUserProfile(User userAuthentication, Optional<Long> id, Model model) {
        User userFromDb = id.map(this::getUserById)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User fullAuthUser = getUserByIdWithUserStorage(userAuthentication);

        if (!hasAccessToProfile(fullAuthUser, userFromDb)) {
            throw new SecurityException("Access denied to user profile");
        }

        // Преобразование пользователя из базы данных в DTO-объект для представления
        UserDTO userDTO = userMapper.convertToDTO(userFromDb);

        // Преобразование хранилища пользователя в DTO-объект для представления
        UserStorageDTO userStorageDTO = userStorageMapper.convertToDTOForProfilePage(userFromDb.getUserStorage(), fullAuthUser);

        // Добавление данных пользователя и хранилища в модель и возврат имени страницы профиля
        return setModelFromProfileUser(userDTO, userStorageDTO, model);
    }

    /**
     * Проверяет, имеет ли текущий пользователь доступ к профилю другого пользователя.
     * Доступ разрешается, если:
     * - Пользователь просматривает собственный профиль.
     * - Пользователь является MainAdmin.
     * - Пользователь является Admin и имеет доступ к профилю целевого пользователя.
     *
     * @param currentUser текущий пользователь
     * @param targetUser  целевой пользователь
     * @return true, если доступ разрешён; false, если запрещён
     */
    private boolean hasAccessToProfile(User currentUser, User targetUser) {
        return currentUser.getId().equals(targetUser.getId()) ||
                currentUser.isMainAdmin() ||
                (currentUser.isAdmin() && !targetUser.isAdmin() && !targetUser.isMainAdmin());
    }

    /**
     * Устанавливает данные профиля пользователя и его хранилища в модель.
     *
     * @param userDTO        DTO-объект пользователя
     * @param userStorageDTO DTO-объект хранилища пользователя
     * @param model          модель для передачи данных в представление
     * @return имя страницы профиля (PROFILE_PAGE)
     */
    private String setModelFromProfileUser(UserDTO userDTO, UserStorageDTO userStorageDTO, Model model) {
        // Добавление доступных ролей, уровней доступа и специальных прав в модель
        model.addAttribute("roles", Role.values());
        model.addAttribute("accessLevels", AccessLevel.values());
        model.addAttribute("specAccesses", SpecAccess.values());

        // Добавление информации о пользователе и его хранилище в модель
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("userStorageDTO", userStorageDTO);

        // Возврат имени страницы профиля
        return Constants.PROFILE_PAGE;
    }

    /**
     * Обновляет профиль пользователя, включая имя, пароль и данные хранилища.
     * Проверяет входные данные на валидность, обновляет связанные сущности и записывает изменения в базу данных.
     *
     * @param model                            объект модели для передачи данных в представление
     * @param usernameNew                      новое имя пользователя
     * @param passwordOld                      старый пароль пользователя
     * @param userAuthentication               текущий аутентифицированный пользователь
     * @param primaryParentStorageNameSelected имя родительского хранилища
     * @param storageIdSelected                ID нового хранилища пользователя
     * @param passwordNew                      новый пароль пользователя
     * @param passwordNew2                     подтверждение нового пароля
     * @param id                               ID пользователя, профиль которого обновляется
     * @param form                             форма с дополнительными параметрами обновления
     * @return имя страницы профиля или редирект на другую страницу
     * @throws JsonProcessingException если возникает ошибка при записи лога
     * @throws UserNotFoundException   если пользователь с указанным ID не найден
     */
    public String updateUserProfile(
            Model model,
            String usernameNew,
            String passwordOld,
            User userAuthentication,
            String primaryParentStorageNameSelected,
            String storageIdSelected,
            String passwordNew,
            String passwordNew2,
            Optional<Long> id,
            Map<String, String> form) throws JsonProcessingException {

        User userFromDb = id.map(this::getUserById)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        User fullAuthUser = getUserByIdWithUserStorage(userAuthentication);

        // Валидация
        validationService.validateUserProfile(model, usernameNew, passwordOld, passwordNew, passwordNew2, userFromDb);

        if (model.asMap().keySet().stream().anyMatch(key -> key.contains("Error"))) {
            return Constants.PROFILE_PAGE;
        }

        // Определение нового хранилища
        UserStorage newUserStorage = resolveUserStorage(primaryParentStorageNameSelected, storageIdSelected);

        // Обновление пользователя и хранилища
        updateUserAndRoles(userFromDb, newUserStorage, form, fullAuthUser);

        // Конвертируем пользователя в DTO и добавляем в модель
        model.addAttribute("userDTO", userMapper.convertToDTO(userFromDb));
        model.addAttribute("userStorageDTO", userStorageMapper.convertToDTOForProfilePage(newUserStorage, fullAuthUser));
        model.addAttribute("message", "Profile updated successfully!");

        if (passwordNew != null && !passwordNew.isEmpty()) {
            return "redirect:/users/logout";
        }

        return fullAuthUser.getId().equals(userFromDb.getId()) ? Constants.PROFILE_PAGE : "redirect:/users";
    }

    /**
     * Обновляет роли, имя и хранилище пользователя.
     * Выполняет проверку данных, переданных через форму, обновляет сущности и сохраняет изменения в базу данных.
     *
     * @param userFromDb пользователь, данные которого обновляются
     * @param newStorage новое хранилище пользователя
     * @param form       данные формы с параметрами обновления
     * @param authUser   текущий аутентифицированный пользователь, выполняющий операцию
     * @throws JsonProcessingException если возникает ошибка при записи лога
     */
    private void updateUserAndRoles(User userFromDb, UserStorage newStorage, Map<String, String> form, User authUser)
            throws JsonProcessingException {
        if (form.containsKey("usernameNew")) {
            userFromDb.setUsername(form.get("usernameNew"));
        }

        updateRoles(userFromDb, form);

        if (form.containsKey(Constants.PASSWORD_NEW)) {
            userFromDb.setPassword(passwordEncoder.encode(form.get(Constants.PASSWORD_NEW)));
        }

        if (!Objects.equals(userFromDb.getUserStorage(), newStorage)) {
            serviceWorkingUserAndStorage.updateUserForStorage(userFromDb.getUserStorage(), newStorage, authUser, userFromDb);
        } else {
            serviceWorkingUserAndStorage.updateUserForStorage(newStorage, authUser, userFromDb);
        }

        userRepository.save(userFromDb);
        logService.writeUserLog(authUser, "Updated user: " + userFromDb.getUsername());
    }

    /**
     * Обновляет роли пользователя на основе переданных данных.
     * Проверяет данные формы, сопоставляет их с доступными ролями, уровнями доступа и специальными правами.
     *
     * @param user пользователь, роли которого обновляются
     * @param form данные формы с параметрами обновления
     */
    private void updateRoles(User user, Map<String, String> form) {
        updateAttributes(user.getRoles(), Role.values(), form, Role.class);
        updateAttributes(user.getAccessLevels(), AccessLevel.values(), form, AccessLevel.class);
        updateAttributes(user.getSpecAccesses(), SpecAccess.values(), form, SpecAccess.class);
    }

    /**
     * Обобщённо обновляет атрибуты пользователя, такие как роли, уровни доступа и специальные права.
     * Сравнивает значения из формы с допустимыми перечислениями и обновляет соответствующий список атрибутов.
     *
     * @param attributes список атрибутов, который необходимо обновить
     * @param allValues все возможные значения атрибутов
     * @param form данные формы с параметрами обновления
     * @param enumType тип перечисления, представляющего атрибуты
     * @param <T> тип перечисления
     */
    private <T extends Enum<T>> void updateAttributes(List<T> attributes, T[] allValues, Map<String, String> form, Class<T> enumType) {
        if (attributes != null) {
            attributes.clear();
        } else {
            attributes = new LinkedList<>();
        }

        Set<String> validKeys = Arrays.stream(allValues).map(Enum::name).collect(Collectors.toSet());

        for (String key : form.keySet()) {
            if (validKeys.contains(key)) {
                attributes.add(Enum.valueOf(enumType, key));
            }
        }
    }

    /**
     * Определяет хранилище пользователя по его имени или ID.
     *
     * @param parentName имя родительского хранилища
     * @param storageId ID хранилища
     * @return найденное хранилище пользователя
     */
    private UserStorage resolveUserStorage(String parentName, String storageId) {
        return Optional.ofNullable(storageId)
                .filter(id -> !id.isEmpty())
                .map(Long::parseLong)
                .map(serviceWorkingUserAndStorage::getUserStorageById)
                .orElseGet(() -> serviceWorkingUserAndStorage.getUserStorageByUsersStorageName(parentName));
    }

    /**
     * Получает пользователя по его ID.
     *
     * @param id уникальный идентификатор пользователя
     * @return пользователь, найденный по ID
     * @throws UserNotFoundException если пользователь не найден
     */
    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    /**
     * Получает пользователя вместе с данными о его хранилище.
     *
     * @param user текущий пользователь
     * @return пользователь с полной информацией о хранилище
     * @throws AuthenticationDataRetrievalException если данные пользователя не получены
     */
    public User getUserByIdWithUserStorage(User user) {
        return Optional.ofNullable(userRepository.findByIdWithUserStorage(user.getId()))
                .orElseThrow(() -> new AuthenticationDataRetrievalException("Authentication data retrieval error"));
    }
}
