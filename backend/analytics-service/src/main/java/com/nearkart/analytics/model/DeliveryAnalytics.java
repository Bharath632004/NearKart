package com.nearkart.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Long totalDeliveries;
    private Long onTimeDeliveries;
    private Long lateDeliveries;
    private Double averageDeliveryTimeMinutes;
    private Double slaBreachRate; // % of deliveries that breached SLA
    private Long failedDeliveries;

    private LocalDateTime updatedAt;
}
