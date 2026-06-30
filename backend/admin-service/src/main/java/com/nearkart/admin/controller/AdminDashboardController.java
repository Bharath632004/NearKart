package com.nearkart.admin.controller;

import com.nearkart.admin.client.MerchantServiceClient;
import com.nearkart.admin.client.UserServiceClient;
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
    private final UserServiceClient userServiceClient;
    private final MerchantServiceClient merchantServiceClient;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long activeCoupons = couponRepository.findByActive(true).size();
        DashboardStatsDTO stats = new DashboardStatsDTO(
            userServiceClient.getUserCount(),
            merchantServiceClient.getMerchantCount(),
            0L,
            merchantServiceClient.getPendingApprovalCount(),
            0L,
            activeCoupons
        );
        return ResponseEntity.ok(stats);
    }
}
