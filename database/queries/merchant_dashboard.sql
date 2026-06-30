-- =============================================================
-- Merchant Dashboard Summary Query
-- =============================================================

-- :shop_id = merchant's shop UUID
-- :from_date, :to_date = date range

SELECT
    COUNT(DISTINCT o.id)                                    AS total_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'DELIVERED'
          THEN o.id END)                                    AS completed_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'CANCELLED'
          THEN o.id END)                                    AS cancelled_orders,
    COALESCE(SUM(CASE WHEN o.status = 'DELIVERED'
          THEN o.total_amount END), 0)                      AS gross_revenue,
    COALESCE(SUM(CASE WHEN o.status = 'DELIVERED'
          THEN c.commission_amount END), 0)                 AS total_commission,
    COALESCE(SUM(CASE WHEN o.status = 'DELIVERED'
          THEN o.total_amount END), 0)
        - COALESCE(SUM(CASE WHEN o.status = 'DELIVERED'
          THEN c.commission_amount END), 0)                 AS net_revenue,
    ROUND(AVG(CASE WHEN o.status = 'DELIVERED'
          THEN o.total_amount END), 2)                      AS avg_order_value,
    COUNT(DISTINCT o.customer_id)                           AS unique_customers
FROM orders o
LEFT JOIN commission c ON c.order_id = o.id
WHERE
    o.shop_id = :shop_id
    AND o.created_at BETWEEN :from_date AND :to_date;
