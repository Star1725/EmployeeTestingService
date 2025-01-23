package com.myservice.employeetestingservice.exception;

public class AccessDeniedException extends Exception{
    public AccessDeniedException(String message){
        super(message);
    }
}
