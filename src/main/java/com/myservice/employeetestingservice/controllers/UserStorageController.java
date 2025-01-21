package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.mapper.UserStorageMapper;
import com.myservice.employeetestingservice.service.UserStorageService;
import com.myservice.employeetestingservice.service.ValidationService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.myservice.employeetestingservice.controllers.Constants.PARENT_USER_STORAGE;
import static com.myservice.employeetestingservice.controllers.Constants.USER_STORAGES_LIST;

@Controller
@RequestMapping("/userStorage")
@Data
public class UserStorageController {
    private final UserStorageService userStorageService;
    private final UserStorageMapper userStorageMapper;
    private final ValidationService validationService;

    // получение списка всех организаций/подразделений ----------------------------------------------------------------- +
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getAllUserStorages(@AuthenticationPrincipal User admin, Model model) {
        UserStorage parentUserStorage = userStorageService.getDefaultUserStorage();
        UserStorageDTO parentUserStorageDTO = userStorageMapper.convertToDTOForUserStorageListPage(parentUserStorage);
        model.addAttribute(PARENT_USER_STORAGE, parentUserStorageDTO);
        return USER_STORAGES_LIST;
    }

    // получение списка дочерних хранилищ конкретной организации/подразделения -----------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getChildUserStorages(@PathVariable Long id,
                                       @AuthenticationPrincipal User userAdmin, Model model) {
        UserStorage parentUserStorage = userStorageService.getStorageById(id);
        UserStorageDTO parentUserStorageDTO = userStorageMapper.convertToDTOForUserStorageListPage(parentUserStorage);
        model.addAttribute(PARENT_USER_STORAGE, parentUserStorageDTO);
        return USER_STORAGES_LIST;
    }

    // добавление организации/подразделения ----------------------------------------------------------------------------
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String addUserStorage(@ModelAttribute UserStorageDTO newUserStorageDTO,
                                 Model model,
                                 @RequestParam(required = false) String userStorageParentNameSelected,
                                 @RequestParam(required = false) String idParentStorage,
                                 @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        // Определение родительского хранилища
        UserStorage parentUserStorage = userStorageService.determineParentStorage(userStorageParentNameSelected, idParentStorage);
        UserStorageDTO parentUserStorageDTO = userStorageMapper.convertToDTOForUserStorageListPage(parentUserStorage);
        // Валидация входящих данных на пустое имя
        if (validationService.isNotValidateStorageName(newUserStorageDTO, parentUserStorageDTO, model, false)) {
            return Constants.USER_STORAGES_LIST;
        }
        // Создание нового хранилища
        UserStorage newUserStorage = userStorageMapper.convertToEntity(newUserStorageDTO);
        boolean isDuplicate = userStorageService.addUserStorage(newUserStorage, userAdmin, parentUserStorage);
        // Валидация входящих данных на дублирование имени в рамках родительского хранилища
        if (validationService.isNotValidateStorageName(newUserStorageDTO, parentUserStorageDTO, model, isDuplicate)) {
            return Constants.USER_STORAGES_LIST;
        }
        return redirectUserStoragePage(parentUserStorage);
    }

    //обновление организации/подразделения -----------------------------------------------------------------------------
    @PostMapping("update/{idStorage}")
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String updateUsersStorage(@PathVariable(required = false, name = "idStorage") String idStorage,
                                     UserStorageDTO updateUserStorageDTO,
                                     Model model,
                                     @RequestParam(required = false) String userStorageParentNameSelected,
                                     @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        //определяем для какой организации либо внутреннего подразделения обновляем
        UserStorage parentUserStorage = userStorageService.determineParentStorage(userStorageParentNameSelected, null);
        UserStorageDTO parentUserStorageDTO = userStorageMapper.convertToDTOForUserStorageListPage(parentUserStorage);
        String id = idStorage.replaceAll("\\D", "");
        updateUserStorageDTO.setId(Long.parseLong(id));
        // Валидация входящих данных на пустое имя
        if (validationService.isNotValidateStorageNameForModal(updateUserStorageDTO, parentUserStorageDTO, model, idStorage, false)) {
            return Constants.USER_STORAGES_LIST;
        }
        UserStorage updatedUserStorage = userStorageMapper.convertToEntity(updateUserStorageDTO);

        boolean isDuplicate = userStorageService.updateUserStorage(updatedUserStorage, userAdmin, parentUserStorage);
        // Валидация входящих данных на дублирование имени в рамках родительского хранилища
        if (validationService.isNotValidateStorageNameForModal(updateUserStorageDTO, parentUserStorageDTO, model, idStorage, isDuplicate)) {
            return Constants.USER_STORAGES_LIST;
        }
        return redirectUserStoragePage(parentUserStorage);
    }

    // удаление организации/подразделения ------------------------------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("delete/{id}")
    public String deleteUsersStorage(@PathVariable(required = false, name = "id") String idStorage,
                                     @AuthenticationPrincipal User userAuthentication) throws JsonProcessingException {
        String id = idStorage.replaceAll("\\D", "");
        String idParentStorage = userStorageService.deleteUserStorage(Long.parseLong(id), userAuthentication);
        return "redirect:/userStorage" + "/" + idParentStorage;
    }

//**********************************************************************************************************************
    //получение дочерних хранилищ для хранилища с id (для асинхронной загрузки из javascript)
    @GetMapping("/{id}/childStorages")
    @ResponseBody
    public List<UserStorageDTO> getChildStorages(@PathVariable Long id) {
        UserStorage parentStorage = userStorageService.getUserStorageRepository().getReferenceById(id);
        Set<UserStorage> childStorages = UserStorage.getAllNestedChildUserStorages(parentStorage);
        childStorages.add(parentStorage);
        return childStorages.stream()
                .map(userStorageMapper::convertToDTOForUserStorageListPage)
                .toList();
    }

    private String redirectUserStoragePage(UserStorage userStorageParent) {
        if (userStorageParent != null && !userStorageParent.getUserStorageName().equals("-")) {
            return "redirect:/userStorage/" + userStorageParent.getId();
        } else {
            return "redirect:/userStorage";
        }
    }
}
