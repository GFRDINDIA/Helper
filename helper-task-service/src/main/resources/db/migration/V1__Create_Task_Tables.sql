-- ============================================
-- V1__Create_Task_Tables.sql
-- Flyway Migration - Task Service Schema
-- ============================================

-- Enable PostGIS extension (required for production)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    task_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id         UUID NOT NULL,
    title               VARCHAR(200) NOT NULL,
    description         TEXT NOT NULL,
    domain              VARCHAR(30) NOT NULL CHECK (domain IN (
        'DELIVERY', 'ELECTRICIAN', 'PLUMBING', 'CONSTRUCTION', 'FARMING',
        'MEDICAL', 'EDUCATION', 'LOGISTICS', 'FINANCE', 'HOUSEHOLD')),
    pricing_model       VARCHAR(20) NOT NULL CHECK (pricing_model IN ('FIXED', 'BIDDING')),
    status              VARCHAR(20) NOT NULL DEFAULT 'POSTED' CHECK (status IN (
        'POSTED', 'OPEN', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED',
        'PAYMENT_DONE', 'CLOSED', 'CANCELLED', 'DISPUTED')),
    budget              DECIMAL(10,2),
    final_price         DECIMAL(10,2),
    latitude            DOUBLE PRECISION NOT NULL,
    longitude           DOUBLE PRECISION NOT NULL,
    geo_location        GEOGRAPHY(POINT, 4326), -- PostGIS spatial column
    address             TEXT NOT NULL,
    assigned_worker_id  UUID,
    scheduled_at        TIMESTAMP,
    cancellation_reason TEXT,
    cancelled_by        UUID,
    dispute_reason      TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP
);

-- Auto-populate geo_location from lat/lng
CREATE OR REPLACE FUNCTION update_geo_location()
RETURNS TRIGGER AS $$
BEGIN
    NEW.geo_location = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_geo_location
    BEFORE INSERT OR UPDATE OF latitude, longitude ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_geo_location();

-- Task images (separate table for ElementCollection)
CREATE TABLE IF NOT EXISTS task_images (
    task_id     UUID NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
    image_url   TEXT NOT NULL
);

-- Bids table
CREATE TABLE IF NOT EXISTS bids (
    bid_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         UUID NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
    worker_id       UUID NOT NULL,
    proposed_price  DECIMAL(10,2) NOT NULL,
    message         TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN (
        'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    responded_at    TIMESTAMP,
    UNIQUE(task_id, worker_id) -- One bid per worker per task
);

-- ===== INDEXES =====

-- Tasks: Spatial index for geo queries (PostGIS)
CREATE INDEX idx_task_geo ON tasks USING GIST (geo_location);

-- Tasks: Status + domain + created_at for filtered listing
CREATE INDEX idx_task_status_domain ON tasks(status, domain, created_at DESC);

-- Tasks: Customer lookup
CREATE INDEX idx_task_customer ON tasks(customer_id);

-- Tasks: Worker lookup
CREATE INDEX idx_task_worker ON tasks(assigned_worker_id);

-- Tasks: Lat/lng for Haversine fallback
CREATE INDEX idx_task_location ON tasks(latitude, longitude);

-- Bids: Task + status for bid retrieval
CREATE INDEX idx_bid_task_status ON bids(task_id, status);

-- Bids: Worker lookup
CREATE INDEX idx_bid_worker ON bids(worker_id);

-- Task images
CREATE INDEX idx_task_images ON task_images(task_id);
