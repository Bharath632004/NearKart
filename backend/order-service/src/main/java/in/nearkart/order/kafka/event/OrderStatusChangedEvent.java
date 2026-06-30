package in.nearkart.order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private UUID shopId;
    private String previousStatus;
    private String newStatus;
    private String reason;
    private LocalDateTime changedAt;
}
