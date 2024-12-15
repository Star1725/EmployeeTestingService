package com.myservice.employeetestingservice.controllers;

import com.myservice.employeetestingservice.domain.UsersStorage;
import com.myservice.employeetestingservice.service.UsersStorageService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/usersStorage")
@Data
public class UsersStorageController {

    private final UsersStorageService usersStorageService;
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MAIN_ADMIN')")
    public String getOrganizationPage(Model model) {

        List<UsersStorage> usersStorages = usersStorageService.getAllUsersStorages();
        model.addAttribute("usersStorages", usersStorages);

        return "organizationPage";
    }
}
