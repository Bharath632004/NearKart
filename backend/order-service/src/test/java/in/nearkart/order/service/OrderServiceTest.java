package in.nearkart.order.service;

import in.nearkart.order.dto.CreateOrderRequest;
import in.nearkart.order.dto.OrderResponse;
import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderItem;
import in.nearkart.order.entity.OrderStatus;
import in.nearkart.order.exception.OrderCancellationException;
import in.nearkart.order.exception.OrderNotFoundException;
import in.nearkart.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockOrder = Order.builder()
                .id(1L)
                .userId(101L)
                .shopId(5L)
                .totalAmount(580.0)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        OrderResponse response = orderService.getOrder(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void getOrder_Throws_WhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(99L));
    }

    @Test
    void cancelOrder_Success_WhenPending() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any())).thenReturn(mockOrder);
        OrderResponse response = orderService.cancelOrder(1L);
        assertNotNull(response);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void cancelOrder_Throws_WhenDelivered() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        assertThrows(OrderCancellationException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void cancelOrder_Throws_WhenAlreadyCancelled() {
        mockOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        assertThrows(OrderCancellationException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void placeOrder_Success() {
        CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest(
                1L, "Test Product", 2, 100.0);
        CreateOrderRequest request = new CreateOrderRequest(
                101L, 5L, List.of(itemReq));

        Order savedOrder = Order.builder()
                .id(1L)
                .userId(101L)
                .shopId(5L)
                .totalAmount(200.0)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .items(new ArrayList<>())
                .build();

        when(orderRepository.save(any())).thenReturn(savedOrder);
        OrderResponse response = orderService.placeOrder(request);
        assertNotNull(response);
        assertEquals(200.0, response.getTotalAmount());
        assertEquals("PENDING", response.getStatus());
    }
}
