import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../providers/cart_provider.dart';
import 'checkout_screen.dart';

class CartScreen extends StatelessWidget {
  const CartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();
    const double deliveryFee = 30.0;
    final double grandTotal =
        cart.items.isEmpty ? 0.0 : cart.totalAmount + deliveryFee;

    return Scaffold(
      appBar: AppBar(
        title: const Text('My Cart'),
        actions: [
          if (cart.items.isNotEmpty)
            TextButton(
              onPressed: () => _confirmClear(context, cart),
              child:
                  const Text('Clear', style: TextStyle(color: Colors.white)),
            ),
        ],
      ),
      body: cart.items.isEmpty
          ? _buildEmptyCart(context)
          : Column(
              children: [
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.all(12),
                    children: cart.items.values
                        .map((item) => _CartItemTile(
                              item: item,
                              onIncrease: () => context
                                  .read<CartProvider>()
                                  .addItemById(item),
                              onDecrease: () => context
                                  .read<CartProvider>()
                                  .decreaseItem(item.id),
                              onRemove: () => context
                                  .read<CartProvider>()
                                  .removeItem(item.id),
                            ))
                        .toList(),
                  ),
                ),
                _OrderSummaryBar(
                  subtotal: cart.totalAmount,
                  deliveryFee: deliveryFee,
                  grandTotal: grandTotal,
                  onCheckout: () => Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => const CheckoutScreen()),
                  ),
                ),
              ],
            ),
    );
  }

  Widget _buildEmptyCart(BuildContext context) => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.shopping_cart_outlined,
                size: 100, color: Colors.grey.shade300),
            const SizedBox(height: 16),
            Text('Your cart is empty',
                style: TextStyle(
                    fontSize: 18,
                    color: Colors.grey.shade600,
                    fontWeight: FontWeight.w500)),
            const SizedBox(height: 8),
            Text('Add items from a nearby shop',
                style: TextStyle(color: Colors.grey.shade400)),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Browse Shops'),
            ),
          ],
        ),
      );

  void _confirmClear(BuildContext context, CartProvider cart) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Clear Cart?'),
        content: const Text('All items will be removed.'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel')),
          TextButton(
            onPressed: () {
              cart.clearCart();
              Navigator.pop(context);
            },
            child:
                const Text('Clear', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }
}

class _CartItemTile extends StatelessWidget {
  final CartItem item;
  final VoidCallback onIncrease;
  final VoidCallback onDecrease;
  final VoidCallback onRemove;

  const _CartItemTile({
    required this.item,
    required this.onIncrease,
    required this.onDecrease,
    required this.onRemove,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: CachedNetworkImage(
                imageUrl: item.imageUrl,
                width: 64,
                height: 64,
                fit: BoxFit.cover,
                placeholder: (_, __) => Container(
                    color: Colors.grey.shade100,
                    child: const Icon(Icons.image, color: Colors.grey)),
                errorWidget: (_, __, ___) => Container(
                    color: Colors.grey.shade100,
                    child: const Icon(Icons.image_not_supported,
                        color: Colors.grey)),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(item.name,
                      style:
                          const TextStyle(fontWeight: FontWeight.bold),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 4),
                  Text('\u20b9${item.price.toStringAsFixed(0)} each',
                      style:
                          TextStyle(color: Colors.grey.shade600)),
                ],
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                    '\u20b9${(item.price * item.quantity).toStringAsFixed(0)}',
                    style: const TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 15)),
                const SizedBox(height: 8),
                Row(
                  children: [
                    _CircleButton(
                        icon: Icons.remove,
                        onTap: onDecrease,
                        color: Colors.red),
                    Padding(
                      padding:
                          const EdgeInsets.symmetric(horizontal: 8),
                      child: Text('${item.quantity}',
                          style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 16)),
                    ),
                    _CircleButton(
                        icon: Icons.add,
                        onTap: onIncrease,
                        color: Colors.green),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _CircleButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  final Color color;

  const _CircleButton(
      {required this.icon, required this.onTap, required this.color});

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          width: 28,
          height: 28,
          decoration: BoxDecoration(
              color: color.withOpacity(0.1), shape: BoxShape.circle),
          child: Icon(icon, size: 16, color: color),
        ),
      );
}

class _OrderSummaryBar extends StatelessWidget {
  final double subtotal;
  final double deliveryFee;
  final double grandTotal;
  final VoidCallback onCheckout;

  const _OrderSummaryBar({
    required this.subtotal,
    required this.deliveryFee,
    required this.grandTotal,
    required this.onCheckout,
  });

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          boxShadow: [
            BoxShadow(
                color: Colors.grey.shade200,
                blurRadius: 10,
                offset: const Offset(0, -3))
          ],
        ),
        child: Column(
          children: [
            _SummaryRow('Subtotal',
                '\u20b9${subtotal.toStringAsFixed(0)}'),
            const SizedBox(height: 4),
            _SummaryRow('Delivery Fee',
                '\u20b9${deliveryFee.toStringAsFixed(0)}'),
            const Divider(),
            _SummaryRow(
                'Total', '\u20b9${grandTotal.toStringAsFixed(0)}',
                bold: true),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                onPressed: onCheckout,
                child: const Text('Proceed to Checkout',
                    style: TextStyle(fontSize: 16)),
              ),
            ),
          ],
        ),
      );
}

class _SummaryRow extends StatelessWidget {
  final String label;
  final String value;
  final bool bold;

  const _SummaryRow(this.label, this.value, {this.bold = false});

  @override
  Widget build(BuildContext context) => Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label,
              style: TextStyle(
                  fontWeight:
                      bold ? FontWeight.bold : FontWeight.normal,
                  fontSize: bold ? 16 : 14)),
          Text(value,
              style: TextStyle(
                  fontWeight:
                      bold ? FontWeight.bold : FontWeight.normal,
                  fontSize: bold ? 16 : 14,
                  color: bold
                      ? Theme.of(context).colorScheme.primary
                      : null)),
        ],
      );
}
