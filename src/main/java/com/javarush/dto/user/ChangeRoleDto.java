package com.javarush.dto.user;

import com.javarush.model.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Запрос на смену роли")
public class ChangeRoleDto {
    @NotBlank(message = "Роль обязательна")
    @Schema(description = "Новая роль", example = "ADMIN")
    private Role role;
}
