package com.javarush.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserUpdateDto {
    @Size(min = 3, max = 20, message = "Имя от 3 до 20 символов")
    private String userName;
    @Email(message = "Неверный формат email")
    private String email;
}
