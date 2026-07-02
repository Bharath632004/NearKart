package com.nearkart.orderservice.service;

import com.nearkart.orderservice.dto.*;
import com.nearkart.orderservice.exception.OrderCannotBeCancelledException;
import com.nearkart.orderservice.exception.OrderNotFoundException;
import com.nearkart.orderservice.model.*;
import com.nearkart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${app.cancel-window-minutes:10}")
    private int cancelWindowMinutes;

    @Value("${app.return-window-hours:24}")
    private int returnWindowHours;

    // ------------------------------------------------------------------ place order

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .shopId(request.getShopId())
                .paymentId(request.getPaymentId())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryPhone(request.getDeliveryPhone())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = request.getItems().stream().map(i -> {
            BigDecimal total = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            return OrderItem.builder()
                    .productId(i.getProductId())
                    .productName(i.getProductName())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .totalPrice(total)
                    .order(order)
                    .build();
        }).collect(Collectors.toList());

        order.setItems(items);
        order.setTotalAmount(
            items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        Order saved = orderRepository.save(order);
        log.info("Order placed: id={}, customer={}, shop={}", saved.getId(), saved.getCustomerId(), saved.getShopId());
        return toResponse(saved);
    }

    // ------------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByShop(Long shopId) {
        return orderRepository.findByShopId(shopId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomerPaged(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByShopPaged(Long shopId, Pageable pageable) {
        return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerAndStatus(Long customerId, OrderStatus status) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByShopAndStatus(Long shopId, OrderStatus status) {
        return orderRepository.findByShopIdAndStatus(shopId, status).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ status update

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = findOrThrow(id);
        OrderStateMachine.validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        log.info("Order {} status updated to {}", id, newStatus);
        return toResponse(orderRepository.save(order));
    }

    // ------------------------------------------------------------------ cancel

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String reason) {
        Order order = findOrThrow(id);
        if (!OrderStateMachine.canCancel(order.getStatus())) {
            throw new OrderCannotBeCancelledException(
                "Order " + id + " cannot be cancelled — current status: " + order.getStatus()
            );
        }
        long minutesSincePlaced = Duration.between(order.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutesSincePlaced > cancelWindowMinutes && order.getStatus() == OrderStatus.CONFIRMED) {
            throw new OrderCannotBeCancelledException(
                "Cancel window of " + cancelWindowMinutes + " minutes has passed"
            );
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        log.info("Order {} cancelled. Reason: {}", id, reason);
        return toResponse(orderRepository.save(order));
    }

    // ------------------------------------------------------------------ return

    @Override
    @Transactional
    public OrderResponse returnOrder(Long id, String reason) {
        Order order = findOrThrow(id);
        if (!OrderStateMachine.canReturn(order.getStatus())) {
            throw new OrderCannotBeCancelledException(
                "Order " + id + " is not eligible for return — status: " + order.getStatus()
            );
        }
        long hoursSinceDelivery = Duration.between(order.getUpdatedAt(), LocalDateTime.now()).toHours();
        if (hoursSinceDelivery > returnWindowHours) {
            throw new OrderCannotBeCancelledException(
                "Return window of " + returnWindowHours + " hours has passed"
            );
        }
        order.setStatus(OrderStatus.RETURNED);
        order.setReturnReason(reason);
        log.info("Order {} returned. Reason: {}", id, reason);
        return toResponse(orderRepository.save(order));
    }

    // ------------------------------------------------------------------ refund

    @Override
    @Transactional
    public OrderResponse initiateRefund(Long id) {
        Order order = findOrThrow(id);
        OrderStateMachine.validateTransition(order.getStatus(), OrderStatus.REFUND_INITIATED);
        order.setStatus(OrderStatus.REFUND_INITIATED);
        order.setRefundStatus(OrderStatus.REFUND_INITIATED);
        log.info("Refund initiated for order {}", id);
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse completeRefund(Long id) {
        Order order = findOrThrow(id);
        OrderStateMachine.validateTransition(order.getStatus(), OrderStatus.REFUND_COMPLETED);
        order.setStatus(OrderStatus.REFUND_COMPLETED);
        order.setRefundStatus(OrderStatus.REFUND_COMPLETED);
        log.info("Refund completed for order {}", id);
        return toResponse(orderRepository.save(order));
    }

    // ------------------------------------------------------------------ helpers

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> itemResponses = o.getItems().stream().map(i ->
            OrderItemResponse.builder()
                .productId(i.getProductId())
                .productName(i.getProductName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .build()
        ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomerId())
                .shopId(o.getShopId())
                .paymentId(o.getPaymentId())
                .status(o.getStatus())
                .refundStatus(o.getRefundStatus())
                .cancelReason(o.getCancelReason())
                .returnReason(o.getReturnReason())
                .items(itemResponses)
                .itemCount(itemResponses.size())
                .totalAmount(o.getTotalAmount())
                .deliveryAddress(o.getDeliveryAddress())
                .deliveryPhone(o.getDeliveryPhone())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
