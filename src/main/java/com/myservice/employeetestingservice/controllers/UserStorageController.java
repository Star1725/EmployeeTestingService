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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<UserStorage> userStorages = userStorageService.getAllUserStorages();

        // Фильтрация списка
        List<UserStorage> filteredUserStorages = userStorages.stream()
                .filter(userStorage -> !"-".equals(userStorage.getUserStorageName()))
                .filter(userStorage -> userStorage.getParentUserStorage().getUserStorageName().equals("-"))
                .collect(Collectors.toList());
        model.addAttribute("userStorages", filteredUserStorages);
        model.addAttribute("userStoragesForChoice", filteredUserStorages);

        return "userStoragesPage";
    }

    // получение списка конкретной организации/подразделения ----------------------------------------------------------------------
    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getChildUserStorages(@PathVariable(required = false, name = "id") String idStorage,
            @AuthenticationPrincipal User userAdmin, Model model) {
        String id = idStorage.replaceAll("[^0-9]","");
        UserStorage parentUserStorage = userStorageService.getUsersStorageRepository().getReferenceById(Long.valueOf(id));
        Set<UserStorage> userStorages = parentUserStorage.getChildUserStorages();
        String allParentStoragesNames = String.valueOf(userStorageService.getAllNameParentStorages(parentUserStorage));
        Set<UserStorage> userStoragesForChoice = new HashSet<>(parentUserStorage.getChildUserStorages());
        userStoragesForChoice.add(parentUserStorage);
        model.addAttribute("parentUserStorage", parentUserStorage);
        model.addAttribute("allParentStoragesNames", allParentStoragesNames);
        model.addAttribute("userStorages", userStorages);
        model.addAttribute("userStoragesForChoice", userStoragesForChoice);

        return "userStoragesPage";
    }

    // добавление организации/подразделения ----------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String addUsersStorage(UserStorageDTO userStorageDTO,
                                  Model model,
                                  @RequestParam(required = false) String userStorageParentNameSelected,
                                  @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        List<UserStorage> userStorages = userStorageService.getAllUserStorages();
        if (validationOfInputData(userStorageDTO, model, userStorages, null)){
            return Constants.USER_STORAGE_PAGE;
        }
        UserStorage userStorage = modelMapper.map(userStorageDTO, UserStorage.class);
        UserStorage userStorageParent = userStorageService.getUsersStorageByUsersStorageName(userStorageParentNameSelected);
        if (userStorageService.addUserStorage(userStorage, userAdmin, userStorageParent)){
            model.addAttribute("userStorageNameError", "Такое название уже существует!");
            model.addAttribute("userStorage", userStorageDTO);
            model.addAttribute("userStorages", userStorages);
            return Constants.USER_STORAGE_PAGE;
        }
        return "redirect:/userStorage";
    }

    private boolean validationOfInputData(UserStorageDTO userStorageDTO, Model model, List<UserStorage> userStorages, String idStorage) {
        if (userStorageDTO.getUserStorageName().isEmpty()){
            model.addAttribute("usersStorageNameError", "Поле не может быть пустым!");
            if (idStorage != null){
                model.addAttribute("openModalId", idStorage);
            }
            model.addAttribute("usersStorages", userStorages);
            return true;
        }
        return false;
    }


    @PostMapping("/update/{idStorage}")
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String updateUsersStorage(@PathVariable(required = false, name = "idStorage") String idStorage,
                                     UserStorageDTO userStorageDTO,
                                     Model model,
                                     @RequestParam(required = false) String userStorageParentNameSelected,
                                     @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        List<UserStorage> userStorages = userStorageService.getAllUserStorages();
        if (validationOfInputData(userStorageDTO, model, userStorages, idStorage)){
            return Constants.USER_STORAGE_PAGE;
        }
        String id = idStorage.replaceAll("[^0-9]","");
        userStorageDTO.setId(Long.parseLong(id));
        UserStorage userStorage = converteToUsersStorage(userStorageDTO);
        UserStorage userStorageParent = userStorageService.getUsersStorageByUsersStorageName(userStorageParentNameSelected);
        if (userStorageService.updateUserStorage(userStorage, userAdmin, userStorageParent)) {
            model.addAttribute("userStorageNameError", "Такое название уже существует!");
            model.addAttribute("openModalId", idStorage);
            model.addAttribute("userStorages", userStorages);
            return Constants.USER_STORAGE_PAGE;
        } else {
            return "redirect:/userStorage";
        }
    }

    // удаление хранилища ----------------------------------------------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteUsersStorage(@PathVariable(required = false, name = "id") String idStorage,
            @AuthenticationPrincipal User userAuthentication) throws JsonProcessingException {
        String id = idStorage.replaceAll("[^0-9]","");
        userStorageService.deleteUserStorage(Long.parseLong(id), userAuthentication);
        return "redirect:/userStorage";
    }

    //------------------------------------------------------------------------------------------------------------------
    private UserStorageDTO convertToUsersStorageDTO(UserStorage userStorage){
        return modelMapper.map(userStorage, UserStorageDTO.class);
    }

    private UserStorage converteToUsersStorage(UserStorageDTO userStorageDTO){
        return modelMapper.map(userStorageDTO, UserStorage.class);
    }
}
