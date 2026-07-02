package com.nearkart.database.repositories;

import com.nearkart.domain.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Inventory entity.
 * Uses optimistic locking via @Version field on Inventory entity.
 * Pessimistic lock available for critical stock reservation flows.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductId(UUID productId);

    // Pessimistic lock for stock reservation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") UUID productId);

    // Bulk deduct – used during order placement (native for performance)
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET quantity = quantity - :qty, "
                 + "version = version + 1, updated_at = NOW() "
                 + "WHERE product_id = :productId AND quantity >= :qty AND version = :version",
           nativeQuery = true)
    int deductStock(@Param("productId") UUID productId,
                    @Param("qty") int qty,
                    @Param("version") int version);

    // Restock
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET quantity = quantity + :qty, "
                 + "version = version + 1, updated_at = NOW() "
                 + "WHERE product_id = :productId",
           nativeQuery = true)
    void restock(@Param("productId") UUID productId,
                 @Param("qty") int qty);
}
