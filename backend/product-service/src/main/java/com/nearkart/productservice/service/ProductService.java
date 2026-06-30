package com.nearkart.productservice.service;

import com.nearkart.productservice.model.Product;
import com.nearkart.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllAvailable() {
        return productRepository.findByAvailableTrue();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getByMerchant(Long merchantId) {
        return productRepository.findByMerchantId(merchantId);
    }

    public List<Product> getByCategory(String category) {
        return productRepository.findByCategoryAndAvailableTrue(category);
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndAvailableTrue(name);
    }

    public Product create(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    public Product update(Long id, Product updated) {
        Product existing = getById(id);
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getPrice() != null) existing.setPrice(updated.getPrice());
        if (updated.getStockQuantity() != null) existing.setStockQuantity(updated.getStockQuantity());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getImageUrl() != null) existing.setImageUrl(updated.getImageUrl());
        existing.setAvailable(updated.isAvailable());
        return productRepository.save(existing);
    }

    public void softDelete(Long id) {
        Product p = getById(id);
        p.setAvailable(false);
        productRepository.save(p);
    }

    public void adjustStock(Long id, int quantity) {
        Product p = getById(id);
        int newStock = p.getStockQuantity() + quantity;
        if (newStock < 0) throw new RuntimeException("Insufficient stock for product id: " + id);
        p.setStockQuantity(newStock);
        productRepository.save(p);
    }
}
