package com.nearkart.merchant.repository;

import com.nearkart.merchant.entity.Settlement;
import com.nearkart.merchant.entity.Settlement.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    long countByStatus(SettlementStatus status);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM Settlement s WHERE s.merchant.id = :merchantId AND s.status = 'COMPLETED'")
    BigDecimal getTotalSettledAmountByMerchant(@Param("merchantId") UUID merchantId);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM Settlement s WHERE s.merchant.id = :merchantId AND s.status = 'PENDING'")
    BigDecimal getPendingSettlementAmountByMerchant(@Param("merchantId") UUID merchantId);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM Settlement s WHERE s.status = 'COMPLETED'")
    BigDecimal getTotalCompletedSettlements();
}
