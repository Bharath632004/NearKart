package com.nearkart.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {
    private Long productId;
    private Long shopId;
    private boolean isAvailable;
    private Integer availableQuantity;
    private Integer requestedQuantity;
    private String message;
}
