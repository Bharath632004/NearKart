-- ============================================================
-- NearKart Flyway Migration V2
-- Additional performance indexes for high-traffic queries
-- ============================================================

-- Full-text search on shop names
CREATE INDEX IF NOT EXISTS idx_shops_name_fts ON shops USING GIN(to_tsvector('english', name));

-- Orders by date (analytics queries)
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

-- Delivery partner availability lookup
CREATE INDEX IF NOT EXISTS idx_delivery_available ON delivery_partners(is_available, is_kyc_verified);

-- Notifications unread count
CREATE INDEX IF NOT EXISTS idx_notif_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- Coupons validity check
CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(is_active, valid_from, valid_until);

-- Product search by barcode/sku
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode) WHERE barcode IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku) WHERE sku IS NOT NULL;
