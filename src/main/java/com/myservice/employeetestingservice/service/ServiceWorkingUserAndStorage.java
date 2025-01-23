package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.Role;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.repository.UserRepository;
import com.myservice.employeetestingservice.repository.UserStorageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Data
@RequiredArgsConstructor
public class ServiceWorkingUserAndStorage {
    private final UserRepository userRepository;
    private final UserStorageRepository userStorageRepository;
    private final LogService logService;

    //GET (Получение)---------------------------------------------------------------------------------------------------
    //Получение хранилища по ID
    public UserStorage getUserStorageById(long id) {
        return userStorageRepository.getReferenceById(id);
    }

    //Получение хранилища по имени
    public UserStorage getUserStorageByUsersStorageName(String userStorageName) {
        return userStorageRepository.findByUserStorageName(userStorageName);
    }

    //Обновление пользователя в хранилищах
    public void updateUserForStorage(
            UserStorage oldUserStorage, UserStorage newUserStorageDb, User userAuthentication, User userFromDb) throws JsonProcessingException {
        //если обновление происходит в одном и том же хранилище
        if (oldUserStorage != null && oldUserStorage.getId() == newUserStorageDb.getId()){
            //обновление пользователя в текущем хранилище
            updateUserForStorage(newUserStorageDb, userFromDb, userAuthentication);
        } else {
            //удаление пользователя из старого хранилища
            deleteUserForStorage(oldUserStorage, userFromDb, userAuthentication);
            //добавление пользователя в новое хранилище
            addUserForStorage(newUserStorageDb, userFromDb, userAuthentication);
        }
    }

    public void updateUserForStorage(UserStorage userStorage, User user, User userAuthentication) throws JsonProcessingException {
        if (userAuthentication != null && !userAuthentication.equals(user)) {
            boolean isUpdated = false;
            List<Role> roles;
            roles = user.getRoles();
            //если в хранилище не назначен администратор, то назначаем
            if (userStorage.getAdministrator() == null) {
                if (user.isAdmin()) {
                    userStorage.setAdministrator(user);
                    isUpdated = true;
                }
            }
            //если в хранилище администратор эквивалентен текущему пользователю, то поступил запрос на удаление ссылки
            // на текущего пользователя из поля администратор обновляемого хранилища
            else if (userStorage.getAdministrator().getId() == user.getId()) {
                userStorage.setAdministrator(null);
                isUpdated = true;
            //если в хранилище администратор не эквивалентен user, то поступил запрос на изменение текущей ссылки на нового user,
            //при этом старый администратор не был удалён. Понижаем роль текущего пользователя до User
            } else if (userStorage.getAdministrator().getId() != user.getId() && user.isAdmin()) {
                user.getRoles().remove(Role.ADMIN);
                isUpdated = true;
            }
            if (isUpdated) {
                logService.writeUserLog(userAuthentication, "Администратор обновил роли пользователя \"" + user.getUsername() +
                        "\"(были - " + roles.toString() + ", стали - " + user.getRoles().toString() + ") в хранилище - \"" +
                        userStorage.getUserStorageName() + "\"");
            }
            userStorageRepository.save(userStorage);
        }
    }

    //добавление пользователя в хранилище
    public void addUserForStorage(UserStorage userStorage, User user, User userAuthentication) throws JsonProcessingException {
        boolean isAdd = false;
        if (userStorage != null){
            Set<User> storageUsers = userStorage.getStorageUsers();
            if (storageUsers != null) {
                if (storageUsers.stream().noneMatch(user1 -> Objects.equals(user1.getId(), user.getId()))){
                    storageUsers.add(user);
                    isAdd = true;
                }
                //если пользователь администратор, и поле администратора для хранилища не пустое, то понижаем ему роль до User
                if (user.isAdmin() && userStorage.getAdministrator() != null) {
                    user.getRoles().remove(Role.ADMIN);
                } else if (user.isAdmin() && userStorage.getAdministrator() == null) {
                    userStorage.setAdministrator(user);
                }
            } else {
                userStorage.setStorageUsers(new HashSet<>());
                //если пользователь администратор, и поле администратора для хранилища пустое
                if (user.isAdmin() && userStorage.getAdministrator() == null) {
                    userStorage.setAdministrator(user);
                }
                userStorage.getStorageUsers().add(user);
                isAdd = true;
            }
            if (isAdd){
                if (userAuthentication != null && !userAuthentication.equals(user)){
                    logService.writeUserLog(userAuthentication, "Администратор добавил пользователя \"" + user.getUsername() + "\" в хранилище - \"" + userStorage.getUserStorageName() + "\"");
                } else {
                    logService.writeUserLog(user, "Пользователь добавил себя в хранилище - \"" + userStorage.getUserStorageName() + "\"");
                }
                userStorageRepository.save(userStorage);
                user.setUserStorage(userStorage);
                userRepository.save(user);
            }
        }
    }

    //удаление пользователя из хранилища
    public void deleteUserForStorage(UserStorage userStorage, User user, User userAuthentication) throws JsonProcessingException {
        if (userAuthentication != null){
            if (userStorage != null){
                Set<User> storageUsers = userStorage.getStorageUsers();
                if (storageUsers != null) {
                    //если пользователь администратор, то обнуляем поле администратора для хранилища
                    if (user.isAdmin() && userStorage.getAdministrator() != null && userStorage.getAdministrator().getId() == user.getId()) {
                        userStorage.setAdministrator(null);
                    }
                    //удаляем пользователя из хранилища
                    storageUsers.remove(user);
                }
                userStorageRepository.save(userStorage);
                logService.writeUserLog(userAuthentication, "Администратор удалил пользователя \"" + user.getUsername() + "\" из хранилища - \"" + userStorage.getUserStorageName() + "\"");
            }
        }
    }


    //работа с User

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
