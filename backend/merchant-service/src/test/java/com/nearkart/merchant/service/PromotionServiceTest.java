package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.PromotionRequest;
import com.nearkart.merchant.dto.PromotionResponse;
import com.nearkart.merchant.entity.Promotion;
import com.nearkart.merchant.entity.Shop;
import com.nearkart.merchant.repository.PromotionRepository;
import com.nearkart.merchant.repository.ShopRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    void createPromotion_shouldSucceed_whenShopExists() {
        UUID shopId = UUID.randomUUID();
        Shop shop = new Shop();
        shop.setId(shopId);

        PromotionRequest request = new PromotionRequest();
        request.setTitle("Summer Sale");
        request.setDiscountPercent(20.0);
        request.setStartDate(LocalDateTime.now());
        request.setEndDate(LocalDateTime.now().plusDays(7));

        Promotion savedPromotion = new Promotion();
        savedPromotion.setId(UUID.randomUUID());
        savedPromotion.setTitle("Summer Sale");
        savedPromotion.setShop(shop);

        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(savedPromotion);

        PromotionResponse response = merchantService.createPromotion(shopId, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Summer Sale");
    }

    @Test
    void getPromotionsByShop_shouldReturnList() {
        UUID shopId = UUID.randomUUID();
        Shop shop = new Shop();
        shop.setId(shopId);

        Promotion p1 = new Promotion();
        p1.setId(UUID.randomUUID());
        p1.setTitle("Promo 1");
        p1.setShop(shop);

        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(promotionRepository.findByShop(shop)).thenReturn(List.of(p1));

        List<PromotionResponse> result = merchantService.getPromotionsByShop(shopId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Promo 1");
    }
}
