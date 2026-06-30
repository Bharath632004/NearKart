-- =============================================================
-- V6: Inventory Table
-- Tables: inventory
-- =============================================================

CREATE TABLE inventory (
    id              BIGSERIAL    PRIMARY KEY,
    product_id      UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    shop_id         UUID         NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    quantity        INT          NOT NULL DEFAULT 0,
    low_stock_alert INT          NOT NULL DEFAULT 5,
    max_stock       INT          NOT NULL DEFAULT 1000,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(product_id, shop_id),
    CONSTRAINT chk_quantity_non_negative CHECK (quantity >= 0)
);

COMMENT ON TABLE inventory IS 'Real-time stock levels per product per shop';
COMMENT ON COLUMN inventory.low_stock_alert IS 'Send alert when quantity falls below this value';

CREATE INDEX idx_inventory_shop_id    ON inventory(shop_id);
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_low_stock  ON inventory(shop_id) WHERE quantity <= low_stock_alert;

CREATE TRIGGER trg_inventory_updated_at
    BEFORE UPDATE ON inventory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
