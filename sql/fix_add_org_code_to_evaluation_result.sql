-- Add org_code column to evaluation_result to store owning organization code
-- PostgreSQL / H2
ALTER TABLE evaluation_result ADD COLUMN IF NOT EXISTS org_code TEXT;

-- MySQL (8.0+). For older MySQL, drop IF NOT EXISTS.
ALTER TABLE evaluation_result ADD COLUMN IF NOT EXISTS org_code VARCHAR(64);

-- Optional: backfill logic (example)
-- UPDATE evaluation_result SET org_code = 'DEFAULT_ORG' WHERE org_code IS NULL;

