package com.nearkart.merchant.dto;

import com.nearkart.merchant.entity.PromoType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PromotionResponse {
    private UUID id;
    private UUID shopId;
    private String title;
    private String description;
    private PromoType promoType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountCap;
    private String promoCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private Integer usageLimit;
    private Integer usageCount;
    private LocalDateTime createdAt;
}
