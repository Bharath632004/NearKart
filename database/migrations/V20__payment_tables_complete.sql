-- ============================================================
-- NearKart Migration V20: Complete Payment Tables
-- Author: Bharath C | Version: 1.0
-- Covers: saved payment methods, refund event log,
--         payout batches, UPI / card vault references
-- ============================================================

-- -------------------------------------------------------
-- 1. SAVED PAYMENT METHODS (cards, UPI IDs, netbanking)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_methods (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    method_type     VARCHAR(20) NOT NULL CHECK (method_type IN ('CARD','UPI','NETBANKING','WALLET')),
    display_name    VARCHAR(100) NOT NULL,     -- e.g. "HDFC ****1234"
    gateway_token   TEXT        NOT NULL,      -- tokenized reference from gateway
    is_default      BOOLEAN     NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    expires_at      DATE,                      -- card expiry (nullable for UPI)
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pm_user_default
    ON payment_methods(user_id)
    WHERE is_default = TRUE;
CREATE INDEX IF NOT EXISTS idx_pm_user_active ON payment_methods(user_id) WHERE is_active = TRUE;

-- -------------------------------------------------------
-- 2. PAYMENT EVENTS LOG  (webhook events from gateway)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_events (
    id              BIGSERIAL   PRIMARY KEY,
    payment_id      UUID        REFERENCES payments(id) ON DELETE SET NULL,
    event_type      VARCHAR(60) NOT NULL,      -- payment.captured, refund.processed …
    gateway         VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY',
    raw_payload     JSONB       NOT NULL,
    is_processed    BOOLEAN     NOT NULL DEFAULT FALSE,
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pe_payment_id    ON payment_events(payment_id);
CREATE INDEX IF NOT EXISTS idx_pe_unprocessed   ON payment_events(created_at)
    WHERE is_processed = FALSE;

-- -------------------------------------------------------
-- 3. REFUND EVENT LOG  (granular refund tracking)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS refund_events (
    id              BIGSERIAL   PRIMARY KEY,
    refund_id       UUID        NOT NULL REFERENCES refunds(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL,
    gateway_refund_id VARCHAR(100),
    notes           TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_re_refund_id ON refund_events(refund_id);

-- -------------------------------------------------------
-- 4. PAYOUT BATCHES  (merchant / partner payouts)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS payout_batches (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    batch_ref       VARCHAR(100) UNIQUE NOT NULL,
    beneficiary_id  UUID        NOT NULL REFERENCES users(id),
    beneficiary_type VARCHAR(20) NOT NULL CHECK (beneficiary_type IN ('MERCHANT','DELIVERY_PARTNER')),
    total_amount    DECIMAL(14,2) NOT NULL CHECK (total_amount > 0),
    currency        VARCHAR(5)  NOT NULL DEFAULT 'INR',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','PROCESSING','SUCCESS','FAILED')),
    gateway_payout_id VARCHAR(100),
    failure_reason  TEXT,
    initiated_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pb_beneficiary ON payout_batches(beneficiary_id, status);
CREATE INDEX IF NOT EXISTS idx_pb_status_date ON payout_batches(status, initiated_at DESC);

-- -------------------------------------------------------
-- 5. PAYOUT BATCH ITEMS  (orders/commissions included)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS payout_batch_items (
    id          UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    batch_id    UUID    NOT NULL REFERENCES payout_batches(id) ON DELETE CASCADE,
    order_id    UUID    REFERENCES orders(id),
    amount      DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    description TEXT
);

CREATE INDEX IF NOT EXISTS idx_pbi_batch_id ON payout_batch_items(batch_id);

-- -------------------------------------------------------
-- 6. TAX LEDGER  (GST breakdowns per order)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS tax_ledger (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID    NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    tax_type        VARCHAR(20) NOT NULL CHECK (tax_type IN ('CGST','SGST','IGST','CESS')),
    rate_percent    DECIMAL(5,2) NOT NULL,
    taxable_amount  DECIMAL(12,2) NOT NULL,
    tax_amount      DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tl_order_id ON tax_ledger(order_id);
