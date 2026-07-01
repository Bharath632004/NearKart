-- ============================================================
-- Order Service Queries
-- Used by: backend/order-service
-- ============================================================

-- Place order (use in a transaction with inventory deduction)
-- Step 1: Deduct inventory
UPDATE inventory
SET quantity = quantity - :qty,
    updated_at = NOW()
WHERE product_id = :product_id AND quantity >= :qty
RETURNING quantity;

-- Step 2: Insert order
INSERT INTO orders (customer_id, shop_id, address_id, coupon_id, payment_method,
                    subtotal, discount_amount, delivery_charge, tax_amount, total_amount, special_note)
VALUES (:customer_id, :shop_id, :address_id, :coupon_id, :payment_method,
        :subtotal, :discount_amount, :delivery_charge, :tax_amount, :total_amount, :special_note)
RETURNING id;

-- Step 3: Insert order items
INSERT INTO order_items (order_id, product_id, product_name, product_image, quantity, unit_price, total_price)
VALUES (:order_id, :product_id, :product_name, :product_image, :quantity, :unit_price, :total_price);

-- Get order with items for customer
SELECT o.id, o.status, o.payment_method, o.payment_status,
       o.subtotal, o.discount_amount, o.delivery_charge, o.tax_amount, o.total_amount,
       o.special_note, o.estimated_delivery_at, o.delivered_at, o.created_at,
       s.name AS shop_name, s.logo_image AS shop_logo,
       a.address_line1, a.city, a.pincode
FROM orders o
JOIN shops s ON o.shop_id = s.id
JOIN addresses a ON o.address_id = a.id
WHERE o.id = :order_id AND o.customer_id = :customer_id;

-- Get order items
SELECT oi.product_id, oi.product_name, oi.product_image,
       oi.quantity, oi.unit_price, oi.total_price
FROM order_items oi
WHERE oi.order_id = :order_id;

-- Get customer order history (paginated)
SELECT o.id, o.status, o.total_amount, o.created_at,
       s.name AS shop_name, s.logo_image
FROM orders o
JOIN shops s ON o.shop_id = s.id
WHERE o.customer_id = :customer_id
ORDER BY o.created_at DESC
LIMIT :limit OFFSET :offset;

-- Get shop orders (for merchant dashboard)
SELECT o.id, o.status, o.payment_status, o.total_amount, o.created_at,
       u.full_name AS customer_name, u.phone AS customer_phone
FROM orders o
JOIN users u ON o.customer_id = u.id
WHERE o.shop_id = :shop_id AND o.status = :status
ORDER BY o.created_at DESC
LIMIT :limit OFFSET :offset;

-- Update order status
UPDATE orders
SET status = :status, updated_at = NOW()
WHERE id = :order_id
RETURNING id, status;

-- Cancel order and restore inventory
UPDATE inventory i
SET quantity = quantity + oi.quantity, updated_at = NOW()
FROM order_items oi
WHERE oi.order_id = :order_id AND i.product_id = oi.product_id;
