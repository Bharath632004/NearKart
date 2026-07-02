-- ============================================================
-- NearKart: Payment Queries
-- Author: Bharath C
-- ============================================================

-- 1. Get payment by order ID with order details
SELECT p.*, o.customer_id, o.total_amount AS order_total
FROM payments p
JOIN orders o ON o.id = p.order_id
WHERE p.order_id = :order_id;

-- 2. Get all pending payments older than 15 minutes (timed-out)
SELECT p.id, p.order_id, p.gateway_order_id, p.amount, p.created_at
FROM payments p
WHERE p.status = 'INITIATED'
  AND p.created_at < NOW() - INTERVAL '15 minutes';

-- 3. Get refund history for an order
SELECT r.id, r.amount, r.method, r.status, r.processed_at,
       ret.reason AS return_reason
FROM refunds r
LEFT JOIN returns ret ON ret.id = r.return_id
WHERE r.order_id = :order_id
ORDER BY r.created_at DESC;

-- 4. Daily payment success rate
SELECT
  DATE(created_at)           AS day,
  COUNT(*)                   AS total_attempts,
  SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) AS successful,
  ROUND(100.0 * SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) / COUNT(*), 2) AS success_rate_pct
FROM payments
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY day DESC;

-- 5. Outstanding wallet balance across all users
SELECT
  COUNT(*)                   AS total_wallets,
  SUM(balance)               AS total_balance_inr,
  AVG(balance)               AS avg_balance_inr
FROM wallets;

-- 6. Wallet transaction history for a user
SELECT wt.*, w.balance AS current_wallet_balance
FROM wallet_transactions wt
JOIN wallets w ON w.id = wt.wallet_id
WHERE w.user_id = :user_id
ORDER BY wt.created_at DESC
LIMIT 50;

-- 7. Pending settlements for admin
SELECT s.*, u.full_name AS merchant_name, u.email AS merchant_email
FROM settlements s
JOIN users u ON u.id = s.merchant_id
WHERE s.status = 'PENDING'
ORDER BY s.created_at ASC;

-- 8. Commission earned per merchant (last 30 days)
SELECT
  c.merchant_id,
  u.full_name,
  COUNT(c.id)                       AS order_count,
  SUM(c.order_amount)               AS gross_order_value,
  SUM(c.commission_amount)          AS total_commission
FROM commissions c
JOIN users u ON u.id = c.merchant_id
WHERE c.created_at >= NOW() - INTERVAL '30 days'
GROUP BY c.merchant_id, u.full_name
ORDER BY total_commission DESC;
