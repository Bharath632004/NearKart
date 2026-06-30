# Section 3: Functional Requirements — Customer Module

> **Document:** NearKart SRS v1.0  
> **Section:** 3 of 20  
> **Module:** Customer

---

## 3.0 Module Overview

The Customer Module covers all interactions performed by end consumers on the NearKart platform. This includes account management, product discovery, cart and checkout, order management, tracking, returns, reviews, and the loyalty/referral program.

**Primary Actor:** Customer (registered or guest)  
**Platforms:** Flutter Android App, Flutter iOS App  
**Authentication:** OTP-based phone login + optional Google/Apple OAuth

---

## 3.1 Customer Registration & Onboarding

### FR-CUST-001 — Phone Number Registration

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-001 |
| **Title** | Phone Number Registration via OTP |
| **Priority** | Must Have |
| **Module** | Customer > Registration |

**Description:**  
A new user must be able to register on NearKart using their mobile phone number. The system shall send a 6-digit OTP via SMS to the entered phone number. Upon successful OTP verification, a new customer account shall be created.

**Preconditions:**
- User has opened the NearKart app for the first time
- User has a valid Indian mobile number (10 digits, starting with 6–9)
- SMS service (MSG91 / Twilio) is operational

**Flow:**
```
1. User opens app → Sees Welcome Screen
2. User taps "Get Started" → Phone Entry Screen
3. User enters 10-digit mobile number
4. System validates format (regex: ^[6-9]\d{9}$)
5. System sends OTP via SMS (expires in 5 minutes)
6. User enters 6-digit OTP
7. System verifies OTP against stored hash
8. If valid → Redirect to Profile Setup Screen
9. If invalid → Show error, allow 3 retries
10. After 3 failed attempts → Lock OTP for 30 minutes
```

**Postconditions:**
- Customer record created in `customers` table
- JWT access token (15 min) and refresh token (30 days) issued
- Welcome notification sent via FCM

**Acceptance Criteria:**
- [ ] OTP delivered within 30 seconds
- [ ] OTP expires after 5 minutes
- [ ] Account locked after 3 incorrect OTP attempts for 30 minutes
- [ ] Duplicate phone number registration returns HTTP 409 with message "Account already exists"
- [ ] JWT token returned in response body with `accessToken`, `refreshToken`, `expiresIn` fields

---

### FR-CUST-002 — Google OAuth Login

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-002 |
| **Title** | Social Login via Google OAuth 2.0 |
| **Priority** | Should Have |
| **Module** | Customer > Registration |

**Description:**  
Customers shall be able to register or log in using their Google account via OAuth 2.0. The system shall retrieve the user's name, email, and profile picture from Google. If no account exists for that email, a new account is created automatically.

**Preconditions:**
- User has a valid Google account
- Firebase Auth / Google Sign-In SDK is configured

**Flow:**
```
1. User taps "Continue with Google"
2. Google OAuth consent screen shown
3. User grants permission
4. Google returns ID token
5. App sends ID token to backend /auth/google
6. Backend verifies token with Google
7. If new user → create account, prompt phone number linking
8. If existing user → return JWT
```

**Postconditions:**
- Customer account created or retrieved
- JWT tokens issued
- Phone number linking prompted if not linked

**Acceptance Criteria:**
- [ ] Successful Google login creates account within 3 seconds
- [ ] Existing Google-linked accounts do not create duplicates
- [ ] User's Google display name and photo pre-populate profile fields

---

### FR-CUST-003 — Profile Setup

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-003 |
| **Title** | Customer Profile Creation |
| **Priority** | Must Have |
| **Module** | Customer > Profile |

**Description:**  
After registration, the customer shall be prompted to complete their profile by entering their full name, profile picture (optional), and primary delivery address.

**Profile Fields:**

