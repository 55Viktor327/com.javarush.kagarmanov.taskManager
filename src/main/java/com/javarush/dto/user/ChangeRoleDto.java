package com.javarush.dto.user;

import com.javarush.model.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangeRoleDto {
    @NotBlank(message = "Роль обязательна")
    private Role role;
}
