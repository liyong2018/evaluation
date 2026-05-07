-- 用生效基层组织机构名称回填乡镇表和社区表的显示名称。
-- 前置条件：已执行 sync_all_year_data_org_names_from_tables.sql。

CREATE TABLE IF NOT EXISTS survey_data_bak_effective_grass_name_sync_20260505 AS
SELECT *
FROM survey_data;

CREATE TABLE IF NOT EXISTS community_capacity_bak_effective_grass_name_sync_20260505 AS
SELECT *
FROM community_disaster_reduction_capacity;

DROP TEMPORARY TABLE IF EXISTS tmp_data_years;
CREATE TEMPORARY TABLE tmp_data_years AS
SELECT year FROM survey_data WHERE year IS NOT NULL GROUP BY year
UNION
SELECT year FROM community_disaster_reduction_capacity WHERE year IS NOT NULL GROUP BY year;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_township;
CREATE TEMPORARY TABLE tmp_effective_township AS
SELECT target_year, code, name, county_name, township_name
FROM (
  SELECT y.year AS target_year, g.code, g.name, g.county_name, g.township_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY y.year, g.code ORDER BY g.year DESC, g.id DESC) AS rn
  FROM tmp_data_years y
  JOIN grassroots_organization g ON g.year <= y.year
  WHERE g.level = 4
) x
WHERE rn = 1 AND is_deleted = 0;

ALTER TABLE tmp_effective_township
  ADD INDEX idx_effective_township_year_code (target_year, code);

DROP TEMPORARY TABLE IF EXISTS tmp_effective_township_for_community;
CREATE TEMPORARY TABLE tmp_effective_township_for_community AS
SELECT * FROM tmp_effective_township;

ALTER TABLE tmp_effective_township_for_community
  ADD INDEX idx_effective_township_comm_year_code (target_year, code);

DROP TEMPORARY TABLE IF EXISTS tmp_effective_community;
CREATE TEMPORARY TABLE tmp_effective_community AS
SELECT target_year, code, name, community_name
FROM (
  SELECT y.year AS target_year, g.code, g.name, g.community_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY y.year, g.code ORDER BY g.year DESC, g.id DESC) AS rn
  FROM tmp_data_years y
  JOIN grassroots_organization g ON g.year <= y.year
  WHERE g.level = 5
) x
WHERE rn = 1 AND is_deleted = 0;

ALTER TABLE tmp_effective_community
  ADD INDEX idx_effective_community_year_code (target_year, code);

UPDATE survey_data s
JOIN tmp_effective_township t
  ON t.target_year = s.year
 AND t.code = LEFT(s.region_code, 9)
SET s.county = t.county_name,
    s.township = t.name,
    s.update_time = NOW()
WHERE s.year IS NOT NULL
  AND s.region_code IS NOT NULL
  AND CHAR_LENGTH(s.region_code) >= 9
  AND (
    COALESCE(s.county, '') <> COALESCE(t.county_name, '')
    OR COALESCE(s.township, '') <> COALESCE(t.name, '')
  );

UPDATE community_disaster_reduction_capacity c
JOIN tmp_effective_township_for_community t
  ON t.target_year = c.year
 AND t.code = LEFT(c.region_code, 9)
SET c.county_name = t.county_name,
    c.township_name = t.name,
    c.update_time = NOW()
WHERE c.year IS NOT NULL
  AND c.region_code IS NOT NULL
  AND CHAR_LENGTH(c.region_code) >= 9
  AND (
    COALESCE(c.county_name, '') <> COALESCE(t.county_name, '')
    OR COALESCE(c.township_name, '') <> COALESCE(t.name, '')
  );

UPDATE community_disaster_reduction_capacity c
JOIN tmp_effective_community g
  ON g.target_year = c.year
 AND g.code = c.region_code
SET c.community_name = g.name,
    c.update_time = NOW()
WHERE c.year IS NOT NULL
  AND c.region_code IS NOT NULL
  AND CHAR_LENGTH(c.region_code) >= 12
  AND COALESCE(c.community_name, '') <> COALESCE(g.name, '');

SELECT 'survey_synced_from_effective_township' AS item, ROW_COUNT() AS last_update_count;
