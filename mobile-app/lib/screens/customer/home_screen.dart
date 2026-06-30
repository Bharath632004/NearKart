import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/cart_provider.dart';
import '../../services/location_service.dart';
import '../../models/shop_model.dart';
import 'cart_screen.dart';
import 'product_list_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String _locationText = 'Detecting location...';
  List<Shop> _shops = [];
  bool _loading = true;
  final _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadLocation();
  }

  Future<void> _loadLocation() async {
    final pos = await LocationService.getCurrentPosition();
    if (pos != null) {
      setState(() {
        _locationText = '${pos.latitude.toStringAsFixed(4)}, ${pos.longitude.toStringAsFixed(4)}';
        // TODO: Replace with real API call using pos.latitude, pos.longitude
        _shops = [
          Shop(id: '1', name: 'Fresh Mart', address: '12 Main St', latitude: pos.latitude, longitude: pos.longitude, distanceKm: 0.3, rating: 4.5),
          Shop(id: '2', name: 'Daily Needs', address: '45 Cross Rd', latitude: pos.latitude, longitude: pos.longitude, distanceKm: 0.7, rating: 4.2),
          Shop(id: '3', name: 'Quick Store', address: '8 Park Ave', latitude: pos.latitude, longitude: pos.longitude, distanceKm: 1.1, rating: 4.0),
        ];
        _loading = false;
      });
    } else {
      setState(() { _locationText = 'Unable to detect location'; _loading = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    final cartCount = context.watch<CartProvider>().itemCount;
    return Scaffold(
      appBar: AppBar(
        title: const Text('NearKart'),
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.shopping_cart),
                onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen())),
              ),
              if (cartCount > 0)
                Positioned(
                  right: 8, top: 8,
                  child: CircleAvatar(
                    radius: 9,
                    backgroundColor: Colors.red,
                    child: Text('$cartCount', style: const TextStyle(fontSize: 11, color: Colors.white)),
                  ),
                ),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            color: Colors.green.shade50,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            child: Row(
              children: [
                const Icon(Icons.location_on, color: Colors.green, size: 18),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(_locationText, style: const TextStyle(fontSize: 13), overflow: TextOverflow.ellipsis),
                ),
                TextButton(
                  onPressed: _loadLocation,
                  child: const Text('Refresh', style: TextStyle(fontSize: 12)),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Search products or shops...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: const Icon(Icons.mic),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text('Nearby Shops', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            ),
          ),
          const SizedBox(height: 8),
          _loading
              ? const Expanded(child: Center(child: CircularProgressIndicator()))
              : Expanded(
                  child: ListView.builder(
                    itemCount: _shops.length,
                    itemBuilder: (context, i) {
                      final shop = _shops[i];
                      return Card(
                        margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor: Colors.green.shade100,
                            child: const Icon(Icons.store, color: Colors.green),
                          ),
                          title: Text(shop.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                          subtitle: Text('${shop.distanceKm.toStringAsFixed(1)} km away • ${shop.address}'),
                          trailing: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Row(mainAxisSize: MainAxisSize.min, children: [
                                const Icon(Icons.star, size: 14, color: Colors.amber),
                                Text('${shop.rating}', style: const TextStyle(fontSize: 12)),
                              ]),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(color: shop.isOpen ? Colors.green : Colors.red, borderRadius: BorderRadius.circular(8)),
                                child: Text(shop.isOpen ? 'Open' : 'Closed', style: const TextStyle(color: Colors.white, fontSize: 10)),
                              ),
                            ],
                          ),
                          onTap: () => Navigator.push(context, MaterialPageRoute(
                            builder: (_) => ProductListScreen(shop: shop),
                          )),
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
