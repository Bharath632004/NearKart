-- ============================================================
-- V16: Merchant Extended Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: store_operating_hours, store_documents, gst_information,
--         stock_history, merchant_analytics
-- ============================================================

-- ------------------------------------------------------------
-- Store Operating Hours
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS store_operating_hours (
    id          UUID     PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id     UUID     NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0=Sunday
    open_time   TIME     NOT NULL,
    close_time  TIME     NOT NULL,
    is_closed   BOOLEAN  DEFAULT FALSE,
    UNIQUE (shop_id, day_of_week)
);

CREATE INDEX IF NOT EXISTS idx_store_hours_shop ON store_operating_hours(shop_id);

-- ------------------------------------------------------------
-- Store Documents (FSSAI, GST Cert, PAN, etc.)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS store_documents (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id     UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    doc_type    VARCHAR(50) NOT NULL
                CHECK (doc_type IN ('FSSAI','PAN','SHOP_ACT','TRADE_LICENSE','GST_CERT','OTHER')),
    doc_url     TEXT     NOT NULL,
    is_verified BOOLEAN  DEFAULT FALSE,
    verified_at TIMESTAMP,
    verified_by UUID     REFERENCES users(id),
    expires_at  DATE,
    uploaded_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_store_docs_shop ON store_documents(shop_id);

-- ------------------------------------------------------------
-- GST Information
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gst_information (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id       UUID UNIQUE NOT NULL REFERENCES users(id),
    gstin             VARCHAR(20) UNIQUE NOT NULL,
    legal_name        VARCHAR(255) NOT NULL,
    trade_name        VARCHAR(255),
    address           TEXT,
    state_code        VARCHAR(5),
    registration_date DATE,
    is_verified       BOOLEAN DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE TRIGGER trg_gst_information_updated_at
    BEFORE UPDATE ON gst_information
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Stock History (inventory movement audit)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inventory_id    UUID NOT NULL REFERENCES inventory(id),
    product_id      UUID NOT NULL REFERENCES products(id),
    change_type     VARCHAR(20) NOT NULL
                    CHECK (change_type IN ('ADD','REMOVE','ADJUST','SOLD','RETURNED','EXPIRED')),
    quantity_before INT NOT NULL,
    quantity_change INT NOT NULL,
    quantity_after  INT NOT NULL,
    reference_type  VARCHAR(50),
    reference_id    UUID,
    performed_by    UUID REFERENCES users(id),
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_history_product   ON stock_history(product_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_history_inventory ON stock_history(inventory_id, created_at DESC);

-- ------------------------------------------------------------
-- Merchant Analytics (daily snapshot per shop)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS merchant_analytics (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id      UUID NOT NULL REFERENCES users(id),
    shop_id          UUID NOT NULL REFERENCES shops(id),
    date             DATE NOT NULL,
    total_orders     INT  DEFAULT 0,
    completed_orders INT  DEFAULT 0,
    cancelled_orders INT  DEFAULT 0,
    gross_revenue    DECIMAL(14,2) DEFAULT 0.0,
    net_revenue      DECIMAL(14,2) DEFAULT 0.0,
    commission_paid  DECIMAL(12,2) DEFAULT 0.0,
    new_customers    INT  DEFAULT 0,
    repeat_customers INT  DEFAULT 0,
    avg_order_value  DECIMAL(10,2) DEFAULT 0.0,
    created_at       TIMESTAMP DEFAULT NOW(),
    UNIQUE (shop_id, date)
);

CREATE INDEX IF NOT EXISTS idx_merchant_analytics_shop_date ON merchant_analytics(shop_id, date DESC);
CREATE INDEX IF NOT EXISTS idx_merchant_analytics_merchant  ON merchant_analytics(merchant_id, date DESC);
