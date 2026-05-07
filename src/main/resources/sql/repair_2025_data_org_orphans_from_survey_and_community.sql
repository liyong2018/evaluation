-- 全省范围修复 2025 年乡镇表/社区表与基层组织机构表的对应关系。
--
-- 目标：
-- 1. survey_data.region_code 的前 9 位必须能对应有效乡镇节点。
-- 2. community_disaster_reduction_capacity.region_code 的前 9 位必须能对应有效乡镇节点。
-- 3. community_disaster_reduction_capacity.region_code 全码必须能对应有效社区/村节点。
-- 4. 已存在但名称为空或名称为纯代码的节点，用业务数据中的名称补正。
--
-- 说明：
-- 这里不删除历史记录。若某个 code 的最新有效节点缺失，插入一条 2025 年修复节点；
-- 若旧节点被 is_deleted=1 标记，新增节点 id 更大，前端有效树会使用新节点。

CREATE TABLE IF NOT EXISTS grassroots_organization_bak_2025_orphan_repair_20260505 AS
SELECT *
FROM grassroots_organization
WHERE year <= 2025;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_org_2025;
CREATE TEMPORARY TABLE tmp_effective_org_2025 AS
SELECT id, code, name, level, year, city_name, county_name, is_deleted
FROM (
  SELECT o.id, o.code, o.name, o.level, o.year, o.city_name, o.county_name, o.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY code ORDER BY year DESC, id DESC) AS rn
  FROM organization o
  WHERE year <= 2025
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_2025_before;
CREATE TEMPORARY TABLE tmp_effective_grass_2025_before AS
SELECT id, county_id, parent_id, code, name, level, year, province_name, city_name, county_name, township_name, community_name, is_deleted
FROM (
  SELECT g.id, g.county_id, g.parent_id, g.code, g.name, g.level, g.year,
         g.province_name, g.city_name, g.county_name, g.township_name, g.community_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY code ORDER BY year DESC, id DESC) AS rn
  FROM grassroots_organization g
  WHERE year <= 2025
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_2025_township_source;
CREATE TEMPORARY TABLE tmp_2025_township_source AS
SELECT
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
  SELECT
    LEFT(region_code, 9) AS township_code,
    province AS province_name,
    city AS city_name,
    county AS county_name,
    township AS township_name
  FROM survey_data
  WHERE year = 2025 AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 9
  UNION ALL
  SELECT
    LEFT(region_code, 9) AS township_code,
    province_name,
    city_name,
    county_name,
    township_name
  FROM community_disaster_reduction_capacity
  WHERE year = 2025 AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 9
) s
GROUP BY township_code;

DROP TEMPORARY TABLE IF EXISTS tmp_2025_missing_township;
CREATE TEMPORARY TABLE tmp_2025_missing_township AS
SELECT
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
FROM tmp_2025_township_source src
LEFT JOIN tmp_effective_grass_2025_before g
  ON g.code = src.township_code AND g.level = 4
LEFT JOIN tmp_effective_org_2025 org
  ON org.code = src.county_code AND org.level = 3
WHERE g.id IS NULL;

INSERT INTO grassroots_organization (
  county_id, parent_id, code, name, level, year, data_source,
  province_name, city_name, county_name, township_name, community_name,
  create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
  county_id,
  NULL,
  township_code,
  township_name,
  4,
  2025,
  'DATA_ORG_REPAIR',
  province_name,
  city_name,
  county_name,
  township_name,
  NULL,
  NOW(),
  NOW(),
  0,
  0,
  township_code
FROM tmp_2025_missing_township
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

-- 用业务数据名称修正现有的代码化乡镇节点。
UPDATE grassroots_organization g
JOIN tmp_2025_township_source src
  ON g.code = src.township_code
LEFT JOIN tmp_effective_org_2025 org
  ON org.code = src.county_code AND org.level = 3
SET
  g.name = CASE WHEN g.name IS NULL OR g.name = '' OR g.name REGEXP '^[0-9]+$' THEN src.township_name ELSE g.name END,
  g.province_name = COALESCE(NULLIF(g.province_name, ''), NULLIF(src.province_name, ''), '四川省'),
  g.city_name = COALESCE(NULLIF(g.city_name, ''), NULLIF(src.city_name, ''), org.city_name, ''),
  g.county_name = COALESCE(
    CASE WHEN g.county_name IS NOT NULL AND g.county_name <> '' AND g.county_name NOT REGEXP '^[0-9]+$' THEN g.county_name END,
    CASE WHEN src.county_name IS NOT NULL AND src.county_name <> '' AND src.county_name NOT REGEXP '^[0-9]+$' THEN src.county_name END,
    org.name,
    src.county_code
  ),
  g.township_name = CASE
    WHEN g.township_name IS NULL OR g.township_name = '' OR g.township_name REGEXP '^[0-9]+$' THEN src.township_name
    ELSE g.township_name
  END,
  g.update_time = NOW()
WHERE g.year = 2025 AND g.level = 4 AND g.is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_2025_after_township;
CREATE TEMPORARY TABLE tmp_effective_grass_2025_after_township AS
SELECT id, county_id, parent_id, code, name, level, year, province_name, city_name, county_name, township_name, community_name, is_deleted
FROM (
  SELECT g.id, g.county_id, g.parent_id, g.code, g.name, g.level, g.year,
         g.province_name, g.city_name, g.county_name, g.township_name, g.community_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY code ORDER BY year DESC, id DESC) AS rn
  FROM grassroots_organization g
  WHERE year <= 2025
) x
WHERE rn = 1 AND is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_2025_after_township_parent;
CREATE TEMPORARY TABLE tmp_effective_grass_2025_after_township_parent AS
SELECT * FROM tmp_effective_grass_2025_after_township;

