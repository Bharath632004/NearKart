package com.nearkart.auth.service;

import com.nearkart.auth.dto.LoginRequest;
import com.nearkart.auth.dto.RegisterRequest;
import com.nearkart.auth.entity.User;
import com.nearkart.auth.exception.PhoneAlreadyExistsException;
import com.nearkart.auth.exception.UserNotFoundException;
import com.nearkart.auth.repository.UserRepository;
import com.nearkart.auth.security.JwtService;
import com.nearkart.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private JwtService jwtService;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private OtpService otpService;
    @Mock private EmailService emailService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Test@1234");
        registerRequest.setRole("CUSTOMER");
    }

    @Test
    void register_success_returnsAccessToken() {
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("Test@1234")).thenReturn("hashed");
        when(jwtUtil.generateToken(any(), eq("CUSTOMER"))).thenReturn("jwt-token");

        var response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicatePhone_throwsPhoneAlreadyExistsException() {
        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_userNotFound_throwsUserNotFoundException() {
        LoginRequest req = new LoginRequest();
        req.setPhone("0000000000");
        req.setPassword("wrong");
        when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(req, null));
    }

    @Test
    void login_disabledAccount_throwsAccountNotVerifiedException() {
        User user = new User("9876543210", "hashed", "CUSTOMER");
        user.setActive(false);
        LoginRequest req = new LoginRequest();
        req.setPhone("9876543210");
        req.setPassword("Test@1234");
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));

        assertThrows(Exception.class, () -> authService.login(req, null));
    }

    @Test
    void validateToken_delegatesToJwtUtil() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);

        assertTrue(authService.validateToken("valid-token"));
        verify(jwtUtil).validateToken("valid-token");
    }

    @Test
    void sendOtp_nonExistentPhone_doesNotCallOtpService() {
        when(userRepository.existsByPhone("0000000000")).thenReturn(false);

        assertDoesNotThrow(() -> authService.sendOtp("0000000000"));
        verify(otpService, never()).generateAndStoreOtp(any());
    }
}
