package com.nearkart.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpVerifyResponse {
    private String message;
    private boolean verified;
}
