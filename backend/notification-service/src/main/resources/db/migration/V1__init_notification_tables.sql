CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100),
    channel VARCHAR(10) NOT NULL,
    type VARCHAR(30) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    message TEXT,
    status VARCHAR(10) NOT NULL,
    error_message TEXT,
    external_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notif_user_id ON notification_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_notif_status ON notification_logs(status);
CREATE INDEX IF NOT EXISTS idx_notif_created_at ON notification_logs(created_at);

CREATE TABLE IF NOT EXISTS device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    fcm_token VARCHAR(500) NOT NULL UNIQUE,
    device_type VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_user_id ON device_tokens(user_id);
