-- ============================================================
-- Notification Service Queries
-- Used by: backend/notification-service
-- ============================================================

-- Insert notification
INSERT INTO notifications (user_id, title, body, type, reference_id)
VALUES (:user_id, :title, :body, :type, :reference_id)
RETURNING id;

-- Get unread notifications for user (paginated)
SELECT id, title, body, type, reference_id, is_read, created_at
FROM notifications
WHERE user_id = :user_id
ORDER BY created_at DESC
LIMIT :limit OFFSET :offset;

-- Count unread notifications
SELECT COUNT(*) AS unread_count
FROM notifications
WHERE user_id = :user_id AND is_read = FALSE;

-- Mark notification as read
UPDATE notifications SET is_read = TRUE WHERE id = :id AND user_id = :user_id;

-- Mark all notifications as read
UPDATE notifications SET is_read = TRUE WHERE user_id = :user_id AND is_read = FALSE;

-- Delete old notifications (run as a cron job - older than 90 days)
DELETE FROM notifications
WHERE created_at < NOW() - INTERVAL '90 days';
