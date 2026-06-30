package in.nearkart.delivery.dto.response;

import in.nearkart.delivery.entity.PartnerStatus;
import in.nearkart.delivery.entity.VehicleType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PartnerResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String email;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private PartnerStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private BigDecimal walletBalance;
    private BigDecimal totalEarnings;
    private Integer totalDeliveries;
    private Double averageRating;
    private Boolean isKycVerified;
    private LocalDateTime createdAt;
}
