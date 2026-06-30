package in.nearkart.order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private UUID shopId;
    private BigDecimal refundAmount;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
}
