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

@Service
@Data
@RequiredArgsConstructor
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final UserMapper userMapper;
    private final UserStorageMapper userStorageMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден!");
        }
        return user;
    }

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

    public void deleteUser(int id, User userAuthentication) throws JsonProcessingException {
        User userFromDB = userRepository.getReferenceById((long) id);
        logService.writeUserLog(userAuthentication, "администратор удалил пользователя - \"" + userFromDB.getUsername() + "\"");
        userRepository.deleteById((long) id);
    }

    public User getUserById(long id) {
        return userRepository.getReferenceById(id);
    }

    public boolean checkOldPassword(String passwordOld, User userFromDb) {
        return passwordEncoder.matches(passwordOld, userFromDb.getPassword());
    }

    public boolean loadUserByUsernameForUpdateUser(String usernameNew) {
        User userFromDb = userRepository.findByUsername(usernameNew);
        return userFromDb != null;
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
        if (form.get("passwordNew") != null && !form.get("passwordNew").isEmpty()) {
            userFromDb.setPassword(passwordEncoder.encode(form.get("passwordNew")));
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

    public User getUserByIdWithUserStorage(User user){
        User userBuff = userRepository.findByIdWithUserStorage(user.getId()); // Загрузка с использованием JOIN FETCH
        return userBuff;
    }

//******************************************************************************************************************

// Получение списка пользователей в зависимости от роли администратора
    public List<UserDTO> getAllUsersForRoleAdmin(User adminUser) {
        List<User> filteredSortedUsers;
        User fullUserAuthentication = getUserByIdWithUserStorage(adminUser);
        //Для MAIN_ADMIN показываем всех пользователей
        if (fullUserAuthentication.isMainAdmin()){
            List<User> userList = findAll();
            filteredSortedUsers = sortingListByRoleByName(userList);
            //Для ADMIN показываем только его пользователей
        } else {
            UserStorage userStorageDb = fullUserAuthentication.getUserStorage();
            filteredSortedUsers = sortingListByRoleByName(userStorageDb.getAllNestedStorageUsers(userStorageDb));
        }
        return userMapper.convertToDTOList(filteredSortedUsers);
    }

    //вспомогательные методы

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> sortingListByRoleByName(List<User> userList){
        return userList.stream()
                .sorted(Comparator.comparing((User user) -> {
                            if (user.getRoles().contains(Role.MAIN_ADMIN)) {
                                return 0; // Высший приоритет
                            } else if (user.getRoles().contains(Role.ADMIN)) {
                                return 1; // Средний приоритет
                            } else {
                                return 2; // Низший приоритет
                            }
                        })
                        .thenComparing(User::getUsername)) // Дополнительно сортируем по username в алфавитном порядке
                .toList();
    }

// получение списка пользователей для конкретного хранилища ------------------------------------------------------------
    public List<UserDTO> getUsersByStorageId(UserStorage userStorage) {
        List<User> filteredSortedUsers;
        filteredSortedUsers = sortingListByRoleByName(userStorage.getAllNestedStorageUsers(userStorage));
        return userMapper.convertToDTOList(filteredSortedUsers);
    }


// получение профиля пользователя ----------------------------------------------------------------------------------

    public String getUserProfile(User userAuthentication, int id, Model model) {
        // Получение пользователя из базы
        User userFromDb = Optional.ofNullable(getUserById(id))
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден."));

        User fullUserAuthentication = Optional.ofNullable(getUserByIdWithUserStorage(userAuthentication))
                .orElseThrow(() -> new IllegalStateException("Ошибка получения данных аутентификации."));

        // Проверка прав доступа
        if (!hasAccessToProfile(fullUserAuthentication, userFromDb)) {
            throw new SecurityException("Доступ к профилю пользователя запрещен.");
        }

        // Конвертация пользователя и хранилища в DTO
        UserDTO userDTO = userMapper.convertToDTO(userFromDb);
        UserStorageDTO userStorageDTO = userStorageMapper.convertToDTOForProfile(userFromDb.getUserStorage(), fullUserAuthentication);

        // Установка данных в модель
        return setModelFromProfileUser(userDTO, userStorageDTO, model);
    }

// Вспомогательные методы

    private boolean hasAccessToProfile(User currentUser, User targetUser) {
        return isUserViewingOwnProfile(currentUser, targetUser)
                || isMainAdmin(currentUser)
                || isAdminWithAccessToUser(currentUser, targetUser);
    }

    private boolean isUserViewingOwnProfile(User currentUser, User targetUser) {
        return currentUser.getId().equals(targetUser.getId());
    }

    private boolean isMainAdmin(User user) {
        return user.isMainAdmin();
    }

    private boolean isAdminWithAccessToUser(User admin, User targetUser) {
        return admin.isAdmin() && !targetUser.getRoles().contains(Role.MAIN_ADMIN) && !targetUser.getRoles().contains(Role.ADMIN);
    }

    private String setModelFromProfileUser(UserDTO userDTO, UserStorageDTO userStorageDTO, Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("accessLevels", AccessLevel.values());
        model.addAttribute("specAccesses", SpecAccess.values());
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("userStorageDTO", userStorageDTO);
        return Constants.PROFILE_PAGE;
    }

}
