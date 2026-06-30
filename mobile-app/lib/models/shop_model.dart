class Shop {
  final String id;
  final String name;
  final String address;
  final double latitude;
  final double longitude;
  final double distanceKm;
  final bool isOpen;
  final double rating;

  Shop({
    required this.id,
    required this.name,
    required this.address,
    required this.latitude,
    required this.longitude,
    required this.distanceKm,
    this.isOpen = true,
    this.rating = 0.0,
  });

  factory Shop.fromJson(Map<String, dynamic> json) => Shop(
        id: json['id'],
        name: json['name'],
        address: json['address'],
        latitude: (json['latitude'] as num).toDouble(),
        longitude: (json['longitude'] as num).toDouble(),
        distanceKm: (json['distanceKm'] as num).toDouble(),
        isOpen: json['isOpen'] ?? true,
        rating: (json['rating'] as num?)?.toDouble() ?? 0.0,
      );
}
