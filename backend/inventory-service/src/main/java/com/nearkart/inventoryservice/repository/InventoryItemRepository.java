package com.nearkart.inventoryservice.repository;

import com.nearkart.inventoryservice.model.InventoryItem;
import com.nearkart.inventoryservice.model.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByShopId(Long shopId);

    Optional<InventoryItem> findByProductIdAndShopId(Long productId, Long shopId);

    List<InventoryItem> findByShopIdAndStatus(Long shopId, InventoryStatus status);

    @Query("SELECT i FROM InventoryItem i WHERE i.shopId = :shopId AND i.quantityAvailable <= i.lowStockThreshold")
    List<InventoryItem> findLowStockItemsByShop(@Param("shopId") Long shopId);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantityAvailable <= i.lowStockThreshold")
    List<InventoryItem> findAllLowStockItems();

    boolean existsByProductIdAndShopId(Long productId, Long shopId);

    List<InventoryItem> findByProductId(Long productId);
}
