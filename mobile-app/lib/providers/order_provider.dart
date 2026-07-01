import 'package:flutter/material.dart';
import '../models/order_model.dart';
import '../services/api_service.dart';

class OrderProvider extends ChangeNotifier {
  List<Order> _orders = [];
  Order? _activeOrder;
  bool _isLoading = false;

  List<Order> get orders => _orders;
  Order? get activeOrder => _activeOrder;
  bool get isLoading => _isLoading;

  Future<void> fetchOrders(String userId) async {
    _isLoading = true;
    notifyListeners();
    try {
      _orders = await ApiService.getMyOrders(userId);
    } catch (e) {
      _orders = [];
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<String?> placeOrder({
    required String shopId,
    required String userId,
    required List<Map<String, dynamic>> items,
    required double totalAmount,
    required String deliveryAddress,
  }) async {
    _isLoading = true;
    notifyListeners();
    try {
      final res = await ApiService.placeOrder({
        'shopId': shopId,
        'userId': userId,
        'items': items,
        'totalAmount': totalAmount,
        'deliveryAddress': deliveryAddress,
      });
      return res['id']?.toString() ?? res['orderId']?.toString();
    } catch (e) {
      return null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void setActiveOrder(Order order) {
    _activeOrder = order;
    notifyListeners();
  }
}
