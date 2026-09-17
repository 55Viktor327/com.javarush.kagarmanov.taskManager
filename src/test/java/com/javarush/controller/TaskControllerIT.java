package com.javarush.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.dto.task.TaskCreateRequest;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import com.javarush.model.repository.TaskRepository;
import com.javarush.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("TaskController Integration Tests")
public class TaskControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private Task task;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setUsername("owner");
        owner.setEmail("owner@mail.com");
        owner.setPassword("$2a$10$encoded");
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(TaskPriority.LOW);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeadline(LocalDateTime.now().plusDays(7));
        task.setOwner(owner);
        task = taskRepository.save(task);
    }

    @Test
    @DisplayName("201 для USER")
    public void createTask_AsUser_Returns201() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/tasks/create")
                        .with(user("owner").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    @DisplayName("401 без авторизации")
    public void createTask_Unauthorized_Returns401() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("409 при дубликате title")
    public void createTask_DuplicateTitle_Returns409() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task");  // ← уже существует!
        request.setDescription("New Description");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/tasks/create")
                        .with(user("owner").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("200 для USER (полный список)")
    public void getAllTasks_AsUser_Returns200() throws Exception {
        mockMvc.perform(get("/tasks")
                        .with(user("owner").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("200 для USER")
    public void getTaskById_AsUser_Returns200() throws Exception {
        mockMvc.perform(get("/tasks/{id}", task.getId())
                        .with(user("owner").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("для несуществующего ID")
    public void getTaskById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 9999L)
                        .with(user("owner").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("200 для ADMIN")
    public void deleteTask_AsAdmin_Returns200() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", task.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("403 для USER")
    public void deleteTask_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", task.getId())
                        .with(user("owner").roles("USER")))
                .andExpect(status().isForbidden());
    }
}