package com.myservice.employeetestingservice.controllers;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ControllerUtils {

    private ControllerUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    static Map<String, String> getErrorsMap(BindingResult bindingResult) {
        Collector<FieldError, ?, Map<String, String>> errorMapCollector = Collectors.toMap(
                fieldError -> fieldError.getField() + "Error",
                FieldError::getDefaultMessage
        );
        return bindingResult.getFieldErrors().stream().collect(errorMapCollector);
    }


}
