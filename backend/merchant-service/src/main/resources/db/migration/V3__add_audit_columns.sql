-- Add audit tracking columns to merchants table
ALTER TABLE merchants
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP;

-- Add last_updated_by to shops table
ALTER TABLE shops
    ADD COLUMN IF NOT EXISTS last_updated_by VARCHAR(255);

-- Add rejection reason to merchants
ALTER TABLE merchants
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
