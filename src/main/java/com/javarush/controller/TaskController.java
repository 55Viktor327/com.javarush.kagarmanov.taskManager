package com.javarush.controller;

import com.javarush.dto.task.*;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.enums.Role;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import com.javarush.security.UserPrincipal;
import com.javarush.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Tag(name = "Tasks", description = "Операции CRUD с задачами")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TaskController {
    private final TaskService taskService;

    @Operation(
            summary = "Создать задачу",
            description = "Создаёт новую задачу. Владелец — текущий аутентифицированный пользователь. " +
                    "Доступно для USER, ADMIN, SUPER_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Задача успешно создана",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные (валидация)"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "409", description = "Задача с таким названием уже существует")
    })
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser){
        Task task = taskService.createTask(request, currentUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponseDto.fromEntity(task));
    }

    @Operation(
            summary = "Получить список задач",
            description = "Возвращает список активных задач. " +
                    "GUEST видит краткую версию (без description, deadline). " +
                    "USER+ видит полную версию."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач",
                    content = @Content(schema = @Schema(oneOf = {TaskListDto.class, TaskResponseDto.class}))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('GUEST', 'USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAllTasks(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser.getRole() == Role.GUEST) {
            List<TaskListDto> tasks = taskService.getAllTasksForGuest();
            return ResponseEntity.ok(tasks);
        }

        List<TaskResponseDto> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Найти задачу по названию")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача найдена",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/by-title")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> getTaskByTitle(@RequestParam String title){
        Task task = taskService.getTaskByTitle(title);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Найти задачи по приоритету")
    @ApiResponse(responseCode = "200", description = "Список задач")
    @GetMapping("/by-priority")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getTasksByPriority(@RequestParam TaskPriority priority){
        List<Task> tasks = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(
                tasks.stream()
                        .map(TaskResponseDto:: fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Найти задачи по статусу")
    @ApiResponse(responseCode = "200", description = "Список задач")
    @GetMapping("/by-status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getTasksByStatus(@RequestParam TaskStatus status){
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(
                tasks.stream()
                        .map(TaskResponseDto:: fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Найти задачи по дате создания")
    @ApiResponse(responseCode = "200", description = "Список задач")
    @GetMapping("/by-createdAt")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getTasksByCreatedAt(@RequestParam LocalDateTime data){
        List<Task> tasks = taskService.getTasksByCreatedAt(data);
        return ResponseEntity.ok(
                tasks.stream()
                        .map(TaskResponseDto:: fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Найти задачи по дедлайну")
    @ApiResponse(responseCode = "200", description = "Список задач")
    @GetMapping("/by-deadline")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getTasksByDeadline(@RequestParam LocalDateTime data){
        List<Task> tasks = taskService.getTasksByDeadline(data);
        return ResponseEntity.ok(
                tasks.stream()
                        .map(TaskResponseDto:: fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Получить задачи владельца")
    @ApiResponse(responseCode = "200", description = "Список задач")
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getByOwner(@PathVariable Long ownerId) {
        List<Task> tasks = taskService.getTasksByOwner(ownerId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponseDto::fromEntity).toList());
    }

    @Operation(summary = "Получить задачи исполнителя")
    @ApiResponse(responseCode = "200", description = "Список задач")
    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getByAssignee(@PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponseDto::fromEntity).toList());
    }


    @Operation(
            summary = "Получить удалённые задачи",
            description = "Доступно только SUPER_ADMIN."
    )
    @ApiResponse(responseCode = "200", description = "Список удалённых задач",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getDeletedTasks(){
        List<Task> tasks = taskService.getAllDeletedTasks();
        return ResponseEntity.ok(
                tasks.stream().map(TaskResponseDto :: fromEntity).toList()
        );
    }

    @Operation(summary = "Получить задачу по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача найдена",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id){
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(
            summary = "Изменить название задачи",
            description = "Доступно владельцу, ADMIN, SUPER_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Название изменено",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "409", description = "Задача с таким названием уже есть")
    })
    @PatchMapping("/{id}/title")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public ResponseEntity<TaskResponseDto> updateTitle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTitleRequest request) {
        Task task = taskService.updateTitle(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Изменить описание задачи")
    @ApiResponse(responseCode = "200", description = "Описание изменено",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @PatchMapping("/{id}/description")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public ResponseEntity<TaskResponseDto> updateDescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDescriptionRequest request) {
        Task task = taskService.updateDescription(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Изменить приоритет задачи")
    @ApiResponse(responseCode = "200", description = "Приоритет изменён",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> changePriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriorityRequest request) {
        Task task = taskService.updatePriority(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Изменить статус задачи")
    @ApiResponse(responseCode = "200", description = "Статус изменён",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        Task task = taskService.updateStatus(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Изменить дедлайн задачи")
    @ApiResponse(responseCode = "200", description = "Дедлайн изменён",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @PatchMapping("/{id}/deadline")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> changeNewDeadline(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeadlineRequest request) {
        Task task = taskService.updateDeadline(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(
            summary = "Назначить исполнителя на задачу",
            description = "Доступно только ADMIN и SUPER_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Исполнитель назначен",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Владелец уже работает над задачей"),
            @ApiResponse(responseCode = "404", description = "Задача или пользователь не найдены"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже назначен")
    })
    @PostMapping("/{taskId}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> assignUser(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        Task task = taskService.assignUser(taskId, userId);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Снять исполнителя с задачи")
    @ApiResponse(responseCode = "200", description = "Исполнитель снят",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @DeleteMapping("/{taskId}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> removeUser(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        Task task = taskService.removeUser(taskId, userId);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(summary = "Заменить всех исполнителей задачи")
    @ApiResponse(responseCode = "200", description = "Исполнители заменены",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @PutMapping("/{taskId}/assignees")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> setAssignees(
            @PathVariable Long taskId,
            @Valid @RequestBody AssigneesRequest request) {
        Task task = taskService.setAssignees(taskId, request.getUserIds());
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @Operation(
            summary = "Удалить задачу (soft delete)",
            description = "Помечает задачу как удалённую. Доступно ADMIN, SUPER_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача удалена"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "409", description = "Задача уже удалена")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> deleteTask(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.softDeleteTask(id, currentUser.getId());
        return ResponseEntity.ok("Задача удалена");
    }

    @Operation(
            summary = "Восстановить задачу",
            description = "Доступно только SUPER_ADMIN."
    )
    @ApiResponse(responseCode = "200", description = "Задача восстановлена",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> restoreTask(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.restoreTask(id));
    }
}
