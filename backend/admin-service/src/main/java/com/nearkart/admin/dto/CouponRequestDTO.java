package com.nearkart.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CouponRequestDTO {

    @NotBlank(message = "Coupon code is required")
    private String code;

    private String description;

    @NotNull(message = "Discount percent is required")
    private Double discountPercent;

    private Double maxDiscountAmount;

    private Double minOrderValue;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private int usageLimit;
}
