package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.Role;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.repository.UserStorageRepository;
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
    private final UserStorageRepository userStorageRepository;
    private final LogService logService;
    private final ServiceWorkingUserAndStorage serviceWorkingUserAndStorage;

    //GET (Получение)---------------------------------------------------------------------------------------------------
    //Получение хранилища по ID
    public UserStorage getUserStorageById(long id) {
        return userStorageRepository.getReferenceById(id);
    }

    //Получение всех хранилищ 1-го уровня(дочерних по отношению к дефолтной)
    public Set<UserStorage> getAllUserStoragesWithDefaultParent() {
        return userStorageRepository.findAll().stream()
                //отфильтровываем дефолтную организацию
                .filter(userStorage -> !"-".equals(userStorage.getUserStorageName()))
                //оставляем только родительские организации верхнего уровня (дочерние организации для дефолтной)
                .filter(userStorage -> userStorage.getParentUserStorage().getUserStorageName().equals("-"))
                .collect(Collectors.toSet());
    }

    //Получение хранилища по имени
    public UserStorage getUserStorageByUsersStorageName(String userStorageName) {
        return userStorageRepository.findByUserStorageName(userStorageName);
    }

    //Получение полного имени хранилища с указанием его родительских подразделений через "/"
    public StringBuilder getNameParentStorages(final UserStorage parentUserStorage) {
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

    //Получение Primary хранилища для дочернего хранилища
    public UserStorage getPrimaryUserStorage (UserStorage userStorage){
        UserStorage bufferUserStorage = userStorage;
        while (bufferUserStorage.getParentUserStorage() != null) {
            if (bufferUserStorage.getParentUserStorage().getUserStorageName().equals("-")) {
                break;
            }
            bufferUserStorage = bufferUserStorage.getParentUserStorage();
        }
        return bufferUserStorage;
    }

    //Получение организации либо внутреннего подразделения, куда добавляем дочернее подразделение
    public UserStorage determineWhichParentStorage(String userStorageParentNameSelected, String idParentStorage) {
        UserStorage userStorageParent = null;
        if (idParentStorage != null && !idParentStorage.isEmpty()){
            String id = idParentStorage.replaceAll("\\D", "");
            UserStorage userStorageUpParent = getUserStorageRepository().getReferenceById(Long.valueOf(id));
            if (!userStorageUpParent.getUserStorageName().equals(userStorageParentNameSelected)){
                userStorageParent = userStorageUpParent.getChildUserStorages().stream().filter(userStorage1 ->
                        userStorage1.getUserStorageName().equals(userStorageParentNameSelected)).findAny().get();
            } else {
                userStorageParent = userStorageUpParent;
            }
        } else {
            userStorageParent = getUserStorageByUsersStorageName(userStorageParentNameSelected);
        }
        return userStorageParent;
    }

    //Post (Добавление/Обновление)--------------------------------------------------------------------------------------
    //Добавление хранилища
    public boolean addUserStorage(UserStorage userStorage, User userAdmin, UserStorage userStorageParent) throws JsonProcessingException {
        //проверяем БД на наличие хранилища с таким же именем
        if (userStorageParent != null){
            if (userStorageParent.getChildUserStorages().stream().anyMatch(userStorage1 -> userStorage1.getUserStorageName().equals(userStorage.getUserStorageName()))){
                return true;
            }
        } else {
            UserStorage userStorageDb = userStorageRepository.findByUserStorageName(userStorage.getUserStorageName());
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
            userStorageRepository.save(userStorageParent);
        } else {
            //устанавливаем для создаваемого хранилища родительское хранилище по умолчанию
            UserStorage userStorageDefault = userStorageRepository.getReferenceById(0L);
            userStorage.setParentUserStorage(userStorageDefault);
            userStorageDefault.setParentStorage(true);
            userStorage.setChildStorage(true);
            userStorageDefault.getChildUserStorages().add(userStorage);
            logService.writeStorageLog(userStorage, ": организации/подразделения создано администратором - \"" + userAdmin.getUsername() + "\"");
            logService.writeUserLog(userAdmin, "администратор добавил организацию/подразделение - \"" + userStorage.getUserStorageName() + "\"");
            userStorageRepository.save(userStorageDefault);
        }
        return false;
    }

    //Обновление хранилища
    public boolean updateUserStorage(UserStorage updatedUserStorage, User userAdmin, UserStorage userStorageParent) throws JsonProcessingException {
        //проверяем БД на наличие хранилища с таким же именем
        UserStorage userStorageDb = userStorageRepository.getReferenceById(updatedUserStorage.getId());
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
        userStorageRepository.save(userStorageParent);
        updatedUserStorage.setDateChanged(time);
        userStorageRepository.save(userStorageDb);
        return false;
    }

    //DELETE (Удаление)-------------------------------------------------------------------------------------------------
    public String deleteUserStorage(long id, User userAuthentication) throws JsonProcessingException {
        UserStorage userStorageDb = userStorageRepository.getReferenceById(id);
        long idParentStorage = 0;
        UserStorage parentUserStorage = userStorageRepository.getReferenceById(idParentStorage);//по умолчанию parentUserStorage - самое верхнее корневое хранилище (DefaultUserStorage)
        // Если это родительское хранилище и у него есть вышестоящее родительское хранилище, то ...
        if (userStorageDb.isParentStorage() && userStorageDb.getParentUserStorage() != null) {
            parentUserStorage = userStorageDb.getParentUserStorage();
            Set<UserStorage> childUserStorages = new HashSet<>(userStorageDb.getChildUserStorages());
            for (UserStorage child : childUserStorages) {
                child.setParentUserStorage(parentUserStorage);
                parentUserStorage.getChildUserStorages().add(child);//переназначить дочерние элементы выше стоящему хранилищу
                child.setUserStorageName(child.getUserStorageName() + "(из состава удаленной организации/подразделения - " + userStorageDb.getUserStorageName() + ")");
//                child.setParentStorage(true);
                userStorageRepository.save(child); // Сохраняем обновление
                logService.writeStorageLog(child, "Родительское хранилище изменено на вышестоящее для удалённого - \"" + userStorageDb.getUserStorageName() + "\"");
            }
            userStorageRepository.save(parentUserStorage);
            userStorageDb.getChildUserStorages().clear(); // Удаляем ссылки на дочерние элементы
        }

        // Если это дочернее хранилище, то ...
        if (userStorageDb.isChildStorage()) {
            parentUserStorage = userStorageDb.getParentUserStorage();
            if (parentUserStorage != null) {
                parentUserStorage.getChildUserStorages().remove(userStorageDb);//удалить данное хранилище из списка дочерних хранилищ родительского хранилища
                if (parentUserStorage.getChildUserStorages().size() == 0) {
                    parentUserStorage.setParentStorage(false);
                }
                userStorageRepository.save(parentUserStorage); // Сохраняем изменения в БД для родительского хранилища
                logService.writeStorageLog(parentUserStorage, "Удалено дочернее подразделение - \"" + userStorageDb.getUserStorageName() + "\"");
                idParentStorage = parentUserStorage.getId();
            }
            userStorageDb.setParentUserStorage(null);//обнулить ссылку на его родительское хранилище
            userStorageRepository.save(userStorageDb);//сохраняем изменения в БД для данного хранилища
        }

        //если в хранилище были пользователи, то ...
        if (userStorageDb.getStorageUsers() != null && !userStorageDb.getStorageUsers().isEmpty()){
            Set<User> storageUsers = new HashSet<>(userStorageDb.getStorageUsers());
            for (User user: storageUsers) {
                user.setUserStorage(parentUserStorage);//указываем для них родительское хранилище
                if (user.isAdmin()){
                    user.getRoles().remove(Role.ADMIN);
                }
                serviceWorkingUserAndStorage.saveUser(user);//сохраняем изменения в БД для пользователей
                parentUserStorage.getStorageUsers().add(user);//в список пользователей родительского хранилища добавляем новых пользователей
            }
            userStorageRepository.save(parentUserStorage); // Сохраняем изменения в БД для родительского хранилища
            userStorageDb.getStorageUsers().clear();
        }

        //если у хранилища был Administrator, то понижаем его роль до User, и удаляем из хранилища ссылку на Administrator
        if (userStorageDb.getAdministrator() != null){
            User admin = userStorageDb.getAdministrator();
            List<Role> roles = admin.getRoles();
            roles.remove(Role.ADMIN);
            admin.setRoles(roles);
//            getUserService().saveUser(admin);
            serviceWorkingUserAndStorage.saveUser(admin);
            userStorageDb.setAdministrator(null);
        }

        logService.writeUserLog(userAuthentication, "Администратор удалил хранилище - \"" + userStorageDb.getUserStorageName() + "\"");
        userStorageRepository.delete(userStorageDb); // Удаляем сам элемент
        return String.valueOf(idParentStorage);
    }

    //добавление пользователя в хранилище
    public void addUserForStorage(UserStorage userStorage, User user, User userAuthentication) throws JsonProcessingException {
        if (userStorage != null){
            Set<User> storageUsers = userStorage.getStorageUsers();
            if (storageUsers != null) {
                //если пользователь администратор, и поле администратора для хранилища пустое
                if (user.isAdmin() && userStorage.getAdministrator() == null) {
                    userStorage.setAdministrator(user);
                }
                storageUsers.add(user);
            } else {
                userStorage.setStorageUsers(new HashSet<>());
                userStorage.getStorageUsers().add(user);
            }
            if (userAuthentication != null){
                logService.writeUserLog(userAuthentication, "Администратор добавил пользователя \"" + user.getUsername() + "\" в хранилище - \"" + userStorage.getUserStorageName() + "\"");
            } else {
                logService.writeUserLog(user, "Пользователь добавил себя в хранилище - \"" + userStorage.getUserStorageName() + "\"");
            }
            userStorageRepository.save(userStorage);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

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

    public UserStorage getDefaultUserStorage() {
        return getUserStorageById(0);
    }
}
