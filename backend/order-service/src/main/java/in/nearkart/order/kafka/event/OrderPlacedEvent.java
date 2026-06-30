package in.nearkart.order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private UUID shopId;
    private UUID deliveryAddressId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private List<OrderItemEvent> items;
    private LocalDateTime createdAt;
}
