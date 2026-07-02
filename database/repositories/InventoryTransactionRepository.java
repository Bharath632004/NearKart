package com.nearkart.database.repositories;

import com.nearkart.domain.inventory.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(
            UUID productId, Pageable pageable);

    // Total deducted for a product in an order (for return flow)
    @Query(value = "SELECT COALESCE(SUM(ABS(change_qty)), 0) FROM inventory_transactions "
                 + "WHERE product_id = :productId AND reference_id = :orderId AND reason = 'ORDER'",
           nativeQuery = true)
    int getTotalDeductedForOrder(@Param("productId") UUID productId,
                                 @Param("orderId") UUID orderId);
}
