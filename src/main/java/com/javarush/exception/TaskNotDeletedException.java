package com.javarush.exception;

public class TaskNotDeletedException extends RuntimeException {
    public TaskNotDeletedException(String message) {
        super(message);
    }
}
