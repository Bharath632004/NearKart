# Section 2: Overall Description

> **Document:** NearKart SRS v1.0  
> **Section:** 2 of 20

---

## 2.1 Product Perspective

NearKart is a new, standalone hyperlocal commerce platform. It is not a module or extension of any existing system. It is designed as a **microservices-based, cloud-native application** deployable on AWS, with future compatibility for GCP and Azure.

The system interfaces with the following external services:

| External System | Purpose | Protocol |
|---|---|---|
| Google Maps Platform | Geocoding, routing, live tracking | REST / WebSocket |
| Razorpay / Stripe | Payment processing | REST / Webhook |
| Firebase Cloud Messaging (FCM) | Push notifications | REST |
| Twilio / MSG91 | SMS OTP delivery | REST |
| SendGrid / AWS SES | Email notifications | REST / SMTP |
| AWS S3 | Media and document storage | REST |
| AWS RDS (PostgreSQL) | Primary data store | JDBC |
| AWS ElastiCache (Redis) | Caching, session management | TCP |
| Apache Kafka | Asynchronous event streaming | TCP |
| Docker / Kubernetes (EKS) | Container orchestration | Internal |

### System Context Diagram

```
+--------------------+       +----------------------+
|   Customer App     |       |  Merchant Portal     |
|   (Flutter)        |       |  (React.js)          |
+--------+-----------+       +----------+-----------+
         |                              |
         +----------+    +--------------+
                    |    |
              +-----v----v------+
              |  API Gateway    |
              |  (Spring Boot)  |
              +-----+-----------+
                    |
       +------------+-------------+
       |            |             |
  +----v---+   +----v---+   +-----v----+
  | Auth   |   | Order  |   | Delivery |
  | Service|   | Service|   | Service  |
  +----+---+   +----+---+   +-----+----+
       |            |             |
  +----v------------v-------------v----+
  |          PostgreSQL (RDS)          |
  |          Redis (ElastiCache)       |
  |          Kafka (MSK)               |
  +------------------------------------+
              |
       +------v------+
       |  AWS S3      |
       |  FCM / SMS   |
       |  Google Maps |
       +-------------+
```

## 2.2 Product Functions (High-Level)

NearKart provides the following major functional areas:

### 2.2.1 Customer Functions
- Account creation, login via OTP/OAuth
- Location detection and address management
- Hyperlocal merchant and product discovery
- Product search, filter, and browse by category
- Cart management and coupon application
- Checkout with multiple payment options
- Real-time order tracking with live GPS
- Order history, reorder, and returns
- Product ratings and reviews
- Referral program and wallet top-up

### 2.2.2 Merchant Functions
- Business registration and KYC onboarding
- Store profile and hours management
- Product catalog creation with images, variants, pricing
- Inventory tracking and low-stock alerts
- Incoming order dashboard and acceptance workflow
- Order preparation and handoff to delivery
- Revenue analytics and payout management
- Promotional offers and discount management
- Customer review responses

### 2.2.3 Delivery Partner Functions
- Driver registration and document verification
- Availability toggle (online / offline)
- Real-time order assignment notifications
- Navigation with turn-by-turn directions
- Proof of delivery (photo / OTP)
- Earnings dashboard and payout history
- Performance metrics and ratings

### 2.2.4 Admin Functions
- Platform-wide dashboard with KPIs
- User, merchant, and delivery partner management
- Order monitoring and dispute resolution
- Category and banner management
- Commission and payout configuration
- Fraud detection and account suspension
- System health monitoring
- Report generation and data export

## 2.3 User Classes and Characteristics

| User Class | Description | Technical Proficiency | Frequency of Use |
|---|---|---|---|
| Customer | End consumer ordering products | Low — basic smartphone usage | Daily to weekly |
| Merchant | Local shop owner managing store | Medium — computer literate | Multiple times daily |
| Delivery Partner | Gig-economy driver | Low-Medium — smartphone only | Daily |
| Admin | NearKart operations staff | High — technical | Continuous |
| Super Admin | CTO / Founder | High — technical | On demand |

