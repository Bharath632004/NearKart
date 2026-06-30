package com.nearkart.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalMerchants;
    private long totalOrders;
    private long pendingMerchantApprovals;
    private long openRefundRequests;
    private long activeCoupons;
}
