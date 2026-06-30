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
        id: json['id'],
        shopId: json['shopId'],
        userId: json['userId'],
        items: (json['items'] as List)
            .map((e) => OrderItem.fromJson(e))
            .toList(),
        totalAmount: (json['totalAmount'] as num).toDouble(),
        status: OrderStatus.values.firstWhere(
          (e) => e.name == json['status'],
          orElse: () => OrderStatus.placed,
        ),
        createdAt: DateTime.parse(json['createdAt']),
        deliveryAddress: json['deliveryAddress'],
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
        productId: json['productId'],
        name: json['name'],
        quantity: json['quantity'],
        price: (json['price'] as num).toDouble(),
      );
}
