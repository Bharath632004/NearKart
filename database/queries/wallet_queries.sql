-- ============================================================
-- NearKart: Wallet Queries
-- Author: Bharath C
-- ============================================================

-- 1. Get wallet balance for a user
SELECT w.id, w.balance, w.currency, w.version, w.updated_at
FROM wallets w
WHERE w.user_id = :user_id;

-- 2. Wallet transaction history (paginated)
SELECT
  wt.id,
  wt.amount,
  wt.type,
  wt.reference_type,
  wt.reference_id,
  wt.description,
  wt.balance_after,
  wt.created_at
FROM wallet_transactions wt
JOIN wallets w ON w.id = wt.wallet_id
WHERE w.user_id = :user_id
ORDER BY wt.created_at DESC
LIMIT :limit OFFSET :offset;

-- 3. Total credits vs debits for a user (last 30 days)
SELECT
  wt.type,
  COUNT(*)             AS transaction_count,
  SUM(wt.amount)       AS total_amount
FROM wallet_transactions wt
JOIN wallets w ON w.id = wt.wallet_id
WHERE w.user_id = :user_id
  AND wt.created_at >= NOW() - INTERVAL '30 days'
GROUP BY wt.type;

-- 4. Refunds credited to wallet (traceability)
SELECT
  wt.id AS txn_id,
  wt.amount,
  wt.balance_after,
  wt.created_at,
  r.id AS refund_id,
  o.id AS order_id
FROM wallet_transactions wt
JOIN wallets w ON w.id = wt.wallet_id
LEFT JOIN refunds r ON r.id = wt.reference_id AND wt.reference_type = 'REFUND'
LEFT JOIN orders o ON o.id = r.order_id
WHERE w.user_id = :user_id AND wt.type = 'CREDIT' AND wt.reference_type = 'REFUND'
ORDER BY wt.created_at DESC;
