-- ============================================================
-- V22: AI / ML Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: product_recommendations, trending_products,
--         customer_segments, demand_forecast, inventory_forecast,
--         fraud_detection_logs
-- All tables are feature-flag gated (see feature_flags in V20)
-- ============================================================

-- ------------------------------------------------------------
-- Product Recommendations (ML model output per user)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_recommendations (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id    UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    score         DECIMAL(8,6) NOT NULL,
    reason        VARCHAR(30)
                  CHECK (reason IN ('COLLABORATIVE','CONTENT','TRENDING','PURCHASED_TOGETHER','RECENTLY_VIEWED')),
    model_version VARCHAR(20),
    generated_at  TIMESTAMP DEFAULT NOW(),
    expires_at    TIMESTAMP,
    UNIQUE (user_id, product_id, reason)
);

CREATE INDEX IF NOT EXISTS idx_rec_user_score  ON product_recommendations(user_id, score DESC);
CREATE INDEX IF NOT EXISTS idx_rec_expires     ON product_recommendations(expires_at) WHERE expires_at IS NOT NULL;

-- ------------------------------------------------------------
-- Trending Products (platform/location-level, refreshed hourly)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trending_products (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    shop_id         UUID REFERENCES shops(id),
    category_id     INT  REFERENCES categories(id),
    pincode         VARCHAR(10),
    trend_score     DECIMAL(10,4) DEFAULT 0.0,
    order_count_24h INT          DEFAULT 0,
    view_count_24h  INT          DEFAULT 0,
    calculated_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_trending_pincode   ON trending_products(pincode, trend_score DESC);
CREATE INDEX IF NOT EXISTS idx_trending_category  ON trending_products(category_id, trend_score DESC);
CREATE INDEX IF NOT EXISTS idx_trending_global    ON trending_products(trend_score DESC);

-- ------------------------------------------------------------
-- Customer Segments (RFM + clustering output)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_segments (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    segment_label VARCHAR(50) NOT NULL,
    rfm_recency   INT,
    rfm_frequency INT,
    rfm_monetary  DECIMAL(12,2),
    cluster_id    INT,
    model_version VARCHAR(20),
    generated_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customer_segments_label   ON customer_segments(segment_label);
CREATE INDEX IF NOT EXISTS idx_customer_segments_cluster ON customer_segments(cluster_id);

-- ------------------------------------------------------------
-- Demand Forecast (per product per shop per day)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS demand_forecast (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id    UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    shop_id       UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    forecast_date DATE NOT NULL,
    predicted_qty INT  NOT NULL,
    confidence    DECIMAL(5,4),
    model_version VARCHAR(20),
    created_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE (product_id, shop_id, forecast_date)
);

CREATE INDEX IF NOT EXISTS idx_demand_forecast_shop ON demand_forecast(shop_id, forecast_date);

-- ------------------------------------------------------------
-- Inventory Forecast (reorder suggestions from ML)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory_forecast (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inventory_id  UUID NOT NULL REFERENCES inventory(id),
    product_id    UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    stockout_date DATE,
    reorder_qty   INT,
    reorder_point INT,
    model_version VARCHAR(20),
    generated_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inv_forecast_product ON inventory_forecast(product_id, generated_at DESC);

-- ------------------------------------------------------------
-- Fraud Detection Logs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fraud_detection_logs (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID REFERENCES users(id),
    order_id     UUID REFERENCES orders(id),
    event_type   VARCHAR(50) NOT NULL,
    risk_score   DECIMAL(5,4) NOT NULL,
    risk_level   VARCHAR(10) NOT NULL
                 CHECK (risk_level IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    signals      JSONB,
    action_taken VARCHAR(20)
                 CHECK (action_taken IN ('ALLOWED','FLAGGED','BLOCKED','MANUAL_REVIEW')),
    reviewed_by  UUID REFERENCES users(id),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fraud_user      ON fraud_detection_logs(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_fraud_order     ON fraud_detection_logs(order_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk      ON fraud_detection_logs(risk_level, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_fraud_unreviewed ON fraud_detection_logs(action_taken)
    WHERE action_taken = 'MANUAL_REVIEW';
