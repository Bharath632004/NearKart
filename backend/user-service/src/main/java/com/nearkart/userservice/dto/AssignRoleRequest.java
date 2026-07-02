package com.nearkart.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AssignRoleRequest(
        @NotBlank(message = "Role is required")
        @Pattern(
            regexp = "CUSTOMER|MERCHANT|DELIVERY_AGENT|ADMIN",
            message = "Role must be one of: CUSTOMER, MERCHANT, DELIVERY_AGENT, ADMIN"
        )
        String role
) {}
