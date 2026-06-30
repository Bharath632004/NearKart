package in.nearkart.order.service.impl;

import in.nearkart.order.dto.request.PlaceOrderRequest;
import in.nearkart.order.dto.request.UpdateOrderStatusRequest;
import in.nearkart.order.dto.response.OrderItemResponse;
import in.nearkart.order.dto.response.OrderResponse;
import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderItem;
import in.nearkart.order.entity.OrderStatus;
import in.nearkart.order.exception.*;
import in.nearkart.order.kafka.event.*;
import in.nearkart.order.kafka.producer.OrderEventProducer;
import in.nearkart.order.repository.OrderRepository;
import in.nearkart.order.service.CartService;
import in.nearkart.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;
    private final CartService cartService;

    private static final BigDecimal DELIVERY_FEE     = new BigDecimal("25.00");
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("300.00");
    private static final BigDecimal GST_RATE         = new BigDecimal("0.05");  // 5%
    private static final int        EST_DELIVERY_MINS = 30;

    // ----------------------------------------------------------------
    // PLACE ORDER
    // ----------------------------------------------------------------
    @Override
    public OrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {

        // TODO: Call product-service (Feign) to validate products and get prices
        // TODO: Call inventory-service (Feign) to check and reserve stock
        // TODO: Call coupon-service to apply discount if couponId present

        // Build order items from request (prices will come from product-service)
        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            BigDecimal unitPrice  = BigDecimal.TEN;   // Replace with Feign call
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            return OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName("Product Name")       // Replace with Feign call
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deliveryFee = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0
                ? BigDecimal.ZERO : DELIVERY_FEE;

        BigDecimal taxAmount    = subtotal.multiply(GST_RATE);
        BigDecimal totalAmount  = subtotal.add(deliveryFee).add(taxAmount);

        Order order = Order.builder()
                .customerId(customerId)
                .shopId(request.getShopId())
                .deliveryAddressId(request.getDeliveryAddressId())
                .paymentMethod(request.getPaymentMethod())
                .specialInstructions(request.getSpecialInstructions())
                .couponId(request.getCouponId())
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .estimatedDeliveryAt(LocalDateTime.now().plusMinutes(EST_DELIVERY_MINS))
                .status(OrderStatus.PENDING)
                .build();

        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order saved = orderRepository.save(order);
        log.info("Order placed: orderNumber={}, customerId={}", saved.getOrderNumber(), customerId);

        // Publish Kafka event → payment-service, notification-service
        eventProducer.publishOrderPlaced(toPlacedEvent(saved));

        // Clear customer cart after successful order
        cartService.clearCart(customerId, request.getShopId());

        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // GET ORDERS
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        return toResponse(findOrderOrThrow(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMerchantOrders(UUID shopId, Pageable pageable) {
        return orderRepository
                .findByShopIdOrderByCreatedAtDesc(shopId, pageable)
                .map(this::toResponse);
    }

    // ----------------------------------------------------------------
    // UPDATE STATUS
    // ----------------------------------------------------------------
    @Override
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, UUID actorId) {
        Order order = findOrderOrThrow(orderId);
        OrderStatus previous = order.getStatus();

        validateStatusTransition(previous, request.getStatus());

        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order updated = orderRepository.save(order);
        log.info("Order status updated: orderNumber={}, {} → {}",
                order.getOrderNumber(), previous, request.getStatus());

        // Publish Kafka event → notification-service
        eventProducer.publishOrderStatusChanged(
                OrderStatusChangedEvent.builder()
                        .orderId(updated.getId())
                        .orderNumber(updated.getOrderNumber())
                        .customerId(updated.getCustomerId())
                        .shopId(updated.getShopId())
                        .previousStatus(previous.name())
                        .newStatus(updated.getStatus().name())
                        .changedAt(LocalDateTime.now())
                        .build()
        );

        return toResponse(updated);
    }

    // ----------------------------------------------------------------
    // CANCEL ORDER
    // ----------------------------------------------------------------
    @Override
    public OrderResponse cancelOrder(UUID orderId, String reason, UUID actorId) {
        Order order = findOrderOrThrow(orderId);

        if (!isCancellable(order.getStatus())) {
            throw new OrderNotCancellableException(
                    "Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        Order cancelled = orderRepository.save(order);
        log.info("Order cancelled: orderNumber={}, reason={}", order.getOrderNumber(), reason);

        // Publish Kafka event → payment-service (trigger refund), notification-service
        eventProducer.publishOrderCancelled(
                OrderCancelledEvent.builder()
                        .orderId(cancelled.getId())
                        .orderNumber(cancelled.getOrderNumber())
                        .customerId(cancelled.getCustomerId())
                        .shopId(cancelled.getShopId())
                        .refundAmount(cancelled.getTotalAmount())
                        .cancellationReason(reason)
                        .cancelledAt(cancelled.getCancelledAt())
                        .build()
        );

        return toResponse(cancelled);
    }

    // ----------------------------------------------------------------
    // KAFKA CONSUMERS: Payment Events
    // ----------------------------------------------------------------
    @Override
    public void confirmOrderAfterPayment(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            eventProducer.publishOrderStatusChanged(
                    OrderStatusChangedEvent.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .customerId(order.getCustomerId())
                            .shopId(order.getShopId())
                            .previousStatus(OrderStatus.PENDING.name())
                            .newStatus(OrderStatus.CONFIRMED.name())
                            .changedAt(LocalDateTime.now())
                            .build()
            );
        }
    }

    @Override
    public void cancelOrderOnPaymentFailure(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason("Payment failed");
            orderRepository.save(order);
        }
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------------------------------
    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    private boolean isCancellable(OrderStatus status) {
        return status == OrderStatus.PENDING ||
               status == OrderStatus.CONFIRMED ||
               status == OrderStatus.PREPARING;
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        // Define allowed transitions
        boolean valid = switch (from) {
            case PENDING           -> to == OrderStatus.CONFIRMED  || to == OrderStatus.CANCELLED;
            case CONFIRMED         -> to == OrderStatus.PREPARING  || to == OrderStatus.CANCELLED;
            case PREPARING         -> to == OrderStatus.READY_FOR_PICKUP || to == OrderStatus.CANCELLED;
            case READY_FOR_PICKUP  -> to == OrderStatus.PICKED_UP;
            case PICKED_UP         -> to == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY  -> to == OrderStatus.DELIVERED;
            case DELIVERED         -> to == OrderStatus.REFUND_INITIATED;
            case REFUND_INITIATED  -> to == OrderStatus.REFUNDED;
            default                -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition: " + from + " → " + to);
        }
    }

    private OrderPlacedEvent toPlacedEvent(Order order) {
        List<OrderItemEvent> itemEvents = order.getItems().stream()
                .map(i -> OrderItemEvent.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        return OrderPlacedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .shopId(order.getShopId())
                .deliveryAddressId(order.getDeliveryAddressId())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod().name())
                .items(itemEvents)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .productImage(i.getProductImage())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .shopId(order.getShopId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .specialInstructions(order.getSpecialInstructions())
                .estimatedDeliveryAt(order.getEstimatedDeliveryAt())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
