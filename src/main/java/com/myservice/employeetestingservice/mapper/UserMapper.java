package com.myservice.employeetestingservice.mapper;

import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.service.UserStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final UserStorageService userStorageService;

    public User convertToEntityRegistration(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());

        return user;
    }

    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUser(user.isUser());
        userDTO.setAdmin(user.isAdmin());
        userDTO.setMainAdmin(user.isMainAdmin());
        userDTO.setUsername(user.getUsername());
        userDTO.setRoles(user.getRoles());
        userDTO.setAccessLevels(user.getAccessLevels());
        userDTO.setSpecAccesses(user.getSpecAccesses());

        UserStorageDTO userStorageDTO = new UserStorageDTO();
        userStorageDTO.setFullUserStorageName(String.valueOf(userStorageService.getNameParentStorages(user.getUserStorage())));
        userDTO.setUserStorageDTO(userStorageDTO);

        return userDTO;
    }

    public List<UserDTO> convertToDTOList(List<User> userList) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : userList) {
            userDTOList.add(convertToDTO(user));
        }
        return userDTOList;
    }

}
