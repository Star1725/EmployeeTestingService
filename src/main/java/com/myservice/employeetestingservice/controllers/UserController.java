package com.myservice.employeetestingservice.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//@RequestMapping("/user")
@Controller
public class UserController {

    @GetMapping("/users")
    public String helloUser(@RequestParam(required = false, defaultValue="World") String username, Model model) {
        model.addAttribute("userName", username);
        return "users";
    }
}
