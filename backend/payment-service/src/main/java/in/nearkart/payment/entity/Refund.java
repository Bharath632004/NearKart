package in.nearkart.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(unique = true)
    private String razorpayRefundId;    // rfnd_XXXXXXXXX

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private RefundMethod refundMethod;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
