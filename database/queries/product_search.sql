-- =============================================================
-- Product Full-Text Search
-- Uses pg_trgm GIN index for fast fuzzy search
-- =============================================================

-- Search products by name within a specific shop or all nearby shops
-- :search_term = user's search input (e.g. 'amul butter')
-- :shop_id     = optional: restrict to one shop

SELECT
    p.id,
    p.name,
    p.selling_price,
    p.mrp,
    p.unit,
    p.unit_quantity,
    p.rating,
    pi.image_url,
    b.name AS brand,
    c.name AS category,
    i.quantity AS stock,
    s.name AS shop_name,
    SIMILARITY(p.name, :search_term) AS relevance
FROM products p
LEFT JOIN product_images pi ON pi.product_id = p.id AND pi.is_primary = TRUE
LEFT JOIN brands b          ON b.id = p.brand_id
LEFT JOIN categories c      ON c.id = p.category_id
LEFT JOIN inventory i       ON i.product_id = p.id AND i.shop_id = p.shop_id
LEFT JOIN shops s           ON s.id = p.shop_id
WHERE
    p.is_available = TRUE
    AND p.is_deleted = FALSE
    AND s.is_open = TRUE
    AND i.quantity > 0
    AND p.name ILIKE '%' || :search_term || '%'
ORDER BY relevance DESC, p.rating DESC
LIMIT 40;
