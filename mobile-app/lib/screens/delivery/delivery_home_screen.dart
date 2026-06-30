import 'package:flutter/material.dart';
import '../customer/order_tracking_screen.dart';
import 'active_order_screen.dart';

class DeliveryHomeScreen extends StatefulWidget {
  const DeliveryHomeScreen({super.key});

  @override
  State<DeliveryHomeScreen> createState() => _DeliveryHomeScreenState();
}

class _DeliveryHomeScreenState extends State<DeliveryHomeScreen> {
  bool _isOnline = true;

  final List<Map<String, dynamic>> _availableOrders = [
    {
      'id': 'NK1001',
      'shop': 'Fresh Mart',
      'pickup': '12 Main Street',
      'drop': 'Sai Nagar, 2nd Line',
      'distance': '1.8 km',
      'earnings': 45,
      'items': 4,
    },
    {
      'id': 'NK1002',
      'shop': 'Daily Needs',
      'pickup': '45 Cross Road',
      'drop': 'Temple Street, Block B',
      'distance': '2.3 km',
      'earnings': 52,
      'items': 6,
    },
    {
      'id': 'NK1003',
      'shop': 'Quick Store',
      'pickup': '8 Park Avenue',
      'drop': 'Market Road, Lane 4',
      'distance': '1.2 km',
      'earnings': 38,
      'items': 3,
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Delivery Dashboard'),
        actions: [
          IconButton(
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                builder: (_) => const OrderTrackingScreen(orderId: 'NK1001'),
              ),
            ),
            icon: const Icon(Icons.local_shipping),
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            width: double.infinity,
            margin: const EdgeInsets.all(16),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: _isOnline
                    ? [Colors.green, Colors.lightGreen]
                    : [Colors.grey.shade500, Colors.grey.shade400],
              ),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Today Earnings', style: TextStyle(color: Colors.white70)),
                        SizedBox(height: 4),
                        Text('₹325', style: TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.bold)),
                      ],
                    ),
                    Switch(
                      value: _isOnline,
                      onChanged: (value) => setState(() => _isOnline = value),
                      activeColor: Colors.white,
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: const [
                    _StatChip(label: 'Orders', value: '8'),
                    _StatChip(label: 'Hours', value: '6.5'),
                    _StatChip(label: 'Rating', value: '4.8'),
                  ],
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Available Orders',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
                ),
                Text(
                  _isOnline ? '${_availableOrders.length} orders' : 'Offline',
                  style: const TextStyle(color: Colors.grey),
                ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          Expanded(
            child: !_isOnline
                ? const Center(
                    child: Text('Go online to receive delivery requests', style: TextStyle(color: Colors.grey)),
                  )
                : ListView.builder(
                    itemCount: _availableOrders.length,
                    itemBuilder: (context, index) {
                      final order = _availableOrders[index];
                      return Card(
                        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(order['id'], style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                                    decoration: BoxDecoration(
                                      color: Colors.green.shade50,
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                    child: Text('₹${order['earnings']}', style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold)),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              Row(
                                children: [
                                  const Icon(Icons.store, size: 18, color: Colors.green),
                                  const SizedBox(width: 8),
                                  Expanded(child: Text('${order['shop']} • ${order['pickup']}')),
                                ],
                              ),
                              const SizedBox(height: 8),
                              Row(
                                children: [
                                  const Icon(Icons.location_on, size: 18, color: Colors.orange),
                                  const SizedBox(width: 8),
                                  Expanded(child: Text(order['drop'])),
                                ],
                              ),
                              const SizedBox(height: 12),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text('${order['items']} items • ${order['distance']}', style: const TextStyle(color: Colors.grey)),
                                  ElevatedButton(
                                    onPressed: () {
                                      Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (_) => ActiveOrderScreen(order: order),
                                        ),
                                      );
                                    },
                                    child: const Text('Accept'),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

class _StatChip extends StatelessWidget {
  final String label;
  final String value;
  const _StatChip({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.15),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        children: [
          Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
          Text(label, style: const TextStyle(color: Colors.white70, fontSize: 12)),
        ],
      ),
    );
  }
}
