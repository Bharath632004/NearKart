enum TransactionType { credit, debit }

class WalletModel {
  final String userId;
  final double balance;
  final List<WalletTransaction> transactions;

  WalletModel({
    required this.userId,
    required this.balance,
    required this.transactions,
  });

  factory WalletModel.fromJson(Map<String, dynamic> json) => WalletModel(
        userId: json['userId']?.toString() ?? '',
        balance: (json['balance'] as num?)?.toDouble() ?? 0.0,
        transactions: (json['transactions'] as List<dynamic>? ?? [])
            .map((e) => WalletTransaction.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class WalletTransaction {
  final String id;
  final double amount;
  final TransactionType type;
  final String description;
  final DateTime createdAt;

  WalletTransaction({
    required this.id,
    required this.amount,
    required this.type,
    required this.description,
    required this.createdAt,
  });

  factory WalletTransaction.fromJson(Map<String, dynamic> json) =>
      WalletTransaction(
        id: json['id']?.toString() ?? '',
        amount: (json['amount'] as num).toDouble(),
        type: json['type'] == 'CREDIT'
            ? TransactionType.credit
            : TransactionType.debit,
        description: json['description'] as String? ?? '',
        createdAt: DateTime.parse(json['createdAt'] as String),
      );
}
