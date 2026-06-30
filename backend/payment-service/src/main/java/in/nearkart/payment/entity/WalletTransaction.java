package in.nearkart.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTxType type;   // CREDIT / DEBIT

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 300)
    private String description;

    @Column
    private UUID referenceId;   // orderId / refundId

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
