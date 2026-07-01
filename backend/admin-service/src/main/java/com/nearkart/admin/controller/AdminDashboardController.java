package com.nearkart.admin.controller;

import com.nearkart.admin.client.MerchantServiceClient;
import com.nearkart.admin.client.UserServiceClient;
import com.nearkart.admin.dto.DashboardStatsDTO;
import com.nearkart.admin.service.AdminCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    // Fix #4: Inject AdminCouponService instead of CouponRepository directly (proper layered architecture)
    private final AdminCouponService couponService;
    private final UserServiceClient userServiceClient;
    private final MerchantServiceClient merchantServiceClient;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long activeCoupons = couponService.getActiveCoupons().size();
        DashboardStatsDTO stats = new DashboardStatsDTO(
                userServiceClient.getUserCount(),
                merchantServiceClient.getMerchantCount(),
                0L,   // TODO: Wire OrderServiceClient when order-service is available
                merchantServiceClient.getPendingApprovalCount(),
                0L,   // TODO: Wire RefundServiceClient when refund-service is available
                activeCoupons
        );
        return ResponseEntity.ok(stats);
    }
}
