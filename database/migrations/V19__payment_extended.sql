-- ============================================================
-- V19: Payment Extended Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: failed_payments, settlement_items
-- Extends: payments (Razorpay fields, UPI, method)
-- ============================================================

-- ------------------------------------------------------------
-- Extend payments table with Razorpay-specific fields
-- ------------------------------------------------------------
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS razorpay_order_id   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS razorpay_payment_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS razorpay_signature  TEXT,
    ADD COLUMN IF NOT EXISTS vpa                 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bank                VARCHAR(100),
    ADD COLUMN IF NOT EXISTS card_id             VARCHAR(100),
    ADD COLUMN IF NOT EXISTS method              VARCHAR(30);

CREATE INDEX IF NOT EXISTS idx_payments_razorpay_order   ON payments(razorpay_order_id);
CREATE INDEX IF NOT EXISTS idx_payments_razorpay_payment ON payments(razorpay_payment_id);

-- ------------------------------------------------------------
-- Failed Payments Log
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS failed_payments (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id         UUID REFERENCES orders(id),
    user_id          UUID REFERENCES users(id),
    gateway          VARCHAR(30) DEFAULT 'RAZORPAY',
    gateway_order_id VARCHAR(255),
    amount           DECIMAL(12,2) NOT NULL,
    failure_code     VARCHAR(50),
    failure_reason   TEXT,
    raw_response     JSONB,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_failed_payments_user    ON failed_payments(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_failed_payments_order   ON failed_payments(order_id);

-- ------------------------------------------------------------
-- Settlement Items (per-order traceability within a settlement)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settlement_items (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    settlement_id     UUID NOT NULL REFERENCES settlements(id),
    order_id          UUID NOT NULL REFERENCES orders(id),
    order_amount      DECIMAL(12,2) NOT NULL,
    commission_rate   DECIMAL(5,2),
    commission_amount DECIMAL(12,2),
    net_amount        DECIMAL(12,2),
    created_at        TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_settlement_items_settlement ON settlement_items(settlement_id);
CREATE INDEX IF NOT EXISTS idx_settlement_items_order      ON settlement_items(order_id);
