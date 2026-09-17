package com.javarush.controller;

import com.javarush.dto.user.JwtResponse;
import com.javarush.dto.user.UserAuthenticatedDto;
import com.javarush.dto.user.UserRegistrationDto;
import com.javarush.model.entity.User;
import com.javarush.security.JwtTokenProvider;
import com.javarush.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.javarush.dto.user.UserResponseDto;

@Tag(name = "Auth", description = "Регистрация и аутентификация пользователей")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Operation(
            summary = "Зарегистрировать пользователя",
            description = "Создаёт нового пользователя с ролью USER. " +
                    "Пароль шифруется BCrypt. Доступно без аутентификации."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные (валидация не пройдена)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким username или email уже существует"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationDto dto){
        User user = userService.createUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponseDto.fromEntity(user));
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Аутентифицирует пользователя по username и паролю. " +
                    "Возвращает JWT-токен для доступа к защищённым эндпоинтам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Авторизация прошла успешно",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные (валидация)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный username или пароль"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody UserAuthenticatedDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        User user = userService.getUserByName(authentication.getName());

        return ResponseEntity.ok(new JwtResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        ));
    }
}
