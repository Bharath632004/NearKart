package in.nearkart.auth.service;

import in.nearkart.auth.dto.request.LoginRequest;
import in.nearkart.auth.dto.request.RegisterRequest;
import in.nearkart.auth.dto.response.AuthResponse;
import in.nearkart.auth.entity.Role;
import in.nearkart.auth.entity.User;
import in.nearkart.auth.exception.UserAlreadyExistsException;
import in.nearkart.auth.repository.RefreshTokenRepository;
import in.nearkart.auth.repository.UserRepository;
import in.nearkart.auth.service.impl.AuthServiceImpl;
import in.nearkart.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private OtpService otpService;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Bharath C");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Test@1234");
        registerRequest.setRole(Role.CUSTOMER);
    }

    @Test
    void register_Success() {
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(new User());
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");
        doNothing().when(otpService).sendOtp(any(), any());

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void register_ThrowsException_WhenPhoneAlreadyExists() {
        when(userRepository.existsByPhone(any())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
    }
}
