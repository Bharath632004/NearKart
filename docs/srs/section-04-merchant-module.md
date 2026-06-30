# Section 4: Functional Requirements — Merchant Module

> **Document:** NearKart SRS v1.0
> **Section:** 4 of 20
> **Module:** Merchant

---

## 4.0 Module Overview

The Merchant Module covers all interactions performed by shop owners and business operators on the NearKart platform. This includes store registration, product/inventory management, order management, earnings, analytics, and store configuration.

**Primary Actor:** Merchant (shop owner / store manager)
**Platforms:** Flutter Android App (Merchant App), React Web Dashboard
**Authentication:** Phone OTP + optional Google OAuth

---

## 4.1 Merchant Registration & Onboarding

### FR-MERCH-001 — Merchant Registration

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-001 |
| **Title** | Merchant Account Registration |
| **Priority** | Must Have |
| **Module** | Merchant > Registration |

**Description:**
A new merchant must register by providing their mobile number, business name, category, and GSTIN (optional for small traders). After OTP verification, the merchant account is created with `status = PENDING_VERIFICATION`.

**Preconditions:**
- Merchant has a valid Indian mobile number
- Merchant has a physical shop/business

**Registration Fields:**

| Field | Type | Validation | Required |
|---|---|---|---|
| Phone Number | String | 10-digit, starts with 6–9 | Yes |
| Owner Full Name | String | 2–60 chars | Yes |
| Business Name | String | 2–100 chars | Yes |
| Business Category | Enum | From predefined list | Yes |
| GSTIN | String | 15-char alphanumeric | No |
| PAN Number | String | 10-char format | Yes (for payouts) |
| Email | String | Valid email | No |

**Flow:**
```
1. Merchant opens Merchant App → Taps "Register Your Store"
2. Enters phone number → OTP sent
3. OTP verified → Registration form shown
4. Merchant fills business details
5. Uploads documents (FSSAI / Shop License / GST)
6. Form submitted → Account created (PENDING_VERIFICATION)
7. Admin reviews within 24 hours
8. On approval → Merchant can go live
```

**Postconditions:**
- Merchant record created in `merchants` table with `status = PENDING_VERIFICATION`
- Admin notified for review
- Merchant receives "Application received" SMS/email

**Acceptance Criteria:**
- [ ] Merchant cannot accept orders until status = ACTIVE
- [ ] Duplicate phone number registration returns HTTP 409
- [ ] Application reviewed within 24 hours (SLA)
- [ ] Merchant notified via SMS on approval or rejection
- [ ] Rejected merchant can re-apply with corrected documents

---

### FR-MERCH-002 — Store Profile Setup

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-002 |
| **Title** | Store Profile Configuration |
| **Priority** | Must Have |
| **Module** | Merchant > Profile |

**Description:**
After approval, the merchant must configure their store profile before going live. This includes store logo, banner image, operating hours, delivery settings, and minimum order value.

**Store Profile Fields:**

| Field | Type | Validation | Required |
|---|---|---|---|
| Store Name | String | 2–100 chars | Yes |
| Store Logo | Image | JPEG/PNG, max 5MB | Yes |
| Banner Image | Image | JPEG/PNG, 1200x400px recommended | No |
| Store Description | String | Max 500 chars | No |
| Business Category | Enum | Grocery/Pharmacy/Electronics/etc. | Yes |
| FSSAI License No. | String | 14-digit (food businesses) | Conditional |
| Store Address | Object | Full address with lat/lng | Yes |
| Operating Hours | Array | Per day: open time, close time | Yes |
| Delivery Radius | Decimal | 1–20 km | Yes |
| Minimum Order Value | Decimal | ₹0 – ₹500 | Yes |
| Delivery Fee | Decimal | ₹0 – ₹100 | Yes |
| Free Delivery Above | Decimal | Cart value threshold | No |
| Average Prep Time | Integer | Minutes (5–120) | Yes |

