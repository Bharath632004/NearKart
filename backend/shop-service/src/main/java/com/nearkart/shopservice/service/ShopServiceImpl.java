package com.nearkart.shopservice.service;

import com.nearkart.shopservice.dto.ShopRequest;
import com.nearkart.shopservice.dto.ShopResponse;
import com.nearkart.shopservice.exception.ShopNotFoundException;
import com.nearkart.shopservice.model.Shop;
import com.nearkart.shopservice.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public ShopResponse createShop(ShopRequest request) {
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toResponse(shopRepository.save(shop));
    }

    @Override
    public ShopResponse getShopById(Long id) {
        return toResponse(shopRepository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found: " + id)));
    }

    @Override
    public List<ShopResponse> getAllShops() {
        return shopRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ShopResponse> getShopsByMerchant(Long merchantId) {
        return shopRepository.findByMerchantId(merchantId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ShopResponse> getNearbyShops(double lat, double lng, double radiusKm) {
        return shopRepository.findNearbyShops(lat, lng, radiusKm).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ShopResponse> searchShops(String keyword) {
        return shopRepository.findByNameContainingIgnoreCase(keyword).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ShopResponse updateShop(Long id, ShopRequest request) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found: " + id));
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
        shop.setUpdatedAt(LocalDateTime.now());
        return toResponse(shopRepository.save(shop));
    }

    @Override
    public ShopResponse toggleShopActive(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found: " + id));
        shop.setActive(!shop.isActive());
        shop.setUpdatedAt(LocalDateTime.now());
        return toResponse(shopRepository.save(shop));
    }

    @Override
    public ShopResponse verifyShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found: " + id));
        shop.setVerified(true);
        shop.setUpdatedAt(LocalDateTime.now());
        return toResponse(shopRepository.save(shop));
    }

    @Override
    public void deleteShop(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new ShopNotFoundException("Shop not found: " + id);
        }
        shopRepository.deleteById(id);
    }

    private ShopResponse toResponse(Shop s) {
        return ShopResponse.builder()
                .id(s.getId()).name(s.getName()).description(s.getDescription())
                .imageUrl(s.getImageUrl()).merchantId(s.getMerchantId())
                .address(s.getAddress()).city(s.getCity()).pincode(s.getPincode())
                .latitude(s.getLatitude()).longitude(s.getLongitude())
                .active(s.isActive()).verified(s.isVerified())
                .phone(s.getPhone()).email(s.getEmail()).category(s.getCategory())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
