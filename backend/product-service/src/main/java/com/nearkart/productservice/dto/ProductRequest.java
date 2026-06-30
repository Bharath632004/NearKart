package com.nearkart.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be >= 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    private String imageUrl;

    private Long categoryId;

    @NotNull(message = "Shop ID is required")
    private Long shopId;
}
