package com.javarush.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Запрос на регистрацию пользователя")
public class UserRegistrationDto {
    @Schema(
            description = "Имя пользователя",
            example = "ivan_ivanov",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 20
    )
    @NotNull(message = "Имя обязательно")
    @Size(min = 3, max = 20, message = "Имя от 3 до 20 символов")
    private String userName;

    @Schema(
            description = "Email",
            example = "ivan@mail.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "email"
    )
    @NotNull(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    @Schema(
            description = "Пароль (мин. 8 символов, цифры, заглавные/строчные, спецсимволы)",
            example = "Pass123@",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 20,
            format = "password"
    )
    @NotNull(message = "Пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
            message = "Пароль должен содержать от 8 до 20 символов, " +
                    "цифры, заглавные и строчные буквы, спецсимволы"
    )
    private String password;

}
