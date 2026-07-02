package com.nearkart.productservice.controller;

import com.nearkart.productservice.dto.*;
import com.nearkart.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management APIs")
public class ProductController {

    private final ProductService productService;

    // ─── Product CRUD ─────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available products")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Get available products by shop")
    public ResponseEntity<List<ProductResponse>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getProductsByShop(shopId));
    }

    @GetMapping("/shop/{shopId}/paged")
    @Operation(summary = "Get paginated products by shop")
    public ResponseEntity<Page<ProductResponse>> getByShopPaged(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByShopPaged(shopId, pageable));
    }

    @GetMapping("/shop/{shopId}/filter/price")
    @Operation(summary = "Filter products by shop and price range")
    public ResponseEntity<List<ProductResponse>> getByShopAndPrice(
            @PathVariable Long shopId,
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productService.getProductsByShopAndPriceRange(shopId, min, max));
    }

    @GetMapping("/shop/{shopId}/low-stock")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get low-stock products for a shop")
    public ResponseEntity<List<ProductResponse>> getLowStock(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(shopId, threshold));
    }

    @GetMapping("/shop/{shopId}/count")
    @Operation(summary = "Get product count for a shop")
    public ResponseEntity<Long> getProductCountByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.countProductsByShop(shopId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/category/{categoryId}/paged")
    @Operation(summary = "Get paginated products by category")
    public ResponseEntity<Page<ProductResponse>> getByCategoryPaged(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProductsByCategoryPaged(categoryId, PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @GetMapping("/filter/price")
    @Operation(summary = "Filter products by price range")
    public ResponseEntity<List<ProductResponse>> filterByPrice(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productService.getProductsByPriceRange(min, max));
    }

    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get out-of-stock products (Admin only)")
    public ResponseEntity<List<ProductResponse>> getOutOfStock() {
        return ResponseEntity.ok(productService.getOutOfStockProducts());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Update a product")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                          @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Update stock \u2014 absolute or delta mode")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable Long id,
                                                        @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(productService.updateStock(id, request.getQuantity(), request.isDelta()));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Toggle product availability")
    public ResponseEntity<ProductResponse> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleAvailability(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Categories ───────────────────────────────────────────────────────────

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createCategory(request));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getCategoryById(id));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(productService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category (only if no products assigned)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
