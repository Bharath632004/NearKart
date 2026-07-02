-- ============================================================
-- V24: Security Hardening - Views, RLS, Constraints, Comments
-- Author: Bharath C | NearKart DB Production Readiness
-- ============================================================

-- ------------------------------------------------------------
-- Soft Delete Views (application layer MUST use these)
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW active_users AS
    SELECT * FROM users
    WHERE deleted_at IS NULL;

CREATE OR REPLACE VIEW active_products AS
    SELECT * FROM products
    WHERE deleted_at IS NULL AND is_active = TRUE;

CREATE OR REPLACE VIEW active_shops AS
    SELECT * FROM shops
    WHERE deleted_at IS NULL AND is_active = TRUE AND is_verified = TRUE;

-- ------------------------------------------------------------
-- Additional Business Constraints
-- ------------------------------------------------------------

-- Orders: total must be positive
ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_order_total_positive;
ALTER TABLE orders
    ADD CONSTRAINT chk_order_total_positive CHECK (total_amount > 0);

-- Refunds: amount must be positive
ALTER TABLE refunds
    DROP CONSTRAINT IF EXISTS chk_refund_positive;
ALTER TABLE refunds
    ADD CONSTRAINT chk_refund_positive CHECK (amount > 0);

-- Settlements: net_amount = total_sales - commission
ALTER TABLE settlements
    DROP CONSTRAINT IF EXISTS chk_settlement_net;
ALTER TABLE settlements
    ADD CONSTRAINT chk_settlement_net
    CHECK (net_amount = total_sales - commission);

-- Products: selling_price must be > 0
ALTER TABLE products
    DROP CONSTRAINT IF EXISTS chk_products_positive_price;
ALTER TABLE products
    ADD CONSTRAINT chk_products_positive_price CHECK (selling_price > 0 AND mrp > 0);

-- ------------------------------------------------------------
-- Row Level Security (Wallet isolation)
-- ------------------------------------------------------------
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallet_transactions ENABLE ROW LEVEL SECURITY;

-- Policies applied via application DB roles (app_user, app_admin)
-- Example: CREATE POLICY wallet_owner ON wallets FOR SELECT
--          USING (current_setting('app.current_user_id')::UUID = user_id);
-- Note: Enable policies when DB roles are configured in Spring Boot datasource.

-- ------------------------------------------------------------
-- Sensitive Field Encryption Reminders
-- ------------------------------------------------------------
COMMENT ON COLUMN delivery_partners.aadhaar_number IS
    'SECURITY: Store AES-256 encrypted value at application layer. Display only last 4 digits.';

COMMENT ON COLUMN users.password_hash IS
    'SECURITY: BCrypt (strength=12) or Argon2id. Never store plaintext.';

COMMENT ON COLUMN otps.otp_code IS
    'SECURITY: Store BCrypt hash of OTP, not plaintext. Compare using hash match.';

COMMENT ON COLUMN delivery_partners.license_number IS
    'SECURITY: Encrypt at rest. Only last 4 chars for display.';

COMMENT ON COLUMN users.referral_code IS
    'Generate as UPPERCASE 8-char alphanumeric at user registration.';

-- ------------------------------------------------------------
-- Audit Trail: Prevent tampering with audit_logs
-- ------------------------------------------------------------
ALTER TABLE audit_logs
    DROP CONSTRAINT IF EXISTS chk_audit_not_future;
ALTER TABLE audit_logs
    ADD CONSTRAINT chk_audit_not_future
    CHECK (created_at <= NOW() + INTERVAL '1 minute');

-- Revoke DELETE on audit tables from application role
-- (Run as superuser during deployment setup)
-- REVOKE DELETE ON audit_logs FROM app_user;
-- REVOKE DELETE ON order_status_history FROM app_user;
-- REVOKE UPDATE ON audit_logs FROM app_user;

-- ------------------------------------------------------------
-- Final Summary Comment
-- ------------------------------------------------------------
COMMENT ON SCHEMA public IS
    'NearKart v2.1 Production Schema | Migrations V1-V24 | PostgreSQL 15+ with PostGIS | Author: Bharath C';
