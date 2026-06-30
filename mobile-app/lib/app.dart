import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/routes/app_routes.dart';
import 'providers/auth_provider.dart';
import 'services/notification_service.dart';

class NearKartApp extends StatefulWidget {
  final GlobalKey<NavigatorState> navigatorKey;
  const NearKartApp({super.key, required this.navigatorKey});

  @override
  State<NearKartApp> createState() => _NearKartAppState();
}

class _NearKartAppState extends State<NearKartApp> {
  @override
  void initState() {
    super.initState();
    // Init notifications after first frame so context is ready
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final auth = context.read<AuthProvider>();
      await NotificationService.init(
        navKey: widget.navigatorKey,
        userId: auth.userId,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'NearKart',
      navigatorKey: widget.navigatorKey,
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.deepPurple,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6C63FF),
        ),
        useMaterial3: true,
      ),
      onGenerateRoute: AppRoutes.generateRoute,
      initialRoute: AppRoutes.splash,
    );
  }
}
