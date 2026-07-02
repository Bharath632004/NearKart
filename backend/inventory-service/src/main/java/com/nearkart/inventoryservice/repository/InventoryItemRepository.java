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

    List<InventoryItem> findByShopIdAndStatusOrderByProductNameAsc(Long shopId, InventoryStatus status);

    boolean existsByProductIdAndShopId(Long productId, Long shopId);

    boolean existsBySkuAndIdNot(String sku, Long id);

    List<InventoryItem> findByProductId(Long productId);

    long countByShopId(Long shopId);

    long countByShopIdAndStatus(Long shopId, InventoryStatus status);

    /** Low-stock items for a shop — only ACTIVE items that haven't already hit OUT_OF_STOCK */
    @Query("SELECT i FROM InventoryItem i " +
           "WHERE i.shopId = :shopId " +
           "AND i.status = 'ACTIVE' " +
           "AND i.quantityAvailable <= i.lowStockThreshold " +
           "AND i.quantityAvailable > 0 " +
           "ORDER BY i.quantityAvailable ASC")
    List<InventoryItem> findLowStockActiveItemsByShop(@Param("shopId") Long shopId);

    /** Low-stock items across all shops — only ACTIVE items */
    @Query("SELECT i FROM InventoryItem i " +
           "WHERE i.status = 'ACTIVE' " +
           "AND i.quantityAvailable <= i.lowStockThreshold " +
           "AND i.quantityAvailable > 0 " +
           "ORDER BY i.quantityAvailable ASC")
    List<InventoryItem> findAllLowStockActiveItems();

    /** Kept for backward-compatibility — delegates to ACTIVE-only query */
    @Deprecated
    @Query("SELECT i FROM InventoryItem i WHERE i.shopId = :shopId AND i.quantityAvailable <= i.lowStockThreshold")
    List<InventoryItem> findLowStockItemsByShop(@Param("shopId") Long shopId);

    /** Kept for backward-compatibility */
    @Deprecated
    @Query("SELECT i FROM InventoryItem i WHERE i.quantityAvailable <= i.lowStockThreshold")
    List<InventoryItem> findAllLowStockItems();
}
