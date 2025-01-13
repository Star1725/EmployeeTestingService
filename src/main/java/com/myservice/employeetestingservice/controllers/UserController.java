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
        User userFromDb = userService.getUserById(id);
        User fullUserAuthentication = userService.getUserByIdWithUserStorage(userAuthentication);

        UserDTO userDTO = userMapper.convertToDTO(userFromDb);
        UserStorageDTO userStorageDTO = userStorageMapper.convertToDTOForProfile(userFromDb.getUserStorage(), fullUserAuthentication);

        //если пользователь просматривает свой же профиль
        if (fullUserAuthentication.getId() == userFromDb.getId()) {
            return setModelFromProfileUser(userDTO, userStorageDTO, model);
        } else {
            //если профиль просматривает MainAdmin
            if (fullUserAuthentication.isMainAdmin()) {
                return setModelFromProfileUser(userDTO, userStorageDTO, model);
            }
            //если профиль просматривает Admin
            else if (fullUserAuthentication.isAdmin()) {

                if (!userFromDb.getRoles().contains(Role.MAIN_ADMIN) && !userFromDb.getRoles().contains(Role.ADMIN)) {
                    return setModelFromProfileUser(userDTO, userStorageDTO, model);
                } else {
                    return setModelFromProfileUser(userDTO, userStorageDTO, model);
                }
            } else {
                return setModelFromProfileUser(userDTO, userStorageDTO, model);
            }
        }
    }

    // отправка формы для обновления профиля пользователя --------------------------------------------------------------
    @PostMapping("profile/{id}")
    public String updateProfileUser(
            Model model,
            @RequestParam String usernameNew,
            @RequestParam String passwordOld,
            @AuthenticationPrincipal User userAuthentication,
            @RequestParam(required = false) String primaryParentStorageNameSelected,
            @RequestParam(required = false) String storageId_Selected,
            @RequestParam(required = false) String passwordNew,
            @RequestParam(required = false) String passwordNew2,
            @PathVariable(required = false) int id,
            @RequestParam Map<String, String> form
    ) throws JsonProcessingException {
        //получаем пользователя, для которого нужно обновить данные и полного администратора
        User userFromDb = userService.getUserById(id);
        User fullUserAuthentication = userService.getUserByIdWithUserStorage(userAuthentication);

        //проверка поля ввода ФИО на пустоту
        if((usernameNew == null || usernameNew.isEmpty())){
            model.addAttribute("usernameNewError", "Поле не может быть пустым!");
        }

        //проверяем меняем ли пароль
        boolean isChangePassword = false;
        if (!passwordNew.isEmpty() && !passwordNew2.isEmpty()){
            if (!passwordNew.equals(passwordNew2)){
                model.addAttribute("passwordNewError", Constants.PASSWORD_MISMATCH);
                model.addAttribute("passwordNew2Error", Constants.PASSWORD_MISMATCH);
            } else {
                isChangePassword = true;
                //проверяем валидность старого пароля для смены на новый
                if (!passwordOld.isEmpty() && !userService.checkOldPassword(passwordOld, userFromDb)){
                    model.addAttribute("passwordOldError", "Вы ввели неправильный пароль");
                }
            }
        }

        //проверяем есть другой пользователь в БД с именем, которое хотим присвоить текущему пользователю userFromDb
        if (userService.loadUserByUsernameForUpdateUser(usernameNew) && !usernameNew.equals(userFromDb.getUsername())){
            model.addAttribute("usernameNewError", Constants.USERNAME_FIND_ERROR);
        }

        //Добавляем в модель необходимые поля для корректного отображения. Для оптимизации отправляемых данных их количество определяется ролью пользователя
        if (userFromDb.isAdmin()){
            model.addAttribute("roles", Role.values());
            model.addAttribute("accessLevels", AccessLevel.values());
            model.addAttribute("specAccesses", SpecAccess.values());
        }

        //конвертируем пользователя
        UserDTO userDTO = userMapper.convertToDTO(userFromDb);
        userDTO.setUsername(usernameNew);
        model.addAttribute("userDTO", userDTO);

        //конвертируем хранилище
        UserStorage oldUserStorage = userFromDb.getUserStorage();
        UserStorage newUserStorageDb;
        if (storageId_Selected != null && !storageId_Selected.isEmpty()){
            int storageId = Integer.parseInt(storageId_Selected);
            newUserStorageDb = userStorageService.getUserStorageById(storageId);
        } else {
            newUserStorageDb = userStorageService.getUserStorageByUsersStorageName(primaryParentStorageNameSelected);
        }
        UserStorageDTO userStorageDTO = userStorageMapper.convertToDTOForProfile(newUserStorageDb, fullUserAuthentication);
        model.addAttribute("userStorageDTO", userStorageDTO);

        //при наличии в model поля с вложенным словом "Error" возвращаем PROFILE_PAGE с ошибкой(-ами)
        if (model.asMap().keySet().stream().anyMatch(key->key.contains("Error"))){
            return Constants.PROFILE_PAGE;
        }

        //обновляем пользователя и хранилище
        long authenticationId = fullUserAuthentication.getId();
        long userId = userFromDb.getId();
        if (authenticationId == userId){
            userService.updateUserFromDb( userFromDb, newUserStorageDb, form, null);
            userStorageService.updateUserForStorage(oldUserStorage, newUserStorageDb, null, userFromDb);
            model.addAttribute("message", "Данные успешно обновлены!");
            if (isChangePassword){
                return "redirect:/users/logout";
            } else {
                return Constants.PROFILE_PAGE;
            }
        } else {
            userService.updateUserFromDb(userFromDb, newUserStorageDb, form, fullUserAuthentication);
            userStorageService.updateUserForStorage(oldUserStorage, newUserStorageDb, fullUserAuthentication, userFromDb);
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


    private String setModelFromProfileUser(UserDTO userDTO, UserStorageDTO userStorageDTO, Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("accessLevels", AccessLevel.values());
        model.addAttribute("specAccesses", SpecAccess.values());
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("userStorageDTO", userStorageDTO);
        return Constants.PROFILE_PAGE;
    }

}


