package in.nearkart.payment.kafka.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentFailedEvent {
    private UUID paymentId;
    private UUID orderId;
    private UUID customerId;
    private String failureReason;
    private LocalDateTime failedAt;
}
