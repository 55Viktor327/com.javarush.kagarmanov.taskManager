package com.javarush.service;

import com.javarush.dto.user.ChangePasswordDto;
import com.javarush.dto.user.ChangeRoleDto;
import com.javarush.dto.user.UserRegistrationDto;
import com.javarush.dto.user.UserUpdateDto;
import com.javarush.exception.*;
import com.javarush.model.entity.Task;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import com.javarush.model.repository.TaskRepository;
import com.javarush.model.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;

    @Transactional
    public User createUser(UserRegistrationDto dto) {
        log.info("Сщздание пользователя");
        if (userRepository.existsActiveByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsActiveByUsername(dto.getUserName())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.GUEST);

        userRepository.save(user);
        log.info("Пользователь username={} успешно создан", user.getUsername());
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findActiveById(id)
                            .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                            );
    }

    public User getUserByEmail(String email) {
        return userRepository.findActiveByEmail(email)
                            .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с email: " + email + " не найден")
                            );
    }

    public User getUserByName(String username) {
        return userRepository.findActiveByUsername(username)
                            .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с именем: " + username + " не найден")
                            );
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findActiveByRole(role);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return (Page<User>) userRepository.findAllActive();
    }

    public User updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findActiveById(id)
                                .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                                );

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (userRepository.existsActiveByEmailAndIdNot(dto.getEmail(), id)) {
                throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
            }

            user.setEmail(dto.getEmail());
        }

        if (dto.getUserName() != null && !dto.getUserName().isEmpty()) {
            if (userRepository.existsActiveByUsernameAndIdNot(dto.getUserName(), id)) {
                throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
            }

            user.setUsername(dto.getUserName());
        }

        userRepository.save(user);
        log.info("Данные пользователя username={} обновлены", user.getUsername());
        return user;
    }

    @Transactional
    public void changeUserPassword(Long id, ChangePasswordDto dto) {
        User userWithOldPassword = userRepository.findActiveById(id)
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                );

        if (!passwordEncoder.matches(dto.getOldPassword(), userWithOldPassword.getPassword())) {
            System.out.println("Введен неверный пароль");
        }

        String updatedPassword = passwordEncoder.encode(dto.getNewPassword());
        userWithOldPassword.setPassword(updatedPassword);
        userRepository.save(userWithOldPassword);
        log.info("Пароль успешно изменен");
    }

    @Transactional
    public void changeUserRole(Long id, ChangeRoleDto dto) {
        User updetedRoleUser = userRepository.findActiveById(id)
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                );

        updetedRoleUser.setRole(dto.getRole());
        userRepository.save(updetedRoleUser);
        log.info("Права пользователя username={} изменены на role={}",updetedRoleUser.getUsername(), updetedRoleUser.getRole());
    }

    @Transactional
    public void softDeleteUser(Long id, Long deletedBy) {
        User user = userRepository.findActiveById(id)
                                        .orElseThrow(
                                                () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                                        );
        if(user.isDeleted()){
            throw new UserAlreadyDeleteException("Пользователь уже удален");
        }

        User superAdmin = userRepository.findActiveById(deletedBy)
                .orElseThrow(() -> new UserNotFoundException("SUPER_ADMIN не найден: " + deletedBy));

        if (superAdmin.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Только SUPER_ADMIN может удалять пользователей");
        }

        if (id.equals(deletedBy)) {
            throw new InvalidOperationException("Нельзя удалить самого себя");
        }

        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Нельзя удалить SUPER_ADMIN");
        }

        reassignOwnedTasks(user, superAdmin);
        removeFromAssignedTasks(user);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        user.setUsername(user.getUsername() + "_deleted_" + System.currentTimeMillis());

        userRepository.save(user);

        log.info("Пользователь username={} удалил пользователя username={}", superAdmin.getUsername(), user.getUsername());
    }

    @Transactional
    public User restoreUser(Long id){
        User user = userRepository.findDeletedById(id)
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                );

        if (!user.isDeleted()) {
            throw new UserNotDeletedException("Пользователь не был удалён");
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);

        String restoredEmail = Arrays.stream(user.getEmail().split("_")).findFirst().get();
        String restoredUsername = Arrays.stream(user.getUsername().split("_")).findFirst().get();

        user.setUsername(restoredUsername);
        user.setEmail(restoredEmail);

        User restored = userRepository.save(user);
        log.info("Пользователь успешно восстановлен");
        return restored;
    }

    public Set<User> getAllDeletedUsers(){
        return userRepository.findAllDeleted();
    }

    private void reassignOwnedTasks(User oldOwner, User newOwner) {
        List<Task> tasks = taskRepository.findActiveByOwnerId(oldOwner.getId());

        if (tasks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        tasks.forEach(task -> {
            task.setOwner(newOwner);
            task.setUpdatedAt(now);
        });

        taskRepository.saveAll(tasks);
    }

    private void removeFromAssignedTasks(User user) {
        List<Task> tasks = taskRepository.findActiveByAssigneeId(user.getId());

        if (tasks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        tasks.forEach(task -> {
            task.getAssignees().removeIf(u -> u.getId().equals(user.getId()));
            task.setUpdatedAt(now);
        });

        taskRepository.saveAll(tasks);
    }
}
