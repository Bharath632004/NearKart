import 'dart:convert';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter/material.dart';
import 'api_service.dart';

/// Background message handler — must be a top-level function
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(
    RemoteMessage message) async {
  // Firebase is already initialised before this runs
  await NotificationService.showLocalNotification(message);
}

class NotificationService {
  NotificationService._();

  static final FirebaseMessaging _fcm = FirebaseMessaging.instance;
  static final FlutterLocalNotificationsPlugin _local =
      FlutterLocalNotificationsPlugin();

  /// High-importance Android channel for delivery alerts
  static const AndroidNotificationChannel _deliveryChannel =
      AndroidNotificationChannel(
    'nearkart_delivery',
    'Delivery Alerts',
    description: 'New delivery assignment notifications',
    importance: Importance.max,
    playSound: true,
    enableVibration: true,
  );

  /// General channel for order updates
  static const AndroidNotificationChannel _orderChannel =
      AndroidNotificationChannel(
    'nearkart_orders',
    'Order Updates',
    description: 'Order status change notifications',
    importance: Importance.high,
    playSound: true,
  );

  // Navigtor key — set from main.dart so we can navigate on tap
  static GlobalKey<NavigatorState>? navigatorKey;

  // ────────────────────────────────────────
  /// Call once from main() after Firebase.initializeApp()
  static Future<void> init({
    required GlobalKey<NavigatorState> navKey,
    String? userId,
  }) async {
    navigatorKey = navKey;

    // 1. Request permission (iOS + Android 13+)
    await _fcm.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      provisional: false,
    );

    // 2. Create Android notification channels
    final androidPlugin = _local
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>();
    await androidPlugin?.createNotificationChannel(_deliveryChannel);
    await androidPlugin?.createNotificationChannel(_orderChannel);

    // 3. Initialise flutter_local_notifications
    const initSettings = InitializationSettings(
      android: AndroidInitializationSettings('@mipmap/ic_launcher'),
      iOS: DarwinInitializationSettings(
        requestAlertPermission: false,
        requestBadgePermission: false,
        requestSoundPermission: false,
      ),
    );
    await _local.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onNotificationTap,
    );

    // 4. Register background handler
    FirebaseMessaging.onBackgroundMessage(
        firebaseMessagingBackgroundHandler);

    // 5. Foreground messages → show local notification
    FirebaseMessaging.onMessage.listen((message) {
      showLocalNotification(message);
    });

    // 6. Notification tap when app is in background (not terminated)
    FirebaseMessaging.onMessageOpenedApp.listen(_handleMessageTap);

    // 7. Notification tap from terminated state
    final initial = await _fcm.getInitialMessage();
    if (initial != null) _handleMessageTap(initial);

    // 8. Get & register FCM token with backend
    await _registerToken(userId);

    // 9. Refresh token listener
    _fcm.onTokenRefresh.listen((token) async {
      if (userId != null && userId.isNotEmpty) {
        try {
          await ApiService.registerFcmToken(userId, token);
        } catch (_) {}
      }
    });
  }

  // ────────────────────────────────────────
  static Future<void> _registerToken(String? userId) async {
    try {
      final token = await _fcm.getToken();
      if (token != null && userId != null && userId.isNotEmpty) {
        await ApiService.registerFcmToken(userId, token);
      }
    } catch (_) {}
  }

  /// Re-register token after login (call from AuthProvider)
  static Future<void> registerForUser(String userId) async {
    await _registerToken(userId);
  }

  /// Delete token on logout
  static Future<void> deleteToken() async {
    try {
      await _fcm.deleteToken();
    } catch (_) {}
  }

  // ────────────────────────────────────────
  static Future<void> showLocalNotification(RemoteMessage message) async {
    final notification = message.notification;
    final data = message.data;

    // Choose channel based on notification type
    final type = data['type'] as String? ?? 'general';
    final channelId = type == 'NEW_ASSIGNMENT'
        ? _deliveryChannel.id
        : _orderChannel.id;
    final channelName = type == 'NEW_ASSIGNMENT'
        ? _deliveryChannel.name
        : _orderChannel.name;

    final title = notification?.title ?? data['title'] ?? 'NearKart';
    final body = notification?.body ?? data['body'] ?? '';

    await _local.show(
      message.hashCode,
      title,
      body,
      NotificationDetails(
        android: AndroidNotificationDetails(
          channelId,
          channelName,
          importance: Importance.max,
          priority: Priority.high,
          icon: '@mipmap/ic_launcher',
          largeIcon:
              const DrawableResourceAndroidBitmap('@mipmap/ic_launcher'),
          styleInformation: BigTextStyleInformation(body),
          ticker: title,
        ),
        iOS: const DarwinNotificationDetails(
          presentAlert: true,
          presentBadge: true,
          presentSound: true,
        ),
      ),
      payload: jsonEncode(data),
    );
  }

  // ────────────────────────────────────────
  static void _onNotificationTap(NotificationResponse response) {
    final payload = response.payload;
    if (payload == null || payload.isEmpty) return;
    try {
      final data = jsonDecode(payload) as Map<String, dynamic>;
      _navigate(data);
    } catch (_) {}
  }

  static void _handleMessageTap(RemoteMessage message) {
    _navigate(message.data);
  }

  /// Smart navigation based on notification type
  static void _navigate(Map<String, dynamic> data) {
    final nav = navigatorKey?.currentState;
    if (nav == null) return;

    final type = data['type'] as String? ?? '';
    final orderId = data['orderId'] as String? ?? '';
    final assignmentId = data['assignmentId'] as String? ?? '';
    final shopId = data['shopId'] as String? ?? '';
    final shopName = data['shopName'] as String? ?? 'Shop';

    switch (type) {
      case 'NEW_ASSIGNMENT':
        // Delivery partner — go to delivery dashboard
        nav.pushNamedAndRemoveUntil(
            '/delivery-home', (route) => false);
        break;
      case 'ORDER_STATUS_UPDATE':
      case 'ORDER_CONFIRMED':
      case 'ORDER_PREPARING':
        nav.pushNamed('/order-tracking',
            arguments: {'orderId': orderId});
        break;
      case 'OUT_FOR_DELIVERY':
        nav.pushNamed('/live-tracking', arguments: {
          'orderId': orderId,
          'shopName': shopName,
        });
        break;
      case 'ORDER_DELIVERED':
        nav.pushNamed('/reviews', arguments: {
          'shopId': shopId,
          'shopName': shopName,
          'orderId': orderId,
        });
        break;
      case 'WALLET_CREDIT':
        nav.pushNamed('/wallet');
        break;
      default:
        // Generic — just open home
        nav.pushNamedAndRemoveUntil('/home', (route) => false);
    }
  }
}
