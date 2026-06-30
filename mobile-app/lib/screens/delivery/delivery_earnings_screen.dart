import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/auth_provider.dart';
import '../../services/api_service.dart';

class DeliveryEarningsScreen extends StatefulWidget {
  const DeliveryEarningsScreen({super.key});

  @override
  State<DeliveryEarningsScreen> createState() =>
      _DeliveryEarningsScreenState();
}

class _DeliveryEarningsScreenState
    extends State<DeliveryEarningsScreen> {
  bool _loading = true;
  String? _error;
  double _todayEarnings = 0;
  double _weekEarnings = 0;
  double _totalEarnings = 0;
  int _totalDeliveries = 0;
  List<dynamic> _transactions = [];

  @override
  void initState() {
    super.initState();
    _loadEarnings();
  }

  Future<void> _loadEarnings() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final partnerId =
          context.read<AuthProvider>().userId ?? '';
      final assignments =
          await ApiService.getDeliveryAssignments(partnerId);

      final now = DateTime.now();
      double today = 0, week = 0, total = 0;
      int deliveries = 0;

      for (final a in assignments) {
        final status =
            (a['status'] as String? ?? '').toUpperCase();
        if (status != 'DELIVERED') continue;
        deliveries++;
        final earnings =
            (a['earnings'] as num?)?.toDouble() ?? 0;
        total += earnings;

        final dateStr = a['createdAt'] as String? ?? '';
        if (dateStr.length >= 10) {
          final date = DateTime.tryParse(dateStr);
          if (date != null) {
            final diff = now.difference(date).inDays;
            if (diff == 0) today += earnings;
            if (diff < 7) week += earnings;
          }
        }
      }

      setState(() {
        _todayEarnings = today;
        _weekEarnings = week;
        _totalEarnings = total;
        _totalDeliveries = deliveries;
        _transactions = assignments
            .where((a) =>
                (a['status'] as String? ?? '').toUpperCase() ==
                'DELIVERED')
            .toList()
          ..sort((a, b) => (b['createdAt'] as String? ?? '')
              .compareTo(a['createdAt'] as String? ?? ''));
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        title: const Text('Earnings',
            style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: _loadEarnings),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline,
                          size: 64, color: Colors.red),
                      const SizedBox(height: 16),
                      Text(_error!),
                      const SizedBox(height: 16),
                      ElevatedButton(
                          onPressed: _loadEarnings,
                          child: const Text('Retry')),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadEarnings,
                  child: ListView(
                    padding: const EdgeInsets.all(16),
                    children: [
                      _buildTotalCard(),
                      const SizedBox(height: 16),
                      _buildPeriodCards(),
                      const SizedBox(height: 24),
                      _buildDeliveryList(),
                    ],
                  ),
                ),
    );
  }

  Widget _buildTotalCard() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF6C63FF), Color(0xFF3F3D56)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
              color: const Color(0xFF6C63FF).withOpacity(0.4),
              blurRadius: 20,
              offset: const Offset(0, 8))
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Total Earned',
              style: TextStyle(color: Colors.white70)),
          const SizedBox(height: 6),
          Text('\u20b9${_totalEarnings.toStringAsFixed(2)}',
              style: const TextStyle(
                  color: Colors.white,
                  fontSize: 36,
                  fontWeight: FontWeight.bold)),
          const SizedBox(height: 12),
          Text('$_totalDeliveries deliveries completed',
              style: const TextStyle(
                  color: Colors.white70, fontSize: 13)),
        ],
      ),
    );
  }

  Widget _buildPeriodCards() {
    return Row(
      children: [
        Expanded(
          child: _PeriodCard(
              label: 'Today',
              amount: _todayEarnings,
              icon: Icons.today,
              color: Colors.teal),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _PeriodCard(
              label: 'This Week',
              amount: _weekEarnings,
              icon: Icons.date_range,
              color: Colors.orange),
        ),
      ],
    );
  }

  Widget _buildDeliveryList() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Delivery History',
            style:
                TextStyle(fontSize: 17, fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        if (_transactions.isEmpty)
          Center(
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 32),
              child: Column(
                children: const [
                  Icon(Icons.receipt_long,
                      size: 64, color: Colors.grey),
                  SizedBox(height: 8),
                  Text('No deliveries yet',
                      style: TextStyle(color: Colors.grey)),
                ],
              ),
            ),
          )
        else
          ...(_transactions.map((a) => _DeliveryTile(assignment: a))),
      ],
    );
  }
}

class _PeriodCard extends StatelessWidget {
  final String label;
  final double amount;
  final IconData icon;
  final Color color;

  const _PeriodCard({
    required this.label,
    required this.amount,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 8,
              offset: const Offset(0, 3))
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color, size: 24),
          const SizedBox(height: 8),
          Text('\u20b9${amount.toStringAsFixed(0)}',
              style: TextStyle(
                  color: color,
                  fontSize: 22,
                  fontWeight: FontWeight.bold)),
          Text(label,
              style: const TextStyle(
                  color: Colors.grey, fontSize: 12)),
        ],
      ),
    );
  }
}

class _DeliveryTile extends StatelessWidget {
  final dynamic assignment;
  const _DeliveryTile({required this.assignment});

  @override
  Widget build(BuildContext context) {
    final orderId =
        assignment['orderId']?.toString() ?? assignment['id']?.toString() ?? '';
    final shopName = assignment['shopName'] as String? ?? 'Shop';
    final earnings =
        (assignment['earnings'] as num?)?.toDouble() ?? 0.0;
    final date = assignment['createdAt'] as String? ?? '';

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 6,
              offset: const Offset(0, 2))
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: Colors.green.withOpacity(0.1),
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.check_circle,
                color: Colors.green, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(shopName,
                    style: const TextStyle(
                        fontWeight: FontWeight.w600, fontSize: 14)),
                Text(
                  '#${orderId.length > 8 ? orderId.substring(0, 8).toUpperCase() : orderId}'
                  '${date.length >= 10 ? '  •  ${date.substring(0, 10)}' : ''}',
                  style: const TextStyle(
                      color: Colors.grey, fontSize: 12),
                ),
              ],
            ),
          ),
          Text(
            '+\u20b9${earnings.toStringAsFixed(0)}',
            style: const TextStyle(
                color: Colors.green,
                fontWeight: FontWeight.bold,
                fontSize: 15),
          ),
        ],
      ),
    );
  }
}
