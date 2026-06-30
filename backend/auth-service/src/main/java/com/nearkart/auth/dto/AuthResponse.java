package com.nearkart.auth.dto;

import com.nearkart.auth.entity.User.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn; // seconds
    private UUID userId;
    private String fullName;
    private String email;
    private Role role;
}