| Field | Type | Validation | Required |
|---|---|---|---|
| Full Name | String | 2–60 chars, letters only | Yes |
| Profile Photo | Image | JPEG/PNG, max 5MB | No |
| Date of Birth | Date | Must be 13+ years old | No |
| Gender | Enum | Male/Female/Other/Prefer not to say | No |
| Email Address | String | Valid email format | No |
| Referral Code | String | 8-char alphanumeric | No |

**Postconditions:**
- Profile saved to `customers` table
- If referral code provided, bonus credited after first order

**Acceptance Criteria:**
- [ ] Profile saved successfully with only name field filled
- [ ] Invalid name (numbers/special chars) rejected with message
- [ ] Profile photo resized to 200x200px on upload
- [ ] Valid referral code shows "Referral Applied" confirmation

---

## 3.2 Address Management

### FR-CUST-004 — Add Delivery Address

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-004 |
| **Title** | Add and Manage Delivery Addresses |
| **Priority** | Must Have |
| **Module** | Customer > Address |

**Description:**  
Customers shall be able to add multiple delivery addresses. Each address shall have a label (Home, Work, Other). The system shall support both manual entry and GPS-based auto-detection.

**Address Schema:**
```json
{
  "addressId": "uuid",
  "label": "Home | Work | Other",
  "flatNo": "string",
  "building": "string",
  "street": "string",
  "landmark": "string",
  "city": "string",
  "state": "string",
  "pincode": "string (6 digits)",
  "latitude": "decimal",
  "longitude": "decimal",
  "isDefault": "boolean"
}
```

**Flow (GPS Detection):**
```
1. User taps "Use Current Location"
2. App requests location permission
3. Device GPS returns lat/lng
4. System calls Google Geocoding API
5. Address fields auto-filled from response
6. User reviews and confirms / edits
7. Address saved to database
```

**Postconditions:**
- Address stored in `customer_addresses` table
- Geocoded lat/lng stored for distance calculations

**Acceptance Criteria:**
- [ ] Customer can save up to 10 addresses
- [ ] Default address auto-selected at checkout
- [ ] GPS detection completes within 5 seconds
- [ ] Pincode validation rejects invalid 6-digit codes
- [ ] Address deletion not allowed if used in active order

---

## 3.3 Product Discovery

### FR-CUST-005 — Hyperlocal Store Discovery

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-005 |
| **Title** | Location-Based Store Listing |
| **Priority** | Must Have |
| **Module** | Customer > Discovery |

**Description:**  
Based on the customer's selected delivery address, the system shall display all active merchants within the configured delivery radius (default: 10 km). Stores shall be sorted by distance by default, with options to sort by rating, delivery time, and minimum order value.

**Business Rules:**
- Only merchants with `status = ACTIVE` and `isOpen = true` are shown
- Merchants beyond delivery radius are hidden
- Out-of-stock or closed stores shown with a "Currently Unavailable" badge
- Sponsored merchants appear at top (Ad placement, Phase 2)

**Store Card Data:**
```json
{
  "merchantId": "uuid",
  "storeName": "string",
  "category": "Grocery | Pharmacy | Electronics | ...",
  "logo": "S3 URL",
  "rating": "decimal (1.0 - 5.0)",
  "totalReviews": "integer",
  "distanceKm": "decimal",
  "estimatedDeliveryMinutes": "integer",
  "minimumOrderValue": "decimal",
  "deliveryFee": "decimal",
  "isOpen": "boolean",
  "tags": ["Fresh", "Express", "Offers"]
}
```

**Acceptance Criteria:**
- [ ] Stores load within 2 seconds for up to 100 merchants
- [ ] Only stores within radius shown
- [ ] Closed stores shown with badge but not orderable
- [ ] Sort by distance/rating/delivery time works correctly
- [ ] Pull-to-refresh updates store list

---

### FR-CUST-006 — Product Search

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-006 |
| **Title** | Full-Text Product Search |
| **Priority** | Must Have |
| **Module** | Customer > Search |

