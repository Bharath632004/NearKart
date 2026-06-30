package in.nearkart.auth.service.impl;

import in.nearkart.auth.dto.request.*;
import in.nearkart.auth.dto.response.AuthResponse;
import in.nearkart.auth.entity.*;
import in.nearkart.auth.exception.*;
import in.nearkart.auth.repository.*;
import in.nearkart.auth.service.AuthService;
import in.nearkart.auth.service.OtpService;
import in.nearkart.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // FIX: Read token expiry values from config to keep DB and JWT in sync
    @Value("${nearkart.jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Value("${nearkart.jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone number already registered: " + request.getPhone());
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isVerified(false)
                .build();

        userRepository.save(user);
        log.info("New user registered: phone={}, role={}", request.getPhone(), request.getRole());

        otpService.sendOtp(request.getPhone(), OtpPurpose.REGISTRATION);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        refreshTokenRepository.revokeAllUserTokens(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);

        log.info("User logged in: phone={}", request.getPhone());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public void sendOtp(String phone, String purpose) {
        OtpPurpose otpPurpose = OtpPurpose.valueOf(purpose.toUpperCase());
        otpService.sendOtp(phone, otpPurpose);
    }

    @Override
    public boolean verifyOtp(OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request.getPhone(), request.getOtp(), request.getPurpose());
        if (verified && request.getPurpose() == OtpPurpose.REGISTRATION) {
            userRepository.findByPhone(request.getPhone()).ifPresent(user -> {
                user.setIsVerified(true);
                userRepository.save(user);
            });
        }
        return verified;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndIsRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = storedToken.getUser();
        storedToken.setIsRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        saveRefreshToken(user, newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByPhone(request.getPhone())) {
            throw new UserNotFoundException("No account found with this phone number");
        }
        otpService.sendOtp(request.getPhone(), OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        boolean valid = otpService.verifyOtp(request.getPhone(), request.getOtp(), OtpPurpose.FORGOT_PASSWORD);
        if (!valid) throw new InvalidOtpException("Invalid or expired OTP");

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Password reset for phone={}", request.getPhone());
    }

    private void saveRefreshToken(User user, String token) {
        // FIX: Derive DB expiry from config value instead of hardcoding plusDays(7)
        // This keeps the DB record in sync with the actual JWT expiry in JwtUtil
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        // FIX: Use config-driven expiry instead of hardcoded 900L seconds
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs / 1000)
                .userId(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .build();
    }
}
