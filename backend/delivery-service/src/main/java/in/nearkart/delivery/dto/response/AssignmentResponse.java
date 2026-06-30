package in.nearkart.delivery.dto.response;

import in.nearkart.delivery.entity.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AssignmentResponse {
    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private UUID partnerId;
    private String partnerName;
    private String partnerPhone;
    private UUID shopId;
    private String shopAddress;
    private String deliveryAddress;
    private AssignmentStatus status;
    private BigDecimal deliveryFeeEarned;
    private Boolean otpVerified;
    private Boolean pickupOtpVerified;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}
