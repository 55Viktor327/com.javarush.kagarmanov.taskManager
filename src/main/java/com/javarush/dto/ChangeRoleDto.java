package com.javarush.dto;

import com.javarush.model.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangeRoleDto {
    @NotBlank(message = "Роль обязательна")
    private Role role;
}
