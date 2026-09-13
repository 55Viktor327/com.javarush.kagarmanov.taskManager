package com.javarush.dto.task;

import com.javarush.model.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdateStatusRequest {
    @NotNull
    private TaskStatus status;
}
