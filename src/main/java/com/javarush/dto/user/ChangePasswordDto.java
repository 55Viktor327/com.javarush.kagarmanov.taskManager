package com.javarush.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangePasswordDto {
    @NotBlank(message = "Старый пароль обязателен")
    private String oldPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
            message = "Пароль должен содержать от 8 до 20 символов, " +
                    "цифры, заглавные и строчные буквы, спецсимволы"
    )
    private String newPassword;
}
