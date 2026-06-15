CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(120),
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(120),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP NULL
);

CREATE TABLE urls (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    original_url TEXT NOT NULL,
    short_code VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NULL,
    clicks BIGINT NOT NULL,
    last_accessed TIMESTAMP NULL,
    CONSTRAINT fk_urls_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_urls_created_at ON urls (created_at);
CREATE INDEX idx_urls_expires_at ON urls (expires_at);
CREATE INDEX idx_urls_user_id ON urls (user_id);
