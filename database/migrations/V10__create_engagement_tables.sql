-- =============================================================
-- V10: Customer Engagement Tables
-- Tables: reviews, ratings, notifications, complaints,
--         returns, refunds
-- =============================================================

-- -------------------------------------------------------------
-- 23. REVIEWS
-- -------------------------------------------------------------
CREATE TYPE review_target AS ENUM ('PRODUCT', 'SHOP', 'DELIVERY_PARTNER');

CREATE TABLE reviews (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users(id),
    target_type review_target NOT NULL,
    target_id   UUID         NOT NULL,  -- product_id, shop_id, or delivery_partner user_id
    order_id    UUID         REFERENCES orders(id),
    title       VARCHAR(200),
    body        TEXT,
    is_approved BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, target_type, target_id, order_id)
);

COMMENT ON TABLE reviews IS 'Customer reviews for products, shops, and delivery partners';

CREATE INDEX idx_reviews_target     ON reviews(target_type, target_id);
CREATE INDEX idx_reviews_user_id    ON reviews(user_id);
CREATE INDEX idx_reviews_order_id   ON reviews(order_id);

-- -------------------------------------------------------------
-- 24. RATINGS
-- -------------------------------------------------------------
CREATE TABLE ratings (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users(id),
    review_id   BIGINT       REFERENCES reviews(id) ON DELETE SET NULL,
    target_type review_target NOT NULL,
    target_id   UUID         NOT NULL,
    stars       SMALLINT     NOT NULL,
    order_id    UUID         REFERENCES orders(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, target_type, target_id, order_id),
    CONSTRAINT chk_stars CHECK (stars BETWEEN 1 AND 5)
);

COMMENT ON TABLE ratings IS '1-5 star ratings linked to reviews';

CREATE INDEX idx_ratings_target   ON ratings(target_type, target_id);
CREATE INDEX idx_ratings_user_id  ON ratings(user_id);

-- Trigger to update product/shop average rating
CREATE OR REPLACE FUNCTION update_avg_rating()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.target_type = 'PRODUCT' THEN
        UPDATE products
        SET rating = (
            SELECT ROUND(AVG(stars)::NUMERIC, 2)
            FROM ratings
            WHERE target_type = 'PRODUCT' AND target_id = NEW.target_id
        ),
        total_reviews = (
            SELECT COUNT(*) FROM ratings
            WHERE target_type = 'PRODUCT' AND target_id = NEW.target_id
        )
        WHERE id = NEW.target_id;
    ELSIF NEW.target_type = 'SHOP' THEN
        UPDATE shops
        SET rating = (
            SELECT ROUND(AVG(stars)::NUMERIC, 2)
            FROM ratings
            WHERE target_type = 'SHOP' AND target_id = NEW.target_id
        ),
        total_reviews = (
            SELECT COUNT(*) FROM ratings
            WHERE target_type = 'SHOP' AND target_id = NEW.target_id
        )
        WHERE id = NEW.target_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_avg_rating
    AFTER INSERT OR UPDATE ON ratings
    FOR EACH ROW EXECUTE FUNCTION update_avg_rating();

-- -------------------------------------------------------------
-- 27. NOTIFICATIONS
-- -------------------------------------------------------------
CREATE TYPE notification_type AS ENUM (
    'ORDER_PLACED', 'ORDER_CONFIRMED', 'ORDER_READY',
    'ORDER_PICKED_UP', 'ORDER_DELIVERED', 'ORDER_CANCELLED',
    'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'REFUND_PROCESSED',
    'PROMO', 'SYSTEM'
);

CREATE TABLE notifications (
    id          BIGSERIAL           PRIMARY KEY,
    user_id     UUID                NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        notification_type   NOT NULL,
    title       VARCHAR(200)        NOT NULL,
    body        TEXT                NOT NULL,
    data        JSONB,
    is_read     BOOLEAN             NOT NULL DEFAULT FALSE,
    sent_at     TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    read_at     TIMESTAMPTZ
);

