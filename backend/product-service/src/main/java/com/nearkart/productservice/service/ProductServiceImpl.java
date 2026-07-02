package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.*;
import com.nearkart.productservice.exception.CategoryNotFoundException;
import com.nearkart.productservice.exception.ProductNotFoundException;
import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.model.Product;
import com.nearkart.productservice.repository.CategoryRepository;
import com.nearkart.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ------------------------------------------------------------------ create / update

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByNameIgnoreCaseAndShopId(request.getName(), request.getShopId())) {
            throw new IllegalArgumentException(
                "Product '" + request.getName() + "' already exists in shop " + request.getShopId());
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .shopId(request.getShopId())
                .available(request.getStockQuantity() > 0)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, name={}, shop={}", saved.getId(), saved.getName(), saved.getShopId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);

        Category category = product.getCategory();
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        product.setAvailable(request.getStockQuantity() > 0);

        return toResponse(productRepository.save(product));
    }

    // ------------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(findProductOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByAvailableTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByShop(Long shopId) {
        return productRepository.findByShopIdAndAvailableTrue(shopId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByShopPaged(Long shopId, Pageable pageable) {
        return productRepository.findByShopIdAndAvailableTrueOrderByCreatedAtDesc(shopId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategoryPaged(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndAvailableTrue(categoryId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Search keyword cannot be blank");
        }
        String trimmed = keyword.trim();
        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(trimmed, trimmed)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("min price cannot be negative");
        if (min.compareTo(max) > 0) throw new IllegalArgumentException("min price cannot exceed max price");
        return productRepository.findByPriceRange(min, max).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByShopAndPriceRange(Long shopId, BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) throw new IllegalArgumentException("min price cannot exceed max price");
        return productRepository.findByShopAndPriceRange(shopId, min, max).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(Long shopId, int threshold) {
        if (threshold < 0) throw new IllegalArgumentException("threshold cannot be negative");
        return productRepository.findLowStockByShop(shopId, threshold).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsByShop(Long shopId) {
        return productRepository.countByShopId(shopId);
    }

    // ------------------------------------------------------------------ stock / availability

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, int quantity, boolean delta) {
        Product product = findProductOrThrow(id);
        int newQty = delta ? product.getStockQuantity() + quantity : quantity;
        if (newQty < 0) throw new IllegalArgumentException("Stock cannot go below 0");
        product.setStockQuantity(newQty);
        product.setAvailable(newQty > 0);
        log.info("Stock updated: product={}, qty={}", id, newQty);
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse toggleAvailability(Long id) {
        Product product = findProductOrThrow(id);
        product.setAvailable(!product.isAvailable());
        return toResponse(productRepository.save(product));
    }

    // ------------------------------------------------------------------ delete

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: id={}", id);
    }

    // ------------------------------------------------------------------ categories

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category already exists: " + request.getName());
        }
        Category category = categoryRepository.save(
            Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build()
        );
        return toCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return toCategoryResponse(findCategoryOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        findCategoryOrThrow(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException(
                "Cannot delete category \u2014 " + productCount + " product(s) are assigned to it. Reassign them first.");
        }
        categoryRepository.deleteById(id);
        log.info("Category deleted: id={}", id);
    }

    // ------------------------------------------------------------------ helpers

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .inStock(p.getStockQuantity() > 0)
                .imageUrl(p.getImageUrl())
                .available(p.isAvailable())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .shopId(p.getShopId())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private CategoryResponse toCategoryResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .productCount(productRepository.countByCategoryId(c.getId()))
                .build();
    }
}
