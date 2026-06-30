package com.nearkart.auth.service;

import com.nearkart.auth.dto.request.*;
import com.nearkart.auth.dto.response.*;
import com.nearkart.auth.entity.RefreshToken;
import com.nearkart.auth.entity.Role;
import com.nearkart.auth.entity.User;
import com.nearkart.auth.exception.*;
import com.nearkart.auth.repository.RefreshTokenRepository;
import com.nearkart.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone already registered: " + request.getPhone());
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .enabled(false)
                .build();

        userRepository.save(user);
        String otp = otpService.generateAndStoreOtp(request.getEmail());
        log.info("User registered, OTP sent to: {}", request.getEmail());
        // TODO: send OTP via SMS/Email notification service

        return RegisterResponse.builder()
                .message("Registration successful. OTP sent to " + request.getEmail())
                .userId(user.getId().toString())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify OTP.");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        boolean valid = otpService.verifyOtp(request.getIdentifier(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        return OtpVerifyResponse.builder()
                .message("OTP verified successfully")
                .verified(true)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        User user = storedToken.getUser();
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("User logged out: {}", email);
    }

    private AuthResponse generateAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "userId", user.getId().toString()
        );
        String accessToken = jwtService.generateAccessToken(claims, user);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
