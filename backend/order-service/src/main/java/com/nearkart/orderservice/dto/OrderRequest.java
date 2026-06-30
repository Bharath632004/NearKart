package com.nearkart.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotBlank(message = "Delivery phone is required")
    private String deliveryPhone;
}
