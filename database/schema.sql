-- ============================================================
-- NearKart Database Schema
-- PostgreSQL
-- ============================================================

-- Drop tables if they exist (for clean re-run)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    phone      VARCHAR(15) UNIQUE,
    role       VARCHAR(30)         NOT NULL DEFAULT 'CUSTOMER',
    active     BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP                    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

-- ============================================================
-- PRODUCTS TABLE
-- ============================================================
CREATE TABLE products (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200)   NOT NULL,
    description      TEXT,
    price            DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity   INTEGER                 DEFAULT 0 CHECK (stock_quantity >= 0),
    category         VARCHAR(100),
    image_url        TEXT,
    merchant_id      BIGINT REFERENCES users (id) ON DELETE SET NULL,
    available        BOOLEAN                 DEFAULT TRUE,
    created_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_merchant ON products (merchant_id);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_available ON products (available);

-- ============================================================
-- ORDERS TABLE
-- ============================================================
CREATE TABLE orders (
    id                 BIGSERIAL PRIMARY KEY,
    customer_id        BIGINT REFERENCES users (id)    NOT NULL,
    merchant_id        BIGINT REFERENCES users (id),
    delivery_agent_id  BIGINT REFERENCES users (id),
    status             VARCHAR(30)                     NOT NULL DEFAULT 'PLACED',
    total_amount       DECIMAL(10, 2)                  NOT NULL,
    delivery_address   TEXT                            NOT NULL,
    delivery_notes     TEXT,
    placed_at          TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP,
    confirmed_at       TIMESTAMP,
    delivered_at       TIMESTAMP,
    cancelled_at       TIMESTAMP
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_merchant ON orders (merchant_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_agent ON orders (delivery_agent_id);

-- ============================================================
-- ORDER ITEMS TABLE
-- ============================================================
CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT REFERENCES orders (id) ON DELETE CASCADE NOT NULL,
    product_id   BIGINT REFERENCES products (id) ON DELETE SET NULL,
    product_name VARCHAR(200)   NOT NULL,
    quantity     INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price   DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

-- ============================================================
-- SEED DATA (optional test data)
-- ============================================================
INSERT INTO users (name, email, password, phone, role)
VALUES
    ('Admin User', 'admin@nearkart.com', '$2a$10$placeholder_hashed_password', '9000000000', 'ADMIN'),
    ('Test Merchant', 'merchant@nearkart.com', '$2a$10$placeholder_hashed_password', '9111111111', 'MERCHANT'),
    ('Test Customer', 'customer@nearkart.com', '$2a$10$placeholder_hashed_password', '9222222222', 'CUSTOMER');