**Description:**  
Customers shall be able to search for products by name, brand, or category across all nearby merchants. The search shall support fuzzy matching, auto-suggestions, and search history.

**Search Features:**
- Minimum 2 characters to trigger search
- Auto-suggestions appear after 300ms debounce
- Results grouped by merchant
- Filters: Price range, Rating, Category, Delivery time, Offers
- Sorting: Relevance, Price (low-high), Price (high-low), Rating
- Recent searches stored locally (last 10)
- Trending searches shown on empty state

**Acceptance Criteria:**
- [ ] Search results appear within 1 second
- [ ] Fuzzy search finds "tomato" when user types "tamato"
- [ ] Filter combination returns correct results
- [ ] Search with no results shows "No products found" with suggestions
- [ ] Recent searches persist across app sessions

---

### FR-CUST-007 — Category Browsing

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-007 |
| **Title** | Browse Products by Category |
| **Priority** | Must Have |
| **Module** | Customer > Discovery |

**Description:**  
The home screen shall display product categories as scrollable icon tiles. Tapping a category shows all products in that category from nearby merchants.

**Default Categories (Phase 1):**
- Groceries & Staples
- Fruits & Vegetables
- Dairy & Eggs
- Beverages
- Snacks & Packaged Foods
- Personal Care
- Household Supplies
- Medicines & Health
- Electronics & Accessories
- Stationery

**Acceptance Criteria:**
- [ ] All 10 categories visible on home screen
- [ ] Category page loads within 1.5 seconds
- [ ] Empty category shows "Coming soon in your area" message

---

## 3.4 Cart Management

### FR-CUST-008 — Add to Cart

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-008 |
| **Title** | Add Products to Cart |
| **Priority** | Must Have |
| **Module** | Customer > Cart |

**Description:**  
Customers shall be able to add products to a cart. The cart is merchant-specific — a customer cannot mix products from two different merchants in one cart. If a customer tries to add from a second merchant, a warning dialog must appear.

**Business Rules:**
- Cart is stored server-side, synced with local state
- Maximum 50 unique SKUs per cart
- Maximum quantity per SKU: 50 units
- If item goes out of stock after being added, it is flagged in cart with "Out of Stock" label
- Cart persists across sessions (stored in Redis with 7-day TTL)

**Cart Item Schema:**
```json
{
  "cartItemId": "uuid",
  "productId": "uuid",
  "productName": "string",
  "quantity": "integer",
  "unitPrice": "decimal",
  "totalPrice": "decimal",
  "imageUrl": "string",
  "merchantId": "uuid",
  "inStock": "boolean"
}
```

**Acceptance Criteria:**
- [ ] Item added to cart within 500ms
- [ ] Adding from new merchant triggers confirmation dialog
- [ ] Cart badge count updates in real-time
- [ ] Out-of-stock items cannot be checked out
- [ ] Cart preserved after app restart

---

### FR-CUST-009 — Apply Coupon

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-009 |
| **Title** | Coupon and Promo Code Application |
| **Priority** | Should Have |
| **Module** | Customer > Cart |

**Description:**  
Customers shall be able to apply coupon codes at the cart stage. The system shall validate the coupon against rules (minimum order, expiry, user eligibility, usage limit) and display the discount applied.

**Coupon Types:**

| Type | Description |
|---|---|
| FLAT | Fixed amount off (e.g., ₹50 off) |
| PERCENT | Percentage off (e.g., 20% off, max ₹100) |
| FREE_DELIVERY | Waives delivery fee |
| BXGY | Buy X Get Y free |
| CASHBACK | Amount credited to wallet post-delivery |

**Validation Rules:**
- Coupon not expired
- Minimum cart value met
- User has not exceeded usage limit
- Coupon applicable to cart's merchant (if merchant-specific)
- Coupon not already applied

