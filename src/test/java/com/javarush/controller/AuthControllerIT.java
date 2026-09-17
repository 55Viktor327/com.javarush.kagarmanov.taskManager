package com.javarush.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.dto.user.UserAuthenticatedDto;
import com.javarush.dto.user.UserRegistrationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /auth/register")
    public class RegisterTests {

        @Test
        @DisplayName("Успешная регистрация → 201")
        public void register_Success_Returns201() throws Exception {
            UserRegistrationDto dto = new UserRegistrationDto();
            dto.setUserName("new_user");
            dto.setEmail("new@mail.com");
            dto.setPassword("Pass123@");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.userName").value("new_user"))
                    .andExpect(jsonPath("$.email").value("new@mail.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("Невалидные данные → 400")
        public void register_InvalidData_Returns400() throws Exception {
            UserRegistrationDto dto = new UserRegistrationDto();
            dto.setUserName("");
            dto.setEmail("not-email");
            dto.setPassword("123");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Email уже занят → 409")
        public void register_EmailExists_Returns409() throws Exception {
            UserRegistrationDto first = new UserRegistrationDto();
            first.setUserName("user1");
            first.setEmail("test@mail.com");
            first.setPassword("Pass123@");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(first)))
                    .andExpect(status().isCreated());

            UserRegistrationDto second = new UserRegistrationDto();
            second.setUserName("user2");
            second.setEmail("test@mail.com");
            second.setPassword("Pass123@");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(second)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    public class LoginTests {

        @Test
        @DisplayName("✅ Успешный логин → 200 + токен")
        public void login_Success_Returns200WithToken() throws Exception {
            UserRegistrationDto reg = new UserRegistrationDto();
            reg.setUserName("login_user");
            reg.setEmail("login@mail.com");
            reg.setPassword("Pass123@");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());

            // 2. Логинимся
            UserAuthenticatedDto login = new UserAuthenticatedDto();
            login.setUsername("login@mail.com");
            login.setPassword("Pass123@");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.userName").value("login_user"))
                    .andExpect(jsonPath("$.email").value("login@mail.com"));
        }

        @Test
        @DisplayName("Неверный пароль → 401")
        public void login_WrongPassword_Returns401() throws Exception {
            UserRegistrationDto reg = new UserRegistrationDto();
            reg.setUserName("user_for_wrong_pass");
            reg.setEmail("wrong@mail.com");
            reg.setPassword("Pass123@");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());

            UserAuthenticatedDto login = new UserAuthenticatedDto();
            login.setUsername("wrong@mail.com");
            login.setPassword("WrongPass123@");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Пользователь не найден → 401")
        public void login_UserNotFound_Returns401() throws Exception {
            UserAuthenticatedDto login = new UserAuthenticatedDto();
            login.setUsername("nonexistent@mail.com");
            login.setPassword("Pass123@");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
