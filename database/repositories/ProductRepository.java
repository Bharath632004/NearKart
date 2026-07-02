package com.nearkart.database.repositories;

import com.nearkart.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Product entity.
 * Soft-delete aware: all queries filter deleted_at IS NULL.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // All active products for a shop
    Page<Product> findByShopIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID shopId, Pageable pageable);

    // Full-text search using PostgreSQL tsvector index
    @Query(value = "SELECT * FROM products "
                 + "WHERE to_tsvector('english', name) @@ plainto_tsquery('english', :query) "
                 + "AND is_active = TRUE AND deleted_at IS NULL "
                 + "ORDER BY avg_rating DESC LIMIT :limit",
           nativeQuery = true)
    List<Product> fullTextSearch(@Param("query") String query,
                                 @Param("limit") int limit);

    // Products by category (active only)
    Page<Product> findByCategoryIdAndIsActiveTrueAndDeletedAtIsNull(
            Integer categoryId, Pageable pageable);

    // Featured products for a shop
    List<Product> findByShopIdAndIsFeaturedTrueAndIsActiveTrueAndDeletedAtIsNull(
            UUID shopId);

    // Soft delete
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDelete(@Param("id") UUID id);

    // Low stock products for a shop
    @Query(value = "SELECT p.* FROM products p "
                 + "JOIN inventory i ON i.product_id = p.id "
                 + "WHERE p.shop_id = :shopId AND i.quantity <= i.low_stock_alert "
                 + "AND p.is_active = TRUE AND p.deleted_at IS NULL",
           nativeQuery = true)
    List<Product> findLowStockProducts(@Param("shopId") UUID shopId);
}
