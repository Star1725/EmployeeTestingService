package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.repository.UsersStorageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
public class UserStorageService {
    private final UsersStorageRepository usersStorageRepository;
    private final LogService logService;

    public Set<UserStorage> getAllUserStoragesWithDefaultParent() {
        return usersStorageRepository.findAll().stream()
                //отфильтровываем дефолтную организацию
                .filter(userStorage -> !"-".equals(userStorage.getUserStorageName()))
                //оставляем только родительские организации верхнего уровня (дочерние организации для дефолтной)
                .filter(userStorage -> userStorage.getParentUserStorage().getUserStorageName().equals("-"))
                .collect(Collectors.toSet());
    }

    public boolean addUserStorage(UserStorage userStorage, User userAdmin, UserStorage userStorageParent) throws JsonProcessingException {
        //проверяем БД на наличие хранилища с таким же именем
        if (userStorageParent != null){
            if (userStorageParent.getChildUserStorages().stream().anyMatch(userStorage1 -> userStorage1.getUserStorageName().equals(userStorage.getUserStorageName()))){
                return true;
            }
        } else {
            UserStorage userStorageDb = usersStorageRepository.findByUserStorageName(userStorage.getUserStorageName());
            if (userStorageDb != null) {
                return true;
            }
        }
        LocalDateTime time = LocalDateTime.now();
        userStorage.setCreatedUser(userAdmin);
        userStorage.setDateCreated(time);
        if (userStorageParent != null){
            userStorageParent.getChildUserStorages().add(userStorage);
            userStorageParent.setParentStorage(true);
            userStorage.setChildStorage(true);
            userStorage.setParentUserStorage(userStorageParent);
            userStorageParent.setDateChanged(time);
            logService.writeStorageLog(userStorageParent, ": администратор - \"" + userAdmin.getUsername() + "\" в состав организации/подразделения добавлено подразделение - \"" + userStorage.getUserStorageName() + "\"");
            logService.writeUserLog(userAdmin, "администратор в состав организации/подразделения - \"" + userStorageParent.getUserStorageName() + "\" добавил подразделение - \"" + userStorage.getUserStorageName() + "\"");
            usersStorageRepository.save(userStorageParent);
        } else {
            //устанавливаем для создаваемого хранилища родительское хранилище по умолчанию
            UserStorage userStorageDefault = usersStorageRepository.getReferenceById(0L);
            userStorage.setParentUserStorage(userStorageDefault);
            userStorageDefault.setParentStorage(true);
            userStorage.setChildStorage(true);
            userStorageDefault.getChildUserStorages().add(userStorage);
            logService.writeStorageLog(userStorage, ": организации/подразделения создано администратором - \"" + userAdmin.getUsername() + "\"");
            logService.writeUserLog(userAdmin, "администратор добавил организацию/подразделение - \"" + userStorage.getUserStorageName() + "\"");
            usersStorageRepository.save(userStorageDefault);
        }
        return false;
    }

    public boolean updateUserStorage(UserStorage updatedUserStorage, User userAdmin, UserStorage userStorageParent) throws JsonProcessingException {
        //проверяем БД на наличие хранилища с таким же именем
        UserStorage userStorageDb = usersStorageRepository.getReferenceById(updatedUserStorage.getId());
        if (userStorageParent.getChildUserStorages().stream().anyMatch(userStorage1 -> userStorage1.getUserStorageName().equals(updatedUserStorage.getUserStorageName()))){
            return true;
        }

        LocalDateTime time = LocalDateTime.now();
        userStorageDb.setChangedUser(userAdmin);
        userStorageDb.setUserStorageName(updatedUserStorage.getUserStorageName());
        userStorageParent.getChildUserStorages().add(userStorageDb);
        userStorageParent.setParentStorage(true);
        userStorageDb.setParentUserStorage(userStorageParent);
        userStorageDb.setChildStorage(true);
        logService.writeStorageLog(userStorageParent, ": администратор - \"" + userAdmin.getUsername() + "\" в составе организации/подразделения обновил данные подразделения - \"" + userStorageDb.getUserStorageName() + "\"");
        logService.writeUserLog(userAdmin, "администратор в составе организации/подразделения - \"" + userStorageParent.getUserStorageName() + "\" обновил данные подразделение - \"" + userStorageDb.getUserStorageName() + "\"");
        usersStorageRepository.save(userStorageParent);
        updatedUserStorage.setDateChanged(time);
        usersStorageRepository.save(userStorageDb);
        return false;
    }

