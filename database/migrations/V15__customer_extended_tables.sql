-- ============================================================
-- V15: Customer Extended Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: loyalty_points, loyalty_transactions, referral_history,
--         recently_viewed_products, search_history, coupon_usage
-- ============================================================

-- ------------------------------------------------------------
-- Loyalty Points (balance per user)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS loyalty_points (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance         INT  NOT NULL DEFAULT 0 CHECK (balance >= 0),
    lifetime_earned INT  NOT NULL DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_loyalty_user ON loyalty_points(user_id);

CREATE TRIGGER trg_loyalty_points_updated_at
    BEFORE UPDATE ON loyalty_points
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Loyalty Transactions (earn/redeem log)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points         INT  NOT NULL,
    type           VARCHAR(10) NOT NULL CHECK (type IN ('EARN','REDEEM','EXPIRE','ADJUST')),
    reference_type VARCHAR(50),
    reference_id   UUID,
    description    TEXT,
    expires_at     TIMESTAMP,
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_loyalty_txn_user   ON loyalty_transactions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_loyalty_txn_type   ON loyalty_transactions(type);

-- ------------------------------------------------------------
-- Referral History
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS referral_history (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    referrer_id   UUID NOT NULL REFERENCES users(id),
    referee_id    UUID NOT NULL REFERENCES users(id),
    referral_code VARCHAR(20) NOT NULL,
    reward_given  BOOLEAN     DEFAULT FALSE,
    reward_amount DECIMAL(10,2),
    created_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE (referee_id)
);

CREATE INDEX IF NOT EXISTS idx_referral_referrer ON referral_history(referrer_id);

-- ------------------------------------------------------------
-- Recently Viewed Products
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS recently_viewed_products (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    viewed_at  TIMESTAMP DEFAULT NOW(),
    UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_recently_viewed_user ON recently_viewed_products(user_id, viewed_at DESC);

-- ------------------------------------------------------------
-- Search History
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS search_history (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID REFERENCES users(id) ON DELETE CASCADE,
    query        VARCHAR(255) NOT NULL,
    result_count INT  DEFAULT 0,
    session_id   VARCHAR(100),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_search_history_user  ON search_history(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_search_history_query ON search_history USING GIN(to_tsvector('english', query));

-- ------------------------------------------------------------
-- Coupon Usage (per-user per-order tracking)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS coupon_usage (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coupon_id UUID NOT NULL REFERENCES coupons(id),
    user_id   UUID NOT NULL REFERENCES users(id),
    order_id  UUID NOT NULL REFERENCES orders(id),
    used_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (coupon_id, user_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_coupon_usage_user   ON coupon_usage(user_id);
CREATE INDEX IF NOT EXISTS idx_coupon_usage_coupon ON coupon_usage(coupon_id);
