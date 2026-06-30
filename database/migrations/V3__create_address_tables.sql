-- =============================================================
-- V3: Address Table
-- Tables: addresses
-- =============================================================

CREATE TYPE address_type AS ENUM ('HOME', 'WORK', 'OTHER');

CREATE TABLE addresses (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label        address_type NOT NULL DEFAULT 'HOME',
    full_address TEXT         NOT NULL,
    house_no     VARCHAR(50),
    street       VARCHAR(150),
    landmark     VARCHAR(150),
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100) NOT NULL,
    pincode      VARCHAR(10)  NOT NULL,
    location     GEOGRAPHY(POINT, 4326) NOT NULL,  -- PostGIS GPS point
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE addresses IS 'Customer delivery addresses with GPS coordinates';
COMMENT ON COLUMN addresses.location IS 'PostGIS GEOGRAPHY point: ST_MakePoint(longitude, latitude)';

CREATE INDEX idx_addresses_user_id   ON addresses(user_id);
CREATE INDEX idx_addresses_location  ON addresses USING GIST(location);  -- Spatial index
CREATE INDEX idx_addresses_pincode   ON addresses(pincode);

CREATE TRIGGER trg_addresses_updated_at
    BEFORE UPDATE ON addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
