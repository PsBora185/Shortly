CREATE TABLE otp_sessions (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_otp_sessions_email ON otp_sessions (email);
