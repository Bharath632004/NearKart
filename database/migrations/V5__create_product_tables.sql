-- =============================================================
-- V5: Product Catalog Tables
-- Tables: brands, categories, subcategories, products, product_images
-- =============================================================

-- -------------------------------------------------------------
-- 10. BRANDS
-- -------------------------------------------------------------
CREATE TABLE brands (
    id         SERIAL       PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    logo_url   VARCHAR(500),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE brands IS 'Product brands (Amul, Britannia, etc.)';

-- -------------------------------------------------------------
-- 11. CATEGORIES
-- -------------------------------------------------------------
CREATE TABLE categories (
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    image_url   VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE categories IS 'Top-level product categories (Dairy, Beverages, Snacks)';

INSERT INTO categories (name) VALUES
    ('Dairy & Eggs'), ('Beverages'), ('Snacks & Bakery'),
    ('Fruits & Vegetables'), ('Cleaning & Household'),
    ('Personal Care'), ('Frozen Foods'), ('Baby Care');

-- -------------------------------------------------------------
-- 12. SUBCATEGORIES
-- -------------------------------------------------------------
CREATE TABLE subcategories (
    id          SERIAL       PRIMARY KEY,
    category_id INT          NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    image_url   VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(category_id, name)
);

COMMENT ON TABLE subcategories IS 'Sub-level categories under each main category';

CREATE INDEX idx_subcategories_category_id ON subcategories(category_id);

-- -------------------------------------------------------------
-- 13. PRODUCTS
-- -------------------------------------------------------------
CREATE TYPE product_unit AS ENUM ('KG', 'GRAM', 'LITRE', 'ML', 'PIECE', 'PACK', 'DOZEN', 'BOX');

CREATE TABLE products (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id         UUID          NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    category_id     INT           NOT NULL REFERENCES categories(id),
    subcategory_id  INT           REFERENCES subcategories(id),
    brand_id        INT           REFERENCES brands(id),
    name            VARCHAR(200)  NOT NULL,
    description     TEXT,
    sku             VARCHAR(100),
    barcode         VARCHAR(50),
    unit            product_unit  NOT NULL DEFAULT 'PIECE',
    unit_quantity   NUMERIC(10,3) NOT NULL DEFAULT 1,
    mrp             NUMERIC(10,2) NOT NULL,
    selling_price   NUMERIC(10,2) NOT NULL,
    tax_percentage  NUMERIC(5,2)  NOT NULL DEFAULT 0.00,
    is_available    BOOLEAN       NOT NULL DEFAULT TRUE,
    is_featured     BOOLEAN       NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    rating          NUMERIC(3,2)  DEFAULT 0.00,
    total_reviews   INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_selling_price CHECK (selling_price <= mrp),
    CONSTRAINT chk_mrp_positive  CHECK (mrp > 0)
);

COMMENT ON TABLE products IS 'Product catalog per shop with pricing and taxonomy';

CREATE INDEX idx_products_shop_id          ON products(shop_id);
CREATE INDEX idx_products_category_id      ON products(category_id);
CREATE INDEX idx_products_subcategory_id   ON products(subcategory_id);
CREATE INDEX idx_products_brand_id         ON products(brand_id);
CREATE INDEX idx_products_is_available     ON products(is_available) WHERE is_available = TRUE;
CREATE INDEX idx_products_name_trgm        ON products USING GIN(name gin_trgm_ops);  -- search
CREATE INDEX idx_products_shop_category    ON products(shop_id, category_id);

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -------------------------------------------------------------
-- 14. PRODUCT_IMAGES
-- -------------------------------------------------------------
CREATE TABLE product_images (
    id          BIGSERIAL    PRIMARY KEY,
    product_id  UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(200),
    is_primary  BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order  SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE product_images IS 'Multiple images per product, stored in AWS S3';

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
