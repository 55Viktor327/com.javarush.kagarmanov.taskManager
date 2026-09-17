package com.javarush.service;

import com.javarush.dto.task.*;
import com.javarush.exception.*;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.TaskStatus;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.repository.TaskRepository;
import com.javarush.model.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public Task createTask(TaskCreateRequest request, Long ownerId) {

        log.info("Создание задачи: title={}, ownerId={}", request.getTitle(), ownerId);

        if(taskRepository.existsTaskByTitle(request.getTitle())){
            throw new TaskAlreadyExistsException("Задача с таким названием уже создана");
        }

        User owner = userRepository.findActiveById(ownerId)
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь с id:" + ownerId + " не найден")
                );

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(TaskPriority.LOW);
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeadline(request.getDeadline());
        task.setOwner(owner);

        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            Set<User> assignees = new HashSet<>(
                    userRepository.findAllActiveByIds(request.getAssigneeIds())
            );

            if (assignees.size() != request.getAssigneeIds().size()) {
                throw new UserNotFoundException("Некоторые исполнители не найдены");
            }

            assignees.removeIf(u -> u.getId().equals(ownerId));

            task.setAssignees(assignees);
        }

        taskRepository.save(task);
        log.info("Задача создана: id={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id){
        Optional<Task> task = taskRepository.findActiveById(id);
        if(task.isEmpty()){
            throw new TaskNotFoundException("Задача не найдена");
        }

        return task.get();
    }

    @Transactional(readOnly = true)
    public Task getTaskByTitle(String title){
        Optional<Task> task = taskRepository.findActiveByTitle(title);
        if(task.isEmpty()){
            throw new TaskNotFoundException("Задача не найдена");
        }

        return task.get();
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByPriority(TaskPriority priority){
        List<Task> tasks = taskRepository.findActiveByPriority(priority);
        if(tasks.isEmpty()){
            throw new TaskNotFoundException("Задачи не найдены");
        }

        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(TaskStatus taskStatus){
        List<Task> tasks = taskRepository.findActiveByStatus(taskStatus);
        if(tasks.isEmpty()){
            throw new TaskNotFoundException("Задачи не найдены");
        }

        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByCreatedAt(LocalDateTime data){
        List<Task> tasks = taskRepository.findActiveByCreatedAt(data);
        if(tasks.isEmpty()){
            throw new TaskNotFoundException("Задачи не найдены");
        }

        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByDeadline(LocalDateTime deadline){
        List<Task> tasks = taskRepository.findActiveByDeadline(deadline);
        if(tasks.isEmpty()){
            throw new TaskNotFoundException("Задачи не найдены");
        }

        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByOwner(Long ownerId) {
        return taskRepository.findActiveByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByAssignee(Long userId) {
        return taskRepository.findActiveByAssigneeId(userId);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAllActiveWithDetails().stream()
                .map(TaskResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskListDto> getAllTasksForGuest() {
        return taskRepository.findAllActive().stream()
                .map(TaskListDto::fromEntity)
                .toList();
    }

    @Transactional
    public Task updateTitle(Long id, UpdateTitleRequest request){
        Task task = taskRepository.findActiveById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            if (taskRepository.existsTaskByTitleAndIdNot(request.getTitle(), id)) {
                throw new TaskAlreadyExistsException("Задача с таким названием уже есть");
            }
            task.setTitle(request.getTitle());
        }

        taskRepository.save(task);
        log.info("Задача обновлена: id={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    @Transactional
    public Task updateDescription(Long id, UpdateDescriptionRequest request){
        Task task = taskRepository.findActiveById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if (request.getDescription() != null && !request.getDescription().isBlank()) {

            task.setDescription(request.getDescription());
        }

        taskRepository.save(task);
        log.info("Описание задачи id={} обновлено", task.getId());
        return task;
    }

    @Transactional
    public Task updatePriority(Long id, UpdatePriorityRequest request) {
        Task task = taskRepository.findActiveById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if (request.getPriority() != null) {

            task.setPriority(request.getPriority());
        }

        taskRepository.save(task);
        log.info("Приоритет задачи id={} обновлен", task.getId());
        return task;
    }

    @Transactional
    public Task updateStatus(Long id, UpdateStatusRequest request){
        Task task = taskRepository.findActiveById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if (request.getStatus() != null) {

            task.setStatus(request.getStatus());
        }

        taskRepository.save(task);
        log.info("Статус задачи id={} обновлен", task.getId());
        return task;
    }

    @Transactional
    public Task updateDeadline(Long id, UpdateDeadlineRequest request){
        Task task = taskRepository.findActiveById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if (request.getNewDeadline() != null) {

            task.setDeadline(request.getNewDeadline());
        }

        taskRepository.save(task);
        log.info("Deadline задачи id={} обновлен - deadline={}", task.getId(), task.getDeadline());
        return task;
    }


    @Transactional
    public Task assignUser(Long taskId, Long userId){
        Task task = taskRepository.findActiveById(taskId)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        User user = userRepository.findActiveById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь с id:" + userId + " не найден")
                );

        if (task.getOwner() != null && task.getOwner().getId().equals(userId)) {
            throw new InvalidOperationException("Владелец уже работает над задачей");
        }

        if (task.getAssignees().contains(user)) {
            throw new UserAlreadyAssignedException("Пользователь уже назначен на задачу");
        }

        task.getAssignees().add(user);
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);
        log.info("Пользователь username={} назначен на задачу title={}", user.getUsername(), task.getTitle());
        return task;
    }

    @Transactional
    public Task removeUser(Long taskId, Long userId) {
        Task task = taskRepository.findActiveById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + taskId));

        boolean removed = task.getAssignees().removeIf(u -> u.getId().equals(userId));

        if (!removed) {
            throw new UserNotAssignedException("Пользователь не назначен на задачу");
        }

        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        log.info("Пользователь userId={} исключён из задачи id={}, title={}",
                userId, taskId, task.getTitle());
        return task;
    }

    @Transactional
    public Task setAssignees(Long taskId, Set<Long> userIds) {
        Task task = taskRepository.findActiveById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + taskId));

        Set<User> users = new HashSet<>(userRepository.findAllActiveByIds(userIds));

        if (users.size() != userIds.size()) {
            throw new UserNotFoundException("Некоторые пользователи не найдены");
        }

        if (task.getOwner() != null) {
            users.removeIf(u -> u.getId().equals(task.getOwner().getId()));
        }

        task.getAssignees().clear();
        task.getAssignees().addAll(users);
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);
        log.info("Пользователи добавлены в задачу title={}", task.getTitle());
        return task;
    }

    @Transactional
    public void softDeleteTask(Long id, Long deletedBy){
        Task task = taskRepository.findActiveById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if(task.isDeleted()){
            throw new TaskAlreadyDeleteException("Задача уже удалена");
        }

        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        task.setDeletedBy(deletedBy);

        taskRepository.save(task);
        log.info("Задача с id={} успешно удалена", id);
    }

    @Transactional
    public Task restoreTask(Long id){
        Task task = taskRepository.findDeletedById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException("Задача не найдена")
                );

        if(!task.isDeleted()){
            throw new TaskNotDeletedException("Задача не была удалена");
        }

        task.setDeleted(false);
        task.setDeletedAt(null);
        task.setDeletedBy(null);

        Task restored = taskRepository.save(task);
        log.info("Задача title={} восстановлена в БД", task.getTitle());

        return restored;
    }

    @Transactional(readOnly = true)
    public List<Task> getAllDeletedTasks(){
        return taskRepository.findAllDeleted();
    }
}