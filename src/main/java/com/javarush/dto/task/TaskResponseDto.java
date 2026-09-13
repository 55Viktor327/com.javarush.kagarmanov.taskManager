package com.javarush.dto.task;

import com.javarush.dto.user.UserShortDto;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private LocalDateTime updatedAt;
    private Long ownerId;
    private String ownerUserName;
    private Set<UserShortDto> assignees;

    public static TaskResponseDto fromEntity(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .ownerId(task.getOwner() != null ? task.getOwner().getId() : null)
                .ownerUserName(task.getOwner() != null ? task.getOwner().getUsername() : null)
                .assignees(task.getAssignees() != null
                        ? task.getAssignees().stream()
                        .map(UserShortDto::fromEntity)
                        .collect(Collectors.toSet())
                        : Collections.emptySet())
                .build();
    }
}
