package com.myservice.employeetestingservice.mapper;

import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User convertToEntityRegistration(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        // Настройте сопоставление
        return user;
    }
}
