package com.nearkart.userservice;

import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.UserRepository;
import com.nearkart.userservice.service.UserService;
import com.nearkart.userservice.dto.UpdateProfileRequest;
import com.nearkart.userservice.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTests {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Bharath");
        mockUser.setEmail("bharath@nearkart.com");
        mockUser.setPassword(new BCryptPasswordEncoder().encode("password123"));
        mockUser.setPhone("9876543210");
        mockUser.setRole(User.Role.CUSTOMER);
        mockUser.setActive(true);
    }

    @Test
    void getProfile_ShouldReturnUser_WhenEmailExists() {
        when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
        User result = userService.getProfile("bharath@nearkart.com");
        assertNotNull(result);
        assertEquals("Bharath", result.getName());
    }

    @Test
    void getProfile_ShouldThrow_WhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getProfile("unknown@test.com"));
    }

    @Test
    void updateProfile_ShouldUpdateName() {
        when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Bharath Updated");

        User result = userService.updateProfile("bharath@nearkart.com", request);
        assertEquals("Bharath Updated", result.getName());
    }

    @Test
    void deleteAccount_ShouldDeactivateUser() {
        when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.deleteAccount("bharath@nearkart.com");

        assertFalse(mockUser.isActive());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void deactivateUser_ShouldSetActiveFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.deactivateUser(1L);

        assertFalse(mockUser.isActive());
    }
}
