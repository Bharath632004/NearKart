package com.nearkart.merchant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalTime;

@Data
public class ShopRequest {

    @NotBlank(message = "Shop name is required")
    private String shopName;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Address is required")
    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian pincode")
    @NotBlank(message = "Pincode is required")
    private String pincode;

    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Open time is required")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    private LocalTime closeTime;

    private String openDays = "MON,TUE,WED,THU,FRI,SAT";
}
