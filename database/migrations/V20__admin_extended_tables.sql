-- ============================================================
-- V20: Admin Extended Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: admin_profiles, login_history, system_settings,
--         feature_flags, notification_logs
-- ============================================================

-- ------------------------------------------------------------
-- Admin Profiles (extends users for admin-specific data)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin_profiles (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    department  VARCHAR(100),
    permissions JSONB   DEFAULT '[]',
    is_super    BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TRIGGER trg_admin_profiles_updated_at
    BEFORE UPDATE ON admin_profiles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Login History
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS login_history (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id),
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    device_type VARCHAR(30),
    location    VARCHAR(100),
    status      VARCHAR(10) NOT NULL CHECK (status IN ('SUCCESS','FAILED','LOCKED')),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_login_history_user ON login_history(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_history_status ON login_history(status, created_at DESC);

-- ------------------------------------------------------------
-- System Settings (key-value config store)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_settings (
    id          SERIAL PRIMARY KEY,
    key         VARCHAR(100) UNIQUE NOT NULL,
    value       TEXT,
    value_type  VARCHAR(20) DEFAULT 'STRING'
                CHECK (value_type IN ('STRING','NUMBER','BOOLEAN','JSON')),
    description TEXT,
    is_public   BOOLEAN DEFAULT FALSE,
    updated_by  UUID REFERENCES users(id),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- ------------------------------------------------------------
-- Feature Flags
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS feature_flags (
    id          SERIAL PRIMARY KEY,
    flag_name   VARCHAR(100) UNIQUE NOT NULL,
    is_enabled  BOOLEAN  DEFAULT FALSE,
    rollout_pct SMALLINT DEFAULT 100 CHECK (rollout_pct BETWEEN 0 AND 100),
    description TEXT,
    updated_by  UUID REFERENCES users(id),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- ------------------------------------------------------------
-- Notification Logs (system-level delivery log)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_logs (
    id          BIGSERIAL PRIMARY KEY,
    channel     VARCHAR(20) NOT NULL CHECK (channel IN ('EMAIL','SMS','PUSH','WHATSAPP')),
    recipient   VARCHAR(255) NOT NULL,
    template_id VARCHAR(100),
    subject     VARCHAR(255),
    body        TEXT,
    status      VARCHAR(20) DEFAULT 'SENT'
                CHECK (status IN ('SENT','DELIVERED','FAILED','BOUNCED')),
    provider_id VARCHAR(255),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notif_logs_recipient ON notification_logs(recipient, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_logs_channel   ON notification_logs(channel, status);

-- ------------------------------------------------------------
-- Seed: Default System Settings & Feature Flags
-- ------------------------------------------------------------
INSERT INTO system_settings (key, value, value_type, description, is_public) VALUES
    ('platform_commission_rate',  '10.0',  'NUMBER',  'Default platform commission % per order',         FALSE),
    ('delivery_charge_per_km',    '3.0',   'NUMBER',  'Delivery charge per km in INR',                  FALSE),
    ('free_delivery_above',       '200',   'NUMBER',  'Free delivery for orders above this value (INR)', TRUE),
    ('max_delivery_radius_km',    '10',    'NUMBER',  'Maximum allowed delivery radius in km',           FALSE),
    ('otp_expiry_minutes',        '10',    'NUMBER',  'OTP validity window in minutes',                  FALSE),
    ('max_cart_items',            '20',    'NUMBER',  'Maximum items allowed in a single cart',          FALSE),
    ('min_order_value',           '49',    'NUMBER',  'Minimum order value in INR',                      TRUE),
    ('loyalty_points_per_100',    '5',     'NUMBER',  'Loyalty points earned per INR 100 spent',         TRUE),
    ('referral_reward_amount',    '50',    'NUMBER',  'Wallet credit for successful referral in INR',    TRUE)
ON CONFLICT (key) DO NOTHING;

INSERT INTO feature_flags (flag_name, is_enabled, rollout_pct, description) VALUES
    ('ai_recommendations',   FALSE, 0,   'AI-based product recommendations'),
    ('demand_forecasting',   FALSE, 0,   'ML-based demand forecasting'),
    ('fraud_detection',      TRUE,  100, 'Real-time fraud detection'),
    ('loyalty_points',       TRUE,  100, 'Customer loyalty points program'),
    ('referral_program',     TRUE,  100, 'Customer referral reward program'),
    ('wallet_payments',      TRUE,  100, 'Wallet as a payment method'),
    ('razorpay_upi',         TRUE,  100, 'UPI payments via Razorpay'),
    ('live_order_tracking',  TRUE,  100, 'Real-time order tracking on map'),
    ('push_notifications',   TRUE,  100, 'Firebase push notifications'),
    ('dark_mode',            FALSE, 50,  'Dark mode UI (beta rollout 50%)')
ON CONFLICT (flag_name) DO NOTHING;
