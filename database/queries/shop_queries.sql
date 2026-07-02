-- ============================================================
-- NearKart: Shop Queries
-- Author: Bharath C
-- ============================================================

-- 1. Nearby open shops within radius (PostGIS)
SELECT
  s.id, s.name, s.avg_rating, s.total_ratings,
  s.delivery_radius_km, s.min_order_value,
  sc.name AS category,
  ROUND(ST_Distance(
    ST_MakePoint(s.longitude, s.latitude)::geography,
    ST_MakePoint(:user_lng, :user_lat)::geography
  ) / 1000.0, 2) AS distance_km
FROM shops s
JOIN shop_categories sc ON sc.id = s.category_id
WHERE ST_DWithin(
  ST_MakePoint(s.longitude, s.latitude)::geography,
  ST_MakePoint(:user_lng, :user_lat)::geography,
  :radius_meters
)
  AND s.is_active = TRUE
  AND s.is_verified = TRUE
  AND s.is_open = TRUE
  AND s.deleted_at IS NULL
ORDER BY distance_km ASC
LIMIT 20;

-- 2. Shop dashboard summary
SELECT
  s.name,
  s.avg_rating,
  COUNT(DISTINCT o.id) FILTER (WHERE o.status NOT IN ('CANCELLED','REFUNDED') AND o.created_at >= NOW() - INTERVAL '30 days') AS orders_30d,
  COALESCE(SUM(o.total_amount) FILTER (WHERE o.payment_status = 'PAID' AND o.created_at >= NOW() - INTERVAL '30 days'), 0) AS revenue_30d,
  COUNT(DISTINCT p.id) FILTER (WHERE p.is_active = TRUE AND p.deleted_at IS NULL) AS active_products
FROM shops s
LEFT JOIN orders o ON o.shop_id = s.id
LEFT JOIN products p ON p.shop_id = s.id
WHERE s.id = :shop_id
GROUP BY s.name, s.avg_rating;

-- 3. Is shop open right now?
SELECT
  s.is_open,
  soh.open_time,
  soh.close_time,
  soh.is_closed AS day_holiday,
  CASE
    WHEN soh.is_closed = TRUE THEN FALSE
    WHEN LOCALTIME BETWEEN soh.open_time AND soh.close_time THEN TRUE
    ELSE FALSE
  END AS open_now
FROM shops s
LEFT JOIN shop_operating_hours soh
  ON soh.shop_id = s.id
  AND soh.day_of_week = EXTRACT(DOW FROM CURRENT_TIMESTAMP)::SMALLINT
WHERE s.id = :shop_id;

-- 4. Top-rated shops by category
SELECT s.id, s.name, s.avg_rating, s.total_ratings, sc.name AS category
FROM shops s
JOIN shop_categories sc ON sc.id = s.category_id
WHERE s.is_active = TRUE AND s.is_verified = TRUE AND s.deleted_at IS NULL
  AND (:category_id IS NULL OR s.category_id = :category_id)
ORDER BY s.avg_rating DESC, s.total_ratings DESC
LIMIT 10;

-- 5. Shop operating hours for a week
SELECT
  day_of_week,
  CASE day_of_week WHEN 0 THEN 'Sunday' WHEN 1 THEN 'Monday'
    WHEN 2 THEN 'Tuesday' WHEN 3 THEN 'Wednesday' WHEN 4 THEN 'Thursday'
    WHEN 5 THEN 'Friday' WHEN 6 THEN 'Saturday' END AS day_name,
  open_time, close_time, is_closed
FROM shop_operating_hours
WHERE shop_id = :shop_id
ORDER BY day_of_week;
