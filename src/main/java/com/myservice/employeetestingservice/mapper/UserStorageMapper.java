package com.myservice.employeetestingservice.mapper;

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
    public UserStorageDTO convertToDTOForProfile(UserStorage userStorage) {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        if (userStorage != null){
            userStorageDTO = convertToDTO(userStorage);
        }

        //добавление списка всех первичных родительских хранилищ (организаций)
        Set<UserStorageDTO> primaryParentStorages = new java.util.HashSet<>(Set.of());
        for (UserStorage storage : userStorageService.getAllUserStoragesWithDefaultParent()) {
            primaryParentStorages.add(convertToDTO(storage));
        }
        userStorageDTO.setAllPrimaryParentStorages(primaryParentStorages);
        userStorageDTO.setDefaultPrimaryParentStorage(convertToDTO(userStorageService.getDefaultUserStorage()));

        if (userStorage != null){
            //добавление первичного родительского хранилища (организации)
            UserStorage primaryUserStorage = userStorageService.getPrimaryUserStorage(userStorage);
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
