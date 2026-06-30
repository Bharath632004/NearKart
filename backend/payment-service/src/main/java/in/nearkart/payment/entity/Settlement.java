package in.nearkart.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID shopId;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal grossAmount;         // Sum of all order totals

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;    // Platform fee deducted

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;           // Transferred to merchant

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(unique = true)
    private String utrNumber;   // Unique Transaction Reference from bank

    @Column
    private LocalDateTime settledAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
