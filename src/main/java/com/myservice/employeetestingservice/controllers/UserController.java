package com.myservice.employeetestingservice.controllers;

import jakarta.websocket.server.PathParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//@RequestMapping("/user")
@Controller
public class UserController {

    @GetMapping("/user")
    public String helloUser(@RequestParam(required = false, defaultValue="World") String userName, Model model) {
        model.addAttribute("userName", userName);
        return "helloUser";
    }
}
