import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:provider/provider.dart';
import 'services/notification_service.dart';
import 'providers/auth_provider.dart';
import 'providers/cart_provider.dart';
import 'providers/order_provider.dart';
import 'providers/wallet_provider.dart';
import 'providers/shop_provider.dart';
import 'providers/review_provider.dart';
import 'app.dart';

/// Must be top-level for Firebase background isolate
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await NotificationService.showLocalNotification(message);
}

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Firebase.initializeApp();

  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

  final authProvider = AuthProvider();
  await authProvider.initAuth();

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider<AuthProvider>(value: authProvider),
        ChangeNotifierProvider<CartProvider>(create: (_) => CartProvider()),
        ChangeNotifierProvider<OrderProvider>(
            create: (_) => OrderProvider()),
        ChangeNotifierProvider<WalletProvider>(
            create: (_) => WalletProvider()),
        ChangeNotifierProvider<ShopProvider>(
            create: (_) => ShopProvider()),
        ChangeNotifierProvider<ReviewProvider>(
            create: (_) => ReviewProvider()),
      ],
      child: NearKartApp(navigatorKey: navigatorKey),
    ),
  );
}
