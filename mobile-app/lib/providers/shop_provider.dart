import 'package:flutter/material.dart';
import '../models/shop_model.dart';
import '../services/api_service.dart';

class ShopProvider extends ChangeNotifier {
  List<Shop> _shops = [];
  Shop? _selectedShop;
  bool _isLoading = false;
  String? _error;

  List<Shop> get shops => _shops;
  Shop? get selectedShop => _selectedShop;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchNearbyShops({
    required double lat,
    required double lng,
    double radiusKm = 5.0,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();
    try {
      _shops = await ApiService.getNearbyShops(
          lat: lat, lng: lng, radiusKm: radiusKm);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchShopById(String shopId) async {
    _isLoading = true;
    notifyListeners();
    try {
      final res = await ApiService.getShopById(shopId);
      _selectedShop = Shop.fromJson(res['data'] ?? res);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void selectShop(Shop shop) {
    _selectedShop = shop;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
