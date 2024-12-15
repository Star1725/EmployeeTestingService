package com.myservice.employeetestingservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myservice.employeetestingservice.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class MainController {

    @GetMapping("/mainPage")
    public String getMainPage(@AuthenticationPrincipal User user, Model model) throws JsonProcessingException {
        Map<String, String> mapLog = new ObjectMapper().readValue(user.getLogFile(), new TypeReference<>() {});
        model.addAttribute("mapLog", mapLog);
        model.addAttribute("user", user);
        return "mainPage";
    }
}
