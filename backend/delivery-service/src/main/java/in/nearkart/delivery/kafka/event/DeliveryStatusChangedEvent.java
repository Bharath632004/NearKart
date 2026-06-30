package in.nearkart.delivery.kafka.event;

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
public class DeliveryStatusChangedEvent {
    private UUID assignmentId;
    private UUID orderId;
    private String orderNumber;
    private UUID partnerId;
    private String partnerName;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;
}
