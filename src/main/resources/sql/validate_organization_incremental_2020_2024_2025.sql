-- ============================================================
-- 组织机构年度增量规范化校验脚本
-- 用途：
-- 1. 执行规范化前，评估当前 organization / grassroots_organization 的冗余情况。
-- 2. 执行规范化后，确认 2020 基准和 2024/2025 增量是否符合预期。
-- 3. 检查评估数据是否能通过 year + region_code 找到有效组织机构。
--
-- 搭配执行脚本：
--   src/main/resources/sql/normalize_organization_incremental_2020_2024_2025.sql
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. 当前组织表按年份/基准标记统计
-- ============================================================

SELECT
    'organization' AS table_name,
    year,
    is_baseline,
    is_deleted,
    COUNT(*) AS row_count
FROM organization
GROUP BY year, is_baseline, is_deleted
ORDER BY year, is_baseline, is_deleted;

SELECT
    'grassroots_organization' AS table_name,
    year,
    is_baseline,
    is_deleted,
    COUNT(*) AS row_count
FROM grassroots_organization
GROUP BY year, is_baseline, is_deleted
ORDER BY year, is_baseline, is_deleted;

-- ============================================================
-- 2. 唯一性与异常编码检查
-- ============================================================

SELECT
    'organization_duplicate_code_year' AS check_name,
    code,
    year,
    COUNT(*) AS duplicate_count
FROM organization
GROUP BY code, year
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, year, code
LIMIT 100;

SELECT
    'grassroots_duplicate_code_year' AS check_name,
    code,
    year,
    COUNT(*) AS duplicate_count
FROM grassroots_organization
GROUP BY code, year
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, year, code
LIMIT 100;

SELECT
    'organization_invalid_code_length' AS check_name,
    id,
    code,
    name,
    level,
    year
FROM organization
WHERE (level = 1 AND CHAR_LENGTH(code) <> 2)
   OR (level = 2 AND CHAR_LENGTH(code) <> 4)
   OR (level = 3 AND CHAR_LENGTH(code) <> 6)
ORDER BY year, level, code
LIMIT 100;

SELECT
    'grassroots_invalid_code_length' AS check_name,
    id,
    code,
    name,
    level,
    year
FROM grassroots_organization
WHERE (level = 4 AND CHAR_LENGTH(code) <> 9)
   OR (level = 5 AND CHAR_LENGTH(code) < 12)
ORDER BY year, level, code
LIMIT 100;

-- ============================================================
-- 3. 评估源数据按年份/编码统计
-- ============================================================

SELECT
    'survey_data' AS source_table,
    year,
    COUNT(*) AS row_count,
    COUNT(DISTINCT region_code) AS distinct_region_count
FROM survey_data
WHERE is_deleted = 0
  AND year IN (2020, 2024, 2025)
GROUP BY year
ORDER BY year;

SELECT
    'community_disaster_reduction_capacity' AS source_table,
    year,
    COUNT(*) AS row_count,
    COUNT(DISTINCT region_code) AS distinct_region_count
FROM community_disaster_reduction_capacity
WHERE year IN (2020, 2024, 2025)
GROUP BY year
ORDER BY year;

SELECT
    'survey_duplicate_region_year' AS check_name,
    year,
    region_code,
    COUNT(*) AS duplicate_count
FROM survey_data
WHERE is_deleted = 0
  AND year IN (2020, 2024, 2025)
GROUP BY year, region_code
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, year, region_code
LIMIT 100;

SELECT
    'community_duplicate_region_year' AS check_name,
    year,
    region_code,
    COUNT(*) AS duplicate_count
FROM community_disaster_reduction_capacity
WHERE year IN (2020, 2024, 2025)
GROUP BY year, region_code
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, year, region_code
LIMIT 100;

-- 2025 年社区表如果 region_code 只有 9 位，说明该字段是乡镇编码；
-- 需要用“乡镇编码 + 社区名”回查已有社区编码，不能直接拿 region_code 当社区编码。
SELECT
    'community_2025_township_code_resolution_summary' AS check_name,
    COUNT(*) AS source_rows,
    COUNT(DISTINCT CONCAT(TRIM(cd.region_code), '|', TRIM(cd.community_name))) AS distinct_township_community_names,
    SUM(CASE WHEN resolved.match_count = 1 THEN 1 ELSE 0 END) AS uniquely_resolved_rows,
    SUM(CASE WHEN COALESCE(resolved.match_count, 0) = 0 THEN 1 ELSE 0 END) AS unresolved_rows,
    SUM(CASE WHEN resolved.match_count > 1 THEN 1 ELSE 0 END) AS ambiguous_rows
FROM community_disaster_reduction_capacity cd
LEFT JOIN (
    SELECT
        LEFT(code, 9) AS township_code,
        name AS community_name,
        COUNT(DISTINCT code) AS match_count
    FROM grassroots_organization
    WHERE level = 5
      AND is_deleted = 0
      AND (year IN (2020, 2024) OR is_baseline = 1)
    GROUP BY LEFT(code, 9), name
) resolved
  ON resolved.township_code = LEFT(TRIM(cd.region_code), 9)
 AND resolved.community_name = TRIM(cd.community_name)
WHERE cd.year = 2025
  AND CHAR_LENGTH(TRIM(cd.region_code)) = 9
  AND NULLIF(TRIM(cd.community_name), '') IS NOT NULL;

SELECT
    'community_2025_township_code_unresolved_sample' AS check_name,
    cd.year,
    cd.region_code AS township_code,
    cd.county_name,
    cd.township_name,
    cd.community_name,
    COALESCE(resolved.match_count, 0) AS existing_community_matches
