package com.myservice.employeetestingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class LogService {

    public void writeUserLog(User userSource, String message) throws JsonProcessingException {
        LocalDateTime time = LocalDateTime.now();
        String dateTime = time.toString();
        if (userSource.getLogFile() == null || userSource.getLogFile().isEmpty()) {
            userSource.setLogFile("{}");
        }
        String logFile = userSource.getLogFile();
        Map<String, String> mapLog = new ObjectMapper().readValue(logFile, new TypeReference<>() {});
        mapLog.put(dateTime, userSource.getUsername() + ": " + message);
        logFile = new ObjectMapper().writeValueAsString(mapLog);
        userSource.setLogFile(logFile);
    }

    public void writeStorageLog(UserStorage storage, String message) throws JsonProcessingException {
        LocalDateTime time = LocalDateTime.now();
        String dateTime = time.toString();
        if (storage.getLogFile() == null || storage.getLogFile().isEmpty()) {
            storage.setLogFile("{}");
        }
        String logFile = storage.getLogFile();
        Map<String, String> mapLog = new ObjectMapper().readValue(logFile, new TypeReference<>() {});
        mapLog.put(dateTime, storage.getUserStorageName() + ": " + message);
        logFile = new ObjectMapper().writeValueAsString(mapLog);
        storage.setLogFile(logFile);
    }
}
