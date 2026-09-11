package com.javarush.dto;

import com.javarush.model.entity.Task;
import com.javarush.model.entity.enums.Role;

import java.util.Set;

public record UserResponseDto (
        Long id,
        String name,
        String email,
        String password,
        Role role,
        Set<Task> assignedTasks,
        Set<Task> ownedTasks
) {}
