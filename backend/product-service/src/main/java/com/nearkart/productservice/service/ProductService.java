package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.*;
import com.nearkart.productservice.model.Category;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByShop(Long shopId);

    List<ProductResponse> getProductsByCategory(Long categoryId);

    List<ProductResponse> searchProducts(String keyword);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse toggleAvailability(Long id);

    // Category
    Category createCategory(CategoryRequest request);

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    Category updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
