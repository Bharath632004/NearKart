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
@Table(name = "merchant_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private LocalDate date;

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalCommissionPaid;
    private Double averageRating;
    private Long totalReviews;
    private Long cancelledOrders;
    private Double fulfillmentRate; // % orders fulfilled on time

    private LocalDateTime updatedAt;
}