COMMENT ON TABLE notifications IS 'In-app and push notification records per user';

CREATE INDEX idx_notifications_user_id  ON notifications(user_id);
CREATE INDEX idx_notifications_unread   ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_type     ON notifications(type);

-- -------------------------------------------------------------
-- 28. COMPLAINTS
-- -------------------------------------------------------------
CREATE TYPE complaint_status AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');
CREATE TYPE complaint_category AS ENUM (
    'WRONG_ITEM', 'MISSING_ITEM', 'DAMAGED_ITEM',
    'LATE_DELIVERY', 'PAYMENT_ISSUE', 'OTHER'
);

CREATE TABLE complaints (
    id          BIGSERIAL          PRIMARY KEY,
    order_id    UUID               NOT NULL REFERENCES orders(id),
    user_id     UUID               NOT NULL REFERENCES users(id),
    category    complaint_category NOT NULL,
    description TEXT               NOT NULL,
    image_urls  TEXT[],
    status      complaint_status   NOT NULL DEFAULT 'OPEN',
    resolution  TEXT,
    resolved_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE complaints IS 'Customer complaints and grievance tracking';

CREATE INDEX idx_complaints_order_id  ON complaints(order_id);
CREATE INDEX idx_complaints_user_id   ON complaints(user_id);
CREATE INDEX idx_complaints_status    ON complaints(status);

CREATE TRIGGER trg_complaints_updated_at
    BEFORE UPDATE ON complaints
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -------------------------------------------------------------
-- 29. RETURNS
-- -------------------------------------------------------------
CREATE TYPE return_status AS ENUM ('REQUESTED', 'APPROVED', 'REJECTED', 'PICKED_UP', 'COMPLETED');
CREATE TYPE return_reason AS ENUM (
    'WRONG_ITEM', 'DAMAGED', 'EXPIRED', 'QUALITY_ISSUE', 'CHANGED_MIND'
);

CREATE TABLE returns (
    id              BIGSERIAL     PRIMARY KEY,
    order_id        UUID          NOT NULL REFERENCES orders(id),
    order_item_id   BIGINT        NOT NULL REFERENCES order_items(id),
    customer_id     UUID          NOT NULL REFERENCES users(id),
    reason          return_reason NOT NULL,
    description     TEXT,
    image_urls      TEXT[],
    quantity        INT           NOT NULL DEFAULT 1,
    status          return_status NOT NULL DEFAULT 'REQUESTED',
    approved_at     TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE returns IS 'Product return requests linked to order items';

CREATE INDEX idx_returns_order_id   ON returns(order_id);
CREATE INDEX idx_returns_customer   ON returns(customer_id);
CREATE INDEX idx_returns_status     ON returns(status);

-- -------------------------------------------------------------
-- 30. REFUNDS
-- -------------------------------------------------------------
CREATE TYPE refund_status AS ENUM ('INITIATED', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE refund_method AS ENUM ('ORIGINAL_PAYMENT', 'WALLET', 'BANK_TRANSFER');

CREATE TABLE refunds (
    id              BIGSERIAL     PRIMARY KEY,
    order_id        UUID          NOT NULL REFERENCES orders(id),
    payment_id      UUID          NOT NULL REFERENCES payments(id),
    return_id       BIGINT        REFERENCES returns(id),
    amount          NUMERIC(10,2) NOT NULL,
    method          refund_method NOT NULL DEFAULT 'ORIGINAL_PAYMENT',
    status          refund_status NOT NULL DEFAULT 'INITIATED',
    razorpay_refund_id VARCHAR(100) UNIQUE,
    failure_reason  TEXT,
    initiated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);

COMMENT ON TABLE refunds IS 'Refund records linked to payments and optional returns';

CREATE INDEX idx_refunds_order_id   ON refunds(order_id);
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status     ON refunds(status);
