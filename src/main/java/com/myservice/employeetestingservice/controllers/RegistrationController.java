package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @GetMapping("/registration")
    public String registration(Model model){
        List<User> userList = userService.findAll();
        for (User user : userList) {
            if (user.getUsername().equals("MAIN_ADMIN") &&
                    passwordEncoder.matches("MAIN_ADMIN", user.getPassword())){
                model.addAttribute("message", "При первоначальном запуске сервиса создан администратор по умолчанию (имя = MAIN_ADMIN, пароль = MAIN_ADMIN)." +
                        " В целях безопасности измените его авторизационные данные и укажите организацию и подразделение");
            }
        }
        return "registration";
    }

    @PostMapping("/registration")
    public String createUser(@Valid UserDTO userDTO,
                          BindingResult bindingResult,
                          Model model) throws JsonProcessingException {
        if (userDTO.getPassword() != null && !userDTO.getPassword().equals(userDTO.getPassword2())
        ){
            model.addAttribute("passwordError", "Пароли не совпадают");
            model.addAttribute("password2Error", "Пароли не совпадают");
            return "registration";
        }

        if (bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrorsMap(bindingResult);
            model.mergeAttributes(errorsMap);
            return "registration";
        }
        User user = converteToUser(userDTO);
        if (!userService.createUser(user)){
            model.addAttribute("usernameError", "Такой Пользователь уже существует!");
            return "registration";
        } else {
            return "redirect:/login";
        }
    }

    private UserDTO converteToUserDTO(User user){
        return modelMapper.map(user, UserDTO.class);
    }


    private User converteToUser(UserDTO userDTO){
        return modelMapper.map(userDTO, User.class);
    }

}
