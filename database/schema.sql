-- ============================================================
-- NearKart PostgreSQL Master Schema
-- Version: 2.1 | Author: Bharath C
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================
-- ROLES & PERMISSIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS permissions (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       INT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name        VARCHAR(150) NOT NULL,
    email            VARCHAR(255) UNIQUE NOT NULL,
    phone            VARCHAR(15) UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    role             VARCHAR(30) NOT NULL CHECK (role IN ('CUSTOMER','MERCHANT','DELIVERY_PARTNER','ADMIN','SUPER_ADMIN')),
    is_enabled       BOOLEAN DEFAULT FALSE,
    is_locked        BOOLEAN DEFAULT FALSE,
    profile_image    TEXT,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_role  ON users(role);

-- ============================================================
-- OTP
-- ============================================================

CREATE TABLE IF NOT EXISTS otps (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    identifier  VARCHAR(255) NOT NULL,
    otp_code    VARCHAR(10) NOT NULL,
    otp_type    VARCHAR(30) NOT NULL CHECK (otp_type IN ('REGISTER','LOGIN','FORGOT_PASSWORD','PHONE_VERIFY')),
    is_used     BOOLEAN DEFAULT FALSE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otps_identifier ON otps(identifier);

-- ============================================================
-- SESSIONS / REFRESH TOKENS
-- ============================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       TEXT UNIQUE NOT NULL,
    is_revoked  BOOLEAN DEFAULT FALSE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ============================================================
-- ADDRESSES
-- ============================================================

CREATE TABLE IF NOT EXISTS addresses (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label         VARCHAR(50) DEFAULT 'Home',
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    pincode       VARCHAR(10) NOT NULL,
    country       VARCHAR(50) DEFAULT 'India',
    latitude      DECIMAL(10,8),
    longitude     DECIMAL(11,8),
    is_default    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);

-- ============================================================
-- SHOP CATEGORIES
-- ============================================================

CREATE TABLE IF NOT EXISTS shop_categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    icon_url    TEXT,
    is_active   BOOLEAN DEFAULT TRUE
);

-- ============================================================
-- SHOPS
-- ============================================================

CREATE TABLE IF NOT EXISTS shops (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id        INT REFERENCES shop_categories(id),
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    phone              VARCHAR(15),
    email              VARCHAR(255),
    address_line1      VARCHAR(255) NOT NULL,
    address_line2      VARCHAR(255),
    city               VARCHAR(100) NOT NULL,
    state              VARCHAR(100) NOT NULL,
    pincode            VARCHAR(10) NOT NULL,
    latitude           DECIMAL(10,8) NOT NULL,
    longitude          DECIMAL(11,8) NOT NULL,
    cover_image        TEXT,
    logo_image         TEXT,
    gstin              VARCHAR(20) UNIQUE,
    fssai_license      VARCHAR(30),
    is_verified        BOOLEAN DEFAULT FALSE,
    is_active          BOOLEAN DEFAULT TRUE,
    is_open            BOOLEAN DEFAULT TRUE,
    avg_rating         DECIMAL(3,2) DEFAULT 0.0,
    total_ratings      INT DEFAULT 0,
    delivery_radius_km DECIMAL(5,2) DEFAULT 5.0,
    min_order_value    DECIMAL(10,2) DEFAULT 0.0,
    created_at         TIMESTAMP DEFAULT NOW(),
    updated_at         TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shops_merchant_id ON shops(merchant_id);
CREATE INDEX IF NOT EXISTS idx_shops_pincode ON shops(pincode);
CREATE INDEX IF NOT EXISTS idx_shops_location ON shops USING GIST (ST_MakePoint(longitude, latitude)::geometry);

-- ============================================================
-- BRANDS & CATEGORIES
-- ============================================================

CREATE TABLE IF NOT EXISTS brands (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150) UNIQUE NOT NULL,
    logo_url    TEXT,
    is_active   BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(150) UNIQUE NOT NULL,
    parent_id   INT REFERENCES categories(id),
    icon_url    TEXT,
    banner_url  TEXT,
    is_active   BOOLEAN DEFAULT TRUE,
    sort_order  INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);

-- ============================================================
-- PRODUCTS
-- ============================================================

CREATE TABLE IF NOT EXISTS products (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id         UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    category_id     INT REFERENCES categories(id),
    brand_id        INT REFERENCES brands(id),
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255),
    description     TEXT,
    mrp             DECIMAL(10,2) NOT NULL,
    selling_price   DECIMAL(10,2) NOT NULL,
    unit            VARCHAR(50),
    unit_value      VARCHAR(50),
    barcode         VARCHAR(50),
    sku             VARCHAR(100),
    is_active       BOOLEAN DEFAULT TRUE,
    is_featured     BOOLEAN DEFAULT FALSE,
    avg_rating      DECIMAL(3,2) DEFAULT 0.0,
    total_ratings   INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_products_selling_price CHECK (selling_price <= mrp)
);

