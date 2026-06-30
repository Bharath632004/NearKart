package com.nearkart.shopservice.dto;

import com.nearkart.shopservice.model.ShopCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopRequest {
    @NotBlank(message = "Shop name is required")
    private String name;
    private String description;
    private String imageUrl;
    @NotNull(message = "Merchant ID is required")
    private Long merchantId;
    @NotBlank(message = "Address is required")
    private String address;
    private String city;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String email;
    private ShopCategory category;
}
