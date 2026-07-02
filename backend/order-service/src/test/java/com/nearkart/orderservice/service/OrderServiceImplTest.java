package com.nearkart.orderservice.service;

import com.nearkart.orderservice.dto.OrderItemRequest;
import com.nearkart.orderservice.dto.OrderRequest;
import com.nearkart.orderservice.dto.OrderResponse;
import com.nearkart.orderservice.exception.OrderCannotBeCancelledException;
import com.nearkart.orderservice.exception.OrderCannotBeReturnedException;
import com.nearkart.orderservice.exception.OrderNotFoundException;
import com.nearkart.orderservice.model.Order;
import com.nearkart.orderservice.model.OrderStatus;
import com.nearkart.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                .customerId(1L).shopId(1L)
                .deliveryAddress("123 Main St").deliveryPhone("9999999999")
                .status(status).totalAmount(BigDecimal.TEN)
                .items(new ArrayList<>()).build();
        ReflectionTestUtils.setField(o, "id", id);
        ReflectionTestUtils.setField(o, "createdAt", LocalDateTime.now().minusMinutes(5));
        ReflectionTestUtils.setField(o, "updatedAt", LocalDateTime.now().minusHours(1));
        return o;
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void cancelOrder_pending_succeeds() {
        Order o = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any())).thenReturn(o);
        orderService.cancelOrder(1L, "Changed mind");
        assertEquals(OrderStatus.CANCELLED, o.getStatus());
    }

    @Test
    void cancelOrder_delivered_throwsException() {
        Order o = buildOrder(2L, OrderStatus.DELIVERED);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(o));
        assertThrows(OrderCannotBeCancelledException.class, () -> orderService.cancelOrder(2L, "test"));
    }

    @Test
    void returnOrder_delivered_withinWindow_succeeds() {
        Order o = buildOrder(3L, OrderStatus.DELIVERED);
        ReflectionTestUtils.setField(o, "deliveredAt", LocalDateTime.now().minusHours(2));
        ReflectionTestUtils.setField(orderService, "returnWindowHours", 24);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any())).thenReturn(o);
        orderService.returnOrder(3L, "Defective");
        assertEquals(OrderStatus.RETURNED, o.getStatus());
    }

    @Test
    void returnOrder_windowExpired_throwsException() {
        Order o = buildOrder(4L, OrderStatus.DELIVERED);
        ReflectionTestUtils.setField(o, "deliveredAt", LocalDateTime.now().minusHours(48));
        ReflectionTestUtils.setField(orderService, "returnWindowHours", 24);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(o));
        assertThrows(OrderCannotBeReturnedException.class, () -> orderService.returnOrder(4L, "Too late"));
    }

    @Test
    void updateStatus_invalidTransition_throwsException() {
        Order o = buildOrder(5L, OrderStatus.DELIVERED);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(o));
        assertThrows(Exception.class, () -> orderService.updateOrderStatus(5L, OrderStatus.PENDING));
    }

    @Test
    void placeOrder_zeroTotal_throwsException() {
        OrderRequest req = new OrderRequest();
        req.setCustomerId(1L);
        req.setShopId(1L);
        req.setDeliveryAddress("123 Street");
        req.setDeliveryPhone("9999999999");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setProductName("Test");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.ZERO);
        req.setItems(List.of(item));
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(req));
    }
}
