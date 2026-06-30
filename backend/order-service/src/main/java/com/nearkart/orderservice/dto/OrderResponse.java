package com.nearkart.orderservice.dto;

import com.nearkart.orderservice.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long customerId;
    private Long shopId;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String deliveryPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
