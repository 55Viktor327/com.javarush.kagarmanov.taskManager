package com.javarush.controller;

import com.javarush.dto.task.*;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.enums.Role;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import com.javarush.security.UserPrincipal;
import com.javarush.service.TaskService;
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

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser){
        Task task = taskService.createTask(request, currentUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponseDto.fromEntity(task));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id){
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @GetMapping("/by-title")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> getTaskByTitle(@RequestParam String title){
        Task task = taskService.getTaskByTitle(title);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

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

    @GetMapping("/by-cretedAt")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getTasksByCreatedAt(@RequestParam LocalDateTime data){
        List<Task> tasks = taskService.getTasksByCreatedAt(data);
        return ResponseEntity.ok(
                tasks.stream()
                        .map(TaskResponseDto:: fromEntity)
                        .collect(Collectors.toList())
        );
    }

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

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getByOwner(@PathVariable Long ownerId) {
        List<Task> tasks = taskService.getTasksByOwner(ownerId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponseDto::fromEntity).toList());
    }

    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getByAssignee(@PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponseDto::fromEntity).toList());
    }

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

    @PatchMapping("/{id}/title")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public ResponseEntity<TaskResponseDto> updateTitle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTitleRequest request) {
        Task task = taskService.updateTitle(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @PatchMapping("/{id}/description")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public ResponseEntity<TaskResponseDto> updateDescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDescriptionRequest request) {
        Task task = taskService.updateDescription(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> changePriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriorityRequest request) {
        Task task = taskService.updatePriority(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        Task task = taskService.updateStatus(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @PatchMapping("/{id}/deadline")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> changeNewDeadline(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeadlineRequest request) {
        Task task = taskService.updateDeadline(id, request);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @PostMapping("/{taskId}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> assignUser(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        Task task = taskService.assignUser(taskId, userId);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @DeleteMapping("/{taskId}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> removeUser(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        Task task = taskService.removeUser(taskId, userId);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @PutMapping("/{taskId}/assignees")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> setAssignees(
            @PathVariable Long taskId,
            @Valid @RequestBody AssigneesRequest request) {
        Task task = taskService.setAssignees(taskId, request.getUserIds());
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> deleteTask(@PathVariable Long id,
                                             @AuthenticationPrincipal UserPrincipal currentUser){
        taskService.softDeleteTask(id, currentUser.getId());
        return ResponseEntity.ok("Задача удалена");
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TaskResponseDto> restoreTask(@PathVariable Long id){
        Task task = taskService.restoreTask(id);
        return ResponseEntity.ok(TaskResponseDto.fromEntity(task));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TaskResponseDto>> getDeletedTasks(){
        List<Task> tasks = taskService.getAllDeletedTasks();
        return ResponseEntity.ok(
                tasks.stream().map(TaskResponseDto :: fromEntity).toList()
        );
    }
}
