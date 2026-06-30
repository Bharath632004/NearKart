package com.nearkart.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String message;
    private String userId;
}
