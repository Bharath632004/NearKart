package com.nearkart.merchant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MerchantRegistrationRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business type is required")
    private String businessType;

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid Indian mobile number required")
    @NotBlank(message = "Phone is required")
    private String phone;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
             message = "Invalid GSTIN format")
    private String gstin;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;
}