**Operating Hours Schema:**
```json
{
  "operatingHours": [
    { "day": "MON", "openTime": "09:00", "closeTime": "21:00", "isClosed": false },
    { "day": "TUE", "openTime": "09:00", "closeTime": "21:00", "isClosed": false },
    { "day": "SUN", "openTime": "10:00", "closeTime": "18:00", "isClosed": false }
  ]
}
```

**Acceptance Criteria:**
- [ ] Store not visible to customers until all required fields are filled
- [ ] Logo and banner images resized and stored on S3
- [ ] Operating hours validated (open time must be before close time)
- [ ] Delivery radius stored as PostGIS geometry for spatial queries
- [ ] Merchant can update profile at any time (changes reflected within 5 minutes)

---

### FR-MERCH-003 — Bank Account & Payout Setup

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-003 |
| **Title** | Bank Account Registration for Payouts |
| **Priority** | Must Have |
| **Module** | Merchant > Finance |

**Description:**
Merchants must link a bank account to receive weekly payouts. The system shall collect account details and verify via penny-drop or Razorpay Routes.

**Bank Account Fields:**

| Field | Validation |
|---|---|
| Account Holder Name | Must match PAN name |
| Account Number | 9–18 digits |
| IFSC Code | 11-char format (e.g., SBIN0001234) |
| Account Type | Savings / Current |

**Acceptance Criteria:**
- [ ] Bank account verified via Razorpay penny-drop before activation
- [ ] Merchant cannot receive payouts without verified bank account
- [ ] Account details masked after verification (show only last 4 digits)
- [ ] Merchant can update bank account (requires re-verification)

---

## 4.2 Product & Inventory Management

### FR-MERCH-004 — Add Product

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-004 |
| **Title** | Add New Product to Store Catalog |
| **Priority** | Must Have |
| **Module** | Merchant > Inventory |

**Description:**
Merchants shall be able to add new products to their store catalog. Each product belongs to a category and has pricing, stock, and image information.

**Product Fields:**

| Field | Type | Validation | Required |
|---|---|---|---|
| Product Name | String | 2–200 chars | Yes |
| Category | Enum | From master category list | Yes |
| Sub-Category | Enum | Based on selected category | No |
| Brand | String | Max 100 chars | No |
| Description | String | Max 1000 chars | No |
| MRP | Decimal | > 0 | Yes |
| Selling Price | Decimal | > 0, ≤ MRP | Yes |
| Unit | Enum | kg/g/L/mL/piece/pack/dozen | Yes |
| Quantity per Unit | Decimal | e.g., 500 for "500g" | Yes |
| Stock Quantity | Integer | ≥ 0 | Yes |
| Low Stock Alert | Integer | Default 10 | No |
| Images | Array | Up to 5, JPEG/PNG, max 5MB each | Yes (min 1) |
| Barcode / SKU | String | Auto-generated if not provided | No |
| Is Active | Boolean | Default true | Yes |
| Tags | Array | Searchable keywords | No |
| Tax Rate | Decimal | 0/5/12/18/28% GST slabs | Yes |
| HSN Code | String | 4–8 digit HSN | No |

**Acceptance Criteria:**
- [ ] Product visible on store within 2 minutes of creation
- [ ] Selling price cannot exceed MRP (rejected with validation error)
- [ ] Images uploaded to S3 with CDN URLs stored
- [ ] Auto-generated SKU format: `{MERCHANT_ID_PREFIX}-{CATEGORY_CODE}-{SEQUENCE}`
- [ ] Product with stock = 0 automatically marked as out of stock
- [ ] Merchant can add up to 10,000 products per store

---

### FR-MERCH-005 — Edit / Delete Product

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-005 |
| **Title** | Edit or Remove Product from Catalog |
| **Priority** | Must Have |
| **Module** | Merchant > Inventory |

**Description:**
Merchants shall be able to edit any product detail or soft-delete products from their catalog. Active orders containing deleted products are not affected.

