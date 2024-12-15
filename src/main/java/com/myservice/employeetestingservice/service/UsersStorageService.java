package com.myservice.employeetestingservice.service;

import com.myservice.employeetestingservice.domain.UsersStorage;
import com.myservice.employeetestingservice.repository.UsersStorageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
public class UsersStorageService {
    private final UsersStorageRepository usersStorageRepository;


    public List<UsersStorage> getAllUsersStorages() {
        return usersStorageRepository.findAll().stream().sorted(Comparator.comparing(UsersStorage::getUsersStorageName)).toList();
    }
}
