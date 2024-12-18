package com.myservice.employeetestingservice.dto;

import com.myservice.employeetestingservice.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsersStorageDTO {

    private Long id;

    @NotBlank(message = "Поле не может быть пустым!")
    private String usersStorageName;
    private String storageDescription;
    private String logFile;
    private User administrator;
}
