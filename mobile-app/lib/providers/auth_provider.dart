import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum UserRole { customer, delivery }

class AuthProvider extends ChangeNotifier {
  String? _userId;
  String? _phone;
  UserRole _role = UserRole.customer;
  bool _isLoading = false;

  String? get userId => _userId;
  String? get phone => _phone;
  UserRole get role => _role;
  bool get isLoading => _isLoading;
  bool get isLoggedIn => _userId != null;

  Future<void> sendOtp(String phone) async {
    _isLoading = true;
    notifyListeners();
    // TODO: Integrate with backend OTP API
    await Future.delayed(const Duration(seconds: 1));
    _phone = phone;
    _isLoading = false;
    notifyListeners();
  }

  Future<bool> verifyOtp(String otp) async {
    _isLoading = true;
    notifyListeners();
    // TODO: Verify OTP with backend
    await Future.delayed(const Duration(seconds: 1));
    if (otp == '123456') { // placeholder
      _userId = 'user_${DateTime.now().millisecondsSinceEpoch}';
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('userId', _userId!);
      _isLoading = false;
      notifyListeners();
      return true;
    }
    _isLoading = false;
    notifyListeners();
    return false;
  }

  void setRole(UserRole role) {
    _role = role;
    notifyListeners();
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('userId');
    _userId = null;
    _phone = null;
    notifyListeners();
  }
}
