import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../models/shop_model.dart';
import '../../models/product_model.dart';
import '../../providers/cart_provider.dart';
import '../../services/api_service.dart';
import 'cart_screen.dart';

class ProductListScreen extends StatefulWidget {
  final Shop shop;
  const ProductListScreen({super.key, required this.shop});

  @override
  State<ProductListScreen> createState() => _ProductListScreenState();
}

class _ProductListScreenState extends State<ProductListScreen> {
  List<Product> _products = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadProducts();
  }

  Future<void> _loadProducts() async {
    setState(() { _loading = true; _error = null; });
    try {
      final products = await ApiService.getProductsByShop(widget.shop.id);
      setState(() {
        _products = products;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Could not load products. Please try again.';
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.shop.name),
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.shopping_cart),
                onPressed: () => Navigator.push(
                    context, MaterialPageRoute(builder: (_) => const CartScreen())),
              ),
              if (cart.itemCount > 0)
                Positioned(
                  right: 8, top: 8,
                  child: CircleAvatar(
                    radius: 9,
                    backgroundColor: Colors.red,
                    child: Text('${cart.itemCount}',
                        style: const TextStyle(fontSize: 11, color: Colors.white)),
                  ),
                ),
            ],
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.error_outline, size: 60, color: Colors.grey),
                        const SizedBox(height: 12),
                        Text(_error!,
                            textAlign: TextAlign.center,
                            style: const TextStyle(color: Colors.grey)),
                        const SizedBox(height: 16),
                        ElevatedButton(
                            onPressed: _loadProducts, child: const Text('Retry')),
                      ],
                    ),
                  ),
                )
              : _products.isEmpty
                  ? const Center(
                      child: Text('No products available',
                          style: TextStyle(color: Colors.grey, fontSize: 16)),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(12),
                      itemCount: _products.length,
                      itemBuilder: (context, i) {
                        final p = _products[i];
                        final qty = cart.items[p.id]?.quantity ?? 0;
                        return Card(
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Row(
                              children: [
                                Container(
                                  width: 60,
                                  height: 60,
                                  decoration: BoxDecoration(
                                    color: Colors.grey.shade100,
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: p.imageUrl.isNotEmpty
                                      ? ClipRRect(
                                          borderRadius: BorderRadius.circular(8),
                                          child: Image.network(
                                            p.imageUrl,
                                            fit: BoxFit.cover,
                                            errorBuilder: (_, __, ___) =>
                                                const Icon(Icons.inventory_2,
                                                    color: Colors.grey),
                                          ),
                                        )
                                      : const Icon(Icons.inventory_2,
                                          color: Colors.grey),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(p.name,
                                          style: const TextStyle(
                                              fontWeight: FontWeight.bold)),
                                      if (p.category.isNotEmpty)
                                        Text(p.category,
                                            style: const TextStyle(
                                                color: Colors.grey, fontSize: 12)),
                                      Text('\u20b9${p.price.toStringAsFixed(0)}',
                                          style: const TextStyle(
                                              color: Colors.green,
                                              fontWeight: FontWeight.bold)),
                                      if (!p.isAvailable)
                                        const Text('Out of stock',
                                            style: TextStyle(
                                                color: Colors.red, fontSize: 11)),
                                    ],
                                  ),
                                ),
                                if (!p.isAvailable)
                                  const Text('Unavailable',
                                      style: TextStyle(
                                          color: Colors.grey, fontSize: 12))
                                else if (qty == 0)
                                  ElevatedButton(
                                    onPressed: () =>
                                        context.read<CartProvider>().addItem(p),
                                    child: const Text('Add'),
                                  )
                                else
                                  Row(
                                    children: [
                                      IconButton(
                                        icon: const Icon(Icons.remove_circle_outline),
                                        onPressed: () => context
                                            .read<CartProvider>()
                                            .decreaseItem(p.id),
                                      ),
                                      Text('$qty',
                                          style: const TextStyle(
                                              fontWeight: FontWeight.bold,
                                              fontSize: 16)),
                                      IconButton(
                                        icon: const Icon(Icons.add_circle_outline,
                                            color: Colors.green),
                                        onPressed: () =>
                                            context.read<CartProvider>().addItem(p),
                                      ),
                                    ],
                                  ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
      bottomNavigationBar: cart.itemCount > 0
          ? SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: ElevatedButton(
                  onPressed: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const CartScreen())),
                  child: Text(
                      'View Cart (${cart.itemCount} items) \u2022 \u20b9${cart.totalAmount.toStringAsFixed(0)}'),
                ),
              ),
            )
          : null,
    );
  }
}
