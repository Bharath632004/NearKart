package com.nearkart.productservice.repository;

import com.nearkart.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByAvailableTrue();
    List<Product> findByMerchantId(Long merchantId);
    List<Product> findByCategoryAndAvailableTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name);
}
