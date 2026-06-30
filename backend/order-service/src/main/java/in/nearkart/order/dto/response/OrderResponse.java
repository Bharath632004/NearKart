package in.nearkart.order.dto.response;

import in.nearkart.order.entity.OrderStatus;
import in.nearkart.order.entity.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private UUID shopId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String specialInstructions;
    private LocalDateTime estimatedDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
