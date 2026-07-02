-- ============================================================
-- NearKart Migration V17: Audit Triggers, Soft Delete,
--   Optimistic Locking, updated_at auto-maintenance
-- Author: Bharath C | Version: 1.0
-- ============================================================

-- ============================================================
-- SECTION A: Generic updated_at trigger function
-- ============================================================
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

-- Apply to every table that has an updated_at column
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND column_name   = 'updated_at'
    LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_set_updated_at ON %I;
             CREATE TRIGGER trg_set_updated_at
             BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- ============================================================
-- SECTION B: Soft Delete
-- Add deleted_at to core tables (idempotent ALTERs)
-- ============================================================
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'users','shops','products','orders',
        'delivery_partners','coupons','reviews',
        'subscription_plans'
    ])
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = tbl AND column_name = 'deleted_at'
        ) THEN
            EXECUTE format('ALTER TABLE %I ADD COLUMN deleted_at TIMESTAMP', tbl);
        END IF;
    END LOOP;
END;
$$;

-- Partial indexes so soft-deleted rows are excluded from normal lookups
CREATE INDEX IF NOT EXISTS idx_users_active     ON users(id)    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_shops_active     ON shops(id)    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_products_active  ON products(id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_orders_active    ON orders(id)   WHERE deleted_at IS NULL;

-- ============================================================
-- SECTION C: Optimistic Locking (version column)
-- ============================================================
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'users','shops','products','inventory',
        'orders','wallets','payments'
    ])
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = tbl AND column_name = 'version'
        ) THEN
            EXECUTE format(
                'ALTER TABLE %I ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
                tbl
            );
        END IF;
    END LOOP;
END;
$$;

CREATE OR REPLACE FUNCTION fn_increment_version()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$;

DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'users','shops','products','inventory',
        'orders','wallets','payments'
    ])
    LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_increment_version ON %I;
             CREATE TRIGGER trg_increment_version
             BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION fn_increment_version();',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- ============================================================
-- SECTION D: Comprehensive Audit Trigger
--   Logs INSERT/UPDATE/DELETE to audit_logs
-- ============================================================
CREATE OR REPLACE FUNCTION fn_audit_trigger()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_old JSONB := NULL;
    v_new JSONB := NULL;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_old := to_jsonb(OLD);
    ELSIF TG_OP = 'INSERT' THEN
        v_new := to_jsonb(NEW);
    ELSE  -- UPDATE
        v_old := to_jsonb(OLD);
        v_new := to_jsonb(NEW);
    END IF;

    INSERT INTO audit_logs (action, entity_type, entity_id, old_value, new_value)
    VALUES (
        TG_OP,
        TG_TABLE_NAME,
        COALESCE(
            (CASE WHEN TG_OP = 'DELETE' THEN (v_old->>'id')::UUID
                  ELSE (v_new->>'id')::UUID END),
            NULL
        ),
        v_old,
        v_new
    );

    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END;
$$;

DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'users','shops','products','orders','payments',
        'delivery_assignments','wallets','coupons','reviews'
    ])
    LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_audit ON %I;
             CREATE TRIGGER trg_audit
             AFTER INSERT OR UPDATE OR DELETE ON %I
             FOR EACH ROW EXECUTE FUNCTION fn_audit_trigger();',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- ============================================================
-- SECTION E: Inventory Auto-Deduct on Order Item Insert
-- ============================================================
CREATE OR REPLACE FUNCTION fn_deduct_inventory()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    UPDATE inventory
    SET quantity   = quantity - NEW.quantity,
        updated_at = NOW()
    WHERE product_id = NEW.product_id
      AND quantity   >= NEW.quantity;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Insufficient stock for product %', NEW.product_id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_deduct_inventory ON order_items;
CREATE TRIGGER trg_deduct_inventory
    AFTER INSERT ON order_items
    FOR EACH ROW EXECUTE FUNCTION fn_deduct_inventory();

-- ============================================================
-- SECTION F: Rating Denormalization Trigger
-- ============================================================
CREATE OR REPLACE FUNCTION fn_update_rating()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.entity_type = 'PRODUCT' THEN
        UPDATE products
        SET avg_rating    = (SELECT ROUND(AVG(rating)::NUMERIC, 2) FROM reviews
                             WHERE entity_type='PRODUCT' AND entity_id = NEW.entity_id),
            total_ratings = (SELECT COUNT(*) FROM reviews
                             WHERE entity_type='PRODUCT' AND entity_id = NEW.entity_id)
        WHERE id = NEW.entity_id;
    ELSIF NEW.entity_type = 'SHOP' THEN
        UPDATE shops
        SET avg_rating    = (SELECT ROUND(AVG(rating)::NUMERIC, 2) FROM reviews
                             WHERE entity_type='SHOP' AND entity_id = NEW.entity_id),
            total_ratings = (SELECT COUNT(*) FROM reviews
                             WHERE entity_type='SHOP' AND entity_id = NEW.entity_id)
        WHERE id = NEW.entity_id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_update_rating ON reviews;
CREATE TRIGGER trg_update_rating
    AFTER INSERT OR UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION fn_update_rating();

-- ============================================================
-- SECTION G: Wallet Balance Audit on Transaction
-- ============================================================
CREATE OR REPLACE FUNCTION fn_wallet_balance_check()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_balance DECIMAL(12,2);
BEGIN
    SELECT balance INTO v_balance FROM wallets WHERE id = NEW.wallet_id FOR UPDATE;

    IF NEW.type = 'DEBIT' AND v_balance < NEW.amount THEN
        RAISE EXCEPTION 'Insufficient wallet balance: available %, requested %',
            v_balance, NEW.amount;
    END IF;

    IF NEW.type = 'CREDIT' THEN
        UPDATE wallets SET balance = balance + NEW.amount WHERE id = NEW.wallet_id;
    ELSE
        UPDATE wallets SET balance = balance - NEW.amount WHERE id = NEW.wallet_id;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_wallet_balance_check ON wallet_transactions;
CREATE TRIGGER trg_wallet_balance_check
    BEFORE INSERT ON wallet_transactions
    FOR EACH ROW EXECUTE FUNCTION fn_wallet_balance_check();
