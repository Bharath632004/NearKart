import 'package:flutter/material.dart';
import '../../models/order_model.dart';
import '../../core/routes/app_routes.dart';

class OrderTrackingScreen extends StatefulWidget {
  final String orderId;
  const OrderTrackingScreen({super.key, required this.orderId});

  @override
  State<OrderTrackingScreen> createState() => _OrderTrackingScreenState();
}

class _OrderTrackingScreenState extends State<OrderTrackingScreen> {
  OrderStatus _status = OrderStatus.placed;
  bool _delivered = false;

  final List<Map<String, dynamic>> _trackingSteps = [
    {'status': OrderStatus.placed, 'title': 'Order Placed', 'subtitle': 'Your order has been received', 'icon': Icons.receipt_long},
    {'status': OrderStatus.confirmed, 'title': 'Order Confirmed', 'subtitle': 'Shop is preparing your order', 'icon': Icons.thumb_up},
    {'status': OrderStatus.picked, 'title': 'Picked Up', 'subtitle': 'Delivery partner picked your order', 'icon': Icons.shopping_bag},
    {'status': OrderStatus.onTheWay, 'title': 'On The Way', 'subtitle': 'Arriving at your location', 'icon': Icons.directions_bike},
    {'status': OrderStatus.delivered, 'title': 'Delivered', 'subtitle': 'Order delivered successfully', 'icon': Icons.check_circle},
  ];

  @override
  void initState() {
    super.initState();
    _simulateTracking();
  }

  Future<void> _simulateTracking() async {
    final statuses = [
      OrderStatus.confirmed,
      OrderStatus.picked,
      OrderStatus.onTheWay,
      OrderStatus.delivered,
    ];
    for (final status in statuses) {
      await Future.delayed(const Duration(seconds: 4));
      if (!mounted) return;
      setState(() {
        _status = status;
        if (status == OrderStatus.delivered) _delivered = true;
      });
    }
  }

  int get _currentStep =>
      _trackingSteps.indexWhere((s) => s['status'] == _status);

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        Navigator.pushNamedAndRemoveUntil(context, AppRoutes.home, (r) => false);
        return false;
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text('Order #${widget.orderId}'),
          leading: IconButton(
            icon: const Icon(Icons.home),
            onPressed: () => Navigator.pushNamedAndRemoveUntil(
                context, AppRoutes.home, (r) => false),
          ),
          actions: [
            if (!_delivered)
              IconButton(
                icon: const Icon(Icons.map),
                tooltip: 'Live Tracking',
                onPressed: () {
                  Navigator.pushNamed(
                    context,
                    AppRoutes.liveTracking,
                    arguments: {
                      'title': 'Live Order Tracking',
                      'orderId': widget.orderId,
                      'sourceLat': 17.3850,
                      'sourceLng': 78.4867,
                      'destinationLat': 17.3950,
                      'destinationLng': 78.4967,
                      'courierName': 'Ravi Kumar',
                      'vehicleInfo': 'Bike • AP 39 XY 1234',
                    },
                  );
                },
              ),
          ],
        ),
        body: SingleChildScrollView(
          child: Column(
            children: [
              Container(
                width: double.infinity,
                margin: const EdgeInsets.all(16),
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: _delivered
                        ? [Colors.green.shade700, Colors.green.shade400]
                        : [Colors.orange.shade600, Colors.orange.shade400],
                  ),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _delivered ? 'Delivered!' : 'Estimated Delivery',
                      style: const TextStyle(color: Colors.white70, fontSize: 14),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _delivered ? 'Enjoy your order 🎉' : '25 - 35 mins',
                      style: const TextStyle(
                          color: Colors.white,
                          fontSize: 26,
                          fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.2),
                          borderRadius: BorderRadius.circular(20)),
                      child: Text(
                        _trackingSteps[_currentStep]['title'],
                        style: const TextStyle(
                            color: Colors.white, fontWeight: FontWeight.w600),
                      ),
                    ),
                  ],
                ),
              ),
              if (!_delivered)
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.pushNamed(
                          context,
                          AppRoutes.liveTracking,
                          arguments: {
                            'title': 'Live Order Tracking',
                            'orderId': widget.orderId,
                            'sourceLat': 17.3850,
                            'sourceLng': 78.4867,
                            'destinationLat': 17.3950,
                            'destinationLng': 78.4967,
                            'courierName': 'Ravi Kumar',
                            'vehicleInfo': 'Bike • AP 39 XY 1234',
                          },
                        );
                      },
                      icon: const Icon(Icons.map),
                      label: const Text('Open Live Tracking'),
                    ),
                  ),
                ),
              const SizedBox(height: 14),
              if (!_delivered)
                Container(
                  margin: const EdgeInsets.symmetric(horizontal: 16),
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Row(
                    children: [
                      CircleAvatar(
                        backgroundColor: Colors.green.shade100,
                        child: const Icon(Icons.person, color: Colors.green),
                      ),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Delivery Partner',
                                style: TextStyle(fontSize: 12, color: Colors.grey)),
                            Text('Ravi Kumar',
                                style: TextStyle(
                                    fontWeight: FontWeight.bold, fontSize: 15)),
                          ],
                        ),
                      ),
                      IconButton(
                        onPressed: () {},
                        icon: const Icon(Icons.call, color: Colors.green),
                        tooltip: 'Call Delivery Partner',
                      ),
                    ],
                  ),
                ),
              const SizedBox(height: 20),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Align(
                  alignment: Alignment.centerLeft,
                  child: Text('Order Progress',
                      style: Theme.of(context)
                          .textTheme
                          .titleMedium
                          ?.copyWith(fontWeight: FontWeight.bold)),
                ),
              ),
              const SizedBox(height: 8),
              ...List.generate(_trackingSteps.length, (index) {
                final step = _trackingSteps[index];
                final isCompleted = index <= _currentStep;
                final isActive = index == _currentStep;
                final isLast = index == _trackingSteps.length - 1;
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Column(
                        children: [
                          Container(
                            width: 32,
                            height: 32,
                            decoration: BoxDecoration(
                              color: isCompleted
                                  ? Colors.green
                                  : Colors.grey.shade300,
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              isCompleted ? Icons.check : step['icon'],
                              size: 16,
                              color: isCompleted ? Colors.white : Colors.grey,
                            ),
                          ),
                          if (!isLast)
                            Container(
                              width: 2,
                              height: 52,
                              color: isCompleted
                                  ? Colors.green
                                  : Colors.grey.shade300,
                            ),
                        ],
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.only(top: 4),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                step['title'],
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: isCompleted ? Colors.black : Colors.grey,
                                  fontSize: isActive ? 15 : 14,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                step['subtitle'],
                                style: TextStyle(
                                    color: Colors.grey.shade600, fontSize: 12),
                              ),
                              const SizedBox(height: 18),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              }),
              if (_delivered)
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton.icon(
                      onPressed: () => Navigator.pushNamedAndRemoveUntil(
                          context, AppRoutes.home, (r) => false),
                      icon: const Icon(Icons.shopping_bag),
                      label: const Text('Order Again'),
                    ),
                  ),
                ),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }
}
