package com.nearkart.merchant.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsResponse {
    private long totalMerchants;
    private long activeMerchants;
    private long pendingKycMerchants;
    private long totalShops;
    private long activeShops;
    private long totalPromotions;
    private long activePromotions;
    private long pendingSettlements;
    private BigDecimal totalSettledAmount;
}
