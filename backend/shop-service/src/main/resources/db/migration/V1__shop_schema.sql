-- NearKart Shop Service — Initial Schema
-- Flyway migration V1

CREATE TABLE IF NOT EXISTS shops (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(500),
    image_url   VARCHAR(500),
    merchant_id BIGINT        NOT NULL,
    address     VARCHAR(300)  NOT NULL,
    city        VARCHAR(100),
    pincode     CHAR(6),
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    active      BOOLEAN       NOT NULL DEFAULT true,
    verified    BOOLEAN       NOT NULL DEFAULT false,
    phone       VARCHAR(20),
    email       VARCHAR(100),
    category    VARCHAR(50),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_shop_merchant_name_city UNIQUE (merchant_id, name, city)
);

CREATE INDEX IF NOT EXISTS idx_shop_merchant   ON shops(merchant_id);
CREATE INDEX IF NOT EXISTS idx_shop_active     ON shops(active);
CREATE INDEX IF NOT EXISTS idx_shop_city       ON shops(city);
CREATE INDEX IF NOT EXISTS idx_shop_category   ON shops(category);
CREATE INDEX IF NOT EXISTS idx_shop_location   ON shops(latitude, longitude) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_shop_created    ON shops(created_at DESC);
