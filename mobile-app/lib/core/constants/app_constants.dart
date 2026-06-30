class AppConstants {
  // ─── Base URL ──────────────────────────────────────────────────────
  // Replace with your deployed backend URL.
  // Local development examples:
  //   Android emulator  → http://10.0.2.2:8080
  //   iOS simulator     → http://localhost:8080
  //   Physical device   → http://<your-machine-IP>:8080
  // Production example  → https://api.nearkart.in
  static const String baseUrl = 'http://10.0.2.2:8080';

  // ─── Razorpay ──────────────────────────────────────────────────────
  // Replace with your key from https://dashboard.razorpay.com
  // Test key  → rzp_test_XXXXXXXXXXXXXXXX
  // Live key  → rzp_live_XXXXXXXXXXXXXXXX
  static const String razorpayKey = 'YOUR_RAZORPAY_KEY';

  // ─── App ───────────────────────────────────────────────────────────
  static const String appName = 'NearKart';
  static const String appVersion = '1.0.0';

  // ─── Timeouts ──────────────────────────────────────────────────────
  static const int connectTimeoutSeconds = 30;
  static const int receiveTimeoutSeconds = 60;

  // ─── Pagination ────────────────────────────────────────────────────
  static const int defaultPageSize = 20;

  // ─── Map defaults ──────────────────────────────────────────────────
  static const double defaultSearchRadiusKm = 5.0;
  static const double defaultMapZoom = 14.0;
}
