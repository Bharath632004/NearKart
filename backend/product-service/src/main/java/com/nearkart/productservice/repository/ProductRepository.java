package com.nearkart.productservice.repository;

import com.nearkart.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopId(Long shopId);

    Page<Product> findByShopIdAndAvailableTrueOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> findByCategoryIdAndAvailableTrue(Long categoryId, Pageable pageable);

    List<Product> findByAvailableTrue();

    List<Product> findByShopIdAndAvailableTrue(Long shopId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max AND p.available = true ORDER BY p.price ASC")
    List<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT p FROM Product p WHERE p.shopId = :shopId AND p.price BETWEEN :min AND :max AND p.available = true ORDER BY p.price ASC")
    List<Product> findByShopAndPriceRange(@Param("shopId") Long shopId, @Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.shopId = :shopId AND p.stockQuantity <= :threshold")
    List<Product> findLowStockByShop(@Param("shopId") Long shopId, @Param("threshold") int threshold);

    long countByShopId(Long shopId);

    long countByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCaseAndShopId(String name, Long shopId);
}
