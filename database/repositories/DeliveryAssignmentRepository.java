package com.nearkart.database.repositories;

import com.nearkart.domain.delivery.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    Optional<DeliveryAssignment> findByOrderId(UUID orderId);

    List<DeliveryAssignment> findByPartnerIdAndStatusIn(UUID partnerId, List<String> statuses);

    // Unassigned orders ready for dispatch
    @Query(value = "SELECT o.id FROM orders o "
                 + "LEFT JOIN delivery_assignments da ON da.order_id = o.id "
                 + "WHERE o.status = 'READY' AND da.id IS NULL "
                 + "ORDER BY o.created_at ASC LIMIT :limit",
           nativeQuery = true)
    List<UUID> findUnassignedReadyOrders(@Param("limit") int limit);

    // Update status
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryAssignment da SET da.status = :status WHERE da.orderId = :orderId")
    void updateStatus(@Param("orderId") UUID orderId, @Param("status") String status);
}
