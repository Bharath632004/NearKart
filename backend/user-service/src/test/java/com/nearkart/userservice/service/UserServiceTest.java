package com.nearkart.userservice.service;

import com.nearkart.userservice.dto.AssignRoleRequest;
import com.nearkart.userservice.dto.ChangePasswordRequest;
import com.nearkart.userservice.dto.UpdateProfileRequest;
import com.nearkart.userservice.dto.UserResponse;
import com.nearkart.userservice.exception.InvalidPasswordException;
import com.nearkart.userservice.exception.PhoneAlreadyExistsException;
import com.nearkart.userservice.exception.UserNotFoundException;
import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Bharath")
                .email("bharath@nearkart.com")
                .password(new BCryptPasswordEncoder().encode("password123"))
                .phone("9876543210")
                .role(User.Role.CUSTOMER)
                .active(true)
                .version(0L)
                .build();
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("should return UserResponse when email exists")
        void shouldReturnUserResponse_whenEmailExists() {
            when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));

            UserResponse result = userService.getProfile("bharath@nearkart.com");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Bharath");
            assertThat(result.getEmail()).isEqualTo("bharath@nearkart.com");
            assertThat(result.getRole()).isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when email not found")
        void shouldThrow_whenEmailNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile("unknown@test.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("should update name successfully")
        void shouldUpdateName_successfully() {
            when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setName("Bharath Updated");

            UserResponse result = userService.updateProfile("bharath@nearkart.com", request);

            assertThat(result).isNotNull();
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("should throw PhoneAlreadyExistsException when phone is taken")
        void shouldThrow_whenPhoneTaken() {
            when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
            when(userRepository.existsByPhone("9999999999")).thenReturn(true);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setPhone("9999999999");

            assertThatThrownBy(() -> userService.updateProfile("bharath@nearkart.com", request))
                    .isInstanceOf(PhoneAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("should change password when current password matches")
        void shouldChangePassword_whenCurrentPasswordMatches() {
            when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("password123");
            request.setNewPassword("newSecurePass456!");

            userService.changePassword("bharath@nearkart.com", request);

            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("should throw InvalidPasswordException when current password is wrong")
        void shouldThrow_whenCurrentPasswordWrong() {
            when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("wrongPassword");
            request.setNewPassword("newPass");

            assertThatThrownBy(() -> userService.changePassword("bharath@nearkart.com", request))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @Test
        @DisplayName("should deactivate user account")
        void shouldDeactivateUser() {
            when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            userService.deleteAccount("bharath@nearkart.com");

            assertThat(mockUser.isActive()).isFalse();
            verify(userRepository).save(mockUser);
        }
    }

    @Nested
    @DisplayName("assignRole")
    class AssignRole {

        @Test
        @DisplayName("should assign MERCHANT role to user")
        void shouldAssignMerchantRole() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            AssignRoleRequest request = new AssignRoleRequest("MERCHANT");
            UserResponse result = userService.assignRole(1L, request);

            assertThat(mockUser.getRole()).isEqualTo(User.Role.MERCHANT);
            verify(userRepository).save(mockUser);
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("should return paginated user responses")
        void shouldReturnPaginatedUsers() {
            Page<User> page = new PageImpl<>(List.of(mockUser), PageRequest.of(0, 20), 1);
            when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

            Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 20));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("bharath@nearkart.com");
        }
    }
}
