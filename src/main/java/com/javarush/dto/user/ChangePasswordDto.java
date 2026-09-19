package com.javarush.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Запрос на смену пароля")
public class ChangePasswordDto {

    @Schema(description = "Старый пароль", example = "OldPass123@", format = "password")
    @NotNull(message = "Старый пароль обязателен")
    private String oldPassword;

    @NotNull(message = "Новый пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
            message = "Пароль должен содержать от 8 до 20 символов, " +
                    "цифры, заглавные и строчные буквы, спецсимволы"
    )
    @Schema(description = "Новый пароль", example = "NewPass123@", format = "password")
    private String newPassword;
}
