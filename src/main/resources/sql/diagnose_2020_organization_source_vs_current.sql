-- ============================================================
-- 2020 评估源组织机构 vs 当前组织机构基准体检脚本
-- 只读诊断，不修改正式数据。
--
-- 核心口径：
-- - survey_data 是 2020 乡镇评估数据源。
-- - community_disaster_reduction_capacity 是 2020 村社评估数据源。
-- - organization / grassroots_organization 的 2020 + is_baseline=1
--   应能解释这些评估源组织机构。
-- ============================================================

SET NAMES utf8mb4;

-- 1. 总量对比
SELECT 'survey_2020_townships' AS metric,
       COUNT(*) AS rows_count,
       COUNT(DISTINCT LEFT(TRIM(region_code), 9)) AS distinct_codes
FROM survey_data
WHERE is_deleted = 0
  AND year = 2020;

SELECT 'community_2020_communities' AS metric,
       COUNT(*) AS rows_count,
       COUNT(DISTINCT TRIM(region_code)) AS distinct_codes,
       SUM(CHAR_LENGTH(TRIM(region_code)) = 9) AS len9_count,
       SUM(CHAR_LENGTH(TRIM(region_code)) >= 12) AS len12_count
FROM community_disaster_reduction_capacity
WHERE year = 2020;

SELECT 'current_2020_grassroots' AS metric,
       level,
       COUNT(*) AS active_count
FROM grassroots_organization
WHERE year = 2020
  AND is_baseline = 1
  AND is_deleted = 0
  AND level IN (4, 5)
GROUP BY level
ORDER BY level;

-- 2. 县级组织机构与 2020 评估源不一致
WITH src AS (
    SELECT LEFT(TRIM(region_code), 6) AS code,
           MAX(TRIM(province)) AS province_name,
           MAX(TRIM(city)) AS city_name,
           MAX(TRIM(county)) AS county_name
    FROM survey_data
    WHERE is_deleted = 0
      AND year = 2020
      AND CHAR_LENGTH(TRIM(region_code)) >= 6
    GROUP BY LEFT(TRIM(region_code), 6)
    UNION
    SELECT LEFT(TRIM(region_code), 6) AS code,
           MAX(TRIM(province_name)) AS province_name,
           MAX(TRIM(city_name)) AS city_name,
           MAX(TRIM(county_name)) AS county_name
    FROM community_disaster_reduction_capacity
    WHERE year = 2020
      AND CHAR_LENGTH(TRIM(region_code)) >= 6
    GROUP BY LEFT(TRIM(region_code), 6)
),
src2 AS (
    SELECT code,
           MAX(province_name) AS province_name,
           MAX(city_name) AS city_name,
           MAX(county_name) AS county_name
    FROM src
    GROUP BY code
),
cur AS (
    SELECT code, name, province_name, city_name, county_name
    FROM organization
    WHERE year = 2020
      AND is_baseline = 1
      AND is_deleted = 0
      AND level = 3
)
SELECT src2.code,
       src2.city_name AS source_city,
       cur.city_name AS current_city,
       src2.county_name AS source_county,
       cur.name AS current_name,
       cur.county_name AS current_county
FROM src2
LEFT JOIN cur ON cur.code = src2.code
WHERE cur.code IS NULL
   OR COALESCE(cur.name, '') <> COALESCE(src2.county_name, '')
   OR COALESCE(cur.county_name, '') <> COALESCE(src2.county_name, '')
   OR COALESCE(cur.city_name, '') <> COALESCE(src2.city_name, '')
ORDER BY src2.code;

-- 3. 2020 评估源乡镇未匹配到当前 2020 基准
WITH src AS (
    SELECT LEFT(TRIM(region_code), 9) AS code,
           MAX(TRIM(county)) AS county_name,
           MAX(TRIM(township)) AS township_name
    FROM survey_data
    WHERE is_deleted = 0
      AND year = 2020
      AND CHAR_LENGTH(TRIM(region_code)) >= 9
    GROUP BY LEFT(TRIM(region_code), 9)
),
cur AS (
    SELECT code, name, county_name
    FROM grassroots_organization
    WHERE year = 2020
      AND is_baseline = 1
      AND is_deleted = 0
      AND level = 4
)
SELECT src.county_name,
       COUNT(*) AS unmatched_townships,
       GROUP_CONCAT(CONCAT(src.code, ':', src.township_name)
                    ORDER BY src.code SEPARATOR '; ') AS samples
