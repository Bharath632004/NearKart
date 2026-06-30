-- =============================================================
-- Docker Init Script
-- Creates DB user and database for local development
-- =============================================================

CREATE USER nearkart WITH PASSWORD 'nearkart123';
CREATE DATABASE nearkart_db OWNER nearkart;
GRANT ALL PRIVILEGES ON DATABASE nearkart_db TO nearkart;

-- Connect to nearkart_db and run migrations
\c nearkart_db
GRANT ALL ON SCHEMA public TO nearkart;
