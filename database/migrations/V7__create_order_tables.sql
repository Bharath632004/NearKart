-- =============================================================
-- V7: Order Tables
-- Tables: coupons, offers, orders, order_items
-- =============================================================

-- -------------------------------------------------------------
-- 21. COUPONS
-- -------------------------------------------------------------
CREATE TYPE coupon_type AS ENUM ('PERCENTAGE', 'FLAT', 'FREE_DELIVERY', 'CASHBACK');

CREATE TABLE coupons (
    id                  BIGSERIAL     PRIMARY KEY,
    code                VARCHAR(30)   NOT NULL UNIQUE,
    description         VARCHAR(255),
    type                coupon_type   NOT NULL,
    value               NUMERIC(10,2) NOT NULL,
    min_order_amount    NUMERIC(10,2) NOT NULL DEFAULT 0,
    max_discount        NUMERIC(10,2),
    usage_limit         INT           NOT NULL DEFAULT 1,
    usage_count         INT           NOT NULL DEFAULT 0,
    per_user_limit      INT           NOT NULL DEFAULT 1,
    valid_from          TIMESTAMPTZ   NOT NULL,
    valid_until         TIMESTAMPTZ   NOT NULL,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_coupon_dates CHECK (valid_until > valid_from),
    CONSTRAINT chk_coupon_value CHECK (value > 0)
);

COMMENT ON TABLE coupons IS 'Discount coupons for customer orders';

CREATE INDEX idx_coupons_code      ON coupons(code);
CREATE INDEX idx_coupons_is_active ON coupons(is_active, valid_until);

-- -------------------------------------------------------------
-- 22. OFFERS
-- -------------------------------------------------------------
CREATE TABLE offers (
    id          BIGSERIAL     PRIMARY KEY,
    shop_id     UUID          REFERENCES shops(id),
    product_id  UUID          REFERENCES products(id),
    title       VARCHAR(200)  NOT NULL,
    description TEXT,
    image_url   VARCHAR(500),
    discount_pct NUMERIC(5,2),
    valid_from  TIMESTAMPTZ   NOT NULL,
    valid_until TIMESTAMPTZ   NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE offers IS 'Promotional offers by shop or for specific products';

CREATE INDEX idx_offers_shop_id    ON offers(shop_id);
CREATE INDEX idx_offers_product_id ON offers(product_id);
CREATE INDEX idx_offers_active     ON offers(is_active, valid_until);

-- -------------------------------------------------------------
-- 15. ORDERS
-- -------------------------------------------------------------
CREATE TYPE order_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'PREPARING',
    'READY_FOR_PICKUP',
    'PICKED_UP',
    'OUT_FOR_DELIVERY',
    'DELIVERED',
    'CANCELLED',
    'REFUND_INITIATED',
    'REFUNDED'
);

CREATE TYPE payment_method AS ENUM ('UPI', 'CARD', 'NETBANKING', 'WALLET', 'CASH_ON_DELIVERY');

CREATE TABLE orders (
    id                  UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number        VARCHAR(30)   NOT NULL UNIQUE,  -- e.g. NK-20260630-00001
    customer_id         UUID          NOT NULL REFERENCES users(id),
    shop_id             UUID          NOT NULL REFERENCES shops(id),
    delivery_address_id UUID          NOT NULL REFERENCES addresses(id),
    status              order_status  NOT NULL DEFAULT 'PENDING',
    subtotal            NUMERIC(10,2) NOT NULL,
    delivery_fee        NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    discount_amount     NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    tax_amount          NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    total_amount        NUMERIC(10,2) NOT NULL,
    coupon_id           BIGINT        REFERENCES coupons(id),
    payment_method      payment_method,
    special_instructions TEXT,
    estimated_delivery_at TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    cancellation_reason TEXT,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE orders IS 'Customer orders. Partitioned by created_at for performance.';

-- Partitions (quarterly)
CREATE TABLE orders_2026_q3 PARTITION OF orders
    FOR VALUES FROM ('2026-07-01') TO ('2026-10-01');
CREATE TABLE orders_2026_q4 PARTITION OF orders
    FOR VALUES FROM ('2026-10-01') TO ('2027-01-01');
CREATE TABLE orders_2027_q1 PARTITION OF orders
    FOR VALUES FROM ('2027-01-01') TO ('2027-04-01');

CREATE INDEX idx_orders_customer_id  ON orders(customer_id);
CREATE INDEX idx_orders_shop_id      ON orders(shop_id);
CREATE INDEX idx_orders_status       ON orders(status);
CREATE INDEX idx_orders_created_at   ON orders(created_at DESC);
CREATE INDEX idx_orders_number       ON orders(order_number);
CREATE INDEX idx_orders_cust_status  ON orders(customer_id, status);

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Order number sequence generator
CREATE SEQUENCE order_seq START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.order_number := 'NK-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(NEXTVAL('order_seq')::TEXT, 5, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_generate_order_number
    BEFORE INSERT ON orders
    FOR EACH ROW EXECUTE FUNCTION generate_order_number();

-- -------------------------------------------------------------
-- 16. ORDER_ITEMS
-- -------------------------------------------------------------
CREATE TABLE order_items (
    id            BIGSERIAL     PRIMARY KEY,
    order_id      UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    UUID          NOT NULL REFERENCES products(id),
    product_name  VARCHAR(200)  NOT NULL,  -- snapshot at order time
    product_image VARCHAR(500),
    quantity      INT           NOT NULL,
    unit_price    NUMERIC(10,2) NOT NULL,  -- price at order time
    total_price   NUMERIC(10,2) NOT NULL,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

COMMENT ON TABLE order_items IS 'Line items for each order. Prices snapshotted at order time.';

CREATE INDEX idx_order_items_order_id   ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
