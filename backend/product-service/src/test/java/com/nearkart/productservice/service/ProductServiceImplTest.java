package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.*;
import com.nearkart.productservice.exception.CategoryNotFoundException;
import com.nearkart.productservice.exception.ProductNotFoundException;
import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.model.Product;
import com.nearkart.productservice.repository.CategoryRepository;
import com.nearkart.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductServiceImpl productService;

    private Product buildProduct(Long id, int stock) {
        Product p = Product.builder()
                .name("Test Product").description("desc")
                .price(BigDecimal.TEN).stockQuantity(stock)
                .shopId(1L).available(stock > 0).build();
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private Category buildCategory(Long id) {
        Category c = Category.builder().name("Fruits").description("Fresh fruits").build();
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    @Test
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void createProduct_duplicateNameInShop_throwsException() {
        when(productRepository.existsByNameIgnoreCaseAndShopId("Test Product", 1L)).thenReturn(true);
        ProductRequest req = new ProductRequest();
        req.setName("Test Product");
        req.setShopId(1L);
        req.setPrice(BigDecimal.TEN);
        req.setStockQuantity(10);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(req));
    }

    @Test
    void updateStock_absolute_setsExactQuantity() {
        Product p = buildProduct(1L, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);
        productService.updateStock(1L, 5, false);
        assertEquals(5, p.getStockQuantity());
        assertTrue(p.isAvailable());
    }

    @Test
    void updateStock_delta_addsToExisting() {
        Product p = buildProduct(1L, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);
        productService.updateStock(1L, 5, true);
        assertEquals(15, p.getStockQuantity());
    }

    @Test
    void updateStock_deltaNegativeBelowZero_throwsException() {
        Product p = buildProduct(1L, 3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(IllegalArgumentException.class, () -> productService.updateStock(1L, -10, true));
    }

    @Test
    void deleteCategory_withProducts_throwsException() {
        Category cat = buildCategory(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(productRepository.countByCategoryId(1L)).thenReturn(3L);
        assertThrows(IllegalStateException.class, () -> productService.deleteCategory(1L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_empty_succeeds() {
        Category cat = buildCategory(2L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(productRepository.countByCategoryId(2L)).thenReturn(0L);
        productService.deleteCategory(2L);
        verify(categoryRepository).deleteById(2L);
    }

    @Test
    void searchProducts_blankKeyword_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> productService.searchProducts("  "));
    }

    @Test
    void updateStock_setToZero_setsUnavailable() {
        Product p = buildProduct(3L, 10);
        when(productRepository.findById(3L)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);
        productService.updateStock(3L, 0, false);
        assertEquals(0, p.getStockQuantity());
        assertFalse(p.isAvailable());
    }
}
