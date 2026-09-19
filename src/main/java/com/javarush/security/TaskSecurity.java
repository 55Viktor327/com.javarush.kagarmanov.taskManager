package com.javarush.security;

import com.javarush.model.entity.Task;
import com.javarush.model.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;

    public boolean isOwner(Long taskId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        return taskRepository.findById(taskId)
                .map(task -> task.getOwner() != null
                        && task.getOwner().getId().equals(userPrincipal.getId()))
                .orElse(false);
    }
}
