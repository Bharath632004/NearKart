package com.nearkart.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceRangeRequest {

    @NotNull(message = "Minimum price is required")
    @DecimalMin(value = "0.0", message = "Minimum price must be >= 0")
    private BigDecimal minPrice;

    @NotNull(message = "Maximum price is required")
    @DecimalMin(value = "0.0", message = "Maximum price must be >= 0")
    private BigDecimal maxPrice;
}
