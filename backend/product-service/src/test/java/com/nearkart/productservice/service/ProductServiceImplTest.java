package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.ProductRequest;
import com.nearkart.productservice.dto.ProductResponse;
import com.nearkart.productservice.exception.ProductNotFoundException;
import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.model.Product;
import com.nearkart.productservice.repository.CategoryRepository;
import com.nearkart.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductServiceImpl productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Groceries").build();

        sampleProduct = Product.builder()
                .id(1L)
                .name("Apple")
                .description("Fresh apples")
                .price(new BigDecimal("50.00"))
                .stockQuantity(100)
                .available(true)
                .shopId(10L)
                .category(category)
                .build();

        sampleRequest = new ProductRequest();
        sampleRequest.setName("Apple");
        sampleRequest.setDescription("Fresh apples");
        sampleRequest.setPrice(new BigDecimal("50.00"));
        sampleRequest.setStockQuantity(100);
        sampleRequest.setShopId(10L);
        sampleRequest.setCategoryId(1L);
    }

    @Test
    void createProduct_shouldReturnProductResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleProduct.getCategory()));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Apple");
        assertThat(response.getPrice()).isEqualByComparingTo("50.00");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Apple");
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllProducts_shouldReturnList() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<ProductResponse> products = productService.getAllProducts();

        assertThat(products).hasSize(1);
    }

    @Test
    void deleteProduct_shouldDelete_whenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_shouldThrow_whenNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void toggleAvailability_shouldFlipAvailable() {
        sampleProduct.setAvailable(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any())).thenReturn(sampleProduct);

        ProductResponse response = productService.toggleAvailability(1L);

        assertThat(sampleProduct.isAvailable()).isFalse();
    }

    @Test
    void searchProducts_shouldReturnMatchingProducts() {
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("apple", "apple"))
                .thenReturn(List.of(sampleProduct));

        List<ProductResponse> results = productService.searchProducts("apple");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Apple");
    }
}
