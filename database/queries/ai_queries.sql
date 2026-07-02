-- ============================================================
-- NearKart: AI / ML Queries
-- Author: Bharath C
-- ============================================================

-- 1. Top recommended products for a user (from ai_product_recommendations)
SELECT
  apr.product_id,
  p.name,
  p.selling_price,
  p.avg_rating,
  apr.score
FROM ai_product_recommendations apr
JOIN products p ON p.id = apr.product_id
WHERE apr.user_id = :user_id
  AND p.is_active = TRUE
  AND p.deleted_at IS NULL
ORDER BY apr.score DESC
LIMIT 10;

-- 2. Search query popularity (for search autocomplete ML training)
SELECT
  query_text,
  COUNT(*)        AS search_count,
  AVG(result_count) AS avg_results
FROM ai_search_logs
WHERE created_at >= NOW() - INTERVAL '7 days'
GROUP BY query_text
ORDER BY search_count DESC
LIMIT 50;

-- 3. ETA prediction accuracy report
SELECT
  model_version,
  COUNT(*)                                        AS predictions,
  AVG(ABS(predicted_minutes - actual_minutes))   AS mae_minutes,
  PERCENTILE_CONT(0.95) WITHIN GROUP
    (ORDER BY ABS(predicted_minutes - actual_minutes)) AS p95_error
FROM ai_delivery_eta_logs
WHERE actual_minutes IS NOT NULL
  AND created_at >= NOW() - INTERVAL '30 days'
GROUP BY model_version;

-- 4. Fraud flag summary (admin view)
SELECT
  aff.order_id,
  aff.flag_reason,
  aff.confidence_score,
  aff.is_reviewed,
  u.email AS customer_email,
  o.total_amount
FROM ai_fraud_flags aff
JOIN orders o ON o.id = aff.order_id
JOIN users u ON u.id = o.customer_id
WHERE aff.is_reviewed = FALSE
ORDER BY aff.confidence_score DESC
LIMIT 100;

-- 5. Demand forecast for top products
SELECT
  adf.product_id,
  p.name,
  adf.forecast_date,
  adf.predicted_quantity,
  i.quantity AS current_stock
FROM ai_demand_forecasts adf
JOIN products p ON p.id = adf.product_id
JOIN inventory i ON i.product_id = adf.product_id
WHERE adf.forecast_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
ORDER BY adf.forecast_date, adf.predicted_quantity DESC;
