package com.myservice.employeetestingservice.dto;

import com.myservice.employeetestingservice.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UserStorageDTO {

    private Long id;

    @NotBlank(message = "Поле не может быть пустым!")
    private String userStorageName;
    //полное имя, от головной организации/подразделения до userStorageName, разделённые "/"
    private String fullUserStorageName;
    private String storageDescription;
    private String logFile;
    private User administrator;

    private Set<User> storageUsers;

    private Set<UserStorageDTO> childStorages;

    private UserStorageDTO parentStorage;

    private UserStorageDTO primaryParentStorage;
    private UserStorageDTO defaultPrimaryParentStorage;
    private Set<UserStorageDTO> allPrimaryParentStorages;
    private Set<UserStorageDTO> allChildStoragesForPrimaryParent;
}
