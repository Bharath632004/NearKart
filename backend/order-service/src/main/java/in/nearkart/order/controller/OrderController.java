package in.nearkart.order.controller;

import in.nearkart.order.dto.request.PlaceOrderRequest;
import in.nearkart.order.dto.request.UpdateOrderStatusRequest;
import in.nearkart.order.dto.response.ApiResponse;
import in.nearkart.order.dto.response.OrderResponse;
import in.nearkart.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Customer: place an order
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestHeader("X-User-Id") UUID customerId,
            @Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched", orderService.getOrderById(orderId)));
    }

    // Get order by order number
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getByOrderNumber(
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(
                ApiResponse.success("Order fetched", orderService.getOrderByNumber(orderNumber)));
    }

    // Customer: get my orders
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") UUID customerId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Orders fetched", orderService.getCustomerOrders(customerId, pageable)));
    }

    // Merchant: get shop orders
    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getShopOrders(
            @PathVariable UUID shopId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Shop orders fetched", orderService.getMerchantOrders(shopId, pageable)));
    }

    // Merchant/Delivery: update order status
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('MERCHANT', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") UUID actorId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Status updated",
                        orderService.updateOrderStatus(orderId, request, actorId)));
    }

    // Customer/Admin: cancel order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") UUID actorId,
            @RequestParam(defaultValue = "Cancelled by user") String reason) {
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled",
                        orderService.cancelOrder(orderId, reason, actorId)));
    }
}
