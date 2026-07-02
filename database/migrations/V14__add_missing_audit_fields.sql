-- ============================================================
-- V14: Add Missing Audit Fields to Core Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Adds: deleted_at (soft delete), version (optimistic lock),
--       updated_at where missing, and auto-update triggers
-- ============================================================

-- users: soft delete + optimistic locking
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS deleted_at   TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version      BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS referral_code VARCHAR(20) UNIQUE;

CREATE INDEX IF NOT EXISTS idx_users_deleted_at    ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_referral_code ON users(referral_code) WHERE referral_code IS NOT NULL;

-- addresses: updated_at + soft delete
ALTER TABLE addresses
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- products: soft delete + GST metadata
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tax_rate   DECIMAL(5,2) DEFAULT 18.0,
    ADD COLUMN IF NOT EXISTS hsn_code   VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_products_deleted_at ON products(deleted_at) WHERE deleted_at IS NULL;

-- inventory: created_at + reserved stock (for cart reservations)
ALTER TABLE inventory
    ADD COLUMN IF NOT EXISTS created_at        TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS reserved_quantity INT NOT NULL DEFAULT 0
        CHECK (reserved_quantity >= 0);

-- orders: optimistic locking + soft delete
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS version    BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- shops: soft delete + optimistic locking
ALTER TABLE shops
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version    BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_shops_deleted_at ON shops(deleted_at) WHERE deleted_at IS NULL;

-- wallets: missing created_at
ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

-- delivery_partners: soft delete + optimistic locking
ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version    BIGINT NOT NULL DEFAULT 0;

-- delivery_assignments: missing audit timestamps
ALTER TABLE delivery_assignments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- coupons: updated_at + soft delete
ALTER TABLE coupons
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- reviews: updated_at + soft delete
ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- ============================================================
-- Universal updated_at trigger function
-- ============================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all mutable tables
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'users','addresses','products','orders','shops',
        'carts','wallets','delivery_partners','delivery_assignments',
        'coupons','reviews','payments'
    ] LOOP
        EXECUTE format('
            DROP TRIGGER IF EXISTS trg_%s_updated_at ON %I;
            CREATE TRIGGER trg_%s_updated_at
            BEFORE UPDATE ON %I
            FOR EACH ROW EXECUTE FUNCTION set_updated_at();
        ', t, t, t, t);
    END LOOP;
END;
$$;