**Acceptance Criteria:**
- [ ] Valid coupon shows discount breakdown
- [ ] Expired coupon returns "Coupon expired" message
- [ ] Below minimum order returns "Add ₹X more to use this coupon"
- [ ] Only one coupon applicable at a time
- [ ] Coupon removed if cart items change eligibility

---

## 3.5 Checkout & Payment

### FR-CUST-010 — Checkout Flow

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-010 |
| **Title** | Multi-Step Checkout Process |
| **Priority** | Must Have |
| **Module** | Customer > Checkout |

**Description:**  
The checkout flow shall guide customers through address confirmation, order review, payment selection, and order placement in a linear multi-step flow.

**Checkout Steps:**
```
Step 1: Confirm Delivery Address
  → Select from saved addresses or add new
  → Show estimated delivery time

Step 2: Review Order
  → List all cart items with quantities and prices
  → Show item total, delivery fee, taxes, discount
  → Final payable amount

Step 3: Select Payment Method
  → UPI (Google Pay, PhonePe, Paytm, BHIM)
  → Credit/Debit Card
  → Net Banking
  → NearKart Wallet
  → Cash on Delivery (if eligible)

Step 4: Place Order
  → Confirm payment
  → Order created with status PENDING
  → Merchant notified
  → Customer redirected to Order Tracking screen
```

**Order Summary Schema:**
```json
{
  "orderId": "string",
  "customerId": "uuid",
  "merchantId": "uuid",
  "items": [],
  "subtotal": "decimal",
  "deliveryFee": "decimal",
  "taxAmount": "decimal",
  "discountAmount": "decimal",
  "totalAmount": "decimal",
  "paymentMethod": "string",
  "deliveryAddress": {},
  "status": "PENDING"
}
```

**Acceptance Criteria:**
- [ ] Checkout completes within 30 seconds of payment
- [ ] Order ID generated and displayed on success screen
- [ ] Payment failure returns to payment step with error message
- [ ] COD only available for orders below ₹2,000
- [ ] Tax calculation accurate to 2 decimal places

---

### FR-CUST-011 — Payment Processing

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-011 |
| **Title** | Razorpay Payment Gateway Integration |
| **Priority** | Must Have |
| **Module** | Customer > Payment |

**Description:**  
All online payments shall be processed via Razorpay. The backend creates a Razorpay order, the Flutter SDK renders the payment sheet, and on success a webhook confirms payment and transitions the order status.

**Payment Flow:**
```
1. Customer taps "Pay ₹XXX"
2. Backend calls Razorpay Orders API → returns order_id
3. Flutter Razorpay SDK opens payment sheet
4. Customer completes payment
5. Razorpay sends webhook to /payments/webhook
6. Backend verifies signature (HMAC SHA256)
7. Payment record saved, order status → CONFIRMED
8. FCM notification sent to customer and merchant
```

**Supported Payment Methods:**
- UPI (VPA, QR, Intent)
- Cards (Visa, Mastercard, RuPay, Amex)
- Net Banking (50+ banks)
- Wallets (Paytm, PhonePe, Amazon Pay)
- EMI (on cards above ₹3,000)
- NearKart Wallet (internal)

**Acceptance Criteria:**
- [ ] Payment success transitions order to CONFIRMED within 10 seconds
- [ ] Payment failure does not create an order record
- [ ] Razorpay signature verification implemented (no plaintext secrets)
- [ ] Refund initiated within 24 hours of cancellation
- [ ] Payment receipt available in order details

---

## 3.6 Order Tracking

### FR-CUST-012 — Real-Time Order Tracking

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-012 |
| **Title** | Live Order Status and GPS Tracking |
| **Priority** | Must Have |
| **Module** | Customer > Orders |

**Description:**  
After order placement, customers shall see a real-time tracking screen showing the current order status and live GPS location of the delivery partner.

