package com.nearkart.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CouponRequestDTO {

    // Fix #6: Added proper validation constraints
    @NotBlank(message = "Coupon code is required")
    private String code;

    private String description;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.1", message = "Discount must be at least 0.1%")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private Double discountPercent;

    @PositiveOrZero(message = "Max discount amount must be zero or positive")
    private Double maxDiscountAmount;

    @PositiveOrZero(message = "Min order value must be zero or positive")
    private Double minOrderValue;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private int usageLimit;
}
