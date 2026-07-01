import 'package:flutter/material.dart';
import '../services/api_service.dart';

class ReviewItem {
  final String id;
  final String userId;
  final String userName;
  final int rating;
  final String comment;
  final DateTime createdAt;

  ReviewItem({
    required this.id,
    required this.userId,
    required this.userName,
    required this.rating,
    required this.comment,
    required this.createdAt,
  });

  factory ReviewItem.fromJson(Map<String, dynamic> json) => ReviewItem(
        id: json['id']?.toString() ?? '',
        userId: json['userId']?.toString() ?? '',
        userName: json['userName'] as String? ?? 'User',
        rating: json['rating'] as int? ?? 5,
        comment: json['comment'] as String? ?? '',
        createdAt: DateTime.tryParse(json['createdAt'] as String? ?? '') ??
            DateTime.now(),
      );
}

class ReviewProvider extends ChangeNotifier {
  List<ReviewItem> _reviews = [];
  double _averageRating = 0.0;
  bool _isLoading = false;
  String? _error;

  List<ReviewItem> get reviews => _reviews;
  double get averageRating => _averageRating;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchReviews(String shopId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();
    try {
      final list = await ApiService.getShopReviews(shopId);
      _reviews = list
          .map((e) => ReviewItem.fromJson(e as Map<String, dynamic>))
          .toList();
      if (_reviews.isNotEmpty) {
        _averageRating =
            _reviews.map((r) => r.rating).reduce((a, b) => a + b) /
                _reviews.length;
      } else {
        _averageRating = 0.0;
      }
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> addReview({
    required String shopId,
    required int rating,
    required String comment,
    String? orderId,
  }) async {
    try {
      final res = await ApiService.addReview(
          shopId: shopId, rating: rating, comment: comment, orderId: orderId);
      final newReview = ReviewItem.fromJson(res['data'] ?? res);
      _reviews.insert(0, newReview);
      _averageRating =
          _reviews.map((r) => r.rating).reduce((a, b) => a + b) /
              _reviews.length;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteReview(String shopId, String reviewId) async {
    try {
      await ApiService.deleteReview(shopId, reviewId);
      _reviews.removeWhere((r) => r.id == reviewId);
      if (_reviews.isNotEmpty) {
        _averageRating =
            _reviews.map((r) => r.rating).reduce((a, b) => a + b) /
                _reviews.length;
      } else {
        _averageRating = 0.0;
      }
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
