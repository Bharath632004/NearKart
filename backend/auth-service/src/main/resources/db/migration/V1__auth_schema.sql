-- NearKart Auth Service — Initial Schema
-- Flyway migration V1

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    PRIMARY KEY,
    phone         VARCHAR(15)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_role  ON users(role);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rt_token   ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_rt_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_rt_revoked ON refresh_tokens(revoked);

-- Default admin user
-- Password: Admin@1234 (BCrypt $2a$12 hash) — CHANGE IMMEDIATELY IN PRODUCTION
INSERT INTO users (phone, password_hash, role, active)
VALUES ('9999999999',
        '$2a$12$8e.bY5K4cXlP3xTzQpUAYujk/s7vWkFZZypmVERrH2q3FKxJTjq4W',
        'ADMIN', TRUE)
ON CONFLICT DO NOTHING;
