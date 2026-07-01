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
        // Use toString() for safety — backend may send int IDs
        id: json['id']?.toString() ?? '',
        shopId: json['shopId']?.toString() ?? '',
        userId: json['userId']?.toString() ?? '',
        items: (json['items'] as List)
            .map((e) => OrderItem.fromJson(e))
            .toList(),
        totalAmount: (json['totalAmount'] as num).toDouble(),
        status: OrderStatus.values.firstWhere(
          (e) => e.name == json['status'],
          orElse: () => OrderStatus.placed,
        ),
        createdAt: DateTime.parse(json['createdAt'] as String),
        deliveryAddress: json['deliveryAddress'] as String? ?? '',
      );
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
        name: json['name'] as String? ?? '',
        quantity: json['quantity'] as int? ?? 1,
        price: (json['price'] as num).toDouble(),
      );
}
