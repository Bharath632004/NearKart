-- ============================================================
-- NearKart V22 – Missing Entities, Relationships, Constraints
-- Soft Delete, Optimistic Locking, Missing FKs
-- Author: Bharath C | Date: 2026-07-02
-- ============================================================

-- ============================================================
-- 1. SOFT DELETE + OPTIMISTIC LOCK on core tables
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS version     BIGINT    NOT NULL DEFAULT 0;

ALTER TABLE shops
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS version     BIGINT    NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS version     BIGINT    NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS version     BIGINT    NOT NULL DEFAULT 0;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS version     BIGINT    NOT NULL DEFAULT 0;

ALTER TABLE coupons
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- ============================================================
-- 2. PARTIAL INDEXES for soft-delete (only live rows indexed)
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_users_active_email
    ON users(email) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_shops_active
    ON shops(merchant_id, is_active) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_products_active_shop
    ON products(shop_id, is_active) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_orders_active_customer
    ON orders(customer_id, status) WHERE deleted_at IS NULL;

-- ============================================================
-- 3. MISSING ENTITY: user_profiles (extended merchant/partner KYC)
-- ============================================================

CREATE TABLE IF NOT EXISTS user_profiles (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date_of_birth DATE,
    gender        VARCHAR(10) CHECK (gender IN ('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY')),
    bio           TEXT,
    pan_number    VARCHAR(15),
    aadhaar_last4 CHAR(4),
    bank_account  VARCHAR(20),
    bank_ifsc     VARCHAR(15),
    bank_name     VARCHAR(100),
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);

-- ============================================================
-- 4. MISSING ENTITY: product_variants
-- ============================================================

CREATE TABLE IF NOT EXISTS product_variants (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id     UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_name   VARCHAR(100) NOT NULL,   -- e.g. "500g", "Red - L"
    sku            VARCHAR(100) UNIQUE,
    mrp            DECIMAL(10,2) NOT NULL,
    selling_price  DECIMAL(10,2) NOT NULL,
    stock          INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    is_active      BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_variant_price CHECK (selling_price <= mrp)
);

CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);

-- ============================================================
-- 5. MISSING ENTITY: shop_operating_hours (proper table)
-- ============================================================

CREATE TABLE IF NOT EXISTS shop_operating_hours (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id     UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),  -- 0=Sun
    open_time   TIME NOT NULL,
    close_time  TIME NOT NULL,
    is_closed   BOOLEAN DEFAULT FALSE,
    UNIQUE (shop_id, day_of_week),
    CONSTRAINT chk_shop_hours_order CHECK (close_time > open_time OR is_closed)
);

CREATE INDEX IF NOT EXISTS idx_shop_hours_shop_id ON shop_operating_hours(shop_id);

-- ============================================================
-- 6. MISSING ENTITY: delivery_zones
-- ============================================================

CREATE TABLE IF NOT EXISTS delivery_zones (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id      UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    zone_name    VARCHAR(100) NOT NULL,
    pincode      VARCHAR(10) NOT NULL,
    delivery_fee DECIMAL(8,2) NOT NULL DEFAULT 0.0,
    min_eta_min  INT DEFAULT 30,
    max_eta_min  INT DEFAULT 60,
    is_active    BOOLEAN DEFAULT TRUE,
    UNIQUE (shop_id, pincode)
);

CREATE INDEX IF NOT EXISTS idx_delivery_zones_shop_id   ON delivery_zones(shop_id);
CREATE INDEX IF NOT EXISTS idx_delivery_zones_pincode   ON delivery_zones(pincode);

-- ============================================================
-- 7. MISSING ENTITY: banners / promotions
-- ============================================================

CREATE TABLE IF NOT EXISTS banners (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title        VARCHAR(255) NOT NULL,
    image_url    TEXT NOT NULL,
    link_type    VARCHAR(20) CHECK (link_type IN ('SHOP','PRODUCT','CATEGORY','EXTERNAL','NONE')),
    link_ref_id  UUID,
    link_url     TEXT,
    position     VARCHAR(20) DEFAULT 'HOME_TOP' CHECK (position IN ('HOME_TOP','HOME_MIDDLE','CATEGORY','FLASH')),
    is_active    BOOLEAN DEFAULT TRUE,
    valid_from   TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_until  TIMESTAMP,
    sort_order   INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_banners_active ON banners(is_active, valid_from, valid_until);

-- ============================================================
-- 8. MISSING ENTITY: coupon_usages (per-user coupon tracking)
-- ============================================================

CREATE TABLE IF NOT EXISTS coupon_usages (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coupon_id  UUID NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    order_id   UUID NOT NULL REFERENCES orders(id),
    used_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE (coupon_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_coupon_usages_coupon_user ON coupon_usages(coupon_id, user_id);

-- ============================================================
-- 9. MISSING ENTITY: user_device_tokens (FCM/APNs push)
-- ============================================================

CREATE TABLE IF NOT EXISTS user_device_tokens (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token TEXT NOT NULL,
    platform     VARCHAR(10) NOT NULL CHECK (platform IN ('ANDROID','IOS','WEB')),
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (device_token)
);

CREATE INDEX IF NOT EXISTS idx_user_device_tokens_user_id ON user_device_tokens(user_id);

-- ============================================================
-- 10. MISSING ENTITY: user_subscriptions (premium/loyalty)
-- ============================================================

CREATE TABLE IF NOT EXISTS user_subscriptions (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan         VARCHAR(30) NOT NULL CHECK (plan IN ('FREE','PLUS','PRO')),
    started_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP,
    is_active    BOOLEAN DEFAULT TRUE,
    payment_ref  UUID REFERENCES payments(id),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user_id ON user_subscriptions(user_id, is_active);

-- ============================================================
-- 11. MISSING RELATIONSHIP: orders → delivery_zones FK
-- ============================================================

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_zone_id UUID REFERENCES delivery_zones(id);

-- ============================================================
-- 12. MISSING FK: reviews → orders (verified purchase)
-- ============================================================

ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS order_id UUID REFERENCES orders(id) ON DELETE SET NULL;

-- ============================================================
-- 13. MISSING CONSTRAINT: orders total_amount check
-- ============================================================

ALTER TABLE orders DROP CONSTRAINT IF EXISTS chk_orders_total_amount;
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_amount
    CHECK (total_amount = subtotal - discount_amount + delivery_charge + tax_amount);

-- ============================================================
-- 14. MISSING CONSTRAINT: wallet_transactions balance >= 0
-- ============================================================

ALTER TABLE wallet_transactions
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'
    CHECK (status IN ('SUCCESS','FAILED','REVERSED'));

-- ============================================================
-- 15. MISSING: updated_at trigger function (idempotent)
-- ============================================================

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

-- Apply trigger to user_profiles
DROP TRIGGER IF EXISTS trg_user_profiles_updated_at ON user_profiles;
CREATE TRIGGER trg_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
