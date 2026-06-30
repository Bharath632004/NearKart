package com.nearkart.merchant;

import com.nearkart.merchant.dto.*;
import com.nearkart.merchant.entity.*;
import com.nearkart.merchant.exception.*;
import com.nearkart.merchant.repository.*;
import com.nearkart.merchant.service.MerchantService;
import com.nearkart.merchant.service.S3Service;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MerchantService Unit Tests")
class MerchantServiceApplicationTests {

    @Mock private MerchantRepository merchantRepository;
    @Mock private KycDocumentRepository kycDocumentRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private PromotionRepository promotionRepository;
    @Mock private SettlementRepository settlementRepository;
    @Mock private S3Service s3Service;

    @InjectMocks
    private MerchantService merchantService;

    private UUID userId;
    private UUID merchantId;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
        merchant = Merchant.builder()
                .id(merchantId)
                .userId(userId)
                .businessName("Test Kirana")
                .businessType("GROCERY")
                .email("test@nearkart.com")
                .phone("9876543210")
                .gstin("22AAAAA0000A1Z5")
                .panNumber("AAAAA0000A")
                .status(MerchantStatus.PENDING_KYC)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── Registration Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should register merchant successfully")
    void registerMerchant_success() {
        when(merchantRepository.existsByUserId(userId)).thenReturn(false);
        when(merchantRepository.existsByEmail(anyString())).thenReturn(false);
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

        MerchantRegistrationRequest request = new MerchantRegistrationRequest();
        request.setBusinessName("Test Kirana");
        request.setBusinessType("GROCERY");
        request.setEmail("test@nearkart.com");
        request.setPhone("9876543210");

        MerchantResponse response = merchantService.registerMerchant(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getBusinessName()).isEqualTo("Test Kirana");
        assertThat(response.getStatus()).isEqualTo(MerchantStatus.PENDING_KYC);
        verify(merchantRepository).save(any(Merchant.class));
    }

    @Test
    @DisplayName("Should throw exception when merchant already registered for user")
    void registerMerchant_duplicateUser_throwsException() {
        when(merchantRepository.existsByUserId(userId)).thenReturn(true);

        MerchantRegistrationRequest request = new MerchantRegistrationRequest();
        request.setEmail("test@nearkart.com");

        assertThatThrownBy(() -> merchantService.registerMerchant(userId, request))
                .isInstanceOf(MerchantAlreadyExistsException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerMerchant_duplicateEmail_throwsException() {
        when(merchantRepository.existsByUserId(userId)).thenReturn(false);
        when(merchantRepository.existsByEmail("test@nearkart.com")).thenReturn(true);

        MerchantRegistrationRequest request = new MerchantRegistrationRequest();
        request.setEmail("test@nearkart.com");

        assertThatThrownBy(() -> merchantService.registerMerchant(userId, request))
                .isInstanceOf(MerchantAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");
    }

    // ─── Get Profile Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return merchant profile by userId")
    void getMerchantByUserId_success() {
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));

        MerchantResponse response = merchantService.getMerchantByUserId(userId);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@nearkart.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when merchant not found")
    void getMerchantByUserId_notFound_throwsException() {
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> merchantService.getMerchantByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Merchant not found for user");
    }

    // ─── Shop Tests ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw BusinessException when creating shop for non-ACTIVE merchant")
    void createShop_merchantNotActive_throwsException() {
        merchant.setStatus(MerchantStatus.KYC_SUBMITTED);
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));

        ShopRequest request = new ShopRequest();
        request.setShopName("My Shop");

        assertThatThrownBy(() -> merchantService.createShop(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("KYC must be approved");
    }

    @Test
    @DisplayName("Should create shop successfully for ACTIVE merchant")
    void createShop_success() {
        merchant.setStatus(MerchantStatus.ACTIVE);
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));
        when(shopRepository.existsByMerchantIdAndShopName(any(), anyString())).thenReturn(false);

        Shop shop = Shop.builder()
                .id(UUID.randomUUID())
                .merchant(merchant)
                .shopName("Bharath Kirana")
                .category("GROCERY")
                .addressLine("Main Road")
                .city("Rajamahendravaram")
                .state("Andhra Pradesh")
                .pincode("533101")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(21, 0))
                .active(true)
                .rating(BigDecimal.ZERO)
                .totalReviews(0)
                .createdAt(LocalDateTime.now())
                .build();
        when(shopRepository.save(any(Shop.class))).thenReturn(shop);

