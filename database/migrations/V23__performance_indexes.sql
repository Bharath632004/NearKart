-- ============================================================
-- V23: Performance Optimization - Composite & Partial Indexes
-- Author: Bharath C | NearKart DB Production Readiness
-- ============================================================

-- ------------------------------------------------------------
-- ORDERS: High-traffic query patterns
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_orders_customer_status
    ON orders(customer_id, status);

CREATE INDEX IF NOT EXISTS idx_orders_shop_status_date
    ON orders(shop_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_orders_created_at
    ON orders(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_orders_payment_status
    ON orders(payment_status) WHERE payment_status = 'PENDING';

-- ------------------------------------------------------------
-- PRODUCTS: Listing & filtering
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_products_shop_active
    ON products(shop_id, is_active) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_products_category_active
    ON products(category_id, is_active) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_products_price_range
    ON products(selling_price) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_products_featured
    ON products(is_featured, shop_id) WHERE is_featured = TRUE;

-- ------------------------------------------------------------
-- DELIVERY ASSIGNMENTS: Status-based routing
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_da_partner_status
    ON delivery_assignments(partner_id, status);

CREATE INDEX IF NOT EXISTS idx_da_status_assigned_at
    ON delivery_assignments(status, assigned_at)
    WHERE status IN ('ASSIGNED', 'ACCEPTED');

-- ------------------------------------------------------------
-- NOTIFICATIONS: Efficient unread + type queries
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_notif_user_type
    ON notifications(user_id, type, created_at DESC);

-- ------------------------------------------------------------
-- WALLET TRANSACTIONS: Ledger queries
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_wallet_txn_type_date
    ON wallet_transactions(wallet_id, type, created_at DESC);

-- ------------------------------------------------------------
-- REVIEWS: Entity lookup
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_reviews_entity_date
    ON reviews(entity_type, entity_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- ------------------------------------------------------------
-- AUDIT LOGS: Time-based cleanup & monitoring
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_audit_logs_time
    ON audit_logs(created_at DESC);

-- ------------------------------------------------------------
-- SHOPS: Active + verified filter (most common query)
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_shops_active_verified
    ON shops(is_active, is_verified) WHERE is_active = TRUE AND is_verified = TRUE;

-- ------------------------------------------------------------
-- PAYMENTS: Gateway ID lookup
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_payments_gateway_ids
    ON payments(razorpay_order_id, razorpay_payment_id);

-- ------------------------------------------------------------
-- SEARCH HISTORY: Popular queries analytics
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_search_popular
    ON search_history(query, created_at DESC);

-- ------------------------------------------------------------
-- Partitioning documentation comments
-- ------------------------------------------------------------
COMMENT ON TABLE audit_logs IS
    'PARTITION RECOMMENDATION: RANGE(created_at) when rows > 10M. Monthly partitions advised.';

COMMENT ON TABLE activity_logs IS
    'PARTITION RECOMMENDATION: RANGE(created_at) when rows > 10M. Retain 90 days, archive older.';

COMMENT ON TABLE delivery_live_location IS
    'Already partitioned by month. Add new monthly partition via pg_cron 1st of each month.';

COMMENT ON TABLE order_status_history IS
    'Immutable audit log. Archive orders older than 2 years to cold storage.';
