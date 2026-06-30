import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:provider/provider.dart';
import 'services/notification_service.dart';
import 'providers/auth_provider.dart';
import 'app.dart';

/// Must be top-level for Firebase background isolate
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(message) async {
  await NotificationService.showLocalNotification(message);
}

final GlobalKey<NavigatorState> navigatorKey =
    GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialise Firebase
  await Firebase.initializeApp();

  // Register background FCM handler before runApp
  // (also set inside NotificationService.init but safe to call here too)
  // FirebaseMessaging.onBackgroundMessage is idempotent

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
      ],
      child: NearKartApp(navigatorKey: navigatorKey),
    ),
  );
}
