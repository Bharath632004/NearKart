package in.nearkart.order.service;

import in.nearkart.order.dto.request.PlaceOrderRequest;
import in.nearkart.order.dto.request.UpdateOrderStatusRequest;
import in.nearkart.order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(UUID customerId, PlaceOrderRequest request);

    OrderResponse getOrderById(UUID orderId);

    OrderResponse getOrderByNumber(String orderNumber);

    Page<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable);

    Page<OrderResponse> getMerchantOrders(UUID shopId, Pageable pageable);

    OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, UUID actorId);

    OrderResponse cancelOrder(UUID orderId, String reason, UUID actorId);

    void confirmOrderAfterPayment(UUID orderId);

    void cancelOrderOnPaymentFailure(UUID orderId);
}
