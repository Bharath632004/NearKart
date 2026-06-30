-- =============================================================
-- V1: Enable Required PostgreSQL Extensions
-- NearKart Database
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";      -- UUID generation
CREATE EXTENSION IF NOT EXISTS "postgis";          -- Geospatial (GPS coordinates)
CREATE EXTENSION IF NOT EXISTS "pg_trgm";          -- Trigram indexes for fast text search
CREATE EXTENSION IF NOT EXISTS "btree_gin";        -- GIN indexes for composite searches
CREATE EXTENSION IF NOT EXISTS "pgcrypto";         -- Cryptographic functions
