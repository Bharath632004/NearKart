import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../core/constants/app_constants.dart';
import '../models/order_model.dart';
import '../models/product_model.dart';
import '../models/shop_model.dart';

class ApiService {
  static const String _tokenKey = 'auth_token';
  static const String _refreshTokenKey = 'refresh_token';

  // ─── Token Helpers ───────────────────────────────────────────────
  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  static Future<void> saveTokens(String token, String refreshToken) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
    await prefs.setString(_refreshTokenKey, refreshToken);
  }

  static Future<void> clearTokens() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_refreshTokenKey);
  }

  static Future<Map<String, String>> _authHeaders() async {
    final token = await getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer \$token',
    };
  }

  // ─── Generic Request ─────────────────────────────────────────────
  static Future<Map<String, dynamic>> _request(
    String method,
    String path, {
    Map<String, dynamic>? body,
    Map<String, String>? queryParams,
  }) async {
    final uri = Uri.parse('\${AppConstants.baseUrl}\$path')
        .replace(queryParameters: queryParams);
    final headers = await _authHeaders();

    http.Response response;
    switch (method) {
      case 'POST':
        response = await http.post(uri, headers: headers, body: jsonEncode(body));
        break;
      case 'PUT':
        response = await http.put(uri, headers: headers, body: jsonEncode(body));
        break;
      case 'PATCH':
        response = await http.patch(uri, headers: headers, body: jsonEncode(body));
        break;
      case 'DELETE':
        response = await http.delete(uri, headers: headers);
        break;
      default:
        response = await http.get(uri, headers: headers);
    }

    final decoded = jsonDecode(response.body);
    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decoded is Map<String, dynamic> ? decoded : {'data': decoded};
    }
    throw ApiException(decoded['message'] ?? 'Request failed', response.statusCode);
  }

  // ══════════════════════════════════════════════════════════════════
  // AUTH  →  /api/v1/auth
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> register({
    required String name,
    required String phone,
    required String role,
  }) =>
      _request('POST', '/api/v1/auth/register',
          body: {'name': name, 'phone': phone, 'role': role});

  static Future<Map<String, dynamic>> login({
    required String identifier,
    required String password,
  }) =>
      _request('POST', '/api/v1/auth/login',
          body: {'identifier': identifier, 'password': password});

  static Future<void> sendOtp(String identifier) =>
      _request('POST', '/api/v1/auth/otp/send',
          queryParams: {'identifier': identifier});

  static Future<Map<String, dynamic>> verifyOtp({
    required String identifier,
    required String otp,
  }) =>
      _request('POST', '/api/v1/auth/otp/verify',
          body: {'identifier': identifier, 'otp': otp});

  static Future<Map<String, dynamic>> refreshToken(String refreshToken) =>
      _request('POST', '/api/v1/auth/refresh-token',
          body: {'refreshToken': refreshToken});

  static Future<void> logout() => _request('POST', '/api/v1/auth/logout');

  // ══════════════════════════════════════════════════════════════════
  // USERS  →  /api/users
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> getProfile(String userId) =>
      _request('GET', '/api/users/\$userId');

  static Future<Map<String, dynamic>> updateProfile(
          String userId, Map<String, dynamic> data) =>
      _request('PUT', '/api/users/\$userId', body: data);

  // ══════════════════════════════════════════════════════════════════
  // SHOPS  →  /api/shops  &  /api/v1/shops
  // FIXED: was returning List<ShopModel> but class is Shop
  // ══════════════════════════════════════════════════════════════════
  static Future<List<Shop>> getNearbyShops({
    required double lat,
    required double lng,
    double radiusKm = 5.0,
  }) async {
    final res = await _request('GET', '/api/shops', queryParams: {
      'lat': lat.toString(),
      'lng': lng.toString(),
      'radius': radiusKm.toString(),
    });
    final list = res['data'] as List? ?? res['content'] as List? ?? [];
    return list.map((e) => Shop.fromJson(e)).toList();
  }

  static Future<Map<String, dynamic>> getShopById(String shopId) =>
      _request('GET', '/api/shops/\$shopId');

  // ══════════════════════════════════════════════════════════════════
  // PRODUCTS  →  /api/products
  // FIXED: was returning List<ProductModel> but class is Product
  // ══════════════════════════════════════════════════════════════════
  static Future<List<Product>> getProductsByShop(String shopId) async {
    final res = await _request('GET', '/api/products',
        queryParams: {'shopId': shopId});
    final list = res['data'] as List? ?? res['content'] as List? ?? [];
    return list.map((e) => Product.fromJson(e)).toList();
  }

  static Future<Map<String, dynamic>> getProductById(String productId) =>
      _request('GET', '/api/products/\$productId');

  // ══════════════════════════════════════════════════════════════════
  // CART  →  /api/v1/cart
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> getCart(String userId) =>
      _request('GET', '/api/v1/cart/\$userId');

  static Future<Map<String, dynamic>> addToCart({
    required String userId,
    required String productId,
    required int quantity,
  }) =>
      _request('POST', '/api/v1/cart/\$userId/items',
          body: {'productId': productId, 'quantity': quantity});

  static Future<Map<String, dynamic>> removeFromCart(
          String userId, String itemId) =>
      _request('DELETE', '/api/v1/cart/\$userId/items/\$itemId');

  static Future<Map<String, dynamic>> clearCart(String userId) =>
      _request('DELETE', '/api/v1/cart/\$userId');

  // ══════════════════════════════════════════════════════════════════
  // ORDERS  →  /api/orders  &  /api/v1/orders
  // FIXED: was returning List<OrderModel> but class is Order
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> placeOrder(
          Map<String, dynamic> orderData) =>
      _request('POST', '/api/orders', body: orderData);

  static Future<List<Order>> getMyOrders(String userId) async {
    final res = await _request('GET', '/api/orders',
        queryParams: {'userId': userId});
    final list = res['data'] as List? ?? res['content'] as List? ?? [];
    return list.map((e) => Order.fromJson(e)).toList();
  }

  static Future<Map<String, dynamic>> getOrderById(String orderId) =>
      _request('GET', '/api/orders/\$orderId');

  static Future<Map<String, dynamic>> cancelOrder(String orderId) =>
      _request('PATCH', '/api/orders/\$orderId/cancel');

  // ══════════════════════════════════════════════════════════════════
  // DELIVERY / TRACKING  →  /api/v1/delivery
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> getOrderTracking(String orderId) =>
      _request('GET', '/api/v1/delivery/tracking/\$orderId');

  static Future<void> updateDeliveryLocation({
    required String assignmentId,
    required double lat,
    required double lng,
  }) =>
      _request('PATCH', '/api/v1/delivery/tracking/\$assignmentId/location',
          body: {'latitude': lat, 'longitude': lng});

  static Future<List<dynamic>> getDeliveryAssignments(String partnerId) async {
    final res = await _request('GET',
        '/api/v1/delivery/assignments', queryParams: {'partnerId': partnerId});
    return res['data'] as List? ?? res['content'] as List? ?? [];
  }

  static Future<Map<String, dynamic>> updateAssignmentStatus({
    required String assignmentId,
    required String status,
  }) =>
      _request('PATCH',
          '/api/v1/delivery/assignments/\$assignmentId/status',
          body: {'status': status});

  // ══════════════════════════════════════════════════════════════════
  // PAYMENT  →  /api/v1/payments
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> initiatePayment(
          Map<String, dynamic> paymentData) =>
      _request('POST', '/api/v1/payments', body: paymentData);

  static Future<Map<String, dynamic>> verifyPayment(
          Map<String, dynamic> verifyData) =>
      _request('POST', '/api/v1/payments/verify', body: verifyData);

  static Future<Map<String, dynamic>> getPaymentById(String paymentId) =>
      _request('GET', '/api/v1/payments/\$paymentId');

  // ══════════════════════════════════════════════════════════════════
  // WALLET  →  /api/v1/wallet
  // ══════════════════════════════════════════════════════════════════
  static Future<Map<String, dynamic>> getWallet(String userId) =>
      _request('GET', '/api/v1/wallet/\$userId');

  static Future<List<dynamic>> getWalletTransactions(String userId) async {
    final res = await _request('GET', '/api/v1/wallet/\$userId/transactions');
    return res['data'] as List? ?? res['content'] as List? ?? [];
  }

  static Future<Map<String, dynamic>> addMoneyToWallet({
    required String userId,
    required double amount,
    required String paymentId,
  }) =>
      _request('POST', '/api/v1/wallet/\$userId/add',
          body: {'amount': amount, 'paymentId': paymentId});

  static Future<Map<String, dynamic>> walletPayment({
    required String userId,
    required double amount,
    required String orderId,
  }) =>
      _request('POST', '/api/v1/wallet/\$userId/pay',
          body: {'amount': amount, 'orderId': orderId});

  // ══════════════════════════════════════════════════════════════════
  // REVIEWS  →  /api/v1/shops/{shopId}/reviews
  // ══════════════════════════════════════════════════════════════════
  static Future<List<dynamic>> getShopReviews(String shopId) async {
    final res = await _request('GET', '/api/v1/shops/\$shopId/reviews');
    return res['data'] as List? ?? res['content'] as List? ?? [];
  }

  static Future<Map<String, dynamic>> addReview({
    required String shopId,
    required int rating,
    required String comment,
    String? orderId,
  }) =>
      _request('POST', '/api/v1/shops/\$shopId/reviews',
          body: {
            'rating': rating,
            'comment': comment,
            if (orderId != null) 'orderId': orderId,
          });

  static Future<void> deleteReview(String shopId, String reviewId) =>
      _request('DELETE', '/api/v1/shops/\$shopId/reviews/\$reviewId');

  // ══════════════════════════════════════════════════════════════════
  // NOTIFICATIONS  →  /api/v1/notifications
  // ══════════════════════════════════════════════════════════════════
  static Future<List<dynamic>> getNotifications(String userId) async {
    final res = await _request('GET', '/api/v1/notifications',
        queryParams: {'userId': userId});
    return res['data'] as List? ?? res['content'] as List? ?? [];
  }

  static Future<void> markNotificationRead(String notificationId) =>
      _request('PATCH', '/api/v1/notifications/\$notificationId/read');

  static Future<void> registerFcmToken(String userId, String fcmToken) =>
      _request('POST', '/api/v1/notifications/register-device',
          body: {'userId': userId, 'fcmToken': fcmToken});
}

// ─── Exception ────────────────────────────────────────────────────
class ApiException implements Exception {
  final String message;
  final int statusCode;
  ApiException(this.message, this.statusCode);
  @override
  String toString() => 'ApiException(\$statusCode): \$message';
}
