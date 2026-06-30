package com.nearkart.merchant.dto;

import com.nearkart.merchant.entity.MerchantStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MerchantResponse {
    private UUID id;
    private UUID userId;
    private String businessName;
    private String businessType;
    private String email;
    private String phone;
    private String gstin;
    private String panNumber;
    private MerchantStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
