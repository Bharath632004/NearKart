package com.nearkart.inventoryservice.dto;

import com.nearkart.inventoryservice.model.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    private Long id;
    private Long productId;
    private Long shopId;
    private String productName;
    private String sku;
    private Integer quantityAvailable;
    private Integer lowStockThreshold;
    private BigDecimal price;
    private InventoryStatus status;
    private boolean isLowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
