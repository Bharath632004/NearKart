-- NearKart Order Service — Initial Schema
-- Flyway migration V1

CREATE TABLE IF NOT EXISTS orders (
    id               BIGSERIAL     PRIMARY KEY,
    customer_id      BIGINT        NOT NULL,
    shop_id          BIGINT        NOT NULL,
    payment_id       VARCHAR(100),
    status           VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    refund_status    VARCHAR(30),
    cancel_reason    TEXT,
    return_reason    TEXT,
    total_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    delivery_address TEXT          NOT NULL,
    delivery_phone   VARCHAR(15)   NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_customer   ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_shop       ON orders(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_status     ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

CREATE TABLE IF NOT EXISTS order_items (
    id           BIGSERIAL     PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    quantity     INT           NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(10,2) NOT NULL,
    total_price  NUMERIC(12,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