**Business Rules:**
- Products are soft-deleted (`is_active = false`), not hard-deleted
- Products in active orders cannot be deleted (marked inactive only after order completion)
- Price changes on active products do not retroactively affect ongoing orders
- Editing product name or images requires no approval
- Bulk edit supported for price and stock updates via CSV upload

**Acceptance Criteria:**
- [ ] Edited product reflects changes within 2 minutes
- [ ] Soft-deleted products disappear from customer-facing catalog immediately
- [ ] Bulk CSV upload supports up to 500 products in a single file
- [ ] Invalid rows in CSV bulk upload reported in a downloadable error report
- [ ] Edit history logged with timestamp and changed fields

---

### FR-MERCH-006 — Inventory Stock Management

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-006 |
| **Title** | Real-Time Stock Update and Low Stock Alerts |
| **Priority** | Must Have |
| **Module** | Merchant > Inventory |

**Description:**
Stock levels shall automatically decrease on order placement and increase on order cancellation. Merchants shall receive low stock alerts and be able to manually update stock quantities.

**Stock Lifecycle:**
```
Order Placed → stock_quantity decremented (reserved)
Order Cancelled → stock_quantity restored
Order Delivered → no change (already decremented)
Manual Update → merchant sets absolute stock value
```

**Alert Rules:**
- Low stock alert sent when `stock_quantity ≤ low_stock_threshold`
- Out-of-stock notification sent when `stock_quantity = 0`
- Alerts delivered via FCM push + in-app notification

**Acceptance Criteria:**
- [ ] Stock decremented atomically (race condition safe via DB transactions)
- [ ] Merchant can update stock via quick-edit on inventory list screen
- [ ] Low stock alert sent within 5 minutes of threshold breach
- [ ] Zero-stock products auto-hidden from customer catalog
- [ ] Stock history log available for last 30 days

---

### FR-MERCH-007 — Product Categories & Collections

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-007 |
| **Title** | Organize Products into Collections |
| **Priority** | Should Have |
| **Module** | Merchant > Catalog |

**Description:**
Merchants shall be able to create custom collections (e.g., "Today's Deals", "Fresh Arrivals", "Best Sellers") and assign products to them. Collections are displayed on the store homepage.

**Collection Schema:**
```json
{
  "collectionId": "uuid",
  "merchantId": "uuid",
  "name": "Today's Deals",
  "description": "string",
  "bannerImage": "S3 URL",
  "sortOrder": 1,
  "isActive": true,
  "productIds": ["uuid1", "uuid2"]
}
```

**Acceptance Criteria:**
- [ ] Merchant can create up to 20 collections per store
- [ ] Collection displayed on store page in sort order
- [ ] A product can belong to multiple collections
- [ ] Empty collections hidden from customer view

---

## 4.3 Order Management

### FR-MERCH-008 — Receive and Accept Orders

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-008 |
| **Title** | New Order Notification and Acceptance |
| **Priority** | Must Have |
| **Module** | Merchant > Orders |

**Description:**
When a customer places an order, the merchant shall be notified instantly via FCM push and an alert sound. The merchant must accept or reject the order within 3 minutes, failing which the order is auto-cancelled.

**Order Card Data (shown in merchant app):**
```json
{
  "orderId": "ORD-20260630-XXXX",
  "customerName": "string",
  "itemCount": "integer",
  "totalAmount": "decimal",
  "deliveryAddress": "short formatted string",
  "estimatedPrepTime": "integer (minutes)",
  "placedAt": "timestamp",
  "acceptanceDeadline": "placedAt + 3 minutes"
}
```

**Flow:**
```
1. Customer places order
2. Merchant app receives FCM push + alert sound
3. New Order screen shown with countdown timer (3 min)
4. Merchant taps "Accept" → order status → CONFIRMED
5. Merchant taps "Reject" → order status → CANCELLED, refund initiated
6. Timer expires without action → auto-cancelled, refund initiated
7. Merchant sets/confirms prep time estimate
```

