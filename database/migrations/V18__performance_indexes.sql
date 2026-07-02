-- ============================================================
-- NearKart Migration V18: Performance & Covering Indexes
-- Author: Bharath C | Version: 1.0
-- ============================================================

-- -------------------------------------------------------
-- ORDERS: composite indexes for common access patterns
-- -------------------------------------------------------
-- Customer order history paginated by date
CREATE INDEX IF NOT EXISTS idx_orders_customer_status_date
    ON orders(customer_id, status, created_at DESC)
    WHERE deleted_at IS NULL;

-- Merchant / shop order dashboard
CREATE INDEX IF NOT EXISTS idx_orders_shop_status_date
    ON orders(shop_id, status, created_at DESC)
    WHERE deleted_at IS NULL;

-- Covering index for order list API (avoids heap fetch)
CREATE INDEX IF NOT EXISTS idx_orders_covering
    ON orders(customer_id, created_at DESC)
    INCLUDE (id, status, total_amount, payment_status)
    WHERE deleted_at IS NULL;

-- -------------------------------------------------------
-- PRODUCTS: search & catalogue
-- -------------------------------------------------------
-- Full-text search on name + description
CREATE INDEX IF NOT EXISTS idx_products_fts
    ON products USING GIN(
        to_tsvector('english', name || ' ' || COALESCE(description,''))
    )
    WHERE deleted_at IS NULL;

-- Price range filter (category browse)
CREATE INDEX IF NOT EXISTS idx_products_category_price
    ON products(category_id, selling_price)
    WHERE is_active = TRUE AND deleted_at IS NULL;

-- Featured products per shop
CREATE INDEX IF NOT EXISTS idx_products_shop_featured
    ON products(shop_id, is_featured, avg_rating DESC)
    WHERE is_active = TRUE AND deleted_at IS NULL;

-- -------------------------------------------------------
-- SHOPS: geo-search
-- -------------------------------------------------------
-- Shops within pincode that are open
CREATE INDEX IF NOT EXISTS idx_shops_pincode_open
    ON shops(pincode, is_active, is_open)
    WHERE deleted_at IS NULL;

-- Category + active shops
CREATE INDEX IF NOT EXISTS idx_shops_category_active
    ON shops(category_id, avg_rating DESC)
    WHERE is_active = TRUE AND deleted_at IS NULL;

-- -------------------------------------------------------
-- DELIVERY ASSIGNMENTS: partner workload
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_da_partner_status
    ON delivery_assignments(partner_id, status)
    WHERE status NOT IN ('DELIVERED','REJECTED');

-- -------------------------------------------------------
-- PAYMENTS: gateway reconciliation
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_payments_gateway_id
    ON payments(gateway_payment_id)
    WHERE gateway_payment_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payments_status_date
    ON payments(status, created_at DESC);

-- -------------------------------------------------------
-- WALLET TRANSACTIONS: balance history
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_wtxn_wallet_type_date
    ON wallet_transactions(wallet_id, type, created_at DESC);

-- -------------------------------------------------------
-- REVIEWS: entity lookups
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_reviews_entity
    ON reviews(entity_type, entity_id, rating DESC);

-- -------------------------------------------------------
-- NOTIFICATIONS: unread count per user
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_notif_user_unread_date
    ON notifications(user_id, created_at DESC)
    WHERE is_read = FALSE;

-- -------------------------------------------------------
-- AUDIT LOGS: time-range admin queries
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_audit_created_action
    ON audit_logs(created_at DESC, action);

-- -------------------------------------------------------
-- AI RECOMMENDATIONS: expire-aware
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_ai_rec_valid
    ON ai_product_recommendations(user_id, score DESC)
    WHERE expires_at > NOW();

-- -------------------------------------------------------
-- COUPON USAGE: validate per-user limit quickly
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_cu_coupon_user
    ON coupon_usages(coupon_id, user_id);

-- -------------------------------------------------------
-- INVENTORY: low-stock alert scan
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock
    ON inventory(product_id)
    WHERE quantity <= low_stock_alert;

-- -------------------------------------------------------
-- USER DEVICE TOKENS: active per user
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_udt_user_active
    ON user_device_tokens(user_id)
    WHERE is_active = TRUE;

-- -------------------------------------------------------
-- BRIN index on large append-only tables for time ranges
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_audit_brin_created  ON audit_logs    USING BRIN(created_at);
CREATE INDEX IF NOT EXISTS idx_activity_brin       ON activity_logs USING BRIN(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_ue_brin_created  ON ai_user_events USING BRIN(created_at);
