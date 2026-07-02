package com.nearkart.database.repositories;

import com.nearkart.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payment entity.
 * Pessimistic lock used during gateway callback processing.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    List<Payment> findByOrderIdAndStatus(UUID orderId, String status);

    // Pessimistic lock for webhook idempotency
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.gatewayPaymentId = :gwPayId")
    Optional<Payment> findByGatewayPaymentIdWithLock(@Param("gwPayId") String gwPayId);

    // All failed payments for retry job
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' ORDER BY p.createdAt DESC")
    List<Payment> findAllFailed();
}