**Acceptance Criteria:**
- [ ] Order notification delivered within 10 seconds of placement
- [ ] Countdown timer visible and accurate
- [ ] Auto-cancellation triggers refund within 5 minutes
- [ ] Accepted orders appear in "Active Orders" list
- [ ] Merchant can reject with a reason (Out of stock / Store closing / Other)

---

### FR-MERCH-009 — Prepare and Dispatch Order

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-009 |
| **Title** | Order Preparation and Handoff to Delivery |
| **Priority** | Must Have |
| **Module** | Merchant > Orders |

**Description:**
After accepting the order, the merchant prepares items and marks them as ready. The system then notifies a nearby available delivery partner to pick up the order.

**Status Transitions by Merchant:**
```
CONFIRMED → PREPARING       (merchant taps "Start Preparing")
PREPARING → READY_FOR_PICKUP (merchant taps "Order Ready")
```

**Ready for Pickup Flow:**
```
1. Merchant taps "Order Ready"
2. System broadcasts to available delivery partners within 3 km
3. Delivery partner accepts pickup
4. Delivery partner details shown to merchant and customer
5. Status → PICKED_UP when delivery partner scans/confirms
```

**Acceptance Criteria:**
- [ ] Merchant can print order receipt (Bluetooth thermal printer support)
- [ ] "Order Ready" notification sent to delivery partners within 30 seconds
- [ ] Merchant sees delivery partner name and ETA to store
- [ ] Merchant can add item-level notes (e.g., "substituted with brand X")
- [ ] Partially fulfillable orders flagged for merchant with option to contact customer

---

### FR-MERCH-010 — Order History & Dispute Management

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-010 |
| **Title** | View Order History and Manage Disputes |
| **Priority** | Must Have |
| **Module** | Merchant > Orders |

**Description:**
Merchants shall view all past orders with status, amount, and customer details. They shall also be able to respond to return/refund requests raised by customers.

**Order History Filters:**
- Date range
- Order status (All / Active / Completed / Cancelled / Disputed)
- Payment method
- Minimum order value

**Dispute Response Flow:**
```
1. Customer raises return request
2. Merchant notified via FCM
3. Merchant reviews photos and reason
4. Merchant taps "Approve Return" or "Dispute"
5. If approved → refund processed
6. If disputed → escalated to admin for final decision
```

**Acceptance Criteria:**
- [ ] Order history paginated (20 per page), filterable
- [ ] Dispute response window: 24 hours from customer request
- [ ] Escalated disputes resolved by admin within 48 hours
- [ ] Merchant can add note/evidence when disputing a return
- [ ] Cancelled orders due to merchant rejection flagged in merchant analytics

---

## 4.4 Store Controls

### FR-MERCH-011 — Open/Close Store Toggle

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-011 |
| **Title** | Instant Store Open/Close Control |
| **Priority** | Must Have |
| **Module** | Merchant > Controls |

**Description:**
Merchants shall be able to toggle their store open or closed at any time, outside of scheduled operating hours. When closed, the store appears with a "Currently Closed" badge to customers and no new orders can be placed.

**Business Rules:**
- Store auto-opens at `openTime` and auto-closes at `closeTime` per schedule
- Manual override lasts until manually reversed or next scheduled time
- If store closed mid-order (rare), existing accepted orders are unaffected

**Acceptance Criteria:**
- [ ] Toggle reflected on customer app within 30 seconds
- [ ] Manual close shown as "Closed by owner" (not "Outside hours") in admin panel
- [ ] Merchant can set "Closed for holidays" with optional reopen date

---

### FR-MERCH-012 — Delivery Zone Configuration

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-012 |
| **Title** | Configure Delivery Radius and Zone |
| **Priority** | Should Have |
| **Module** | Merchant > Settings |

**Description:**
Merchants shall configure their delivery radius on an interactive map. They can choose a circular radius (1–20 km) or draw a custom polygon for their delivery zone.

