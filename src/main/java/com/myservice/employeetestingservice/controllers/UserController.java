package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.AccessLevel;
import com.myservice.employeetestingservice.domain.Role;
import com.myservice.employeetestingservice.domain.SpecAccess;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/users")
@Data
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    // получение списка пользователей ----------------------------------------------------------------------------------
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping
    public String getUserList(@AuthenticationPrincipal User userAdmin, Model model) {
        List<User> userList = userService.findAll();
        List<User> filteredSortedUsers;
            filteredSortedUsers = userList.stream()
                    .sorted(Comparator.comparing((User user) -> {
                                if (user.getRoles().contains(Role.MAIN_ADMIN)) {
                                    return 0; // Высший приоритет
                                } else if (user.getRoles().contains(Role.ADMIN)) {
                                    return 1; // Средний приоритет
                                } else {
                                    return 2; // Низший приоритет
                                }
                            })
                            .thenComparing(User::getUsername)) // Дополнительно сортируем по username в алфавитном порядке
                    .toList();
        model.addAttribute("users", filteredSortedUsers);
        return "usersList";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteUser(
            @AuthenticationPrincipal User userAuthentication,
            @PathVariable("id") Integer id) throws JsonProcessingException {
        userService.deleteUser((long)id, userAuthentication);
        return "redirect:/users";
    }

    // получение профиля пользователя ----------------------------------------------------------------------------------
    @GetMapping("profile/{id}")
    public String getProfileAnyUser(
            @AuthenticationPrincipal User userAuthentication,
            @PathVariable(name = "id") Integer id,
            Model model) {
        User userFromDb = userService.getUserById(id.longValue());
        if (userAuthentication.getId() == userFromDb.getId()) {
            return setModelFromProfileUser(userAuthentication, true, model);
        } else {
            if (userAuthentication.isMainAdmin()) {
                return setModelFromProfileUser(userFromDb, false, model);
            } else if (userAuthentication.isAdmin()) {
                if (!userFromDb.getRoles().contains(Role.MAIN_ADMIN) && !userFromDb.getRoles().contains(Role.ADMIN)) {
                    return setModelFromProfileUser(userFromDb, false, model);
                } else {
                    return setModelFromProfileUser(userAuthentication, true, model);
                }
            } else {
                return setModelFromProfileUser(userAuthentication, true, model);
            }
        }
    }

    // отправка формы для обновления профиля пользователя --------------------------------------------------------------
    @PostMapping("profile/{id}")
    public String updateProfileUser(
//            @Valid
//            User user,
//            BindingResult bindingResult,
            Model model,
            @RequestParam String usernameNew,
            @RequestParam String passwordOld,
            @AuthenticationPrincipal User userAuthentication,
            @RequestParam(required = false) String organizationName_Selected,
            @RequestParam(required = false) String divisionName_Selected,
            @RequestParam(required = false) String passwordNew,
            @RequestParam(required = false) String passwordNew2,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String usernameOld,
            @RequestParam Map<String, String> form
    ) throws JsonProcessingException {
        User userFromDb = userService.getUserById(Long.parseLong(id));
        if((usernameNew == null || usernameNew.isEmpty())){
            model.addAttribute("usernameNewError", "Поле не может быть пустым!");
        }
        boolean isChangePassword = false;
        if (!passwordNew.isEmpty() && !passwordNew2.isEmpty()){
            if (!passwordNew.equals(passwordNew2)){
                model.addAttribute("passwordNewError", Constants.PASSWORD_MISMATCH);
                model.addAttribute("passwordNew2Error", Constants.PASSWORD_MISMATCH);
            } else {
                isChangePassword = true;
            }
        }
        if (!passwordOld.isEmpty() && !userService.checkOldPassword(passwordOld, userFromDb)){
            model.addAttribute("passwordOldError", "Вы ввели неправильный пароль");
        }
        if (userService.loadUserByUsernameForUpdateUser(usernameNew) && !usernameNew.equals(usernameOld)){
            model.addAttribute("usernameNewError", Constants.USERNAME_ERROR);
        }
        model.addAttribute("roles", Role.values());
        model.addAttribute("accessLevels", AccessLevel.values());
        model.addAttribute("specAccesses", SpecAccess.values());
        UserDTO userDTO = modelMapper.map(userFromDb, UserDTO.class);
        userDTO.setUsername(usernameNew);
        model.addAttribute("user", userDTO);
        if (model.asMap().keySet().stream().anyMatch(key->key.contains("Error"))){
            return Constants.PROFILE_PAGE;
        }

        long idUserAuth = userAuthentication.getId();
        long idUserDb = userFromDb.getId();
        if (idUserAuth == idUserDb){
            userService.writeLogFile(userFromDb, "пользователь изменил свои данные", null);
            userService.updateUserFromDb(userFromDb, form);
            model.addAttribute("message", "Данные успешно обновлены!");
            if (isChangePassword){
                return "redirect:/users/logout";
            } else {
                return Constants.PROFILE_PAGE;
            }
        } else {
            userService.writeLogFile(userAuthentication, "администратор изменил данные пользователя", userFromDb);
            userService.updateUserFromDb(userFromDb, form);
            return "redirect:/users";
        }
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
//----------------------------------------------------------------------------------------------------------------------

    private UserDTO converteToUserDTO(User user){
        return modelMapper.map(user, UserDTO.class);
    }

    private User converteToUser(UserDTO userDTO){
        return modelMapper.map(userDTO, User.class);
    }

    private String setModelFromProfileUser(User user, boolean isCurrentUser, Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("accessLevels", AccessLevel.values());
        model.addAttribute("specAccesses", SpecAccess.values());
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        model.addAttribute("user", userDTO);
        return Constants.PROFILE_PAGE;
    }

}


