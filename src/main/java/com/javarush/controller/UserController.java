package com.javarush.controller;

import com.javarush.dto.ChangePasswordDto;
import com.javarush.dto.ChangeRoleDto;
import com.javarush.dto.UserResponseDto;
import com.javarush.dto.UserUpdateDto;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import com.javarush.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @GetMapping("/by-username")
    public ResponseEntity<UserResponseDto> getUserByName(@RequestParam String username) {
        User user = userService.getUserByName(username);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @GetMapping("/by-role")
    public ResponseEntity<List<UserResponseDto>> getUsers(@RequestParam(required = false) Role role) {
        List<User> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(
                users.stream()
                        .map(UserResponseDto::fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Page<UserResponseDto> page = userService.getAllUsers(pageable)
                .map(UserResponseDto::fromEntity);

        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid
            @RequestBody UserUpdateDto dto) {
        User user = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(@PathVariable Long id,
                                               @Valid
                                               @RequestBody ChangePasswordDto dto) {
        userService.changeUserPassword(id, dto);
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<String> changeRole(@PathVariable Long id,
                                           @Valid
                                           @RequestBody ChangeRoleDto dto) {
        userService.changeUserRole(id, dto);
        return ResponseEntity.ok("Роль успешно изменена");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
