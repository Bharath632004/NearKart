import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../models/shop_model.dart';
import '../../models/product_model.dart';
import '../../providers/cart_provider.dart';
import 'cart_screen.dart';

class ProductListScreen extends StatelessWidget {
  final Shop shop;
  const ProductListScreen({super.key, required this.shop});

  // Placeholder products - replace with ApiService.getShopProducts(shop.id)
  List<Product> get _products => [
    Product(id: 'p1', name: 'Milk 500ml', price: 28.0, imageUrl: '', shopId: shop.id, category: 'Dairy'),
    Product(id: 'p2', name: 'Bread', price: 35.0, imageUrl: '', shopId: shop.id, category: 'Bakery'),
    Product(id: 'p3', name: 'Eggs (6 pcs)', price: 60.0, imageUrl: '', shopId: shop.id, category: 'Dairy'),
    Product(id: 'p4', name: 'Rice 1kg', price: 55.0, imageUrl: '', shopId: shop.id, category: 'Grains'),
    Product(id: 'p5', name: 'Sugar 1kg', price: 42.0, imageUrl: '', shopId: shop.id, category: 'Essentials'),
  ];

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();
    return Scaffold(
      appBar: AppBar(
        title: Text(shop.name),
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.shopping_cart),
                onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen())),
              ),
              if (cart.itemCount > 0)
                Positioned(right: 8, top: 8,
                  child: CircleAvatar(radius: 9, backgroundColor: Colors.red,
                    child: Text('${cart.itemCount}', style: const TextStyle(fontSize: 11, color: Colors.white)))),
            ],
          ),
        ],
      ),
      body: ListView.builder(
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
                    width: 60, height: 60,
                    decoration: BoxDecoration(color: Colors.grey.shade100, borderRadius: BorderRadius.circular(8)),
                    child: const Icon(Icons.inventory_2, color: Colors.grey),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(p.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                        Text(p.category, style: const TextStyle(color: Colors.grey, fontSize: 12)),
                        Text('₹${p.price.toStringAsFixed(0)}', style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold)),
                      ],
                    ),
                  ),
                  qty == 0
                      ? ElevatedButton(
                          onPressed: () => context.read<CartProvider>().addItem(p),
                          child: const Text('Add'),
                        )
                      : Row(
                          children: [
                            IconButton(icon: const Icon(Icons.remove_circle_outline), onPressed: () => context.read<CartProvider>().decreaseItem(p.id)),
                            Text('$qty', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                            IconButton(icon: const Icon(Icons.add_circle_outline, color: Colors.green), onPressed: () => context.read<CartProvider>().addItem(p)),
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
                  onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen())),
                  child: Text('View Cart (${cart.itemCount} items) • ₹${cart.totalAmount.toStringAsFixed(0)}'),
                ),
              ),
            )
          : null,
    );
  }
}
