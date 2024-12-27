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
        if (userStorageDb != null && !Objects.equals(userStorageDb.getId(), userStorage.getId())) {
            return true;
        }
        LocalDateTime time = LocalDateTime.now();
        userStorage.setChangedUser(userAdmin);
        if (userStorageParent != null){
            userStorageParent.getChildUserStorages().add(userStorage);
            userStorageParent.setParentStorage(true);
            userStorage.setParentUserStorage(userStorageParent);
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

    public String deleteUserStorage(long id, User userAuthentication) throws JsonProcessingException {
        UserStorage userStorageDb = usersStorageRepository.findById(id).get();
        String idParentStorage = String.valueOf(0);
        // Если это родительское хранилище, переназначить дочерние элементы
        if (userStorageDb.isParentStorage()) {
            Set<UserStorage> childUserStorages = new HashSet<>(userStorageDb.getChildUserStorages());
            for (UserStorage child : childUserStorages) {
                child.setParentUserStorage(usersStorageRepository.getReferenceById(0L));
                child.setUserStorageName(child.getUserStorageName() + "(из состава удаленной организации/подразделения - " + userStorageDb.getUserStorageName() + ")");
                child.setParentStorage(true);
                usersStorageRepository.save(child); // Сохраняем обновление
                logService.writeStorageLog(child, "Родительское хранилище изменено на корневое после удаления - \"" + userStorageDb.getUserStorageName() + "\"");
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