**Acceptance Criteria:**
- [ ] Circular radius shown on map as draggable circle
- [ ] Custom polygon support (up to 20 vertices)
- [ ] Zone saved as PostGIS geometry in DB
- [ ] Change reflected in customer discovery within 10 minutes
- [ ] Merchant can define multiple delivery fee tiers by distance zone

---

## 4.5 Merchant Analytics & Earnings

### FR-MERCH-013 — Earnings Dashboard

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-013 |
| **Title** | Real-Time Earnings and Payout Tracking |
| **Priority** | Must Have |
| **Module** | Merchant > Finance |

**Description:**
Merchants shall view their earnings dashboard showing today's revenue, pending payouts, total earnings, and payout history.

**Dashboard Metrics:**

| Metric | Description |
|---|---|
| Today's Revenue | Sum of completed orders today |
| This Week's Revenue | Rolling 7-day total |
| Pending Payout | Amount not yet disbursed |
| Total Earnings (Lifetime) | All-time completed order revenue |
| Platform Commission | NearKart's commission deducted (configurable %) |
| Net Payout | Revenue minus commission |

**Payout Schedule:**
- Payouts processed every Monday for the previous week (Mon–Sun)
- Minimum payout threshold: ₹100
- Razorpay Routes used for bank transfer

**Acceptance Criteria:**
- [ ] Today's earnings update within 5 minutes of order delivery
- [ ] Commission rate displayed transparently per order
- [ ] Payout history downloadable as CSV/PDF
- [ ] Failed payout retry within 24 hours with merchant notification
- [ ] Tax invoice (GST) generated monthly for commission charges

---

### FR-MERCH-014 — Sales Analytics

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-014 |
| **Title** | Store Performance Analytics |
| **Priority** | Should Have |
| **Module** | Merchant > Analytics |

**Description:**
Merchants shall have access to sales analytics showing top-selling products, peak order hours, customer return rates, and revenue trends.

**Analytics Metrics:**

| Report | Dimensions |
|---|---|
| Revenue Trend | Daily/Weekly/Monthly line chart |
| Top 10 Products | By units sold and by revenue |
| Order Volume by Hour | Heatmap by day and hour |
| Average Order Value | Trend over time |
| Customer Repeat Rate | % returning customers |
| Cancellation Rate | % orders cancelled and reason breakdown |
| Return Rate | % orders returned |

**Acceptance Criteria:**
- [ ] Analytics data refreshed hourly
- [ ] Date range filter: Today / 7 Days / 30 Days / Custom
- [ ] Charts rendered in-app (Flutter fl_chart library)
- [ ] Analytics exportable as PDF report
- [ ] Data retained for 12 months

---

## 4.6 Merchant Notifications

### FR-MERCH-015 — Merchant Push Notifications

| Field | Detail |
|---|---|
| **Requirement ID** | FR-MERCH-015 |
| **Title** | Merchant Notification Events |
| **Priority** | Must Have |
| **Module** | Merchant > Notifications |

**Notification Triggers:**

| Event | Message | Channel |
|---|---|---|
| New Order | "New order ₹XXX from [Customer]! Accept now." | FCM + Alert Sound |
| Order Auto-Cancelled | "Order #ORD-XXXX auto-cancelled (no response)" | FCM |
| Low Stock Alert | "[Product] is running low (X units left)" | FCM |
| Out of Stock | "[Product] is now out of stock" | FCM |
| Return Request | "Customer raised return for Order #ORD-XXXX" | FCM |
| Payout Processed | "₹XXXX credited to your bank account" | FCM + SMS |
| Payout Failed | "Payout of ₹XXXX failed. Check bank details." | FCM + SMS |
| Account Approved | "Your store is approved! Go live now." | FCM + SMS |
| Account Suspended | "Your store has been suspended. Contact support." | FCM + SMS |

**Acceptance Criteria:**
- [ ] New order notifications have distinct alert sound (different from other notifications)
- [ ] Notification tapped opens relevant screen (e.g., order detail, inventory)
- [ ] Merchant can configure which non-critical alerts to receive

