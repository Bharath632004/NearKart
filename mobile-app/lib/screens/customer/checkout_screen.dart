import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:razorpay_flutter/razorpay_flutter.dart';
import '../../providers/cart_provider.dart';
import '../../providers/auth_provider.dart';
import '../../providers/order_provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/routes/app_routes.dart';

class CheckoutScreen extends StatefulWidget {
  const CheckoutScreen({super.key});

  @override
  State<CheckoutScreen> createState() => _CheckoutScreenState();
}

class _CheckoutScreenState extends State<CheckoutScreen> {
  final _addressController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  String _paymentMethod = 'cod';
  late Razorpay _razorpay;
  bool _isPlacingOrder = false;

  @override
  void initState() {
    super.initState();
    _razorpay = Razorpay();
    _razorpay.on(Razorpay.EVENT_PAYMENT_SUCCESS, _onPaymentSuccess);
    _razorpay.on(Razorpay.EVENT_PAYMENT_ERROR, _onPaymentError);
    _razorpay.on(Razorpay.EVENT_EXTERNAL_WALLET, _onExternalWallet);
  }

  @override
  void dispose() {
    _addressController.dispose();
    _razorpay.clear();
    super.dispose();
  }

  void _onPaymentSuccess(PaymentSuccessResponse response) {
    _placeOrder(transactionId: response.paymentId);
  }

  void _onPaymentError(PaymentFailureResponse response) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Payment failed: ${response.message}')),
    );
  }

  void _onExternalWallet(ExternalWalletResponse response) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('External wallet selected: ${response.walletName}')),
    );
  }

  void _openRazorpay() {
    final cart = context.read<CartProvider>();
    final auth = context.read<AuthProvider>();
    final options = {
      'key': AppConstants.razorpayKey,
      'amount': (cart.totalAmount * 100).toInt(), // in paise
      'name': 'NearKart',
      'description': 'Order Payment',
      'prefill': {
        'contact': auth.phone ?? '',
      },
      'theme': {'color': '#2E7D32'},
    };
    try {
      _razorpay.open(options);
    } catch (e) {
      debugPrint('Razorpay error: $e');
    }
  }

  Future<void> _placeOrder({String? transactionId}) async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isPlacingOrder = true);

    final cart = context.read<CartProvider>();
    final auth = context.read<AuthProvider>();
    final orderProvider = context.read<OrderProvider>();

    final items = cart.items.values
        .map((item) => {
              'productId': item.id,
              'name': item.name,
              'quantity': item.quantity,
              'price': item.price,
            })
        .toList();

    final orderId = await orderProvider.placeOrder(
      shopId: cart.shopId ?? '',
      userId: auth.userId ?? '',
      items: items,
      totalAmount: cart.totalAmount,
      deliveryAddress: _addressController.text.trim(),
    );

    setState(() => _isPlacingOrder = false);

    if (orderId != null && mounted) {
      cart.clearCart();
      Navigator.pushNamedAndRemoveUntil(
        context,
        AppRoutes.orderTracking,
        (route) => route.settings.name == AppRoutes.home,
        arguments: orderId,
      );
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to place order. Try again.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();
    final double deliveryFee = cart.totalAmount >= 200 ? 0.0 : 20.0;
    final double total = cart.totalAmount + deliveryFee;

    return Scaffold(
      appBar: AppBar(title: const Text('Checkout')),
      body: Form(
        key: _formKey,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Delivery Address',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const SizedBox(height: 8),
              TextFormField(
                controller: _addressController,
                maxLines: 3,
                decoration: const InputDecoration(
                  hintText: 'Enter full delivery address',
                  prefixIcon: Icon(Icons.location_on),
                ),
                validator: (val) =>
                    val == null || val.trim().isEmpty ? 'Address required' : null,
              ),
              const SizedBox(height: 24),
              const Text('Payment Method',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const SizedBox(height: 8),
              RadioListTile(
                title: const Text('Cash on Delivery'),
                subtitle: const Text('Pay when your order arrives'),
                secondary: const Icon(Icons.money, color: Colors.green),
                value: 'cod',
                groupValue: _paymentMethod,
                onChanged: (v) => setState(() => _paymentMethod = v!),
              ),
              RadioListTile(
                title: const Text('Razorpay'),
                subtitle: const Text('UPI / Debit / Credit Card'),
                secondary: const Icon(Icons.payment, color: Colors.blue),
                value: 'razorpay',
                groupValue: _paymentMethod,
                onChanged: (v) => setState(() => _paymentMethod = v!),
              ),
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.grey.shade50,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.grey.shade200),
                ),
                child: Column(
                  children: [
                    const Text('Order Summary',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 12),
                    ...cart.items.values.map((item) => Padding(
                          padding: const EdgeInsets.symmetric(vertical: 4),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${item.name} x${item.quantity}',
                                  style: const TextStyle(fontSize: 14)),
                              Text('\u20b9${(item.price * item.quantity).toStringAsFixed(0)}'),
                            ],
                          ),
                        )),
                    const Divider(height: 20),
                    _SummaryRow(label: 'Subtotal', value: '\u20b9${cart.totalAmount.toStringAsFixed(0)}'),
                    _SummaryRow(
                      label: 'Delivery Fee',
                      value: deliveryFee == 0 ? 'FREE' : '\u20b9${deliveryFee.toStringAsFixed(0)}',
                      valueColor: deliveryFee == 0 ? Colors.green : null,
                    ),
                    if (deliveryFee == 0)
                      const Padding(
                        padding: EdgeInsets.only(bottom: 6),
                        child: Text('Free delivery on orders above \u20b9200',
                            style: TextStyle(color: Colors.green, fontSize: 12)),
                      ),
                    const Divider(height: 12),
                    _SummaryRow(
                      label: 'Total',
                      value: '\u20b9${total.toStringAsFixed(0)}',
                      bold: true,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  onPressed: _isPlacingOrder
                      ? null
                      : () {
                          if (_paymentMethod == 'razorpay') {
                            if (_formKey.currentState!.validate()) _openRazorpay();
                          } else {
                            _placeOrder();
                          }
                        },
                  child: _isPlacingOrder
                      ? const CircularProgressIndicator(color: Colors.white)
                      : Text(
                          _paymentMethod == 'razorpay'
                              ? 'Pay \u20b9${total.toStringAsFixed(0)} via Razorpay'
                              : 'Place Order \u2022 \u20b9${total.toStringAsFixed(0)}',
                          style: const TextStyle(fontSize: 16),
                        ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SummaryRow extends StatelessWidget {
  final String label;
  final String value;
  final bool bold;
  final Color? valueColor;
  const _SummaryRow({required this.label, required this.value, this.bold = false, this.valueColor});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: TextStyle(fontWeight: bold ? FontWeight.bold : FontWeight.normal, fontSize: bold ? 16 : 14)),
          Text(value, style: TextStyle(fontWeight: bold ? FontWeight.bold : FontWeight.normal, fontSize: bold ? 16 : 14, color: valueColor)),
        ],
      ),
    );
  }
}
