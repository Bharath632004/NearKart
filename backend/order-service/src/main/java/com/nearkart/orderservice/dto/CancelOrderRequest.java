package com.nearkart.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CancelOrderRequest {
    private String reason;
}
