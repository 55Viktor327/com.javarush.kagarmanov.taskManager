package com.javarush.exception;

public class UserAlreadyDeleteException extends RuntimeException {
    public UserAlreadyDeleteException(String message) {
        super(message);
    }
}
