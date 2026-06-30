package com.nearkart.admin.controller;

import com.nearkart.admin.dto.DashboardStatsDTO;
import com.nearkart.admin.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final CouponRepository couponRepository;

    // TODO: inject user/merchant/order feign clients and populate real counts
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long activeCoupons = couponRepository.findByActive(true).size();
        DashboardStatsDTO stats = new DashboardStatsDTO(
            0L, // totalUsers       - wire from user-service
            0L, // totalMerchants   - wire from merchant-service
            0L, // totalOrders      - wire from order-service
            0L, // pendingApprovals - wire from merchant-service
            0L, // openRefunds      - wire from payment-service
            activeCoupons
        );
        return ResponseEntity.ok(stats);
    }
}
