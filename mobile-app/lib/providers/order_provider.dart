import 'package:flutter/material.dart';
import '../models/order_model.dart';
import '../services/api_service.dart';

class OrderProvider extends ChangeNotifier {
  List<Order> _orders = [];
  Order? _activeOrder;
  bool _isLoading = false;
  String? _error;

  List<Order> get orders => _orders;
  Order? get activeOrder => _activeOrder;
  bool get isLoading => _isLoading;
  String? get error => _error;

  void _setLoading(bool v) {
    _isLoading = v;
    notifyListeners();
  }

  Future<void> fetchOrders(String userId) async {
    _setLoading(true);
    _error = null;
    try {
      _orders = await ApiService.getMyOrders(userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<String?> placeOrder({
    required String shopId,
    required String userId,
    required List<Map<String, dynamic>> items,
    required double totalAmount,
    required String deliveryAddress,
    String paymentMethod = 'WALLET',
    String? paymentId,
  }) async {
    _setLoading(true);
    _error = null;
    try {
      final res = await ApiService.placeOrder({
        'shopId': shopId,
        'userId': userId,
        'items': items,
        'totalAmount': totalAmount,
        'deliveryAddress': deliveryAddress,
        'paymentMethod': paymentMethod,
        if (paymentId != null) 'paymentId': paymentId,
      });
      return res['id']?.toString() ?? res['orderId']?.toString();
    } catch (e) {
      _error = e.toString();
      return null;
    } finally {
      _setLoading(false);
    }
  }

  Future<Order?> fetchOrderById(String orderId) async {
    try {
      final res = await ApiService.getOrderById(orderId);
      final order = Order.fromJson(res['data'] ?? res);
      _activeOrder = order;
      notifyListeners();
      return order;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return null;
    }
  }

  Future<bool> cancelOrder(String orderId) async {
    _setLoading(true);
    try {
      await ApiService.cancelOrder(orderId);
      _orders = _orders.map((o) {
        if (o.id == orderId) {
          return Order(
            id: o.id,
            shopId: o.shopId,
            userId: o.userId,
            items: o.items,
            totalAmount: o.totalAmount,
            status: OrderStatus.cancelled,
            createdAt: o.createdAt,
            deliveryAddress: o.deliveryAddress,
          );
        }
        return o;
      }).toList();
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    } finally {
      _setLoading(false);
    }
  }

  void setActiveOrder(Order order) {
    _activeOrder = order;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
