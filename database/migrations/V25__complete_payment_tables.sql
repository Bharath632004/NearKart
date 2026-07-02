-- ============================================================
-- NearKart V25 – Complete Payment Tables
-- Refund flow, merchant payouts, split payments, webhooks
-- Author: Bharath C | Date: 2026-07-02
-- ============================================================

-- ============================================================
-- 1. PAYMENT METHODS (saved cards / UPI / wallets)
-- ============================================================

CREATE TABLE IF NOT EXISTS saved_payment_methods (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    method_type     VARCHAR(20) NOT NULL CHECK (method_type IN ('CARD','UPI','NETBANKING','WALLET')),
    display_name    VARCHAR(100),          -- e.g. "HDFC **** 4242"
    gateway_token   TEXT NOT NULL,         -- Razorpay/Stripe token
    is_default      BOOLEAN DEFAULT FALSE,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saved_pm_user_id ON saved_payment_methods(user_id);

-- ============================================================
-- 2. SPLIT PAYMENT (multi-instrument checkout)
-- ============================================================

CREATE TABLE IF NOT EXISTS payment_splits (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id      UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    split_method    VARCHAR(20) NOT NULL CHECK (split_method IN ('WALLET','ONLINE','COD')),
    amount          DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payment_splits_payment_id ON payment_splits(payment_id);

-- ============================================================
-- 3. GATEWAY WEBHOOK EVENTS (idempotency + replay)
-- ============================================================

CREATE TABLE IF NOT EXISTS gateway_webhook_events (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gateway         VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY',
    event_id        VARCHAR(255) UNIQUE NOT NULL,  -- gateway idempotency key
    event_type      VARCHAR(100) NOT NULL,          -- e.g. 'payment.captured'
    payload         JSONB NOT NULL,
    is_processed    BOOLEAN DEFAULT FALSE,
    processed_at    TIMESTAMP,
    error_message   TEXT,
    received_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_webhook_event_id        ON gateway_webhook_events(event_id);
CREATE INDEX IF NOT EXISTS idx_webhook_unprocessed     ON gateway_webhook_events(is_processed) WHERE is_processed = FALSE;
CREATE INDEX IF NOT EXISTS idx_webhook_received_at     ON gateway_webhook_events(received_at DESC);

-- ============================================================
-- 4. MERCHANT PAYOUT BATCHES
-- ============================================================

CREATE TABLE IF NOT EXISTS payout_batches (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id     UUID NOT NULL REFERENCES users(id),
    batch_ref       VARCHAR(100) UNIQUE NOT NULL,
    period_from     DATE NOT NULL,
    period_to       DATE NOT NULL,
    gross_amount    DECIMAL(14,2) NOT NULL,
    deductions      DECIMAL(12,2) DEFAULT 0.0,
    net_amount      DECIMAL(14,2) NOT NULL,
    status          VARCHAR(20) DEFAULT 'QUEUED'
                    CHECK (status IN ('QUEUED','PROCESSING','PAID','FAILED','ON_HOLD')),
    gateway_ref     VARCHAR(255),
    initiated_at    TIMESTAMP,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payout_batches_merchant ON payout_batches(merchant_id);
CREATE INDEX IF NOT EXISTS idx_payout_batches_status   ON payout_batches(status);

-- ============================================================
-- 5. PAYMENT DISPUTES
-- ============================================================

CREATE TABLE IF NOT EXISTS payment_disputes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id      UUID NOT NULL REFERENCES payments(id),
    order_id        UUID NOT NULL REFERENCES orders(id),
    dispute_type    VARCHAR(30) NOT NULL CHECK (dispute_type IN ('CHARGEBACK','FRAUD','NOT_RECEIVED','DUPLICATE')),
    amount          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20) DEFAULT 'OPEN'
                    CHECK (status IN ('OPEN','UNDER_REVIEW','RESOLVED_MERCHANT','RESOLVED_CUSTOMER','CLOSED')),
    evidence_url    TEXT,
    opened_at       TIMESTAMP DEFAULT NOW(),
    resolved_at     TIMESTAMP,
    notes           TEXT
);

CREATE INDEX IF NOT EXISTS idx_payment_disputes_payment ON payment_disputes(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_disputes_order   ON payment_disputes(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_disputes_status  ON payment_disputes(status);

-- ============================================================
-- 6. DELIVERY PARTNER EARNINGS LEDGER
-- ============================================================

CREATE TABLE IF NOT EXISTS partner_earnings_ledger (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id       UUID NOT NULL REFERENCES delivery_partners(id),
    order_id         UUID REFERENCES orders(id),
    amount           DECIMAL(10,2) NOT NULL,
    entry_type       VARCHAR(20) NOT NULL CHECK (entry_type IN ('DELIVERY_FEE','BONUS','PENALTY','ADJUSTMENT','PAYOUT')),
    balance_after    DECIMAL(12,2) NOT NULL,
    description      TEXT,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_earnings_ledger_partner ON partner_earnings_ledger(partner_id);
CREATE INDEX IF NOT EXISTS idx_earnings_ledger_date    ON partner_earnings_ledger(created_at DESC);

-- ============================================================
-- 7. TAX LEDGER (GST records)
-- ============================================================

CREATE TABLE IF NOT EXISTS tax_ledger (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID NOT NULL REFERENCES orders(id),
    merchant_id     UUID NOT NULL REFERENCES users(id),
    taxable_amount  DECIMAL(12,2) NOT NULL,
    cgst_rate       DECIMAL(5,2)  DEFAULT 0.0,
    sgst_rate       DECIMAL(5,2)  DEFAULT 0.0,
    igst_rate       DECIMAL(5,2)  DEFAULT 0.0,
    cgst_amount     DECIMAL(10,2) DEFAULT 0.0,
    sgst_amount     DECIMAL(10,2) DEFAULT 0.0,
    igst_amount     DECIMAL(10,2) DEFAULT 0.0,
    total_tax       DECIMAL(10,2) NOT NULL,
    invoice_number  VARCHAR(50),
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tax_ledger_order      ON tax_ledger(order_id);
CREATE INDEX IF NOT EXISTS idx_tax_ledger_merchant   ON tax_ledger(merchant_id);
CREATE INDEX IF NOT EXISTS idx_tax_ledger_date       ON tax_ledger(created_at DESC);
