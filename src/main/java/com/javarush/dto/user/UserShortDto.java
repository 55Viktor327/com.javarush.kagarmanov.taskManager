package com.javarush.dto.user;

import com.javarush.model.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortDto {

    private Long id;
    private String userName;
    private String email;

    public static UserShortDto fromEntity(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
