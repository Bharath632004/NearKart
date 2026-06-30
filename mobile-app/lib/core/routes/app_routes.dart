import 'package:flutter/material.dart';
import '../../screens/splash_screen.dart';
import '../../screens/auth/login_screen.dart';
import '../../screens/auth/otp_screen.dart';
import '../../screens/customer/home_screen.dart';
import '../../screens/customer/cart_screen.dart';
import '../../screens/customer/checkout_screen.dart';
import '../../screens/customer/order_tracking_screen.dart';
import '../../screens/delivery/delivery_home_screen.dart';
import '../../screens/delivery/active_order_screen.dart';
import '../../screens/common/live_tracking_screen.dart';

class AppRoutes {
  static const String splash = '/';
  static const String login = '/login';
  static const String otp = '/otp';
  static const String home = '/home';
  static const String cart = '/cart';
  static const String checkout = '/checkout';
  static const String orderTracking = '/order-tracking';
  static const String deliveryHome = '/delivery-home';
  static const String activeOrder = '/active-order';
  static const String liveTracking = '/live-tracking';

  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case splash:
        return _build(const SplashScreen());
      case login:
        return _build(const LoginScreen());
      case otp:
        return _build(const OtpScreen());
      case home:
        return _build(const HomeScreen());
      case cart:
        return _build(const CartScreen());
      case checkout:
        return _build(const CheckoutScreen());
      case orderTracking:
        final orderId = settings.arguments as String? ?? 'ORDER';
        return _build(OrderTrackingScreen(orderId: orderId));
      case deliveryHome:
        return _build(const DeliveryHomeScreen());
      case activeOrder:
        final order = settings.arguments as Map<String, dynamic>? ?? {};
        return _build(ActiveOrderScreen(order: order));
      case liveTracking:
        final args = settings.arguments as Map<String, dynamic>? ?? {};
        return _build(LiveTrackingScreen(
          title: args['title'] ?? 'Live Tracking',
          orderId: args['orderId'] ?? 'ORDER',
          sourceLat: (args['sourceLat'] ?? 17.3850).toDouble(),
          sourceLng: (args['sourceLng'] ?? 78.4867).toDouble(),
          destinationLat: (args['destinationLat'] ?? 17.3950).toDouble(),
          destinationLng: (args['destinationLng'] ?? 78.4967).toDouble(),
          courierName: args['courierName'] ?? 'Delivery Partner',
          vehicleInfo: args['vehicleInfo'] ?? 'Bike',
        ));
      default:
        return _build(const Scaffold(
          body: Center(child: Text('Page not found')),
        ));
    }
  }

  static MaterialPageRoute _build(Widget page) =>
      MaterialPageRoute(builder: (_) => page);
}
