package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.*;
import com.nearkart.merchant.entity.*;
import com.nearkart.merchant.exception.*;
import com.nearkart.merchant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final ShopRepository shopRepository;
    private final PromotionRepository promotionRepository;
    private final SettlementRepository settlementRepository;
    private final S3Service s3Service;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Value("${merchant.kyc.required-docs:2}")
    private int kycRequiredDocs;

    // ─── Merchant Registration ──────────────────────────────────────────────────

    @Transactional
    public MerchantResponse registerMerchant(UUID userId, MerchantRegistrationRequest request) {
        if (merchantRepository.existsByUserId(userId)) {
            throw new MerchantAlreadyExistsException("Merchant already registered for this user");
        }
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new MerchantAlreadyExistsException("Email already registered");
        }
        Merchant merchant = Merchant.builder()
                .userId(userId)
                .businessName(request.getBusinessName())
                .businessType(request.getBusinessType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .status(MerchantStatus.PENDING_KYC)
                .build();
        log.info("Registering new merchant for userId: {}", userId);
        return toMerchantResponse(merchantRepository.save(merchant));
    }

    public MerchantResponse getMerchantByUserId(UUID userId) {
        return toMerchantResponse(findMerchantByUserId(userId));
    }

    public MerchantResponse getMerchantById(UUID merchantId) {
        return toMerchantResponse(merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId)));
    }

    @Transactional
    public MerchantResponse updateMerchantProfile(UUID userId, MerchantUpdateRequest request) {
        Merchant merchant = findMerchantByUserId(userId);
        if (request.getBusinessName() != null) merchant.setBusinessName(request.getBusinessName());
        if (request.getBusinessType() != null) merchant.setBusinessType(request.getBusinessType());
        if (request.getPhone() != null)        merchant.setPhone(request.getPhone());
        if (request.getGstin() != null)        merchant.setGstin(request.getGstin());
        if (request.getPanNumber() != null)    merchant.setPanNumber(request.getPanNumber());
        log.info("Updated profile for merchant userId: {}", userId);
        return toMerchantResponse(merchantRepository.save(merchant));
    }

    @Transactional
    public MerchantResponse updateMerchantStatus(UUID merchantId, MerchantStatus status) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));
        merchant.setStatus(status);
        return toMerchantResponse(merchantRepository.save(merchant));
    }

    // ─── KYC ────────────────────────────────────────────────────────────────────

    @Transactional
    public KycDocumentResponse uploadKycDocument(UUID userId, MultipartFile file, String documentType) {
        Merchant merchant = findMerchantByUserId(userId);
        s3Service.validateUpload(file);
        String s3Key = s3Service.uploadKycDocument(file, merchant.getId(), documentType);
        String documentUrl = s3Service.getPresignedUrl(s3Key);

        KycDocument doc = KycDocument.builder()
                .merchant(merchant)
                .documentType(documentType)
                .documentUrl(documentUrl)
                .s3Key(s3Key)
                .verified(false)
                .build();
        KycDocument saved = kycDocumentRepository.save(doc);

        if (merchant.getStatus() == MerchantStatus.PENDING_KYC) {
            merchant.setStatus(MerchantStatus.KYC_SUBMITTED);
            merchantRepository.save(merchant);
        }
        return toKycDocumentResponse(saved);
    }

    public List<KycDocumentResponse> getKycDocuments(UUID userId) {
        Merchant merchant = findMerchantByUserId(userId);
        return kycDocumentRepository.findByMerchantId(merchant.getId())
                .stream().map(this::toKycDocumentResponse).collect(Collectors.toList());
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
        long verifiedDocs = kycDocumentRepository.countByMerchantIdAndVerified(merchant.getId(), true);
        if (verifiedDocs >= kycRequiredDocs) {
            merchant.setStatus(MerchantStatus.ACTIVE);
            merchantRepository.save(merchant);
        }
        return toKycDocumentResponse(saved);
    }

    // ─── Shop Management ────────────────────────────────────────────────────────

    @Transactional
    public ShopResponse createShop(UUID userId, ShopRequest request) {
        Merchant merchant = findMerchantByUserId(userId);
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new BusinessException("Merchant KYC must be approved before creating a shop");
        }
        if (shopRepository.existsByMerchantIdAndShopName(merchant.getId(), request.getShopName())) {
            throw new BusinessException("Shop with this name already exists");
        }
        Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude()));

        Shop shop = Shop.builder()
                .merchant(merchant)
                .shopName(request.getShopName())
                .description(request.getDescription())
                .category(request.getCategory())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .location(location)
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .openDays(request.getOpenDays())
                .active(true)
                .build();
        log.info("Creating shop '{}' for merchant {}", request.getShopName(), merchant.getId());
        return toShopResponse(shopRepository.save(shop));
    }

    public List<ShopResponse> getMerchantShops(UUID userId) {
        Merchant merchant = findMerchantByUserId(userId);
        return shopRepository.findByMerchantId(merchant.getId())
                .stream().map(this::toShopResponse).collect(Collectors.toList());
    }

    public ShopResponse getShopById(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
        return toShopResponse(shop);
    }

    @Transactional
    public ShopResponse updateShop(UUID userId, UUID shopId, ShopRequest request) {
        Merchant merchant = findMerchantByUserId(userId);
        Shop shop = shopRepository.findByIdAndMerchantId(shopId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        shop.setShopName(request.getShopName());
        shop.setDescription(request.getDescription());
        shop.setCategory(request.getCategory());
        shop.setAddressLine(request.getAddressLine());
        shop.setCity(request.getCity());
        shop.setState(request.getState());
        shop.setPincode(request.getPincode());
        shop.setOpenTime(request.getOpenTime());
        shop.setCloseTime(request.getCloseTime());
        shop.setOpenDays(request.getOpenDays());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point location = geometryFactory.createPoint(
                    new Coordinate(request.getLongitude(), request.getLatitude()));
            shop.setLocation(location);
        }
        return toShopResponse(shopRepository.save(shop));
    }

    @Transactional
    public ShopResponse uploadShopImage(UUID userId, UUID shopId, MultipartFile file, String imageType) {
        Merchant merchant = findMerchantByUserId(userId);
        Shop shop = shopRepository.findByIdAndMerchantId(shopId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        s3Service.validateUpload(file);
        String s3Key = s3Service.uploadShopImage(file, shopId, imageType);
        String imageUrl = s3Service.getPresignedUrl(s3Key);

        if ("logo".equalsIgnoreCase(imageType)) {
            shop.setLogoUrl(imageUrl);
        } else {
            shop.setCoverImageUrl(imageUrl);
        }
        return toShopResponse(shopRepository.save(shop));
    }

    @Transactional
    public void toggleShopStatus(UUID userId, UUID shopId) {
        Merchant merchant = findMerchantByUserId(userId);
        Shop shop = shopRepository.findByIdAndMerchantId(shopId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        shop.setActive(!shop.isActive());
        shopRepository.save(shop);
        log.info("Shop {} toggled to active={}", shopId, shop.isActive());
    }

    public List<ShopResponse> getNearbyShops(double latitude, double longitude, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        return shopRepository.findNearbyShops(latitude, longitude, radiusMeters)
                .stream().map(this::toShopResponse).collect(Collectors.toList());
    }

    public List<ShopResponse> getNearbyShopsByCategory(double latitude, double longitude,
                                                        double radiusKm, String category) {
        double radiusMeters = radiusKm * 1000;
        return shopRepository.findNearbyShopsByCategory(latitude, longitude, radiusMeters, category)
                .stream().map(this::toShopResponse).collect(Collectors.toList());
    }

    // ─── Promotions ──────────────────────────────────────────────────────────────

    @Transactional
    public PromotionResponse createPromotion(UUID userId, UUID shopId, PromotionRequest request) {
        Merchant merchant = findMerchantByUserId(userId);
        Shop shop = shopRepository.findByIdAndMerchantId(shopId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        if (request.getPromoCode() != null && promotionRepository.existsByPromoCode(request.getPromoCode())) {
            throw new BusinessException("Promo code already exists: " + request.getPromoCode());
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be after start date");
        }

        Promotion promotion = Promotion.builder()
                .shop(shop)
                .title(request.getTitle())
                .description(request.getDescription())
                .promoType(request.getPromoType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscountCap(request.getMaxDiscountCap())
                .promoCode(request.getPromoCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .active(true)
                .build();
        return toPromotionResponse(promotionRepository.save(promotion));
    }

    public List<PromotionResponse> getShopPromotions(UUID shopId) {
        return promotionRepository.findByShopId(shopId)
                .stream().map(this::toPromotionResponse).collect(Collectors.toList());
    }

    public List<PromotionResponse> getActiveShopPromotions(UUID shopId) {
        return promotionRepository.findActivePromotionsByShop(shopId, LocalDateTime.now())
                .stream().map(this::toPromotionResponse).collect(Collectors.toList());
    }

    @Transactional
    public PromotionResponse deactivatePromotion(UUID userId, UUID promotionId) {
        Promotion promo = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        Merchant merchant = findMerchantByUserId(userId);
        if (!promo.getShop().getMerchant().getId().equals(merchant.getId())) {
            throw new BusinessException("Unauthorized to modify this promotion");
        }
        promo.setActive(false);
        return toPromotionResponse(promotionRepository.save(promo));
    }

    // ─── Settlements ─────────────────────────────────────────────────────────────

    public List<SettlementResponse> getMerchantSettlements(UUID userId) {
        Merchant merchant = findMerchantByUserId(userId);
        return settlementRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.getId())
                .stream().map(this::toSettlementResponse).collect(Collectors.toList());
    }

    public SettlementResponse getSettlementById(UUID userId, UUID settlementId) {
        Merchant merchant = findMerchantByUserId(userId);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));
        if (!settlement.getMerchant().getId().equals(merchant.getId())) {
            throw new BusinessException("Unauthorized to view this settlement");
        }
        return toSettlementResponse(settlement);
    }

    // ─── Analytics ───────────────────────────────────────────────────────────────

    public AnalyticsSummaryResponse getMerchantAnalytics(UUID userId) {
        Merchant merchant = findMerchantByUserId(userId);
        UUID merchantId = merchant.getId();

        List<Shop> shops = shopRepository.findByMerchantId(merchantId);
        long activePromos = shops.stream()
                .flatMap(s -> promotionRepository.findActivePromotionsByShop(s.getId(), LocalDateTime.now()).stream())
                .count();

        BigDecimal settled = settlementRepository.getTotalSettledAmountByMerchant(merchantId);
        BigDecimal pending = settlementRepository.getPendingSettlementAmountByMerchant(merchantId);
        long totalOrders = settlementRepository.getTotalOrdersByMerchant(merchantId);

        return AnalyticsSummaryResponse.builder()
                .totalShops(shops.size())
                .totalRevenue(settled)
                .pendingSettlement(pending)
                .completedSettlement(settled)
                .activePromotions(activePromos)
                .totalOrders(totalOrders)
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private Merchant findMerchantByUserId(UUID userId) {
        return merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found for user: " + userId));
    }

    private MerchantResponse toMerchantResponse(Merchant m) {
        return MerchantResponse.builder()
                .id(m.getId()).userId(m.getUserId())
                .businessName(m.getBusinessName()).businessType(m.getBusinessType())
                .email(m.getEmail()).phone(m.getPhone())
                .gstin(m.getGstin()).panNumber(m.getPanNumber())
                .status(m.getStatus()).createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt())
                .build();
    }

    private ShopResponse toShopResponse(Shop s) {
        Double lat = s.getLocation() != null ? s.getLocation().getY() : null;
        Double lon = s.getLocation() != null ? s.getLocation().getX() : null;
        return ShopResponse.builder()
                .id(s.getId()).merchantId(s.getMerchant().getId())
                .shopName(s.getShopName()).description(s.getDescription())
                .category(s.getCategory()).addressLine(s.getAddressLine())
                .city(s.getCity()).state(s.getState()).pincode(s.getPincode())
                .latitude(lat).longitude(lon)
                .openTime(s.getOpenTime()).closeTime(s.getCloseTime()).openDays(s.getOpenDays())
                .active(s.isActive()).logoUrl(s.getLogoUrl()).coverImageUrl(s.getCoverImageUrl())
                .rating(s.getRating()).totalReviews(s.getTotalReviews())
                .createdAt(s.getCreatedAt()).build();
    }

    private KycDocumentResponse toKycDocumentResponse(KycDocument doc) {
        return KycDocumentResponse.builder()
                .id(doc.getId()).merchantId(doc.getMerchant().getId())
                .documentType(doc.getDocumentType()).documentUrl(doc.getDocumentUrl())
                .verified(doc.isVerified()).rejectionReason(doc.getRejectionReason())
                .uploadedAt(doc.getUploadedAt()).verifiedAt(doc.getVerifiedAt())
                .build();
    }

    private PromotionResponse toPromotionResponse(Promotion p) {
        return PromotionResponse.builder()
                .id(p.getId()).shopId(p.getShop().getId())
                .title(p.getTitle()).description(p.getDescription())
                .promoType(p.getPromoType()).discountValue(p.getDiscountValue())
                .minOrderValue(p.getMinOrderValue()).maxDiscountCap(p.getMaxDiscountCap())
                .promoCode(p.getPromoCode()).startDate(p.getStartDate()).endDate(p.getEndDate())
                .active(p.isActive()).usageLimit(p.getUsageLimit()).usageCount(p.getUsageCount())
                .createdAt(p.getCreatedAt()).build();
    }

    private SettlementResponse toSettlementResponse(Settlement s) {
        return SettlementResponse.builder()
                .id(s.getId()).merchantId(s.getMerchant().getId())
                .periodStart(s.getPeriodStart()).periodEnd(s.getPeriodEnd())
                .totalOrders(s.getTotalOrders()).grossAmount(s.getGrossAmount())
                .platformFee(s.getPlatformFee()).taxDeducted(s.getTaxDeducted())
                .netAmount(s.getNetAmount()).status(s.getStatus())
                .utrNumber(s.getUtrNumber()).settledAt(s.getSettledAt())
                .createdAt(s.getCreatedAt()).build();
    }
}
