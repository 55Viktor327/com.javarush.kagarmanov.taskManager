package com.javarush.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Запрос на создание задачи")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskCreateRequest {
    @Schema(
            description = "Название задачи",
            example = "Написать API",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 50
    )
    @NotBlank
    @Size(max = 255)
    private String title;

    @Schema(
            description = "Описание",
            example = "REST API для TaskManager"
    )
    @NotBlank
    private String description;

    @Schema(
            description = "Дедлайн",
            example = "2026-12-31T23:59:59",
            format = "date-time"
    )
    @NotNull
    @Future(message = "Дедлайн должен быть в будущем")
    private LocalDateTime deadline;

    @Schema(
            description = "ID исполнителей (опционально)",
            example = "[2, 3]"
    )
    private Set<Long> assigneeIds;
}
