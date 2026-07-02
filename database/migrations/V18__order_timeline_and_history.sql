-- ============================================================
-- V18: Order Timeline, Status History & Exchange Requests
-- Author: Bharath C | NearKart DB Production Readiness
-- ============================================================

-- ------------------------------------------------------------
-- Order Status History (immutable audit log)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_status_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status      VARCHAR(30),
    new_status      VARCHAR(30) NOT NULL,
    changed_by      UUID REFERENCES users(id),
    changed_by_role VARCHAR(30),
    note            TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_status_hist_order ON order_status_history(order_id, created_at);

-- ------------------------------------------------------------
-- Order Timeline (customer-facing milestones with icons)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_timeline (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    event       VARCHAR(100) NOT NULL,
    description TEXT,
    icon        VARCHAR(50),
    occurred_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_timeline_order ON order_timeline(order_id, occurred_at);

-- ------------------------------------------------------------
-- Exchange Requests
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exchange_requests (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id             UUID NOT NULL REFERENCES orders(id),
    order_item_id        UUID NOT NULL REFERENCES order_items(id),
    reason               TEXT NOT NULL,
    requested_product_id UUID REFERENCES products(id),
    status               VARCHAR(20) DEFAULT 'REQUESTED'
                         CHECK (status IN ('REQUESTED','APPROVED','REJECTED','COMPLETED')),
    admin_note           TEXT,
    created_at           TIMESTAMP DEFAULT NOW(),
    updated_at           TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_exchange_order ON exchange_requests(order_id);

CREATE TRIGGER trg_exchange_requests_updated_at
    BEFORE UPDATE ON exchange_requests
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Trigger: Auto-insert into order_status_history on status change
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION log_order_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO order_status_history (order_id, old_status, new_status)
        VALUES (NEW.id, OLD.status, NEW.status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_order_status_log ON orders;
CREATE TRIGGER trg_order_status_log
    AFTER UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION log_order_status_change();
