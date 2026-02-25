-- ============================================
-- V1__Create_Notification_Tables.sql
-- Flyway Migration - Notification Service
-- Helper Marketplace Platform
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===== 1. NOTIFICATIONS TABLE =====
CREATE TABLE IF NOT EXISTS notifications (
    notification_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    event               VARCHAR(30) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    body                TEXT NOT NULL,
    data_json           TEXT,
    priority            VARCHAR(15) NOT NULL DEFAULT 'NORMAL',
    status              VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMP,
    push_sent           BOOLEAN DEFAULT FALSE,
    sms_sent            BOOLEAN DEFAULT FALSE,
    email_sent          BOOLEAN DEFAULT FALSE,
    retry_count         INTEGER DEFAULT 0,
    error_message       TEXT,
    created_at          TIMESTAMP DEFAULT NOW()
);

-- ===== 2. DEVICE TOKENS TABLE =====
CREATE TABLE IF NOT EXISTS device_tokens (
    token_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    token               TEXT NOT NULL UNIQUE,
    platform            VARCHAR(20) DEFAULT 'ANDROID',
    device_name         VARCHAR(100),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- ===== 3. USER NOTIFICATION PREFERENCES =====
CREATE TABLE IF NOT EXISTS user_notification_preferences (
    user_id                 UUID PRIMARY KEY,
    push_enabled            BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_start_hour        INTEGER DEFAULT 22,
    quiet_end_hour          INTEGER DEFAULT 7,
    promotional_enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at              TIMESTAMP DEFAULT NOW()
);

-- ===== INDEXES =====
CREATE INDEX idx_notif_user_status ON notifications(user_id, status, created_at);
CREATE INDEX idx_notif_user_read ON notifications(user_id, is_read, created_at);
CREATE INDEX idx_notif_event ON notifications(event);
CREATE INDEX idx_notif_status_retry ON notifications(status, retry_count);
CREATE INDEX idx_notif_created ON notifications(created_at);
CREATE INDEX idx_device_user ON device_tokens(user_id);
CREATE INDEX idx_device_active ON device_tokens(is_active);
