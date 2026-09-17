package com.javarush.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ с ошибкой")
public class ErrorResponse {

    @Schema(description = "Время ошибки", example = "2026-09-17T18:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP-код", example = "404")
    private int status;

    @Schema(description = "Тип ошибки", example = "Not Found")
    private String error;

    @Schema(description = "Сообщение", example = "Задача не найдена: 999")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/tasks/999")
    private String path;
}
