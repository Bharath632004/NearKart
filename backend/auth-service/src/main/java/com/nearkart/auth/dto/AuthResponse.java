package com.nearkart.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String role;
    private String userId;
    private long expiresIn;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
