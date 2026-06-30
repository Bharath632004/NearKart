# NearKart Database Design

## Tech Stack
- **PostgreSQL 15+** — Primary relational database
- **Redis 7+** — Session caching, OTP storage, rate limiting
- **Flyway** — Database migration management

## Tables (35+)

| Table | Description |
|-------|-------------|
| users | All platform users (customer, merchant, delivery, admin) |
| roles / permissions | RBAC system |
| otps | OTP storage (backed by Redis) |
| refresh_tokens | JWT refresh token management |
| addresses | User delivery addresses with GPS coordinates |
| shop_categories | Shop type classification |
| shops | Merchant shop profiles with geolocation |
| brands | Product brands |
| categories | Product categories (hierarchical) |
| products | Product catalog per shop |
| product_images | Multiple images per product |
| inventory | Stock levels per product |
| coupons | Discount coupon management |
| wallets | User wallet balances |
| wallet_transactions | Wallet credit/debit ledger |
| orders | Customer orders |
| order_items | Line items per order |
| payments | Payment gateway records |
| delivery_partners | Delivery partner profiles |
| delivery_assignments | Order-to-partner assignment |
| reviews | Reviews for product/shop/delivery |
| notifications | Push notification records |
| complaints | Customer complaints |
| returns | Return requests |
| refunds | Refund processing |
| invoices | Order invoices |
| settlements | Merchant settlement records |
| commissions | Per-order commission tracking |
| audit_logs | Admin action audit trail |
| activity_logs | User activity tracking |
| wishlists | Customer saved products |
| carts | Shopping cart |
| cart_items | Cart line items |

## Running Migrations
```bash
flyway -url=jdbc:postgresql://localhost:5432/nearkart \
       -user=nearkart -password=nearkart123 migrate
```
