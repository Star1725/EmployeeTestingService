package com.myservice.employeetestingservice.config;

import com.myservice.employeetestingservice.repository.UserStorageRepository;
import com.myservice.employeetestingservice.service.LogService;
import com.myservice.employeetestingservice.service.UserStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class MyConfig {
//    @Bean
//    public PasswordEncoder getPasswordEncoder() {
//        return new BCryptPasswordEncoder(8);
//    }
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public ModelMapper getModelMapper() {
            return new ModelMapper();
    }

    @Bean
    public UserStorageService userStorageService(UserStorageRepository userStorageRepository, LogService logService) {
        return new UserStorageService(userStorageRepository, logService);
    }

}
