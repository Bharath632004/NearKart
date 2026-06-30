package com.nearkart.productservice.controller;

import com.nearkart.productservice.dto.*;
import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─── Product Endpoints ───────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ProductResponse>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getProductsByShop(shopId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleAvailability(id));
    }

    // ─── Category Endpoints ──────────────────────────────────────────────────

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createCategory(request));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getCategoryById(id));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                    @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(productService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
