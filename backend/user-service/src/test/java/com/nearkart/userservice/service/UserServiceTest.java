package com.nearkart.userservice.service;

import com.nearkart.userservice.dto.ChangePasswordRequest;
import com.nearkart.userservice.dto.UpdateProfileRequest;
import com.nearkart.userservice.exception.InvalidPasswordException;
import com.nearkart.userservice.exception.PhoneAlreadyExistsException;
import com.nearkart.userservice.exception.UserNotFoundException;
import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User buildUser() {
        return User.builder().id(1L).name("Test").email("test@test.com")
                .password("hashed").phone("9999999999").role(User.Role.CUSTOMER).active(true).build();
    }

    @Test
    void getProfile_found_returnsResponse() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(buildUser()));
        assertNotNull(userService.getProfile("test@test.com"));
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getProfile("x@x.com"));
    }

    @Test
    void updateProfile_phoneConflict_throwsException() {
        User user = buildUser();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone("8888888888")).thenReturn(true);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setPhone("8888888888");
        assertThrows(PhoneAlreadyExistsException.class,
                () -> userService.updateProfile("test@test.com", req));
    }

    @Test
    void changePassword_wrongCurrent_throwsException() {
        User user = buildUser();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newpass123");
        assertThrows(InvalidPasswordException.class,
                () -> userService.changePassword("test@test.com", req));
    }

    @Test
    void deleteAccount_softDeletes() {
        User user = buildUser();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.deleteAccount("test@test.com");
        assertFalse(user.isActive());
    }

    @Test
    void deactivateUser_setsInactive() {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.deactivateUser(1L);
        assertFalse(user.isActive());
    }
}
