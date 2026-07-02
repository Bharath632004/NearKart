package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // Products
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getAvailableProducts();
    List<ProductResponse> getProductsByShop(Long shopId);
    Page<ProductResponse> getProductsByShopPaged(Long shopId, Pageable pageable);
    List<ProductResponse> getProductsByCategory(Long categoryId);
    Page<ProductResponse> getProductsByCategoryPaged(Long categoryId, Pageable pageable);
    List<ProductResponse> searchProducts(String keyword);
    List<ProductResponse> getProductsByPriceRange(BigDecimal min, BigDecimal max);
    List<ProductResponse> getProductsByShopAndPriceRange(Long shopId, BigDecimal min, BigDecimal max);
    List<ProductResponse> getOutOfStockProducts();
    List<ProductResponse> getLowStockProducts(Long shopId, int threshold);
    long countProductsByShop(Long shopId);
    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse updateStock(Long id, int quantity, boolean delta);
    void deleteProduct(Long id);
    ProductResponse toggleAvailability(Long id);

    // Categories
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
