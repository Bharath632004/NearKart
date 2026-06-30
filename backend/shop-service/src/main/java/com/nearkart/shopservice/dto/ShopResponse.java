package com.nearkart.shopservice.dto;

import com.nearkart.shopservice.model.ShopCategory;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long merchantId;
    private String address;
    private String city;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private boolean active;
    private boolean verified;
    private String phone;
    private String email;
    private ShopCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
