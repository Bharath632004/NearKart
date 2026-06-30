import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/auth_provider.dart';
import '../../services/api_service.dart';
import '../../core/routes/app_routes.dart';
import 'delivery_profile_screen.dart';
import 'delivery_earnings_screen.dart';

class DeliveryHomeScreen extends StatefulWidget {
  const DeliveryHomeScreen({super.key});

  @override
  State<DeliveryHomeScreen> createState() => _DeliveryHomeScreenState();
}

class _DeliveryHomeScreenState extends State<DeliveryHomeScreen> {
  int _currentTab = 0;

  final List<Widget> _tabs = const [
    _DashboardTab(),
    DeliveryEarningsScreen(),
    DeliveryProfileScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(index: _currentTab, children: _tabs),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentTab,
        onTap: (i) => setState(() => _currentTab = i),
        selectedItemColor: const Color(0xFF6C63FF),
        unselectedItemColor: Colors.grey,
        items: const [
          BottomNavigationBarItem(
              icon: Icon(Icons.dashboard_outlined),
              activeIcon: Icon(Icons.dashboard),
              label: 'Dashboard'),
          BottomNavigationBarItem(
              icon: Icon(Icons.account_balance_wallet_outlined),
              activeIcon: Icon(Icons.account_balance_wallet),
              label: 'Earnings'),
          BottomNavigationBarItem(
              icon: Icon(Icons.person_outline),
              activeIcon: Icon(Icons.person),
              label: 'Profile'),
        ],
      ),
    );
  }
}

// ──────────────────────────────────────────────────────────────────
class _DashboardTab extends StatefulWidget {
  const _DashboardTab();
  @override
  State<_DashboardTab> createState() => _DashboardTabState();
}

class _DashboardTabState extends State<_DashboardTab> {
  bool _isOnline = true;
  bool _loading = true;
  List<dynamic> _assignments = [];
  String? _error;
  double _todayEarnings = 0;
  int _todayOrders = 0;
  double _rating = 0;

  @override
  void initState() {
    super.initState();
    _loadDashboard();
  }

  Future<void> _loadDashboard() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final auth = context.read<AuthProvider>();
      final partnerId = auth.userId ?? '';

      final assignments = await ApiService.getDeliveryAssignments(partnerId);

      // Compute today's stats from assignments
      final today = DateTime.now();
      double earnings = 0;
      int orders = 0;
      double ratingSum = 0;
      int ratingCount = 0;

      for (final a in assignments) {
        final dateStr = a['createdAt'] as String? ?? '';
        if (dateStr.length >= 10) {
          final date = DateTime.tryParse(dateStr);
          if (date != null &&
              date.year == today.year &&
              date.month == today.month &&
              date.day == today.day) {
            orders++;
            earnings += (a['earnings'] as num?)?.toDouble() ?? 0;
          }
        }
        final r = (a['rating'] as num?)?.toDouble();
        if (r != null) {
          ratingSum += r;
          ratingCount++;
        }
      }

      // Show only PENDING assignments in the available list
      final pending = assignments
          .where((a) =>
              (a['status'] as String? ?? '').toUpperCase() == 'PENDING' ||
              (a['status'] as String? ?? '').toUpperCase() == 'ASSIGNED')
          .toList();

      setState(() {
        _assignments = pending;
        _todayEarnings = earnings;
        _todayOrders = orders;
        _rating = ratingCount > 0 ? ratingSum / ratingCount : 0;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  Future<void> _acceptAssignment(String assignmentId) async {
    try {
      await ApiService.updateAssignmentStatus(
          assignmentId: assignmentId, status: 'ACCEPTED');
      if (mounted) {
        Navigator.pushNamed(context, AppRoutes.activeOrder,
            arguments: {'assignmentId': assignmentId});
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text('Failed to accept: $e'),
              backgroundColor: Colors.red),
        );
      }
    }
  }

