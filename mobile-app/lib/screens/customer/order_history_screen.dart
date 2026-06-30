import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../providers/auth_provider.dart';
import '../../services/api_service.dart';
import '../../core/routes/app_routes.dart';

class OrderHistoryScreen extends StatefulWidget {
  const OrderHistoryScreen({super.key});

  @override
  State<OrderHistoryScreen> createState() => _OrderHistoryScreenState();
}

class _OrderHistoryScreenState extends State<OrderHistoryScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  bool _loading = true;
  List<dynamic> _allOrders = [];
  String? _error;

  static const _tabs = ['All', 'Active', 'Delivered', 'Cancelled'];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _tabs.length, vsync: this);
    _loadOrders();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadOrders() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final userId = context.read<AuthProvider>().userId ?? '';
      final orders = await ApiService.getMyOrders(userId);
      setState(() {
        _allOrders = orders.map((o) => o.toJson()).toList();
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  List<dynamic> _filteredOrders(String tab) {
    if (tab == 'All') return _allOrders;
    return _allOrders.where((o) {
      final status = (o['status'] as String? ?? '').toUpperCase();
      switch (tab) {
        case 'Active':
          return ['PENDING', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY']
              .contains(status);
        case 'Delivered':
          return status == 'DELIVERED';
        case 'Cancelled':
          return status == 'CANCELLED';
        default:
          return true;
      }
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        title: const Text('My Orders',
            style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          IconButton(icon: const Icon(Icons.refresh), onPressed: _loadOrders),
        ],
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          labelColor: const Color(0xFF6C63FF),
          unselectedLabelColor: Colors.grey,
          indicatorColor: const Color(0xFF6C63FF),
          tabs: _tabs.map((t) => Tab(text: t)).toList(),
        ),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildError()
              : TabBarView(
                  controller: _tabController,
                  children: _tabs
                      .map((tab) => _OrderList(
                            orders: _filteredOrders(tab),
                            onRefresh: _loadOrders,
                          ))
                      .toList(),
                ),
    );
  }

  Widget _buildError() => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 16),
            Text(_error!, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: _loadOrders, child: const Text('Retry')),
          ],
        ),
      );
}

class _OrderList extends StatelessWidget {
  final List<dynamic> orders;
  final Future<void> Function() onRefresh;

  const _OrderList({required this.orders, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    if (orders.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: const [
            Icon(Icons.shopping_bag_outlined, size: 72, color: Colors.grey),
            SizedBox(height: 12),
            Text('No orders here yet',
                style: TextStyle(color: Colors.grey, fontSize: 16)),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: orders.length,
        itemBuilder: (ctx, i) => _OrderCard(order: orders[i]),
      ),
    );
  }
}

class _OrderCard extends StatelessWidget {
  final dynamic order;
  const _OrderCard({required this.order});

  Color _statusColor(String status) {
    switch (status.toUpperCase()) {
      case 'DELIVERED':
        return Colors.green;
      case 'CANCELLED':
        return Colors.red;
      case 'OUT_FOR_DELIVERY':
        return Colors.blue;
      case 'PREPARING':
      case 'CONFIRMED':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  IconData _statusIcon(String status) {
    switch (status.toUpperCase()) {
      case 'DELIVERED':
        return Icons.check_circle;
      case 'CANCELLED':
        return Icons.cancel;
      case 'OUT_FOR_DELIVERY':
        return Icons.delivery_dining;
      case 'PREPARING':
        return Icons.restaurant;
      case 'CONFIRMED':
        return Icons.thumb_up;
      default:
        return Icons.hourglass_empty;
    }
  }

  @override
  Widget build(BuildContext context) {
    final orderId = order['id']?.toString() ?? order['orderId']?.toString() ?? '';
    final shopName = order['shopName'] as String? ?? 'Shop';
    final shopId = order['shopId']?.toString() ?? '';
    final status = order['status'] as String? ?? 'PENDING';
    final total = (order['totalAmount'] as num?)?.toDouble() ?? 0.0;
    final date = order['createdAt'] as String? ?? '';
    final items = order['items'] as List? ?? [];
    final statusColor = _statusColor(status);

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 4)),
        ],
      ),
      child: Column(
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: statusColor.withOpacity(0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(_statusIcon(status), color: statusColor, size: 22),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(shopName,
                          style: const TextStyle(
                              fontWeight: FontWeight.bold, fontSize: 15)),
                      Text(
                        'Order #${orderId.length > 8 ? orderId.substring(0, 8).toUpperCase() : orderId}',
                        style: const TextStyle(
                            color: Colors.grey, fontSize: 12),
                      ),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: statusColor.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        status.replaceAll('_', ' '),
                        style: TextStyle(
                            color: statusColor,
                            fontSize: 11,
                            fontWeight: FontWeight.w600),
                      ),
                    ),
                    if (date.length >= 10)
                      Padding(
                        padding: const EdgeInsets.only(top: 4),
                        child: Text(
                          date.substring(0, 10),
                          style: const TextStyle(
                              color: Colors.grey, fontSize: 11),
                        ),
                      ),
                  ],
                ),
              ],
            ),
          ),

          if (items.isNotEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                children: (items.take(2).toList())
                    .map((item) => Padding(
                          padding: const EdgeInsets.only(bottom: 4),
                          child: Row(
                            children: [
                              const Icon(Icons.circle,
                                  size: 6, color: Colors.grey),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  item['productName'] as String? ?? 'Item',
                                  style: const TextStyle(
                                      color: Colors.black87, fontSize: 13),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              Text(
                                'x${item['quantity'] ?? 1}',
                                style: const TextStyle(
                                    color: Colors.grey, fontSize: 12),
                              ),
                            ],
                          ),
                        ))
                    .toList(),
              ),
            ),

          if (items.length > 2)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text('+${items.length - 2} more items',
                    style:
                        const TextStyle(color: Colors.grey, fontSize: 12)),
              ),
            ),

          const Divider(height: 16),

          Padding(
            padding:
                const EdgeInsets.only(left: 16, right: 16, bottom: 12),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('₹${total.toStringAsFixed(2)}',
                    style: const TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 16)),
                Row(
                  children: [
                    if (status.toUpperCase() == 'OUT_FOR_DELIVERY' ||
                        status.toUpperCase() == 'CONFIRMED')
                      TextButton.icon(
                        onPressed: () => Navigator.pushNamed(
                          context,
                          AppRoutes.liveTracking,
                          arguments: {
                            'orderId': orderId,
                            'shopName': shopName,
                          },
                        ),
                        icon: const Icon(Icons.map_outlined, size: 16),
                        label: const Text('Track'),
                        style: TextButton.styleFrom(
                            foregroundColor: const Color(0xFF6C63FF)),
                      ),
                    if (status.toUpperCase() == 'DELIVERED')
                      TextButton.icon(
                        onPressed: () => Navigator.pushNamed(
                          context,
                          AppRoutes.reviews,
                          arguments: {
                            'shopId': shopId,
                            'shopName': shopName,
                            'orderId': orderId,
                          },
                        ),
                        icon: const Icon(Icons.star_outline, size: 16),
                        label: const Text('Review'),
                        style: TextButton.styleFrom(
                            foregroundColor: Colors.amber),
                      ),
                    TextButton(
                      onPressed: () => Navigator.pushNamed(
                        context,
                        AppRoutes.orderTracking,
                        arguments: {'orderId': orderId},
                      ),
                      child: const Text('Details'),
                      style: TextButton.styleFrom(
                          foregroundColor: Colors.grey),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
