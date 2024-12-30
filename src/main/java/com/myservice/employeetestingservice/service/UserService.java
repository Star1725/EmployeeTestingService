package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.AccessLevel;
import com.myservice.employeetestingservice.domain.Role;
import com.myservice.employeetestingservice.domain.SpecAccess;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден!");
        }
        return user;
    }

    public List<User> findAll() {
        return userRepository.findAll();
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
        user.setRoles(List.of(Role.USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User userDb = userRepository.save(user);
        userDb.setCreatedUser(userDb);
        userRepository.save(userDb);
        return true;
    }

    public void deleteUser(Long id, User userAuthentication) throws JsonProcessingException {
        User userFromDB = userRepository.findById(id).get();
        logService.writeUserLog(userAuthentication, "администратор удалил пользователя - \"" + userFromDB.getUsername() + "\"");
        userRepository.deleteById(id);
    }

//    public void writeLogFile(User userSource, String message) {
//        LocalDateTime time = LocalDateTime.now();
//        String dateTime = time.toString();
//        if (userSource.getLogFile() == null || userSource.getLogFile().isEmpty()) {
//            userSource.setLogFile("{}");
//        }
//        String logFile = userSource.getLogFile();
//        Map<String, String> mapLog;
//        try {
//            mapLog = new ObjectMapper().readValue(logFile, new TypeReference<>() {});
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        mapLog.put(dateTime, userSource.getUsername() + ": " + message);
//        try {
//            logFile = new ObjectMapper().writeValueAsString(mapLog);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        userSource.setLogFile(logFile);
//    }

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

    public void updateUserFromDb(User userAuthentication, User userFromDb, Map<String, String> form) throws JsonProcessingException {
        userFromDb.setUsername(form.get("usernameNew"));
        if (!userFromDb.getRoles().contains(Role.MAIN_ADMIN)) {
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
}
