package com.nearkart.orderservice.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED,
    REFUND_INITIATED,
    REFUND_COMPLETED
}
