import 'package:flutter/material.dart';
import '../../screens/auth/splash_screen.dart';
import '../../screens/auth/login_screen.dart';
import '../../screens/auth/otp_screen.dart';
import '../../screens/customer/home_screen.dart';
import '../../screens/customer/shop_detail_screen.dart';
import '../../screens/customer/cart_screen.dart';
import '../../screens/customer/order_tracking_screen.dart';
import '../../screens/customer/order_history_screen.dart';
import '../../screens/customer/wallet_screen.dart';
import '../../screens/customer/reviews_screen.dart';
import '../../screens/customer/profile_screen.dart';
import '../../screens/delivery/delivery_home_screen.dart';
import '../../screens/delivery/active_order_screen.dart';
import '../../screens/common/live_tracking_screen.dart';

class AppRoutes {
  static const String splash = '/';
  static const String login = '/login';
  static const String otp = '/otp';
  static const String home = '/home';
  static const String shopDetail = '/shop-detail';
  static const String cart = '/cart';
  static const String orderTracking = '/order-tracking';
  static const String orderHistory = '/order-history';
  static const String wallet = '/wallet';
  static const String reviews = '/reviews';
  static const String profile = '/profile';
  static const String deliveryHome = '/delivery-home';
  static const String activeOrder = '/active-order';
  static const String liveTracking = '/live-tracking';

  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case splash:
        return MaterialPageRoute(builder: (_) => const SplashScreen());
      case login:
        return MaterialPageRoute(builder: (_) => const LoginScreen());
      case otp:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
            builder: (_) => OtpScreen(phone: args?['phone'] ?? ''));
      case home:
        return MaterialPageRoute(builder: (_) => const HomeScreen());
      case shopDetail:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
            builder: (_) => ShopDetailScreen(
                shopId: args?['shopId'] ?? '',
                shopName: args?['shopName'] ?? ''));
      case cart:
        return MaterialPageRoute(builder: (_) => const CartScreen());
      case orderTracking:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
            builder: (_) =>
                OrderTrackingScreen(orderId: args?['orderId'] ?? ''));
      case orderHistory:
        return MaterialPageRoute(builder: (_) => const OrderHistoryScreen());
      case wallet:
        return MaterialPageRoute(builder: (_) => const WalletScreen());
      case reviews:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
            builder: (_) => ReviewsScreen(
                  shopId: args?['shopId'] ?? '',
                  shopName: args?['shopName'] ?? '',
                  completedOrderId: args?['orderId'],
                ));
      case profile:
        return MaterialPageRoute(builder: (_) => const ProfileScreen());
      case deliveryHome:
        return MaterialPageRoute(builder: (_) => const DeliveryHomeScreen());
      case activeOrder:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
            builder: (_) =>
                ActiveOrderScreen(assignmentId: args?['assignmentId'] ?? ''));
      case liveTracking:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
            builder: (_) => LiveTrackingScreen(
                  orderId: args?['orderId'] ?? '',
                  shopName: args?['shopName'] ?? 'Shop',
                ));
      default:
        return MaterialPageRoute(
          builder: (_) => Scaffold(
            body: Center(
                child: Text('No route defined for \${settings.name}')),
          ),
        );
    }
  }
}
