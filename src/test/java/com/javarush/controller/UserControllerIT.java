package com.javarush.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.dto.user.UserUpdateDto;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import com.javarush.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setUsername("test_user");
        testUser.setEmail("test@mail.com");
        testUser.setPassword("$2a$10$encoded");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("200 для авторизованного USER")
    @WithMockUser(roles = "USER")
    public void getUserById_AsUser_Returns200() throws Exception {
        mockMvc.perform(get("/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.userName").value("test_user"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    @DisplayName("404 для несуществующего ID")
    @WithMockUser(roles = "USER")
    public void getUserById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("401 без авторизации")
    public void getUserById_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(get("/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("200 при обновлении владельцем")
    public void updateUser_AsOwner_Returns200() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("updated_name");
        dto.setEmail("updated@mail.com");

        mockMvc.perform(put("/users/{id}", testUser.getId())
                        .with(user("test_user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("updated_name"))
                .andExpect(jsonPath("$.email").value("updated@mail.com"));
    }

    @Test
    @DisplayName("403 при обновлении чужого профиля")
    public void updateUser_AsStranger_Returns403() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("hacked");

        mockMvc.perform(put("/users/{id}", testUser.getId())
                        .with(user("stranger").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403 для USER")
    public void deleteUser_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/users/{id}", testUser.getId())
                        .with(user("test_user").roles("USER")))
                .andExpect(status().isForbidden());
    }
}