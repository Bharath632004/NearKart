package in.nearkart.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String fullName;

    private String email;

    @Column(nullable = false, unique = true)
    private String aadhaarNumber;

    @Column(nullable = false, unique = true)
    private String panNumber;

    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PartnerStatus status = PartnerStatus.PENDING_KYC;

    private String profileImageUrl;
    private String vehicleImageUrl;
    private String aadhaarImageUrl;
    private String licenseImageUrl;

    /** Current geo coordinates (updated via WebSocket / REST) */
    private Double currentLatitude;
    private Double currentLongitude;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalDeliveries = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double averageRating = 5.0;

    private String fcmToken;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isKycVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
