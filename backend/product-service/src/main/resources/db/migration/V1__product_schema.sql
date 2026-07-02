-- NearKart Product Service \u2014 Initial Schema
-- Flyway migration V1

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);

CREATE TABLE IF NOT EXISTS products (
    id             BIGSERIAL      PRIMARY KEY,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    price          NUMERIC(10,2)  NOT NULL CHECK (price >= 0),
    stock_quantity INT            NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    image_url      VARCHAR(500),
    available      BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id    BIGINT         REFERENCES categories(id) ON DELETE SET NULL,
    shop_id        BIGINT         NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_shop      ON products(shop_id);
CREATE INDEX IF NOT EXISTS idx_product_category  ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_product_available ON products(available);
CREATE INDEX IF NOT EXISTS idx_product_price     ON products(price);
