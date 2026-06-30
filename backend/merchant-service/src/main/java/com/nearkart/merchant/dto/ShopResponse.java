package com.nearkart.merchant.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class ShopResponse {
    private UUID id;
    private UUID merchantId;
    private String shopName;
    private String description;
    private String category;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String openDays;
    private boolean active;
    private String logoUrl;
    private String coverImageUrl;
    private BigDecimal rating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
}
