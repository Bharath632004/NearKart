import 'package:flutter/material.dart';
import 'core/theme/app_theme.dart';
import 'core/routes/app_routes.dart';

class NearKartApp extends StatelessWidget {
  const NearKartApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'NearKart',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      initialRoute: AppRoutes.splash,
      onGenerateRoute: AppRoutes.generateRoute,
    );
  }
}
