-- ============================================================
-- NearKart Migration V21: Complete Delivery Tables
-- Author: Bharath C | Version: 1.0
-- Covers: partner earnings, location history, route events,
--         delivery SLA config, partner availability slots
-- ============================================================

-- -------------------------------------------------------
-- 1. DELIVERY PARTNER EARNINGS
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS partner_earnings (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id      UUID    NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    assignment_id   UUID    REFERENCES delivery_assignments(id),
    earning_type    VARCHAR(30) NOT NULL
                    CHECK (earning_type IN ('DELIVERY_FEE','TIP','BONUS','INCENTIVE','DEDUCTION')),
    amount          DECIMAL(10,2) NOT NULL,
    description     TEXT,
    earned_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pe_partner_date  ON partner_earnings(partner_id, earned_at DESC);
CREATE INDEX IF NOT EXISTS idx_pe_assignment    ON partner_earnings(assignment_id);

-- -------------------------------------------------------
-- 2. PARTNER LOCATION HISTORY  (GPS breadcrumb)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS partner_location_history (
    id              BIGSERIAL   PRIMARY KEY,
    partner_id      UUID        NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    latitude        DECIMAL(10,8) NOT NULL,
    longitude       DECIMAL(11,8) NOT NULL,
    accuracy_m      DECIMAL(8,2),
    bearing         DECIMAL(6,2),
    speed_kmph      DECIMAL(6,2),
    recorded_at     TIMESTAMP   NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (recorded_at);

CREATE TABLE IF NOT EXISTS partner_location_history_default
    PARTITION OF partner_location_history DEFAULT;

CREATE INDEX IF NOT EXISTS idx_plh_partner_time
    ON partner_location_history(partner_id, recorded_at DESC);

-- -------------------------------------------------------
-- 3. DELIVERY ROUTE EVENTS  (status transitions)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_route_events (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id   UUID        NOT NULL REFERENCES delivery_assignments(id) ON DELETE CASCADE,
    event_type      VARCHAR(40) NOT NULL
                    CHECK (event_type IN (
                        'ACCEPTED','REACHED_SHOP','PICKED_UP',
                        'EN_ROUTE','REACHED_CUSTOMER','DELIVERED','FAILED'
                    )),
    latitude        DECIMAL(10,8),
    longitude       DECIMAL(11,8),
    notes           TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dre_assignment ON delivery_route_events(assignment_id, created_at);

-- -------------------------------------------------------
-- 4. DELIVERY SLA CONFIG  (per shop or global)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_sla_config (
    id                  UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id             UUID    REFERENCES shops(id) ON DELETE CASCADE,  -- NULL = global default
    category_id         INT     REFERENCES shop_categories(id),
    max_delivery_mins   INT     NOT NULL DEFAULT 45,
    priority_order      INT     NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (shop_id, category_id)
);

-- -------------------------------------------------------
-- 5. PARTNER AVAILABILITY SLOTS  (working shift schedule)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS partner_availability_slots (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id      UUID        NOT NULL REFERENCES delivery_partners(id) ON DELETE CASCADE,
    day_of_week     SMALLINT    NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (partner_id, day_of_week, start_time),
    CONSTRAINT chk_slot_order CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_pas_partner_day ON partner_availability_slots(partner_id, day_of_week);

-- -------------------------------------------------------
-- 6. DELIVERY TIPS  (customer tips per assignment)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_tips (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id   UUID    UNIQUE NOT NULL REFERENCES delivery_assignments(id) ON DELETE CASCADE,
    customer_id     UUID    NOT NULL REFERENCES users(id),
    amount          DECIMAL(8,2) NOT NULL CHECK (amount > 0),
    paid_at         TIMESTAMP NOT NULL DEFAULT NOW()
);
