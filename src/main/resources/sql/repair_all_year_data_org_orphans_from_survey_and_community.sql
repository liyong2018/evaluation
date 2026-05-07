-- 全年份修复乡镇表/社区表与基层组织机构表的孤点。
-- 覆盖 survey_data 与 community_disaster_reduction_capacity 中实际存在的年份。

CREATE TABLE IF NOT EXISTS grassroots_organization_bak_all_year_orphan_repair_20260505 AS
SELECT *
FROM grassroots_organization;

DROP TEMPORARY TABLE IF EXISTS tmp_data_years;
CREATE TEMPORARY TABLE tmp_data_years AS
SELECT year FROM survey_data WHERE year IS NOT NULL GROUP BY year
UNION
SELECT year FROM community_disaster_reduction_capacity WHERE year IS NOT NULL GROUP BY year;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_org_by_year;
CREATE TEMPORARY TABLE tmp_effective_org_by_year AS
SELECT target_year, id, code, name, level, city_name, county_name
FROM (
  SELECT y.year AS target_year, o.id, o.code, o.name, o.level, o.city_name, o.county_name, o.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY y.year, o.code ORDER BY o.year DESC, o.id DESC) AS rn
  FROM tmp_data_years y
  JOIN organization o ON o.year <= y.year
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_before;
CREATE TEMPORARY TABLE tmp_effective_grass_before AS
SELECT target_year, id, county_id, parent_id, code, name, level, province_name, city_name, county_name, township_name, community_name
FROM (
  SELECT y.year AS target_year, g.id, g.county_id, g.parent_id, g.code, g.name, g.level,
         g.province_name, g.city_name, g.county_name, g.township_name, g.community_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY y.year, g.code ORDER BY g.year DESC, g.id DESC) AS rn
  FROM tmp_data_years y
  JOIN grassroots_organization g ON g.year <= y.year
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_township_source_all_years;
CREATE TEMPORARY TABLE tmp_township_source_all_years AS
SELECT
  year,
  township_code,
  LEFT(township_code, 6) AS county_code,
  MAX(NULLIF(province_name, '')) AS province_name,
  MAX(NULLIF(city_name, '')) AS city_name,
  MAX(NULLIF(county_name, '')) AS county_name,
  COALESCE(
    MAX(CASE WHEN township_name IS NOT NULL AND township_name <> '' AND township_name NOT REGEXP '^[0-9]+$' THEN township_name END),
    MAX(NULLIF(township_name, '')),
    township_code
  ) AS township_name
FROM (
  SELECT year, LEFT(region_code, 9) AS township_code, province AS province_name, city AS city_name,
         county AS county_name, township AS township_name
  FROM survey_data
  WHERE year IS NOT NULL AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 9
  UNION ALL
  SELECT year, LEFT(region_code, 9) AS township_code, province_name, city_name, county_name, township_name
  FROM community_disaster_reduction_capacity
  WHERE year IS NOT NULL AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 9
) s
GROUP BY year, township_code;

DROP TEMPORARY TABLE IF EXISTS tmp_missing_township_all_years;
CREATE TEMPORARY TABLE tmp_missing_township_all_years AS
SELECT
  src.year,
  src.township_code,
  src.county_code,
  COALESCE(org.id, 0) AS county_id,
  COALESCE(NULLIF(src.province_name, ''), '四川省') AS province_name,
  COALESCE(NULLIF(src.city_name, ''), org.city_name, '') AS city_name,
  COALESCE(
    CASE WHEN src.county_name IS NOT NULL AND src.county_name <> '' AND src.county_name NOT REGEXP '^[0-9]+$' THEN src.county_name END,
    org.name,
    src.county_code
  ) AS county_name,
  src.township_name
FROM tmp_township_source_all_years src
LEFT JOIN tmp_effective_grass_before g
  ON g.target_year = src.year AND g.code = src.township_code AND g.level = 4
LEFT JOIN tmp_effective_org_by_year org
  ON org.target_year = src.year AND org.code = src.county_code AND org.level = 3
WHERE g.id IS NULL;

