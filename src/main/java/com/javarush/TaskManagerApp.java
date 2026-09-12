package com.javarush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TaskManagerApp {
    static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(TaskManagerApp.class, args);
    }
}
