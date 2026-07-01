import 'package:flutter/material.dart';
import '../models/product_model.dart';

class CartItem {
  final String id;
  final String name;
  final double price;
  final String imageUrl;
  int quantity;

  CartItem({
    required this.id,
    required this.name,
    required this.price,
    required this.imageUrl,
    required this.quantity,
  });
}

class CartProvider extends ChangeNotifier {
  final Map<String, CartItem> _items = {};
  String? _shopId;

  Map<String, CartItem> get items => _items;
  int get itemCount => _items.values.fold(0, (sum, i) => sum + i.quantity);
  String? get shopId => _shopId;

  double get totalAmount =>
      _items.values.fold(0.0, (sum, item) => sum + item.price * item.quantity);

  void addItem(Product product) {
    if (_shopId != null && _shopId != product.shopId) {
      _items.clear();
    }
    _shopId = product.shopId;
    if (_items.containsKey(product.id)) {
      _items[product.id]!.quantity++;
    } else {
      _items[product.id] = CartItem(
        id: product.id,
        name: product.name,
        price: product.price,
        imageUrl: product.imageUrl,
        quantity: 1,
      );
    }
    notifyListeners();
  }

  /// Increment an already-in-cart item by its CartItem reference
  void addItemById(CartItem item) {
    if (_items.containsKey(item.id)) {
      _items[item.id]!.quantity++;
      notifyListeners();
    }
  }

  void decreaseItem(String productId) {
    if (!_items.containsKey(productId)) return;
    if (_items[productId]!.quantity > 1) {
      _items[productId]!.quantity--;
    } else {
      _items.remove(productId);
    }
    notifyListeners();
  }

  void removeItem(String productId) {
    _items.remove(productId);
    notifyListeners();
  }

  void clearCart() {
    _items.clear();
    _shopId = null;
    notifyListeners();
  }
}
