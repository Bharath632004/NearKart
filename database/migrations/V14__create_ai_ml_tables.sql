-- ============================================================
-- NearKart Migration V14: AI / ML Tables
-- Author: Bharath C | Version: 1.0
-- Covers: product recommendations, search events, demand
--         forecasts, fraud flags, chatbot sessions
-- ============================================================

-- -------------------------------------------------------
-- 1. PRODUCT RECOMMENDATIONS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_product_recommendations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    model_version   VARCHAR(50)  NOT NULL DEFAULT 'v1',
    score           DECIMAL(6,4) NOT NULL CHECK (score BETWEEN 0 AND 1),
    reason          VARCHAR(100),                          -- 'collab_filter','content','trending'
    is_clicked      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_purchased    BOOLEAN      NOT NULL DEFAULT FALSE,
    generated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP    NOT NULL DEFAULT (NOW() + INTERVAL '24 hours'),
    UNIQUE (user_id, product_id, model_version)
);

CREATE INDEX IF NOT EXISTS idx_ai_rec_user_id    ON ai_product_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_rec_product_id ON ai_product_recommendations(product_id);
CREATE INDEX IF NOT EXISTS idx_ai_rec_score      ON ai_product_recommendations(user_id, score DESC);

-- -------------------------------------------------------
-- 2. SEARCH EVENTS  (feeds search-ranking model)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_search_events (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      UUID         REFERENCES users(id) ON DELETE SET NULL,
    session_id   VARCHAR(100),
    query        TEXT         NOT NULL,
    result_count INT          NOT NULL DEFAULT 0,
    clicked_id   UUID         REFERENCES products(id) ON DELETE SET NULL,
    search_lat   DECIMAL(10,8),
    search_lng   DECIMAL(11,8),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_search_user_id   ON ai_search_events(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_search_query     ON ai_search_events USING GIN(to_tsvector('english', query));
CREATE INDEX IF NOT EXISTS idx_ai_search_created   ON ai_search_events(created_at DESC);

-- -------------------------------------------------------
-- 3. DEMAND FORECAST
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_demand_forecasts (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    shop_id         UUID        NOT NULL REFERENCES shops(id)    ON DELETE CASCADE,
    forecast_date   DATE        NOT NULL,
    predicted_qty   INT         NOT NULL CHECK (predicted_qty >= 0),
    actual_qty      INT,
    model_version   VARCHAR(50) NOT NULL DEFAULT 'v1',
    confidence      DECIMAL(5,2) CHECK (confidence BETWEEN 0 AND 100),
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, shop_id, forecast_date, model_version)
);

CREATE INDEX IF NOT EXISTS idx_ai_demand_product ON ai_demand_forecasts(product_id, forecast_date);
CREATE INDEX IF NOT EXISTS idx_ai_demand_shop    ON ai_demand_forecasts(shop_id, forecast_date);

-- -------------------------------------------------------
-- 4. FRAUD FLAGS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_fraud_flags (
    id            UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type   VARCHAR(30) NOT NULL CHECK (entity_type IN ('ORDER','PAYMENT','USER')),
    entity_id     UUID        NOT NULL,
    risk_score    DECIMAL(5,4) NOT NULL CHECK (risk_score BETWEEN 0 AND 1),
    risk_level    VARCHAR(10) NOT NULL CHECK (risk_level IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    reasons       JSONB,
    is_reviewed   BOOLEAN     NOT NULL DEFAULT FALSE,
    reviewed_by   UUID        REFERENCES users(id),
    reviewed_at   TIMESTAMP,
    model_version VARCHAR(50) NOT NULL DEFAULT 'v1',
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_fraud_entity   ON ai_fraud_flags(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_ai_fraud_risk     ON ai_fraud_flags(risk_level) WHERE is_reviewed = FALSE;
CREATE INDEX IF NOT EXISTS idx_ai_fraud_created  ON ai_fraud_flags(created_at DESC);

-- -------------------------------------------------------
-- 5. CHATBOT SESSIONS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_chatbot_sessions (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID        REFERENCES users(id) ON DELETE SET NULL,
    session_key  VARCHAR(150) UNIQUE NOT NULL,
    channel      VARCHAR(30) NOT NULL DEFAULT 'WEB' CHECK (channel IN ('WEB','APP','WHATSAPP')),
    started_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    ended_at     TIMESTAMP,
    message_count INT        NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ai_chatbot_messages (
    id          BIGSERIAL   PRIMARY KEY,
    session_id  UUID        NOT NULL REFERENCES ai_chatbot_sessions(id) ON DELETE CASCADE,
    role        VARCHAR(10) NOT NULL CHECK (role IN ('USER','BOT')),
    content     TEXT        NOT NULL,
    intent      VARCHAR(100),
    confidence  DECIMAL(5,4),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chatbot_session_user ON ai_chatbot_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chatbot_msg_session  ON ai_chatbot_messages(session_id);

-- -------------------------------------------------------
-- 6. USER BEHAVIOR EVENTS  (clickstream)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_user_events (
    id           BIGSERIAL   PRIMARY KEY,
    user_id      UUID        REFERENCES users(id) ON DELETE SET NULL,
    session_id   VARCHAR(100),
    event_type   VARCHAR(50) NOT NULL,          -- VIEW_PRODUCT, ADD_TO_CART, CHECKOUT …
    entity_type  VARCHAR(30),
    entity_id    UUID,
    metadata     JSONB,
    device_type  VARCHAR(20),
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- Default (non-partitioned fallback) – real partitions added per month in production
CREATE TABLE IF NOT EXISTS ai_user_events_default
    PARTITION OF ai_user_events DEFAULT;

CREATE INDEX IF NOT EXISTS idx_ai_ue_user_id   ON ai_user_events(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_ue_event     ON ai_user_events(event_type);
CREATE INDEX IF NOT EXISTS idx_ai_ue_created   ON ai_user_events(created_at DESC);
