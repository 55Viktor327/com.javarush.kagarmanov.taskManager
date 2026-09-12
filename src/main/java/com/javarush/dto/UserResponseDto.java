package com.javarush.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import lombok.*;

import java.util.Set;

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
        userResponseDto.setUserName(user.getUserName());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setPassword(user.getPassword());
        userResponseDto.setRole(user.getRole());

        return userResponseDto;
    }
}
