package in.nearkart.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private DeliveryPartner partner;

    @Column(nullable = false)
    private UUID shopId;

    @Column(nullable = false)
    private UUID customerId;

    /** Pickup address (shop location) */
    private String shopAddress;
    private Double shopLatitude;
    private Double shopLongitude;

    /** Drop address (customer location) */
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    /** OTP that customer shows at door */
    private String deliveryOtp;
    private LocalDateTime otpExpiresAt;
    private Boolean otpVerified;

    /** OTP that shop shows to partner at pickup */
    private String pickupOtp;
    private Boolean pickupOtpVerified;

    private BigDecimal deliveryFeeEarned;

    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime failedAt;
    private String failureReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
