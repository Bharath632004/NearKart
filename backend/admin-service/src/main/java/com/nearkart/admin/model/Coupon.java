package com.nearkart.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    private Double discountPercent;

    private Double maxDiscountAmount;

    private Double minOrderValue;

    private LocalDate expiryDate;

    private boolean active;

    private int usageLimit;

    private int usedCount;
}
