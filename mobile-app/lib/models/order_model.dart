enum OrderStatus { placed, confirmed, picked, onTheWay, delivered, cancelled }

class Order {
  final String id;
  final String shopId;
  final String userId;
  final List<OrderItem> items;
  final double totalAmount;
  final OrderStatus status;
  final DateTime createdAt;
  final String deliveryAddress;

  Order({
    required this.id,
    required this.shopId,
    required this.userId,
    required this.items,
    required this.totalAmount,
    required this.status,
    required this.createdAt,
    required this.deliveryAddress,
  });

  factory Order.fromJson(Map<String, dynamic> json) => Order(
        id: json['id']?.toString() ?? '',
        shopId: json['shopId']?.toString() ?? '',
        userId: json['userId']?.toString() ?? '',
        items: (json['items'] as List? ?? [])
            .map((e) => OrderItem.fromJson(e))
            .toList(),
        totalAmount: (json['totalAmount'] as num?)?.toDouble() ?? 0.0,
        status: OrderStatus.values.firstWhere(
          (e) => e.name.toLowerCase() == (json['status'] as String? ?? '').toLowerCase(),
          orElse: () => OrderStatus.placed,
        ),
        createdAt: json['createdAt'] != null
            ? DateTime.tryParse(json['createdAt']) ?? DateTime.now()
            : DateTime.now(),
        deliveryAddress: json['deliveryAddress'] as String? ?? '',
      );

  // ADDED: used by OrderHistoryScreen to convert back to Map for display
  Map<String, dynamic> toJson() => {
        'id': id,
        'shopId': shopId,
        'shopName': null, // populated from API response if available
        'userId': userId,
        'items': items.map((e) => e.toJson()).toList(),
        'totalAmount': totalAmount,
        'status': status.name,
        'createdAt': createdAt.toIso8601String(),
        'deliveryAddress': deliveryAddress,
      };
}

class OrderItem {
  final String productId;
  final String name;
  final int quantity;
  final double price;

  OrderItem({
    required this.productId,
    required this.name,
    required this.quantity,
    required this.price,
  });

  factory OrderItem.fromJson(Map<String, dynamic> json) => OrderItem(
        productId: json['productId']?.toString() ?? '',
        name: json['name'] as String? ?? json['productName'] as String? ?? 'Item',
        quantity: (json['quantity'] as num?)?.toInt() ?? 1,
        price: (json['price'] as num?)?.toDouble() ?? 0.0,
      );

  // ADDED: used by Order.toJson()
  Map<String, dynamic> toJson() => {
        'productId': productId,
        'productName': name,
        'quantity': quantity,
        'price': price,
      };
}
