package com.nearkart.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String merchantId;
    private String customerId;
    private String status; // PLACED, CONFIRMED, DELIVERED, CANCELLED
    private BigDecimal orderAmount;
    private BigDecimal commissionAmount;
    private int itemCount;
    private LocalDateTime eventTime;
    private Long deliveryTimeMinutes; // null if not delivered yet
    private boolean onTime;
}
