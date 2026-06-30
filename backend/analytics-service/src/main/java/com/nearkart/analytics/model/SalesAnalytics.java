package com.nearkart.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period; // DAILY, WEEKLY, MONTHLY

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalItems;
    private BigDecimal averageOrderValue;
    private BigDecimal totalCommission;

    @Column(updatedAt = true)
    private LocalDateTime updatedAt;

    public enum Period {
        DAILY, WEEKLY, MONTHLY
    }
}
