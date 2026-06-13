CREATE TABLE urls (
    id UUID PRIMARY KEY,
    original_url TEXT NOT NULL,
    short_code VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NULL
);

CREATE TABLE analytics (
    id UUID PRIMARY KEY,
    short_code VARCHAR(32) NOT NULL UNIQUE,
    clicks BIGINT NOT NULL,
    last_accessed TIMESTAMP NULL
);

ALTER TABLE analytics
    ADD CONSTRAINT fk_analytics_short_code
    FOREIGN KEY (short_code)
    REFERENCES urls (short_code)
    ON DELETE CASCADE;

CREATE INDEX idx_urls_created_at ON urls (created_at);
CREATE INDEX idx_urls_expires_at ON urls (expires_at);
