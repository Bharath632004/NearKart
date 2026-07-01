-- =============================================================
-- V13: Data integrity and audit timestamp fixes
-- NearKart Database
-- =============================================================

CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_cart_item_shop()
RETURNS TRIGGER AS $$
DECLARE
    cart_shop_id UUID;
    product_shop_id UUID;
BEGIN
    SELECT shop_id INTO cart_shop_id
    FROM carts
    WHERE id = NEW.cart_id;

    SELECT shop_id INTO product_shop_id
    FROM products
    WHERE id = NEW.product_id;

    IF product_shop_id IS NULL THEN
        RAISE EXCEPTION 'Product % does not exist or has no shop', NEW.product_id;
    END IF;

    IF cart_shop_id IS NULL THEN
        UPDATE carts
        SET shop_id = product_shop_id, updated_at = NOW()
        WHERE id = NEW.cart_id;
    ELSIF cart_shop_id <> product_shop_id THEN
        RAISE EXCEPTION 'Cannot add products from different shops into the same cart';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS chk_products_selling_price,
    ADD CONSTRAINT chk_products_selling_price CHECK (selling_price <= mrp);

ALTER TABLE coupons
    DROP CONSTRAINT IF EXISTS chk_coupon_usage_non_negative,
    DROP CONSTRAINT IF EXISTS chk_coupon_usage_limit,
    ADD CONSTRAINT chk_coupon_usage_non_negative CHECK (used_count >= 0),
    ADD CONSTRAINT chk_coupon_usage_limit CHECK (usage_limit IS NULL OR used_count <= usage_limit);

ALTER TABLE order_items
    DROP CONSTRAINT IF EXISTS chk_order_items_total_price,
    ADD CONSTRAINT chk_order_items_total_price CHECK (total_price = quantity * unit_price);

DROP TRIGGER IF EXISTS set_updated_at_users ON users;
CREATE TRIGGER set_updated_at_users
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_shops ON shops;
CREATE TRIGGER set_updated_at_shops
BEFORE UPDATE ON shops
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_products ON products;
CREATE TRIGGER set_updated_at_products
BEFORE UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_inventory ON inventory;
CREATE TRIGGER set_updated_at_inventory
BEFORE UPDATE ON inventory
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_wallets ON wallets;
CREATE TRIGGER set_updated_at_wallets
BEFORE UPDATE ON wallets
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_orders ON orders;
CREATE TRIGGER set_updated_at_orders
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_payments ON payments;
CREATE TRIGGER set_updated_at_payments
BEFORE UPDATE ON payments
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_delivery_partners ON delivery_partners;
CREATE TRIGGER set_updated_at_delivery_partners
BEFORE UPDATE ON delivery_partners
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS set_updated_at_carts ON carts;
CREATE TRIGGER set_updated_at_carts
BEFORE UPDATE ON carts
FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

DROP TRIGGER IF EXISTS trg_validate_cart_item_shop ON cart_items;
CREATE TRIGGER trg_validate_cart_item_shop
BEFORE INSERT OR UPDATE ON cart_items
FOR EACH ROW EXECUTE FUNCTION validate_cart_item_shop();
