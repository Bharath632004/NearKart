package com.nearkart.merchant.dto;

import com.nearkart.merchant.entity.SettlementStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SettlementResponse {
    private UUID id;
    private UUID merchantId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Integer totalOrders;
    private BigDecimal grossAmount;
    private BigDecimal platformFee;
    private BigDecimal taxDeducted;
    private BigDecimal netAmount;
    private SettlementStatus status;
    private String utrNumber;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
}
