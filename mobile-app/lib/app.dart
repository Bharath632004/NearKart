import 'package:flutter/material.dart';
import 'core/theme/app_theme.dart';
import 'screens/splash_screen.dart';

class NearKartApp extends StatelessWidget {
  const NearKartApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'NearKart',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const SplashScreen(),
    );
  }
}