**Order Status Lifecycle:**
```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP
→ PICKED_UP → OUT_FOR_DELIVERY → DELIVERED

Alternate paths:
PENDING → CANCELLED (by customer or merchant)
OUT_FOR_DELIVERY → DELIVERY_FAILED
```

**Tracking Screen Elements:**
- Progress stepper showing current status
- Estimated delivery time (ETA countdown)
- Google Maps view with delivery partner's live pin
- Delivery partner name, photo, phone number
- "Call Delivery Partner" button
- "Cancel Order" button (only when status is PENDING or CONFIRMED)

**Real-Time Updates:**
- Delivery partner location sent via WebSocket every 10 seconds
- Order status changes trigger FCM push notifications

**Acceptance Criteria:**
- [ ] Map pin updates within 15 seconds of driver movement
- [ ] All status transitions displayed correctly
- [ ] ETA updates dynamically based on driver location
- [ ] Customer can call delivery partner directly from app
- [ ] Cancel button hidden after PREPARING status

---

## 3.7 Order History & Returns

### FR-CUST-013 — Order History

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-013 |
| **Title** | View Past Orders |
| **Priority** | Must Have |
| **Module** | Customer > Orders |

**Description:**  
Customers shall be able to view all their past orders with details including items, amounts, date, merchant, and order status.

**Order History List Item:**
```
[Order ID] [Merchant Name]
[Date & Time]               [Status Badge]
[Item count] items          ₹[Total Amount]
[Reorder Button]            [View Details Button]
```

**Acceptance Criteria:**
- [ ] Orders paginated (20 per page)
- [ ] Filter by date range and status
- [ ] Order details screen shows full invoice breakdown
- [ ] Reorder adds same items to cart (with stock validation)

---

### FR-CUST-014 — Return & Refund Request

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-014 |
| **Title** | Initiate Return or Refund |
| **Priority** | Should Have |
| **Module** | Customer > Returns |

**Description:**  
Customers shall be able to raise a return or refund request within 24 hours of delivery for eligible products. The system shall collect reason, supporting photos, and route the request to admin for review.

**Return Reasons:**
- Wrong item delivered
- Item damaged / expired
- Missing item in order
- Quality not as expected
- Changed my mind

**Return Flow:**
```
1. Customer taps "Return / Refund" on delivered order
2. Selects items to return
3. Selects reason (+ optional description)
4. Uploads 1–3 photos (required for damaged/wrong)
5. System creates return ticket
6. Admin reviews within 24 hours
7. Approved → Refund to original payment method or wallet
8. Rejected → Customer notified with reason
```

**Acceptance Criteria:**
- [ ] Return window: 24 hours from DELIVERED timestamp
- [ ] Photo upload mandatory for damaged/wrong item reasons
- [ ] Refund processed within 3–5 business days to source
- [ ] Wallet refund credited within 2 hours

---

## 3.8 Ratings & Reviews

### FR-CUST-015 — Rate Order and Merchant

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-015 |
| **Title** | Post-Delivery Rating and Review |
| **Priority** | Should Have |
| **Module** | Customer > Reviews |

**Description:**  
After order delivery, customers shall be prompted to rate the overall experience, the merchant, and the delivery partner. Ratings are on a 1–5 star scale with optional text review.

**Rating Components:**

| Component | Scale | Mandatory |
|---|---|---|
| Overall Experience | 1–5 stars | Yes (on prompt) |
| Merchant / Food Quality | 1–5 stars | No |
| Delivery Partner | 1–5 stars | No |
| Text Review | 500 chars max | No |
| Photos | Up to 3 images | No |

**Business Rules:**
- Rating prompt shown 30 minutes after DELIVERED status
- Only customers who placed the order can review it
- Reviews moderated for abusive content (AI moderation, Phase 2)
- Merchant can respond to reviews

**Acceptance Criteria:**
- [ ] Rating prompt appears after 30 minutes post-delivery
- [ ] Star rating saved immediately on tap
- [ ] Merchant's average rating recalculated after each review
- [ ] Review visible on merchant profile within 5 minutes
- [ ] Customer can edit review within 24 hours of posting

