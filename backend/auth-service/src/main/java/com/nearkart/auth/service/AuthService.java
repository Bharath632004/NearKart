package com.nearkart.auth.service;

import com.nearkart.auth.dto.AuthResponse;
import com.nearkart.auth.dto.LoginRequest;
import com.nearkart.auth.dto.RegisterRequest;
import com.nearkart.auth.dto.request.ResetPasswordRequest;
import com.nearkart.auth.entity.RefreshToken;
import com.nearkart.auth.entity.User;
import com.nearkart.auth.exception.AccountNotVerifiedException;
import com.nearkart.auth.exception.InvalidOtpException;
import com.nearkart.auth.exception.PhoneAlreadyExistsException;
import com.nearkart.auth.exception.UserNotFoundException;
import com.nearkart.auth.repository.UserRepository;
import com.nearkart.auth.security.JwtService;
import com.nearkart.auth.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       JwtService jwtService,
                       BCryptPasswordEncoder passwordEncoder,
                       RefreshTokenService refreshTokenService,
                       OtpService otpService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    // ------------------------------------------------------------------ register

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone number already registered");
        }

        String role = (request.getRole() == null || request.getRole().isBlank())
                ? "CUSTOMER"
                : request.getRole().toUpperCase();

        User user = new User(
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        userRepository.save(user);
        log.info("New user registered: phone={}, role={}", request.getPhone(), role);

        String accessToken = jwtUtil.generateToken(user.getPhone(), user.getRole());
        return new AuthResponse(accessToken);
    }

    // ------------------------------------------------------------------ login

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UserNotFoundException("Invalid phone or password"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AccountNotVerifiedException("Account is disabled. Contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UserNotFoundException("Invalid phone or password");
        }

        String accessToken = jwtUtil.generateToken(user.getPhone(), user.getRole());
        String ip = extractIp(httpRequest);
        String ua = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ip, ua);

        log.info("User logged in: phone={}, role={}", user.getPhone(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .role(user.getRole())
                .userId(String.valueOf(user.getId()))
                .expiresIn(900)
                .build();
    }

    // ------------------------------------------------------------------ refresh token

    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken, HttpServletRequest httpRequest) {
        RefreshToken storedToken = refreshTokenService.validateRefreshToken(rawRefreshToken);
        User user = storedToken.getUser();

        refreshTokenService.revokeRefreshToken(rawRefreshToken);
        String ip = extractIp(httpRequest);
        String ua = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user, ip, ua);

        String newAccessToken = jwtUtil.generateToken(user.getPhone(), user.getRole());
        log.info("Token refreshed for user: {}", user.getPhone());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .role(user.getRole())
                .userId(String.valueOf(user.getId()))
                .expiresIn(900)
                .build();
    }

    // ------------------------------------------------------------------ logout

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("User logged out via refresh token revocation");
    }

    // ------------------------------------------------------------------ send OTP

    public void sendOtp(String phone) {
        if (!userRepository.existsByPhone(phone)) {
            log.warn("OTP requested for non-existent phone: {}", phone);
            return;
        }
        String otp = otpService.generateAndStoreOtp(phone);
        // TODO: replace log with SMS gateway (MSG91 / Twilio) in production
        log.info("OTP for {} — integrate SMS gateway (dev mode only)", phone);
    }

    // ------------------------------------------------------------------ verify OTP

    public void verifyOtp(String phone, String otp) {
        boolean valid = otpService.verifyOtp(phone, otp);
        if (!valid) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }
    }

    // ------------------------------------------------------------------ forgot password

    public void forgotPassword(String phone) {
        userRepository.findByPhone(phone).ifPresent(user -> {
            String otp = otpService.generateAndStoreOtp("pwd_reset:" + phone);
            // TODO: replace log with SMS gateway in production
            log.info("Password reset OTP for {} — integrate SMS gateway (dev mode only)", phone);
        });
    }

    // ------------------------------------------------------------------ reset password

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        boolean valid = otpService.verifyOtp("pwd_reset:" + request.getPhone(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllUserTokens(user);
        log.info("Password reset successful for: {}", request.getPhone());
    }

    // ------------------------------------------------------------------ validate token

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    // ------------------------------------------------------------------ helper

    private String extractIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
