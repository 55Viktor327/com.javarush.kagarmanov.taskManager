package com.javarush.exception;

public class UserNotAssignedException extends RuntimeException {
    public UserNotAssignedException(String message) {
        super(message);
    }
}
