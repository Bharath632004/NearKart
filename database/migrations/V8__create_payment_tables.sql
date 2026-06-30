-- =============================================================
-- V8: Payment & Financial Tables
-- Tables: payments, transactions, wallet, wallet_transactions,
--         invoices, settlements, commission
-- =============================================================

-- -------------------------------------------------------------
-- 17. PAYMENTS
-- -------------------------------------------------------------
CREATE TYPE payment_status AS ENUM (
    'INITIATED', 'PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'
);

CREATE TABLE payments (
    id                  UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id            UUID           NOT NULL REFERENCES orders(id),
    customer_id         UUID           NOT NULL REFERENCES users(id),
    amount              NUMERIC(10,2)  NOT NULL,
    currency            VARCHAR(5)     NOT NULL DEFAULT 'INR',
    method              payment_method NOT NULL,
    status              payment_status NOT NULL DEFAULT 'INITIATED',
    razorpay_order_id   VARCHAR(100),
    razorpay_payment_id VARCHAR(100)   UNIQUE,
    razorpay_signature  VARCHAR(255),
    gateway_response    JSONB,
    failure_reason      TEXT,
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE payments IS 'Razorpay payment records per order';

CREATE INDEX idx_payments_order_id           ON payments(order_id);
CREATE INDEX idx_payments_customer_id        ON payments(customer_id);
CREATE INDEX idx_payments_status             ON payments(status);
CREATE INDEX idx_payments_razorpay_payment   ON payments(razorpay_payment_id);

CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -------------------------------------------------------------
-- 18. TRANSACTIONS
-- -------------------------------------------------------------
CREATE TYPE txn_type AS ENUM (
    'ORDER_PAYMENT', 'REFUND', 'WALLET_TOPUP', 'WALLET_DEBIT',
    'SETTLEMENT', 'COMMISSION', 'CASHBACK'
);

CREATE TABLE transactions (
    id              BIGSERIAL      PRIMARY KEY,
    reference_id    UUID           NOT NULL,  -- order_id or payment_id
    user_id         UUID           NOT NULL REFERENCES users(id),
    type            txn_type       NOT NULL,
    amount          NUMERIC(10,2)  NOT NULL,
    currency        VARCHAR(5)     NOT NULL DEFAULT 'INR',
    balance_after   NUMERIC(10,2),
    description     TEXT,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE transactions IS 'Immutable financial transaction ledger';

CREATE INDEX idx_transactions_user_id      ON transactions(user_id);
CREATE INDEX idx_transactions_reference_id ON transactions(reference_id);
CREATE INDEX idx_transactions_type         ON transactions(type);
CREATE INDEX idx_transactions_created_at   ON transactions(created_at DESC);

-- -------------------------------------------------------------
-- 19. WALLET
-- -------------------------------------------------------------
CREATE TABLE wallet (
    id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance     NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    currency    VARCHAR(5)    NOT NULL DEFAULT 'INR',
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_wallet_balance CHECK (balance >= 0)
);

COMMENT ON TABLE wallet IS 'One wallet per user for cashback, credits and refunds';

CREATE INDEX idx_wallet_user_id ON wallet(user_id);

CREATE TRIGGER trg_wallet_updated_at
    BEFORE UPDATE ON wallet
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -------------------------------------------------------------
-- 20. WALLET_TRANSACTIONS
-- -------------------------------------------------------------
CREATE TYPE wallet_txn_type AS ENUM ('CREDIT', 'DEBIT');

CREATE TABLE wallet_transactions (
    id             BIGSERIAL      PRIMARY KEY,
    wallet_id      UUID           NOT NULL REFERENCES wallet(id) ON DELETE CASCADE,
    type           wallet_txn_type NOT NULL,
    amount         NUMERIC(10,2)  NOT NULL,
    balance_before NUMERIC(10,2)  NOT NULL,
    balance_after  NUMERIC(10,2)  NOT NULL,
    description    VARCHAR(255)   NOT NULL,
    reference_id   VARCHAR(100),
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_wallet_txn_amount CHECK (amount > 0)
);

COMMENT ON TABLE wallet_transactions IS 'Wallet credit/debit history with before/after balance';

CREATE INDEX idx_wallet_txn_wallet_id   ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_txn_created_at  ON wallet_transactions(created_at DESC);

-- -------------------------------------------------------------
-- 31. INVOICES
-- -------------------------------------------------------------
CREATE TABLE invoices (
    id              BIGSERIAL    PRIMARY KEY,
    order_id        UUID         NOT NULL UNIQUE REFERENCES orders(id),
    invoice_number  VARCHAR(50)  NOT NULL UNIQUE,
    pdf_url         VARCHAR(500),
    subtotal        NUMERIC(10,2) NOT NULL,
    tax_amount      NUMERIC(10,2) NOT NULL,
    discount        NUMERIC(10,2) NOT NULL DEFAULT 0,
    delivery_fee    NUMERIC(10,2) NOT NULL DEFAULT 0,
    total           NUMERIC(10,2) NOT NULL,
    issued_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE invoices IS 'GST-compliant tax invoices per order';

CREATE INDEX idx_invoices_order_id ON invoices(order_id);

-- -------------------------------------------------------------
-- 32. SETTLEMENTS
-- -------------------------------------------------------------
CREATE TYPE settlement_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');

CREATE TABLE settlements (
    id              BIGSERIAL       PRIMARY KEY,
    shop_id         UUID            NOT NULL REFERENCES shops(id),
    merchant_id     UUID            NOT NULL REFERENCES users(id),
    period_from     DATE            NOT NULL,
    period_to       DATE            NOT NULL,
    gross_amount    NUMERIC(10,2)   NOT NULL,
    commission      NUMERIC(10,2)   NOT NULL,
    tax_on_commission NUMERIC(10,2) NOT NULL DEFAULT 0,
    net_amount      NUMERIC(10,2)   NOT NULL,
    status          settlement_status NOT NULL DEFAULT 'PENDING',
    utr_number      VARCHAR(50),   -- Unique Transaction Reference from bank
    settled_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE settlements IS 'Weekly/biweekly merchant payment settlements';

CREATE INDEX idx_settlements_shop_id     ON settlements(shop_id);
CREATE INDEX idx_settlements_merchant_id ON settlements(merchant_id);
CREATE INDEX idx_settlements_status      ON settlements(status);

-- -------------------------------------------------------------
-- 33. COMMISSION
-- -------------------------------------------------------------
CREATE TABLE commission (
    id              BIGSERIAL     PRIMARY KEY,
    order_id        UUID          NOT NULL UNIQUE REFERENCES orders(id),
    shop_id         UUID          NOT NULL REFERENCES shops(id),
    order_amount    NUMERIC(10,2) NOT NULL,
    commission_rate NUMERIC(5,2)  NOT NULL,
    commission_amount NUMERIC(10,2) NOT NULL,
    gst_on_commission NUMERIC(10,2) NOT NULL DEFAULT 0,
    settlement_id   BIGINT        REFERENCES settlements(id),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE commission IS 'Platform commission per order for reconciliation';

CREATE INDEX idx_commission_shop_id       ON commission(shop_id);
CREATE INDEX idx_commission_order_id      ON commission(order_id);
CREATE INDEX idx_commission_settlement_id ON commission(settlement_id);
