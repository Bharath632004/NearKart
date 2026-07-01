class Product {
  final String id;
  final String name;
  final double price;
  final String imageUrl;
  final String shopId;
  final String category;
  final bool isAvailable;

  Product({
    required this.id,
    required this.name,
    required this.price,
    required this.imageUrl,
    required this.shopId,
    this.category = '',
    this.isAvailable = true,
  });

  factory Product.fromJson(Map<String, dynamic> json) => Product(
        // Use toString() for safety — backend may send int IDs
        id: json['id']?.toString() ?? '',
        name: json['name'] as String? ?? '',
        price: (json['price'] as num).toDouble(),
        imageUrl: json['imageUrl'] as String? ?? '',
        shopId: json['shopId']?.toString() ?? '',
        category: json['category'] as String? ?? '',
        isAvailable: json['isAvailable'] as bool? ?? true,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'price': price,
        'imageUrl': imageUrl,
        'shopId': shopId,
        'category': category,
        'isAvailable': isAvailable,
      };
}
