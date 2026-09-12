package com.javarush.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserAuthenticatedDto {
    @NotBlank(message = "Имя обязательно")
    @Size(min = 3, max = 20, message = "Имя от 3 до 20 символов")
    private String userName;

    @NotBlank(message = "Пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
            message = "Пароль должен содержать от 8 до 20 символов, " +
                    "цифры, заглавные и строчные буквы, спецсимволы"
    )
    private String password;
}
