package com.javarush.dto.task;

import com.javarush.model.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UpdatePriorityRequest {
    @NotNull
    private TaskPriority priority;
}
