package com.javarush.service;

import com.javarush.dto.task.TaskCreateRequest;
import com.javarush.dto.task.UpdateTitleRequest;
import com.javarush.exception.*;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import com.javarush.model.repository.TaskRepository;
import com.javarush.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TaskService taskService;

    private User owner;
    private User assignee;
    private Task task;
    private TaskCreateRequest createRequest;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setEmail("owner@mail.com");

        assignee = new User();
        assignee.setId(2L);
        assignee.setUsername("assignee");
        assignee.setEmail("assignee@mail.com");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setPriority(TaskPriority.LOW);
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeadline(LocalDateTime.now().plusDays(7));
        task.setOwner(owner);
        task.setAssignees(new HashSet<>());
        task.setDeleted(false);

        createRequest = new TaskCreateRequest();
        createRequest.setTitle("New Task");
        createRequest.setDescription("New Description");
        createRequest.setDeadline(LocalDateTime.now().plusDays(7));
    }

    @Test
    @DisplayName("Успешное создание задачи")
    public void createTask_Success() {
        when(taskRepository.existsTaskByTitle("New Task")).thenReturn(false);
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(owner));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        Task result = taskService.createTask(createRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.LOW);

        verify(taskRepository).existsTaskByTitle("New Task");
        verify(userRepository).findActiveById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Дубликат title → TaskAlreadyExistsException")
    public void createTask_DuplicateTitle_Throws() {
        when(taskRepository.existsTaskByTitle("New Task")).thenReturn(true);

        assertThatThrownBy(() -> taskService.createTask(createRequest, 1L))
                .isInstanceOf(TaskAlreadyExistsException.class)
                .hasMessageContaining("уже создана");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Owner не найден → UserNotFoundException")
    public void createTask_OwnerNotFound_Throws() {
        when(taskRepository.existsTaskByTitle("New Task")).thenReturn(false);
        when(userRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(createRequest, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание с assignees")
    public void createTask_WithAssignees_Success() {
        createRequest.setAssigneeIds(Set.of(2L));

        when(taskRepository.existsTaskByTitle("New Task")).thenReturn(false);
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findAllActiveByIds(Set.of(2L))).thenReturn(Set.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.createTask(createRequest, 1L);

        assertThat(result.getAssignees()).hasSize(1);
        assertThat(result.getAssignees()).contains(assignee);
    }

    @Test
    @DisplayName("Owner не попадает в assignees")
    public void createTask_OwnerInAssignees_Removed() {
        createRequest.setAssigneeIds(Set.of(1L, 2L));

        when(taskRepository.existsTaskByTitle("New Task")).thenReturn(false);
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findAllActiveByIds(Set.of(1L, 2L))).thenReturn(Set.of(owner, assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.createTask(createRequest, 1L);

        assertThat(result.getAssignees()).hasSize(1);
        assertThat(result.getAssignees()).contains(assignee);
        assertThat(result.getAssignees()).doesNotContain(owner);
    }

    @Test
    @DisplayName("Некоторые assignees не найдены → UserNotFoundException")
    public void createTask_AssigneesNotFound_Throws() {
        createRequest.setAssigneeIds(Set.of(2L, 999L));

        when(taskRepository.existsTaskByTitle("New Task")).thenReturn(false);
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findAllActiveByIds(Set.of(2L, 999L))).thenReturn(Set.of(assignee));

        assertThatThrownBy(() -> taskService.createTask(createRequest, 1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Успешное получение")
    public void getTaskById_Success() {
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertThat(result).isEqualTo(task);
    }

    @Test
    @DisplayName("Не найдена → TaskNotFoundException")
    public void getTaskById_NotFound_Throws() {
        when(taskRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("Успешное назначение")
    public void assignUser_Success() {
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findActiveById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.assignUser(1L, 2L);

        assertThat(result.getAssignees()).contains(assignee);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Владелец → InvalidOperationException")
    public void assignUser_Owner_Throws() {
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> taskService.assignUser(1L, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Владелец");
    }

    @Test
    @DisplayName("Уже назначен → UserAlreadyAssignedException")
    public void assignUser_AlreadyAssigned_Throws() {
        task.getAssignees().add(assignee);

        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findActiveById(2L)).thenReturn(Optional.of(assignee));

        assertThatThrownBy(() -> taskService.assignUser(1L, 2L))
                .isInstanceOf(UserAlreadyAssignedException.class);
    }

    @Test
    @DisplayName("Task не найдена → TaskNotFoundException")
    public void assignUser_TaskNotFound_Throws() {
        when(taskRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignUser(999L, 2L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("User не найден → UserNotFoundException")
    public void assignUser_UserNotFound_Throws() {
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignUser(1L, 999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Успешное снятие")
    public void removeUser_Success() {
        task.getAssignees().add(assignee);

        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.removeUser(1L, 2L);

        assertThat(result.getAssignees()).doesNotContain(assignee);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Не назначен → UserNotAssignedException")
    public void removeUser_NotAssigned_Throws() {
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.removeUser(1L, 2L))
                .isInstanceOf(UserNotAssignedException.class);
    }

    @Test
    @DisplayName("Успешное обновление")
    public void updateTitle_Success() {
        UpdateTitleRequest request = new UpdateTitleRequest();
        request.setTitle("New Title");

        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.existsTaskByTitleAndIdNot("New Title", 1L)).thenReturn(false);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.updateTitle(1L, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("Дубликат → TaskAlreadyExistsException")
    public void updateTitle_Duplicate_Throws() {
        UpdateTitleRequest request = new UpdateTitleRequest();
        request.setTitle("Existing Title");

        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.existsTaskByTitleAndIdNot("Existing Title", 1L)).thenReturn(true);

        assertThatThrownBy(() -> taskService.updateTitle(1L, request))
                .isInstanceOf(TaskAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Null → title не меняется")
    public void updateTitle_Null_NotChanged() {
        UpdateTitleRequest request = new UpdateTitleRequest();
        request.setTitle(null);

        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.updateTitle(1L, request);

        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository, never()).existsTaskByTitleAndIdNot(any(), any());
    }

    @Test
    @DisplayName("Успешное удаление")
    public void softDeleteTask_Success() {
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        taskService.softDeleteTask(1L, 1L);

        assertThat(task.isDeleted()).isTrue();
        assertThat(task.getDeletedAt()).isNotNull();
        assertThat(task.getDeletedBy()).isEqualTo(1L);

        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Уже удалена → TaskAlreadyDeleteException")
    public void softDeleteTask_AlreadyDeleted_Throws() {
        task.setDeleted(true);
        when(taskRepository.findActiveById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.softDeleteTask(1L, 1L))
                .isInstanceOf(TaskAlreadyDeleteException.class);
    }

    @Test
    @DisplayName("Task не найдена → TaskNotFoundException")
    public void softDeleteTask_NotFound_Throws() {
        when(taskRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.softDeleteTask(999L, 1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("Успешное восстановление")
    public void restoreTask_Success() {
        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        task.setDeletedBy(1L);

        when(taskRepository.findDeletedById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.restoreTask(1L);

        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
    }
}