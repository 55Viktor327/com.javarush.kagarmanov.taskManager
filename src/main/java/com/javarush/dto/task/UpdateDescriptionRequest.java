package com.javarush.dto.task;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateDescriptionRequest {
    @Size(max = 5000)
    private String description;
}
