package com.nearkart.merchant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for admin-triggered settlement creation.
 */
@Data
public class SettlementRequest {

    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;

    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;

    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;

    @Min(value = 0, message = "Total orders cannot be negative")
    @NotNull(message = "Total orders is required")
    private Integer totalOrders;

    @DecimalMin(value = "0.0", inclusive = false, message = "Gross amount must be positive")
    @NotNull(message = "Gross amount is required")
    private BigDecimal grossAmount;
}
