-- =============================================================
-- V12: Seed / Reference Data
-- Initial data for non-production sensitive tables
-- =============================================================

-- Permissions
INSERT INTO permissions (name, description) VALUES
    ('PLACE_ORDER',         'Customer can place orders'),
    ('VIEW_ORDERS',         'View own orders'),
    ('MANAGE_PRODUCTS',     'Merchant can add/edit products'),
    ('MANAGE_INVENTORY',    'Merchant can update stock'),
    ('ACCEPT_ORDER',        'Merchant can accept/reject orders'),
    ('ACCEPT_DELIVERY',     'Delivery partner can accept deliveries'),
    ('MANAGE_USERS',        'Admin: manage all users'),
    ('MANAGE_MERCHANTS',    'Admin: approve/suspend merchants'),
    ('VIEW_ANALYTICS',      'Admin: view platform analytics'),
    ('MANAGE_COUPONS',      'Admin: create/edit coupons'),
    ('PROCESS_REFUNDS',     'Admin: process refunds'),
    ('VIEW_AUDIT_LOGS',     'Admin: view audit logs');

-- Role-Permission mapping
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CUSTOMER'   AND p.name IN ('PLACE_ORDER', 'VIEW_ORDERS');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MERCHANT'   AND p.name IN ('MANAGE_PRODUCTS', 'MANAGE_INVENTORY', 'ACCEPT_ORDER', 'VIEW_ORDERS');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'DELIVERY_PARTNER' AND p.name IN ('ACCEPT_DELIVERY', 'VIEW_ORDERS');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- Brands (sample)
INSERT INTO brands (name) VALUES
    ('Amul'), ('Britannia'), ('Nestle'), ('Parle'), ('Haldirams'),
    ('MTR'), ('Tata'), ('ITC'), ('HUL'), ('P&G');
