# NearKart Mobile App — Setup Guide

## Prerequisites
- Flutter 3.x installed
- Android Studio / VS Code
- Firebase project
- Google Cloud project

---

## Step 1: Google Maps API Key

### Get your key
1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Click **Create Credentials → API Key**
3. Enable these APIs in your project:
   - Maps SDK for Android
   - Maps SDK for iOS
   - Directions API
   - Places API
   - Geocoding API
4. (Recommended) Restrict the key to your app's package name `com.nearkart.app`

### Android
Paste your key in:
```
android/app/src/main/res/values/strings.xml
```
Replace `YOUR_GOOGLE_MAPS_API_KEY` with your actual key.

### iOS
Paste your key in:
```
ios/Runner/AppDelegate.swift
```
Replace `YOUR_GOOGLE_MAPS_IOS_API_KEY` with your actual key.

---

## Step 2: Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project (or use existing)
3. Add Android app:
   - Package name: `com.nearkart.app`
   - Download `google-services.json`
   - Replace `android/app/google-services.json` with the downloaded file
4. Add iOS app:
   - Bundle ID: `com.nearkart.app`
   - Download `GoogleService-Info.plist`
   - Place it inside `ios/Runner/`

---

## Step 3: Razorpay Key

Open `lib/core/constants/app_constants.dart` and replace:
```dart
static const String razorpayKey = 'YOUR_RAZORPAY_KEY';
```
With your Razorpay test key from [Razorpay Dashboard](https://dashboard.razorpay.com).

- Test key starts with `rzp_test_`
- Live key starts with `rzp_live_`

---

## Step 4: Run the app

```bash
cd mobile-app
flutter pub get
flutter run
```

### Run on specific device
```bash
flutter devices                  # list connected devices
flutter run -d <device_id>       # run on specific device
flutter run --release            # production build
```

### Build APK
```bash
flutter build apk --release
# Output: build/app/outputs/flutter-apk/app-release.apk
```

### Build App Bundle (for Play Store)
```bash
flutter build appbundle --release
```

---

## Step 5: Replace Placeholder Data

These files contain `TODO` markers to replace with real backend calls:

| File | What to replace |
|------|----------------|
| `lib/providers/auth_provider.dart` | OTP send/verify with your backend API |
| `lib/providers/order_provider.dart` | Fetch/place orders with your backend API |
| `lib/services/api_service.dart` | Set `baseUrl` to your actual backend URL |
| `lib/screens/customer/home_screen.dart` | Replace mock shops with `ApiService.getNearbyShops()` |
| `lib/screens/common/live_tracking_screen.dart` | Replace simulated movement with real GPS from backend/WebSocket |
| `lib/core/constants/app_constants.dart` | Set `baseUrl`, `razorpayKey` |

---

## Package Structure

```
mobile-app/
├── android/                  # Android native config
│   ├── app/
│   │   ├── build.gradle
│   │   ├── google-services.json   ← Replace with real file
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/nearkart/app/MainActivity.kt
│   │       └── res/values/
│   │           └── strings.xml    ← Add Google Maps key here
│   ├── build.gradle
│   └── settings.gradle
├── ios/
│   └── Runner/
│       ├── AppDelegate.swift      ← Add iOS Maps key here
│       └── Info.plist             ← Permissions configured
├── lib/
│   ├── main.dart
│   ├── app.dart
│   ├── core/
│   │   ├── constants/app_constants.dart  ← Add Razorpay key here
│   │   ├── routes/app_routes.dart
│   │   └── theme/app_theme.dart
│   ├── models/
│   ├── providers/
│   ├── screens/
│   │   ├── auth/
│   │   ├── common/live_tracking_screen.dart
│   │   ├── customer/
│   │   └── delivery/
│   └── services/
├── pubspec.yaml
└── SETUP.md
```
