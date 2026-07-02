package com.nearkart.auth.controller;

import com.nearkart.auth.dto.AuthResponse;
import com.nearkart.auth.dto.LoginRequest;
import com.nearkart.auth.dto.RegisterRequest;
import com.nearkart.auth.dto.request.ForgotPasswordRequest;
import com.nearkart.auth.dto.request.LogoutRequest;
import com.nearkart.auth.dto.request.OtpVerifyRequest;
import com.nearkart.auth.dto.request.RefreshTokenRequest;
import com.nearkart.auth.dto.request.ResetPasswordRequest;
import com.nearkart.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh, OTP, password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with phone + password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rotate refresh token and obtain new access token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken(), httpRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — revokes the refresh token")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to a registered phone number")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestParam String phone) {
        authService.sendOtp(phone);
        return ResponseEntity.ok(Map.of("message", "OTP sent if phone is registered"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP for a phone number")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset OTP")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getPhone());
        return ResponseEntity.ok(Map.of("message", "If account exists, reset OTP has been sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token — used internally by API Gateway")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(token)));
    }
}
