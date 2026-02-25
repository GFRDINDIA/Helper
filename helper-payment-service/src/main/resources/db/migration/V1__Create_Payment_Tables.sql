-- ============================================
-- V1__Create_Payment_Tables.sql
-- Flyway Migration - Payment Service Schema
-- Helper Marketplace Platform
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===== 1. PAYMENTS TABLE (PRD Section 4.1) =====
CREATE TABLE IF NOT EXISTS payments (
    payment_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id             UUID NOT NULL UNIQUE,
    payer_id            UUID NOT NULL,
    payee_id            UUID NOT NULL,

    -- Money fields (all DECIMAL for precision)
    amount              DECIMAL(10,2) NOT NULL,
    commission          DECIMAL(10,2) NOT NULL,
    commission_rate     DECIMAL(5,4) NOT NULL,
    tax                 DECIMAL(10,2) NOT NULL,
    tax_rate            DECIMAL(5,4) NOT NULL,
    tip                 DECIMAL(10,2) DEFAULT 0.00,
    worker_payout       DECIMAL(10,2) NOT NULL,

    -- Metadata
    method              VARCHAR(20) NOT NULL CHECK (method IN ('CASH','UPI','CARD','NET_BANKING','WALLET')),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','COMPLETED','REFUNDED','FAILED','CANCELLED')),
    invoice_number      VARCHAR(50) UNIQUE,
    invoice_url         TEXT,
    payment_reference   VARCHAR(100),
    notes               TEXT,
    processed_at        TIMESTAMP,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- ===== 2. WORKER LEDGER TABLE (Cash commission tracking) =====
CREATE TABLE IF NOT EXISTS worker_ledger (
    ledger_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_id           UUID NOT NULL,
    payment_id          UUID REFERENCES payments(payment_id),
    type                VARCHAR(25) NOT NULL CHECK (type IN ('COMMISSION_DUE','COMMISSION_PAID','BONUS','PENALTY','CANCELLATION_FEE')),
    amount              DECIMAL(10,2) NOT NULL,
    balance_after       DECIMAL(10,2) NOT NULL,
    description         TEXT,
    created_at          TIMESTAMP DEFAULT NOW()
);

-- ===== 3. PLATFORM CONFIG TABLE (Admin-configurable rates) =====
CREATE TABLE IF NOT EXISTS platform_config (
    config_key          VARCHAR(50) PRIMARY KEY,
    config_value        VARCHAR(100) NOT NULL,
    description         TEXT,
    updated_by          UUID,
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- ===== INDEXES =====
CREATE INDEX idx_pay_task ON payments(task_id);
CREATE INDEX idx_pay_payer ON payments(payer_id);
CREATE INDEX idx_pay_payee ON payments(payee_id);
CREATE INDEX idx_pay_status_date ON payments(status, created_at);
CREATE INDEX idx_pay_method ON payments(method);
CREATE INDEX idx_pay_invoice ON payments(invoice_number);
CREATE INDEX idx_ledger_worker ON worker_ledger(worker_id, created_at);
CREATE INDEX idx_ledger_payment ON worker_ledger(payment_id);

-- ===== SEED DATA: Default platform config =====
INSERT INTO platform_config (config_key, config_value, description) VALUES
    ('COMMISSION_RATE', '0.02', 'Platform commission rate (2%). Deducted from worker payout per transaction.'),
    ('GST_RATE', '0.18', 'GST rate (18%). Applied on platform commission only, not on task price.'),
    ('CANCELLATION_FEE_RATE', '0.10', 'Cancellation fee rate (10%). Applied when task cancelled after ACCEPTED status.');

-- ===== UPDATE TRIGGER =====
CREATE OR REPLACE FUNCTION update_payment_timestamp() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_payment_updated BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_payment_timestamp();
