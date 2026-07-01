import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/routes/app_routes.dart';
import 'core/theme/app_theme.dart';
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
      theme: AppTheme.light,
      onGenerateRoute: AppRoutes.generateRoute,
      initialRoute: AppRoutes.splash,
    );
  }
}