DROP TEMPORARY TABLE IF EXISTS tmp_2025_community_source;
CREATE TEMPORARY TABLE tmp_2025_community_source AS
SELECT
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
WHERE year = 2025 AND region_code IS NOT NULL AND CHAR_LENGTH(region_code) >= 12
GROUP BY region_code;

DROP TEMPORARY TABLE IF EXISTS tmp_2025_missing_community;
CREATE TEMPORARY TABLE tmp_2025_missing_community AS
SELECT
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
FROM tmp_2025_community_source src
LEFT JOIN tmp_effective_grass_2025_after_township cg
  ON cg.code = src.community_code AND cg.level = 5
LEFT JOIN tmp_effective_grass_2025_after_township_parent tg
  ON tg.code = src.township_code AND tg.level = 4
LEFT JOIN tmp_effective_org_2025 org
  ON org.code = src.county_code AND org.level = 3
WHERE cg.id IS NULL;

INSERT INTO grassroots_organization (
  county_id, parent_id, code, name, level, year, data_source,
  province_name, city_name, county_name, township_name, community_name,
  create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
  county_id,
  parent_id,
  community_code,
  community_name,
  5,
  2025,
  'DATA_ORG_REPAIR',
  province_name,
  city_name,
  county_name,
  township_name,
  community_name,
  NOW(),
  NOW(),
  0,
  0,
  community_code
FROM tmp_2025_missing_community
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

-- 用业务数据名称修正现有的代码化社区节点。
UPDATE grassroots_organization g
JOIN tmp_2025_community_source src
  ON g.code = src.community_code
LEFT JOIN tmp_effective_grass_2025_after_township tg
  ON tg.code = src.township_code AND tg.level = 4
LEFT JOIN tmp_effective_org_2025 org
  ON org.code = src.county_code AND org.level = 3
SET
  g.parent_id = COALESCE(g.parent_id, tg.id),
  g.name = CASE WHEN g.name IS NULL OR g.name = '' OR g.name REGEXP '^[0-9]+$' THEN src.community_name ELSE g.name END,
  g.province_name = COALESCE(NULLIF(g.province_name, ''), NULLIF(src.province_name, ''), tg.province_name, '四川省'),
  g.city_name = COALESCE(NULLIF(g.city_name, ''), NULLIF(src.city_name, ''), tg.city_name, org.city_name, ''),
  g.county_name = COALESCE(
    CASE WHEN g.county_name IS NOT NULL AND g.county_name <> '' AND g.county_name NOT REGEXP '^[0-9]+$' THEN g.county_name END,
    CASE WHEN src.county_name IS NOT NULL AND src.county_name <> '' AND src.county_name NOT REGEXP '^[0-9]+$' THEN src.county_name END,
    tg.county_name,
    org.name,
    src.county_code
  ),
  g.township_name = COALESCE(NULLIF(g.township_name, ''), NULLIF(src.township_name, ''), tg.township_name, src.township_code),
  g.community_name = CASE
    WHEN g.community_name IS NULL OR g.community_name = '' OR g.community_name REGEXP '^[0-9]+$' THEN src.community_name
    ELSE g.community_name
  END,
  g.update_time = NOW()
WHERE g.year = 2025 AND g.level = 5 AND g.is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_effective_grass_2025_final;
CREATE TEMPORARY TABLE tmp_effective_grass_2025_final AS
SELECT id, county_id, parent_id, code, name, level, year, province_name, city_name, county_name, township_name, community_name, is_deleted
FROM (
  SELECT g.id, g.county_id, g.parent_id, g.code, g.name, g.level, g.year,
         g.province_name, g.city_name, g.county_name, g.township_name, g.community_name, g.is_deleted,
         ROW_NUMBER() OVER (PARTITION BY code ORDER BY year DESC, id DESC) AS rn
  FROM grassroots_organization g
  WHERE year <= 2025
) x
WHERE rn = 1 AND is_deleted = 0;

SELECT 'inserted_missing_townships' AS item, COUNT(*) AS cnt FROM tmp_2025_missing_township;

SELECT 'inserted_missing_communities' AS item, COUNT(*) AS cnt FROM tmp_2025_missing_community;

SELECT 'survey_missing_township_after' AS item, COUNT(*) AS cnt
FROM survey_data s
LEFT JOIN tmp_effective_grass_2025_final g
  ON g.code = LEFT(s.region_code, 9) AND g.level = 4
WHERE s.year = 2025 AND g.id IS NULL;

SELECT 'community_missing_township_after' AS item, COUNT(*) AS cnt
FROM community_disaster_reduction_capacity c
LEFT JOIN tmp_effective_grass_2025_final g
  ON g.code = LEFT(c.region_code, 9) AND g.level = 4
WHERE c.year = 2025 AND g.id IS NULL;

SELECT 'community_missing_community_len12_after' AS item, COUNT(*) AS cnt
FROM community_disaster_reduction_capacity c
LEFT JOIN tmp_effective_grass_2025_final g
  ON g.code = c.region_code AND g.level = 5
WHERE c.year = 2025 AND CHAR_LENGTH(c.region_code) >= 12 AND g.id IS NULL;
