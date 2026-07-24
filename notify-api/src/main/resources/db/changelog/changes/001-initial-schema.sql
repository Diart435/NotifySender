CREATE TABLE IF NOT EXISTS notification(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    channel VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(255) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL
);