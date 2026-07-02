-- ============================================================
-- NearKart: Inventory Queries
-- Author: Bharath C
-- ============================================================

-- 1. Current stock for a product
SELECT i.quantity, i.low_stock_alert, i.version, i.updated_at
FROM inventory i
WHERE i.product_id = :product_id;

-- 2. Low-stock alerts for a shop
SELECT
  p.id AS product_id,
  p.name,
  p.sku,
  i.quantity,
  i.low_stock_alert
FROM inventory i
JOIN products p ON p.id = i.product_id
WHERE p.shop_id = :shop_id
  AND i.quantity <= i.low_stock_alert
  AND p.is_active = TRUE
  AND p.deleted_at IS NULL
ORDER BY i.quantity ASC;

-- 3. Out-of-stock products for a shop
SELECT p.id, p.name, p.sku, p.category_id
FROM inventory i
JOIN products p ON p.id = i.product_id
WHERE p.shop_id = :shop_id
  AND i.quantity = 0
  AND p.is_active = TRUE
  AND p.deleted_at IS NULL;

-- 4. Stock movement history for a product (last 90 days)
SELECT
  it.id,
  it.change_qty,
  it.reason,
  it.reference_id,
  it.balance_after,
  it.created_at,
  u.full_name AS created_by_name
FROM inventory_transactions it
LEFT JOIN users u ON u.id = it.created_by
WHERE it.product_id = :product_id
  AND it.created_at >= NOW() - INTERVAL '90 days'
ORDER BY it.created_at DESC;

-- 5. Bulk stock status for all products in a shop
SELECT
  p.id, p.name, p.sku,
  i.quantity,
  i.low_stock_alert,
  CASE
    WHEN i.quantity = 0 THEN 'OUT_OF_STOCK'
    WHEN i.quantity <= i.low_stock_alert THEN 'LOW_STOCK'
    ELSE 'IN_STOCK'
  END AS stock_status
FROM products p
LEFT JOIN inventory i ON i.product_id = p.id
WHERE p.shop_id = :shop_id
  AND p.is_active = TRUE
  AND p.deleted_at IS NULL
ORDER BY stock_status ASC, p.name ASC;
