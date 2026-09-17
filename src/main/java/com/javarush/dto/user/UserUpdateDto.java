package com.javarush.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на обновление пользователя")
public class UserUpdateDto {
    @Size(min = 3, max = 20, message = "Имя от 3 до 20 символов")
    @Schema(description = "Новое имя", example = "new_username", minLength = 3, maxLength = 20)
    private String username;
    @Email(message = "Неверный формат email")
    @Schema(description = "Новый email", example = "new@mail.com", format = "email")
    private String email;
}