CREATE INDEX IF NOT EXISTS idx_products_shop_id ON products(shop_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_name ON products USING GIN(to_tsvector('english', name));

CREATE TABLE IF NOT EXISTS product_images (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   TEXT NOT NULL,
    is_primary  BOOLEAN DEFAULT FALSE,
    sort_order  INT DEFAULT 0
);

-- ============================================================
-- INVENTORY
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID UNIQUE NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity        INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    low_stock_alert INT DEFAULT 10,
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- COUPONS & OFFERS
-- ============================================================

CREATE TABLE IF NOT EXISTS coupons (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code                VARCHAR(50) UNIQUE NOT NULL,
    description         TEXT,
    discount_type       VARCHAR(20) NOT NULL CHECK (discount_type IN ('FLAT','PERCENT')),
    discount_value      DECIMAL(10,2) NOT NULL,
    max_discount        DECIMAL(10,2),
    min_order_value     DECIMAL(10,2) DEFAULT 0.0,
    usage_limit         INT,
    used_count          INT DEFAULT 0,
    per_user_limit      INT DEFAULT 1,
    is_active           BOOLEAN DEFAULT TRUE,
    valid_from          TIMESTAMP NOT NULL,
    valid_until         TIMESTAMP NOT NULL,
    created_at          TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_coupon_usage_non_negative CHECK (used_count >= 0),
    CONSTRAINT chk_coupon_usage_limit CHECK (usage_limit IS NULL OR used_count <= usage_limit)
);

-- ============================================================
-- WALLET
-- ============================================================

CREATE TABLE IF NOT EXISTS wallets (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance     DECIMAL(12,2) NOT NULL DEFAULT 0.0 CHECK (balance >= 0),
    currency    VARCHAR(5) DEFAULT 'INR',
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id       UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    amount          DECIMAL(12,2) NOT NULL,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('CREDIT','DEBIT')),
    reference_type  VARCHAR(50),
    reference_id    UUID,
    description     TEXT,
    balance_after   DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallet_txn_wallet_id ON wallet_transactions(wallet_id);

-- ============================================================
-- ORDERS
-- ============================================================

CREATE TABLE IF NOT EXISTS orders (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id           UUID NOT NULL REFERENCES users(id),
    shop_id               UUID NOT NULL REFERENCES shops(id),
    address_id            UUID NOT NULL REFERENCES addresses(id),
    coupon_id             UUID REFERENCES coupons(id),
    status                VARCHAR(30) NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING','CONFIRMED','PREPARING','READY','PICKED_UP','DELIVERED','CANCELLED','REFUNDED')),
    payment_method        VARCHAR(30) NOT NULL CHECK (payment_method IN ('ONLINE','COD','WALLET')),
    payment_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                          CHECK (payment_status IN ('PENDING','PAID','FAILED','REFUNDED')),
    subtotal              DECIMAL(12,2) NOT NULL,
    discount_amount       DECIMAL(12,2) DEFAULT 0.0,
    delivery_charge       DECIMAL(10,2) DEFAULT 0.0,
    tax_amount            DECIMAL(10,2) DEFAULT 0.0,
    total_amount          DECIMAL(12,2) NOT NULL,
    special_note          TEXT,
    estimated_delivery_at TIMESTAMP,
    delivered_at          TIMESTAMP,
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_shop_id ON orders(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id      UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    UUID NOT NULL REFERENCES products(id),
    product_name  VARCHAR(255) NOT NULL,
    product_image TEXT,
    quantity      INT NOT NULL CHECK (quantity > 0),
    unit_price    DECIMAL(10,2) NOT NULL,
    total_price   DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_order_items_total_price CHECK (total_price = quantity * unit_price)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- ============================================================
-- PAYMENTS & TRANSACTIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS payments (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id            UUID NOT NULL REFERENCES orders(id),
    gateway             VARCHAR(30) DEFAULT 'RAZORPAY',
    gateway_order_id    VARCHAR(255),
    gateway_payment_id  VARCHAR(255) UNIQUE,
    gateway_signature   TEXT,
    amount              DECIMAL(12,2) NOT NULL,
    currency            VARCHAR(5) DEFAULT 'INR',
    status              VARCHAR(20) NOT NULL DEFAULT 'INITIATED'
                        CHECK (status IN ('INITIATED','SUCCESS','FAILED','REFUNDED')),
    failure_reason      TEXT,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);

-- ============================================================
-- DELIVERY PARTNERS
-- ============================================================

CREATE TABLE IF NOT EXISTS delivery_partners (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_type     VARCHAR(30) CHECK (vehicle_type IN ('BIKE','SCOOTER','BICYCLE','AUTO')),
    vehicle_number   VARCHAR(20),
    license_number   VARCHAR(30),
    aadhaar_number   VARCHAR(20),
    is_kyc_verified  BOOLEAN DEFAULT FALSE,
    is_available     BOOLEAN DEFAULT FALSE,
    current_lat      DECIMAL(10,8),
    current_lng      DECIMAL(11,8),
    total_deliveries INT DEFAULT 0,
    avg_rating       DECIMAL(3,2) DEFAULT 0.0,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS delivery_assignments (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id       UUID UNIQUE NOT NULL REFERENCES orders(id),
    partner_id     UUID NOT NULL REFERENCES delivery_partners(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED'
                   CHECK (status IN ('ASSIGNED','ACCEPTED','REJECTED','PICKED_UP','DELIVERED')),
    pickup_otp     VARCHAR(6),
    delivery_otp   VARCHAR(6),
    assigned_at    TIMESTAMP DEFAULT NOW(),
    picked_up_at   TIMESTAMP,
    delivered_at   TIMESTAMP,
    distance_km    DECIMAL(8,2),
    earnings       DECIMAL(10,2)
);

CREATE INDEX IF NOT EXISTS idx_delivery_assignments_partner_id ON delivery_assignments(partner_id);

-- ============================================================
-- REVIEWS & RATINGS
-- ============================================================

CREATE TABLE IF NOT EXISTS reviews (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reviewer_id  UUID NOT NULL REFERENCES users(id),
    entity_type  VARCHAR(20) NOT NULL CHECK (entity_type IN ('PRODUCT','SHOP','DELIVERY_PARTNER')),
    entity_id    UUID NOT NULL,
    rating       SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_text  TEXT,
    images       TEXT[],
    is_verified  BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_reviews_unique ON reviews(reviewer_id, entity_type, entity_id);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    body         TEXT NOT NULL,
    type         VARCHAR(30) NOT NULL,
    reference_id UUID,
    is_read      BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- ============================================================
-- COMPLAINTS & RETURNS
-- ============================================================

CREATE TABLE IF NOT EXISTS complaints (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id),
    order_id    UUID REFERENCES orders(id),
    subject     VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status      VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
    created_at  TIMESTAMP DEFAULT NOW(),
    resolved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS returns (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id    UUID NOT NULL REFERENCES orders(id),
    reason      TEXT NOT NULL,
    status      VARCHAR(20) DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED','APPROVED','REJECTED','COMPLETED')),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refunds (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id     UUID NOT NULL REFERENCES orders(id),
    return_id    UUID REFERENCES returns(id),
    amount       DECIMAL(12,2) NOT NULL,
    method       VARCHAR(20) CHECK (method IN ('ORIGINAL','WALLET')),
    status       VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','PROCESSED','FAILED')),
    processed_at TIMESTAMP,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- INVOICES & SETTLEMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS invoices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID UNIQUE NOT NULL REFERENCES orders(id),
    invoice_number  VARCHAR(50) UNIQUE NOT NULL,
    pdf_url         TEXT,
    issued_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS settlements (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id UUID NOT NULL REFERENCES users(id),
    period_from DATE NOT NULL,
    period_to   DATE NOT NULL,
    total_sales DECIMAL(12,2) NOT NULL,
    commission  DECIMAL(12,2) NOT NULL,
    net_amount  DECIMAL(12,2) NOT NULL,
    status      VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','PROCESSING','COMPLETED')),
    settled_at  TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS commissions (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id          UUID NOT NULL REFERENCES orders(id),
    merchant_id       UUID NOT NULL REFERENCES users(id),
    order_amount      DECIMAL(12,2) NOT NULL,
    rate_percent      DECIMAL(5,2) NOT NULL,
    commission_amount DECIMAL(12,2) NOT NULL,
    created_at        TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- AUDIT & ACTIVITY LOGS
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   UUID,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

CREATE TABLE IF NOT EXISTS activity_logs (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID REFERENCES users(id),
    activity   VARCHAR(150) NOT NULL,
    metadata   JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- WISHLIST & CART
-- ============================================================

CREATE TABLE IF NOT EXISTS wishlists (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS carts (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shop_id    UUID REFERENCES shops(id),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cart_items (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id    UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity   INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);

-- ============================================================
-- SEED DATA
-- ============================================================

INSERT INTO roles (name, description) VALUES
    ('CUSTOMER', 'End customer who buys products'),
    ('MERCHANT', 'Shop owner who sells products'),
    ('DELIVERY_PARTNER', 'Delivery personnel'),
    ('ADMIN', 'Platform administrator'),
    ('SUPER_ADMIN', 'Super administrator with full access')
ON CONFLICT (name) DO NOTHING;

INSERT INTO shop_categories (name, icon_url) VALUES
    ('Grocery', '/icons/grocery.svg'),
    ('Pharmacy', '/icons/pharmacy.svg'),
    ('Electronics', '/icons/electronics.svg'),
    ('Bakery', '/icons/bakery.svg'),
    ('Restaurant', '/icons/restaurant.svg'),
    ('Stationery', '/icons/stationery.svg'),
    ('Clothing', '/icons/clothing.svg'),
    ('Pet Supplies', '/icons/pet.svg')
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, slug, sort_order) VALUES
    ('Fruits & Vegetables', 'fruits-vegetables', 1),
    ('Dairy & Eggs', 'dairy-eggs', 2),
    ('Snacks & Beverages', 'snacks-beverages', 3),
    ('Household', 'household', 4),
    ('Personal Care', 'personal-care', 5),
    ('Medicines', 'medicines', 6),
    ('Electronics', 'electronics', 7),
    ('Bakery', 'bakery', 8)
ON CONFLICT (slug) DO NOTHING;
