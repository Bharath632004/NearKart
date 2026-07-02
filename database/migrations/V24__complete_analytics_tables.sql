-- ============================================================
-- NearKart V24 – Complete Analytics Tables
-- Platform / Shop / Product / Funnel / Partner Analytics
-- Author: Bharath C | Date: 2026-07-02
-- ============================================================

-- ============================================================
-- 1. PLATFORM DAILY METRICS (already exists – extend columns)
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_platform_daily (
    id                   BIGSERIAL PRIMARY KEY,
    metric_date          DATE UNIQUE NOT NULL,
    new_users            INT  DEFAULT 0,
    active_users         INT  DEFAULT 0,
    new_merchants        INT  DEFAULT 0,
    orders_placed        INT  DEFAULT 0,
    orders_delivered     INT  DEFAULT 0,
    orders_cancelled     INT  DEFAULT 0,
    gross_merchandise_value DECIMAL(14,2) DEFAULT 0.0,
    total_commission     DECIMAL(12,2) DEFAULT 0.0,
    avg_delivery_min     DECIMAL(8,2),
    created_at           TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_platform_date ON analytics_platform_daily(metric_date DESC);

-- ============================================================
-- 2. PRODUCT ANALYTICS (views, cart adds, conversions)
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_product_daily (
    id             BIGSERIAL PRIMARY KEY,
    metric_date    DATE NOT NULL,
    product_id     UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    views          INT  DEFAULT 0,
    cart_adds      INT  DEFAULT 0,
    purchases      INT  DEFAULT 0,
    revenue        DECIMAL(12,2) DEFAULT 0.0,
    UNIQUE (metric_date, product_id)
);

CREATE INDEX IF NOT EXISTS idx_analytics_product_date   ON analytics_product_daily(metric_date DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_product_id     ON analytics_product_daily(product_id);

-- ============================================================
-- 3. SEARCH ANALYTICS
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_search_log (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID REFERENCES users(id),
    query           TEXT NOT NULL,
    result_count    INT  DEFAULT 0,
    clicked_id      UUID,
    clicked_type    VARCHAR(20) CHECK (clicked_type IN ('PRODUCT','SHOP','CATEGORY')),
    session_id      UUID,
    searched_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_search_date ON analytics_search_log(searched_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_search_user ON analytics_search_log(user_id);

-- ============================================================
-- 4. FUNNEL / USER JOURNEY EVENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_events (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id),
    session_id  UUID,
    event_name  VARCHAR(100) NOT NULL,   -- e.g. 'PRODUCT_VIEW', 'ADD_TO_CART'
    entity_type VARCHAR(30),
    entity_id   UUID,
    properties  JSONB,
    platform    VARCHAR(10) CHECK (platform IN ('ANDROID','IOS','WEB')),
    occurred_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_events_name   ON analytics_events(event_name);
CREATE INDEX IF NOT EXISTS idx_analytics_events_user   ON analytics_events(user_id);
CREATE INDEX IF NOT EXISTS idx_analytics_events_date   ON analytics_events(occurred_at DESC);

-- ============================================================
-- 5. DELIVERY PARTNER PERFORMANCE
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_partner_daily (
    id                  BIGSERIAL PRIMARY KEY,
    metric_date         DATE NOT NULL,
    partner_id          UUID NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    deliveries_done     INT  DEFAULT 0,
    deliveries_rejected INT  DEFAULT 0,
    total_distance_km   DECIMAL(10,2) DEFAULT 0.0,
    total_earnings      DECIMAL(10,2) DEFAULT 0.0,
    avg_rating          DECIMAL(3,2),
    online_minutes      INT  DEFAULT 0,
    UNIQUE (metric_date, partner_id)
);

CREATE INDEX IF NOT EXISTS idx_analytics_partner_date   ON analytics_partner_daily(metric_date DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_partner_id     ON analytics_partner_daily(partner_id);

-- ============================================================
-- 6. COUPON ANALYTICS
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_coupon_performance (
    id               BIGSERIAL PRIMARY KEY,
    coupon_id        UUID NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    metric_date      DATE NOT NULL,
    times_applied    INT  DEFAULT 0,
    discount_granted DECIMAL(12,2) DEFAULT 0.0,
    orders_count     INT  DEFAULT 0,
    UNIQUE (coupon_id, metric_date)
);

CREATE INDEX IF NOT EXISTS idx_analytics_coupon_date ON analytics_coupon_performance(metric_date DESC);

-- ============================================================
-- 7. REVENUE SUMMARY (materialized-ready view placeholder)
-- ============================================================

CREATE TABLE IF NOT EXISTS analytics_revenue_daily (
    id                 BIGSERIAL PRIMARY KEY,
    metric_date        DATE UNIQUE NOT NULL,
    gross_revenue      DECIMAL(14,2) DEFAULT 0.0,
    platform_fee       DECIMAL(12,2) DEFAULT 0.0,
    delivery_revenue   DECIMAL(12,2) DEFAULT 0.0,
    refund_amount      DECIMAL(12,2) DEFAULT 0.0,
    net_revenue        DECIMAL(14,2) GENERATED ALWAYS AS
                           (gross_revenue - refund_amount) STORED,
    created_at         TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_revenue_date ON analytics_revenue_daily(metric_date DESC);
