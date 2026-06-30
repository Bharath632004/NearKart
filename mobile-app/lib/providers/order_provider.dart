import 'package:flutter/material.dart';
import '../models/order_model.dart';

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
    // TODO: Fetch from backend API
    await Future.delayed(const Duration(seconds: 1));
    _orders = []; // Replace with parsed API response
    _isLoading = false;
    notifyListeners();
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
    // TODO: POST to backend API
    await Future.delayed(const Duration(seconds: 1));
    _isLoading = false;
    notifyListeners();
    return 'ORDER_${DateTime.now().millisecondsSinceEpoch}';
  }

  void setActiveOrder(Order order) {
    _activeOrder = order;
    notifyListeners();
  }
}
