package com.nearkart.shopservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shops", uniqueConstraints = {
    @UniqueConstraint(name = "uq_shop_merchant_name_city", columnNames = {"merchant_id", "name", "city"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private String address;

    private String city;
    private String pincode;

    private Double latitude;
    private Double longitude;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean verified = false;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    private ShopCategory category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
