-- =============================================================
-- Nearby Shops Query (Core Feature)
-- Uses PostGIS ST_DWithin for GPS-based shop proximity search
-- =============================================================

-- Find all open, verified shops within X km of customer GPS location
-- :latitude  = customer latitude  (e.g. 16.5062)
-- :longitude = customer longitude (e.g. 80.6480)
-- :radius_km = search radius in km (e.g. 5.0)

SELECT
    s.id,
    s.name,
    s.logo_url,
    s.rating,
    s.total_reviews,
    s.avg_delivery_mins,
    s.min_order_amount,
    sc.name AS category,
    ROUND(
        ST_Distance(
            s.location::GEOGRAPHY,
            ST_MakePoint(:longitude, :latitude)::GEOGRAPHY
        ) / 1000.0, 2
    ) AS distance_km
FROM shops s
JOIN shop_categories sc ON s.shop_category_id = sc.id
WHERE
    s.is_open = TRUE
    AND s.status = 'VERIFIED'
    AND s.is_deleted = FALSE
    AND ST_DWithin(
        s.location::GEOGRAPHY,
        ST_MakePoint(:longitude, :latitude)::GEOGRAPHY,
        :radius_km * 1000   -- Convert km to metres
    )
ORDER BY distance_km ASC
LIMIT 50;
