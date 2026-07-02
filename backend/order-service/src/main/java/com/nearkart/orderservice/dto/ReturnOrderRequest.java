package com.nearkart.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReturnOrderRequest {

    @NotBlank(message = "Return reason is required")
    private String reason;
}
