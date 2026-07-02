package com.nearkart.shopservice.service;

import com.nearkart.shopservice.dto.ShopRequest;
import com.nearkart.shopservice.dto.ShopResponse;
import com.nearkart.shopservice.exception.ShopNotFoundException;
import com.nearkart.shopservice.model.Shop;
import com.nearkart.shopservice.model.ShopCategory;
import com.nearkart.shopservice.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public ShopResponse createShop(ShopRequest request) {
        shopRepository.findByMerchantIdAndNameIgnoreCaseAndCity(
                request.getMerchantId(), request.getName(), request.getCity())
            .ifPresent(existing -> {
                throw new IllegalArgumentException(
                    "Shop '" + request.getName() + "' already exists in " + request.getCity() + " for this merchant");
            });

        Shop shop = Shop.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .merchantId(request.getMerchantId())
                .address(request.getAddress())
                .city(request.getCity())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .email(request.getEmail())
                .category(request.getCategory())
                .active(true)
                .verified(false)
                .build();

        Shop saved = shopRepository.save(shop);
        log.info("Shop created: id={}, name={}, merchant={}", saved.getId(), saved.getName(), saved.getMerchantId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getAllShops() {
        return shopRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getActiveShopsPaged(Pageable pageable) {
        return shopRepository.findByActiveTrueOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getShopsByMerchant(Long merchantId) {
        return shopRepository.findByMerchantId(merchantId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getShopsByMerchantPaged(Long merchantId, Pageable pageable) {
        return shopRepository.findByMerchantId(merchantId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getNearbyShops(double lat, double lng, double radiusKm) {
        return shopRepository.findNearbyShops(lat, lng, radiusKm).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getNearbyShopsByCategory(double lat, double lng, double radiusKm, ShopCategory category) {
        return shopRepository.findNearbyShopsByCategory(lat, lng, radiusKm, category.name())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> searchShops(String keyword) {
        return shopRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShopResponse updateShop(Long id, ShopRequest request) {
        Shop shop = findOrThrow(id);
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setImageUrl(request.getImageUrl());
        shop.setAddress(request.getAddress());
        shop.setCity(request.getCity());
        shop.setPincode(request.getPincode());
        shop.setLatitude(request.getLatitude());
        shop.setLongitude(request.getLongitude());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setCategory(request.getCategory());
        log.info("Shop updated: id={}", id);
        return toResponse(shopRepository.save(shop));
    }

    @Override
    @Transactional
    public ShopResponse toggleShopActive(Long id) {
        Shop shop = findOrThrow(id);
        shop.setActive(!shop.isActive());
        log.info("Shop {} toggled to active={}", id, shop.isActive());
        return toResponse(shopRepository.save(shop));
    }

    @Override
    @Transactional
    public ShopResponse verifyShop(Long id) {
        Shop shop = findOrThrow(id);
        shop.setVerified(true);
        log.info("Shop {} verified", id);
        return toResponse(shopRepository.save(shop));
    }

    @Override
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = findOrThrow(id);
        shop.setActive(false);
        shopRepository.save(shop);
        log.info("Shop {} soft-deleted (deactivated)", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getShopsByCity(String city) {
        return shopRepository.findByCityAndActiveTrue(city)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getShopsByCategory(ShopCategory category) {
        return shopRepository.findByCategoryAndActiveTrue(category)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getActiveShops() {
        return shopRepository.findByActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countShopsByMerchant(Long merchantId) {
        return shopRepository.countByMerchantId(merchantId);
    }

    private Shop findOrThrow(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found: " + id));
    }

    private ShopResponse toResponse(Shop s) {
        return ShopResponse.builder()
                .id(s.getId()).name(s.getName()).description(s.getDescription())
                .imageUrl(s.getImageUrl()).merchantId(s.getMerchantId())
                .address(s.getAddress()).city(s.getCity()).pincode(s.getPincode())
                .latitude(s.getLatitude()).longitude(s.getLongitude())
                .active(s.isActive()).verified(s.isVerified())
                .phone(s.getPhone()).email(s.getEmail()).category(s.getCategory())
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
                .build();
    }
}