FROM community_disaster_reduction_capacity cd
LEFT JOIN (
    SELECT
        LEFT(code, 9) AS township_code,
        name AS community_name,
        COUNT(DISTINCT code) AS match_count
    FROM grassroots_organization
    WHERE level = 5
      AND is_deleted = 0
      AND (year IN (2020, 2024) OR is_baseline = 1)
    GROUP BY LEFT(code, 9), name
) resolved
  ON resolved.township_code = LEFT(TRIM(cd.region_code), 9)
 AND resolved.community_name = TRIM(cd.community_name)
WHERE cd.year = 2025
  AND CHAR_LENGTH(TRIM(cd.region_code)) = 9
  AND NULLIF(TRIM(cd.community_name), '') IS NOT NULL
  AND COALESCE(resolved.match_count, 0) <> 1
ORDER BY cd.region_code, cd.community_name
LIMIT 100;

-- ============================================================
-- 4. 评估数据匹配组织机构检查
--    规则：优先匹配当年增量；不存在则回退到 2020 基准。
-- ============================================================

SELECT
    'survey_unmatched_township' AS check_name,
    sd.year,
    sd.region_code,
    sd.province,
    sd.city,
    sd.county,
    sd.township
FROM survey_data sd
LEFT JOIN grassroots_organization gy
       ON gy.code = LEFT(TRIM(sd.region_code), 9)
      AND gy.year = sd.year
      AND gy.is_deleted = 0
LEFT JOIN grassroots_organization gb
       ON gb.code = LEFT(TRIM(sd.region_code), 9)
      AND gb.year = 2020
      AND gb.is_baseline = 1
      AND gb.is_deleted = 0
WHERE sd.is_deleted = 0
  AND sd.year IN (2020, 2024, 2025)
  AND CHAR_LENGTH(TRIM(sd.region_code)) >= 9
  AND gy.id IS NULL
  AND gb.id IS NULL
ORDER BY sd.year, sd.region_code
LIMIT 200;

SELECT
    'community_unmatched_community' AS check_name,
    cd.year,
    cd.region_code,
    cd.province_name,
    cd.city_name,
    cd.county_name,
    cd.township_name,
    cd.community_name
FROM community_disaster_reduction_capacity cd
LEFT JOIN grassroots_organization gy
       ON gy.code = TRIM(cd.region_code)
      AND gy.year = cd.year
      AND gy.is_deleted = 0
LEFT JOIN grassroots_organization gb
       ON gb.code = TRIM(cd.region_code)
      AND gb.year = 2020
      AND gb.is_baseline = 1
      AND gb.is_deleted = 0
WHERE cd.year IN (2020, 2024, 2025)
  AND CHAR_LENGTH(TRIM(cd.region_code)) >= 12
  AND gy.id IS NULL
  AND gb.id IS NULL
ORDER BY cd.year, cd.region_code
LIMIT 200;

-- ============================================================
-- 5. 年度冗余检查
--    如果结果较多，说明 2024/2025 仍有大量“与 2020 基准同名同级”的记录。
--    这不是绝对错误，但通常代表可以继续压缩。
-- ============================================================

SELECT
    'organization_same_as_baseline' AS check_name,
    y.year,
    y.code,
    y.name,
    y.level
FROM organization y
JOIN organization b
  ON b.code = y.code
 AND b.year = 2020
 AND b.is_baseline = 1
WHERE y.year IN (2024, 2025)
  AND COALESCE(y.is_baseline, 0) = 0
  AND y.is_deleted = 0
  AND b.is_deleted = 0
  AND COALESCE(y.name, '') = COALESCE(b.name, '')
  AND COALESCE(y.level, 0) = COALESCE(b.level, 0)
  AND COALESCE(y.province_name, '') = COALESCE(b.province_name, '')
  AND COALESCE(y.city_name, '') = COALESCE(b.city_name, '')
  AND COALESCE(y.county_name, '') = COALESCE(b.county_name, '')
ORDER BY y.year, y.code
LIMIT 200;

SELECT
    'grassroots_same_as_baseline' AS check_name,
    y.year,
    y.code,
    y.name,
    y.level
FROM grassroots_organization y
JOIN grassroots_organization b
  ON b.code = y.code
 AND b.year = 2020
 AND b.is_baseline = 1
WHERE y.year IN (2024, 2025)
  AND COALESCE(y.is_baseline, 0) = 0
  AND y.is_deleted = 0
  AND b.is_deleted = 0
  AND COALESCE(y.name, '') = COALESCE(b.name, '')
  AND COALESCE(y.level, 0) = COALESCE(b.level, 0)
  AND COALESCE(y.province_name, '') = COALESCE(b.province_name, '')
  AND COALESCE(y.city_name, '') = COALESCE(b.city_name, '')
  AND COALESCE(y.county_name, '') = COALESCE(b.county_name, '')
  AND COALESCE(y.township_name, '') = COALESCE(b.township_name, '')
  AND COALESCE(y.community_name, '') = COALESCE(b.community_name, '')
ORDER BY y.year, y.code
LIMIT 200;

-- ============================================================
-- 6. 规范化脚本生成的疑似撤销候选
-- ============================================================

SELECT
    'delete_candidates_need_manual_review' AS check_name,
    COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'org_incremental_delete_candidate_2020_2025';

-- 规范化脚本执行后，如上方 row_count = 1，可单独执行：
-- SELECT *
-- FROM org_incremental_delete_candidate_2020_2025
-- ORDER BY target_year, org_table, level, code
-- LIMIT 200;

SELECT
    'community_code_resolution_issue_2025_table_exists' AS check_name,
    COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'org_community_code_resolution_issue_2025';

-- 规范化脚本执行后，如上方 row_count = 1，可单独执行：
-- SELECT match_status, COUNT(*)
-- FROM org_community_code_resolution_issue_2025
-- GROUP BY match_status;
