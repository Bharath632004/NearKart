-- ============================================================
-- NearKart PostgreSQL Schema
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(15) UNIQUE,
    role        VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',  -- CUSTOMER | MERCHANT | DELIVERY_AGENT | ADMIN
    active      BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Addresses (reusable for users, shops, orders)
CREATE TABLE addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
    label       VARCHAR(50),         -- Home, Work, Other
    street      VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(100),
    pincode     VARCHAR(10),
    latitude    DECIMAL(9,6),
    longitude   DECIMAL(9,6),
    is_default  BOOLEAN DEFAULT FALSE
);

-- Shops
CREATE TABLE shops (
    id            BIGSERIAL PRIMARY KEY,
    owner_id      BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name          VARCHAR(150) NOT NULL,
    description   TEXT,
    category      VARCHAR(100),       -- Grocery | Pharmacy | Electronics | etc.
    phone         VARCHAR(15),
    email         VARCHAR(150),
    street        VARCHAR(255),
    city          VARCHAR(100),
    state         VARCHAR(100),
    pincode       VARCHAR(10),
    latitude      DECIMAL(9,6),
    longitude     DECIMAL(9,6),
    is_open       BOOLEAN DEFAULT TRUE,
    rating        DECIMAL(2,1) DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Products
CREATE TABLE products (
    id            BIGSERIAL PRIMARY KEY,
    shop_id       BIGINT REFERENCES shops(id) ON DELETE CASCADE,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    category      VARCHAR(100),
    price         DECIMAL(10,2) NOT NULL,
    mrp           DECIMAL(10,2),
    stock         INT DEFAULT 0,
    unit          VARCHAR(30),        -- kg, litre, piece, pack
    image_url     VARCHAR(500),
    is_available  BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Orders
CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    customer_id      BIGINT REFERENCES users(id),
    shop_id          BIGINT REFERENCES shops(id),
    delivery_agent_id BIGINT REFERENCES users(id),
    delivery_address_id BIGINT REFERENCES addresses(id),
    status           VARCHAR(30) DEFAULT 'PENDING',  -- PENDING | CONFIRMED | PREPARING | PICKED_UP | DELIVERED | CANCELLED
    total_amount     DECIMAL(10,2) NOT NULL,
    delivery_fee     DECIMAL(10,2) DEFAULT 0.00,
    discount         DECIMAL(10,2) DEFAULT 0.00,
    payment_method   VARCHAR(30),    -- COD | UPI | CARD
    payment_status   VARCHAR(20) DEFAULT 'PENDING',  -- PENDING | PAID | FAILED | REFUNDED
    notes            TEXT,
    created_at       TIMESTAMP DEFAULT NOW(),
    delivered_at     TIMESTAMP
);

-- Order Items
CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id  BIGINT REFERENCES products(id),
    quantity    INT NOT NULL,
    unit_price  DECIMAL(10,2) NOT NULL,
    subtotal    DECIMAL(10,2) GENERATED ALWAYS AS (quantity * unit_price) STORED
);

-- Delivery Agents
CREATE TABLE delivery_agents (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    vehicle     VARCHAR(50),         -- BIKE | CYCLE | SCOOTER
    is_online   BOOLEAN DEFAULT FALSE,
    latitude    DECIMAL(9,6),
    longitude   DECIMAL(9,6),
    rating      DECIMAL(2,1) DEFAULT 0.0,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Reviews
CREATE TABLE reviews (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT UNIQUE REFERENCES orders(id),
    customer_id BIGINT REFERENCES users(id),
    shop_id     BIGINT REFERENCES shops(id),
    rating      INT CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- Indexes for performance
-- ============================================================
CREATE INDEX idx_shops_city ON shops(city);
CREATE INDEX idx_shops_category ON shops(category);
CREATE INDEX idx_products_shop ON products(shop_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_shop ON orders(shop_id);
CREATE INDEX idx_orders_status ON orders(status);
