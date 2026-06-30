package com.nearkart.productservice.repository;

import com.nearkart.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(Long shopId);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByAvailableTrue();
    List<Product> findByShopIdAndAvailableTrue(Long shopId);
    List<Product> findByNameContainingIgnoreCase(String name);
}
