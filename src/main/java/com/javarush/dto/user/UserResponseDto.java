package com.javarush.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponseDto {
    Long id;
    String userName;
    String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;
    Role role;

    public static UserResponseDto fromEntity(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setUserName(user.getUsername());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setPassword(user.getPassword());
        userResponseDto.setRole(user.getRole());

        return userResponseDto;
    }
}
