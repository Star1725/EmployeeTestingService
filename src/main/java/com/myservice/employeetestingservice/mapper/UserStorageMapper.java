package com.myservice.employeetestingservice.mapper;

import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.service.UserStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserStorageMapper {
    private final UserStorageService userStorageService;

    //конвертация хранилища пользователей для отображения на Profile.ftl
    public UserStorageDTO convertToDTOForProfilePage(UserStorage userStorageDb, User userAuthentication) {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        if (userStorageDb != null){
            userStorageDTO = convertToDTOForUserStorageListPage(userStorageDb);
        }

        //Далее заполнение полей для UserStorageDTO будет определяться ролью userAuthentication
        Set<UserStorageDTO> primaryParentStorages = new java.util.HashSet<>(Set.of());
        //Для MAIN_ADMIN или если userStorageDb == null, добавляем список всех первичных родительских хранилищ (организаций)
        if (userAuthentication != null && userAuthentication.isMainAdmin() || userStorageDb == null) {
//            for (UserStorage storage : userStorageService.getAllUserStoragesWithDefaultParent()) {
            for (UserStorage storage : userStorageService.getTopLevelStorages()) {
                primaryParentStorages.add(convertToDTOForUserStorageListPage(storage));
            }
        //Для ADMIN добавляем в список всех первичных родительских хранилищ (организаций) только первичное родительское хранилище дял ADMIN
        } else if (userAuthentication != null && userAuthentication.isAdmin()){
            UserStorage primaryUserStorage = userStorageService.getPrimaryUserStorage(userAuthentication.getUserStorage());
            primaryParentStorages.add(convertToDTOForUserStorageListPage(primaryUserStorage));
        }
        userStorageDTO.setAllPrimaryParentStorages(primaryParentStorages);
        userStorageDTO.setDefaultPrimaryParentStorage(convertToDTOForUserStorageListPage(userStorageService.getDefaultUserStorage()));

        if (userStorageDb != null){
            //добавление первичного родительского хранилища (организации)
            UserStorage primaryUserStorage = userStorageService.getPrimaryUserStorage(userStorageDb);
            userStorageDTO.setPrimaryParentStorage(convertToDTOForUserStorageListPage(primaryUserStorage));
            //добавление списка всех дочерних подразделений первичного родительского хранилища (организации)
            Set<UserStorageDTO> allChildStoragesForPrimaryParent = new java.util.HashSet<>(Set.of());
            for (UserStorage storage : UserStorage.getAllNestedChildUserStorages(primaryUserStorage)) {
                allChildStoragesForPrimaryParent.add(convertToDTOForUserStorageListPage(storage));
            }
            userStorageDTO.setAllChildStoragesForPrimaryParent(allChildStoragesForPrimaryParent);
        }

        return userStorageDTO;
    }

    public UserStorageDTO convertToDTOForUserStorageListPage(UserStorage userStorage) {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        if (userStorage != null){
            userStorageDTO.setId(userStorage.getId());
            userStorageDTO.setUserStorageName(userStorage.getUserStorageName());
            userStorageDTO.setFullUserStorageName(String.valueOf(userStorageService.getParentStorageNames(userStorage)));
            if (userStorage.isChildStorage()){
                userStorageDTO.setChildStorage(true);
                UserStorage parentUserStorage = userStorage.getParentUserStorage();
                userStorageDTO.setParentUserStorage(convertToDTOForUserStorageListPage(parentUserStorage));
            }
            userStorageDTO.setAdministrator(userStorage.getAdministrator());
            userStorageDTO.setStorageUsers(userStorage.getAllNestedStorageUsers(userStorage));
            Set<UserStorageDTO> childStorages = new HashSet<>();
            for (UserStorage childStorage : userStorage.getChildUserStorages()) {
                UserStorageDTO storageDTO = new UserStorageDTO();
                storageDTO.setId(childStorage.getId());
                storageDTO.setUserStorageName(childStorage.getUserStorageName());
                storageDTO.setParentStorage(childStorage.isParentStorage());
                storageDTO.setStorageUsers(childStorage.getAllNestedStorageUsers(childStorage));
                storageDTO.setAdministrator(childStorage.getAdministrator());
                childStorages.add(storageDTO);
            }
            userStorageDTO.setChildStorages(childStorages);
        }
        return userStorageDTO;
    }

    public UserStorageDTO convertToDTO(UserStorage userStorage) {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        if (userStorage != null){
            userStorageDTO.setId(userStorage.getId());
            userStorageDTO.setUserStorageName(userStorage.getUserStorageName());
            userStorageDTO.setFullUserStorageName(String.valueOf(userStorageService.getParentStorageNames(userStorage)));
        }
        return userStorageDTO;
    }

    public UserStorage convertToEntity(UserStorageDTO userStorageDTO) {
        UserStorage userStorage = new UserStorage();
        userStorage.setId(userStorageDTO.getId());
        userStorage.setUserStorageName(userStorageDTO.getUserStorageName().trim());
        userStorage.setStorageDescription(userStorageDTO.getFullUserStorageName());

        return userStorage;
    }
}
