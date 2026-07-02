package com.nearkart.database.repositories;

import com.nearkart.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Order entity.
 * Uses optimistic locking (@Version) via JPA; pessimistic lock available
 * via @Lock for critical payment-confirmation flows.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Customer orders – paginated, newest first
    Page<Order> findByCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID customerId, Pageable pageable);

    // Shop orders by status
    List<Order> findByShopIdAndStatusAndDeletedAtIsNull(
            UUID shopId, String status);

    // Pending orders for a shop older than X minutes (SLA breach detection)
    @Query("SELECT o FROM Order o WHERE o.shopId = :shopId AND o.status = 'PENDING' "
         + "AND o.createdAt < :threshold AND o.deletedAt IS NULL")
    List<Order> findSLABreachedOrders(@Param("shopId") UUID shopId,
                                      @Param("threshold") LocalDateTime threshold);

    // Pessimistic lock for payment confirmation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") UUID id);

    // Revenue summary for a shop
    @Query(value = "SELECT COALESCE(SUM(total_amount),0) FROM orders "
                 + "WHERE shop_id = :shopId AND payment_status = 'PAID' "
                 + "AND deleted_at IS NULL AND created_at BETWEEN :from AND :to",
           nativeQuery = true)
    Double getShopRevenue(@Param("shopId") UUID shopId,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);

    // Count by status for dashboard
    long countByShopIdAndStatusAndDeletedAtIsNull(UUID shopId, String status);
}
