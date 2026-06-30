package com.nearkart.shopservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shops")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    private String imageUrl;

    // References user-service merchantId
    private Long merchantId;

    private String address;
    private String city;
    private String pincode;

    private Double latitude;
    private Double longitude;

    private boolean active = true;
    private boolean verified = false;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    private ShopCategory category;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
