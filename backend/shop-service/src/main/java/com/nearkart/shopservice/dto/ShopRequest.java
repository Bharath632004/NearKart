package com.nearkart.shopservice.dto;

import com.nearkart.shopservice.model.ShopCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ShopRequest {

    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be 2-100 characters")
    private String name;

    @Size(max = 500, message = "Description too long")
    private String description;

    private String imageUrl;

    @NotNull(message = "Merchant ID is required")
    private Long merchantId;

    @NotBlank(message = "Address is required")
    @Size(max = 300, message = "Address too long")
    private String address;

    @Size(max = 100, message = "City name too long")
    private String city;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email address")
    private String email;

    private ShopCategory category;
}
