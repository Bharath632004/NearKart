-- ─── V2: Performance Indexes ────────────────────────────────────────────────
-- Adds composite and covering indexes for the most frequent query patterns.

-- Merchants: fast lookup by status (admin listing, analytics)
CREATE INDEX IF NOT EXISTS idx_merchants_status
    ON merchants (status);

-- Merchants: email lookup during login/registration
CREATE INDEX IF NOT EXISTS idx_merchants_email
    ON merchants (email);

-- KYC Documents: merchant + verification status (admin pending-docs listing)
CREATE INDEX IF NOT EXISTS idx_kyc_merchant_verified
    ON kyc_documents (merchant_id, verified);

-- Shops: active shops per merchant (merchant dashboard)
CREATE INDEX IF NOT EXISTS idx_shops_merchant_active
    ON shops (merchant_id, is_active);

-- Shops: category filter for nearby search
CREATE INDEX IF NOT EXISTS idx_shops_category_active
    ON shops (category, is_active);

-- Promotions: date-range filtered active promotions (very hot path)
CREATE INDEX IF NOT EXISTS idx_promotions_shop_active_dates
    ON promotions (shop_id, is_active, start_date, end_date);

-- Promotions: promo-code lookup at checkout
CREATE INDEX IF NOT EXISTS idx_promotions_promo_code_active
    ON promotions (promo_code, is_active)
    WHERE promo_code IS NOT NULL;

-- Settlements: merchant + status for payout queries
CREATE INDEX IF NOT EXISTS idx_settlements_merchant_status
    ON settlements (merchant_id, status);

-- Settlements: date-range queries for reporting
CREATE INDEX IF NOT EXISTS idx_settlements_period
    ON settlements (period_start, period_end);
