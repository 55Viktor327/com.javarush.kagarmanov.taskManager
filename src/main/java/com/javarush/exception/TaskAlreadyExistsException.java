package com.javarush.exception;

public class TaskAlreadyExistsException extends RuntimeException {
    public TaskAlreadyExistsException(String message) {
        super(message);
    }
}
