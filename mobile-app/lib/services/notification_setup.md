# Firebase FCM Notification Setup Guide

## 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project: **NearKart**
3. Enable Google Analytics (optional)

## 2. Add Android App
1. Click "Add app" → Android
2. Package name: `com.nearkart.app` (must match `android/app/build.gradle`)
3. Download `google-services.json`
4. Place it at: `mobile-app/android/app/google-services.json`

## 3. Add iOS App (optional)
1. Click "Add app" → iOS
2. Bundle ID: `com.nearkart.app`
3. Download `GoogleService-Info.plist`
4. Place it at: `mobile-app/ios/Runner/GoogleService-Info.plist`
5. In Xcode: enable Push Notifications + Background Modes (Remote notifications)

## 4. Update Android build files

### `android/build.gradle` — add to `dependencies`:
```gradle
classpath 'com.google.gms:google-services:4.4.1'
```

### `android/app/build.gradle` — add at the very bottom:
```gradle
apply plugin: 'com.google.gms.google-services'
```

### `android/app/src/main/AndroidManifest.xml` — add inside `<application>`:
```xml
<!-- FCM default notification channel -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_channel_id"
    android:value="nearkart_delivery" />

<!-- FCM default notification icon -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@mipmap/ic_launcher" />

<!-- FCM default notification color -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_color"
    android:resource="@color/purple_500" />
```

## 5. Backend: Send notifications
Your Spring Boot backend should send FCM messages via the **HTTP v1 API**.

### Notification payload structure:
```json
{
  "message": {
    "token": "<device_fcm_token>",
    "notification": {
      "title": "New Delivery Request!",
      "body": "Order #NK1234 — Fresh Mart, 1.8 km away. Earn ₹45"
    },
    "data": {
      "type": "NEW_ASSIGNMENT",
      "assignmentId": "<id>",
      "orderId": "<id>",
      "shopName": "Fresh Mart"
    },
    "android": {
      "priority": "high",
      "notification": {
        "channel_id": "nearkart_delivery",
        "sound": "default",
        "default_vibrate_timings": true
      }
    }
  }
}
```

### Supported `data.type` values and where they navigate:
| type | Navigate to |
|---|---|
| `NEW_ASSIGNMENT` | Delivery Dashboard |
| `ORDER_STATUS_UPDATE` | Order Tracking |
| `ORDER_CONFIRMED` | Order Tracking |
| `ORDER_PREPARING` | Order Tracking |
| `OUT_FOR_DELIVERY` | Live Tracking Map |
| `ORDER_DELIVERED` | Reviews Screen |
| `WALLET_CREDIT` | Wallet Screen |
| _(anything else)_ | Home Screen |

## 6. Test notifications
```bash
# Send a test FCM message from Firebase Console:
# Firebase Console → Cloud Messaging → New campaign → Test on device
# Enter your device FCM token (printed in debug console on app start)
```

## 7. Spring Boot dependency
Add to `pom.xml`:
```xml
<dependency>
  <groupId>com.google.firebase</groupId>
  <artifactId>firebase-admin</artifactId>
  <version>9.2.0</version>
</dependency>
```
