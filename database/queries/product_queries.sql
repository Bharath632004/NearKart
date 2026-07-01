-- ============================================================
-- Product & Shop Service Queries
-- Used by: backend/product-service, backend/shop-service
-- ============================================================

-- Full-text search products by name
SELECT p.id, p.name, p.mrp, p.selling_price, p.unit, p.avg_rating,
       pi2.image_url AS primary_image,
       i.quantity AS stock
FROM products p
LEFT JOIN product_images pi2 ON p.id = pi2.product_id AND pi2.is_primary = TRUE
LEFT JOIN inventory i ON p.id = i.product_id
WHERE p.shop_id = :shop_id
  AND p.is_active = TRUE
  AND to_tsvector('english', p.name) @@ plainto_tsquery('english', :search_term)
ORDER BY p.is_featured DESC, p.avg_rating DESC
LIMIT :limit OFFSET :offset;

-- Get products by category
SELECT p.id, p.name, p.mrp, p.selling_price, p.unit, p.avg_rating, p.total_ratings,
       pi2.image_url AS primary_image,
       i.quantity AS stock
FROM products p
LEFT JOIN product_images pi2 ON p.id = pi2.product_id AND pi2.is_primary = TRUE
LEFT JOIN inventory i ON p.id = i.product_id
WHERE p.category_id = :category_id
  AND p.shop_id = :shop_id
  AND p.is_active = TRUE
ORDER BY p.is_featured DESC
LIMIT :limit OFFSET :offset;

-- Get nearby shops within radius (using PostGIS)
SELECT s.id, s.name, s.description, s.logo_image, s.cover_image,
       s.avg_rating, s.total_ratings, s.is_open,
       s.min_order_value, s.delivery_radius_km,
       sc.name AS category_name,
       ST_Distance(
           ST_MakePoint(s.longitude, s.latitude)::geography,
           ST_MakePoint(:user_lng, :user_lat)::geography
       ) / 1000 AS distance_km
FROM shops s
JOIN shop_categories sc ON s.category_id = sc.id
WHERE s.is_active = TRUE AND s.is_verified = TRUE
  AND ST_DWithin(
      ST_MakePoint(s.longitude, s.latitude)::geography,
      ST_MakePoint(:user_lng, :user_lat)::geography,
      s.delivery_radius_km * 1000
  )
ORDER BY distance_km ASC
LIMIT :limit OFFSET :offset;

-- Get featured products across nearby shops
SELECT p.id, p.name, p.mrp, p.selling_price, p.avg_rating,
       pi2.image_url AS primary_image,
       s.name AS shop_name
FROM products p
JOIN shops s ON p.shop_id = s.id
LEFT JOIN product_images pi2 ON p.id = pi2.product_id AND pi2.is_primary = TRUE
WHERE p.is_featured = TRUE AND p.is_active = TRUE AND s.is_active = TRUE
ORDER BY p.avg_rating DESC
LIMIT 20;

-- Low stock alert products for merchant
SELECT p.id, p.name, p.sku, i.quantity, i.low_stock_alert
FROM inventory i
JOIN products p ON i.product_id = p.id
WHERE p.shop_id = :shop_id
  AND i.quantity <= i.low_stock_alert
ORDER BY i.quantity ASC;
