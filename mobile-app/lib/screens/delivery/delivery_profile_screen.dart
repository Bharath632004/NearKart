import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/auth_provider.dart';
import '../../services/api_service.dart';
import '../../core/routes/app_routes.dart';

class DeliveryProfileScreen extends StatefulWidget {
  const DeliveryProfileScreen({super.key});

  @override
  State<DeliveryProfileScreen> createState() =>
      _DeliveryProfileScreenState();
}

class _DeliveryProfileScreenState
    extends State<DeliveryProfileScreen> {
  bool _loading = true;
  Map<String, dynamic> _profile = {};
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadProfile();
  }

  Future<void> _loadProfile() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final auth = context.read<AuthProvider>();
      final data = await ApiService.getProfile(auth.userId ?? '');
      setState(() {
        _profile = data;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  Future<void> _confirmLogout() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Logout'),
        content:
            const Text('Are you sure you want to logout?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red),
            child: const Text('Logout',
                style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
    if (confirm == true && mounted) {
      await context.read<AuthProvider>().logout();
      if (mounted) {
        Navigator.pushNamedAndRemoveUntil(
            context, AppRoutes.login, (_) => false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final name =
        _profile['name'] as String? ?? auth.userName ?? 'Partner';
    final phone =
        _profile['phone'] as String? ?? auth.userPhone ?? '';
    final email = _profile['email'] as String? ?? '';
    final vehicleType =
        _profile['vehicleType'] as String? ?? 'Bike';
    final vehicleNo =
        _profile['vehicleNumber'] as String? ?? '';
    final rating =
        (_profile['rating'] as num?)?.toStringAsFixed(1) ?? 'N/A';
    final totalDeliveries =
        (_profile['totalDeliveries'] as num?)?.toInt() ?? 0;

    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
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
                          onPressed: _loadProfile,
                          child: const Text('Retry')),
                    ],
                  ),
                )
              : CustomScrollView(
                  slivers: [
                    SliverAppBar(
                      expandedHeight: 230,
                      pinned: true,
                      backgroundColor: const Color(0xFF6C63FF),
                      flexibleSpace: FlexibleSpaceBar(
                        background: Container(
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(
                              colors: [
                                Color(0xFF6C63FF),
                                Color(0xFF3F3D56)
                              ],
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                            ),
                          ),
                          child: Column(
                            mainAxisAlignment:
                                MainAxisAlignment.center,
                            children: [
                              const SizedBox(height: 50),
                              CircleAvatar(
                                radius: 44,
                                backgroundColor:
                                    Colors.white.withOpacity(0.2),
                                child: Text(
                                  name.isNotEmpty
                                      ? name[0].toUpperCase()
                                      : 'D',
                                  style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 36,
                                      fontWeight: FontWeight.bold),
                                ),
                              ),
                              const SizedBox(height: 10),
                              Text(name,
                                  style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 20,
                                      fontWeight:
                                          FontWeight.bold)),
                              const SizedBox(height: 4),
                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 12, vertical: 4),
                                decoration: BoxDecoration(
                                  color:
                                      Colors.white.withOpacity(0.2),
                                  borderRadius:
                                      BorderRadius.circular(20),
                                ),
                                child: const Text('DELIVERY PARTNER',
                                    style: TextStyle(
                                        color: Colors.white,
                                        fontSize: 11)),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    SliverToBoxAdapter(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          children: [
                            // Stats
                            Row(
                              children: [
                                Expanded(
                                    child: _StatCard(
                                        label: 'Rating',
                                        value: rating,
                                        icon: Icons.star,
                                        color: Colors.amber)),
                                const SizedBox(width: 12),
                                Expanded(
                                    child: _StatCard(
                                        label: 'Deliveries',
                                        value: totalDeliveries
                                            .toString(),
                                        icon:
                                            Icons.delivery_dining,
                                        color:
                                            const Color(0xFF6C63FF))),
                              ],
                            ),
                            const SizedBox(height: 16),
                            // Info card
                            _InfoCard(children: [
                              _InfoRow(
                                  icon: Icons.phone,
                                  label: 'Phone',
                                  value: phone),
                              if (email.isNotEmpty) ...
                                [
                                  const Divider(height: 16),
                                  _InfoRow(
                                      icon: Icons.email,
                                      label: 'Email',
                                      value: email),
                                ],
                              if (vehicleNo.isNotEmpty) ...
                                [
                                  const Divider(height: 16),
                                  _InfoRow(
                                      icon: Icons.two_wheeler,
                                      label: 'Vehicle',
                                      value:
                                          '$vehicleType  •  $vehicleNo'),
                                ],
                            ]),
                            const SizedBox(height: 16),
                            // Menu
                            _InfoCard(children: [
                              _MenuTile(
                                  icon: Icons.help_outline,
                                  label: 'Help & Support',
                                  onTap: () {}),
                              const Divider(height: 1, indent: 52),
                              _MenuTile(
                                  icon: Icons.policy_outlined,
                                  label: 'Terms & Policy',
                                  onTap: () {}),
                            ]),
                            const SizedBox(height: 24),
                            SizedBox(
                              width: double.infinity,
                              child: OutlinedButton.icon(
                                onPressed: _confirmLogout,
                                icon: const Icon(Icons.logout,
                                    color: Colors.red),
                                label: const Text('Logout',
                                    style: TextStyle(
                                        color: Colors.red,
                                        fontWeight: FontWeight.w600,
                                        fontSize: 16)),
                                style: OutlinedButton.styleFrom(
                                  padding:
                                      const EdgeInsets.symmetric(
                                          vertical: 14),
                                  side: const BorderSide(
                                      color: Colors.red),
                                  shape: RoundedRectangleBorder(
                                      borderRadius:
                                          BorderRadius.circular(
                                              12)),
                                ),
                              ),
                            ),
                            const SizedBox(height: 32),
                            const Text('NearKart v1.0.0',
                                style: TextStyle(
                                    color: Colors.grey,
                                    fontSize: 12)),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
    );
  }
}

class _StatCard extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final Color color;

  const _StatCard({
    required this.label,
    required this.value,
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
      child: Row(
        children: [
          Icon(icon, color: color, size: 28),
          const SizedBox(width: 10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(value,
                  style: TextStyle(
                      color: color,
                      fontSize: 20,
                      fontWeight: FontWeight.bold)),
              Text(label,
                  style: const TextStyle(
                      color: Colors.grey, fontSize: 12)),
            ],
          ),
        ],
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  final List<Widget> children;
  const _InfoCard({required this.children});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 4))
        ],
      ),
      child: Column(children: children),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const _InfoRow(
      {required this.icon,
      required this.label,
      required this.value});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: const Color(0xFF6C63FF), size: 20),
        const SizedBox(width: 12),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label,
                style: const TextStyle(
                    color: Colors.grey, fontSize: 12)),
            Text(value,
                style: const TextStyle(
                    fontWeight: FontWeight.w600, fontSize: 14)),
          ],
        ),
      ],
    );
  }
}

class _MenuTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  const _MenuTile(
      {required this.icon,
      required this.label,
      required this.onTap});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: const Color(0xFF6C63FF).withOpacity(0.08),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Icon(icon,
            color: const Color(0xFF6C63FF), size: 20),
      ),
      title: Text(label, style: const TextStyle(fontSize: 14)),
      trailing: const Icon(Icons.chevron_right, color: Colors.grey),
      onTap: onTap,
    );
  }
}
