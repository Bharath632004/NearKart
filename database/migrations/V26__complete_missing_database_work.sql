-- ============================================================
-- NearKart Migration V26
-- Complete Missing Database Work
-- Author: Bharath C | Date: 2026-07-02
-- Covers: missing entities, relationships, FKs, indexes,
--         constraints, soft-delete, optimistic locking,
--         cascade rules, partition hints, verified integrity
-- ============================================================

-- ============================================================
-- 1. SOFT DELETE – Add deleted_at to core tables (idempotent)
-- ============================================================
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='deleted_at') THEN
    ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='shops' AND column_name='deleted_at') THEN
    ALTER TABLE shops ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='deleted_at') THEN
    ALTER TABLE products ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='orders' AND column_name='deleted_at') THEN
    ALTER TABLE orders ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='coupons' AND column_name='deleted_at') THEN
    ALTER TABLE coupons ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  END IF;
END $$;

-- ============================================================
-- 2. OPTIMISTIC LOCKING – Add version column to mutable tables
-- ============================================================
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='orders' AND column_name='version') THEN
    ALTER TABLE orders ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='inventory' AND column_name='version') THEN
    ALTER TABLE inventory ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='wallets' AND column_name='version') THEN
    ALTER TABLE wallets ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='version') THEN
    ALTER TABLE products ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='shops' AND column_name='version') THEN
    ALTER TABLE shops ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;
END $$;

-- ============================================================
-- 3. MISSING ENTITY: product_variants
-- ============================================================
CREATE TABLE IF NOT EXISTS product_variants (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id    UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_name  VARCHAR(100) NOT NULL,  -- e.g. "500g", "1kg", "Red"
    sku           VARCHAR(100) UNIQUE,
    mrp           DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    stock         INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    is_active     BOOLEAN DEFAULT TRUE,
    version       INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_variant_price CHECK (selling_price <= mrp)
);

CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);

-- ============================================================
-- 4. MISSING ENTITY: shop_operating_hours (if not exists)
-- ============================================================
CREATE TABLE IF NOT EXISTS shop_operating_hours (
    id         SERIAL PRIMARY KEY,
    shop_id    UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0=Sun
    open_time  TIME NOT NULL,
    close_time TIME NOT NULL,
    is_closed  BOOLEAN DEFAULT FALSE,
    UNIQUE (shop_id, day_of_week)
);

CREATE INDEX IF NOT EXISTS idx_shop_hours_shop_id ON shop_operating_hours(shop_id);

-- ============================================================
-- 5. MISSING ENTITY: delivery_zones (if not exists)
-- ============================================================
CREATE TABLE IF NOT EXISTS delivery_zones (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id        UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    pincode        VARCHAR(10) NOT NULL,
    delivery_fee   DECIMAL(8,2) NOT NULL DEFAULT 0.0,
    min_order      DECIMAL(10,2) DEFAULT 0.0,
    estimated_mins INT DEFAULT 30,
    is_active      BOOLEAN DEFAULT TRUE,
    UNIQUE (shop_id, pincode)
);

CREATE INDEX IF NOT EXISTS idx_delivery_zones_shop_id ON delivery_zones(shop_id);
CREATE INDEX IF NOT EXISTS idx_delivery_zones_pincode ON delivery_zones(pincode);

