-- ============================================================
-- Analytics Service Queries
-- Used by: backend/analytics-service, admin-panel
-- ============================================================

-- Daily order revenue (last 30 days)
SELECT DATE(created_at) AS order_date,
       COUNT(*) AS total_orders,
       SUM(total_amount) AS total_revenue,
       AVG(total_amount) AS avg_order_value
FROM orders
WHERE status NOT IN ('CANCELLED', 'REFUNDED')
  AND created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY order_date DESC;

-- Top selling products
SELECT oi.product_id, oi.product_name,
       SUM(oi.quantity) AS total_units_sold,
       SUM(oi.total_price) AS total_revenue
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'DELIVERED'
  AND o.created_at >= NOW() - INTERVAL '30 days'
GROUP BY oi.product_id, oi.product_name
ORDER BY total_units_sold DESC
LIMIT 10;

-- Top shops by revenue
SELECT s.id, s.name, s.city,
       COUNT(o.id) AS total_orders,
       SUM(o.total_amount) AS total_revenue
FROM shops s
JOIN orders o ON s.id = o.shop_id
WHERE o.status = 'DELIVERED'
  AND o.created_at >= NOW() - INTERVAL '30 days'
GROUP BY s.id, s.name, s.city
ORDER BY total_revenue DESC
LIMIT 10;

-- Merchant settlement summary
SELECT u.id AS merchant_id, u.full_name,
       COUNT(o.id) AS total_orders,
       SUM(o.total_amount) AS gross_sales,
       SUM(c.commission_amount) AS total_commission,
       SUM(o.total_amount) - SUM(c.commission_amount) AS net_payout
FROM users u
JOIN shops s ON u.id = s.merchant_id
JOIN orders o ON s.id = o.shop_id
JOIN commissions c ON o.id = c.order_id
WHERE u.role = 'MERCHANT'
  AND o.status = 'DELIVERED'
  AND o.created_at BETWEEN :period_from AND :period_to
GROUP BY u.id, u.full_name
ORDER BY net_payout DESC;

-- Delivery partner performance
SELECT dp.id, u.full_name,
       COUNT(da.id) AS total_deliveries,
       AVG(dp.avg_rating) AS avg_rating,
       SUM(da.earnings) AS total_earnings
FROM delivery_partners dp
JOIN users u ON dp.user_id = u.id
JOIN delivery_assignments da ON dp.id = da.partner_id
WHERE da.status = 'DELIVERED'
  AND da.delivered_at >= NOW() - INTERVAL '30 days'
GROUP BY dp.id, u.full_name
ORDER BY total_deliveries DESC
LIMIT 10;

-- User growth (new registrations per day)
SELECT DATE(created_at) AS reg_date,
       role,
       COUNT(*) AS new_users
FROM users
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at), role
ORDER BY reg_date DESC;
