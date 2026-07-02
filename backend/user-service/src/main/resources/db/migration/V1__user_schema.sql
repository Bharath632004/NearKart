-- NearKart User Service -- Initial Schema
-- Flyway migration V1

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    phone       VARCHAR(20)     UNIQUE,
    role        VARCHAR(30)     NOT NULL DEFAULT 'CUSTOMER',
    active      BOOLEAN         NOT NULL DEFAULT true,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS addresses (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    street      VARCHAR(300)    NOT NULL,
    city        VARCHAR(100)    NOT NULL,
    state       VARCHAR(100)    NOT NULL,
    pincode     CHAR(6)         NOT NULL,
    landmark    VARCHAR(200),
    is_default  BOOLEAN         NOT NULL DEFAULT false,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_email     ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_active    ON users(active);
CREATE INDEX IF NOT EXISTS idx_address_user   ON addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_address_default ON addresses(user_id, is_default);
