package com.nearkart.merchant.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AnalyticsSummaryResponse {
    private int totalShops;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal pendingSettlement;
    private BigDecimal completedSettlement;
    private long activePromotions;
}
