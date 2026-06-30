package com.nearkart.merchant.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KycDocumentResponse {
    private UUID id;
    private UUID merchantId;
    private String documentType;
    private String documentUrl;
    private boolean verified;
    private String rejectionReason;
    private LocalDateTime uploadedAt;
    private LocalDateTime verifiedAt;
}
