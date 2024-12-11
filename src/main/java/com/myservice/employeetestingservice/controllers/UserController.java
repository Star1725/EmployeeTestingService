package com.myservice.employeetestingservice.controllers;

import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.service.UserService;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/users")
@Data
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MAIN_ADMIN')")
    @GetMapping
    public String getUserList(Model model) {
        List<UserDTO> userList = userService.findAll().stream().map(this::converteToUserDTO).toList();
        model.addAttribute("users", userList);
        return "usersList";
    }

    private UserDTO converteToUserDTO(User user){
        return modelMapper.map(user, UserDTO.class);
    }


    private User converteToUser(UserDTO userDTO){
        return modelMapper.map(userDTO, User.class);
    }
}
