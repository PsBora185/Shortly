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

ALTER TABLE urls
    ADD COLUMN owner_email VARCHAR(320) NULL;

CREATE INDEX idx_urls_owner_email ON urls (owner_email);
