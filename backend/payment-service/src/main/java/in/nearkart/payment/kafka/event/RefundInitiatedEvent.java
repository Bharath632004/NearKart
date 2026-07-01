package in.nearkart.payment.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RefundInitiatedEvent {
    private UUID refundId;
    private UUID paymentId;        // added – used by RefundServiceImpl
    private UUID orderId;
    private UUID customerId;
    private BigDecimal refundAmount;
    private BigDecimal amount;     // alias used by RefundServiceImpl
    private String refundMethod;
    private String reason;
    private LocalDateTime initiatedAt;
}
