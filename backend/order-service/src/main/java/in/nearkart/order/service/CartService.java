package in.nearkart.order.service;

import in.nearkart.order.dto.request.CartRequest;
import in.nearkart.order.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    CartResponse addOrUpdateCartItem(CartRequest request);
    CartResponse getCart(UUID customerId, UUID shopId);
    CartResponse removeFromCart(UUID customerId, UUID productId);
    void clearCart(UUID customerId, UUID shopId);
}
