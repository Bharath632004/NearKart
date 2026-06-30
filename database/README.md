# NearKart Database Design

## Database: PostgreSQL 16 + PostGIS

## Schema Files

| File | Tables |
|---|---|
| `V1__create_extensions.sql` | UUID, PostGIS, pg_trgm extensions |
| `V2__create_auth_tables.sql` | users, roles, permissions, otp_records, refresh_tokens, sessions |
| `V3__create_address_tables.sql` | addresses |
| `V4__create_shop_tables.sql` | shops, shop_categories |
| `V5__create_product_tables.sql` | brands, categories, subcategories, products, product_images |
| `V6__create_inventory_tables.sql` | inventory |
| `V7__create_order_tables.sql` | orders, order_items, coupons, offers |
| `V8__create_payment_tables.sql` | payments, transactions, wallet, wallet_transactions, invoices, settlements, commission |
| `V9__create_delivery_tables.sql` | delivery_partners, delivery_assignments |
| `V10__create_engagement_tables.sql` | reviews, ratings, notifications, complaints, returns, refunds |
| `V11__create_log_tables.sql` | audit_logs, activity_logs |
| `V12__seed_data.sql` | Initial seed/reference data |

## Total Tables: 35

## Naming Conventions
- All table names: `snake_case`, plural
- All column names: `snake_case`
- Primary keys: `id` (UUID or BIGSERIAL)
- Foreign keys: `{referenced_table_singular}_id`
- Timestamps: `created_at`, `updated_at`, `deleted_at`
- Soft deletes: `is_deleted BOOLEAN DEFAULT FALSE`
- Boolean flags: `is_*` prefix

## Run Migrations (Flyway)
```bash
mvn flyway:migrate
```

## Run Manually
```bash
psql -U nearkart -d nearkart_db -f migrations/V1__create_extensions.sql
psql -U nearkart -d nearkart_db -f migrations/V2__create_auth_tables.sql
# ... continue in order
```
