class UserModel {
  final String id;
  final String name;
  final String phone;
  final String email;
  final String role; // CUSTOMER | DELIVERY
  final String? profileImageUrl;
  final String? address;

  UserModel({
    required this.id,
    required this.name,
    required this.phone,
    required this.email,
    required this.role,
    this.profileImageUrl,
    this.address,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) => UserModel(
        id: json['id']?.toString() ?? '',
        name: json['name'] as String? ?? '',
        phone: json['phone'] as String? ?? '',
        email: json['email'] as String? ?? '',
        role: json['role'] as String? ?? 'CUSTOMER',
        profileImageUrl: json['profileImageUrl'] as String?,
        address: json['address'] as String?,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'phone': phone,
        'email': email,
        'role': role,
        if (profileImageUrl != null) 'profileImageUrl': profileImageUrl,
        if (address != null) 'address': address,
      };

  UserModel copyWith({
    String? name,
    String? email,
    String? address,
    String? profileImageUrl,
  }) =>
      UserModel(
        id: id,
        name: name ?? this.name,
        phone: phone,
        email: email ?? this.email,
        role: role,
        profileImageUrl: profileImageUrl ?? this.profileImageUrl,
        address: address ?? this.address,
      );
}
