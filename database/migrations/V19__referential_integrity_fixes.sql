-- ============================================================
-- NearKart Migration V19: Referential Integrity Fixes
-- Author: Bharath C | Version: 1.0
-- Adds: missing FKs, orphan-cleanup, constraint hardening
-- ============================================================

-- -------------------------------------------------------
-- 1. Ensure order_items.product_id has a proper FK
--    (schema.sql has it but earlier migrations may lack it)
-- -------------------------------------------------------
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
        WHERE tc.table_name = 'order_items'
          AND tc.constraint_type = 'FOREIGN KEY'
          AND kcu.column_name = 'product_id'
    ) THEN
        ALTER TABLE order_items
            ADD CONSTRAINT fk_oi_product FOREIGN KEY (product_id)
                REFERENCES products(id) ON DELETE RESTRICT;
    END IF;
END $$;

-- -------------------------------------------------------
-- 2. commissions.merchant_id FK (missing cascade rule)
-- -------------------------------------------------------
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
        WHERE tc.table_name = 'commissions'
          AND kcu.column_name = 'merchant_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE commissions
            ADD CONSTRAINT fk_commission_merchant FOREIGN KEY (merchant_id)
                REFERENCES users(id) ON DELETE RESTRICT;
    END IF;
END $$;

-- -------------------------------------------------------
-- 3. complaints.order_id - already nullable FK, ensure
--    ON DELETE SET NULL semantics
-- -------------------------------------------------------
ALTER TABLE complaints DROP CONSTRAINT IF EXISTS complaints_order_id_fkey;
ALTER TABLE complaints
    ADD CONSTRAINT fk_complaint_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE SET NULL;

-- -------------------------------------------------------
-- 4. Clean orphan cart_items whose cart no longer exists
-- -------------------------------------------------------
DELETE FROM cart_items
WHERE cart_id NOT IN (SELECT id FROM carts);

-- -------------------------------------------------------
-- 5. Clean orphan order_items whose order no longer exists
-- -------------------------------------------------------
DELETE FROM order_items
WHERE order_id NOT IN (SELECT id FROM orders);

-- -------------------------------------------------------
-- 6. Clean orphan wallet_transactions
-- -------------------------------------------------------
DELETE FROM wallet_transactions
WHERE wallet_id NOT IN (SELECT id FROM wallets);

-- -------------------------------------------------------
-- 7. Clean orphan refresh_tokens for deleted users
-- -------------------------------------------------------
DELETE FROM refresh_tokens
WHERE user_id NOT IN (SELECT id FROM users);

-- -------------------------------------------------------
-- 8. Clean orphan notifications
-- -------------------------------------------------------
DELETE FROM notifications
WHERE user_id NOT IN (SELECT id FROM users);

-- -------------------------------------------------------
-- 9. Ensure delivery_assignments has a proper FK for
--    partner_id with correct CASCADE
-- -------------------------------------------------------
ALTER TABLE delivery_assignments DROP CONSTRAINT IF EXISTS delivery_assignments_partner_id_fkey;
ALTER TABLE delivery_assignments
    ADD CONSTRAINT fk_da_partner FOREIGN KEY (partner_id)
        REFERENCES delivery_partners(id) ON DELETE RESTRICT;

-- -------------------------------------------------------
-- 10. settlements.merchant_id - add ON DELETE RESTRICT
-- -------------------------------------------------------
ALTER TABLE settlements DROP CONSTRAINT IF EXISTS settlements_merchant_id_fkey;
ALTER TABLE settlements
    ADD CONSTRAINT fk_settlement_merchant FOREIGN KEY (merchant_id)
        REFERENCES users(id) ON DELETE RESTRICT;

-- -------------------------------------------------------
-- 11. Verify no orphan inventory records
-- -------------------------------------------------------
DELETE FROM inventory
WHERE product_id NOT IN (SELECT id FROM products);

-- -------------------------------------------------------
-- 12. Add check: payment amount must equal order total
--    (advisory constraint, non-blocking via trigger)
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_check_payment_amount()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_order_total DECIMAL(12,2);
BEGIN
    SELECT total_amount INTO v_order_total
    FROM orders WHERE id = NEW.order_id;

    IF NEW.amount <> v_order_total THEN
        RAISE WARNING 'Payment amount % does not match order total % for order %',
            NEW.amount, v_order_total, NEW.order_id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_check_payment_amount ON payments;
CREATE TRIGGER trg_check_payment_amount
    BEFORE INSERT ON payments
    FOR EACH ROW EXECUTE FUNCTION fn_check_payment_amount();

-- -------------------------------------------------------
-- 13. Prevent duplicate default addresses per user
-- -------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS idx_addresses_one_default
    ON addresses(user_id)
    WHERE is_default = TRUE;
