import 'package:flutter/material.dart';
import '../models/wallet_model.dart';
import '../services/api_service.dart';

class WalletProvider extends ChangeNotifier {
  double _balance = 0.0;
  List<WalletTransaction> _transactions = [];
  bool _isLoading = false;
  String? _error;

  double get balance => _balance;
  List<WalletTransaction> get transactions => _transactions;
  bool get isLoading => _isLoading;
  String? get error => _error;

  void _setLoading(bool v) {
    _isLoading = v;
    notifyListeners();
  }

  Future<void> fetchWallet(String userId) async {
    _setLoading(true);
    _error = null;
    try {
      final res = await ApiService.getWallet(userId);
      final wallet = WalletModel.fromJson(res['data'] ?? res);
      _balance = wallet.balance;

      final txList = await ApiService.getWalletTransactions(userId);
      _transactions = txList
          .map((e) => WalletTransaction.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> addMoney({
    required String userId,
    required double amount,
    required String paymentId,
  }) async {
    _setLoading(true);
    try {
      final res = await ApiService.addMoneyToWallet(
        userId: userId,
        amount: amount,
        paymentId: paymentId,
      );
      _balance = (res['balance'] as num?)?.toDouble() ?? _balance + amount;
      notifyListeners();
      await fetchWallet(userId);
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> payWithWallet({
    required String userId,
    required double amount,
    required String orderId,
  }) async {
    if (_balance < amount) {
      _error = 'Insufficient wallet balance';
      notifyListeners();
      return false;
    }
    _setLoading(true);
    try {
      await ApiService.walletPayment(
          userId: userId, amount: amount, orderId: orderId);
      _balance -= amount;
      notifyListeners();
      await fetchWallet(userId);
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    } finally {
      _setLoading(false);
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
