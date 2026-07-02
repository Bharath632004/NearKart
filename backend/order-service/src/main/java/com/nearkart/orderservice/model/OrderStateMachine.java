package com.nearkart.orderservice.model;

import com.nearkart.orderservice.exception.InvalidOrderTransitionException;

import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING,           Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
        OrderStatus.CONFIRMED,         Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING,         Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED),
        OrderStatus.OUT_FOR_DELIVERY,  Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED,         Set.of(OrderStatus.RETURNED),
        OrderStatus.RETURNED,          Set.of(OrderStatus.REFUND_INITIATED),
        OrderStatus.REFUND_INITIATED,  Set.of(OrderStatus.REFUND_COMPLETED),
        OrderStatus.CANCELLED,         Set.of(),
        OrderStatus.REFUND_COMPLETED,  Set.of()
    );

    public static void validateTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new InvalidOrderTransitionException(
                "Cannot transition order from " + current + " to " + next +
                ". Allowed: " + allowed
            );
        }
    }

    public static boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public static boolean canReturn(OrderStatus status) {
        return status == OrderStatus.DELIVERED;
    }
}
