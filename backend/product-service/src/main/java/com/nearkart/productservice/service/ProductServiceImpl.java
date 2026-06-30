package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.*;
import com.nearkart.productservice.exception.ProductNotFoundException;
import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.model.Product;
import com.nearkart.productservice.repository.CategoryRepository;
import com.nearkart.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId()).orElse(null)
                : null;

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .shopId(request.getShopId())
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id)));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByShop(Long shopId) {
        return productRepository.findByShopIdAndAvailableTrue(shopId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId()).orElse(product.getCategory())
                : product.getCategory();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse toggleAvailability(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setAvailable(!product.isAvailable());
        product.setUpdatedAt(LocalDateTime.now());
        return toResponse(productRepository.save(product));
    }

    @Override
    public Category createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl())
                .available(p.isAvailable())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .shopId(p.getShopId())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
