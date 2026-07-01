import 'package:flutter/material.dart';
import 'dart:async';
import '../../services/api_service.dart';

class LiveTrackingScreen extends StatefulWidget {
  final String orderId;
  final String shopName;

  const LiveTrackingScreen(
      {super.key, required this.orderId, required this.shopName});

  @override
  State<LiveTrackingScreen> createState() => _LiveTrackingScreenState();
}

class _LiveTrackingScreenState extends State<LiveTrackingScreen> {
  Timer? _timer;
  Map<String, dynamic>? _trackingData;
  bool _loading = true;

  static const _statusLabels = {
    'placed': 'Order Placed',
    'confirmed': 'Confirmed by Shop',
    'picked': 'Picked Up',
    'onTheWay': 'On the Way',
    'delivered': 'Delivered',
    'cancelled': 'Cancelled',
  };

  @override
  void initState() {
    super.initState();
    _fetchTracking();
    _timer =
        Timer.periodic(const Duration(seconds: 10), (_) => _fetchTracking());
  }

  Future<void> _fetchTracking() async {
    try {
      final data = await ApiService.getOrderTracking(widget.orderId);
      if (mounted) setState(() { _trackingData = data; _loading = false; });
    } catch (_) {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final rawStatus =
        (_trackingData?['status'] as String? ?? '').toLowerCase();
    final statusLabel =
        _statusLabels[rawStatus] ?? _trackingData?['status'] ?? 'Fetching...';
    final eta = _trackingData?['estimatedMinutes'];

    return Scaffold(
      appBar: AppBar(title: Text('Tracking \u2014 ${widget.shopName}')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _trackingData == null
              ? const Center(child: Text('Tracking data unavailable'))
              : Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Row(
                            children: [
                              const Icon(Icons.delivery_dining,
                                  color: Colors.green, size: 40),
                              const SizedBox(width: 16),
                              Column(
                                crossAxisAlignment:
                                    CrossAxisAlignment.start,
                                children: [
                                  Text(statusLabel,
                                      style: const TextStyle(
                                          fontWeight: FontWeight.bold,
                                          fontSize: 16)),
                                  if (eta != null)
                                    Text('ETA: $eta min',
                                        style: const TextStyle(
                                            color: Colors.grey)),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),
                      Expanded(
                        child: Card(
                          child: Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.map_outlined,
                                    size: 80,
                                    color: Colors.grey.shade300),
                                const SizedBox(height: 12),
                                const Text(
                                  'Live map requires a Google Maps API key.\nAdd it in AndroidManifest.xml and Info.plist.',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(color: Colors.grey),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
    );
  }
}
