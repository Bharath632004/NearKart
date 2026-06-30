package com.nearkart.shopservice.service;

import com.nearkart.shopservice.dto.ShopRequest;
import com.nearkart.shopservice.dto.ShopResponse;
import com.nearkart.shopservice.model.ShopCategory;

import java.util.List;

public interface ShopService {
    ShopResponse createShop(ShopRequest request);
    ShopResponse getShopById(Long id);
    List<ShopResponse> getAllShops();
    List<ShopResponse> getShopsByMerchant(Long merchantId);
    List<ShopResponse> getNearbyShops(double lat, double lng, double radiusKm);
    List<ShopResponse> searchShops(String keyword);
    ShopResponse updateShop(Long id, ShopRequest request);
    ShopResponse toggleShopActive(Long id);
    ShopResponse verifyShop(Long id);
    void deleteShop(Long id);
    List<ShopResponse> getShopsByCity(String city);
    List<ShopResponse> getShopsByCategory(ShopCategory category);
    List<ShopResponse> getActiveShops();
}
