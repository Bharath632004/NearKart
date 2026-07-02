package com.nearkart.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantityAvailable;

    @Builder.Default
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;
}
