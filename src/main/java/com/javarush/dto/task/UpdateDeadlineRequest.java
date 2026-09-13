package com.javarush.dto.task;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UpdateDeadlineRequest {
    @Future(message = "Deadline должен быть в будущем")
    @NotNull(message = "Deadline не может быть пустым")
    private LocalDateTime newDeadline;
}
