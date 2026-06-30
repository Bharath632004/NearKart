package com.nearkart.admin.service;

import com.nearkart.admin.dto.CouponRequestDTO;
import com.nearkart.admin.model.Coupon;
import com.nearkart.admin.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private final CouponRepository couponRepository;
    private final AuditLogService auditLogService;

    public Coupon createCoupon(CouponRequestDTO dto, String adminUsername) {
        Coupon coupon = new Coupon();
        coupon.setCode(dto.getCode().toUpperCase());
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountPercent(dto.getDiscountPercent());
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setExpiryDate(dto.getExpiryDate());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setActive(true);
        Coupon saved = couponRepository.save(coupon);
        auditLogService.log(adminUsername, "CREATE_COUPON", "COUPON", saved.getId(), "Code: " + saved.getCode());
        return saved;
    }

    public void deactivateCoupon(Long couponId, String adminUsername) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setActive(false);
        couponRepository.save(coupon);
        auditLogService.log(adminUsername, "DEACTIVATE_COUPON", "COUPON", couponId, null);
    }

    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByActive(true);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
}
