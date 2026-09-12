package com.javarush.controller;

import com.javarush.dto.UserAuthenticatedDto;
import com.javarush.dto.UserRegistrationDto;
import com.javarush.model.entity.User;
import com.javarush.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.javarush.dto.UserResponseDto;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationDto dto){
        User user = userService.createUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponseDto.fromEntity(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(
            @Valid @RequestBody UserAuthenticatedDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUserName(), dto.getPassword()
                ));

        User user = userService.getUserByName(authentication.getName());
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }
}
