-- ============================================
-- Helper Platform - Database Initialization
-- ============================================
-- This script runs automatically when PostgreSQL container starts for the first time.

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create enum types
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('CUSTOMER', 'WORKER', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE verification_status AS ENUM ('PENDING', 'VERIFIED', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Note: The application uses JPA/Hibernate for table creation.
-- This script only sets up extensions and types.
-- For production, use Flyway or Liquibase for schema migrations.

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE helperdb TO helper_admin;
