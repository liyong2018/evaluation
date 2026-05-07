-- Repair 2025 survey organization fields using the 2025 authority organization tables.
--
-- Authority:
-- - organization: province/city/county
-- - grassroots_organization: township/community
--
-- Scope:
-- - survey_data, year = 2025, township-level rows
-- - community_disaster_reduction_capacity, year = 2025, community-level rows
--
-- Safety:
-- - Creates timestamped backups before modifying data.
-- - Only updates rows with deterministic active organization matches.
-- - Leaves ambiguous/unmatched rows unchanged and records them in
--   survey_2025_organization_repair_issue.

SET @repair_suffix = DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s');
SET @survey_backup_sql = CONCAT(
    'CREATE TABLE IF NOT EXISTS survey_data_bak_2025_org_repair_', @repair_suffix,
    ' AS SELECT * FROM survey_data WHERE year = 2025'
);
SET @community_backup_sql = CONCAT(
    'CREATE TABLE IF NOT EXISTS community_capacity_bak_2025_org_repair_', @repair_suffix,
    ' AS SELECT * FROM community_disaster_reduction_capacity WHERE year = 2025'
);

PREPARE stmt FROM @survey_backup_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

PREPARE stmt FROM @community_backup_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TABLE IF EXISTS survey_2025_organization_repair_issue;
CREATE TABLE survey_2025_organization_repair_issue (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(128) NOT NULL,
    record_id BIGINT NOT NULL,
    old_region_code VARCHAR(50),
    old_province_name VARCHAR(100),
    old_city_name VARCHAR(100),
    old_county_name VARCHAR(100),
    old_township_name VARCHAR(100),
    old_community_name VARCHAR(100),
    issue_type VARCHAR(64) NOT NULL,
    detail VARCHAR(512),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_table_record (table_name, record_id),
    KEY idx_issue_type (issue_type),
    KEY idx_old_region_code (old_region_code)
);

DROP TEMPORARY TABLE IF EXISTS tmp_2025_active_township;
CREATE TEMPORARY TABLE tmp_2025_active_township AS
SELECT
    id,
    code,
    name,
    province_name,
    city_name,
    county_name,
    township_name
FROM grassroots_organization
WHERE year = 2025
  AND level = 4
  AND is_deleted = 0;

ALTER TABLE tmp_2025_active_township ADD PRIMARY KEY (code);

DROP TEMPORARY TABLE IF EXISTS tmp_2025_active_community;
CREATE TEMPORARY TABLE tmp_2025_active_community AS
SELECT
    id,
    code,
    LEFT(code, 9) AS township_code,
    name,
    province_name,
    city_name,
    county_name,
    township_name,
    community_name
FROM grassroots_organization
WHERE year = 2025
  AND level = 5
  AND is_deleted = 0;

ALTER TABLE tmp_2025_active_community ADD PRIMARY KEY (code);
ALTER TABLE tmp_2025_active_community ADD KEY idx_township_community (township_code, community_name);

DROP TEMPORARY TABLE IF EXISTS tmp_2025_unique_community_name;
CREATE TEMPORARY TABLE tmp_2025_unique_community_name AS
SELECT
    township_code,
    community_name,
    MIN(code) AS code,
    COUNT(*) AS match_count
FROM tmp_2025_active_community
GROUP BY township_code, community_name
HAVING COUNT(*) = 1;

ALTER TABLE tmp_2025_unique_community_name ADD PRIMARY KEY (township_code, community_name);

DROP TEMPORARY TABLE IF EXISTS tmp_2025_unique_county_community_name;
CREATE TEMPORARY TABLE tmp_2025_unique_county_community_name AS
SELECT
    LEFT(code, 6) AS county_code,
    community_name,
    MIN(code) AS code,
    COUNT(*) AS match_count
FROM tmp_2025_active_community
GROUP BY LEFT(code, 6), community_name
HAVING COUNT(*) = 1;

ALTER TABLE tmp_2025_unique_county_community_name ADD PRIMARY KEY (county_code, community_name);

INSERT INTO survey_2025_organization_repair_issue (
    table_name,
    record_id,
    old_region_code,
    old_province_name,
    old_city_name,
    old_county_name,
    old_township_name,
    issue_type,
    detail
)
SELECT
    'survey_data',
    s.id,
    s.region_code,
    s.province,
    s.city,
    s.county,
    s.township,
    'TOWNSHIP_NOT_ACTIVE_2025',
    'survey_data.region_code cannot be matched to an active 2025 township'
FROM survey_data s
LEFT JOIN tmp_2025_active_township t ON t.code = s.region_code
WHERE s.year = 2025
  AND t.code IS NULL;

