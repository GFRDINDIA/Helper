-- ============================================
-- V1__Create_Auth_Tables.sql
-- Flyway Migration - Auth Service Schema
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(20) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'WORKER', 'ADMIN')),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
                    CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    profile_image_url TEXT,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP
);

-- Indexes on users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_role_status ON users(role, verification_status);

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(255) NOT NULL UNIQUE,
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    expires_at      TIMESTAMP NOT NULL,
    is_revoked      BOOLEAN NOT NULL DEFAULT FALSE,
    device_info     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes on refresh_tokens
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expires ON refresh_tokens(expires_at);

-- OTP tokens table
CREATE TABLE IF NOT EXISTS otp_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    otp_code        VARCHAR(10) NOT NULL,
    purpose         VARCHAR(30) NOT NULL,
    attempts        INTEGER NOT NULL DEFAULT 0,
    max_attempts    INTEGER NOT NULL DEFAULT 5,
    is_used         BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at      TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes on otp_tokens
CREATE INDEX idx_otp_email ON otp_tokens(email);
CREATE INDEX idx_otp_expires ON otp_tokens(expires_at);
CREATE INDEX idx_otp_email_purpose ON otp_tokens(email, purpose, is_used);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
