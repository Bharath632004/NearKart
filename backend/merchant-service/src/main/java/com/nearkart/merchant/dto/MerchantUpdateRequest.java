package com.nearkart.merchant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for merchant profile updates (PATCH /api/v1/merchants/me).
 * All fields are optional — only non-null fields will be applied.
 */
@Data
public class MerchantUpdateRequest {

    @Size(min = 2, max = 255, message = "Business name must be between 2 and 255 characters")
    private String businessName;

    @Size(min = 2, max = 100, message = "Business type must be between 2 and 100 characters")
    private String businessType;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid Indian mobile number required")
    private String phone;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
             message = "Invalid GSTIN format")
    private String gstin;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;
}
