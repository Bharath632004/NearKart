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
public class OrderReadyForPickupEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID shopId;
    private UUID customerId;
    private String shopAddress;
    private Double shopLatitude;
    private Double shopLongitude;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private LocalDateTime readyAt;
}
