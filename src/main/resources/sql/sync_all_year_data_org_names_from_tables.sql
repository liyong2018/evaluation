-- 同步全年份数据表与组织机构名称。
-- 目标：
-- 1. 修正 organization 中同代码明显冲突的区县名称。
-- 2. 按有效区县组织机构回填 survey_data/community 表的省市县名称。
-- 3. 按 survey_data/community 表实际代码与名称，同步 grassroots_organization 的乡镇/社区节点。

CREATE TABLE IF NOT EXISTS organization_bak_all_year_name_sync_20260505 AS
SELECT *
FROM organization;

CREATE TABLE IF NOT EXISTS survey_data_bak_all_year_name_sync_20260505 AS
SELECT *
FROM survey_data;

CREATE TABLE IF NOT EXISTS community_capacity_bak_all_year_name_sync_20260505 AS
SELECT *
FROM community_disaster_reduction_capacity;

CREATE TABLE IF NOT EXISTS grassroots_organization_bak_all_year_name_sync_20260505 AS
SELECT *
FROM grassroots_organization;

-- 同一代码跨年份名称冲突中，已由后续年份明确纠正的区县名称。
UPDATE organization
SET name = '沿滩区',
    county_name = '沿滩区',
    update_time = NOW()
WHERE year = 2020
  AND code = '510311'
  AND level = 3
  AND is_deleted = 0;

UPDATE organization
SET name = '前锋区',
    county_name = '前锋区',
    update_time = NOW()
WHERE year = 2020
  AND code = '511603'
  AND level = 3
  AND is_deleted = 0;

UPDATE organization
SET name = '绵阳市仙海水利风景区',
    county_name = '绵阳市仙海水利风景区',
    update_time = NOW()
WHERE code = '510772'
  AND level = 3
  AND is_deleted = 0;

UPDATE organization
SET name = '绵阳科技城科教创业园区',
    county_name = '绵阳科技城科教创业园区',
    update_time = NOW()
WHERE code = '510773'
  AND level = 3
  AND is_deleted = 0;

UPDATE organization
SET name = '协兴园区',
    county_name = '协兴园区',
    update_time = NOW()
WHERE code = '511672'
  AND level = 3
  AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_data_years;
CREATE TEMPORARY TABLE tmp_data_years AS
SELECT year FROM survey_data WHERE year IS NOT NULL GROUP BY year
UNION
SELECT year FROM community_disaster_reduction_capacity WHERE year IS NOT NULL GROUP BY year;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_county;
CREATE TEMPORARY TABLE tmp_effective_county AS
SELECT target_year, id, code, name, city_name
FROM (
  SELECT y.year AS target_year, o.id, o.code, o.name, o.city_name, o.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY y.year, o.code ORDER BY o.year DESC, o.id DESC) AS rn
  FROM tmp_data_years y
  JOIN organization o ON o.year <= y.year
  WHERE o.level = 3
) x
WHERE rn = 1 AND is_deleted = 0;

-- 先把数据表里的区县名按区县代码修正，避免“510371 却显示旌阳区/代码/空值”。
UPDATE survey_data s
JOIN tmp_effective_county ec
  ON ec.target_year = s.year
 AND ec.code = LEFT(s.region_code, 6)
SET s.province = '四川省',
    s.city = ec.city_name,
    s.county = ec.name,
    s.update_time = NOW()
WHERE s.year IS NOT NULL
  AND s.region_code IS NOT NULL
  AND CHAR_LENGTH(s.region_code) >= 6
  AND (
    COALESCE(s.province, '') <> '四川省'
    OR COALESCE(s.city, '') <> COALESCE(ec.city_name, '')
    OR COALESCE(s.county, '') <> COALESCE(ec.name, '')
  );

UPDATE community_disaster_reduction_capacity c
JOIN tmp_effective_county ec
  ON ec.target_year = c.year
 AND ec.code = LEFT(c.region_code, 6)
SET c.province_name = '四川省',
    c.city_name = ec.city_name,
    c.county_name = ec.name,
    c.update_time = NOW()
WHERE c.year IS NOT NULL
  AND c.region_code IS NOT NULL
  AND CHAR_LENGTH(c.region_code) >= 6
  AND (
    COALESCE(c.province_name, '') <> '四川省'
    OR COALESCE(c.city_name, '') <> COALESCE(ec.city_name, '')
    OR COALESCE(c.county_name, '') <> COALESCE(ec.name, '')
  );

DROP TEMPORARY TABLE IF EXISTS tmp_township_source;
CREATE TEMPORARY TABLE tmp_township_source AS
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
  SELECT year, LEFT(region_code, 9) AS township_code,
         province AS province_name, city AS city_name, county AS county_name, township AS township_name
  FROM survey_data
  WHERE year IS NOT NULL AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 9
  UNION ALL
  SELECT year, LEFT(region_code, 9) AS township_code,
         province_name, city_name, county_name, township_name
  FROM community_disaster_reduction_capacity
  WHERE year IS NOT NULL AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 9
) s
GROUP BY year, township_code;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_before;
CREATE TEMPORARY TABLE tmp_effective_grass_before AS
SELECT target_year, id, county_id, parent_id, code, name, level, year AS source_year,
       province_name, city_name, county_name, township_name, community_name, is_baseline