  Future<void> _rejectAssignment(String assignmentId) async {
    try {
      await ApiService.updateAssignmentStatus(
          assignmentId: assignmentId, status: 'REJECTED');
      setState(() {
        _assignments
            .removeWhere((a) => a['id']?.toString() == assignmentId);
      });
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text('Failed: $e'), backgroundColor: Colors.red),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final name = auth.userName ?? 'Partner';

    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Dashboard',
                style:
                    TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
            Text('Hi, $name',
                style:
                    const TextStyle(fontSize: 12, color: Colors.grey)),
          ],
        ),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          // Online / Offline toggle
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: Row(
              children: [
                Text(_isOnline ? 'Online' : 'Offline',
                    style: TextStyle(
                        color: _isOnline ? Colors.green : Colors.grey,
                        fontWeight: FontWeight.w600,
                        fontSize: 13)),
                Switch(
                  value: _isOnline,
                  activeColor: Colors.green,
                  onChanged: (v) => setState(() => _isOnline = v),
                ),
              ],
            ),
          ),
          IconButton(
              icon: const Icon(Icons.refresh), onPressed: _loadDashboard),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildError()
              : RefreshIndicator(
                  onRefresh: _loadDashboard,
                  child: ListView(
                    padding: const EdgeInsets.all(16),
                    children: [
                      _buildStatsCard(),
                      const SizedBox(height: 20),
                      _buildAssignmentsList(),
                    ],
                  ),
                ),
    );
  }

  Widget _buildStatsCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: _isOnline
              ? [const Color(0xFF6C63FF), const Color(0xFF3F3D56)]
              : [Colors.grey.shade500, Colors.grey.shade600],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
              color: (_isOnline
                      ? const Color(0xFF6C63FF)
                      : Colors.grey)
                  .withOpacity(0.35),
              blurRadius: 16,
              offset: const Offset(0, 8))
        ],
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Today Earnings',
                      style: TextStyle(color: Colors.white70, fontSize: 13)),
                  const SizedBox(height: 4),
                  Text(
                    '\u20b9${_todayEarnings.toStringAsFixed(0)}',
                    style: const TextStyle(
                        color: Colors.white,
                        fontSize: 34,
                        fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 14, vertical: 8),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  _isOnline ? '🟢 ONLINE' : '⚫ OFFLINE',
                  style: const TextStyle(
                      color: Colors.white, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _StatChip(
                  label: 'Orders',
                  value: _todayOrders.toString()),
              _StatChip(
                  label: 'Pending',
                  value: _assignments.length.toString()),
              _StatChip(
                  label: 'Rating',
                  value: _rating > 0
                      ? _rating.toStringAsFixed(1)
                      : 'N/A'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildAssignmentsList() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text('Available Orders',
                style:
                    TextStyle(fontSize: 17, fontWeight: FontWeight.bold)),
            Text(
              _isOnline
                  ? '${_assignments.length} waiting'
                  : 'Go online to receive',
              style: TextStyle(
                  color: _isOnline ? const Color(0xFF6C63FF) : Colors.grey,
                  fontSize: 12),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (!_isOnline)
          _buildOfflineBanner()
        else if (_assignments.isEmpty)
          _buildEmptyState()
        else
          ...(_assignments
              .map((a) => _AssignmentCard(
                    assignment: a,
                    onAccept: () =>
                        _acceptAssignment(a['id']?.toString() ?? ''),
                    onReject: () =>
                        _rejectAssignment(a['id']?.toString() ?? ''),
                  ))
              .toList()),
      ],
    );
  }

  Widget _buildOfflineBanner() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.grey.shade100,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: const Row(
        children: [
          Icon(Icons.wifi_off, color: Colors.grey, size: 32),
          SizedBox(width: 12),
          Expanded(
            child: Text(
              'You are offline. Toggle the switch above to go online and receive delivery requests.',
              style: TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 48),
        child: Column(
          children: const [
            Icon(Icons.delivery_dining, size: 72, color: Colors.grey),
            SizedBox(height: 12),
            Text('No pending orders right now',
                style: TextStyle(color: Colors.grey, fontSize: 15)),
            SizedBox(height: 4),
            Text('Pull down to refresh',
                style: TextStyle(color: Colors.grey, fontSize: 12)),
          ],
        ),
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
            ElevatedButton(
                onPressed: _loadDashboard, child: const Text('Retry')),
          ],
        ),
      );
}

