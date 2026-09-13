package com.javarush.dto.task;

import com.javarush.dto.user.UserShortDto;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import lombok.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListDto {

    private Long id;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private Long ownerId;
    private String ownerUserName;
    private Set<UserShortDto> assignees;

    public static TaskListDto fromEntity(Task task) {
        return TaskListDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .ownerId(task.getOwner() != null ? task.getOwner().getId() : null)
                .ownerUserName(task.getOwner() != null ? task.getOwner().getUsername() : null)
                .assignees(task.getAssignees() != null
                        ? task.getAssignees().stream().map(UserShortDto::fromEntity).collect(Collectors.toSet())
                        : Collections.emptySet())
                .build();
    }
}
