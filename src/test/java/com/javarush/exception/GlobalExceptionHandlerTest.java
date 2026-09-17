package com.javarush.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.security.JwtTokenProvider;
import com.javarush.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.test.autoconfigure.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class )
@Import(GlobalExceptionHandler.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("404 Not Found")
    class NotFoundTests {

        @Test
        @DisplayName("TaskNotFoundException → 404")
        void taskNotFound_Returns404() throws Exception {
            mockMvc.perform(get("/test/task-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Задача не найдена: 999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("UserNotFoundException → 404")
        void userNotFound_Returns404() throws Exception {
            mockMvc.perform(get("/test/user-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Пользователь не найден: 999"));
        }
    }

    @Nested
    @DisplayName("409 Conflict")
    class ConflictTests {

        @Test
        @DisplayName("TaskAlreadyExistsException → 409")
        void taskAlreadyExists_Returns409() throws Exception {
            mockMvc.perform(get("/test/task-already-exists"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Задача уже существует"));
        }

        @Test
        @DisplayName("UserAlreadyExistsException → 409")
        void userAlreadyExists_Returns409() throws Exception {
            mockMvc.perform(get("/test/user-already-exists"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("UserAlreadyAssignedException → 409")
        void userAlreadyAssigned_Returns409() throws Exception {
            mockMvc.perform(get("/test/user-already-assigned"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }
    }

    @Nested
    @DisplayName("400 Bad Request")
    class BadRequestTests {

        @Test
        @DisplayName("InvalidPasswordException → 400")
        void invalidPassword_Returns400() throws Exception {
            mockMvc.perform(get("/test/invalid-password"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Неверный пароль"));
        }

        @Test
        @DisplayName("InvalidOperationException → 400")
        void invalidOperation_Returns400() throws Exception {
            mockMvc.perform(get("/test/invalid-operation"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("MethodArgumentNotValidException → 400 с errors")
        void validation_Returns400WithErrors() throws Exception {
            TestDto dto = new TestDto();
            dto.setName("");   // ← невалидно

            mockMvc.perform(post("/test/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors.name").value("Имя обязательно"));
        }
    }

    @Nested
    @DisplayName("403 Forbidden")
    class ForbiddenTests {

        @Test
        @DisplayName("AccessDeniedException → 403")
        void accessDenied_Returns403() throws Exception {
            mockMvc.perform(get("/test/access-denied"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("Доступ запрещён"));
        }
    }

    @Nested
    @DisplayName("500 Internal Server Error")
    class InternalErrorTests {

        @Test
        @DisplayName("RuntimeException → 500")
        void runtimeException_Returns500() throws Exception {
            mockMvc.perform(get("/test/internal-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"));
        }
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/task-not-found")
        public void throwTaskNotFound() {
            throw new TaskNotFoundException("Задача не найдена: 999");
        }

        @GetMapping("/user-not-found")
        public void throwUserNotFound() {
            throw new UserNotFoundException("Пользователь не найден: 999");
        }

        @GetMapping("/task-already-exists")
        public void throwTaskAlreadyExists() {
            throw new TaskAlreadyExistsException("Задача уже существует");
        }

        @GetMapping("/user-already-exists")
        public void throwUserAlreadyExists() {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        @GetMapping("/user-already-assigned")
        public void throwUserAlreadyAssigned() {
            throw new UserAlreadyAssignedException("Пользователь уже назначен");
        }

        @GetMapping("/invalid-password")
        public void throwInvalidPassword() {
            throw new InvalidPasswordException("Неверный пароль");
        }

        @GetMapping("/invalid-operation")
        public void throwInvalidOperation() {
            throw new InvalidOperationException("Неверная операция");
        }

        @GetMapping("/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Доступ запрещён");
        }

        @GetMapping("/internal-error")
        public void throwInternalError() {
            throw new RuntimeException("Внутренняя ошибка");
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestDto dto) {
            // просто валидация
        }
    }

    @Data
    static class TestDto {

        @NotBlank(message = "Имя обязательно")
        private String name;
    }
}