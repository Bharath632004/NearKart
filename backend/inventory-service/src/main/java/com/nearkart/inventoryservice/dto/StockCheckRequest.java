package com.nearkart.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockCheckRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    @NotNull(message = "Required quantity is required")
    @Min(value = 1, message = "Required quantity must be at least 1")
    private Integer requiredQuantity;
}
