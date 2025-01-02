package com.myservice.employeetestingservice.dto;

import com.myservice.employeetestingservice.domain.AccessLevel;
import com.myservice.employeetestingservice.domain.Role;
import com.myservice.employeetestingservice.domain.SpecAccess;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "Поле не может быть пустым!")
    private String username;

    @NotBlank(message = "Поле не может быть пустым!")
    private String password;

    @NotBlank(message = "Поле не может быть пустым!")
    private String password2;

    private String logFile;

    @OrderBy
    private List<Role> roles;

    public boolean isMainAdmin;
    private boolean isAdmin;
    public boolean isUser;

    @OrderBy
    private List<AccessLevel> accessLevels;

    @OrderBy
    private List<SpecAccess> specAccesses;

    public boolean isAdmin(){
        return roles.contains(Role.ADMIN);
    }
    public boolean isUser(){
        return roles.contains(Role.USER);
    }
    public boolean isMainAdmin(){
        return roles.contains(Role.MAIN_ADMIN);
    }

    private UserStorageDTO userStorageDTO;
}