// ─── Assignment Card ────────────────────────────────────────────────
class _AssignmentCard extends StatelessWidget {
  final dynamic assignment;
  final VoidCallback onAccept;
  final VoidCallback onReject;

  const _AssignmentCard({
    required this.assignment,
    required this.onAccept,
    required this.onReject,
  });

  @override
  Widget build(BuildContext context) {
    final id = assignment['id']?.toString() ?? '';
    final orderId = assignment['orderId']?.toString() ?? id;
    final shopName = assignment['shopName'] as String? ?? 'Shop';
    final pickupAddress =
        assignment['pickupAddress'] as String? ?? 'Pickup location';
    final dropAddress =
        assignment['dropAddress'] as String? ?? 'Drop location';
    final distanceKm =
        (assignment['distanceKm'] as num?)?.toStringAsFixed(1) ?? '?';
    final earnings =
        (assignment['earnings'] as num?)?.toDouble() ?? 0.0;
    final itemCount = (assignment['itemCount'] as num?)?.toInt() ?? 0;

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.06),
              blurRadius: 10,
              offset: const Offset(0, 4))
        ],
      ),
      child: Column(
        children: [
          // Top row
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '#${orderId.length > 8 ? orderId.substring(0, 8).toUpperCase() : orderId}',
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 15),
                    ),
                    Text(shopName,
                        style: const TextStyle(
                            color: Colors.grey, fontSize: 12)),
                  ],
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.green.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    '\u20b9${earnings.toStringAsFixed(0)}',
                    style: const TextStyle(
                        color: Colors.green,
                        fontWeight: FontWeight.bold,
                        fontSize: 16),
                  ),
                ),
              ],
            ),
          ),

          const Divider(height: 1),

          // Route
          Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              children: [
                _RouteRow(
                    icon: Icons.store,
                    color: Colors.green,
                    label: 'Pickup',
                    address: pickupAddress),
                Padding(
                  padding: const EdgeInsets.only(left: 11),
                  child: Container(
                      height: 18,
                      width: 1,
                      color: Colors.grey.shade300),
                ),
                _RouteRow(
                    icon: Icons.location_on,
                    color: Colors.red,
                    label: 'Drop',
                    address: dropAddress),
              ],
            ),
          ),

          // Meta row
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                const Icon(Icons.straighten, size: 14, color: Colors.grey),
                const SizedBox(width: 4),
                Text('$distanceKm km',
                    style: const TextStyle(
                        color: Colors.grey, fontSize: 12)),
                const SizedBox(width: 16),
                const Icon(Icons.shopping_bag_outlined,
                    size: 14, color: Colors.grey),
                const SizedBox(width: 4),
                Text('$itemCount items',
                    style: const TextStyle(
                        color: Colors.grey, fontSize: 12)),
              ],
            ),
          ),

          const SizedBox(height: 12),
          const Divider(height: 1),

          // Action buttons
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: onReject,
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.red,
                      side: const BorderSide(color: Colors.red),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10)),
                    ),
                    child: const Text('Reject'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  flex: 2,
                  child: ElevatedButton(
                    onPressed: onAccept,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF6C63FF),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10)),
                    ),
                    child: const Text('Accept Order',
                        style: TextStyle(color: Colors.white)),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _RouteRow extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String label;
  final String address;

  const _RouteRow({
    required this.icon,
    required this.color,
    required this.label,
    required this.address,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: color, size: 20),
        const SizedBox(width: 10),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label,
                  style: const TextStyle(
                      color: Colors.grey, fontSize: 11)),
              Text(address,
                  style: const TextStyle(fontSize: 13),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis),
            ],
          ),
        ),
      ],
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
      padding:
          const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.15),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        children: [
          Text(value,
              style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 16)),
          Text(label,
              style: const TextStyle(
                  color: Colors.white70, fontSize: 12)),
        ],
      ),
    );
  }
}
