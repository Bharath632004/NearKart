-- ============================================================
-- NearKart Migration V15: Analytics / Reporting Tables
-- Author: Bharath C | Version: 1.0
-- ============================================================

-- -------------------------------------------------------
-- 1. DAILY PLATFORM METRICS  (pre-aggregated KPIs)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_daily_metrics (
    id                UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_date       DATE    NOT NULL UNIQUE,
    total_orders      INT     NOT NULL DEFAULT 0,
    completed_orders  INT     NOT NULL DEFAULT 0,
    cancelled_orders  INT     NOT NULL DEFAULT 0,
    total_revenue     DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_discount    DECIMAL(14,2) NOT NULL DEFAULT 0,
    new_users         INT     NOT NULL DEFAULT 0,
    active_users      INT     NOT NULL DEFAULT 0,
    new_shops         INT     NOT NULL DEFAULT 0,
    total_deliveries  INT     NOT NULL DEFAULT 0,
    avg_delivery_mins DECIMAL(8,2),
    avg_order_value   DECIMAL(10,2),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_adm_date ON analytics_daily_metrics(metric_date DESC);

-- -------------------------------------------------------
-- 2. SHOP DAILY METRICS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_shop_daily (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id         UUID    NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    metric_date     DATE    NOT NULL,
    orders_count    INT     NOT NULL DEFAULT 0,
    revenue         DECIMAL(14,2) NOT NULL DEFAULT 0,
    items_sold      INT     NOT NULL DEFAULT 0,
    new_customers   INT     NOT NULL DEFAULT 0,
    avg_rating      DECIMAL(3,2),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (shop_id, metric_date)
);

CREATE INDEX IF NOT EXISTS idx_asd_shop_date ON analytics_shop_daily(shop_id, metric_date DESC);

-- -------------------------------------------------------
-- 3. PRODUCT DAILY METRICS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_product_daily (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    metric_date     DATE    NOT NULL,
    views           INT     NOT NULL DEFAULT 0,
    add_to_cart     INT     NOT NULL DEFAULT 0,
    orders_count    INT     NOT NULL DEFAULT 0,
    revenue         DECIMAL(14,2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, metric_date)
);

CREATE INDEX IF NOT EXISTS idx_apd_product_date ON analytics_product_daily(product_id, metric_date DESC);

-- -------------------------------------------------------
-- 4. FUNNEL EVENTS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_funnel_events (
    id            BIGSERIAL   PRIMARY KEY,
    user_id       UUID        REFERENCES users(id) ON DELETE SET NULL,
    session_id    VARCHAR(100),
    funnel_name   VARCHAR(60) NOT NULL,        -- 'CHECKOUT','REGISTRATION' ...
    step_name     VARCHAR(60) NOT NULL,
    step_order    SMALLINT    NOT NULL,
    is_completed  BOOLEAN     NOT NULL DEFAULT FALSE,
    metadata      JSONB,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_afe_funnel   ON analytics_funnel_events(funnel_name, step_name);
CREATE INDEX IF NOT EXISTS idx_afe_user     ON analytics_funnel_events(user_id);
CREATE INDEX IF NOT EXISTS idx_afe_created  ON analytics_funnel_events(created_at DESC);

-- -------------------------------------------------------
-- 5. COHORT RETENTION
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_cohort_retention (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    cohort_month    DATE    NOT NULL,          -- first day of cohort month
    period_number   INT     NOT NULL,          -- 0 = acquisition month
    cohort_size     INT     NOT NULL,
    retained_users  INT     NOT NULL,
    retention_rate  DECIMAL(5,2) GENERATED ALWAYS AS
                    (CASE WHEN cohort_size > 0
                          THEN ROUND((retained_users::DECIMAL / cohort_size) * 100, 2)
                          ELSE 0 END) STORED,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (cohort_month, period_number)
);

-- -------------------------------------------------------
-- 6. REVENUE BREAKDOWN
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_revenue_breakdown (
    id                  UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    period_date         DATE    NOT NULL,
    period_type         VARCHAR(10) NOT NULL CHECK (period_type IN ('DAY','WEEK','MONTH')),
    category_id         INT     REFERENCES categories(id),
    shop_id             UUID    REFERENCES shops(id),
    gross_revenue       DECIMAL(14,2) NOT NULL DEFAULT 0,
    commission_revenue  DECIMAL(14,2) NOT NULL DEFAULT 0,
    delivery_revenue    DECIMAL(14,2) NOT NULL DEFAULT 0,
    discount_given      DECIMAL(14,2) NOT NULL DEFAULT 0,
    refunds_issued      DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_revenue         DECIMAL(14,2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (period_date, period_type, COALESCE(category_id, -1), COALESCE(shop_id, uuid_nil()))
);

CREATE INDEX IF NOT EXISTS idx_arb_period ON analytics_revenue_breakdown(period_date DESC, period_type);

-- -------------------------------------------------------
-- 7. DELIVERY SLA
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_delivery_sla (
    id                      UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_assignment_id  UUID    UNIQUE REFERENCES delivery_assignments(id),
    promised_minutes        INT,
    actual_minutes          INT,
    is_on_time              BOOLEAN GENERATED ALWAYS AS
                            (CASE WHEN actual_minutes IS NOT NULL AND promised_minutes IS NOT NULL
                                  THEN actual_minutes <= promised_minutes
                                  ELSE NULL END) STORED,
    distance_km             DECIMAL(8,2),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sla_on_time ON analytics_delivery_sla(is_on_time, created_at DESC);
