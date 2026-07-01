import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/api_service.dart';

/// Roles the user can select on the login screen
enum UserRole { customer, delivery }

class AuthProvider extends ChangeNotifier {
  bool _isAuthenticated = false;
  String? _userId;
  String? _userPhone;
  String? _userName;
  String? _userRole; // CUSTOMER | DELIVERY | MERCHANT
  bool _isLoading = false;
  UserRole _role = UserRole.customer;

  bool get isAuthenticated => _isAuthenticated;
  bool get isLoggedIn => _isAuthenticated; // alias used by SplashScreen
  String? get userId => _userId;
  String? get userPhone => _userPhone;
  String? get phone => _userPhone; // alias used by OtpScreen
  String? get userName => _userName;
  String? get userRole => _userRole;
  bool get isLoading => _isLoading;
  bool get isDelivery => _userRole == 'DELIVERY';
  bool get isCustomer => _userRole == 'CUSTOMER';
  UserRole get role => _role; // enum role used by OtpScreen

  /// Called from LoginScreen to persist the chosen role before sending OTP
  void setRole(UserRole role) {
    _role = role;
    notifyListeners();
  }

  Future<void> initAuth() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('auth_token');
    if (token != null) {
      _isAuthenticated = true;
      _userId = prefs.getString('user_id');
      _userPhone = prefs.getString('user_phone');
      _userName = prefs.getString('user_name');
      _userRole = prefs.getString('user_role') ?? 'CUSTOMER';
      _role = _userRole == 'DELIVERY' ? UserRole.delivery : UserRole.customer;
      notifyListeners();
    }
  }

  Future<void> sendOtp(String phone) async {
    _userPhone = phone; // store so OtpScreen can read auth.phone
    _isLoading = true;
    notifyListeners();
    try {
      await ApiService.sendOtp(phone);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Convenience wrapper used by OtpScreen (phone already stored via sendOtp)
  Future<bool> verifyOtp(String otp) async {
    return verifyOtpAndLogin(_userPhone ?? '', otp);
  }

  Future<bool> verifyOtpAndLogin(String phone, String otp) async {
    _isLoading = true;
    notifyListeners();
    try {
      final res = await ApiService.verifyOtp(identifier: phone, otp: otp);
      final token = res['token'] as String? ?? res['accessToken'] as String?;
      final refreshToken = res['refreshToken'] as String?;
      if (token == null) return false;

      await ApiService.saveTokens(token, refreshToken ?? '');

      final prefs = await SharedPreferences.getInstance();
      final user = res['user'] as Map<String, dynamic>? ?? {};
      _userId = user['id']?.toString() ?? res['userId']?.toString();
      _userPhone = phone;
      _userName = user['name'] as String?;
      _userRole = user['role'] as String? ?? 'CUSTOMER';
      _role = _userRole == 'DELIVERY' ? UserRole.delivery : UserRole.customer;

      await prefs.setString('user_id', _userId ?? '');
      await prefs.setString('user_phone', _userPhone ?? '');
      await prefs.setString('user_name', _userName ?? '');
      await prefs.setString('user_role', _userRole ?? 'CUSTOMER');

      _isAuthenticated = true;
      notifyListeners();
      return true;
    } catch (e) {
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    try {
      await ApiService.logout();
    } catch (_) {}
    await ApiService.clearTokens();
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    _isAuthenticated = false;
    _userId = null;
    _userPhone = null;
    _userName = null;
    _userRole = null;
    _role = UserRole.customer;
    notifyListeners();
  }
}
