-- ============================================================
-- V17: Delivery Extended Tables
-- Author: Bharath C | NearKart DB Production Readiness
-- Tables: delivery_live_location (partitioned), delivery_earnings,
--         shift_schedule, delivery_attendance, delivery_ratings,
--         delivery_proof
-- ============================================================

-- ------------------------------------------------------------
-- Live Location History (partitioned by month for scale)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_live_location (
    id          BIGSERIAL,
    partner_id  UUID          NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    order_id    UUID          REFERENCES orders(id),
    latitude    DECIMAL(10,8) NOT NULL,
    longitude   DECIMAL(11,8) NOT NULL,
    accuracy    DECIMAL(8,2),
    recorded_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, recorded_at)
) PARTITION BY RANGE (recorded_at);

-- Initial partitions (extend via cron monthly)
CREATE TABLE IF NOT EXISTS delivery_live_location_2026_07
    PARTITION OF delivery_live_location
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE IF NOT EXISTS delivery_live_location_2026_08
    PARTITION OF delivery_live_location
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE TABLE IF NOT EXISTS delivery_live_location_2026_09
    PARTITION OF delivery_live_location
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

CREATE INDEX IF NOT EXISTS idx_dloc_partner_time ON delivery_live_location(partner_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_dloc_order        ON delivery_live_location(order_id);

-- ------------------------------------------------------------
-- Delivery Earnings
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_earnings (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id     UUID NOT NULL REFERENCES delivery_partners(id),
    assignment_id  UUID REFERENCES delivery_assignments(id),
    date           DATE NOT NULL,
    base_pay       DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    distance_bonus DECIMAL(10,2) DEFAULT 0.0,
    tip            DECIMAL(10,2) DEFAULT 0.0,
    surge_pay      DECIMAL(10,2) DEFAULT 0.0,
    deductions     DECIMAL(10,2) DEFAULT 0.0,
    net_earnings   DECIMAL(10,2) GENERATED ALWAYS AS
                   (base_pay + distance_bonus + tip + surge_pay - deductions) STORED,
    is_paid        BOOLEAN   DEFAULT FALSE,
    paid_at        TIMESTAMP,
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_earnings_partner_date ON delivery_earnings(partner_id, date DESC);
CREATE INDEX IF NOT EXISTS idx_earnings_unpaid       ON delivery_earnings(partner_id, is_paid) WHERE is_paid = FALSE;

-- ------------------------------------------------------------
-- Shift Schedule
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shift_schedule (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id   UUID NOT NULL REFERENCES delivery_partners(id),
    shift_date   DATE NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    status       VARCHAR(20) DEFAULT 'SCHEDULED'
                 CHECK (status IN ('SCHEDULED','ACTIVE','COMPLETED','MISSED','CANCELLED')),
    check_in_at  TIMESTAMP,
    check_out_at TIMESTAMP,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shift_partner_date ON shift_schedule(partner_id, shift_date);

-- ------------------------------------------------------------
-- Attendance
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_attendance (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id  UUID NOT NULL REFERENCES delivery_partners(id),
    date        DATE NOT NULL,
    is_present  BOOLEAN  DEFAULT FALSE,
    login_at    TIMESTAMP,
    logout_at   TIMESTAMP,
    total_hours DECIMAL(5,2),
    UNIQUE (partner_id, date)
);

CREATE INDEX IF NOT EXISTS idx_attendance_partner_date ON delivery_attendance(partner_id, date DESC);

-- ------------------------------------------------------------
-- Delivery Ratings (dedicated, separate from generic reviews)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_ratings (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id UUID UNIQUE NOT NULL REFERENCES delivery_assignments(id),
    customer_id   UUID NOT NULL REFERENCES users(id),
    partner_id    UUID NOT NULL REFERENCES delivery_partners(id),
    rating        SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       TEXT,
    is_anonymous  BOOLEAN  DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_del_ratings_partner ON delivery_ratings(partner_id, created_at DESC);

-- Auto-update delivery_partners.avg_rating on new delivery rating
CREATE OR REPLACE FUNCTION update_partner_avg_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE delivery_partners
    SET avg_rating = (
        SELECT COALESCE(AVG(rating)::DECIMAL(3,2), 0.0)
        FROM delivery_ratings
        WHERE partner_id = NEW.partner_id
    )
    WHERE id = NEW.partner_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_partner_rating
    AFTER INSERT OR UPDATE ON delivery_ratings
    FOR EACH ROW EXECUTE FUNCTION update_partner_avg_rating();

-- ------------------------------------------------------------
-- Delivery Proof
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_proof (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id UUID UNIQUE NOT NULL REFERENCES delivery_assignments(id),
    photo_url     TEXT,
    signature_url TEXT,
    otp_verified  BOOLEAN  DEFAULT FALSE,
    geo_lat       DECIMAL(10,8),
    geo_lng       DECIMAL(11,8),
    delivered_at  TIMESTAMP DEFAULT NOW()
);
