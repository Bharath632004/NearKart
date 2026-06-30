# Volume 4 – Database Design (250+ Pages)

## Database
- PostgreSQL (Primary)
- Redis (Cache & Sessions)
- PostGIS Extension (Geospatial)

## Core Tables

| # | Table | Description |
|---|---|---|
| 1 | users | All platform users |
| 2 | roles | User roles (customer, merchant, delivery, admin) |
| 3 | permissions | Role-permission mapping |
| 4 | otp | OTP records |
| 5 | sessions | Active sessions |
| 6 | addresses | User addresses with GPS coordinates |
| 7 | shops | Merchant shops |
| 8 | shop_categories | Shop category types |
| 9 | products | Product catalog |
| 10 | product_images | Product image URLs |
| 11 | brands | Product brands |
| 12 | categories | Product categories |
| 13 | subcategories | Product subcategories |
| 14 | inventory | Stock per product per shop |
| 15 | orders | Customer orders |
| 16 | order_items | Items within an order |
| 17 | payments | Payment records |
| 18 | transactions | Transaction log |
| 19 | wallet | User & merchant wallets |
| 20 | wallet_transactions | Wallet credit/debit log |
| 21 | coupons | Discount coupons |
| 22 | offers | Promotional offers |
| 23 | reviews | Product & shop reviews |
| 24 | ratings | Star ratings |
| 25 | delivery_partners | Delivery partner profiles |
| 26 | delivery_assignments | Order-to-partner assignments |
| 27 | notifications | Notification records |
| 28 | complaints | Customer complaints |
| 29 | returns | Return requests |
| 30 | refunds | Refund records |
| 31 | invoices | Order invoices |
| 32 | settlements | Merchant settlement records |
| 33 | commission | Platform commission per order |
| 34 | audit_logs | System audit logs |
| 35 | activity_logs | User activity logs |

Each table includes: Primary Key, Foreign Keys, Constraints, Indexes, Relationships, Sample Data.
