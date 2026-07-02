package com.nearkart.shopservice.service;

import com.nearkart.shopservice.dto.ShopRequest;
import com.nearkart.shopservice.exception.ShopNotFoundException;
import com.nearkart.shopservice.model.Shop;
import com.nearkart.shopservice.model.ShopCategory;
import com.nearkart.shopservice.repository.ShopRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest {

    @Mock private ShopRepository shopRepository;
    @InjectMocks private ShopServiceImpl shopService;

    private Shop buildShop(Long id, boolean active, boolean verified) {
        Shop s = Shop.builder()
                .name("Test Shop").merchantId(1L).address("123 Main")
                .city("Chennai").active(active).verified(verified)
                .category(ShopCategory.GROCERY).build();
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    @Test
    void createShop_noDuplicate_succeeds() {
        ShopRequest req = new ShopRequest();
        req.setName("New Shop"); req.setMerchantId(1L); req.setAddress("Addr");
        req.setCity("Chennai");
        when(shopRepository.findByMerchantIdAndNameIgnoreCaseAndCity(1L, "New Shop", "Chennai"))
                .thenReturn(Optional.empty());
        Shop saved = buildShop(1L, true, false);
        when(shopRepository.save(any())).thenReturn(saved);
        assertNotNull(shopService.createShop(req));
    }

    @Test
    void createShop_duplicate_throwsException() {
        ShopRequest req = new ShopRequest();
        req.setName("Dup Shop"); req.setMerchantId(1L); req.setCity("Chennai"); req.setAddress("Addr");
        when(shopRepository.findByMerchantIdAndNameIgnoreCaseAndCity(1L, "Dup Shop", "Chennai"))
                .thenReturn(Optional.of(buildShop(1L, true, false)));
        assertThrows(IllegalArgumentException.class, () -> shopService.createShop(req));
    }

    @Test
    void getShopById_notFound_throwsException() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShopNotFoundException.class, () -> shopService.getShopById(99L));
    }

    @Test
    void toggleShopActive_togglesCorrectly() {
        Shop s = buildShop(1L, true, false);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(s));
        when(shopRepository.save(any())).thenReturn(s);
        shopService.toggleShopActive(1L);
        assertFalse(s.isActive());
    }

    @Test
    void verifyShop_setsVerifiedTrue() {
        Shop s = buildShop(1L, true, false);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(s));
        when(shopRepository.save(any())).thenReturn(s);
        shopService.verifyShop(1L);
        assertTrue(s.isVerified());
    }

    @Test
    void deleteShop_softDeletes() {
        Shop s = buildShop(1L, true, false);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(s));
        when(shopRepository.save(any())).thenReturn(s);
        shopService.deleteShop(1L);
        assertFalse(s.isActive());
        verify(shopRepository, never()).deleteById(any());
    }
}
