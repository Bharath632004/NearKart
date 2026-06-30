package in.nearkart.order.service.impl;

import in.nearkart.order.dto.request.CartRequest;
import in.nearkart.order.dto.response.CartResponse;
import in.nearkart.order.entity.CartItem;
import in.nearkart.order.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CART_KEY_PREFIX  = "cart:";
    private static final long   CART_TTL_HOURS   = 24;
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("25.00");
    private static final BigDecimal FREE_DELIVERY = new BigDecimal("300.00");

    private String cartKey(UUID customerId, UUID shopId) {
        return CART_KEY_PREFIX + customerId + ":" + shopId;
    }

    @Override
    public CartResponse addOrUpdateCartItem(CartRequest request) {
        String key = cartKey(request.getCustomerId(), request.getShopId());

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>)
                redisTemplate.opsForValue().get(key);

        if (cart == null) cart = new HashMap<>();

        String productKey = request.getProductId().toString();

        if (request.getQuantity() == 0) {
            cart.remove(productKey);
        } else {
            // TODO: Call product-service (Feign) to get current price and name
            CartItem item = CartItem.builder()
                    .productId(request.getProductId())
                    .shopId(request.getShopId())
                    .quantity(request.getQuantity())
                    .unitPrice(BigDecimal.TEN)           // Replace with Feign
                    .totalPrice(BigDecimal.TEN.multiply(BigDecimal.valueOf(request.getQuantity())))
                    .productName("Product Name")          // Replace with Feign
                    .build();
            cart.put(productKey, item);
        }

        redisTemplate.opsForValue().set(key, cart, CART_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Cart updated: customerId={}, shopId={}", request.getCustomerId(), request.getShopId());

        return buildCartResponse(request.getCustomerId(), request.getShopId(), cart);
    }

    @Override
    public CartResponse getCart(UUID customerId, UUID shopId) {
        String key = cartKey(customerId, shopId);
        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>)
                redisTemplate.opsForValue().get(key);
        return buildCartResponse(customerId, shopId, cart == null ? new HashMap<>() : cart);
    }

    @Override
    public CartResponse removeFromCart(UUID customerId, UUID productId) {
        // Simplified — in production find which shop the cart belongs to
        log.debug("Removing product {} from cart of customer {}", productId, customerId);
        return CartResponse.builder().customerId(customerId).items(List.of()).build();
    }

    @Override
    public void clearCart(UUID customerId, UUID shopId) {
        redisTemplate.delete(cartKey(customerId, shopId));
        log.info("Cart cleared: customerId={}, shopId={}", customerId, shopId);
    }

    private CartResponse buildCartResponse(UUID customerId, UUID shopId, Map<String, CartItem> cart) {
        List<CartItem> items = new ArrayList<>(cart.values());
        BigDecimal subtotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFee = subtotal.compareTo(FREE_DELIVERY) >= 0 ? BigDecimal.ZERO : DELIVERY_FEE;

        return CartResponse.builder()
                .customerId(customerId)
                .shopId(shopId)
                .items(items)
                .totalItems(items.stream().mapToInt(CartItem::getQuantity).sum())
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .totalAmount(subtotal.add(deliveryFee))
                .build();
    }
}
