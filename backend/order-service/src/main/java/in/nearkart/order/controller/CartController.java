package in.nearkart.order.controller;

import in.nearkart.order.dto.request.CartRequest;
import in.nearkart.order.dto.response.ApiResponse;
import in.nearkart.order.dto.response.CartResponse;
import in.nearkart.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> updateCart(
            @Valid @RequestBody CartRequest request) {
        CartResponse cart = cartService.addOrUpdateCartItem(request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestParam UUID customerId,
            @RequestParam UUID shopId) {
        return ResponseEntity.ok(ApiResponse.success("Cart fetched", cartService.getCart(customerId, shopId)));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader("X-User-Id") UUID customerId,
            @PathVariable UUID productId) {
        CartResponse cart = cartService.removeFromCart(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed", cart));
    }
}
