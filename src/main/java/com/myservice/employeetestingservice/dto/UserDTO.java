package com.myservice.employeetestingservice.dto;

import com.myservice.employeetestingservice.domain.AccessLevel;
import com.myservice.employeetestingservice.domain.Role;
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

    private boolean accessToSd;

    @OrderBy
    private List<Role> roles;

    @OrderBy
    private List<AccessLevel> accessLevels;
}
