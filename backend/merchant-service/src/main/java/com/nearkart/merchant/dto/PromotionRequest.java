package com.nearkart.merchant.dto;

import com.nearkart.merchant.entity.Promotion.PromoType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Promo type is required")
    private PromoType promoType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal minOrderValue = BigDecimal.ZERO;

    private BigDecimal maxDiscountCap;

    private String promoCode;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Integer usageLimit;
}
