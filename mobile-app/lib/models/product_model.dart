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
        id: json['id'],
        name: json['name'],
        price: (json['price'] as num).toDouble(),
        imageUrl: json['imageUrl'] ?? '',
        shopId: json['shopId'],
        category: json['category'] ?? '',
        isAvailable: json['isAvailable'] ?? true,
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
