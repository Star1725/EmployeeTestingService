package com.myservice.employeetestingservice.service;

import com.myservice.employeetestingservice.controllers.Constants;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static com.myservice.employeetestingservice.controllers.Constants.*;

@Service
@Data
@RequiredArgsConstructor
public class ValidationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //@param isDuplicate - для валидации дублирования имени в рамках родительского хранилища
    public boolean isNotValidateStorageName(UserStorageDTO userStorageDTO, UserStorageDTO parentUserStorageDTO, Model model, boolean isDuplicate) {
        if (userStorageDTO.getUserStorageName().trim().length() < 3 || isDuplicate) {
            model.addAttribute(PARENT_USER_STORAGE, parentUserStorageDTO);
            model.addAttribute(USER_STORAGE, userStorageDTO);
            model.addAttribute("userStorageNameError", "Поле не может быть пустым или слишком коротким!");
            if (isDuplicate) {
                model.addAttribute("userStorageNameError", "Такое название уже существует!");
            }
            return true;
        }
        return false;
    }

    //@param idStorage - для валидации в модальном окне
    public boolean isNotValidateStorageNameForModal(UserStorageDTO userStorageDTO, UserStorageDTO parentUserStorageDTO, Model model, String idStorage, boolean isDuplicate) {
        if (userStorageDTO.getUserStorageName().trim().length() < 3 || isDuplicate) {
            model.addAttribute(PARENT_USER_STORAGE, parentUserStorageDTO);
            model.addAttribute(UPDATED_USER_STORAGE, userStorageDTO);
            model.addAttribute("userStorageNameUpdateError", "Поле не может быть пустым или слишком коротким!");
            model.addAttribute("openModalId", idStorage);
            boolean isCheck = parentUserStorageDTO.getChildStorages().stream().anyMatch(storage -> storage.getId() == userStorageDTO.getId());
            if (isCheck) {
                model.addAttribute(PARENT_USER_STORAGE, parentUserStorageDTO);
            } else {
                UserStorageDTO grandParentUserStorageDTO = parentUserStorageDTO.getParentUserStorage();
                model.addAttribute(PARENT_USER_STORAGE, grandParentUserStorageDTO);
                model.addAttribute(PARENT_USER_STORAGE_FOR_UPDATED, parentUserStorageDTO);
            }
            if (isDuplicate) {
                model.addAttribute("userStorageNameUpdateError", "Такое название в выбранной организации/подразделении уже существует!");
            }
            return true;
        }
        return false;
    }

    public void validateUserProfile(Model model, String usernameNew, String passwordOld, String passwordNew, String passwordNew2, User userFromDb) {
        if (usernameNew == null || usernameNew.isEmpty()) {
            model.addAttribute("usernameNewError", "Username cannot be empty!");
        } else if (isUsernameTaken(usernameNew, userFromDb)) {
            model.addAttribute("usernameNewError", Constants.USERNAME_FIND_ERROR);
        }

        if (passwordNew != null && !passwordNew.isEmpty() && !passwordNew.equals(passwordNew2)) {
            model.addAttribute("passwordNewError", Constants.PASSWORD_MISMATCH);
        } else if (!passwordOld.isEmpty() && !checkOldPassword(passwordOld, userFromDb)) {
            model.addAttribute("passwordOldError", "Incorrect old password.");
        }
    }

    private boolean isUsernameTaken(String usernameNew, User userFromDb) {
        User existingUser = userRepository.findByUsername(usernameNew);
        return existingUser != null && !existingUser.getUsername().equals(userFromDb.getUsername());
    }

    private boolean checkOldPassword(String passwordOld, User userFromDb) {
        return passwordEncoder.matches(passwordOld, userFromDb.getPassword());
    }
}


