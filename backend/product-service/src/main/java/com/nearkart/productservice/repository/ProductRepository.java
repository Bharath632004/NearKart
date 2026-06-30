package com.nearkart.productservice.repository;

import com.nearkart.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopId(Long shopId);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByAvailableTrue();

    List<Product> findByShopIdAndAvailableTrue(Long shopId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max AND p.available = true")
    List<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

    long countByShopId(Long shopId);
}
