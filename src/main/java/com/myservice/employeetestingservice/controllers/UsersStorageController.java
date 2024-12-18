package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UsersStorage;
import com.myservice.employeetestingservice.dto.UsersStorageDTO;
import com.myservice.employeetestingservice.service.LogService;
import com.myservice.employeetestingservice.service.UserService;
import com.myservice.employeetestingservice.service.UsersStorageService;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usersStorage")
@Data
public class UsersStorageController {
    private final UsersStorageService usersStorageService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final LogService logService;

    // получение списка организаций/подразделений ----------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String getOrganizationPage(@AuthenticationPrincipal User userAdmin, Model model) {
        List<UsersStorage> usersStorages = usersStorageService.getAllUsersStorages();
        model.addAttribute("usersStorages", usersStorages);

        return "usersStoragesPage";
    }

    // добавление организации/подразделения ----------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String addUsersStorage(UsersStorageDTO usersStorageDTO,
                                  Model model,
                                  @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        List<UsersStorage> usersStorages = usersStorageService.getAllUsersStorages();
        if (usersStorageDTO.getUsersStorageName().isEmpty()){
            model.addAttribute("usersStorageNameError", "Поле не может быть пустым!");
            model.addAttribute("usersStorages", usersStorages);
            return Constants.USERS_STORAGE_PAGE;
        }
        UsersStorage usersStorage = modelMapper.map(usersStorageDTO, UsersStorage.class);
        if (usersStorageService.createUsersStorage(usersStorage, userAdmin, true)){
            model.addAttribute("usersStorageNameError", "Такое название уже существует!");
            model.addAttribute("usersStorage", usersStorageDTO);
            model.addAttribute("usersStorages", usersStorages);
            return Constants.USERS_STORAGE_PAGE;
        } else {
            logService.writeUserLog(userAdmin, "администратор добавил организацию/подразделение - " + usersStorageDTO.getUsersStorageName());
            return "redirect:/usersStorage";
        }
    }

    @PostMapping("/update/{idStorage}")
    @PreAuthorize(value = "hasAnyAuthority('MAIN_ADMIN', 'ADMIN')")
    public String updateUsersStorage(@PathVariable(required = false, name = "idStorage") String idStorage,
                                     UsersStorageDTO usersStorageDTO,
                                     Model model,
                                     @AuthenticationPrincipal User userAdmin) throws JsonProcessingException {
        List<UsersStorage> usersStorages = usersStorageService.getAllUsersStorages();
        if (usersStorageDTO.getUsersStorageName().isEmpty()) {
            model.addAttribute("usersStorageNewNameError", "Поле не может быть пустым!");
            model.addAttribute("openModalId", idStorage);
            model.addAttribute("usersStorages", usersStorages);
            return Constants.USERS_STORAGE_PAGE;
        }
        String id = idStorage.replaceAll("[^0-9]","");
        usersStorageDTO.setId(Long.parseLong(id));
        UsersStorage usersStorage = converteToUsersStorage(usersStorageDTO);
        if (usersStorageService.createUsersStorage(usersStorage, userAdmin, false)) {
            model.addAttribute("usersStorageNameError", "Такое название уже существует!");
            model.addAttribute("openModalId", idStorage);
            model.addAttribute("usersStorages", usersStorages);
            return Constants.USERS_STORAGE_PAGE;
        } else {
            logService.writeUserLog(userAdmin, "администратор обновил данные организации/подразделения - " + usersStorage.getUsersStorageName());
            return "redirect:/usersStorage";
        }
    }

    // удаление хранилища ----------------------------------------------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteUsersStorage(@PathVariable(required = false, name = "id") String idStorage,
            @AuthenticationPrincipal User userAuthentication) throws JsonProcessingException {
        String id = idStorage.replaceAll("[^0-9]","");
        usersStorageService.deleteUsersStorage(Long.parseLong(id), userAuthentication);
        return "redirect:/usersStorage";
    }

    //------------------------------------------------------------------------------------------------------------------
    private UsersStorageDTO converteToUsersStorageDTO(UsersStorage usersStorage){
        return modelMapper.map(usersStorage, UsersStorageDTO.class);
    }

    private UsersStorage converteToUsersStorage(UsersStorageDTO usersStorageDTO){
        return modelMapper.map(usersStorageDTO, UsersStorage.class);
    }
}
