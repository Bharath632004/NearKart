package com.nearkart.orderservice.service;

import com.nearkart.orderservice.exception.InvalidOrderTransitionException;
import com.nearkart.orderservice.exception.OrderCannotBeCancelledException;
import com.nearkart.orderservice.exception.OrderNotFoundException;
import com.nearkart.orderservice.model.*;
import com.nearkart.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderServiceImpl orderService;

    private Order buildOrder(Long id, OrderStatus status) {
        Order o = Order.builder()
                .customerId(1L).shopId(10L)
                .status(status)
                .totalAmount(BigDecimal.TEN)
                .deliveryAddress("123 Main St")
                .deliveryPhone("9876543210")
                .build();
        ReflectionTestUtils.setField(o, "id", id);
        ReflectionTestUtils.setField(o, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(o, "updatedAt", LocalDateTime.now());
        o.setItems(List.of());
        return o;
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void cancelOrder_pendingOrder_succeeds() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        orderService.cancelOrder(1L, "Changed mind");
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_deliveredOrder_throwsException() {
        Order order = buildOrder(2L, OrderStatus.DELIVERED);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        assertThrows(OrderCannotBeCancelledException.class, () -> orderService.cancelOrder(2L, "reason"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void returnOrder_deliveredOrder_succeeds() {
        Order order = buildOrder(3L, OrderStatus.DELIVERED);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        orderService.returnOrder(3L, "Damaged item");
        assertEquals(OrderStatus.RETURNED, order.getStatus());
    }

    @Test
    void updateStatus_invalidTransition_throwsException() {
        Order order = buildOrder(4L, OrderStatus.DELIVERED);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        assertThrows(InvalidOrderTransitionException.class,
            () -> orderService.updateOrderStatus(4L, OrderStatus.PENDING));
    }

    @Test
    void initiateRefund_returnedOrder_succeeds() {
        Order order = buildOrder(5L, OrderStatus.RETURNED);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        orderService.initiateRefund(5L);
        assertEquals(OrderStatus.REFUND_INITIATED, order.getStatus());
    }

    @Test
    void completeRefund_refundInitiatedOrder_succeeds() {
        Order order = buildOrder(6L, OrderStatus.REFUND_INITIATED);
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        orderService.completeRefund(6L);
        assertEquals(OrderStatus.REFUND_COMPLETED, order.getStatus());
    }
}
