package in.nearkart.payment.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RefundInitiatedEvent {
    private UUID refundId;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal refundAmount;
    private String refundMethod;
    private LocalDateTime initiatedAt;
}
