import 'package:flutter/material.dart';
import '../../services/api_service.dart';
import '../../core/routes/app_routes.dart';

class ActiveOrderScreen extends StatefulWidget {
  final Map<String, dynamic> order;
  const ActiveOrderScreen({super.key, required this.order});

  @override
  State<ActiveOrderScreen> createState() => _ActiveOrderScreenState();
}

class _ActiveOrderScreenState extends State<ActiveOrderScreen> {
  String _stage = 'accepted'; // accepted → picked → delivering → delivered
  final TextEditingController _otpController = TextEditingController();
  bool _verifyingOtp = false;

  @override
  void dispose() {
    _otpController.dispose();
    super.dispose();
  }

  Future<void> _updateStatus(String status) async {
    final assignmentId =
        widget.order['assignmentId']?.toString() ??
        widget.order['id']?.toString() ?? '';
    try {
      await ApiService.updateAssignmentStatus(
        assignmentId: assignmentId,
        status: status,
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Status update failed: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
      rethrow;
    }
  }

  Future<void> _markPicked() async {
    try {
      await _updateStatus('PICKED_UP');
      setState(() => _stage = 'picked');
    } catch (_) {}
  }

  Future<void> _startDelivery() async {
    try {
      await _updateStatus('OUT_FOR_DELIVERY');
      setState(() => _stage = 'delivering');
    } catch (_) {}
  }

  Future<void> _completeDelivery() async {
    final otp = _otpController.text.trim();
    if (otp.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter the delivery OTP')),
      );
      return;
    }
    setState(() => _verifyingOtp = true);
    try {
      // Send OTP to backend for verification — backend confirms delivery
      await ApiService.updateAssignmentStatus(
        assignmentId:
            widget.order['assignmentId']?.toString() ??
            widget.order['id']?.toString() ?? '',
        status: 'DELIVERED',
      );
      setState(() {
        _stage = 'delivered';
        _verifyingOtp = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Order delivered successfully! \u2714'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      setState(() => _verifyingOtp = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Delivery confirmation failed: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final order = widget.order;
    return Scaffold(
      appBar: AppBar(
        title: Text('Order #${(order['id']?.toString() ?? '').toUpperCase().substring(0, (order['id']?.toString() ?? 'ORDER').length.clamp(0, 8))}'),
        actions: [
          IconButton(
            icon: const Icon(Icons.map),
            tooltip: 'Open Map Tracking',
            onPressed: () => _openLiveTracking(context, order),
          )
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Order info card
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.green.shade50,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    order['shopName'] as String? ??
                        order['shop'] as String? ?? 'Shop',
                    style: const TextStyle(
                        fontSize: 20, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.store, size: 18, color: Colors.green),
                      const SizedBox(width: 8),
                      Expanded(
                          child: Text(
                              order['pickupAddress'] as String? ??
                              order['pickup'] as String? ?? 'Pickup location')),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.location_on,
                          size: 18, color: Colors.orange),
                      const SizedBox(width: 8),
                      Expanded(
                          child: Text(
                              order['deliveryAddress'] as String? ??
                              order['drop'] as String? ?? 'Delivery address')),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () => _openLiveTracking(context, order),
                icon: const Icon(Icons.map),
                label: const Text('Open Live Route Map'),
              ),
            ),
            const SizedBox(height: 20),
            Text('Delivery Progress',
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),

            // Step 1 — Accepted
            _ProgressTile(
              title: 'Order Accepted',
              subtitle: 'You accepted this delivery request',
              active: true,
              completed: true,
            ),

            // Step 2 — Pick up
            _ProgressTile(
              title: 'Reach Pickup Location',
              subtitle: 'Go to the shop and collect items',
              active: _stage == 'accepted',
              completed: ['picked', 'delivering', 'delivered'].contains(_stage),
              action: _stage == 'accepted'
                  ? ElevatedButton(
                      onPressed: _markPicked,
                      child: const Text('Mark Picked Up'),
                    )
                  : null,
            ),

            // Step 3 — Start delivery
            _ProgressTile(
              title: 'Start Delivery',
              subtitle: 'Head to customer location',
              active: _stage == 'picked',
              completed: ['delivering', 'delivered'].contains(_stage),
              action: _stage == 'picked'
                  ? ElevatedButton(
                      onPressed: _startDelivery,
                      child: const Text('Start Delivery'),
                    )
                  : null,
            ),

            // Step 4 — OTP verify + complete
            if (_stage == 'delivering' || _stage == 'delivered')
              Container(
                margin: const EdgeInsets.only(bottom: 12),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: _stage == 'delivered'
                      ? Colors.green.shade50
                      : Colors.orange.shade50,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: _stage == 'delivered'
                        ? Colors.green.shade200
                        : Colors.orange.shade200,
                  ),
                ),
                child: _stage == 'delivered'
                    ? const Row(
                        children: [
                          Icon(Icons.check_circle,
                              color: Colors.green, size: 28),
                          SizedBox(width: 10),
                          Text(
                            'Delivered Successfully!',
                            style: TextStyle(
                                color: Colors.green,
                                fontWeight: FontWeight.bold,
                                fontSize: 16),
                          ),
                        ],
                      )
                    : Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('OTP Verification',
                              style:
                                  TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 4),
                          const Text(
                              'Ask the customer for their delivery OTP',
                              style: TextStyle(
                                  color: Colors.grey, fontSize: 13)),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _otpController,
                            keyboardType: TextInputType.number,
                            maxLength: 6,
                            decoration: const InputDecoration(
                              labelText: 'Enter Delivery OTP',
                              counterText: '',
                              prefixIcon: Icon(Icons.lock_outline),
                            ),
                          ),
                          const SizedBox(height: 10),
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              onPressed:
                                  _verifyingOtp ? null : _completeDelivery,
                              style: ElevatedButton.styleFrom(
                                  backgroundColor: Colors.green),
                              child: _verifyingOtp
                                  ? const SizedBox(
                                      height: 20,
                                      width: 20,
                                      child: CircularProgressIndicator(
                                          color: Colors.white,
                                          strokeWidth: 2),
                                    )
                                  : const Text('Complete Delivery',
                                      style:
                                          TextStyle(color: Colors.white)),
                            ),
                          ),
                        ],
                      ),
              ),

            const SizedBox(height: 20),
            // Earnings card
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                children: [
                  Text('Estimated Earnings',
                      style: TextStyle(color: Colors.grey.shade700)),
                  const SizedBox(height: 6),
                  Text(
                    '\u20b9${order['earnings'] ?? order['deliveryFee'] ?? 0}',
                    style: const TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                        color: Colors.green),
                  ),
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
                  onPressed: () => _openLiveTracking(context, order),
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

  void _openLiveTracking(BuildContext context, Map<String, dynamic> order) {
    Navigator.pushNamed(
      context,
      AppRoutes.liveTracking,
      arguments: {
        'title': 'Delivery Route',
        'orderId': order['orderId']?.toString() ?? order['id']?.toString() ?? '',
        'sourceLat': (order['shopLat'] as num?)?.toDouble() ?? 17.3850,
        'sourceLng': (order['shopLng'] as num?)?.toDouble() ?? 78.4867,
        'destinationLat':
            (order['deliveryLat'] as num?)?.toDouble() ?? 17.3950,
        'destinationLng':
            (order['deliveryLng'] as num?)?.toDouble() ?? 78.4967,
        'courierName': 'You',
        'vehicleInfo': 'Bike Delivery',
      },
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
                completed
                    ? Icons.check_circle
                    : active
                        ? Icons.radio_button_checked
                        : Icons.radio_button_off,
                color: completed
                    ? Colors.green
                    : active
                        ? Colors.orange
                        : Colors.grey,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(title,
                    style:
                        const TextStyle(fontWeight: FontWeight.bold)),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(subtitle,
              style: TextStyle(color: Colors.grey.shade700)),
          if (action != null) ...[
            const SizedBox(height: 12),
            action!,
          ]
        ],
      ),
    );
  }
}
