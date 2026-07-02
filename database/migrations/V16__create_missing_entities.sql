-- ============================================================
-- NearKart Migration V16: Missing Entities & Relationships
-- Author: Bharath C | Version: 1.0
-- ============================================================

-- -------------------------------------------------------
-- 1. COUPON USAGE TRACKING  (per-user usage enforcement)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS coupon_usages (
    id          UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    coupon_id   UUID      NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    user_id     UUID      NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    order_id    UUID      NOT NULL REFERENCES orders(id)  ON DELETE CASCADE,
    used_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (coupon_id, user_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_cu_coupon_id ON coupon_usages(coupon_id);
CREATE INDEX IF NOT EXISTS idx_cu_user_id   ON coupon_usages(user_id);

-- -------------------------------------------------------
-- 2. PRODUCT TAGS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS tags (
    id      SERIAL      PRIMARY KEY,
    name    VARCHAR(80) UNIQUE NOT NULL,
    slug    VARCHAR(80) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS product_tags (
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag_id      INT  NOT NULL REFERENCES tags(id)     ON DELETE CASCADE,
    PRIMARY KEY (product_id, tag_id)
);

-- -------------------------------------------------------
-- 3. SHOP OPERATING HOURS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS shop_operating_hours (
    id          UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id     UUID    NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),  -- 0=Sun
    open_time   TIME    NOT NULL,
    close_time  TIME    NOT NULL,
    is_closed   BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (shop_id, day_of_week),
    CONSTRAINT chk_hours_order CHECK (close_time > open_time OR is_closed)
);

CREATE INDEX IF NOT EXISTS idx_soh_shop_id ON shop_operating_hours(shop_id);

-- -------------------------------------------------------
-- 4. DELIVERY ZONES
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_zones (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id         UUID        NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    zone_name       VARCHAR(100) NOT NULL,
    pincode         VARCHAR(10) NOT NULL,
    delivery_charge DECIMAL(8,2) NOT NULL DEFAULT 0,
    min_order_value DECIMAL(10,2) NOT NULL DEFAULT 0,
    est_minutes_min INT,
    est_minutes_max INT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (shop_id, pincode)
);

CREATE INDEX IF NOT EXISTS idx_dz_shop_pincode ON delivery_zones(shop_id, pincode);

-- -------------------------------------------------------
-- 5. BANNER ADS / PROMOTIONS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS banners (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    title        VARCHAR(255) NOT NULL,
    image_url    TEXT        NOT NULL,
    link_type    VARCHAR(20) CHECK (link_type IN ('SHOP','PRODUCT','CATEGORY','EXTERNAL')),
    link_id      UUID,
    link_url     TEXT,
    placement    VARCHAR(30) NOT NULL DEFAULT 'HOME_TOP',
    sort_order   INT         NOT NULL DEFAULT 0,
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    valid_from   TIMESTAMP   NOT NULL DEFAULT NOW(),
    valid_until  TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_banners_placement ON banners(placement, sort_order) WHERE is_active = TRUE;

-- -------------------------------------------------------
-- 6. SUBSCRIPTION / LOYALTY PLANS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS subscription_plans (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(100) UNIQUE NOT NULL,
    description     TEXT,
    price           DECIMAL(10,2) NOT NULL,
    duration_days   INT         NOT NULL,
    free_deliveries INT         NOT NULL DEFAULT 0,
    extra_discount  DECIMAL(5,2) NOT NULL DEFAULT 0,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_subscriptions (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id         UUID    NOT NULL REFERENCES subscription_plans(id),
    started_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP NOT NULL,
    is_active       BOOLEAN   NOT NULL DEFAULT TRUE,
    payment_id      UUID      REFERENCES payments(id),
    UNIQUE (user_id, plan_id, started_at)
);

CREATE INDEX IF NOT EXISTS idx_us_user_id    ON user_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_us_expires    ON user_subscriptions(expires_at) WHERE is_active = TRUE;

-- -------------------------------------------------------
-- 7. USER DEVICE TOKENS  (push notifications)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_device_tokens (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token TEXT        NOT NULL,
    platform     VARCHAR(10) NOT NULL CHECK (platform IN ('ANDROID','IOS','WEB')),
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (device_token)
);

CREATE INDEX IF NOT EXISTS idx_udt_user_id ON user_device_tokens(user_id);
