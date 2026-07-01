class AppConstants {
  // Change to your backend IP when testing on a physical device
  static const String baseUrl = 'http://10.0.2.2:8080';

  static const String appName = 'NearKart';
  static const double defaultRadiusKm = 5.0;
  static const int otpLength = 6;
  static const Duration apiTimeout = Duration(seconds: 15);

  // Razorpay — replace with your actual key
  static const String razorpayKeyId = 'rzp_test_YOUR_KEY_HERE';
}
