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
@Table(name = "customer_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Long newCustomers;
    private Long activeCustomers;
    private Long returningCustomers;
    private BigDecimal averageSpendPerCustomer;
    private Long totalCartAbandoned;
    private Double retentionRate;

    private LocalDateTime updatedAt;
}
