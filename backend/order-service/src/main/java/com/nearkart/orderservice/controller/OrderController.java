package com.nearkart.orderservice.controller;

import com.nearkart.orderservice.dto.*;
import com.nearkart.orderservice.model.OrderStatus;
import com.nearkart.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Place, track, cancel, return, and refund orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','SELLER','ADMIN')")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Get all orders for a customer")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/customer/{customerId}/paged")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Get paginated orders for a customer")
    public ResponseEntity<Page<OrderResponse>> getByCustomerPaged(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getOrdersByCustomerPaged(customerId, pageable));
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Get orders for a customer filtered by status")
    public ResponseEntity<List<OrderResponse>> getByCustomerAndStatus(
            @PathVariable Long customerId,
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerAndStatus(customerId, status));
    }

    @GetMapping("/customer/{customerId}/count")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Get order count for a customer")
    public ResponseEntity<Long> countByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.countOrdersByCustomer(customerId));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get all orders for a shop")
    public ResponseEntity<List<OrderResponse>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(orderService.getOrdersByShop(shopId));
    }

    @GetMapping("/shop/{shopId}/paged")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get paginated orders for a shop")
    public ResponseEntity<Page<OrderResponse>> getByShopPaged(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getOrdersByShopPaged(shopId, pageable));
    }

    @GetMapping("/shop/{shopId}/status/{status}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get orders for a shop filtered by status")
    public ResponseEntity<List<OrderResponse>> getByShopAndStatus(
            @PathVariable Long shopId,
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByShopAndStatus(shopId, status));
    }

    @GetMapping("/shop/{shopId}/count")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get order count for a shop")
    public ResponseEntity<Long> countByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(orderService.countOrdersByShop(shopId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Update order status — state machine enforced")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Cancel order with optional reason")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(orderService.cancelOrder(id, reason));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Return delivered order within the return window")
    public ResponseEntity<OrderResponse> returnOrder(
            @PathVariable Long id,
            @Valid @RequestBody ReturnOrderRequest request) {
        return ResponseEntity.ok(orderService.returnOrder(id, request.getReason()));
    }

    @PostMapping("/{id}/refund/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initiate refund for a returned order")
    public ResponseEntity<OrderResponse> initiateRefund(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.initiateRefund(id));
    }

    @PostMapping("/{id}/refund/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark refund as completed")
    public ResponseEntity<OrderResponse> completeRefund(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeRefund(id));
    }
}
