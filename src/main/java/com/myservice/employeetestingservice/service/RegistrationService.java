package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myservice.employeetestingservice.domain.Role;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class RegistrationService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден!");
        }
        return user;
    }

    //Создание пользователя через страницу регистрации------------------------------------------------------------------
    public boolean createUserFromRegistrationPage(User user) throws JsonProcessingException {
        User userFromDB = userRepository.findByUsername(user.getUsername());
        if (userFromDB != null) {
            return false;
        }
        user.setActive(true);
        LocalDateTime timeCreated = LocalDateTime.now();
        user.setDateCreated(timeCreated);
        logService.writeUserLog(user, "пользователь создан через страницу регистрации.");
        user.setRoles(new ArrayList<>(List.of(Role.USER)));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User userDb = userRepository.save(user);
        userDb.setCreatedUser(userDb);
        userRepository.save(userDb);
        return true;
    }
}
