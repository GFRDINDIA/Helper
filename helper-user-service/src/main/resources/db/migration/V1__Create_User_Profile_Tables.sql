-- ============================================
-- V1__Create_User_Profile_Tables.sql
-- Flyway Migration - User Profile & KYC Schema
-- ============================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Worker Profiles
CREATE TABLE IF NOT EXISTS worker_profiles (
    worker_id               UUID PRIMARY KEY,
    bio                     TEXT,
    profile_image_url       TEXT,
    latitude                DOUBLE PRECISION NOT NULL,
    longitude               DOUBLE PRECISION NOT NULL,
    geo_location            GEOGRAPHY(POINT, 4326),
    base_address            TEXT,
    average_rating          DOUBLE PRECISION DEFAULT 0.0,
    total_ratings           INTEGER DEFAULT 0,
    total_tasks_completed   INTEGER DEFAULT 0,
    verification_status     VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED')),
    is_available            BOOLEAN DEFAULT TRUE,
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_at              TIMESTAMP DEFAULT NOW()
);

-- Auto-populate geo_location for worker profiles
CREATE OR REPLACE FUNCTION update_worker_geo() RETURNS TRIGGER AS $$
BEGIN
    NEW.geo_location = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_worker_geo BEFORE INSERT OR UPDATE OF latitude, longitude
    ON worker_profiles FOR EACH ROW EXECUTE FUNCTION update_worker_geo();

-- Worker Skills (PRD: worker_skills table)
CREATE TABLE IF NOT EXISTS worker_skills (
    skill_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_id           UUID NOT NULL REFERENCES worker_profiles(worker_id) ON DELETE CASCADE,
    domain              VARCHAR(30) NOT NULL CHECK (domain IN (
        'DELIVERY','ELECTRICIAN','PLUMBING','CONSTRUCTION','FARMING',
        'MEDICAL','EDUCATION','LOGISTICS','FINANCE','HOUSEHOLD')),
    price_model         VARCHAR(20) NOT NULL CHECK (price_model IN ('FIXED','BIDDING','BOTH')),
    fixed_rate          DECIMAL(10,2),
    latitude            DOUBLE PRECISION NOT NULL,
    longitude           DOUBLE PRECISION NOT NULL,
    geo_location        GEOGRAPHY(POINT, 4326),
    service_radius_km   INTEGER DEFAULT 10,
    is_available        BOOLEAN DEFAULT TRUE,
    UNIQUE(worker_id, domain)
);

CREATE OR REPLACE FUNCTION update_skill_geo() RETURNS TRIGGER AS $$
BEGIN
    NEW.geo_location = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_skill_geo BEFORE INSERT OR UPDATE OF latitude, longitude
    ON worker_skills FOR EACH ROW EXECUTE FUNCTION update_skill_geo();

-- Portfolio Items
CREATE TABLE IF NOT EXISTS portfolio_items (
    item_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_id   UUID NOT NULL REFERENCES worker_profiles(worker_id) ON DELETE CASCADE,
    image_url   TEXT NOT NULL,
    description TEXT,
    domain      VARCHAR(30),
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Availability Slots
CREATE TABLE IF NOT EXISTS availability_slots (
    slot_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_id   UUID NOT NULL REFERENCES worker_profiles(worker_id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN (
        'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    start_time  TIME NOT NULL,
    end_time    TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    UNIQUE(worker_id, day_of_week)
);

-- KYC Documents
CREATE TABLE IF NOT EXISTS kyc_documents (
    document_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_id       UUID NOT NULL REFERENCES worker_profiles(worker_id) ON DELETE CASCADE,
    document_type   VARCHAR(30) NOT NULL CHECK (document_type IN (
        'AADHAAR_CARD','PAN_CARD','SELFIE','PROFESSIONAL_LICENSE',
        'EDUCATIONAL_CERTIFICATE','REGULATORY_LICENSE','OTHER')),
    kyc_level       VARCHAR(30) NOT NULL CHECK (kyc_level IN ('BASIC','PROFESSIONAL','PROFESSIONAL_PLUS_LICENSE')),
    document_url    TEXT NOT NULL,
    document_number VARCHAR(50),
    status          VARCHAR(30) DEFAULT 'SUBMITTED' CHECK (status IN (
        'NOT_SUBMITTED','SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','RESUBMISSION_REQUIRED','EXPIRED')),
    reviewed_by     UUID,
    review_comments TEXT,
    reviewed_at     TIMESTAMP,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- Customer Profiles
CREATE TABLE IF NOT EXISTS customer_profiles (
    customer_id         UUID PRIMARY KEY,
    profile_image_url   TEXT,
    average_rating      DOUBLE PRECISION DEFAULT 0.0,
    total_ratings       INTEGER DEFAULT 0,
    total_tasks_posted  INTEGER DEFAULT 0,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- Customer Addresses
CREATE TABLE IF NOT EXISTS customer_addresses (
    address_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID NOT NULL REFERENCES customer_profiles(customer_id) ON DELETE CASCADE,
    label           VARCHAR(50) NOT NULL,
    address_line1   TEXT NOT NULL,
    address_line2   TEXT,
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    pin_code        VARCHAR(10) NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    is_default      BOOLEAN DEFAULT FALSE
);

-- Saved Payment Methods
CREATE TABLE IF NOT EXISTS saved_payment_methods (
    method_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id         UUID NOT NULL REFERENCES customer_profiles(customer_id) ON DELETE CASCADE,
    method_type         VARCHAR(20) NOT NULL CHECK (method_type IN (
        'CASH','UPI','CREDIT_CARD','DEBIT_CARD','NET_BANKING','WALLET')),
    label               VARCHAR(100),
    masked_identifier   VARCHAR(50),
    is_default          BOOLEAN DEFAULT FALSE
);

-- ===== INDEXES =====
CREATE INDEX idx_wp_geo ON worker_profiles USING GIST (geo_location);
CREATE INDEX idx_wp_status ON worker_profiles(verification_status);
CREATE INDEX idx_ws_geo ON worker_skills USING GIST (geo_location);
CREATE INDEX idx_ws_domain ON worker_skills(domain);
CREATE INDEX idx_ws_worker ON worker_skills(worker_id);
CREATE INDEX idx_ws_location ON worker_skills(latitude, longitude);
CREATE INDEX idx_portfolio_worker ON portfolio_items(worker_id);
CREATE INDEX idx_avail_worker ON availability_slots(worker_id);
CREATE INDEX idx_kyc_worker ON kyc_documents(worker_id);
CREATE INDEX idx_kyc_status ON kyc_documents(status);
CREATE INDEX idx_addr_customer ON customer_addresses(customer_id);
CREATE INDEX idx_pm_customer ON saved_payment_methods(customer_id);
