package com.javarush.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с JWT-токеном")
public class JwtResponse {
    @Schema(
            description = "JWT-токен",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String token;
    @Schema(description = "Тип токена", example = "Bearer")
    private String type = "Bearer";
    @Schema(description = "ID пользователя", example = "1")
    private Long id;
    @Schema(description = "Имя пользователя", example = "ivan_ivanov")
    private String username;
    @Schema(description = "Email", example = "ivan@mail.com")
    private String email;
    @Schema(description = "Роль", example = "USER")
    private String role;
}
