-- =============================================================
-- V11: Audit & Activity Log Tables
-- Tables: audit_logs, activity_logs
-- =============================================================

-- -------------------------------------------------------------
-- 34. AUDIT_LOGS
-- -------------------------------------------------------------
CREATE TABLE audit_logs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     UUID         REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,  -- e.g. USER_CREATED, ORDER_CANCELLED
    entity_type VARCHAR(100) NOT NULL,  -- e.g. users, orders, shops
    entity_id   VARCHAR(100) NOT NULL,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  INET,
    user_agent  TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE audit_logs IS 'Immutable audit trail of all significant platform actions';

CREATE TABLE audit_logs_2026 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE audit_logs_2027 PARTITION OF audit_logs
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE INDEX idx_audit_logs_user_id     ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity      ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action      ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at  ON audit_logs(created_at DESC);

-- -------------------------------------------------------------
-- 35. ACTIVITY_LOGS
-- -------------------------------------------------------------
CREATE TABLE activity_logs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     UUID         REFERENCES users(id) ON DELETE SET NULL,
    session_id  UUID         REFERENCES sessions(id) ON DELETE SET NULL,
    event       VARCHAR(100) NOT NULL,  -- e.g. PRODUCT_VIEWED, SEARCH_PERFORMED
    properties  JSONB,
    ip_address  INET,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE activity_logs IS 'User behaviour events for analytics and ML features';

CREATE TABLE activity_logs_2026 PARTITION OF activity_logs
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE activity_logs_2027 PARTITION OF activity_logs
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE INDEX idx_activity_logs_user_id    ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_event      ON activity_logs(event);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at DESC);