FROM (
  SELECT y.year AS target_year, g.*, 
         ROW_NUMBER() OVER (PARTITION BY y.year, g.code ORDER BY g.year DESC, g.id DESC) AS rn
  FROM tmp_data_years y
  JOIN grassroots_organization g ON g.year <= y.year
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_township_upserts;
CREATE TEMPORARY TABLE tmp_township_upserts AS
SELECT
  src.year,
  src.township_code,
  src.county_code,
  COALESCE(ec.id, eg.county_id, 0) AS county_id,
  COALESCE(NULLIF(src.province_name, ''), '四川省') AS province_name,
  COALESCE(NULLIF(src.city_name, ''), ec.city_name, '') AS city_name,
  COALESCE(NULLIF(src.county_name, ''), ec.name, src.county_code) AS county_name,
  src.township_name
FROM tmp_township_source src
LEFT JOIN tmp_effective_county ec
  ON ec.target_year = src.year AND ec.code = src.county_code
LEFT JOIN tmp_effective_grass_before eg
  ON eg.target_year = src.year AND eg.code = src.township_code AND eg.level = 4
WHERE eg.id IS NULL
   OR COALESCE(eg.name, '') <> COALESCE(src.township_name, '')
   OR COALESCE(eg.township_name, '') <> COALESCE(src.township_name, '')
   OR COALESCE(eg.county_name, '') <> COALESCE(NULLIF(src.county_name, ''), ec.name, src.county_code);

INSERT INTO grassroots_organization (
  county_id, parent_id, code, name, level, year, data_source,
  province_name, city_name, county_name, township_name, community_name,
  create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
  county_id, NULL, township_code, township_name, 4, year, 'DATA_ORG_NAME_SYNC',
  province_name, city_name, county_name, township_name, NULL,
  NOW(), NOW(), 0, CASE WHEN year = 2020 THEN 1 ELSE 0 END, township_code
FROM tmp_township_upserts
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
SELECT target_year, id, county_id, parent_id, code, name, level, year AS source_year,
       province_name, city_name, county_name, township_name, community_name, is_baseline
FROM (
  SELECT y.year AS target_year, g.*, 
         ROW_NUMBER() OVER (PARTITION BY y.year, g.code ORDER BY g.year DESC, g.id DESC) AS rn
  FROM tmp_data_years y
  JOIN grassroots_organization g ON g.year <= y.year
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_after_township_parent;
CREATE TEMPORARY TABLE tmp_effective_grass_after_township_parent AS
SELECT * FROM tmp_effective_grass_after_township;

DROP TEMPORARY TABLE IF EXISTS tmp_community_source;
CREATE TEMPORARY TABLE tmp_community_source AS
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

DROP TEMPORARY TABLE IF EXISTS tmp_community_upserts;
CREATE TEMPORARY TABLE tmp_community_upserts AS
SELECT
  src.year,
  src.community_code,
  src.township_code,
  src.county_code,
  COALESCE(ec.id, tg.county_id, cg.county_id, 0) AS county_id,
  tg.id AS parent_id,
  COALESCE(NULLIF(src.province_name, ''), tg.province_name, '四川省') AS province_name,
  COALESCE(NULLIF(src.city_name, ''), tg.city_name, ec.city_name, '') AS city_name,
  COALESCE(NULLIF(src.county_name, ''), tg.county_name, ec.name, src.county_code) AS county_name,
  COALESCE(NULLIF(src.township_name, ''), tg.name, src.township_code) AS township_name,
  src.community_name
FROM tmp_community_source src
LEFT JOIN tmp_effective_county ec
  ON ec.target_year = src.year AND ec.code = src.county_code
LEFT JOIN tmp_effective_grass_after_township_parent tg
  ON tg.target_year = src.year AND tg.code = src.township_code AND tg.level = 4
LEFT JOIN tmp_effective_grass_after_township cg
  ON cg.target_year = src.year AND cg.code = src.community_code AND cg.level = 5
WHERE cg.id IS NULL
   OR COALESCE(cg.name, '') <> COALESCE(src.community_name, '')
   OR COALESCE(cg.community_name, '') <> COALESCE(src.community_name, '')
   OR COALESCE(cg.township_name, '') <> COALESCE(NULLIF(src.township_name, ''), tg.name, src.township_code)
   OR COALESCE(cg.county_name, '') <> COALESCE(NULLIF(src.county_name, ''), tg.county_name, ec.name, src.county_code);

INSERT INTO grassroots_organization (
  county_id, parent_id, code, name, level, year, data_source,
  province_name, city_name, county_name, township_name, community_name,
  create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
  county_id, parent_id, community_code, community_name, 5, year, 'DATA_ORG_NAME_SYNC',
  province_name, city_name, county_name, township_name, community_name,
  NOW(), NOW(), 0, CASE WHEN year = 2020 THEN 1 ELSE 0 END, community_code
FROM tmp_community_upserts
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

SELECT 'township_nodes_synced' AS item, year, COUNT(*) AS cnt
FROM tmp_township_upserts
GROUP BY year
ORDER BY year;

SELECT 'community_nodes_synced' AS item, year, COUNT(*) AS cnt
FROM tmp_community_upserts
GROUP BY year
ORDER BY year;