FROM src
LEFT JOIN cur ON cur.code = src.code
WHERE cur.code IS NULL
GROUP BY src.county_name
ORDER BY unmatched_townships DESC, src.county_name;

-- 4. 2020 评估源村社未匹配到当前 2020 基准
WITH src AS (
    SELECT TRIM(region_code) AS code,
           MAX(TRIM(county_name)) AS county_name,
           MAX(TRIM(township_name)) AS township_name,
           MAX(TRIM(community_name)) AS community_name
    FROM community_disaster_reduction_capacity
    WHERE year = 2020
      AND CHAR_LENGTH(TRIM(region_code)) >= 12
    GROUP BY TRIM(region_code)
),
cur AS (
    SELECT code, name, county_name, township_name
    FROM grassroots_organization
    WHERE year = 2020
      AND is_baseline = 1
      AND is_deleted = 0
      AND level = 5
)
SELECT src.county_name,
       COUNT(*) AS unmatched_communities,
       COUNT(DISTINCT LEFT(src.code, 9)) AS affected_townships,
       MIN(src.code) AS sample_code,
       MIN(src.township_name) AS sample_township,
       MIN(src.community_name) AS sample_community
FROM src
LEFT JOIN cur ON cur.code = src.code
WHERE cur.code IS NULL
GROUP BY src.county_name
ORDER BY unmatched_communities DESC, src.county_name;

-- 5. 当前 2020 基准乡镇没有任何村社下级
WITH town AS (
    SELECT g.id, g.code, g.name, g.county_name
    FROM grassroots_organization g
    WHERE g.year = 2020
      AND g.is_baseline = 1
      AND g.is_deleted = 0
      AND g.level = 4
),
child AS (
    SELECT LEFT(code, 9) AS parent_code, COUNT(*) AS cnt
    FROM grassroots_organization
    WHERE year = 2020
      AND is_baseline = 1
      AND is_deleted = 0
      AND level = 5
    GROUP BY LEFT(code, 9)
)
SELECT town.county_name,
       COUNT(*) AS no_child_townships,
       GROUP_CONCAT(CONCAT(town.code, ':', town.name)
                    ORDER BY town.code SEPARATOR '; ') AS samples
FROM town
LEFT JOIN child ON child.parent_code = town.code
WHERE COALESCE(child.cnt, 0) = 0
GROUP BY town.county_name
ORDER BY no_child_townships DESC, town.county_name;

-- 6. 当前 2020 基准基层组织 county_id 指向的县名与自身 county_name 不一致
SELECT g.county_name AS grassroots_county_name,
       o.name AS organization_county_name,
       o.code AS organization_code,
       COUNT(*) AS rows_count,
       MIN(g.code) AS sample_code,
       MIN(g.name) AS sample_name
FROM grassroots_organization g
JOIN organization o ON o.id = g.county_id
WHERE g.year = 2020
  AND g.is_baseline = 1
  AND g.is_deleted = 0
  AND COALESCE(g.county_name, '') <> COALESCE(o.name, '')
GROUP BY g.county_name, o.name, o.code
ORDER BY rows_count DESC, o.code;

-- 7. 疑似“旌阳区”污染：城市不是德阳，但 2020 评估源 county 写成旌阳区。
SELECT 'survey_data' AS source_table,
       LEFT(TRIM(region_code), 6) AS county_code,
       MAX(TRIM(city)) AS city_name,
       MAX(TRIM(county)) AS county_name,
       COUNT(*) AS rows_count,
       MIN(TRIM(region_code)) AS sample_region_code
FROM survey_data
WHERE is_deleted = 0
  AND year = 2020
  AND TRIM(county) = '旌阳区'
  AND TRIM(city) <> '德阳市'
GROUP BY LEFT(TRIM(region_code), 6)
UNION ALL
SELECT 'community_disaster_reduction_capacity' AS source_table,
       LEFT(TRIM(region_code), 6) AS county_code,
       MAX(TRIM(city_name)) AS city_name,
       MAX(TRIM(county_name)) AS county_name,
       COUNT(*) AS rows_count,
       MIN(TRIM(region_code)) AS sample_region_code
FROM community_disaster_reduction_capacity
WHERE year = 2020
  AND TRIM(county_name) = '旌阳区'
  AND TRIM(city_name) <> '德阳市'
GROUP BY LEFT(TRIM(region_code), 6)
ORDER BY county_code, source_table;
