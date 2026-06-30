package in.nearkart.order.dto.response;

import in.nearkart.order.entity.CartItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {
    private UUID customerId;
    private UUID shopId;
    private List<CartItem> items;
    private int totalItems;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
}
