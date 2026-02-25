-- ============================================
-- V1__Create_Rating_Tables.sql
-- Flyway Migration - Rating & Feedback Service
-- Helper Marketplace Platform
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===== 1. RATINGS TABLE (PRD Section 3.6 + 4.1) =====
CREATE TABLE IF NOT EXISTS ratings (
    rating_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id             UUID NOT NULL,
    given_by            UUID NOT NULL,
    given_to            UUID NOT NULL,
    score               INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    feedback            TEXT,
    rating_type         VARCHAR(25) NOT NULL CHECK (rating_type IN ('CUSTOMER_TO_WORKER', 'WORKER_TO_CUSTOMER')),
    is_visible          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT NOW(),

    -- One rating per direction per task
    CONSTRAINT uk_rating_task_by_to UNIQUE (task_id, given_by, given_to)
);

-- ===== 2. FLAGS TABLE =====
CREATE TABLE IF NOT EXISTS flags (
    flag_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rating_id           UUID REFERENCES ratings(rating_id),
    task_id             UUID NOT NULL,
    reporter_id         UUID NOT NULL,
    reported_user_id    UUID NOT NULL,
    reason              VARCHAR(30) NOT NULL CHECK (reason IN ('INAPPROPRIATE_LANGUAGE','HARASSMENT','FAKE_REVIEW','SPAM','DISCRIMINATION','SAFETY_CONCERN','OTHER')),
    description         TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','REVIEWED','DISMISSED','ACTION_TAKEN')),
    admin_notes         TEXT,
    reviewed_by         UUID,
    reviewed_at         TIMESTAMP,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- ===== 3. USER RATING SUMMARIES (precomputed) =====
CREATE TABLE IF NOT EXISTS user_rating_summaries (
    user_id             UUID PRIMARY KEY,
    average_rating      DECIMAL(3,2) DEFAULT 0.00,
    weighted_rating     DECIMAL(3,2) DEFAULT 0.00,
    total_ratings       INTEGER DEFAULT 0,
    total_five_star     INTEGER DEFAULT 0,
    total_four_star     INTEGER DEFAULT 0,
    total_three_star    INTEGER DEFAULT 0,
    total_two_star      INTEGER DEFAULT 0,
    total_one_star      INTEGER DEFAULT 0,
    total_flags_received INTEGER DEFAULT 0,
    is_public           BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- ===== INDEXES (PRD: B-tree on given_to, created_at) =====
CREATE INDEX idx_rating_given_to ON ratings(given_to, created_at);
CREATE INDEX idx_rating_given_by ON ratings(given_by);
CREATE INDEX idx_rating_task ON ratings(task_id);
CREATE INDEX idx_rating_type ON ratings(rating_type);
CREATE INDEX idx_rating_visible ON ratings(is_visible);
CREATE INDEX idx_flag_status ON flags(status, created_at);
CREATE INDEX idx_flag_reported ON flags(reported_user_id);
CREATE INDEX idx_flag_reporter ON flags(reporter_id);
CREATE INDEX idx_summary_weighted ON user_rating_summaries(weighted_rating DESC) WHERE is_public = TRUE;

-- ===== UPDATE TRIGGER =====
CREATE OR REPLACE FUNCTION update_flag_timestamp() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_flag_updated BEFORE UPDATE ON flags
    FOR EACH ROW EXECUTE FUNCTION update_flag_timestamp();
