package com.nearkart.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String userId;
    private String email;
    private String fullName;
    private String role;
}
