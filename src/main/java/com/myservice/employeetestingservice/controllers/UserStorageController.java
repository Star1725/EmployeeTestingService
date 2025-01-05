package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.mapper.UserStorageMapper;
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

import java.util.List;
import java.util.Set;

import static com.myservice.employeetestingservice.controllers.Constants.*;

@Controller
@RequestMapping("/userStorage")
@Data
public class UserStorageController {
    private final UserStorageService userStorageService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final UserStorageMapper userStorageMapper;
    private final LogService logService;

    // получение списка всех организаций/подразделений ----------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getAllUserStorages(@AuthenticationPrincipal User userAdmin, Model model) {
        Set<UserStorage> userStorages = userStorageService.getAllUserStoragesWithDefaultParent();
        model.addAttribute(USER_STORAGES, userStorages);
        return "userStoragesList";
    }

    // получение списка конкретной организации/подразделения ----------------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getChildUserStorages(@PathVariable(required = false, name = "id") String idStorage,
                                       @AuthenticationPrincipal User userAdmin, Model model) {
        if (idStorage.equals("0")){
            return redirectUserStoragePage(null);
        }
        String id = idStorage.replaceAll("\\D", "");
        UserStorage parentUserStorage = userStorageService.getUserStorageRepository().getReferenceById(Long.valueOf(id));
        Set<UserStorage> userStorages = parentUserStorage.getChildUserStorages();
        String allParentStoragesNames = String.valueOf(userStorageService.getNameParentStorages(parentUserStorage));
        model.addAttribute(PARENT_USER_STORAGE, parentUserStorage);
        model.addAttribute(ALL_PARENT_STORAGES_NAMES, allParentStoragesNames);
        model.addAttribute(USER_STORAGES, userStorages);
        return USER_STORAGES_LIST;
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
            allParentStoragesNames = String.valueOf(userStorageService.getNameParentStorages(parentUserStorage));
        } else {
            userStorages = userStorageService.getAllUserStoragesWithDefaultParent();
        }
        //валидация входящих данных на пустое имя
        if (validationOfEmptyName(userStorageDTO, model, null)) {
            model.addAttribute(PARENT_USER_STORAGE, parentUserStorage);
            model.addAttribute(ALL_PARENT_STORAGES_NAMES, allParentStoragesNames);
            model.addAttribute(USER_STORAGES, userStorages);
            model.addAttribute("userStorage", userStorageDTO);
            return Constants.USER_STORAGES_LIST;
        }

        UserStorage userStorage = convertToUsersStorage(userStorageDTO);
        if (userStorageService.addUserStorage(userStorage, userAdmin, parentUserStorage)) {
            model.addAttribute("userStorageNameError", "Такое название уже существует!");
            model.addAttribute(PARENT_USER_STORAGE, parentUserStorage);
            model.addAttribute(ALL_PARENT_STORAGES_NAMES, allParentStoragesNames);
            model.addAttribute(USER_STORAGES, userStorages);
            model.addAttribute("userStorage", userStorageDTO);
            return Constants.USER_STORAGES_LIST;
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
        UserStorage parentUserStorage = userStorageService.getUserStorageByUsersStorageName(userStorageParentNameSelected);

        //получение:
        Set<UserStorage> userStorages;                           //списка подчинённых подразделений для заполнения таблицы
        String allParentStoragesNames = "";                      //строки-дерева иерархии организации
        if (parentUserStorage != null && !parentUserStorage.getUserStorageName().equals("-")){
            userStorages = parentUserStorage.getChildUserStorages();
            allParentStoragesNames = String.valueOf(userStorageService.getNameParentStorages(parentUserStorage));
        } else {
            userStorages = userStorageService.getAllUserStoragesWithDefaultParent();
        }
        
        String id = idStorage.replaceAll("\\D", "");
        userStorageDTO.setId(Long.parseLong(id));
        UserStorage updatedUserStorage = convertToUsersStorage(userStorageDTO);
        
        //валидация пустого имени
        if (validationOfEmptyName(userStorageDTO, model, idStorage)) {
            model.addAttribute(PARENT_USER_STORAGE, parentUserStorage);
            model.addAttribute(ALL_PARENT_STORAGES_NAMES, allParentStoragesNames);
            model.addAttribute(USER_STORAGES, userStorages);
            return Constants.USER_STORAGES_LIST;
        }
        
        if (userStorageService.updateUserStorage(updatedUserStorage, userAdmin, parentUserStorage)) {
            model.addAttribute("userStorageNameUpdateError", "Такое название уже существует!");
            model.addAttribute("openModalId", idStorage);

            if (parentUserStorage.getChildUserStorages().stream().anyMatch(storage -> storage.getId().equals(Long.parseLong(id)))){
                model.addAttribute(PARENT_USER_STORAGE, parentUserStorage);
            } else {
                allParentStoragesNames = String.valueOf(userStorageService.getNameParentStorages(parentUserStorage.getParentUserStorage()));
                UserStorage grandParentUserStorage = parentUserStorage.getParentUserStorage();
                userStorages = grandParentUserStorage.getChildUserStorages();
                model.addAttribute(PARENT_USER_STORAGE, parentUserStorage.getParentUserStorage());
            }
            model.addAttribute(ALL_PARENT_STORAGES_NAMES, allParentStoragesNames);
            model.addAttribute(USER_STORAGES, userStorages);
            model.addAttribute("updatedUserStorage", updatedUserStorage);
            return USER_STORAGES_LIST;
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

    //получение дочерних подразделений для организации подразделения с id
    @GetMapping("/{id}/childStorages")
    @ResponseBody
    public List<UserStorageDTO> getChildStorages(@PathVariable Long id) {
        UserStorage parentStorage = userStorageService.getUserStorageRepository().getReferenceById(id);
        Set<UserStorage> childStorages = UserStorage.getAllNestedChildUserStorages(parentStorage);
        childStorages.add(parentStorage);
        return childStorages.stream()
                .map(userStorageMapper::convertToDTO)
                .toList();
    }

    //------------------------------------------------------------------------------------------------------------------
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
