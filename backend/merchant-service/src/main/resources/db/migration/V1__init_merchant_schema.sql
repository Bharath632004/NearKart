-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Merchant table
CREATE TABLE merchants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    gstin VARCHAR(15),
    pan_number VARCHAR(10),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_KYC',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- KYC Documents table
CREATE TABLE kyc_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,
    document_url VARCHAR(500) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    rejection_reason VARCHAR(500),
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    verified_at TIMESTAMP
);

-- Shops table
CREATE TABLE shops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    shop_name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    address_line VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    location GEOMETRY(POINT, 4326),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    open_days VARCHAR(100) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI,SAT',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    logo_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    rating DECIMAL(3,2) DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create spatial index
CREATE INDEX idx_shops_location ON shops USING GIST(location);
CREATE INDEX idx_shops_merchant_id ON shops(merchant_id);
CREATE INDEX idx_shops_category ON shops(category);

-- Promotions table
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    promo_type VARCHAR(50) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_value DECIMAL(10,2) DEFAULT 0,
    max_discount_cap DECIMAL(10,2),
    promo_code VARCHAR(50) UNIQUE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    usage_limit INT,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_promotions_shop_id ON promotions(shop_id);
CREATE INDEX idx_promotions_promo_code ON promotions(promo_code);

-- Settlement table
CREATE TABLE settlements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    total_orders INT NOT NULL DEFAULT 0,
    gross_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    platform_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_deducted DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    utr_number VARCHAR(100),
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_settlements_merchant_id ON settlements(merchant_id);
CREATE INDEX idx_settlements_status ON settlements(status);
