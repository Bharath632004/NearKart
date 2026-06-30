# 🛒 NearKart — Hyperlocal Delivery Platform

> **Connect customers with nearby shops for same-day, same-hour delivery.**
> NearKart is a full-stack hyperlocal commerce platform built with Flutter (mobile), Spring Boot (backend), React (web/admin), Firebase (notifications), and PostgreSQL.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Backend Setup](#backend-setup)
    - [Mobile App Setup](#mobile-app-setup)
    - [Firebase FCM Setup](#firebase-fcm-setup)
- [API Reference](#api-reference)
- [Mobile Screen Map](#mobile-screen-map)
- [Push Notification Types](#push-notification-types)
- [Environment Variables](#environment-variables)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

NearKart is a **three-sided marketplace**:

| Role | What they do |
|---|---|
| 🛍️ **Customer** | Browse nearby shops, add to cart, pay, track delivery in real time |
| 🏪 **Shop Owner** | List products, manage inventory, receive & process orders |
| 🛵 **Delivery Partner** | Accept assignments, pick up from shop, deliver to customer |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│                                                                 │
│   Flutter Mobile App          React Web / Admin Panel           │
│   (Customer + Delivery)       (Shop Owner + Admin)              │
└──────────────────┬──────────────────────┬───────────────────────┘
                   │  REST / HTTPS         │  REST / HTTPS
                   ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BACKEND LAYER                              │
│                                                                 │
│            Spring Boot REST API  (Port 8080)                    │
│                                                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────────┐  │
│  │   Auth   │ │  Orders  │ │ Delivery │ │   Notifications   │  │
│  │  Service │ │  Service │ │  Service │ │  (FCM via Admin)  │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────────┐  │
│  │  Shops   │ │ Products │ │ Payments │ │      Wallet       │  │
│  │  Service │ │  Service │ │(Razorpay)│ │      Service      │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────────┘  │
└──────────────────┬──────────────────────────────────────────────┘
                   │
       ┌───────────┴────────────┐
       ▼                        ▼
┌─────────────┐        ┌────────────────┐
│  PostgreSQL │        │   Firebase     │
│  Database   │        │  (FCM + Auth)  │
└─────────────┘        └────────────────┘
```

---

## Project Structure

```
NearKart/
├── mobile-app/               # Flutter app (Customer + Delivery Partner)
│   ├── lib/
│   │   ├── main.dart         # Entry point, Firebase init
│   │   ├── app.dart          # MaterialApp, NavigatorKey, theme
│   │   ├── core/
│   │   │   ├── constants/    # API base URL, app constants
│   │   │   └── routes/       # Named route definitions
│   │   ├── models/           # Dart data models (Order, Shop, Product …)
│   │   ├── providers/        # Auth state (Provider)
│   │   ├── services/
│   │   │   ├── api_service.dart          # All HTTP calls
│   │   │   └── notification_service.dart # FCM + local notifications
│   │   └── screens/
│   │       ├── auth/         # Splash, Login, OTP
│   │       ├── customer/     # Home, Shop, Cart, Orders, Wallet, Reviews, Profile
│   │       ├── delivery/     # Dashboard, Active Order, Earnings, Profile
│   │       └── common/       # Live Tracking (Google Maps)
│   ├── android/              # Android native config
│   ├── ios/                  # iOS native config
│   └── pubspec.yaml
│
├── backend/                  # Spring Boot REST API
│   └── src/main/java/com/nearkart/
│       ├── auth/             # JWT, OTP, refresh token
│       ├── users/            # User profiles
│       ├── shops/            # Shop CRUD + geolocation search
│       ├── products/         # Product catalogue
│       ├── orders/           # Order lifecycle
│       ├── delivery/         # Assignment management
│       ├── payments/         # Razorpay integration
│       ├── wallet/           # Wallet & transactions
│       ├── reviews/          # Shop reviews & ratings
│       └── notifications/    # FCM token registry & push sending
│
├── frontend-web/             # React customer web app
├── admin-panel/              # React admin dashboard
├── database/                 # SQL schema & migrations
├── devops/                   # Docker, CI/CD configs
├── infra/                    # Terraform / cloud infra
├── ml-ai/                    # Recommendation & ETA models
└── docs/                     # Architecture diagrams & API specs
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Mobile App** | Flutter 3.x, Dart, Provider |
| **Backend** | Spring Boot 3.x, Java 17, Spring Security + JWT |
| **Database** | PostgreSQL 15 |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Payments** | Razorpay |
| **Maps** | Google Maps Flutter SDK |
| **Location** | Geolocator |
| **Web/Admin** | React 18, TypeScript |
| **Storage** | flutter_secure_storage (JWT), SharedPreferences |
| **DevOps** | Docker, GitHub Actions |

---

## Getting Started

### Prerequisites

- **Flutter** ≥ 3.0.0 ([install](https://docs.flutter.dev/get-started/install))
- **Java** 17+ and **Maven** 3.8+
- **PostgreSQL** 15
- **Firebase project** (for FCM)
- **Razorpay account** (for payments)
- **Google Maps API key**

---

### Backend Setup

```bash
# 1. Clone the repo
git clone https://github.com/Bharath632004/NearKart.git
cd NearKart/backend

# 2. Create PostgreSQL database
psql -U postgres -c "CREATE DATABASE nearkart;"

# 3. Copy and fill environment variables
cp .env.example .env
# Edit .env with your DB credentials, JWT secret, Razorpay keys, Firebase service account

# 4. Run
mvn spring-boot:run
# API available at http://localhost:8080
```

---

### Mobile App Setup

```bash
cd NearKart/mobile-app

# 1. Install dependencies
flutter pub get

# 2. Set your backend base URL
# Edit: lib/core/constants/app_constants.dart
#   Android emulator  → http://10.0.2.2:8080
#   Physical device   → http://<your-machine-IP>:8080
#   Production        → https://api.nearkart.in

# 3. Add your Google Maps API key
# Android: android/app/src/main/AndroidManifest.xml
#   <meta-data android:name="com.google.android.geo.API_KEY"
#              android:value="YOUR_KEY_HERE"/>
# iOS: ios/Runner/AppDelegate.swift
#   GMSServices.provideAPIKey("YOUR_KEY_HERE")

# 4. Run
flutter run
```

---

### Firebase FCM Setup

```bash
# 1. Create a Firebase project at https://console.firebase.google.com/
# 2. Add Android app with package: com.nearkart.app
# 3. Download google-services.json → place at mobile-app/android/app/

# 4. android/build.gradle (project-level) → add inside plugins {} block:
#    id 'com.google.gms.google-services' version '4.4.1' apply false

# 5. android/app/build.gradle (app-level) → add inside plugins {} block:
#    id 'com.google.gms.google-services'

# 6. AndroidManifest.xml → add inside <application>:
#    <meta-data android:name="com.google.firebase.messaging.default_notification_channel_id"
#               android:value="nearkart_delivery" />

# Full guide: mobile-app/lib/services/notification_setup.md
```

---

## API Reference

Base URL: `http://localhost:8080`

> All endpoints are versioned under `/api/v1/`. Examples below show paths relative to the base URL.

### 🔐 Auth — `/api/v1/auth`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Register new user |
| POST | `/login` | Login with phone + password |
| POST | `/send-otp` | Send OTP to phone |
| POST | `/verify-otp` | Verify OTP → returns JWT |
| POST | `/refresh` | Refresh access token |
| POST | `/logout` | Invalidate token |

### 👤 Users — `/api/v1/users`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/{userId}` | Get user profile |
| PUT | `/{userId}` | Update profile |

### 🏪 Shops — `/api/v1/shops`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/nearby?lat=&lng=&radius=` | Get shops near location |
| GET | `/{shopId}` | Get shop details |
| POST | `/` | Create shop (owner) |
| PUT | `/{shopId}` | Update shop |

### 📦 Products — `/api/v1/products`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/shop/{shopId}` | Get products by shop |
| GET | `/{productId}` | Get product details |
| POST | `/` | Create product |
| PUT | `/{productId}` | Update product |
| DELETE | `/{productId}` | Delete product |

### 🛒 Cart — `/api/v1/cart`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/{userId}` | Get cart |
| POST | `/{userId}/add` | Add item to cart |
| DELETE | `/{userId}/remove/{productId}` | Remove item |
| DELETE | `/{userId}/clear` | Clear cart |

### 📋 Orders — `/api/v1/orders`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Place order |
| GET | `/user/{userId}` | Get my orders |
| GET | `/{orderId}` | Get order details |
| PATCH | `/{orderId}/cancel` | Cancel order |

### 🛵 Delivery — `/api/v1/delivery`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/tracking/{orderId}` | Get live order tracking |
| POST | `/location` | Update delivery location |
| GET | `/assignments/{partnerId}` | Get assignments for partner |
| PATCH | `/assignments/{id}/status` | Update assignment status |

### 💳 Payments — `/api/v1/payments`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/initiate` | Initiate Razorpay payment |
| POST | `/verify` | Verify payment signature |
| GET | `/{paymentId}` | Get payment details |

### 👛 Wallet — `/api/v1/wallet`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/{userId}` | Get wallet balance |
| GET | `/{userId}/transactions` | Get transaction history |
| POST | `/{userId}/add` | Add money |
| POST | `/{userId}/pay` | Pay with wallet |

### ⭐ Reviews — `/api/v1/shops/{shopId}/reviews`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Get shop reviews |
| POST | `/` | Submit review |
| DELETE | `/{reviewId}` | Delete review |

### 🔔 Notifications — `/api/v1/notifications`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/{userId}` | Get notifications |
| PATCH | `/{id}/read` | Mark as read |
| POST | `/fcm-token` | Register FCM device token |

---

## Mobile Screen Map

```
/ (Splash)
└── /login
    └── /otp
        ├── /home  [Customer]
        │   ├── /shop-detail
        │   │   └── /cart
        │   │       └── (checkout → order placed)
        │   ├── /order-history
        │   │   ├── /order-tracking   (order details)
        │   │   ├── /live-tracking    (map, active orders)
        │   │   └── /reviews          (delivered orders)
        │   ├── /wallet
        │   └── /profile
        │       ├── /order-history
        │       └── /wallet
        │
        └── /delivery-home  [Delivery Partner]
            ├── Tab: Dashboard
            │   └── Accept → /active-order
            │       └── /live-tracking
            ├── Tab: Earnings
            └── Tab: Profile
```

---

## Push Notification Types

| `data.type` | Triggered when | Navigates to |
|---|---|---|
| `NEW_ASSIGNMENT` | New delivery assigned to partner | `/delivery-home` |
| `ORDER_CONFIRMED` | Shop confirms order | `/order-tracking` |
| `ORDER_PREPARING` | Shop starts preparing | `/order-tracking` |
| `OUT_FOR_DELIVERY` | Partner picks up order | `/live-tracking` |
| `ORDER_DELIVERED` | Order delivered | `/reviews` |
| `WALLET_CREDIT` | Wallet topped up | `/wallet` |
| _(default)_ | Any other notification | `/home` |

---

## Environment Variables

### Backend (`.env`)

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/nearkart
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_256bit_secret
JWT_EXPIRY_MS=86400000

# Firebase Admin SDK
FIREBASE_SERVICE_ACCOUNT_PATH=./firebase-service-account.json

# Razorpay
RAZORPAY_KEY_ID=rzp_test_xxxx
RAZORPAY_KEY_SECRET=your_secret

# OTP (Twilio / MSG91)
OTP_PROVIDER=twilio
TWILIO_ACCOUNT_SID=ACxxxx
TWILIO_AUTH_TOKEN=your_token
TWILIO_PHONE_NUMBER=+1xxxxxxxxxx
```

### Mobile App (`lib/core/constants/app_constants.dart`)

```dart
class AppConstants {
  // Change baseUrl before building for production!
  static const String baseUrl = 'http://10.0.2.2:8080'; // Android emulator default
  static const String googleMapsApiKey = 'YOUR_GOOGLE_MAPS_KEY';
  static const String razorpayKeyId = 'rzp_test_xxxx';
}
```

---

## Contributing

> TODO: Add [CONTRIBUTING.md](CONTRIBUTING.md) with branch naming, code style, and PR checklist.

1. Fork the repo
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Commit with conventional commits: `git commit -m "feat: add xyz"`
4. Push and open a PR against `main`

---

## License

> TODO: Add [LICENSE](LICENSE) file (MIT recommended).

This project is licensed under the MIT License.

---

<div align="center">
  Built with ❤️ by <a href="https://github.com/Bharath632004">Bharath</a>
</div>
