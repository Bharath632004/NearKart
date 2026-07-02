-- ============================================================
-- NearKart V23 – Complete Audit Tables & Row-Level Triggers
-- Author: Bharath C | Date: 2026-07-02
-- ============================================================

-- ============================================================
-- 1. GENERIC AUDIT TABLE (already exists as audit_logs)
--    Add missing columns for completeness
-- ============================================================

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS user_agent   TEXT,
    ADD COLUMN IF NOT EXISTS request_id   UUID,
    ADD COLUMN IF NOT EXISTS session_id   UUID;

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at  ON audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action       ON audit_logs(action);

-- ============================================================
-- 2. ORDER AUDIT TABLE – immutable order status history
-- ============================================================

CREATE TABLE IF NOT EXISTS order_status_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status  VARCHAR(30),
    new_status  VARCHAR(30) NOT NULL,
    changed_by  UUID REFERENCES users(id),
    note        TEXT,
    changed_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history(order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_at       ON order_status_history(changed_at DESC);

-- Trigger: auto-log order status changes
CREATE OR REPLACE FUNCTION fn_audit_order_status()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO order_status_history(order_id, old_status, new_status, changed_at)
        VALUES (NEW.id, OLD.status, NEW.status, NOW());
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_order_status_audit ON orders;
CREATE TRIGGER trg_order_status_audit
    AFTER UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION fn_audit_order_status();

-- ============================================================
-- 3. PAYMENT AUDIT TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS payment_audit_log (
    id             BIGSERIAL PRIMARY KEY,
    payment_id     UUID NOT NULL REFERENCES payments(id),
    old_status     VARCHAR(20),
    new_status     VARCHAR(20) NOT NULL,
    gateway_event  VARCHAR(100),
    raw_payload    JSONB,
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payment_audit_payment_id ON payment_audit_log(payment_id);

CREATE OR REPLACE FUNCTION fn_audit_payment_status()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO payment_audit_log(payment_id, old_status, new_status, created_at)
        VALUES (NEW.id, OLD.status, NEW.status, NOW());
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_payment_status_audit ON payments;
CREATE TRIGGER trg_payment_status_audit
    AFTER UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION fn_audit_payment_status();

-- ============================================================
-- 4. DELIVERY AUDIT TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS delivery_status_history (
    id             BIGSERIAL PRIMARY KEY,
    assignment_id  UUID NOT NULL REFERENCES delivery_assignments(id) ON DELETE CASCADE,
    old_status     VARCHAR(20),
    new_status     VARCHAR(20) NOT NULL,
    lat            DECIMAL(10,8),
    lng            DECIMAL(11,8),
    note           TEXT,
    changed_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_delivery_audit_assignment ON delivery_status_history(assignment_id);

CREATE OR REPLACE FUNCTION fn_audit_delivery_status()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO delivery_status_history(assignment_id, old_status, new_status, changed_at)
        VALUES (NEW.id, OLD.status, NEW.status, NOW());
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_delivery_status_audit ON delivery_assignments;
CREATE TRIGGER trg_delivery_status_audit
    AFTER UPDATE ON delivery_assignments
    FOR EACH ROW EXECUTE FUNCTION fn_audit_delivery_status();

-- ============================================================
-- 5. INVENTORY AUDIT TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_audit_log (
    id            BIGSERIAL PRIMARY KEY,
    inventory_id  UUID NOT NULL REFERENCES inventory(id),
    product_id    UUID NOT NULL REFERENCES products(id),
    old_quantity  INT,
    new_quantity  INT NOT NULL,
    change_reason VARCHAR(50) CHECK (change_reason IN ('PURCHASE','RETURN','ADJUSTMENT','RESTOCK','DAMAGE')),
    changed_by    UUID REFERENCES users(id),
    changed_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inventory_audit_product ON inventory_audit_log(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_audit_date    ON inventory_audit_log(changed_at DESC);

CREATE OR REPLACE FUNCTION fn_audit_inventory()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.quantity <> OLD.quantity THEN
        INSERT INTO inventory_audit_log(inventory_id, product_id, old_quantity, new_quantity, changed_at)
        VALUES (NEW.id, NEW.product_id, OLD.quantity, NEW.quantity, NOW());
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_inventory_audit ON inventory;
CREATE TRIGGER trg_inventory_audit
    AFTER UPDATE ON inventory
    FOR EACH ROW EXECUTE FUNCTION fn_audit_inventory();

-- ============================================================
-- 6. SHOP VERIFICATION AUDIT
-- ============================================================

CREATE TABLE IF NOT EXISTS shop_verification_log (
    id           BIGSERIAL PRIMARY KEY,
    shop_id      UUID NOT NULL REFERENCES shops(id),
    verified_by  UUID REFERENCES users(id),
    action       VARCHAR(20) NOT NULL CHECK (action IN ('APPROVED','REJECTED','SUSPENDED','REACTIVATED')),
    reason       TEXT,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shop_verification_shop_id ON shop_verification_log(shop_id);

-- ============================================================
-- 7. USER LOGIN AUDIT
-- ============================================================

CREATE TABLE IF NOT EXISTS user_login_history (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    login_type  VARCHAR(20) DEFAULT 'PASSWORD' CHECK (login_type IN ('PASSWORD','OTP','GOOGLE','FACEBOOK')),
    is_success  BOOLEAN DEFAULT TRUE,
    fail_reason VARCHAR(100),
    logged_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_login_history_user_id ON user_login_history(user_id);
CREATE INDEX IF NOT EXISTS idx_user_login_history_date    ON user_login_history(logged_at DESC);