## 2.4 Operating Environment

### Customer App
- Platform: Android 8.0+ / iOS 13.0+
- Device: Smartphones with GPS, camera, and internet
- Network: 3G minimum, 4G/5G preferred
- Storage: Minimum 100MB app size

### Merchant Portal
- Platform: Web browser (Chrome 90+, Firefox 88+, Edge 90+, Safari 14+)
- Device: Desktop or laptop with stable internet
- Screen: Minimum 1280 x 768 resolution

### Delivery App
- Platform: Android 8.0+
- Device: Smartphone with GPS, camera
- Network: 4G preferred (GPS dependency)

### Admin Dashboard
- Platform: Web browser (Chrome 90+)
- Device: Desktop with minimum 1920 x 1080 resolution

### Backend (Server)
- Cloud: AWS (Primary), Multi-region
- OS: Ubuntu 22.04 LTS (Docker containers)
- Runtime: Java 21 (Spring Boot 3.x)
- Database: PostgreSQL 15+
- Cache: Redis 7+
- Queue: Apache Kafka 3.5+

## 2.5 Design and Implementation Constraints

1. **Language Constraint** — Backend must be implemented in Java (Spring Boot). Frontend in React.js and Flutter.
2. **Database Constraint** — Primary database must be PostgreSQL. Redis is mandatory for session and caching.
3. **API Constraint** — All external-facing APIs must be RESTful with JSON payloads.
4. **Security Constraint** — All APIs must use HTTPS. Authentication via JWT tokens. OTP via SMS for Indian phone numbers.
5. **Payment Constraint** — Payment gateway must be Razorpay (India) for Phase 1. Stripe for international in Phase 2.
6. **Map Constraint** — Google Maps Platform APIs for geocoding, routing, and live tracking.
7. **Notification Constraint** — Push notifications via FCM. SMS via Twilio or MSG91.
8. **Storage Constraint** — All media (product images, documents) stored in AWS S3.
9. **Compliance** — Must comply with Indian IT Act 2000, Digital Personal Data Protection Act 2023 (DPDP), and PCI-DSS for payment data.
10. **Localization** — Phase 1 supports English and Telugu. Hindi and other regional languages in Phase 2.

## 2.6 Assumptions and Dependencies

### Assumptions
1. All merchants have smartphones and basic internet connectivity.
2. Delivery partners own their own vehicles and smartphones.
3. Target geography is urban and semi-urban areas of Andhra Pradesh and Telangana (Phase 1).
4. Customers have access to Android 8.0+ devices.
5. Payment infrastructure (Razorpay) is available and functional in the target geography.
6. Google Maps API will be available within budget constraints.
7. AWS services will maintain 99.9% uptime SLA.

### Dependencies
1. Razorpay merchant account must be active before payment module go-live.
2. Firebase project must be configured before push notifications can be tested.
3. Google Maps API key with Places, Directions, and Geocoding APIs enabled.
4. Domain name and SSL certificate must be provisioned before production deployment.
5. AWS account with required service limits raised.

## 2.7 User Stories Overview

### Epic 1: Customer Journey
```
As a customer,
I want to open the NearKart app, enter my location,
and see nearby stores selling what I need,
so that I can order and receive delivery within 2 hours.
```

### Epic 2: Merchant Fulfilment
```
As a merchant,
I want to manage my product catalog and accept orders
from the NearKart platform,
so that I can earn additional revenue from online customers.
```

### Epic 3: Delivery Execution
```
As a delivery partner,
I want to receive order assignments on my phone,
navigate to the merchant and customer,
and confirm delivery with proof,
so that I can earn money per delivery.
```

### Epic 4: Platform Governance
```
As an admin,
I want full visibility and control over all platform activity,
so that I can resolve disputes, prevent fraud, and ensure quality.
```

---

*End of Section 2*

> Previous: [Section 1 — Introduction](section-01-introduction.md)  
> Next: [Section 3 — Customer Module](section-03-customer-module.md)
