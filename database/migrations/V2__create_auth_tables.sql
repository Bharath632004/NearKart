-- =============================================================
-- V2: Authentication & Authorization Tables
-- Tables: users, roles, permissions, role_permissions,
--         otp_records, refresh_tokens, sessions
-- =============================================================

-- -------------------------------------------------------------
-- 1. ROLES
-- -------------------------------------------------------------
CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE roles IS 'Platform roles: CUSTOMER, MERCHANT, DELIVERY_PARTNER, ADMIN';

INSERT INTO roles (name, description) VALUES
    ('CUSTOMER',         'End customer who places orders'),
    ('MERCHANT',         'Shop owner who lists products'),
    ('DELIVERY_PARTNER', 'Delivery agent who fulfils orders'),
    ('ADMIN',            'Platform administrator');

-- -------------------------------------------------------------
-- 2. PERMISSIONS
-- -------------------------------------------------------------
CREATE TABLE permissions (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE permissions IS 'Granular permissions for RBAC';

-- -------------------------------------------------------------
-- 3. ROLE_PERMISSIONS (junction)
-- -------------------------------------------------------------
CREATE TABLE role_permissions (
    role_id       INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- -------------------------------------------------------------
-- 4. USERS
-- -------------------------------------------------------------
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone         VARCHAR(15)  NOT NULL UNIQUE,
    email         VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    profile_photo VARCHAR(500),
    role_id       INT          NOT NULL REFERENCES roles(id),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE users IS 'All platform users: customers, merchants, delivery partners, admins';
COMMENT ON COLUMN users.phone IS '10-digit Indian mobile number, primary login identifier';

CREATE INDEX idx_users_phone       ON users(phone);
CREATE INDEX idx_users_email       ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_role_id     ON users(role_id);
CREATE INDEX idx_users_is_active   ON users(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_users_is_deleted  ON users(is_deleted) WHERE is_deleted = FALSE;

-- auto-update updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -------------------------------------------------------------
-- 5. OTP_RECORDS
-- -------------------------------------------------------------
CREATE TYPE otp_purpose AS ENUM (
    'REGISTRATION',
    'LOGIN',
    'FORGOT_PASSWORD',
    'DELIVERY_VERIFICATION'
);

CREATE TABLE otp_records (
    id         BIGSERIAL    PRIMARY KEY,
    phone      VARCHAR(15)  NOT NULL,
    otp        VARCHAR(6)   NOT NULL,
    purpose    otp_purpose  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    is_used    BOOLEAN      NOT NULL DEFAULT FALSE,
    attempts   SMALLINT     NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE otp_records IS 'OTP log with purpose, expiry, and attempt tracking';

CREATE INDEX idx_otp_phone_purpose ON otp_records(phone, purpose, is_used);
CREATE INDEX idx_otp_expires_at    ON otp_records(expires_at);

-- -------------------------------------------------------------
-- 6. REFRESH_TOKENS
-- -------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    is_revoked BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens stored for revocation support';

CREATE INDEX idx_refresh_tokens_user_id   ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token     ON refresh_tokens(token) WHERE is_revoked = FALSE;

-- -------------------------------------------------------------
-- 7. SESSIONS
-- -------------------------------------------------------------
CREATE TABLE sessions (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_info VARCHAR(255),
    ip_address  INET,
    user_agent  TEXT,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    last_seen   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sessions IS 'Active login sessions per device for audit and security';

CREATE INDEX idx_sessions_user_id   ON sessions(user_id);
CREATE INDEX idx_sessions_is_active ON sessions(is_active) WHERE is_active = TRUE;
