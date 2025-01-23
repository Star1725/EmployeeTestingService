package com.myservice.employeetestingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработка исключения UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFoundException(UserNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return new ModelAndView("errorPage"); // Возвращает представление с ошибкой
    }

    // Обработка исключения AuthenticationDataRetrievalException
    @ExceptionHandler(AuthenticationDataRetrievalException.class)
    public ResponseEntity<String> handleAuthenticationDataRetrieval(AuthenticationDataRetrievalException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Обработка исключения AccessDeniedException
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // Общая обработка других исключений
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Произошла ошибка: " + ex.getMessage());
        return new ModelAndView("errorPage");
    }
}
