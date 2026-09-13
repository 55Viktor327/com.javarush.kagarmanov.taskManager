package com.javarush.dto.task;

import com.javarush.model.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdatePriorityRequest {
    @NotNull
    private TaskPriority priority;
}
