-- =============================================================
-- V9: Delivery Tables
-- Tables: delivery_partners, delivery_assignments
-- =============================================================

-- -------------------------------------------------------------
-- 25. DELIVERY_PARTNERS
-- -------------------------------------------------------------
CREATE TYPE vehicle_type AS ENUM ('BICYCLE', 'MOTORCYCLE', 'SCOOTER', 'CAR');
CREATE TYPE partner_status AS ENUM ('PENDING', 'VERIFIED', 'ACTIVE', 'INACTIVE', 'SUSPENDED');

CREATE TABLE delivery_partners (
    id                  UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID          NOT NULL UNIQUE REFERENCES users(id),
    vehicle_type        vehicle_type  NOT NULL DEFAULT 'MOTORCYCLE',
    vehicle_number      VARCHAR(20)   NOT NULL,
    dl_number           VARCHAR(20)   NOT NULL UNIQUE,  -- Driving License
    aadhaar_number      VARCHAR(12)   NOT NULL UNIQUE,
    profile_photo_url   VARCHAR(500),
    dl_photo_url        VARCHAR(500),
    vehicle_rc_url      VARCHAR(500),
    current_location    GEOGRAPHY(POINT, 4326),
    is_online           BOOLEAN       NOT NULL DEFAULT FALSE,
    status              partner_status NOT NULL DEFAULT 'PENDING',
    rating              NUMERIC(3,2)  DEFAULT 0.00,
    total_deliveries    INT           NOT NULL DEFAULT 0,
    kyc_verified        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE delivery_partners IS 'Delivery partner profiles with KYC and live location';

CREATE INDEX idx_dp_user_id         ON delivery_partners(user_id);
CREATE INDEX idx_dp_location        ON delivery_partners USING GIST(current_location);
CREATE INDEX idx_dp_is_online       ON delivery_partners(is_online) WHERE is_online = TRUE;
CREATE INDEX idx_dp_status          ON delivery_partners(status);

CREATE TRIGGER trg_dp_updated_at
    BEFORE UPDATE ON delivery_partners
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -------------------------------------------------------------
-- 26. DELIVERY_ASSIGNMENTS
-- -------------------------------------------------------------
CREATE TYPE assignment_status AS ENUM (
    'ASSIGNED',
    'ACCEPTED',
    'REJECTED',
    'PICKED_UP',
    'DELIVERED',
    'FAILED'
);

CREATE TABLE delivery_assignments (
    id                  BIGSERIAL       PRIMARY KEY,
    order_id            UUID            NOT NULL REFERENCES orders(id),
    delivery_partner_id UUID            NOT NULL REFERENCES delivery_partners(id),
    status              assignment_status NOT NULL DEFAULT 'ASSIGNED',
    pickup_otp          VARCHAR(6),
    delivery_otp        VARCHAR(6),
    pickup_at           TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    distance_km         NUMERIC(7,2),
    earnings            NUMERIC(10,2),
    rejection_reason    TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE delivery_assignments IS 'Order-to-delivery-partner assignment with OTP and earnings';

CREATE INDEX idx_da_order_id            ON delivery_assignments(order_id);
CREATE INDEX idx_da_partner_id          ON delivery_assignments(delivery_partner_id);
CREATE INDEX idx_da_status              ON delivery_assignments(status);
CREATE INDEX idx_da_partner_status      ON delivery_assignments(delivery_partner_id, status);

CREATE TRIGGER trg_da_updated_at
    BEFORE UPDATE ON delivery_assignments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