INSERT INTO grassroots_organization (
  county_id, parent_id, code, name, level, year, data_source,
  province_name, city_name, county_name, township_name, community_name,
  create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
  county_id, NULL, township_code, township_name, 4, year, 'DATA_ORG_REPAIR',
  province_name, city_name, county_name, township_name, NULL,
  NOW(), NOW(), 0, CASE WHEN year = 2020 THEN 1 ELSE 0 END, township_code
FROM tmp_missing_township_all_years
ON DUPLICATE KEY UPDATE
  county_id = VALUES(county_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  level = VALUES(level),
  data_source = VALUES(data_source),
  province_name = VALUES(province_name),
  city_name = VALUES(city_name),
  county_name = VALUES(county_name),
  township_name = VALUES(township_name),
  community_name = VALUES(community_name),
  update_time = NOW(),
  is_deleted = 0,
  is_baseline = VALUES(is_baseline),
  baseline_code = VALUES(baseline_code);

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_after_township;
CREATE TEMPORARY TABLE tmp_effective_grass_after_township AS
SELECT target_year, id, county_id, parent_id, code, name, level, province_name, city_name, county_name, township_name, community_name
FROM (
  SELECT y.year AS target_year, g.id, g.county_id, g.parent_id, g.code, g.name, g.level,
         g.province_name, g.city_name, g.county_name, g.township_name, g.community_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY y.year, g.code ORDER BY g.year DESC, g.id DESC) AS rn
  FROM tmp_data_years y
  JOIN grassroots_organization g ON g.year <= y.year
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_after_township_parent;
CREATE TEMPORARY TABLE tmp_effective_grass_after_township_parent AS
SELECT * FROM tmp_effective_grass_after_township;

DROP TEMPORARY TABLE IF EXISTS tmp_community_source_all_years;
CREATE TEMPORARY TABLE tmp_community_source_all_years AS
SELECT
  year,
  region_code AS community_code,
  LEFT(region_code, 9) AS township_code,
  LEFT(region_code, 6) AS county_code,
  MAX(NULLIF(province_name, '')) AS province_name,
  MAX(NULLIF(city_name, '')) AS city_name,
  MAX(NULLIF(county_name, '')) AS county_name,
  MAX(NULLIF(township_name, '')) AS township_name,
  COALESCE(
    MAX(CASE WHEN community_name IS NOT NULL AND community_name <> '' AND community_name NOT REGEXP '^[0-9]+$' THEN community_name END),
    MAX(NULLIF(community_name, '')),
    region_code
  ) AS community_name
FROM community_disaster_reduction_capacity
WHERE year IS NOT NULL AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 12
GROUP BY year, region_code;

DROP TEMPORARY TABLE IF EXISTS tmp_missing_community_all_years;
CREATE TEMPORARY TABLE tmp_missing_community_all_years AS
SELECT
  src.year,
  src.community_code,
  src.township_code,
  src.county_code,
  COALESCE(org.id, tg.county_id, 0) AS county_id,
  tg.id AS parent_id,
  COALESCE(NULLIF(src.province_name, ''), tg.province_name, '四川省') AS province_name,
  COALESCE(NULLIF(src.city_name, ''), tg.city_name, org.city_name, '') AS city_name,
  COALESCE(
    CASE WHEN src.county_name IS NOT NULL AND src.county_name <> '' AND src.county_name NOT REGEXP '^[0-9]+$' THEN src.county_name END,
    tg.county_name,
    org.name,
    src.county_code
  ) AS county_name,
  COALESCE(NULLIF(src.township_name, ''), tg.township_name, src.township_code) AS township_name,
  src.community_name
FROM tmp_community_source_all_years src
LEFT JOIN tmp_effective_grass_after_township cg
  ON cg.target_year = src.year AND cg.code = src.community_code AND cg.level = 5
LEFT JOIN tmp_effective_grass_after_township_parent tg
  ON tg.target_year = src.year AND tg.code = src.township_code AND tg.level = 4
LEFT JOIN tmp_effective_org_by_year org
  ON org.target_year = src.year AND org.code = src.county_code AND org.level = 3
WHERE cg.id IS NULL;

INSERT INTO grassroots_organization (
  county_id, parent_id, code, name, level, year, data_source,
  province_name, city_name, county_name, township_name, community_name,
  create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
  county_id, parent_id, community_code, community_name, 5, year, 'DATA_ORG_REPAIR',
  province_name, city_name, county_name, township_name, community_name,
  NOW(), NOW(), 0, CASE WHEN year = 2020 THEN 1 ELSE 0 END, community_code
FROM tmp_missing_community_all_years
ON DUPLICATE KEY UPDATE
  county_id = VALUES(county_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  level = VALUES(level),
  data_source = VALUES(data_source),
  province_name = VALUES(province_name),
  city_name = VALUES(city_name),
  county_name = VALUES(county_name),
  township_name = VALUES(township_name),
  community_name = VALUES(community_name),
  update_time = NOW(),
  is_deleted = 0,
  is_baseline = VALUES(is_baseline),
  baseline_code = VALUES(baseline_code);

SELECT year, 'missing_townships_repaired' AS item, COUNT(*) AS cnt
FROM tmp_missing_township_all_years
GROUP BY year
ORDER BY year;

SELECT year, 'missing_communities_repaired' AS item, COUNT(*) AS cnt
FROM tmp_missing_community_all_years
GROUP BY year
ORDER BY year;
