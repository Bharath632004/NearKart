-- ============================================================
-- Delivery Service Queries
-- Used by: backend/delivery-service
-- ============================================================

-- Find nearest available delivery partner
SELECT dp.id, u.full_name, u.phone,
       dp.vehicle_type, dp.avg_rating,
       ST_Distance(
           ST_MakePoint(dp.current_lng, dp.current_lat)::geography,
           ST_MakePoint(:shop_lng, :shop_lat)::geography
       ) / 1000 AS distance_from_shop_km
FROM delivery_partners dp
JOIN users u ON dp.user_id = u.id
WHERE dp.is_available = TRUE
  AND dp.is_kyc_verified = TRUE
  AND ST_DWithin(
      ST_MakePoint(dp.current_lng, dp.current_lat)::geography,
      ST_MakePoint(:shop_lng, :shop_lat)::geography,
      10000  -- 10 km radius
  )
ORDER BY distance_from_shop_km ASC
LIMIT 5;

-- Assign order to delivery partner
INSERT INTO delivery_assignments (order_id, partner_id, pickup_otp, delivery_otp, distance_km, earnings)
VALUES (:order_id, :partner_id, :pickup_otp, :delivery_otp, :distance_km, :earnings)
RETURNING id;

-- Update delivery partner location
UPDATE delivery_partners
SET current_lat = :lat, current_lng = :lng, updated_at = NOW()
WHERE user_id = :user_id;

-- Toggle availability
UPDATE delivery_partners
SET is_available = :is_available, updated_at = NOW()
WHERE user_id = :user_id;

-- Get active assignment for partner
SELECT da.id, da.order_id, da.status, da.pickup_otp, da.delivery_otp,
       o.total_amount, s.name AS shop_name, s.address_line1 AS shop_address,
       a.address_line1 AS delivery_address, a.city, a.latitude, a.longitude
FROM delivery_assignments da
JOIN orders o ON da.order_id = o.id
JOIN shops s ON o.shop_id = s.id
JOIN addresses a ON o.address_id = a.id
WHERE da.partner_id = :partner_id
  AND da.status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP')
LIMIT 1;

-- Update assignment status
UPDATE delivery_assignments
SET status = :status,
    picked_up_at = CASE WHEN :status = 'PICKED_UP' THEN NOW() ELSE picked_up_at END,
    delivered_at = CASE WHEN :status = 'DELIVERED' THEN NOW() ELSE delivered_at END
WHERE id = :assignment_id AND partner_id = :partner_id
RETURNING id, status;
