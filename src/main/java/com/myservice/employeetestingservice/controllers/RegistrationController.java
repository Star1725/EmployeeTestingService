package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.mapper.UserMapper;
import com.myservice.employeetestingservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final UserMapper userMapper;

    @GetMapping("/registration")
    public String registration(Model model){
        List<User> userList = userService.findAll();
        for (User user : userList) {
            if (user.getUsername().equals("MAIN_ADMIN") &&
                    passwordEncoder.matches("MAIN_ADMIN", user.getPassword())){
                model.addAttribute("message", Constants.DEFAULT_ADMIN_WARNING);
            }
        }
        return Constants.REGISTRATION_PAGE;
    }

    @PostMapping("/registration")
    public String createUser(@Valid UserDTO userDTO,
                          BindingResult bindingResult,
                          Model model) throws JsonProcessingException {
        if (userDTO.getPassword() != null && !userDTO.getPassword().equals(userDTO.getPassword2())
        ){
            model.addAttribute("passwordError", Constants.PASSWORD_MISMATCH);
            model.addAttribute("password2Error", Constants.PASSWORD_MISMATCH);
            return Constants.REGISTRATION_PAGE;
        }
        if (bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrorsMap(bindingResult);
            model.mergeAttributes(errorsMap);
            return Constants.REGISTRATION_PAGE;
        }
        User user = userMapper.convertToEntityRegistration(userDTO);
        if (!userService.createUserFromRegistrationPage(user)){
            model.addAttribute("usernameError", "Такой Пользователь уже существует!");
            return Constants.REGISTRATION_PAGE;
        } else {
            return "redirect:/login";
        }
    }

}
