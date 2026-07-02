-- NearKart Order Service — Initial Schema
-- Flyway migration V1

CREATE TABLE IF NOT EXISTS orders (
    id               BIGSERIAL     PRIMARY KEY,
    customer_id      BIGINT        NOT NULL,
    shop_id          BIGINT        NOT NULL,
    payment_id       VARCHAR(255),
    status           VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    refund_status    VARCHAR(50),
    cancel_reason    TEXT,
    return_reason    TEXT,
    total_amount     NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    delivery_address VARCHAR(500)  NOT NULL,
    delivery_phone   VARCHAR(20)   NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    delivered_at     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_customer   ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_shop       ON orders(shop_id);
CREATE INDEX IF NOT EXISTS idx_order_status     ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_created    ON orders(created_at DESC);

CREATE TABLE IF NOT EXISTS order_items (
    id           BIGSERIAL     PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    quantity     INT           NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(10,2) NOT NULL CHECK (unit_price > 0),
    total_price  NUMERIC(12,2) NOT NULL CHECK (total_price > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_item_order   ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_product ON order_items(product_id);
