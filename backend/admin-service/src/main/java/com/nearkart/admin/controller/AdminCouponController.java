package com.nearkart.admin.controller;

import com.nearkart.admin.dto.CouponRequestDTO;
import com.nearkart.admin.model.Coupon;
import com.nearkart.admin.service.AdminCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCouponService couponService;

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getActiveCoupons());
    }

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(
            @Valid @RequestBody CouponRequestDTO dto,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(couponService.createCoupon(dto, admin.getUsername()));
    }

    @PutMapping("/{couponId}/deactivate")
    public ResponseEntity<String> deactivateCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal UserDetails admin) {
        couponService.deactivateCoupon(couponId, admin.getUsername());
        return ResponseEntity.ok("Coupon " + couponId + " deactivated");
    }
}