        ShopRequest request = new ShopRequest();
        request.setShopName("Bharath Kirana");
        request.setCategory("GROCERY");
        request.setAddressLine("Main Road");
        request.setCity("Rajamahendravaram");
        request.setState("Andhra Pradesh");
        request.setPincode("533101");
        request.setLatitude(17.0005);
        request.setLongitude(81.7799);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(21, 0));

        ShopResponse response = merchantService.createShop(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getShopName()).isEqualTo("Bharath Kirana");
        assertThat(response.isActive()).isTrue();
    }

    // ─── Promotion Tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw BusinessException when end date is before start date")
    void createPromotion_invalidDates_throwsException() {
        merchant.setStatus(MerchantStatus.ACTIVE);
        UUID shopId = UUID.randomUUID();
        Shop shop = Shop.builder().id(shopId).merchant(merchant).build();

        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));
        when(shopRepository.findByIdAndMerchantId(shopId, merchantId)).thenReturn(Optional.of(shop));
        when(promotionRepository.existsByPromoCode(anyString())).thenReturn(false);

        PromotionRequest request = new PromotionRequest();
        request.setTitle("Diwali Sale");
        request.setPromoType(Promotion.PromoType.PERCENTAGE);
        request.setDiscountValue(BigDecimal.valueOf(10));
        request.setPromoCode("DIWALI10");
        request.setStartDate(LocalDateTime.now().plusDays(5));
        request.setEndDate(LocalDateTime.now().plusDays(1)); // end before start

        assertThatThrownBy(() -> merchantService.createPromotion(userId, shopId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    @DisplayName("Should throw BusinessException when promo code already exists")
    void createPromotion_duplicatePromoCode_throwsException() {
        merchant.setStatus(MerchantStatus.ACTIVE);
        UUID shopId = UUID.randomUUID();
        Shop shop = Shop.builder().id(shopId).merchant(merchant).build();

        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));
        when(shopRepository.findByIdAndMerchantId(shopId, merchantId)).thenReturn(Optional.of(shop));
        when(promotionRepository.existsByPromoCode("SAVE20")).thenReturn(true);

        PromotionRequest request = new PromotionRequest();
        request.setTitle("Flash Sale");
        request.setPromoType(Promotion.PromoType.FLAT_AMOUNT);
        request.setDiscountValue(BigDecimal.valueOf(20));
        request.setPromoCode("SAVE20");
        request.setStartDate(LocalDateTime.now());
        request.setEndDate(LocalDateTime.now().plusDays(7));

        assertThatThrownBy(() -> merchantService.createPromotion(userId, shopId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Promo code already exists");
    }

    // ─── Settlement Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return merchant settlements in descending order")
    void getMerchantSettlements_success() {
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));
        when(settlementRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId))
                .thenReturn(Collections.emptyList());

        List<SettlementResponse> result = merchantService.getMerchantSettlements(userId);

        assertThat(result).isNotNull().isEmpty();
        verify(settlementRepository).findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    @Test
    @DisplayName("Should throw exception when accessing another merchant's settlement")
    void getSettlementById_unauthorized_throwsException() {
        UUID otherMerchantId = UUID.randomUUID();
        Merchant otherMerchant = Merchant.builder().id(otherMerchantId).build();
        UUID settlementId = UUID.randomUUID();

        Settlement settlement = Settlement.builder()
                .id(settlementId)
                .merchant(otherMerchant) // belongs to different merchant
                .status(Settlement.SettlementStatus.PENDING)
                .grossAmount(BigDecimal.valueOf(5000))
                .netAmount(BigDecimal.valueOf(4500))
                .platformFee(BigDecimal.valueOf(250))
                .taxDeducted(BigDecimal.valueOf(45))
                .totalOrders(10)
                .periodStart(LocalDateTime.now().minusDays(7))
                .periodEnd(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

        assertThatThrownBy(() -> merchantService.getSettlementById(userId, settlementId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unauthorized to view this settlement");
    }

    // ─── Analytics Tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return analytics summary with correct data")
    void getMerchantAnalytics_success() {
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(merchant));
        when(shopRepository.findByMerchantId(merchantId)).thenReturn(Collections.emptyList());
        when(settlementRepository.getTotalSettledAmountByMerchant(merchantId))
                .thenReturn(BigDecimal.valueOf(10000));
        when(settlementRepository.getPendingSettlementAmountByMerchant(merchantId))
                .thenReturn(BigDecimal.valueOf(2000));

        AnalyticsSummaryResponse response = merchantService.getMerchantAnalytics(userId);

        assertThat(response).isNotNull();
        assertThat(response.getTotalShops()).isZero();
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(response.getPendingSettlement()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }
}
