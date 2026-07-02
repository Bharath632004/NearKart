package com.nearkart.userservice.service;

import com.nearkart.userservice.dto.*;
import com.nearkart.userservice.exception.AccountDeactivatedException;
import com.nearkart.userservice.exception.EmailAlreadyExistsException;
import com.nearkart.userservice.exception.InvalidPasswordException;
import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.UserRepository;
import com.nearkart.userservice.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Spy  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private final TokenPair tokenPair = new TokenPair("access.token", "refresh.token");

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
                .build();
    }

    @Test
    @DisplayName("register should always create CUSTOMER role regardless of input")
    void register_shouldAlwaysCreateCustomer() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(mockUser);
        when(jwtUtils.generateTokenPair(any(), any())).thenReturn(tokenPair);

        RegisterRequest req = new RegisterRequest();
        req.setName("Bharath");
        req.setEmail("bharath@nearkart.com");
        req.setPassword("password123");

        AuthResponse response = authService.register(req);

        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        verify(userRepository).save(argThat(u -> u.getRole() == User.Role.CUSTOMER));
    }

    @Test
    @DisplayName("register should throw EmailAlreadyExistsException for duplicate email")
    void register_shouldThrow_whenEmailExists() {
        when(userRepository.existsByEmail("bharath@nearkart.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest();
        req.setEmail("bharath@nearkart.com");
        req.setPassword("password");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("login should return AuthResponse with access and refresh tokens")
    void login_shouldReturnTokens_whenCredentialsValid() {
        when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateTokenPair(any(), any())).thenReturn(tokenPair);

        LoginRequest req = new LoginRequest();
        req.setEmail("bharath@nearkart.com");
        req.setPassword("password123");

        AuthResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token");
    }

    @Test
    @DisplayName("login should throw InvalidPasswordException on bad credentials")
    void login_shouldThrow_onBadCredentials() {
        doThrow(BadCredentialsException.class)
                .when(authenticationManager).authenticate(any());

        LoginRequest req = new LoginRequest();
        req.setEmail("bharath@nearkart.com");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("login should throw AccountDeactivatedException for inactive user")
    void login_shouldThrow_whenUserInactive() {
        mockUser.setActive(false);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("bharath@nearkart.com", null));
        when(userRepository.findByEmail("bharath@nearkart.com")).thenReturn(Optional.of(mockUser));

        LoginRequest req = new LoginRequest();
        req.setEmail("bharath@nearkart.com");
        req.setPassword("password123");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AccountDeactivatedException.class);
    }
}
