package com.javarush.dto.task;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssigneesRequest {
    @NotNull(message = "Список исполнителей обязателен")
    @NotEmpty(message = "Список не может быть пустым")
    private Set<Long> userIds;
}
