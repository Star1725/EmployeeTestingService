package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UsersStorage;
import com.myservice.employeetestingservice.repository.UsersStorageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Data
@RequiredArgsConstructor
public class UsersStorageService{
    private final UsersStorageRepository usersStorageRepository;
    private final LogService logService;

    public List<UsersStorage> getAllUsersStorages() {
        return usersStorageRepository.findAll().stream().sorted(Comparator.comparing(UsersStorage::getUsersStorageName)).toList();
    }

    public boolean createUsersStorage(UsersStorage usersStorage, User userAdmin, boolean isCreated){
        UsersStorage usersStorageDb = usersStorageRepository.findByUsersStorageName(usersStorage.getUsersStorageName());
        if (usersStorageDb != null) {
            return true;
        }
        LocalDateTime time = LocalDateTime.now();
        if (isCreated){
            usersStorage.setDateCreated(time);
            usersStorage.setCreatedUser(userAdmin);
            try {
                logService.writeStorageLog(usersStorage, ": хранилище создано администратором - " + userAdmin.getUsername());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            usersStorage.setDateChanged(time);
            usersStorage.setChangedUser(userAdmin);
            try {
                logService.writeStorageLog(usersStorage, ": данные хранилища обновлены администратором - " + userAdmin.getUsername());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        usersStorageRepository.save(usersStorage);
        return false;
    }

//    public void writeLogFile(UsersStorage usersStorage, String message) throws JsonProcessingException {
//        LocalDateTime time = LocalDateTime.now();
//        String dateTime = time.toString();
//        if (usersStorage.getLogFile() == null || usersStorage.getLogFile().isEmpty()) {
//            usersStorage.setLogFile("{}");
//        }
//        String logFile = usersStorage.getLogFile();
//        Map<String, String> mapLog = new ObjectMapper().readValue(logFile, new TypeReference<>() {});
//        mapLog.put(dateTime, usersStorage.getUsersStorageName() + ": " + message);
//        logFile = new ObjectMapper().writeValueAsString(mapLog);
//        usersStorage.setLogFile(logFile);
//    }

    public void deleteUsersStorage(long id, User userAuthentication) throws JsonProcessingException {
        UsersStorage usersStorageDb = usersStorageRepository.findById(id).get();
        logService.writeUserLog(userAuthentication, "администратор удалил пользователя - " + usersStorageDb.getUsersStorageName());
        usersStorageRepository.deleteById(id);
    }
}