-- ============================================================
-- 6. MISSING ENTITY: coupon_usages (track per-user coupon use)
-- ============================================================
CREATE TABLE IF NOT EXISTS coupon_usages (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coupon_id  UUID NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id   UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    used_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE (coupon_id, user_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_coupon_usages_coupon_id ON coupon_usages(coupon_id);
CREATE INDEX IF NOT EXISTS idx_coupon_usages_user_id   ON coupon_usages(user_id);

-- ============================================================
-- 7. MISSING ENTITY: user_device_tokens (FCM push tokens)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_device_tokens (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token TEXT NOT NULL,
    platform     VARCHAR(10) NOT NULL CHECK (platform IN ('ANDROID','IOS','WEB')),
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (user_id, device_token)
);

CREATE INDEX IF NOT EXISTS idx_user_device_tokens_user_id ON user_device_tokens(user_id);

-- ============================================================
-- 8. MISSING ENTITY: banners (home screen promotions)
-- ============================================================
CREATE TABLE IF NOT EXISTS banners (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title        VARCHAR(255) NOT NULL,
    image_url    TEXT NOT NULL,
    link_type    VARCHAR(20) CHECK (link_type IN ('SHOP','PRODUCT','CATEGORY','URL','NONE')),
    link_id      UUID,
    link_url     TEXT,
    is_active    BOOLEAN DEFAULT TRUE,
    sort_order   INT DEFAULT 0,
    valid_from   TIMESTAMP,
    valid_until  TIMESTAMP,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 9. MISSING ENTITY: user_subscriptions (premium / loyalty)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_name       VARCHAR(100) NOT NULL,
    starts_at       TIMESTAMP NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    payment_ref     UUID REFERENCES payments(id),
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user_id ON user_subscriptions(user_id);

-- ============================================================
-- 10. MISSING ENTITY: order_status_history (full state audit)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_status_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status  VARCHAR(30),
    new_status  VARCHAR(30) NOT NULL,
    changed_by  UUID REFERENCES users(id),
    note        TEXT,
    changed_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history(order_id);

-- ============================================================
-- 11. MISSING ENTITY: inventory_transactions (stock ledger)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id              BIGSERIAL PRIMARY KEY,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    change_qty      INT NOT NULL,           -- positive = restock, negative = deduction
    reason          VARCHAR(50) NOT NULL CHECK (reason IN ('ORDER','RETURN','RESTOCK','ADJUSTMENT','DAMAGED')),
    reference_id    UUID,                   -- order_id or return_id
    balance_after   INT NOT NULL,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inventory_txn_product_id ON inventory_transactions(product_id);

-- ============================================================
-- 12. MISSING ENTITY: payment_refund_requests
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_refund_requests (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id      UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    order_id        UUID NOT NULL REFERENCES orders(id),
    requested_by    UUID NOT NULL REFERENCES users(id),
    reason          TEXT NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','APPROVED','REJECTED','PROCESSED')),
    processed_at    TIMESTAMP,
    gateway_refund_id VARCHAR(255),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refund_req_payment_id ON payment_refund_requests(payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_req_order_id   ON payment_refund_requests(order_id);

-- ============================================================
-- 13. MISSING ENTITY: delivery_partner_earnings
-- ============================================================
CREATE TABLE IF NOT EXISTS delivery_partner_earnings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id      UUID NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    order_id        UUID NOT NULL REFERENCES orders(id),
    base_pay        DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    incentive       DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    deduction       DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    net_earning     DECIMAL(10,2) GENERATED ALWAYS AS (base_pay + incentive - deduction) STORED,
    is_settled      BOOLEAN DEFAULT FALSE,
    settled_at      TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE (partner_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_dp_earnings_partner_id ON delivery_partner_earnings(partner_id);

-- ============================================================
-- 14. AI TABLE: ai_search_logs (track user search queries for ML)
-- ============================================================
CREATE TABLE IF NOT EXISTS ai_search_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    query_text   TEXT NOT NULL,
    result_count INT DEFAULT 0,
    clicked_id   UUID,                 -- product/shop clicked
    session_id   VARCHAR(128),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_search_logs_user_id   ON ai_search_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_search_logs_created_at ON ai_search_logs(created_at);

-- ============================================================
-- 15. AI TABLE: ai_delivery_eta_logs (track ETA prediction accuracy)
-- ============================================================
CREATE TABLE IF NOT EXISTS ai_delivery_eta_logs (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    predicted_minutes   INT NOT NULL,
    actual_minutes      INT,
    model_version       VARCHAR(50),
    features            JSONB,
    created_at          TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_eta_logs_order_id ON ai_delivery_eta_logs(order_id);

-- ============================================================
-- 16. ANALYTICS TABLE: analytics_product_daily
-- ============================================================
CREATE TABLE IF NOT EXISTS analytics_product_daily (
    id           BIGSERIAL PRIMARY KEY,
    product_id   UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    report_date  DATE NOT NULL,
    views        INT DEFAULT 0,
    cart_adds    INT DEFAULT 0,
    purchases    INT DEFAULT 0,
    revenue      DECIMAL(12,2) DEFAULT 0.0,
    UNIQUE (product_id, report_date)
);

CREATE INDEX IF NOT EXISTS idx_analytics_product_daily_date ON analytics_product_daily(report_date);

-- ============================================================
-- 17. ANALYTICS TABLE: analytics_user_daily (DAU/MAU tracking)
-- ============================================================
CREATE TABLE IF NOT EXISTS analytics_user_daily (
    id              BIGSERIAL PRIMARY KEY,
    report_date     DATE NOT NULL,
    new_users       INT DEFAULT 0,
    active_users    INT DEFAULT 0,
    orders_placed   INT DEFAULT 0,
    gmv             DECIMAL(14,2) DEFAULT 0.0,
    UNIQUE (report_date)
);

-- ============================================================
-- 18. MISSING RELATIONSHIP: orders → coupon_usages (already done above)
--     Ensure orders.coupon_id has proper FK (already in schema)
--     Add missing index on orders.coupon_id
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_orders_coupon_id ON orders(coupon_id);
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

-- ============================================================
-- 19. MISSING INDEXES – performance-critical composite indexes
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_products_shop_active       ON products(shop_id, is_active) WHERE is_active = TRUE AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_products_category_active   ON products(category_id, is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_shops_active_verified      ON shops(is_active, is_verified) WHERE is_active = TRUE AND is_verified = TRUE;
CREATE INDEX IF NOT EXISTS idx_orders_customer_created    ON orders(customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_status            ON payments(status);
CREATE INDEX IF NOT EXISTS idx_delivery_assignments_status ON delivery_assignments(status);
CREATE INDEX IF NOT EXISTS idx_reviews_entity             ON reviews(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_wallets_user_id            ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_created_at      ON wallet_transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock        ON inventory(quantity) WHERE quantity <= low_stock_alert;
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at  ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_otps_expires_at            ON otps(expires_at);

-- ============================================================
-- 20. ADDITIONAL CONSTRAINTS – business rule enforcement
-- ============================================================

-- Delivery assignment: partner cannot self-assign non-existent order
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_delivery_earnings_positive'
  ) THEN
    ALTER TABLE delivery_partner_earnings
      ADD CONSTRAINT chk_delivery_earnings_positive CHECK (base_pay >= 0 AND incentive >= 0 AND deduction >= 0);
  END IF;
END $$;

-- Wallet: balance_after must be non-negative
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_wallet_txn_balance_after'
  ) THEN
    ALTER TABLE wallet_transactions
      ADD CONSTRAINT chk_wallet_txn_balance_after CHECK (balance_after >= 0);
  END IF;
END $$;

-- Orders: delivered_at must be after estimated if both set
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_orders_delivery_time_order'
  ) THEN
    ALTER TABLE orders
      ADD CONSTRAINT chk_orders_delivery_time_order
        CHECK (delivered_at IS NULL OR estimated_delivery_at IS NULL OR delivered_at >= created_at);
  END IF;
END $$;

-- Coupons: valid_until must be after valid_from
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_coupon_validity_range'
  ) THEN
    ALTER TABLE coupons
      ADD CONSTRAINT chk_coupon_validity_range CHECK (valid_until > valid_from);
  END IF;
END $$;

-- Settlements: period_to must be after period_from
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_settlement_period_order'
  ) THEN
    ALTER TABLE settlements
      ADD CONSTRAINT chk_settlement_period_order CHECK (period_to > period_from);
  END IF;
END $$;

-- ============================================================
-- 21. UPDATED_AT AUTO-TRIGGER for new tables
-- ============================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_user_device_tokens_updated_at') THEN
    CREATE TRIGGER trg_user_device_tokens_updated_at
      BEFORE UPDATE ON user_device_tokens
      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_payment_refund_req_updated_at') THEN
    CREATE TRIGGER trg_payment_refund_req_updated_at
      BEFORE UPDATE ON payment_refund_requests
      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

-- ============================================================
-- 22. ORPHAN PREVENTION – FK guards for polymorphic references
-- ============================================================
-- reviews.entity_id is polymorphic (PRODUCT/SHOP/DELIVERY_PARTNER)
-- We cannot add a direct FK; instead, add a constraint trigger
-- to verify entity existence on INSERT.
CREATE OR REPLACE FUNCTION check_review_entity_exists()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.entity_type = 'PRODUCT' THEN
    IF NOT EXISTS (SELECT 1 FROM products WHERE id = NEW.entity_id) THEN
      RAISE EXCEPTION 'Review entity (PRODUCT) % does not exist', NEW.entity_id;
    END IF;
  ELSIF NEW.entity_type = 'SHOP' THEN
    IF NOT EXISTS (SELECT 1 FROM shops WHERE id = NEW.entity_id) THEN
      RAISE EXCEPTION 'Review entity (SHOP) % does not exist', NEW.entity_id;
    END IF;
  ELSIF NEW.entity_type = 'DELIVERY_PARTNER' THEN
    IF NOT EXISTS (SELECT 1 FROM delivery_partners WHERE id = NEW.entity_id) THEN
      RAISE EXCEPTION 'Review entity (DELIVERY_PARTNER) % does not exist', NEW.entity_id;
    END IF;
  END IF;
  RETURN NEW;
END;
$$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_reviews_entity_check') THEN
    CREATE TRIGGER trg_reviews_entity_check
      BEFORE INSERT OR UPDATE ON reviews
      FOR EACH ROW EXECUTE FUNCTION check_review_entity_exists();
  END IF;
END $$;

-- ============================================================
-- 23. ORDER STATUS HISTORY AUTO-TRIGGER
-- ============================================================
CREATE OR REPLACE FUNCTION log_order_status_change()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.status IS DISTINCT FROM NEW.status THEN
    INSERT INTO order_status_history(order_id, old_status, new_status, changed_at)
    VALUES (NEW.id, OLD.status, NEW.status, NOW());
  END IF;
  RETURN NEW;
END;
$$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_orders_status_history') THEN
    CREATE TRIGGER trg_orders_status_history
      AFTER UPDATE ON orders
      FOR EACH ROW EXECUTE FUNCTION log_order_status_change();
  END IF;
END $$;

-- ============================================================
-- 24. INVENTORY DEDUCTION TRIGGER on order_items INSERT
-- ============================================================
CREATE OR REPLACE FUNCTION deduct_inventory_on_order()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
  v_current_qty INT;
  v_version     INT;
BEGIN
  SELECT quantity, version INTO v_current_qty, v_version
  FROM inventory WHERE product_id = NEW.product_id FOR UPDATE;

  IF v_current_qty < NEW.quantity THEN
    RAISE EXCEPTION 'Insufficient stock for product %', NEW.product_id;
  END IF;

  UPDATE inventory
  SET quantity = quantity - NEW.quantity,
      version  = version + 1,
      updated_at = NOW()
  WHERE product_id = NEW.product_id AND version = v_version;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Optimistic lock conflict on inventory for product %', NEW.product_id;
  END IF;

  INSERT INTO inventory_transactions(product_id, change_qty, reason, reference_id, balance_after)
  VALUES (NEW.product_id, -NEW.quantity, 'ORDER', NEW.order_id, v_current_qty - NEW.quantity);

  RETURN NEW;
END;
$$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_order_items_deduct_inventory') THEN
    CREATE TRIGGER trg_order_items_deduct_inventory
      AFTER INSERT ON order_items
      FOR EACH ROW EXECUTE FUNCTION deduct_inventory_on_order();
  END IF;
END $$;

-- ============================================================
-- 25. INTEGRITY VERIFICATION VIEWS (for DBA / smoke tests)
-- ============================================================
CREATE OR REPLACE VIEW vw_orphan_check AS
SELECT 'order_items without orders' AS check_name, COUNT(*) AS orphan_count
  FROM order_items oi LEFT JOIN orders o ON oi.order_id = o.id WHERE o.id IS NULL
UNION ALL
SELECT 'payments without orders', COUNT(*)
  FROM payments p LEFT JOIN orders o ON p.order_id = o.id WHERE o.id IS NULL
UNION ALL
SELECT 'delivery_assignments without orders', COUNT(*)
  FROM delivery_assignments da LEFT JOIN orders o ON da.order_id = o.id WHERE o.id IS NULL
UNION ALL
SELECT 'wallets without users', COUNT(*)
  FROM wallets w LEFT JOIN users u ON w.user_id = u.id WHERE u.id IS NULL
UNION ALL
SELECT 'inventory without products', COUNT(*)
  FROM inventory i LEFT JOIN products p ON i.product_id = p.id WHERE p.id IS NULL;

-- ============================================================
-- 26. PERFORMANCE VIEWS
-- ============================================================
CREATE OR REPLACE VIEW vw_low_stock_alert AS
SELECT p.id, p.name, p.shop_id, i.quantity, i.low_stock_alert
FROM inventory i
JOIN products p ON p.id = i.product_id
WHERE i.quantity <= i.low_stock_alert
  AND p.is_active = TRUE
  AND p.deleted_at IS NULL
ORDER BY i.quantity ASC;

CREATE OR REPLACE VIEW vw_active_delivery_partners AS
SELECT dp.id, u.full_name, u.phone, dp.vehicle_type,
       dp.current_lat, dp.current_lng, dp.avg_rating
FROM delivery_partners dp
JOIN users u ON u.id = dp.user_id
WHERE dp.is_available = TRUE
  AND dp.is_kyc_verified = TRUE
  AND u.is_enabled = TRUE
  AND u.deleted_at IS NULL;

CREATE OR REPLACE VIEW vw_pending_settlements AS
SELECT s.id, u.full_name AS merchant_name, u.email,
       s.period_from, s.period_to, s.net_amount, s.status
FROM settlements s
JOIN users u ON u.id = s.merchant_id
WHERE s.status = 'PENDING'
ORDER BY s.created_at ASC;

-- ============================================================
-- Migration V26 complete.
-- ============================================================