---

## 4.7 Merchant Module — Data Model

### Entity: `merchants`

```sql
CREATE TABLE merchants (
  merchant_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_name          VARCHAR(100) NOT NULL,
  phone_number        VARCHAR(15) UNIQUE NOT NULL,
  email               VARCHAR(255),
  business_name       VARCHAR(200) NOT NULL,
  category            VARCHAR(100) NOT NULL,
  gstin               VARCHAR(15),
  pan_number          VARCHAR(10),
  fssai_license       VARCHAR(20),
  store_logo          TEXT,
  banner_image        TEXT,
  description         VARCHAR(500),
  address_line        TEXT,
  city                VARCHAR(100),
  state               VARCHAR(100),
  pincode             VARCHAR(6),
  latitude            DECIMAL(10, 8),
  longitude           DECIMAL(11, 8),
  delivery_radius_km  DECIMAL(5, 2) DEFAULT 10.00,
  delivery_fee        DECIMAL(8, 2) DEFAULT 0.00,
  free_delivery_above DECIMAL(8, 2),
  min_order_value     DECIMAL(8, 2) DEFAULT 0.00,
  avg_prep_time_min   INTEGER DEFAULT 20,
  commission_rate     DECIMAL(5, 2) DEFAULT 15.00,
  status              VARCHAR(30) DEFAULT 'PENDING_VERIFICATION',
  is_open             BOOLEAN DEFAULT FALSE,
  rating              DECIMAL(3, 2) DEFAULT 0.00,
  total_reviews       INTEGER DEFAULT 0,
  bank_account_id     UUID,
  created_at          TIMESTAMP DEFAULT NOW(),
  updated_at          TIMESTAMP DEFAULT NOW()
);
-- status values: PENDING_VERIFICATION, ACTIVE, SUSPENDED, REJECTED, CLOSED
```

### Entity: `merchant_operating_hours`

```sql
CREATE TABLE merchant_operating_hours (
  hours_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id  UUID REFERENCES merchants(merchant_id) ON DELETE CASCADE,
  day_of_week  VARCHAR(3) NOT NULL,  -- MON, TUE, WED, THU, FRI, SAT, SUN
  open_time    TIME,
  close_time   TIME,
  is_closed    BOOLEAN DEFAULT FALSE,
  UNIQUE (merchant_id, day_of_week)
);
```

### Entity: `products`

```sql
CREATE TABLE products (
  product_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id       UUID REFERENCES merchants(merchant_id) ON DELETE CASCADE,
  category_id       UUID REFERENCES categories(category_id),
  name              VARCHAR(200) NOT NULL,
  description       TEXT,
  brand             VARCHAR(100),
  sku               VARCHAR(100) UNIQUE,
  barcode           VARCHAR(50),
  mrp               DECIMAL(10, 2) NOT NULL,
  selling_price     DECIMAL(10, 2) NOT NULL,
  unit              VARCHAR(20) NOT NULL,
  quantity_per_unit DECIMAL(10, 3),
  stock_quantity    INTEGER DEFAULT 0,
  low_stock_alert   INTEGER DEFAULT 10,
  tax_rate          DECIMAL(5, 2) DEFAULT 0.00,
  hsn_code          VARCHAR(8),
  is_active         BOOLEAN DEFAULT TRUE,
  tags              TEXT[],
  created_at        TIMESTAMP DEFAULT NOW(),
  updated_at        TIMESTAMP DEFAULT NOW(),
  CONSTRAINT price_lte_mrp CHECK (selling_price <= mrp)
);

CREATE TABLE product_images (
  image_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id  UUID REFERENCES products(product_id) ON DELETE CASCADE,
  image_url   TEXT NOT NULL,
  sort_order  INTEGER DEFAULT 0,
  is_primary  BOOLEAN DEFAULT FALSE
);
```

### Entity: `merchant_bank_accounts`

