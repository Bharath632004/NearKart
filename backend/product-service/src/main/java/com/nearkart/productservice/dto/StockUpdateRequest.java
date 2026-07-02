package com.nearkart.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StockUpdateRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    /**
     * If true, 'quantity' is added to existing stock (delta mode).
     * If false (default), 'quantity' replaces the current stock (absolute mode).
     */
    private boolean delta = false;
}