---

## 3.9 Wallet & Referral Program

### FR-CUST-016 — NearKart Wallet

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-016 |
| **Title** | In-App Wallet Management |
| **Priority** | Should Have |
| **Module** | Customer > Wallet |

**Description:**  
Each customer shall have a NearKart Wallet that can be topped up and used to pay for orders. The wallet shall display balance, transaction history, and support top-up via payment gateway.

**Wallet Rules:**
- Maximum wallet balance: ₹10,000
- Top-up via UPI/Card/Net Banking via Razorpay
- Wallet cannot be used with COD
- Wallet balance non-transferable between accounts
- Cashback from offers credited to wallet

**Acceptance Criteria:**
- [ ] Wallet balance displayed on profile and checkout screens
- [ ] Top-up of ₹1 to ₹10,000 accepted
- [ ] Transaction history shows credit/debit with reason
- [ ] Refund credited to wallet within 2 hours of approval

---

### FR-CUST-017 — Referral Program

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-017 |
| **Title** | Customer Referral and Reward System |
| **Priority** | Could Have |
| **Module** | Customer > Referral |

**Description:**  
Customers shall receive a unique 8-character referral code. When a new user registers using this code and completes their first order, both the referrer and referee receive wallet credits.

**Referral Rules:**
- Referrer reward: ₹50 wallet credit after referee's first order
- Referee reward: ₹50 off on first order (coupon auto-applied)
- Maximum referral earnings: ₹500 per month per user
- Self-referral detection and blocking enabled

**Acceptance Criteria:**
- [ ] Unique referral code generated on registration
- [ ] Referral link shareable via WhatsApp, SMS, email
- [ ] Reward credited within 1 hour of qualifying order delivery
- [ ] Referral dashboard shows: referred friends, pending rewards, earned rewards

---

## 3.10 Notifications

### FR-CUST-018 — Push Notifications

| Field | Detail |
|---|---|
| **Requirement ID** | FR-CUST-018 |
| **Title** | Customer Push Notification Events |
| **Priority** | Must Have |
| **Module** | Customer > Notifications |

**Notification Triggers:**

| Event | Message | Channel |
|---|---|---|
| Order Confirmed | "Your order #ORD-XXXX is confirmed!" | FCM + SMS |
| Merchant Preparing | "[Store] is preparing your order" | FCM |
| Out for Delivery | "Your order is on the way!" | FCM + SMS |
| Delivered | "Order delivered. Rate your experience!" | FCM |
| Order Cancelled | "Order #ORD-XXXX was cancelled. Refund initiated." | FCM + SMS |
| Coupon Available | "New offer: 20% off at [Store]!" | FCM |
| Wallet Credit | "₹50 added to your NearKart Wallet" | FCM |
| OTP | "Your NearKart OTP is XXXXXX" | SMS only |

**Acceptance Criteria:**
- [ ] Notification delivered within 30 seconds of event
- [ ] Customer can disable promotional notifications
- [ ] Transactional notifications (OTP, order status) cannot be disabled
- [ ] Notification tapped opens relevant screen in app

---

## 3.11 Customer Module — Data Model

### Entity: `customers`

```sql
CREATE TABLE customers (
  customer_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phone_number    VARCHAR(15) UNIQUE NOT NULL,
  full_name       VARCHAR(100),
  email           VARCHAR(255),
  profile_photo   TEXT,
  date_of_birth   DATE,
  gender          VARCHAR(20),
  referral_code   VARCHAR(8) UNIQUE,
  referred_by     UUID REFERENCES customers(customer_id),
  wallet_balance  DECIMAL(10,2) DEFAULT 0.00,
  is_active       BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMP DEFAULT NOW(),
  updated_at      TIMESTAMP DEFAULT NOW()
);
```

### Entity: `customer_addresses`

