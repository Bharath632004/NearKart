package com.nearkart.productservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private boolean inStock;
    private String imageUrl;
    private boolean available;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
