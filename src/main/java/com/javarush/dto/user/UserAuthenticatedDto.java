package com.javarush.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на авторизацию")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserAuthenticatedDto {

    @Schema(
            description = "Имя пользователя",
            example = "ivan_ivanov",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Имя обязательно")
    @Size(min = 3, max = 20, message = "Имя от 3 до 20 символов")
    private String username;

    @Schema(
            description = "Пароль",
            example = "Pass123@",
            requiredMode = Schema.RequiredMode.REQUIRED,
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