UPDATE survey_data s
JOIN tmp_2025_active_township t ON t.code = s.region_code
SET
    s.province = t.province_name,
    s.city = t.city_name,
    s.county = t.county_name,
    s.township = COALESCE(t.township_name, t.name),
    s.update_time = CURRENT_TIMESTAMP
WHERE s.year = 2025;

UPDATE community_disaster_reduction_capacity c
JOIN tmp_2025_unique_community_name u
    ON u.township_code = c.region_code
   AND u.community_name = c.community_name
JOIN tmp_2025_active_community ac ON ac.code = u.code
SET
    c.region_code = ac.code,
    c.province_name = ac.province_name,
    c.city_name = ac.city_name,
    c.county_name = ac.county_name,
    c.township_name = ac.township_name,
    c.community_name = ac.community_name,
    c.update_time = CURRENT_TIMESTAMP
WHERE c.year = 2025
  AND CHAR_LENGTH(c.region_code) = 9;

UPDATE community_disaster_reduction_capacity c
JOIN tmp_2025_unique_county_community_name u
    ON u.county_code = LEFT(c.region_code, 6)
   AND u.community_name = c.community_name
JOIN tmp_2025_active_community ac ON ac.code = u.code
LEFT JOIN community_disaster_reduction_capacity existing
    ON existing.year = c.year
   AND existing.region_code = ac.code
   AND existing.community_name = ac.community_name
   AND existing.id <> c.id
SET
    c.region_code = ac.code,
    c.province_name = ac.province_name,
    c.city_name = ac.city_name,
    c.county_name = ac.county_name,
    c.township_name = ac.township_name,
    c.community_name = ac.community_name,
    c.update_time = CURRENT_TIMESTAMP
WHERE c.year = 2025
  AND CHAR_LENGTH(c.region_code) = 9
  AND existing.id IS NULL;

UPDATE community_disaster_reduction_capacity c
JOIN tmp_2025_active_community ac ON ac.code = c.region_code
SET
    c.province_name = ac.province_name,
    c.city_name = ac.city_name,
    c.county_name = ac.county_name,
    c.township_name = ac.township_name,
    c.community_name = ac.community_name,
    c.update_time = CURRENT_TIMESTAMP
WHERE c.year = 2025
  AND CHAR_LENGTH(c.region_code) = 12;

INSERT INTO survey_2025_organization_repair_issue (
    table_name,
    record_id,
    old_region_code,
    old_province_name,
    old_city_name,
    old_county_name,
    old_township_name,
    old_community_name,
    issue_type,
    detail
)
SELECT
    'community_disaster_reduction_capacity',
    c.id,
    c.region_code,
    c.province_name,
    c.city_name,
    c.county_name,
    c.township_name,
    c.community_name,
    'COMMUNITY_NOT_UNIQUELY_MATCHED_2025',
    'region_code/community_name cannot be matched to one active 2025 community'
FROM community_disaster_reduction_capacity c
LEFT JOIN tmp_2025_active_community ac
    ON ac.code = c.region_code
WHERE c.year = 2025
  AND ac.code IS NULL;

SELECT 'survey_backup_suffix' AS metric, @repair_suffix AS value
UNION ALL
SELECT 'survey_rows_2025', CAST(COUNT(*) AS CHAR)
FROM survey_data
WHERE year = 2025
UNION ALL
SELECT 'survey_matched_active_township', CAST(COUNT(*) AS CHAR)
FROM survey_data s
JOIN tmp_2025_active_township t ON t.code = s.region_code
WHERE s.year = 2025
UNION ALL
SELECT 'community_rows_2025', CAST(COUNT(*) AS CHAR)
FROM community_disaster_reduction_capacity
WHERE year = 2025
UNION ALL
SELECT 'community_region_code_12_digit', CAST(COUNT(*) AS CHAR)
FROM community_disaster_reduction_capacity
WHERE year = 2025
  AND CHAR_LENGTH(region_code) = 12
UNION ALL
SELECT 'community_region_code_active_match', CAST(COUNT(*) AS CHAR)
FROM community_disaster_reduction_capacity c
JOIN tmp_2025_active_community ac ON ac.code = c.region_code
WHERE c.year = 2025
UNION ALL
SELECT 'repair_issue_rows', CAST(COUNT(*) AS CHAR)
FROM survey_2025_organization_repair_issue;

DROP TEMPORARY TABLE IF EXISTS tmp_2025_unique_county_community_name;
DROP TEMPORARY TABLE IF EXISTS tmp_2025_unique_community_name;
DROP TEMPORARY TABLE IF EXISTS tmp_2025_active_community;
DROP TEMPORARY TABLE IF EXISTS tmp_2025_active_township;
