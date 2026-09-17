package com.javarush.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Данные пользователя")
public class UserResponseDto {

    @Schema(description = "ID пользователя", example = "1")
    Long id;
    @Schema(description = "Имя пользователя", example = "ivan_ivanov")
    String username;
    @Schema(description = "Email", example = "ivan@mail.com")
    String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;
    @Schema(description = "Роль", example = "USER")
    Role role;

    public static UserResponseDto fromEntity(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setUsername(user.getUsername());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setPassword(user.getPassword());
        userResponseDto.setRole(user.getRole());

        return userResponseDto;
    }
}