```sql
CREATE TABLE merchant_bank_accounts (
  bank_account_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id         UUID REFERENCES merchants(merchant_id) ON DELETE CASCADE,
  account_holder_name VARCHAR(100) NOT NULL,
  account_number      VARCHAR(18) NOT NULL,
  ifsc_code           VARCHAR(11) NOT NULL,
  account_type        VARCHAR(20) DEFAULT 'SAVINGS',
  is_verified         BOOLEAN DEFAULT FALSE,
  verified_at         TIMESTAMP,
  created_at          TIMESTAMP DEFAULT NOW()
);
```

### Entity: `payouts`

```sql
CREATE TABLE payouts (
  payout_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id       UUID REFERENCES merchants(merchant_id),
  period_start      DATE NOT NULL,
  period_end        DATE NOT NULL,
  gross_revenue     DECIMAL(12, 2) NOT NULL,
  commission_amount DECIMAL(12, 2) NOT NULL,
  net_payout        DECIMAL(12, 2) NOT NULL,
  status            VARCHAR(20) DEFAULT 'PENDING',
  razorpay_payout_id VARCHAR(100),
  processed_at      TIMESTAMP,
  created_at        TIMESTAMP DEFAULT NOW()
  -- status values: PENDING, PROCESSING, COMPLETED, FAILED
);
```

---

## 4.8 Merchant Module — API Endpoints Summary

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/v1/merchants/register | Merchant registration | Public |
| POST | /api/v1/merchants/verify-otp | OTP verification | Public |
| GET | /api/v1/merchants/me | Get store profile | Merchant |
| PUT | /api/v1/merchants/me | Update store profile | Merchant |
| POST | /api/v1/merchants/me/bank-account | Add bank account | Merchant |
| PUT | /api/v1/merchants/me/status | Toggle open/close | Merchant |
| GET | /api/v1/merchants/me/products | List products | Merchant |
| POST | /api/v1/merchants/me/products | Add product | Merchant |
| GET | /api/v1/merchants/me/products/{id} | Get product detail | Merchant |
| PUT | /api/v1/merchants/me/products/{id} | Update product | Merchant |
| DELETE | /api/v1/merchants/me/products/{id} | Delete (soft) product | Merchant |
| POST | /api/v1/merchants/me/products/bulk | Bulk CSV upload | Merchant |
| PATCH | /api/v1/merchants/me/products/{id}/stock | Update stock | Merchant |
| GET | /api/v1/merchants/me/collections | List collections | Merchant |
| POST | /api/v1/merchants/me/collections | Create collection | Merchant |
| PUT | /api/v1/merchants/me/collections/{id} | Update collection | Merchant |
| DELETE | /api/v1/merchants/me/collections/{id} | Delete collection | Merchant |
| GET | /api/v1/merchants/me/orders | List orders | Merchant |
| GET | /api/v1/merchants/me/orders/{id} | Order detail | Merchant |
| PATCH | /api/v1/merchants/me/orders/{id}/accept | Accept order | Merchant |
| PATCH | /api/v1/merchants/me/orders/{id}/reject | Reject order | Merchant |
| PATCH | /api/v1/merchants/me/orders/{id}/ready | Mark ready for pickup | Merchant |
| GET | /api/v1/merchants/me/earnings | Earnings summary | Merchant |
| GET | /api/v1/merchants/me/payouts | Payout history | Merchant |
| GET | /api/v1/merchants/me/analytics/sales | Sales analytics | Merchant |
| GET | /api/v1/merchants/me/analytics/products | Product analytics | Merchant |
| GET | /api/v1/merchants/me/operating-hours | Get operating hours | Merchant |
| PUT | /api/v1/merchants/me/operating-hours | Update hours | Merchant |

---

*End of Section 4 — Merchant Module*

> Previous: [Section 3 — Customer Module](section-03-customer-module.md)
> Next: [Section 5 — Delivery Partner Module](section-05-delivery-partner-module.md)
