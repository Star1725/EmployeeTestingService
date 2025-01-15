package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.*;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.mapper.UserMapper;
import com.myservice.employeetestingservice.mapper.UserStorageMapper;
import com.myservice.employeetestingservice.service.UserService;
import com.myservice.employeetestingservice.service.UserStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserStorageMapper userStorageMapper;
    private final UserStorageService userStorageService;

    // получение списка пользователей с учётом профиля администратора --------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping
    public String getAllUsers(@AuthenticationPrincipal User adminUser, Model model) {
        List<UserDTO> users = userService.getAllUsersForRoleAdmin(adminUser);

        model.addAttribute("users", users);
        return "usersList";
    }

    // получение списка пользователей для конкретного хранилища --------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("/storage/{id}")
    public String getUsersByStorage(@PathVariable Long id, Model model) {
        UserStorage userStorageDb = userStorageService.getUserStorageById(id);
        List<UserDTO> users = userService.getUsersByStorageId(userStorageDb);

        model.addAttribute("users", users);
        return "usersList";
    }

    // удаление пользователя -------------------------------------------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteUser(
            @AuthenticationPrincipal User userAuthentication,
            @PathVariable(required = false) int id) throws JsonProcessingException {
        User userFromDb = userService.getUserById(id);
        userStorageService.deleteUserForStorage(userFromDb.getUserStorage(), userFromDb, userAuthentication);
        userService.deleteUser(id, userAuthentication);

        return "redirect:/users";
    }

    // получение профиля пользователя ----------------------------------------------------------------------------------
    @GetMapping("profile/{id}")
    public String getUserProfile(
            @AuthenticationPrincipal User userAuthentication,
            @PathVariable(required = false) int id,
            Model model) {
        return userService.getUserProfile(userAuthentication, id, model);
    }

    // отправка формы для обновления профиля пользователя --------------------------------------------------------------
    @PostMapping("profile/{id}")
    public String updateProfileUser(
            Model model,
            @RequestParam String usernameNew,
            @RequestParam String passwordOld,
            @AuthenticationPrincipal User userAuthentication,
            @RequestParam(required = false) String primaryParentStorageNameSelected,
            @RequestParam(required = false) String storageIdSelected,
            @RequestParam(required = false) String passwordNew,
            @RequestParam(required = false) String passwordNew2,
            @PathVariable(required = false) Optional<Integer> id,
            @RequestParam Map<String, String> form) throws JsonProcessingException {
        return userService.updateUserProfile(
                model, usernameNew, passwordOld, userAuthentication, primaryParentStorageNameSelected,
                storageIdSelected, passwordNew, passwordNew2, id, form);
    }

    // выход -----------------------------------------------------------------------------------------------------------
    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null){
            request.getSession().invalidate();
        }
        return "login";
    }
}


