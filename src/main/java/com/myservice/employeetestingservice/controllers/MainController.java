package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ModelMapper modelMapper;
    private final UserService userService;

    @GetMapping("/mainPage")
    public String getMainPage(@AuthenticationPrincipal User user, Model model) throws JsonProcessingException {
        User fullUser = userService.getUserByIdWithUserStorage(user);
        Map<String, String> mapLog = new ObjectMapper().readValue(user.getLogFile(), new TypeReference<>() {});
        model.addAttribute("mapLog", mapLog);
        UserDTO userDTO = converteToUserDTO(fullUser);

        model.addAttribute("user", userDTO);
        return "mainPage";
    }
    private UserDTO converteToUserDTO(User user){

        return modelMapper.map(user, UserDTO.class);
    }
}
