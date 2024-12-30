package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.service.LogService;
import com.myservice.employeetestingservice.service.UserService;
import com.myservice.employeetestingservice.service.UserStorageService;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequestMapping("/userStorage")
@Data
public class UserStorageController {
    private final UserStorageService userStorageService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final LogService logService;

    // получение списка всех организаций/подразделений ----------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getAllUserStorages(@AuthenticationPrincipal User userAdmin, Model model) {
        Set<UserStorage> userStorages = userStorageService.getAllUserStoragesWithDefaultParent();
        model.addAttribute("userStorages", userStorages);
        return "userStoragesPage";
    }

    // получение списка конкретной организации/подразделения ----------------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getChildUserStorages(@PathVariable(required = false, name = "id") String idStorage,
                                       @AuthenticationPrincipal User userAdmin, Model model) {
        if (idStorage.equals("0")){
            return redirectUserStoragePage(null);
        }
        String id = idStorage.replaceAll("[^0-9]", "");
        UserStorage parentUserStorage = userStorageService.getUsersStorageRepository().getReferenceById(Long.valueOf(id));
        Set<UserStorage> userStorages = parentUserStorage.getChildUserStorages();
        String allParentStoragesNames = String.valueOf(userStorageService.getAllNameParentStorages(parentUserStorage));
        model.addAttribute("parentUserStorage", parentUserStorage);
        model.addAttribute("allParentStoragesNames", allParentStoragesNames);
        model.addAttribute("userStorages", userStorages);
        return "userStoragesPage";
    }

    // добавление организации/подразделения ----------------------------------------------------------------------------
    @PostMapping("/add")
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String addUsersStorage(UserStorageDTO userStorageDTO,
                                  Model model,
                                  @RequestParam(required = false) String userStorageParentNameSelected,
                                  @RequestParam(required = false) String idParentStorage,
                                  @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        //определяем в какую организацию либо внутреннее подразделение добавляем
        UserStorage parentUserStorage = userStorageService.determineWhichParentStorage(userStorageParentNameSelected, idParentStorage);

        //получение:
        Set<UserStorage> userStorages;                           //списка подчинённых подразделений для заполнения таблицы
        String allParentStoragesNames = "";                      //строки-дерева иерархии организации
        if (idParentStorage != null && !idParentStorage.isEmpty()){
            userStorages = parentUserStorage.getChildUserStorages();
            allParentStoragesNames = String.valueOf(userStorageService.getAllNameParentStorages(parentUserStorage));
        } else {
            userStorages = userStorageService.getAllUserStoragesWithDefaultParent();
        }
        //валидация входящих данных на пустое имя
        if (validationOfEmptyName(userStorageDTO, model, null)) {
            model.addAttribute("parentUserStorage", parentUserStorage);
            model.addAttribute("allParentStoragesNames", allParentStoragesNames);
            model.addAttribute("userStorages", userStorages);
            model.addAttribute("userStorage", userStorageDTO);
            return Constants.USER_STORAGE_PAGE;
        }

        UserStorage userStorage = convertToUsersStorage(userStorageDTO);
        if (userStorageService.addUserStorage(userStorage, userAdmin, parentUserStorage)) {
            model.addAttribute("userStorageNameError", "Такое название уже существует!");
            model.addAttribute("parentUserStorage", parentUserStorage);
            model.addAttribute("allParentStoragesNames", allParentStoragesNames);
            model.addAttribute("userStorages", userStorages);
            model.addAttribute("userStorage", userStorageDTO);
            return Constants.USER_STORAGE_PAGE;
        }
        return redirectUserStoragePage(parentUserStorage);
    }

    //обновление организации/подразделения -----------------------------------------------------------------------------
    @PostMapping("update/{idStorage}")
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String updateUsersStorage(@PathVariable(required = false, name = "idStorage") String idStorage,
                                     UserStorageDTO userStorageDTO,
                                     Model model,
                                     @RequestParam(required = false) String userStorageParentNameSelected,
                                     @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        //определяем для какой организации либо внутреннего подразделения обновляем
        UserStorage parentUserStorage = userStorageService.getUsersStorageByUsersStorageName(userStorageParentNameSelected);

        //получение:
        Set<UserStorage> userStorages;                           //списка подчинённых подразделений для заполнения таблицы
        String allParentStoragesNames = "";                      //строки-дерева иерархии организации
        if (parentUserStorage != null && !parentUserStorage.getUserStorageName().equals("-")){
            userStorages = parentUserStorage.getChildUserStorages();
            allParentStoragesNames = String.valueOf(userStorageService.getAllNameParentStorages(parentUserStorage));
        } else {
            userStorages = userStorageService.getAllUserStoragesWithDefaultParent();
        }
        
        String id = idStorage.replaceAll("[^0-9]", "");
        userStorageDTO.setId(Long.parseLong(id));
        UserStorage updatedUserStorage = convertToUsersStorage(userStorageDTO);
        
        //валидация пустого имени
        if (validationOfEmptyName(userStorageDTO, model, idStorage)) {
            model.addAttribute("parentUserStorage", parentUserStorage);
            model.addAttribute("allParentStoragesNames", allParentStoragesNames);
            model.addAttribute("userStorages", userStorages);
            return Constants.USER_STORAGE_PAGE;
        }
        
        if (userStorageService.updateUserStorage(updatedUserStorage, userAdmin, parentUserStorage)) {
            model.addAttribute("userStorageNameUpdateError", "Такое название уже существует!");
            model.addAttribute("openModalId", idStorage);
            if (parentUserStorage.getChildUserStorages().stream().anyMatch(storage -> storage.getId().equals(Long.parseLong(id)))){
                model.addAttribute("parentUserStorage", parentUserStorage);
            } else {
                allParentStoragesNames = String.valueOf(userStorageService.getAllNameParentStorages(parentUserStorage.getParentUserStorage()));
                UserStorage grandParentUserStorage = parentUserStorage.getParentUserStorage();
                userStorages = grandParentUserStorage.getChildUserStorages();
                model.addAttribute("parentUserStorage", parentUserStorage.getParentUserStorage());
            }
            model.addAttribute("allParentStoragesNames", allParentStoragesNames);
            model.addAttribute("userStorages", userStorages);
            model.addAttribute("updatedUserStorage", updatedUserStorage);
            return Constants.USER_STORAGE_PAGE;
        }
        return redirectUserStoragePage(parentUserStorage);
    }

    // удаление организации/подразделения ------------------------------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("delete/{id}")
    public String deleteUsersStorage(@PathVariable(required = false, name = "id") String idStorage,
                                     @AuthenticationPrincipal User userAuthentication) throws JsonProcessingException {
        String id = idStorage.replaceAll("[^0-9]", "");
        String idParentStorage = userStorageService.deleteUserStorage(Long.parseLong(id), userAuthentication);
        return "redirect:/userStorage" + "/" + idParentStorage;
    }

    //------------------------------------------------------------------------------------------------------------------
    private UserStorageDTO convertToUsersStorageDTO(UserStorage userStorage) {
        return modelMapper.map(userStorage, UserStorageDTO.class);
    }

    private UserStorage convertToUsersStorage(UserStorageDTO userStorageDTO) {
        return modelMapper.map(userStorageDTO, UserStorage.class);
    }

    private String redirectUserStoragePage(UserStorage userStorageParent) {
        if (userStorageParent != null && !userStorageParent.getUserStorageName().equals("-")) {
            return "redirect:/userStorage/" + userStorageParent.getId();
        } else {
            return "redirect:/userStorage";
        }
    }

    private boolean validationOfEmptyName(UserStorageDTO userStorageDTO, Model model, String idStorage) {
        if (userStorageDTO.getUserStorageName().isEmpty()) {
            if (idStorage != null) {
                model.addAttribute("openModalId", idStorage);
                model.addAttribute("userStorageNameUpdateError", "Поле не может быть пустым!");
            } else {
                model.addAttribute("userStorageNameError", "Поле не может быть пустым!");
            }
            return true;
        }
        return false;
    }
}
