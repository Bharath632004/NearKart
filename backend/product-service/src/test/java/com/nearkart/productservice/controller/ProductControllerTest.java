package com.nearkart.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearkart.productservice.dto.ProductRequest;
import com.nearkart.productservice.dto.ProductResponse;
import com.nearkart.productservice.dto.StockUpdateRequest;
import com.nearkart.productservice.exception.GlobalExceptionHandler;
import com.nearkart.productservice.exception.ProductNotFoundException;
import com.nearkart.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse sampleResponse;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = ProductResponse.builder()
                .id(1L)
                .name("Apple")
                .description("Fresh apples")
                .price(new BigDecimal("50.00"))
                .stockQuantity(100)
                .available(true)
                .shopId(10L)
                .categoryId(1L)
                .categoryName("Groceries")
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
    @DisplayName("GET /api/products - returns 200 with product list")
    void getAllProducts_shouldReturn200() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apple"))
                .andExpect(jsonPath("$[0].price").value(50.00));
    }

    @Test
    @DisplayName("GET /api/products/{id} - returns 200 when product exists")
    void getProductById_shouldReturn200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - returns 404 when not found")
    void getProductById_shouldReturn404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ProductNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products - SELLER can create product")
    @WithMockUser(roles = "SELLER")
    void createProduct_asSeller_shouldReturn201() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    @DisplayName("POST /api/products - unauthenticated returns 401")
    void createProduct_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - ADMIN can delete product")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_asAdmin_shouldReturn204() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/stock - updates stock quantity")
    @WithMockUser(roles = "SELLER")
    void updateStock_shouldReturn200() throws Exception {
        StockUpdateRequest stockRequest = new StockUpdateRequest();
        stockRequest.setQuantity(50);

        when(productService.updateStock(1L, 50)).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/products/1/stock")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/products/search - returns matching products")
    void searchProducts_shouldReturn200() throws Exception {
        when(productService.searchProducts("apple")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/products/search").param("keyword", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apple"));
    }

    @Test
    @DisplayName("GET /api/products/filter/price - returns products in range")
    void filterByPrice_shouldReturn200() throws Exception {
        when(productService.getProductsByPriceRange(any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/products/filter/price")
                        .param("min", "10")
                        .param("max", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apple"));
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/toggle - SELLER toggles availability")
    @WithMockUser(roles = "SELLER")
    void toggleAvailability_shouldReturn200() throws Exception {
        when(productService.toggleAvailability(1L)).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/products/1/toggle").with(csrf()))
                .andExpect(status().isOk());
    }
}
