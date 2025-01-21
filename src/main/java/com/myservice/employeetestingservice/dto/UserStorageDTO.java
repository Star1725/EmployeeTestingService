package com.myservice.employeetestingservice.dto;

import com.myservice.employeetestingservice.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserStorageDTO {

    private Long id;

    @NotBlank(message = "Поле не может быть пустым!")
    private String userStorageName;
    private String fullUserStorageName;//полное имя, от головной организации/подразделения до userStorageName, разделённые "/"
    private String storageDescription;
    private String logFile;
    private User administrator;

    private List<User> storageUsers;

    private boolean isParentStorage;
    private Set<UserStorageDTO> childStorages;

    private boolean isChildStorage;
    private UserStorageDTO parentUserStorage;

    private UserStorageDTO primaryParentStorage;
    private UserStorageDTO defaultPrimaryParentStorage;
    private Set<UserStorageDTO> allPrimaryParentStorages;
    private Set<UserStorageDTO> allChildStoragesForPrimaryParent;
}
