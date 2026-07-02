-- ============================================================
-- V21: Analytics Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: daily_revenue, product_performance, customer_analytics,
--         delivery_analytics, platform_statistics
-- ============================================================

-- ------------------------------------------------------------
-- Daily Revenue Snapshot (populated by scheduled job)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS daily_revenue (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date                 DATE UNIQUE NOT NULL,
    total_orders         INT          DEFAULT 0,
    completed_orders     INT          DEFAULT 0,
    gross_revenue        DECIMAL(14,2) DEFAULT 0.0,
    total_discount       DECIMAL(12,2) DEFAULT 0.0,
    delivery_revenue     DECIMAL(12,2) DEFAULT 0.0,
    commission_earned    DECIMAL(12,2) DEFAULT 0.0,
    net_platform_revenue DECIMAL(14,2) DEFAULT 0.0,
    new_customers        INT          DEFAULT 0,
    active_merchants     INT          DEFAULT 0,
    created_at           TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_daily_revenue_date ON daily_revenue(date DESC);

-- ------------------------------------------------------------
-- Product Performance (daily per-product metrics)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_performance (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    views           INT  DEFAULT 0,
    cart_adds       INT  DEFAULT 0,
    orders          INT  DEFAULT 0,
    units_sold      INT  DEFAULT 0,
    revenue         DECIMAL(12,2) DEFAULT 0.0,
    returns         INT  DEFAULT 0,
    conversion_rate DECIMAL(5,4)  DEFAULT 0.0,
    UNIQUE (product_id, date)
);

CREATE INDEX IF NOT EXISTS idx_product_perf_date    ON product_performance(product_id, date DESC);
CREATE INDEX IF NOT EXISTS idx_product_perf_revenue ON product_performance(revenue DESC, date DESC);

-- ------------------------------------------------------------
-- Customer Analytics (lifetime value & churn signals)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_analytics (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_orders      INT          DEFAULT 0,
    total_spent       DECIMAL(14,2) DEFAULT 0.0,
    avg_order_value   DECIMAL(10,2) DEFAULT 0.0,
    last_order_at     TIMESTAMP,
    favourite_shop_id UUID REFERENCES shops(id),
    favourite_category INT REFERENCES categories(id),
    churn_risk_score  DECIMAL(5,4) DEFAULT 0.0,
    clv_score         DECIMAL(10,2) DEFAULT 0.0,
    segment           VARCHAR(20) DEFAULT 'NEW'
                      CHECK (segment IN ('NEW','ACTIVE','AT_RISK','CHURNED','HIGH_VALUE')),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customer_analytics_segment ON customer_analytics(segment);
CREATE INDEX IF NOT EXISTS idx_customer_analytics_clv     ON customer_analytics(clv_score DESC);

CREATE TRIGGER trg_customer_analytics_updated_at
    BEFORE UPDATE ON customer_analytics
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Delivery Analytics (daily per-partner metrics)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_analytics (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id            UUID NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    date                  DATE NOT NULL,
    total_assignments     INT  DEFAULT 0,
    completed             INT  DEFAULT 0,
    rejected              INT  DEFAULT 0,
    avg_delivery_time_min DECIMAL(8,2) DEFAULT 0.0,
    avg_rating            DECIMAL(3,2) DEFAULT 0.0,
    total_distance_km     DECIMAL(10,2) DEFAULT 0.0,
    total_earnings        DECIMAL(10,2) DEFAULT 0.0,
    UNIQUE (partner_id, date)
);

CREATE INDEX IF NOT EXISTS idx_delivery_analytics_partner_date ON delivery_analytics(partner_id, date DESC);

-- ------------------------------------------------------------
-- Platform Statistics (high-level KPIs per day)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS platform_statistics (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date             DATE UNIQUE NOT NULL,
    total_users      INT          DEFAULT 0,
    total_merchants  INT          DEFAULT 0,
    total_partners   INT          DEFAULT 0,
    total_products   INT          DEFAULT 0,
    gmv              DECIMAL(16,2) DEFAULT 0.0,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_platform_stats_date ON platform_statistics(date DESC);
