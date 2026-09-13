package com.javarush.dto.task;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdateTitleRequest {
    @Size(max = 255)
    private String title;
}
