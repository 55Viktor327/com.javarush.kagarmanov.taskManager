package com.javarush.controller;

import com.javarush.dto.user.ChangePasswordDto;
import com.javarush.dto.user.ChangeRoleDto;
import com.javarush.dto.user.UserResponseDto;
import com.javarush.dto.user.UserUpdateDto;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import com.javarush.security.UserPrincipal;
import com.javarush.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управление пользователями")
@SecurityRequirement(name = "Bearer")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Operation(summary = "Получить пользователя по username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/by-username")
    public ResponseEntity<UserResponseDto> getUserByName(@RequestParam String username) {
        User user = userService.getUserByName(username);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Operation(summary = "Получить пользователя по email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Operation(
            summary = "Получить пользователей по роли",
            description = "Если роль не указана — возвращает всех пользователей"
    )
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    @GetMapping("/by-role")
    public ResponseEntity<List<UserResponseDto>> getUsers(@RequestParam(required = false) Role role) {
        List<User> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(
                users.stream()
                        .map(UserResponseDto::fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает страницу пользователей с пагинацией"
    )
    @ApiResponse(responseCode = "200", description = "Страница пользователей")
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Page<UserResponseDto> page = userService.getAllUsers(pageable)
                .map(UserResponseDto::fromEntity);

        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Обновить профиль пользователя",
            description = "Доступно владельцу профиля или ADMIN, SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлён",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Username или email заняты")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid
            @RequestBody UserUpdateDto dto) {
        User user = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Operation(
            summary = "Сменить пароль",
            description = "Доступно владельцу профиля"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль изменён"),
            @ApiResponse(responseCode = "400", description = "Неверный старый пароль"),
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    @PutMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<String> changePassword(@PathVariable Long id,
                                               @Valid
                                               @RequestBody ChangePasswordDto dto) {
        userService.changeUserPassword(id, dto);
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    @Operation(
            summary = "Сменить роль пользователя",
            description = "Доступно только SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль изменена"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<String> changeRole(@PathVariable Long id,
                                           @Valid
                                           @RequestBody ChangeRoleDto dto) {
        userService.changeUserRole(id, dto);
        return ResponseEntity.ok("Роль успешно изменена");
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "400", description = "Нельзя удалить SUPER_ADMIN"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже удалён")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<String> deleteUser(@PathVariable Long id,
                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.softDeleteUser(id, currentUser.getId());
        return ResponseEntity.ok("Пользователь удален");
    }

    @Operation(
            summary = "Восстановить пользователя",
            description = "Доступно только SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь восстановлен",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Пользователь не был удалён")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponseDto> restoreUser(@PathVariable Long id) {
        User user = userService.restoreUser(id);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @Operation(
            summary = "Получить удалённых пользователей",
            description = "Доступно только SUPER_ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Список удалённых пользователей")
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getDeletedUsers() {
        Set<User> users = userService.getAllDeletedUsers();
        return ResponseEntity.ok(
                users.stream().map(UserResponseDto::fromEntity).toList()
        );
    }
}
