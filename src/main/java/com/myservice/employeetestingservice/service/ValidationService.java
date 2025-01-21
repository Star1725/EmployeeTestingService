package com.myservice.employeetestingservice.service;

import com.myservice.employeetestingservice.dto.UserStorageDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static com.myservice.employeetestingservice.controllers.Constants.*;

@Service
@Data
@RequiredArgsConstructor
public class ValidationService {


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
}


