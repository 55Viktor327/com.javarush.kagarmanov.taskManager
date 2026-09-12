package com.javarush.service;

import com.javarush.dto.ChangePasswordDto;
import com.javarush.dto.ChangeRoleDto;
import com.javarush.dto.UserRegistrationDto;
import com.javarush.dto.UserUpdateDto;
import com.javarush.exeption.UserAlreadyExistsException;
import com.javarush.exeption.UserNotFoundException;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import com.javarush.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsByUserName(dto.getUserName())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                            .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                            );
    }

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                            .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с email: " + email + " не найден")
                            );
    }

    public User getUserByName(String userName) {
        return userRepository.findUserByUserName(userName)
                            .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с именем: " + userName + " не найден")
                            );
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findAllUsersByRole(role);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return (Page<User>) userRepository.findAll();
    }

    public User updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findUserById(id)
                                .orElseThrow(
                                    () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                                );

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
            }

            user.setEmail(dto.getEmail());
        }

        if (dto.getUserName() != null && !dto.getUserName().isEmpty()) {
            if (userRepository.existsByUserNameAndIdNot(dto.getUserName(), id)) {
                throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
            }

            user.setUserName(dto.getUserName());
        }

        userRepository.save(user);
        return user;
    }

    public void deleteUser(Long id) {
        User deletedUser = userRepository.findUserById(id)
                                        .orElseThrow(
                                                () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                                        );

        userRepository.delete(deletedUser);
        System.out.printf("Пользователь с %d успешно удален", id);
    }

    public void changeUserPassword(Long id, ChangePasswordDto dto) {
        User userWithOldPassword = userRepository.findUserById(id)
                                                .orElseThrow(
                                                        () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                                                );

        if (!passwordEncoder.matches(dto.getOldPassword(), userWithOldPassword.getPassword())) {
            System.out.println("Введен неверный пароль");
        }

        String updatedPassword = passwordEncoder.encode(dto.getNewPassword());
        userWithOldPassword.setPassword(updatedPassword);
        userRepository.save(userWithOldPassword);
        System.out.println("Пароль успешно изменен");
    }

    public void changeUserRole(Long id, ChangeRoleDto dto) {
        User updetedRoleUser = userRepository.findUserById(id)
                                            .orElseThrow(
                                                    () -> new UserNotFoundException("Пользователь с id:" + id + " не найден")
                                            );

        updetedRoleUser.setRole(dto.getRole());
        userRepository.save(updetedRoleUser);
    }
}
