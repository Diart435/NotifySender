CREATE TABLE IF NOT EXISTS users(
    id UUID PRIMARY KEY DEFAULT get_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATE NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    api_key_lookup VARCHAR(255) NOT NULL
);