```sql
CREATE TABLE customer_addresses (
  address_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID REFERENCES customers(customer_id) ON DELETE CASCADE,
  label         VARCHAR(20) DEFAULT 'Home',
  flat_no       VARCHAR(50),
  building      VARCHAR(100),
  street        VARCHAR(200),
  landmark      VARCHAR(100),
  city          VARCHAR(100) NOT NULL,
  state         VARCHAR(100) NOT NULL,
  pincode       VARCHAR(6) NOT NULL,
  latitude      DECIMAL(10, 8),
  longitude     DECIMAL(11, 8),
  is_default    BOOLEAN DEFAULT FALSE,
  created_at    TIMESTAMP DEFAULT NOW()
);
```

### Entity: `carts`

```sql
CREATE TABLE carts (
  cart_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID REFERENCES customers(customer_id),
  merchant_id   UUID REFERENCES merchants(merchant_id),
  coupon_code   VARCHAR(50),
  discount      DECIMAL(10,2) DEFAULT 0.00,
  updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cart_items (
  cart_item_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  cart_id       UUID REFERENCES carts(cart_id) ON DELETE CASCADE,
  product_id    UUID REFERENCES products(product_id),
  quantity      INTEGER NOT NULL CHECK (quantity > 0),
  unit_price    DECIMAL(10,2) NOT NULL,
  added_at      TIMESTAMP DEFAULT NOW()
);
```

---

## 3.12 Customer Module — API Endpoints Summary

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/v1/auth/send-otp | Send OTP to phone | Public |
| POST | /api/v1/auth/verify-otp | Verify OTP, return JWT | Public |
| POST | /api/v1/auth/google | Google OAuth login | Public |
| POST | /api/v1/auth/refresh | Refresh access token | Refresh token |
| POST | /api/v1/auth/logout | Invalidate tokens | Customer |
| GET | /api/v1/customers/me | Get customer profile | Customer |
| PUT | /api/v1/customers/me | Update profile | Customer |
| GET | /api/v1/customers/me/addresses | List addresses | Customer |
| POST | /api/v1/customers/me/addresses | Add address | Customer |
| PUT | /api/v1/customers/me/addresses/{id} | Update address | Customer |
| DELETE | /api/v1/customers/me/addresses/{id} | Delete address | Customer |
| GET | /api/v1/merchants/nearby | Get nearby merchants | Customer |
| GET | /api/v1/products/search | Search products | Customer |
| GET | /api/v1/categories | List categories | Public |
| GET | /api/v1/cart | Get cart | Customer |
| POST | /api/v1/cart/items | Add item to cart | Customer |
| PUT | /api/v1/cart/items/{id} | Update quantity | Customer |
| DELETE | /api/v1/cart/items/{id} | Remove item | Customer |
| POST | /api/v1/cart/coupon | Apply coupon | Customer |
| DELETE | /api/v1/cart/coupon | Remove coupon | Customer |
| POST | /api/v1/orders | Place order | Customer |
| GET | /api/v1/orders | Order history | Customer |
| GET | /api/v1/orders/{id} | Order details | Customer |
| GET | /api/v1/orders/{id}/track | Live tracking | Customer |
| POST | /api/v1/orders/{id}/cancel | Cancel order | Customer |
| POST | /api/v1/orders/{id}/return | Return request | Customer |
| POST | /api/v1/orders/{id}/review | Post review | Customer |
| GET | /api/v1/wallet | Get wallet balance | Customer |
| POST | /api/v1/wallet/topup | Top up wallet | Customer |
| GET | /api/v1/wallet/transactions | Transaction history | Customer |
| GET | /api/v1/referral | Get referral code + stats | Customer |

---

*End of Section 3 — Customer Module*

> Previous: [Section 2 — Overall Description](section-02-overall-description.md)  
> Next: [Section 4 — Merchant Module](section-04-merchant-module.md)
