package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.*;
import com.nearkart.merchant.entity.*;
import com.nearkart.merchant.entity.Settlement.SettlementStatus;
import com.nearkart.merchant.exception.ResourceNotFoundException;
import com.nearkart.merchant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MerchantRepository merchantRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final SettlementRepository settlementRepository;
    private final ShopRepository shopRepository;
    private final PromotionRepository promotionRepository;

    public List<MerchantResponse> getAllMerchants(MerchantStatus status) {
        List<Merchant> merchants = (status != null)
                ? merchantRepository.findAll().stream()
                    .filter(m -> m.getStatus() == status).collect(Collectors.toList())
                : merchantRepository.findAll();
        return merchants.stream().map(this::toMerchantResponse).collect(Collectors.toList());
    }

    public MerchantResponse getMerchantById(UUID merchantId) {
        return toMerchantResponse(merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId)));
    }

    @Transactional
    public MerchantResponse updateMerchantStatus(UUID merchantId, MerchantStatus status) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));
        merchant.setStatus(status);
        log.info("Admin updated merchant {} status to {}", merchantId, status);
        return toMerchantResponse(merchantRepository.save(merchant));
    }

    public List<KycDocumentResponse> getPendingKycDocuments() {
        return kycDocumentRepository.findAll().stream()
                .filter(doc -> !doc.isVerified() && doc.getRejectionReason() == null)
                .map(this::toKycDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public KycDocumentResponse verifyKycDocument(UUID documentId, boolean approved, String rejectionReason) {
        KycDocument doc = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found: " + documentId));
        doc.setVerified(approved);
        doc.setRejectionReason(approved ? null : rejectionReason);
        doc.setVerifiedAt(LocalDateTime.now());
        KycDocument saved = kycDocumentRepository.save(doc);

        Merchant merchant = doc.getMerchant();
        long verifiedCount = kycDocumentRepository.countByMerchantIdAndVerified(merchant.getId(), true);
        if (approved && verifiedCount >= 2) {
            merchant.setStatus(MerchantStatus.ACTIVE);
            merchantRepository.save(merchant);
            log.info("Merchant {} activated after KYC verification", merchant.getId());
        } else if (!approved) {
            merchant.setStatus(MerchantStatus.KYC_REJECTED);
            merchantRepository.save(merchant);
        }
        return toKycDocumentResponse(saved);
    }

    public List<SettlementResponse> getAllSettlements(String statusFilter) {
        List<Settlement> settlements;
        if (statusFilter != null && !statusFilter.isBlank()) {
            SettlementStatus status = SettlementStatus.valueOf(statusFilter.toUpperCase());
            settlements = settlementRepository.findAll().stream()
                    .filter(s -> s.getStatus() == status).collect(Collectors.toList());
        } else {
            settlements = settlementRepository.findAll();
        }
        return settlements.stream().map(this::toSettlementResponse).collect(Collectors.toList());
    }

    @Transactional
    public SettlementResponse triggerSettlement(SettlementRequest request) {
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + request.getMerchantId()));

        BigDecimal platformFee = request.getGrossAmount()
                .multiply(BigDecimal.valueOf(0.05)); // 5% platform fee
        BigDecimal taxDeducted = platformFee
                .multiply(BigDecimal.valueOf(0.18)); // 18% GST on fee
        BigDecimal netAmount = request.getGrossAmount().subtract(platformFee).subtract(taxDeducted);

        Settlement settlement = Settlement.builder()
                .merchant(merchant)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .totalOrders(request.getTotalOrders())
                .grossAmount(request.getGrossAmount())
                .platformFee(platformFee)
                .taxDeducted(taxDeducted)
                .netAmount(netAmount)
                .status(SettlementStatus.PENDING)
                .build();

        log.info("Settlement triggered for merchant {} | net: {}", merchant.getId(), netAmount);
        return toSettlementResponse(settlementRepository.save(settlement));
    }

    @Transactional
    public SettlementResponse completeSettlement(UUID settlementId, String utrNumber) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setUtrNumber(utrNumber);
        settlement.setSettledAt(LocalDateTime.now());
        log.info("Settlement {} completed with UTR: {}", settlementId, utrNumber);
        return toSettlementResponse(settlementRepository.save(settlement));
    }

    public AdminAnalyticsResponse getPlatformAnalytics() {
        long totalMerchants = merchantRepository.count();
        long activeMerchants = merchantRepository.countByStatus(MerchantStatus.ACTIVE);
        long pendingKyc = merchantRepository.countByStatus(MerchantStatus.KYC_SUBMITTED);
        long totalShops = shopRepository.count();
        long activeShops = shopRepository.countByActiveTrue();
        long totalPromotions = promotionRepository.count();
        long activePromotions = promotionRepository.countByActiveTrue();
        long pendingSettlements = settlementRepository.countByStatus(SettlementStatus.PENDING);
        BigDecimal totalSettled = settlementRepository.getTotalCompletedSettlements();

        return AdminAnalyticsResponse.builder()
                .totalMerchants(totalMerchants)
                .activeMerchants(activeMerchants)
                .pendingKycMerchants(pendingKyc)
                .totalShops(totalShops)
                .activeShops(activeShops)
                .totalPromotions(totalPromotions)
                .activePromotions(activePromotions)
                .pendingSettlements(pendingSettlements)
                .totalSettledAmount(totalSettled)
                .build();
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────────

    private MerchantResponse toMerchantResponse(Merchant m) {
        return MerchantResponse.builder()
                .id(m.getId()).userId(m.getUserId())
                .businessName(m.getBusinessName()).businessType(m.getBusinessType())
                .email(m.getEmail()).phone(m.getPhone())
                .gstin(m.getGstin()).panNumber(m.getPanNumber())
                .status(m.getStatus()).createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt())
                .build();
    }

    private KycDocumentResponse toKycDocumentResponse(KycDocument doc) {
        return KycDocumentResponse.builder()
                .id(doc.getId()).merchantId(doc.getMerchant().getId())
                .documentType(doc.getDocumentType()).documentUrl(doc.getDocumentUrl())
                .verified(doc.isVerified()).rejectionReason(doc.getRejectionReason())
                .uploadedAt(doc.getUploadedAt()).verifiedAt(doc.getVerifiedAt())
                .build();
    }

    private SettlementResponse toSettlementResponse(Settlement s) {
        return SettlementResponse.builder()
                .id(s.getId()).merchantId(s.getMerchant().getId())
                .periodStart(s.getPeriodStart()).periodEnd(s.getPeriodEnd())
                .totalOrders(s.getTotalOrders()).grossAmount(s.getGrossAmount())
                .platformFee(s.getPlatformFee()).taxDeducted(s.getTaxDeducted())
                .netAmount(s.getNetAmount()).status(s.getStatus())
                .utrNumber(s.getUtrNumber()).settledAt(s.getSettledAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
