package in.nearkart.order.service;

import in.nearkart.order.dto.request.OrderItemRequest;
import in.nearkart.order.dto.request.PlaceOrderRequest;
import in.nearkart.order.dto.request.UpdateOrderStatusRequest;
import in.nearkart.order.dto.response.OrderResponse;
import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderStatus;
import in.nearkart.order.entity.PaymentMethod;
import in.nearkart.order.exception.InvalidStatusTransitionException;
import in.nearkart.order.exception.OrderNotCancellableException;
import in.nearkart.order.kafka.producer.OrderEventProducer;
import in.nearkart.order.repository.OrderRepository;
import in.nearkart.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderEventProducer eventProducer;
    @Mock private CartService cartService;

    @InjectMocks private OrderServiceImpl orderService;

    private Order mockOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        mockOrder = Order.builder()
                .id(orderId)
                .orderNumber("NK-20260630-00001")
                .customerId(UUID.randomUUID())
                .shopId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("200.00"))
                .deliveryFee(new BigDecimal("25.00"))
                .taxAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("235.00"))
                .paymentMethod(PaymentMethod.UPI)
                .build();
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        OrderResponse response = orderService.getOrderById(orderId);
        assertNotNull(response);
        assertEquals("NK-20260630-00001", response.getOrderNumber());
    }

    @Test
    void cancelOrder_Success_WhenPending() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any())).thenReturn(mockOrder);
        doNothing().when(eventProducer).publishOrderCancelled(any());

        OrderResponse response = orderService.cancelOrder(orderId, "Changed my mind", UUID.randomUUID());
        assertNotNull(response);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void cancelOrder_Throws_WhenDelivered() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        assertThrows(OrderNotCancellableException.class,
                () -> orderService.cancelOrder(orderId, "reason", UUID.randomUUID()));
    }

    @Test
    void updateStatus_Throws_OnInvalidTransition() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.PENDING);

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, req, UUID.randomUUID()));
    }
}
