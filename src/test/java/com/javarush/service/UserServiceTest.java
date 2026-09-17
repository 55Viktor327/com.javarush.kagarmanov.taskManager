package com.javarush.service;

import com.javarush.dto.user.ChangePasswordDto;
import com.javarush.dto.user.ChangeRoleDto;
import com.javarush.dto.user.UserRegistrationDto;
import com.javarush.dto.user.UserUpdateDto;
import com.javarush.exception.*;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private User superAdmin;
    private User admin;
    private UserRegistrationDto registrationDto;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setEmail("user1@mail.com");
        user.setPassword("encoded_password");
        user.setRole(Role.USER);
        user.setDeleted(false);

        superAdmin = new User();
        superAdmin.setId(2L);
        superAdmin.setUsername("super_admin");
        superAdmin.setEmail("super@mail.com");
        superAdmin.setRole(Role.SUPER_ADMIN);

        admin = new User();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setEmail("admin@mail.com");
        admin.setRole(Role.ADMIN);

        registrationDto = new UserRegistrationDto();
        registrationDto.setUserName("new_user");
        registrationDto.setEmail("new@mail.com");
        registrationDto.setPassword("Pass123@");
    }

    @Nested
    @DisplayName("createUser()")
    public class CreateUserTests {

        @Test
        @DisplayName("Успешное создание")
        public void createUser_Success() {
            when(userRepository.existsActiveByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.existsActiveByUsername("new_user")).thenReturn(false);
            when(passwordEncoder.encode("Pass123@")).thenReturn("encoded_pass");
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setId(1L);
                return u;
            });

            User result = userService.createUser(registrationDto);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("new_user");
            assertThat(result.getEmail()).isEqualTo("new@mail.com");
            assertThat(result.getPassword()).isEqualTo("encoded_pass");
            assertThat(result.getRole()).isEqualTo(Role.GUEST);

            verify(passwordEncoder).encode("Pass123@");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Email занят → UserAlreadyExistsException")
        public void createUser_EmailExists_Throws() {
            when(userRepository.existsActiveByEmail("new@mail.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(registrationDto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("email");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Username занят → UserAlreadyExistsException")
        public void createUser_UsernameExists_Throws() {
            when(userRepository.existsActiveByEmail("new@mail.com")).thenReturn(false);
            when(userRepository.existsActiveByUsername("new_user")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(registrationDto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("именем");
        }

        @Test
        @DisplayName("Пароль шифруется")
        public void createUser_PasswordEncoded() {
            when(userRepository.existsActiveByEmail(any())).thenReturn(false);
            when(userRepository.existsActiveByUsername(any())).thenReturn(false);
            when(passwordEncoder.encode("Pass123@")).thenReturn("BCRYPT_HASH");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = userService.createUser(registrationDto);

            assertThat(result.getPassword()).isEqualTo("BCRYPT_HASH");
            assertThat(result.getPassword()).isNotEqualTo("Pass123@");
        }
    }

    @Test
    @DisplayName("Успешное получение")
    public void getUserById_Success() {
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Не найден → UserNotFoundException")
    void getUserById_NotFound_Throws() {
        when(userRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Успешное обновление email")
    void updateUser_Email_Success() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("new@mail.com");

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsActiveByEmailAndIdNot("new@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, dto);

        assertThat(result.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    @DisplayName("Email занят другим → UserAlreadyExistsException")
    void updateUser_EmailTaken_Throws() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("taken@mail.com");

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsActiveByEmailAndIdNot("taken@mail.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, dto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Null email → не меняется")
    void updateUser_NullEmail_NotChanged() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail(null);

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, dto);

        assertThat(result.getEmail()).isEqualTo("user1@mail.com");
        verify(userRepository, never()).existsActiveByEmailAndIdNot(any(), any());
    }

    @Test
    @DisplayName("Успешная смена пароля")
    void changePassword_Success() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("OldPass123@");
        dto.setNewPassword("NewPass123@");

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass123@", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewPass123@")).thenReturn("new_encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.changeUserPassword(1L, dto);

        assertThat(user.getPassword()).isEqualTo("new_encoded");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Неверный старый пароль → InvalidPasswordException")
    void changePassword_WrongOld_Throws() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("WrongPass");
        dto.setNewPassword("NewPass123@");

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changeUserPassword(1L, dto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная смена роли")
    void changeRole_Success() {
        ChangeRoleDto dto = new ChangeRoleDto();
        dto.setRole(Role.ADMIN);

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.changeUserRole(1L, dto);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Успешное удаление")
    void softDelete_Success() {
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findActiveById(2L)).thenReturn(Optional.of(superAdmin));
        when(taskRepository.findActiveByOwnerId(1L)).thenReturn(List.of());
        when(taskRepository.findActiveByAssigneeId(1L)).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.softDeleteUser(1L, 2L);

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedBy()).isEqualTo(2L);
        assertThat(user.getEmail()).contains("_deleted_");
        assertThat(user.getUsername()).contains("_deleted_");
    }

    @Test
    @DisplayName("Уже удалён → UserAlreadyDeleteException")
    void softDelete_AlreadyDeleted_Throws() {
        user.setDeleted(true);
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.softDeleteUser(1L, 2L))
                .isInstanceOf(UserAlreadyDeleteException.class);
    }

    @Test
    @DisplayName("Не SUPER_ADMIN → AccessDeniedException")
    void softDelete_NotSuperAdmin_Throws() {
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findActiveById(3L)).thenReturn(Optional.of(admin));   // ADMIN

        assertThatThrownBy(() -> userService.softDeleteUser(1L, 3L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Удаление себя → InvalidOperationException")
    void softDelete_Self_Throws() {
        User self = new User();
        self.setId(5L);
        self.setRole(Role.SUPER_ADMIN);
        self.setEmail("self@mail.com");
        self.setUsername("self");

        when(userRepository.findActiveById(5L)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> userService.softDeleteUser(5L, 5L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("самого себя");
    }

    @Test
    @DisplayName("Удаление SUPER_ADMIN → AccessDeniedException")
    void softDelete_SuperAdmin_Throws() {
        when(userRepository.findActiveById(2L)).thenReturn(Optional.of(superAdmin));
        when(userRepository.findActiveById(2L)).thenReturn(Optional.of(superAdmin));

        User anotherSuper = new User();
        anotherSuper.setId(10L);
        anotherSuper.setRole(Role.SUPER_ADMIN);

        when(userRepository.findActiveById(10L)).thenReturn(Optional.of(anotherSuper));

        assertThatThrownBy(() -> userService.softDeleteUser(10L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("SUPER_ADMIN");
    }

    @Test
    @DisplayName("Успешное восстановление")
    void restore_Success() {
        user.setDeleted(true);
        user.setEmail("user1@mail.com_deleted_123");
        user.setUsername("user1_deleted_123");

        when(userRepository.findDeletedById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.restoreUser(1L);

        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getEmail()).isEqualTo("user1@mail.com");
        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("Не был удалён → UserNotDeletedException")
    void restore_NotDeleted_Throws() {
        user.setDeleted(false);
        when(userRepository.findDeletedById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.restoreUser(1L))
                .isInstanceOf(UserNotDeletedException.class);
    }

    @Test
    @DisplayName("Возвращает удалённых")
    void getAllDeleted_Success() {
        user.setDeleted(true);
        Set<User> deleted = Set.of(user);

        when(userRepository.findAllDeleted()).thenReturn(deleted);

        Set<User> result = userService.getAllDeletedUsers();

        assertThat(result).hasSize(1);
        assertThat(result).contains(user);
    }
}