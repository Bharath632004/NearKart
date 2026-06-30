package com.nearkart.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class AddressResponse {
    private UUID id;
    private String label;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
}
