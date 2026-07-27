CREATE TABLE IF NOT EXISTS delivery_log(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL,
    attempt_number INT NOT NULL DEFAULT 0,
    result VARCHAR(255) NOT NULL,
    created_at DATE NOT NULL
);