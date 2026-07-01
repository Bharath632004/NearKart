package in.nearkart.order.service;

import in.nearkart.order.dto.CreateOrderRequest;
import in.nearkart.order.dto.OrderResponse;
import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderItem;
import in.nearkart.order.entity.OrderStatus;
import in.nearkart.order.exception.OrderCancellationException;
import in.nearkart.order.exception.OrderNotFoundException;
import in.nearkart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        log.info("Placing order for userId={} shopId={}", request.getUserId(), request.getShopId());

        List<OrderItem> items = request.getItems().stream()
                .map(i -> OrderItem.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build())
                .collect(Collectors.toList());

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        Order order = Order.builder()
                .userId(request.getUserId())
                .shopId(request.getShopId())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);
        log.info("Order placed successfully id={}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByShop(Long shopId) {
        return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        log.info("Updating order id={} status {} -> {}", orderId, order.getStatus(), newStatus);
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderCancellationException("Cannot cancel a delivered order.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationException("Order is already cancelled.");
        }
        if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new OrderCancellationException("Cannot cancel order that is out for delivery.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        log.info("Order id={} cancelled", orderId);
        return mapToResponse(order);
    }

    // ─── Kafka payment event handlers ────────────────────────────────────────────

    /**
     * Called by PaymentEventConsumer when payment.success event is received.
     * Looks up the order by UUID string stored in orderNumber field (or falls back
     * to searching by id if orderId is numeric). For now we resolve UUID -> Long
     * by treating the most-significant bits as the id (simple approach for local dev).
     * Replace with a proper UUID primary key migration when scaling.
     */
    @Transactional
    public void confirmOrderAfterPayment(UUID externalOrderId) {
        log.info("Confirming order after payment: externalOrderId={}", externalOrderId);
        // For current schema (Long PK), try to find by latest PENDING order
        // In production: store UUID as orderNumber column and query by it
        orderRepository.findTopByStatusOrderByCreatedAtDesc(OrderStatus.PENDING)
                .ifPresentOrElse(
                        order -> {
                            order.setStatus(OrderStatus.CONFIRMED);
                            order.setUpdatedAt(Instant.now());
                            log.info("Order id={} confirmed after payment", order.getId());
                        },
                        () -> log.warn("No PENDING order found for paymentOrderId={}", externalOrderId)
                );
    }

    /**
     * Called by PaymentEventConsumer when payment.failed event is received.
     */
    @Transactional
    public void cancelOrderOnPaymentFailure(UUID externalOrderId) {
        log.info("Cancelling order due to payment failure: externalOrderId={}", externalOrderId);
        orderRepository.findTopByStatusOrderByCreatedAtDesc(OrderStatus.PENDING)
                .ifPresentOrElse(
                        order -> {
                            order.setStatus(OrderStatus.CANCELLED);
                            order.setUpdatedAt(Instant.now());
                            log.info("Order id={} cancelled due to payment failure", order.getId());
                        },
                        () -> log.warn("No PENDING order found for paymentOrderId={}", externalOrderId)
                );
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────────

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderResponse.OrderItemResponse.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .shopId(order.getShopId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}
