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

@Service
@Data
@RequiredArgsConstructor
public class UserStorageService {
    private final UsersStorageRepository usersStorageRepository;
    private final LogService logService;

    public List<UserStorage> getAllUserStorages() {
        return usersStorageRepository.findAll().stream().sorted(Comparator.comparing(UserStorage::getUserStorageName)).toList();
    }

    public boolean addUserStorage(UserStorage userStorage, User userAdmin, UserStorage userStorageParent) throws JsonProcessingException {
        //проверяем БД на наличие хранилища с таким же именем
        UserStorage userStorageDb = usersStorageRepository.findByUserStorageName(userStorage.getUserStorageName());
        if (userStorageDb != null) {
            return true;
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
            userStorage.setParentUserStorage(usersStorageRepository.getReferenceById(0L));
            logService.writeStorageLog(userStorage, ": организации/подразделения создано администратором - \"" + userAdmin.getUsername() + "\"");
            logService.writeUserLog(userAdmin, "администратор добавил организацию/подразделение - \"" + userStorage.getUserStorageName() + "\"");
            usersStorageRepository.save(userStorage);
        }
        return false;
    }

    public boolean updateUserStorage(UserStorage userStorage, User userAdmin, UserStorage userStorageParent) throws JsonProcessingException {
        //проверяем БД на наличие хранилища с таким же именем
        UserStorage userStorageDb = usersStorageRepository.findByUserStorageName(userStorage.getUserStorageName());
        if (userStorageDb != null) {
            return true;
        }
        LocalDateTime time = LocalDateTime.now();
        userStorage.setChangedUser(userAdmin);
        if (userStorageParent != null){
            userStorageParent.getChildUserStorages().add(userStorage);
            userStorageParent.setParentStorage(true);
            userStorage.setChildStorage(true);
            logService.writeStorageLog(userStorageParent, ": администратор - \"" + userAdmin.getUsername() + "\" в составе организации/подразделения обновил данные подразделения - \"" + userStorage.getUserStorageName() + "\"");
            logService.writeUserLog(userAdmin, "администратор в составе организации/подразделения - \"" + userStorageParent.getUserStorageName() + "\" обновил данные подразделение - \"" + userStorage.getUserStorageName() + "\"");
            usersStorageRepository.save(userStorageParent);
        } else {
            //устанавливаем для обновляемого хранилища родительское хранилище по умолчанию
            userStorage.setParentUserStorage(usersStorageRepository.getReferenceById(0L));
            logService.writeStorageLog(userStorage, ": данные организации/подразделения обновлены администратором - \"" + userAdmin.getUsername() + "\"");
            logService.writeUserLog(userAdmin, "администратор обновил данные организации/подразделения - \"" + userStorage.getUserStorageName() + "\"");
        }
        userStorage.setDateChanged(time);
        usersStorageRepository.save(userStorage);
        return false;
    }

    public void deleteUserStorage(long id, User userAuthentication) throws JsonProcessingException {
        UserStorage userStorageDb = usersStorageRepository.findById(id).get();
        if (userStorageDb.isChildStorage()){
            UserStorage parentUserStorage = userStorageDb.getParentUserStorage();
            if (!parentUserStorage.getUserStorageName().equals("-")){
                Set<UserStorage> userStorageSet = parentUserStorage.getChildUserStorages();
                userStorageSet.remove(userStorageDb);
                usersStorageRepository.save(parentUserStorage);
            }
            logService.writeStorageLog(parentUserStorage, "администратором удалено дочернее подразделение - \"" + userStorageDb.getUserStorageName()+ "\"");
        }
        if (userStorageDb.isParentStorage()){
            Set<UserStorage> userStorageSet = userStorageDb.getChildUserStorages();
            for (UserStorage userStorage : userStorageSet) {
                userStorage.setParentStorage(true);
                userStorage.setParentUserStorage(usersStorageRepository.getReferenceById(0L));
                userStorageDb.getChildUserStorages().remove(userStorage);
                usersStorageRepository.save(userStorageDb);
                usersStorageRepository.save(userStorage);
                logService.writeStorageLog(userStorage, "администратором удалена родительская организация/подразделение - \"" + userStorageDb.getUserStorageName()+ "\"");
            }

        }
        logService.writeUserLog(userAuthentication, "администратор удалил организацию/подразделение - \"" + userStorageDb.getUserStorageName()+ "\"");
        usersStorageRepository.deleteById(id);
    }

    public UserStorage getUsersStorageByUsersStorageName(String usersStorageName) {
        return usersStorageRepository.findByUserStorageName(usersStorageName);
    }

    public List<UserStorage> getChildUserStorages(String id) {
        UserStorage userStorageDb = usersStorageRepository.findById(Long.parseLong(id)).get();
        Set<UserStorage> childUserStorage = usersStorageRepository.findById(Long.valueOf(id)).get().getChildUserStorages();
        return childUserStorage.stream().toList();
    }

    public StringBuffer getAllNameParentStorages(UserStorage parentUserStorage) {
        List<String> nameParentStorages = new LinkedList<>();
        UserStorage bufferUserStorage = parentUserStorage;
        nameParentStorages.add(bufferUserStorage.getUserStorageName());
        while (true){
            bufferUserStorage = getParentStorage(bufferUserStorage);
            if (bufferUserStorage == null){
                break;
            }
            if (bufferUserStorage.getUserStorageName().equals("-")){
                continue;
            }
            nameParentStorages.add(bufferUserStorage.getUserStorageName());
        }
        Collections.reverse(nameParentStorages);
        StringBuffer result = new StringBuffer();
        for (String storage : nameParentStorages) {
            if (result.length() > 0) {
                result.append("/"); // добавляем разделитель
            }
            result.append(storage);
        }
        return result;
    }

    private UserStorage getParentStorage(UserStorage parentUserStorage){
        return parentUserStorage.getParentUserStorage();

    }
}
