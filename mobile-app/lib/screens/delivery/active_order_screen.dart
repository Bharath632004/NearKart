import 'package:flutter/material.dart';
import '../../core/routes/app_routes.dart';

class ActiveOrderScreen extends StatefulWidget {
  final Map<String, dynamic> order;
  const ActiveOrderScreen({super.key, required this.order});

  @override
  State<ActiveOrderScreen> createState() => _ActiveOrderScreenState();
}

class _ActiveOrderScreenState extends State<ActiveOrderScreen> {
  String _stage = 'accepted';
  final TextEditingController _otpController = TextEditingController();

  @override
  void dispose() {
    _otpController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final order = widget.order;
    return Scaffold(
      appBar: AppBar(
        title: Text('Order ${order['id']}'),
        actions: [
          IconButton(
            icon: const Icon(Icons.map),
            tooltip: 'Open Map Tracking',
            onPressed: () {
              Navigator.pushNamed(
                context,
                AppRoutes.liveTracking,
                arguments: {
                  'title': 'Delivery Route',
                  'orderId': order['id'] ?? 'ORDER',
                  'sourceLat': 17.3850,
                  'sourceLng': 78.4867,
                  'destinationLat': 17.3950,
                  'destinationLng': 78.4967,
                  'courierName': 'You',
                  'vehicleInfo': 'Bike Delivery',
                },
              );
            },
          )
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.green.shade50,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(order['shop'], style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.store, size: 18, color: Colors.green),
                      const SizedBox(width: 8),
                      Expanded(child: Text(order['pickup'])),
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
                ],
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () {
                  Navigator.pushNamed(
                    context,
                    AppRoutes.liveTracking,
                    arguments: {
                      'title': 'Delivery Route',
                      'orderId': order['id'] ?? 'ORDER',
                      'sourceLat': 17.3850,
                      'sourceLng': 78.4867,
                      'destinationLat': 17.3950,
                      'destinationLng': 78.4967,
                      'courierName': 'You',
                      'vehicleInfo': 'Bike Delivery',
                    },
                  );
                },
                icon: const Icon(Icons.map),
                label: const Text('Open Live Route Map'),
              ),
            ),
            const SizedBox(height: 20),
            Text('Delivery Progress', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            _ProgressTile(
              title: 'Order Accepted',
              subtitle: 'You accepted this delivery request',
              active: true,
              completed: true,
            ),
            _ProgressTile(
              title: 'Reach Pickup Location',
              subtitle: 'Go to the shop and collect items',
              active: _stage == 'accepted',
              completed: _stage == 'picked' || _stage == 'delivering' || _stage == 'delivered',
              action: _stage == 'accepted'
                  ? ElevatedButton(
                      onPressed: () => setState(() => _stage = 'picked'),
                      child: const Text('Mark Picked'),
                    )
                  : null,
            ),
            _ProgressTile(
              title: 'Deliver Order',
              subtitle: 'Deliver to customer location',
              active: _stage == 'picked' || _stage == 'delivering',
              completed: _stage == 'delivered',
              action: _stage == 'picked'
                  ? ElevatedButton(
                      onPressed: () => setState(() => _stage = 'delivering'),
                      child: const Text('Start Delivery'),
                    )
                  : null,
            ),
            if (_stage == 'delivering' || _stage == 'delivered')
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.orange.shade50,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('OTP Verification', style: TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    const Text('Ask customer for OTP to complete delivery'),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _otpController,
                      keyboardType: TextInputType.number,
                      maxLength: 6,
                      decoration: const InputDecoration(
                        labelText: 'Enter Delivery OTP',
                        counterText: '',
                      ),
                    ),
                    const SizedBox(height: 8),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: () {
                          if (_otpController.text == '1234' || _otpController.text == '123456') {
                            setState(() => _stage = 'delivered');
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Order delivered successfully')),
                            );
                          } else {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Invalid OTP')),
                            );
                          }
                        },
                        child: const Text('Complete Delivery'),
                      ),
                    ),
                  ],
                ),
              ),
            const SizedBox(height: 20),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                children: [
                  Text('Estimated Earnings', style: TextStyle(color: Colors.grey.shade700)),
                  const SizedBox(height: 6),
                  Text('₹${order['earnings']}', style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.green)),
                ],
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () {
                    Navigator.pushNamed(
                      context,
                      AppRoutes.liveTracking,
                      arguments: {
                        'title': 'Delivery Route',
                        'orderId': order['id'] ?? 'ORDER',
                        'sourceLat': 17.3850,
                        'sourceLng': 78.4867,
                        'destinationLat': 17.3950,
                        'destinationLng': 78.4967,
                        'courierName': 'You',
                        'vehicleInfo': 'Bike Delivery',
                      },
                    );
                  },
                  icon: const Icon(Icons.map),
                  label: const Text('Navigate'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: () {},
                  icon: const Icon(Icons.call),
                  label: const Text('Call'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ProgressTile extends StatelessWidget {
  final String title;
  final String subtitle;
  final bool active;
  final bool completed;
  final Widget? action;

  const _ProgressTile({
    required this.title,
    required this.subtitle,
    this.active = false,
    this.completed = false,
    this.action,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: completed
            ? Colors.green.shade50
            : active
                ? Colors.orange.shade50
                : Colors.grey.shade100,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
          color: completed
              ? Colors.green.shade200
              : active
                  ? Colors.orange.shade200
                  : Colors.grey.shade300,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                completed ? Icons.check_circle : active ? Icons.radio_button_checked : Icons.radio_button_off,
                color: completed ? Colors.green : active ? Colors.orange : Colors.grey,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(subtitle, style: TextStyle(color: Colors.grey.shade700)),
          if (action != null) ...[
            const SizedBox(height: 12),
            action!,
          ]
        ],
      ),
    );
  }
}
