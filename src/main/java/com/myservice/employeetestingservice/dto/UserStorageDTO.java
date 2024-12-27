package com.myservice.employeetestingservice.dto;

import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UserStorageDTO {

    private Long id;

    @NotBlank(message = "Поле не может быть пустым!")
    private String userStorageName;
    private String storageDescription;
    private String logFile;
    private User administrator;

    private Set<User> storageUsers;
    private Set<UserStorage> childUserStorages;
}
