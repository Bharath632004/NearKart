-- ============================================================
-- Auth Service Queries
-- Used by: backend/auth-service
-- ============================================================

-- Find user by email for login
SELECT id, full_name, email, phone, password_hash, role, is_enabled, is_locked
FROM users
WHERE email = :email;

-- Find user by phone
SELECT id, full_name, phone, role, is_enabled
FROM users
WHERE phone = :phone;

-- Get active OTP for identifier
SELECT id, otp_code, otp_type, expires_at
FROM otps
WHERE identifier = :identifier
  AND otp_type = :otp_type
  AND is_used = FALSE
  AND expires_at > NOW()
ORDER BY created_at DESC
LIMIT 1;

-- Mark OTP as used
UPDATE otps SET is_used = TRUE WHERE id = :id;

-- Save refresh token
INSERT INTO refresh_tokens (user_id, token, expires_at)
VALUES (:user_id, :token, :expires_at);

-- Validate refresh token
SELECT rt.id, rt.user_id, rt.expires_at, u.role, u.is_enabled, u.is_locked
FROM refresh_tokens rt
JOIN users u ON rt.user_id = u.id
WHERE rt.token = :token AND rt.is_revoked = FALSE AND rt.expires_at > NOW();

-- Revoke refresh token
UPDATE refresh_tokens SET is_revoked = TRUE WHERE token = :token;

-- Revoke all tokens for user (logout all devices)
UPDATE refresh_tokens SET is_revoked = TRUE WHERE user_id = :user_id;

-- Enable user after OTP verification
UPDATE users SET is_enabled = TRUE, updated_at = NOW() WHERE id = :user_id;
