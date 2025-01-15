package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.controllers.Constants;
import com.myservice.employeetestingservice.domain.*;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.mapper.UserMapper;
import com.myservice.employeetestingservice.mapper.UserStorageMapper;
import com.myservice.employeetestingservice.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.myservice.employeetestingservice.controllers.Constants.PASSWORD_NEW;

@Service
@Data
@RequiredArgsConstructor
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final UserMapper userMapper;
    private final UserStorageMapper userStorageMapper;
    private final UserStorageService userStorageService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден!");
        }
        return user;
    }


//Создание пользователя через страницу регистрации------------------------------------------------------------------
    public boolean createUserFromRegistrationPage(User user) throws JsonProcessingException {
        User userFromDB = userRepository.findByUsername(user.getUsername());
        if (userFromDB != null) {
            return false;
        }
        user.setActive(true);
        LocalDateTime timeCreated = LocalDateTime.now();
        user.setDateCreated(timeCreated);
        logService.writeUserLog(user, "пользователь создан через страницу регистрации.");
        user.setRoles(new ArrayList<>(List.of(Role.USER)));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User userDb = userRepository.save(user);
        userDb.setCreatedUser(userDb);
        userRepository.save(userDb);
        return true;
    }

//Удаление пользователя-------------------------------------------------------------------------------------------------
    public void deleteUser(int id, User userAuthentication) throws JsonProcessingException {
        User userFromDB = userRepository.getReferenceById((long) id);
        logService.writeUserLog(userAuthentication, "администратор удалил пользователя - \"" + userFromDB.getUsername() + "\"");
        userRepository.deleteById((long) id);
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
        List<User> filteredSortedUsers;

        // Получаем полную информацию о текущем администраторе, включая его хранилище
        User fullUserAuthentication = getUserByIdWithUserStorage(adminUser);

        // Если пользователь является MAIN_ADMIN, возвращаем всех пользователей
        if (fullUserAuthentication.isMainAdmin()) {
            List<User> userList = findAll(); // Получаем всех пользователей из базы данных
            filteredSortedUsers = sortingListByRoleByName(userList); // Сортируем пользователей по ролям и имени
        } else {
            // Если пользователь является ADMIN, возвращаем пользователей его хранилища
            UserStorage userStorageDb = fullUserAuthentication.getUserStorage();
            // Получаем пользователей из текущего и вложенных хранилищ, связанных с администратором
            filteredSortedUsers = sortingListByRoleByName(userStorageDb.getAllNestedStorageUsers(userStorageDb));
        }

        // Преобразуем список пользователей в DTO и возвращаем
        return userMapper.convertToDTOList(filteredSortedUsers);
    }

    /**
     * Получает всех пользователей из базы данных.
     *
     * @return список всех пользователей
     */
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
     * @param id ID пользователя, профиль которого необходимо просмотреть
     * @param model модель для передачи данных в представление
     * @return имя страницы профиля
     * @throws IllegalArgumentException если пользователь с указанным ID не найден
     * @throws IllegalStateException если информация о текущем пользователе не получена
     * @throws SecurityException если доступ к профилю пользователя запрещён
     */
    public String getUserProfile(User userAuthentication, int id, Model model) {
        // Получение пользователя из базы данных по ID
        // Если пользователь не найден, выбрасывается IllegalArgumentException с соответствующим сообщением
        User userFromDb = Optional.ofNullable(getUserById(id))
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден."));

        // Получение полной информации о текущем аутентифицированном пользователе, включая данные о хранилище
        // Если информация не получена, выбрасывается IllegalStateException с соответствующим сообщением
        User fullUserAuthentication = Optional.ofNullable(getUserByIdWithUserStorage(userAuthentication))
                .orElseThrow(() -> new IllegalStateException("Ошибка получения данных аутентификации."));

        // Проверка прав доступа текущего пользователя к профилю пользователя из базы данных
        // Если доступ запрещён, выбрасывается SecurityException
        if (!hasAccessToProfile(fullUserAuthentication, userFromDb)) {
            throw new SecurityException("Доступ к профилю пользователя запрещен.");
        }

        // Преобразование пользователя из базы данных в DTO-объект для представления
        UserDTO userDTO = userMapper.convertToDTO(userFromDb);

        // Преобразование хранилища пользователя в DTO-объект для представления
        UserStorageDTO userStorageDTO = userStorageMapper.convertToDTOForProfile(userFromDb.getUserStorage(), fullUserAuthentication);

        // Добавление данных пользователя и хранилища в модель и возврат имени страницы профиля
        return setModelFromProfileUser(userDTO, userStorageDTO, model);
    }

    /**
     * Проверяет, имеет ли текущий пользователь доступ к профилю другого пользователя.
     * Доступ разрешается, если:
     * - Пользователь просматривает собственный профиль.
     * - Пользователь является MainAdmin.
     * - Пользователь является Admin и имеет доступ к профилю целевого пользователя.
     * @param currentUser текущий пользователь
     * @param targetUser целевой пользователь
     * @return true, если доступ разрешён; false, если запрещён
     */
    private boolean hasAccessToProfile(User currentUser, User targetUser) {
        return isUserViewingOwnProfile(currentUser, targetUser)
                || isMainAdmin(currentUser)
                || isAdminWithAccessToUser(currentUser, targetUser);
    }

    /**
     * Проверяет, просматривает ли пользователь свой собственный профиль.
     * @param currentUser текущий пользователь
     * @param targetUser целевой пользователь
     * @return true, если текущий пользователь просматривает свой профиль; иначе false
     */
    private boolean isUserViewingOwnProfile(User currentUser, User targetUser) {
        return currentUser.getId().equals(targetUser.getId());
    }

    /**
     * Проверяет, является ли пользователь MainAdmin.
     * @param user пользователь для проверки
     * @return true, если пользователь имеет роль MainAdmin; иначе false
     */
    private boolean isMainAdmin(User user) {
        return user.isMainAdmin();
    }

    /**
     * Проверяет, имеет ли Admin доступ к профилю целевого пользователя.
     * Admin не может просматривать профили MainAdmin и других Admin.
     * @param admin пользователь с ролью Admin
     * @param targetUser целевой пользователь
     * @return true, если Admin имеет доступ; иначе false
     */
    private boolean isAdminWithAccessToUser(User admin, User targetUser) {
        return admin.isAdmin() && !targetUser.getRoles().contains(Role.MAIN_ADMIN) && !targetUser.getRoles().contains(Role.ADMIN);
    }

    /**
     * Устанавливает данные профиля пользователя и его хранилища в модель.
     * @param userDTO DTO-объект пользователя
     * @param userStorageDTO DTO-объект хранилища пользователя
     * @param model модель для передачи данных в представление
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

//обновления профиля пользователя --------------------------------------------------------------------------------------
    /**
     * Обновляет профиль пользователя, включая его имя, пароль и связанное хранилище.
     * Проверяет права доступа и валидность введённых данных. Если данные некорректны, возвращает страницу профиля с ошибками.
     *
     * @param model модель для передачи данных в представление
     * @param usernameNew новое имя пользователя
     * @param passwordOld старый пароль пользователя
     * @param userAuthentication текущий аутентифицированный пользователь
     * @param primaryParentStorageNameSelected имя выбранного хранилища
     * @param storageIdSelected ID выбранного хранилища
     * @param passwordNew новый пароль
     * @param passwordNew2 подтверждение нового пароля
     * @param id ID пользователя, профиль которого обновляется
     * @param form данные, отправленные через форму
     * @return имя страницы профиля или перенаправление на другую страницу
     * @throws JsonProcessingException если возникает ошибка обработки JSON
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
            Optional<Integer> id,
            Map<String, String> form) throws JsonProcessingException {

        // Получаем пользователя из базы данных по ID
        // Если пользователь не найден, выбрасывается IllegalArgumentException
        User userFromDb = id.map(this::getUserById)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));

        // Получаем полные данные о текущем пользователе, включая информацию о хранилище
        // Если данные не получены, выбрасывается IllegalStateException
        User fullUserAuthentication = Optional.ofNullable(getUserByIdWithUserStorage(userAuthentication))
                .orElseThrow(() -> new IllegalStateException("Ошибка получения данных аутентификации."));

        // Проверяем валидность нового имени пользователя
        // Если имя некорректно, добавляем соответствующие ошибки в модель
        validateUsername(usernameNew, userFromDb, model);

        // Проверяем необходимость изменения пароля
        // Если пароль некорректен или подтверждение не совпадает, добавляем ошибки в модель
        boolean isChangePassword = validatePassword(passwordOld, passwordNew, passwordNew2, userFromDb, model);

        // Определяем новое хранилище пользователя на основе выбранного ID или имени
        UserStorage newUserStorage = resolveUserStorage(primaryParentStorageNameSelected, storageIdSelected);

        // Добавляем необходимые данные в модель
        addProfileAttributesToModel(model, userFromDb, usernameNew, newUserStorage, fullUserAuthentication);

        // Если в модели присутствуют ошибки (например, валидатор добавил их), возвращаем страницу профиля
        if (model.asMap().keySet().stream().anyMatch(key -> key.contains("Error"))) {
            return Constants.PROFILE_PAGE;
        }

        // Обновляем данные пользователя и его связанное хранилище
        updateUserAndStorage(userFromDb, newUserStorage, form, fullUserAuthentication);

        ///добавляем в модель сообщение об обновлении данных
        model.addAttribute("message", "Данные успешно обновлены!");

        // Если пароль был изменён, перенаправляем пользователя на страницу выхода
        if (isChangePassword) {
            return "redirect:/users/logout";
        }

        // Если текущий пользователь обновляет свои данные, остаёмся на странице профиля
        // В остальных случаях перенаправляем на список пользователей
        return fullUserAuthentication.getId().equals(userFromDb.getId()) ? Constants.PROFILE_PAGE : "redirect:/users";
    }

    /**
     * Проверяет валидность нового имени пользователя и добавляет ошибки в модель, если они есть.
     * @param usernameNew новое имя пользователя
     * @param userFromDb текущий пользователь из базы данных
     * @param model модель для добавления ошибок
     */
    private void validateUsername(String usernameNew, User userFromDb, Model model) {
        if (usernameNew == null || usernameNew.isEmpty()) {
            model.addAttribute("usernameNewError", "Поле не может быть пустым!");
        } else if (loadUserByUsernameForUpdateUser(usernameNew) && !usernameNew.equals(userFromDb.getUsername())) {
            model.addAttribute("usernameNewError", Constants.USERNAME_FIND_ERROR);
        }
    }

    public boolean loadUserByUsernameForUpdateUser(String usernameNew) {
        User userFromDb = userRepository.findByUsername(usernameNew);
        return userFromDb != null;
    }

    /**
     * Проверяет валидность нового пароля и добавляет ошибки в модель, если они есть.
     * @param passwordOld старый пароль
     * @param passwordNew новый пароль
     * @param passwordNew2 подтверждение нового пароля
     * @param userFromDb текущий пользователь из базы данных
     * @param model модель для добавления ошибок
     * @return true, если пароль успешно прошёл проверку, иначе false
     */
    private boolean validatePassword(String passwordOld, String passwordNew, String passwordNew2, User userFromDb, Model model) {
        if (passwordNew == null || passwordNew.isEmpty() || passwordNew2 == null || passwordNew2.isEmpty()) {
            return false; // Пароль не изменяется
        }
        if (!passwordNew.equals(passwordNew2)) {
            model.addAttribute("passwordNewError", Constants.PASSWORD_MISMATCH);
            model.addAttribute("passwordNew2Error", Constants.PASSWORD_MISMATCH);
            return false;
        }
        if (!passwordOld.isEmpty() && !checkOldPassword(passwordOld, userFromDb)) {
            model.addAttribute("passwordOldError", "Вы ввели неправильный пароль");
            return false;
        }
        return true;
    }

    public boolean checkOldPassword(String passwordOld, User userFromDb) {
        return passwordEncoder.matches(passwordOld, userFromDb.getPassword());
    }

    /**
     * Определяет новое хранилище пользователя, используя ID или имя хранилища.
     * @param primaryParentStorageNameSelected имя хранилища, выбранного пользователем
     * @param storageIdSelected ID хранилища, выбранного пользователем
     * @return объект UserStorage, представляющий новое хранилище
     */
    private UserStorage resolveUserStorage(String primaryParentStorageNameSelected, String storageIdSelected) {
        return Optional.ofNullable(storageIdSelected)
                .filter(id -> !id.isEmpty()) // Проверяем, что ID не пустой
                .map(Integer::parseInt) // Конвертируем ID из строки в Integer
                .map(userStorageService::getUserStorageById) // Получаем хранилище по ID
                .orElseGet(() -> userStorageService.getUserStorageByUsersStorageName(primaryParentStorageNameSelected)); // Иначе получаем по имени
    }

    /**
     * Добавляет необходимые атрибуты в модель для корректного отображения страницы профиля.
     *
     * @param model модель для передачи данных в представление
     * @param userFromDb текущий пользователь из базы данных
     * @param usernameNew новое имя пользователя
     * @param newUserStorage новое хранилище пользователя
     * @param fullUserAuthentication текущий аутентифицированный пользователь с полной информацией
     */
    private void addProfileAttributesToModel(Model model, User userFromDb, String usernameNew, UserStorage newUserStorage, User fullUserAuthentication) {
        // Добавляем роли, уровни доступа и специальные права
        if (userFromDb.isAdmin()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("accessLevels", AccessLevel.values());
            model.addAttribute("specAccesses", SpecAccess.values());
        }

        // Конвертируем пользователя в DTO и добавляем в модель
        UserDTO userDTO = userMapper.convertToDTO(userFromDb);
        userDTO.setUsername(usernameNew);
        model.addAttribute("userDTO", userDTO);

        // Конвертируем хранилище в DTO и добавляем в модель
        UserStorageDTO userStorageDTO = userStorageMapper.convertToDTOForProfile(newUserStorage, fullUserAuthentication);
        model.addAttribute("userStorageDTO", userStorageDTO);
    }

    /**
     * Обновляет данные пользователя и связывает их с новым хранилищем.
     * @param userFromDb текущий пользователь из базы данных
     * @param newUserStorage новое хранилище
     * @param form данные формы, отправленные пользователем
     * @param fullUserAuthentication текущий пользователь с полной информацией
     * @throws JsonProcessingException если возникает ошибка обработки JSON
     */
    private void updateUserAndStorage(User userFromDb, UserStorage newUserStorage, Map<String, String> form, User fullUserAuthentication) throws JsonProcessingException {
        userFromDb.setUserStorage(newUserStorage);
        updateUserFromDb(userFromDb, newUserStorage, form, fullUserAuthentication);
        userStorageService.updateUserForStorage(userFromDb.getUserStorage(), newUserStorage, fullUserAuthentication, userFromDb);
    }

    public void updateUserFromDb(User userFromDb, UserStorage userStorage, Map<String, String> form, User userAuthentication) throws JsonProcessingException {
        userFromDb.setUsername(form.get("usernameNew"));
        userFromDb.setUserStorage(userStorage);
        if (userAuthentication != null) {
            //получение списка всех ролей, из которых потом проверить какие установлены данному пользователю
            //для этого переводим Enum в строковый вид
            Set<String> roles = Arrays.stream(Role.values())
                    .map(Role::name)
                    .collect(Collectors.toSet());
            updateRoles(userFromDb.getRoles(), roles, form);
            //аналогично поступаем с AccessLevel
            Set<String> accessLevels = Arrays.stream(AccessLevel.values())
                    .map(AccessLevel::name)
                    .collect(Collectors.toSet());
            updateAccessLevels(userFromDb.getAccessLevels(), accessLevels, form);
            //аналогично поступаем со SpecAccess
            Set<String> specAccessLevels = Arrays.stream(SpecAccess.values())
                    .map(SpecAccess::name)
                    .collect(Collectors.toSet());
            updateSpecAccessLevels(userFromDb.getSpecAccesses(), specAccessLevels, form);
        }
        if (form.get(PASSWORD_NEW) != null && !form.get(PASSWORD_NEW).isEmpty()) {
            userFromDb.setPassword(passwordEncoder.encode(form.get(PASSWORD_NEW)));
        }
        if (userAuthentication == null){
            logService.writeUserLog(userFromDb, "пользователь изменил свои данные");
        } else {
            logService.writeUserLog(userAuthentication, "администратор изменил данные пользователя - \"" + userFromDb.getUsername()+ "\"");
        }
        userRepository.save(userFromDb);
    }

    private void updateRoles(List<Role> roleList, Set<String> stringSet, Map<String, String> form) {
        //очищаем роли пользователя, чтобы назначить новые, взятые из переданной формы
        if (roleList != null){
            roleList.clear();
        } else {
            roleList = new LinkedList<>();
        }
        //теперь проверяем какие роли содержит наша форма - Map<String, String> form
        for (String key : form.keySet()) {
            if (stringSet.contains(key)) {
                roleList.add(Role.valueOf(key));
            }
        }
    }

    private void updateSpecAccessLevels(List<SpecAccess> accessLevelList, Set<String> stringSet, Map<String, String> form) {
        if (accessLevelList != null){
            accessLevelList.clear();
        } else {
            accessLevelList = new LinkedList<>();
        }
        for (String key : form.keySet()) {
            if (stringSet.contains(key)) {
                accessLevelList.add(SpecAccess.valueOf(key));
            }
        }
    }

    private void updateAccessLevels(List<AccessLevel> accessLevelList, Set<String> stringSet, Map<String, String> form) {
        if (accessLevelList != null){
            accessLevelList.clear();
        } else {
            accessLevelList = new LinkedList<>();
        }
        for (String key : form.keySet()) {
            if (stringSet.contains(key)) {
                accessLevelList.add(AccessLevel.valueOf(key));
            }
        }
    }

    public User getUserById(long id) {
        return userRepository.getReferenceById(id);
    }

    public User getUserByIdWithUserStorage(User user){
        return userRepository.findByIdWithUserStorage(user.getId()); // Загрузка с использованием JOIN FETCH
    }
}
