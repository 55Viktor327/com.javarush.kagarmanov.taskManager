package com.javarush.dto.user;


import jakarta.validation.constraints.NotBlank;
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
