import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../providers/cart_provider.dart';
import '../../models/product_model.dart';
import '../../services/api_service.dart';
import '../../core/routes/app_routes.dart';

class ShopDetailScreen extends StatefulWidget {
  final String shopId;
  final String shopName;

  const ShopDetailScreen(
      {super.key, required this.shopId, required this.shopName});

  @override
  State<ShopDetailScreen> createState() => _ShopDetailScreenState();
}

class _ShopDetailScreenState extends State<ShopDetailScreen> {
  List<Product> _products = [];
  bool _loading = true;
  String? _error;
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _loadProducts();
  }

  Future<void> _loadProducts() async {
    try {
      final products = await ApiService.getProductsByShop(widget.shopId);
      if (mounted) {
        setState(() {
          _products = products;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString();
          _loading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();
    final filtered = _products
        .where((p) =>
            p.name.toLowerCase().contains(_searchQuery.toLowerCase()))
        .toList();

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.shopName),
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.shopping_cart_outlined),
                onPressed: () =>
                    Navigator.pushNamed(context, AppRoutes.cart),
              ),
              if (cart.itemCount > 0)
                Positioned(
                  right: 6,
                  top: 6,
                  child: CircleAvatar(
                    radius: 8,
                    backgroundColor: Colors.red,
                    child: Text('${cart.itemCount}',
                        style: const TextStyle(
                            color: Colors.white, fontSize: 10)),
                  ),
                ),
            ],
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _ErrorView(message: _error!, onRetry: _loadProducts)
              : Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(12),
                      child: TextField(
                        decoration: const InputDecoration(
                          hintText: 'Search products...',
                          prefixIcon: Icon(Icons.search),
                          contentPadding:
                              EdgeInsets.symmetric(horizontal: 16),
                        ),
                        onChanged: (v) =>
                            setState(() => _searchQuery = v),
                      ),
                    ),
                    Expanded(
                      child: filtered.isEmpty
                          ? const Center(
                              child: Text('No products found'))
                          : GridView.builder(
                              padding: const EdgeInsets.all(12),
                              gridDelegate:
                                  const SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: 2,
                                crossAxisSpacing: 12,
                                mainAxisSpacing: 12,
                                childAspectRatio: 0.72,
                              ),
                              itemCount: filtered.length,
                              itemBuilder: (_, i) => _ProductCard(
                                product: filtered[i],
                                cartQty: cart.items[filtered[i].id]
                                        ?.quantity ??
                                    0,
                                onAdd: () => context
                                    .read<CartProvider>()
                                    .addItem(filtered[i]),
                                onDecrease: () => context
                                    .read<CartProvider>()
                                    .decreaseItem(filtered[i].id),
                              ),
                            ),
                    ),
                  ],
                ),
      bottomNavigationBar: cart.items.isNotEmpty
          ? SafeArea(
              child: Padding(
                padding: const EdgeInsets.symmetric(
                    horizontal: 16, vertical: 8),
                child: ElevatedButton(
                  onPressed: () =>
                      Navigator.pushNamed(context, AppRoutes.cart),
                  child: Text(
                      'View Cart \u2022 ${cart.itemCount} items \u2022 \u20b9${cart.totalAmount.toStringAsFixed(0)}'),
                ),
              ),
            )
          : null,
    );
  }
}

class _ProductCard extends StatelessWidget {
  final Product product;
  final int cartQty;
  final VoidCallback onAdd;
  final VoidCallback onDecrease;

  const _ProductCard({
    required this.product,
    required this.cartQty,
    required this.onAdd,
    required this.onDecrease,
  });

  @override
  Widget build(BuildContext context) => Card(
        clipBehavior: Clip.antiAlias,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: CachedNetworkImage(
                imageUrl: product.imageUrl,
                width: double.infinity,
                fit: BoxFit.cover,
                placeholder: (_, __) =>
                    Container(color: Colors.grey.shade100),
                errorWidget: (_, __, ___) => Container(
                  color: Colors.grey.shade100,
                  child: const Icon(Icons.image_not_supported,
                      color: Colors.grey),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(product.name,
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 13),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 2),
                  Text(
                      '\u20b9${product.price.toStringAsFixed(0)}',
                      style: TextStyle(
                          color:
                              Theme.of(context).colorScheme.primary,
                          fontWeight: FontWeight.w600)),
                  const SizedBox(height: 6),
                  cartQty == 0
                      ? SizedBox(
                          width: double.infinity,
                          child: ElevatedButton(
                            onPressed: onAdd,
                            style: ElevatedButton.styleFrom(
                                padding: EdgeInsets.zero,
                                minimumSize: const Size(0, 32)),
                            child: const Text('Add'),
                          ),
                        )
                      : Row(
                          mainAxisAlignment:
                              MainAxisAlignment.spaceBetween,
                          children: [
                            InkWell(
                              onTap: onDecrease,
                              child: const CircleAvatar(
                                  radius: 12,
                                  child: Icon(Icons.remove,
                                      size: 14)),
                            ),
                            Text('$cartQty',
                                style: const TextStyle(
                                    fontWeight: FontWeight.bold)),
                            InkWell(
                              onTap: onAdd,
                              child: const CircleAvatar(
                                  radius: 12,
                                  child:
                                      Icon(Icons.add, size: 14)),
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

class _ErrorView extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;
  const _ErrorView({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 12),
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            ElevatedButton(
                onPressed: onRetry, child: const Text('Retry')),
          ],
        ),
      );
}
