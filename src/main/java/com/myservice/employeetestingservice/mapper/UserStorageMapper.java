package com.myservice.employeetestingservice.mapper;

import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.service.UserStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserStorageMapper {
    private final UserStorageService userStorageService;

    //конвертация хранилища пользователей для отображения на Profile.ftl
    public UserStorageDTO convertToDTOForProfile(UserStorage userStorageDb, User userAuthentication) {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        if (userStorageDb != null){
            userStorageDTO = convertToDTO(userStorageDb);
        }

        //Далее заполнение полей для UserStorageDTO будет определяться ролью userAuthentication
        Set<UserStorageDTO> primaryParentStorages = new java.util.HashSet<>(Set.of());
        //Для MAIN_ADMIN или если userStorageDb == null, добавляем список всех первичных родительских хранилищ (организаций)
        if (userAuthentication != null && userAuthentication.isMainAdmin() || userStorageDb == null) {
            for (UserStorage storage : userStorageService.getAllUserStoragesWithDefaultParent()) {
                primaryParentStorages.add(convertToDTO(storage));
            }
        //Для ADMIN добавляем в список всех первичных родительских хранилищ (организаций) только первичное родительское хранилище дял ADMIN
        } else if (userAuthentication != null && userAuthentication.isAdmin()){
            UserStorage primaryUserStorage = userStorageService.getPrimaryUserStorage(userAuthentication.getUserStorage());
            primaryParentStorages.add(convertToDTO(primaryUserStorage));
        }
        userStorageDTO.setAllPrimaryParentStorages(primaryParentStorages);
        userStorageDTO.setDefaultPrimaryParentStorage(convertToDTO(userStorageService.getDefaultUserStorage()));

        if (userStorageDb != null){
            //добавление первичного родительского хранилища (организации)
            UserStorage primaryUserStorage = userStorageService.getPrimaryUserStorage(userStorageDb);
            userStorageDTO.setPrimaryParentStorage(convertToDTO(primaryUserStorage));
            //добавление списка всех дочерних подразделений первичного родительского хранилища (организации)
            Set<UserStorageDTO> allChildStoragesForPrimaryParent = new java.util.HashSet<>(Set.of());
            for (UserStorage storage : UserStorage.getAllNestedChildUserStorages(primaryUserStorage)) {
                allChildStoragesForPrimaryParent.add(convertToDTO(storage));
            }
            userStorageDTO.setAllChildStoragesForPrimaryParent(allChildStoragesForPrimaryParent);
        }

        return userStorageDTO;
    }

    public UserStorageDTO convertToDTO(UserStorage userStorage) {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        if (userStorage != null){
            userStorageDTO.setId(userStorage.getId());
            userStorageDTO.setUserStorageName(userStorage.getUserStorageName());
            userStorageDTO.setFullUserStorageName(String.valueOf(userStorageService.getNameParentStorages(userStorage)));
        }
        return userStorageDTO;
    }
}
