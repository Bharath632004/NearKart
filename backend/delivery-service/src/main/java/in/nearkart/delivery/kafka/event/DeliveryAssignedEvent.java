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
public class DeliveryAssignedEvent {
    private UUID assignmentId;
    private UUID orderId;
    private String orderNumber;
    private UUID partnerId;
    private String partnerName;
    private String partnerPhone;
    private UUID customerId;
    private UUID shopId;
    private String deliveryOtp;
    private LocalDateTime assignedAt;
}
