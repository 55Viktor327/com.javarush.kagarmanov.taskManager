package com.javarush.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