    public String deleteUserStorage(long id, User userAuthentication) throws JsonProcessingException {
        UserStorage userStorageDb = usersStorageRepository.getReferenceById(id);
        String idParentStorage = String.valueOf(0);
        // Если это родительское хранилище, переназначить дочерние элементы
        if (userStorageDb.isParentStorage()) {
            UserStorage grandParentStorage = userStorageDb.getParentUserStorage();
            Set<UserStorage> childUserStorages = new HashSet<>(userStorageDb.getChildUserStorages());
            for (UserStorage child : childUserStorages) {
                if(grandParentStorage != null){
                    child.setParentUserStorage(grandParentStorage);
                    grandParentStorage.getChildUserStorages().add(child);
                    usersStorageRepository.save(grandParentStorage);
                } else {
                    child.setParentUserStorage(usersStorageRepository.getReferenceById(0L));
                }
                child.setUserStorageName(child.getUserStorageName() + "(из состава удаленной организации/подразделения - " + userStorageDb.getUserStorageName() + ")");
                child.setParentStorage(true);
                usersStorageRepository.save(child); // Сохраняем обновление
                logService.writeStorageLog(child, "Родительское хранилище изменено на вышестоящее для удалённого - \"" + userStorageDb.getUserStorageName() + "\"");
            }
            userStorageDb.getChildUserStorages().clear(); // Удаляем ссылки на дочерние элементы
        }
        // Если это дочернее хранилище, обновить родительское хранилище
        if (userStorageDb.isChildStorage()) {
            UserStorage parentUserStorage = userStorageDb.getParentUserStorage();
            if (parentUserStorage != null) {
                parentUserStorage.getChildUserStorages().remove(userStorageDb);
                usersStorageRepository.save(parentUserStorage); // Сохраняем изменения в родительском хранилище
                logService.writeStorageLog(parentUserStorage, "Удалено дочернее подразделение - \"" + userStorageDb.getUserStorageName() + "\"");
                idParentStorage = String.valueOf(parentUserStorage.getId());
            }
        }

        logService.writeUserLog(userAuthentication, "Администратор удалил хранилище - \"" + userStorageDb.getUserStorageName() + "\"");
        usersStorageRepository.delete(userStorageDb); // Удаляем сам элемент
        return idParentStorage;
    }

    public UserStorage getUsersStorageByUsersStorageName(String usersStorageName) {
        return usersStorageRepository.findByUserStorageName(usersStorageName);
    }

    public StringBuilder getAllNameParentStorages(final UserStorage parentUserStorage) {
        List<String> nameParentStorages = new ArrayList<>();
        UserStorage bufferUserStorage = parentUserStorage;

        while (bufferUserStorage != null) {
            String name = bufferUserStorage.getUserStorageName();
            if (!"-".equals(name)) {
                nameParentStorages.add(name);
            }
            bufferUserStorage = bufferUserStorage.getParentUserStorage();
        }

        Collections.reverse(nameParentStorages);
        return joinNamesWithSeparator(nameParentStorages, "/");
    }

    private StringBuilder joinNamesWithSeparator(List<String> names, String separator) {
        StringBuilder result = new StringBuilder();
        for (String name : names) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(name);
        }
        return result;
    }

    public UserStorage determineWhichParentStorage(String userStorageParentNameSelected, String idParentStorage) {
        UserStorage userStorageParent = null;
        if (idParentStorage != null && !idParentStorage.isEmpty()){
            String id = idParentStorage.replaceAll("\\D", "");
            UserStorage userStorageUpParent = getUsersStorageRepository().getReferenceById(Long.valueOf(id));
            if (!userStorageUpParent.getUserStorageName().equals(userStorageParentNameSelected)){
                userStorageParent = userStorageUpParent.getChildUserStorages().stream().filter(userStorage1 -> userStorage1.getUserStorageName().equals(userStorageParentNameSelected)).findAny().get();
            } else {
                userStorageParent = userStorageUpParent;
            }
        } else {
            userStorageParent = getUsersStorageByUsersStorageName(userStorageParentNameSelected);
        }
        return userStorageParent;
    }

}
