package com.nearkart.repository;

import com.nearkart.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    /** Count how many times a user has used a specific coupon */
    long countByCouponIdAndUserId(UUID couponId, UUID userId);

    /** Check whether this exact order already redeemed a coupon */
    boolean existsByCouponIdAndOrderId(UUID couponId, UUID orderId);

    /** Total platform-wide usage of a coupon */
    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.couponId = :couponId")
    long countTotalUsage(@Param("couponId") UUID couponId);
}
