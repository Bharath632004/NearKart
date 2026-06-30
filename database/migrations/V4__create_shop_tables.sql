-- =============================================================
-- V4: Shop Tables
-- Tables: shop_categories, shops
-- =============================================================

-- -------------------------------------------------------------
-- 8. SHOP_CATEGORIES
-- -------------------------------------------------------------
CREATE TABLE shop_categories (
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_url    VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE shop_categories IS 'Types of shops: Grocery, Pharmacy, Electronics, etc.';

INSERT INTO shop_categories (name, description) VALUES
    ('Grocery',    'Kirana & general grocery stores'),
    ('Pharmacy',   'Medical & pharmacy shops'),
    ('Electronics','Electronics & accessories'),
    ('Bakery',     'Bakeries and confectioneries'),
    ('Stationery', 'Books, stationery and office supplies'),
    ('Pet Supplies','Pet food and accessories');

-- -------------------------------------------------------------
-- 9. SHOPS
-- -------------------------------------------------------------
CREATE TYPE shop_status AS ENUM ('PENDING', 'VERIFIED', 'SUSPENDED', 'CLOSED');

CREATE TABLE shops (
    id                  UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id         UUID        NOT NULL REFERENCES users(id),
    shop_category_id    INT         NOT NULL REFERENCES shop_categories(id),
    name                VARCHAR(150) NOT NULL,
    description         TEXT,
    logo_url            VARCHAR(500),
    cover_url           VARCHAR(500),
    phone               VARCHAR(15)  NOT NULL,
    email               VARCHAR(100),
    gstin               VARCHAR(20)  UNIQUE,
    fssai_license       VARCHAR(20),
    full_address        TEXT         NOT NULL,
    city                VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    pincode             VARCHAR(10)  NOT NULL,
    location            GEOGRAPHY(POINT, 4326) NOT NULL,
    delivery_radius_km  NUMERIC(5,2) NOT NULL DEFAULT 5.0,
    min_order_amount    NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    avg_delivery_mins   SMALLINT     NOT NULL DEFAULT 30,
    commission_rate     NUMERIC(5,2) NOT NULL DEFAULT 8.00,  -- platform commission %
    status              shop_status  NOT NULL DEFAULT 'PENDING',
    is_open             BOOLEAN      NOT NULL DEFAULT FALSE,
    rating              NUMERIC(3,2) DEFAULT 0.00,
    total_reviews       INT          NOT NULL DEFAULT 0,
    kyc_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    kyc_docs_url        TEXT[],
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE shops IS 'Merchant shops with GPS location for proximity queries';
COMMENT ON COLUMN shops.location IS 'Used with ST_DWithin for nearby shop search';
COMMENT ON COLUMN shops.delivery_radius_km IS 'Max km radius for delivery from this shop';

CREATE INDEX idx_shops_location        ON shops USING GIST(location);  -- Critical spatial index
CREATE INDEX idx_shops_merchant_id     ON shops(merchant_id);
CREATE INDEX idx_shops_pincode         ON shops(pincode);
CREATE INDEX idx_shops_status          ON shops(status);
CREATE INDEX idx_shops_is_open         ON shops(is_open) WHERE is_open = TRUE;
CREATE INDEX idx_shops_category        ON shops(shop_category_id);
CREATE INDEX idx_shops_name_trgm       ON shops USING GIN(name gin_trgm_ops);  -- Full-text search

CREATE TRIGGER trg_shops_updated_at
    BEFORE UPDATE ON shops
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
