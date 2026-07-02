package com.nearkart.orderservice.dto;

import com.nearkart.orderservice.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private Long id;
    private Long customerId;
    private Long shopId;
    private String paymentId;
    private OrderStatus status;
    private OrderStatus refundStatus;
    private String cancelReason;
    private String returnReason;
    private List<OrderItemResponse> items;
    private int itemCount;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String deliveryPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
