-- ============================================================
-- 2020 系统组织机构 XLS 暂存与修复候选查看脚本
--
-- 前置：
-- 运行 OrganizationImportServiceTest.testStage2020SystemXlsAndGenerateRepairCandidates
-- 生成：
--   org_2020_system_xls_staging
--   org_2020_repair_candidate
--
-- 该脚本只读，不修改正式数据。
-- ============================================================

SET NAMES utf8mb4;

SELECT 'system_xls_staging' AS metric,
       COUNT(*) AS community_count,
       COUNT(DISTINCT township_code) AS township_count,
       COUNT(DISTINCT county_code) AS county_count,
       COUNT(DISTINCT city_code) AS city_count
FROM org_2020_system_xls_staging;

SELECT issue_type,
       COUNT(*) AS candidate_count
FROM org_2020_repair_candidate
GROUP BY issue_type
ORDER BY candidate_count DESC, issue_type;

SELECT issue_type,
       COALESCE(source_county, current_county, '') AS county_name,
       COUNT(*) AS cnt,
       MIN(COALESCE(source_code, display_code)) AS sample_code,
       MIN(COALESCE(source_name, current_name)) AS sample_name
FROM org_2020_repair_candidate
GROUP BY issue_type, COALESCE(source_county, current_county, '')
ORDER BY issue_type, cnt DESC, county_name
LIMIT 300;

SELECT *
FROM org_2020_repair_candidate
WHERE issue_type IN (
    'MISSING_COMMUNITY_FROM_XLS',
    'MISSING_TOWNSHIP_FROM_XLS',
    'CURRENT_TOWNSHIP_NO_CHILD'
)
ORDER BY issue_type, COALESCE(source_code, display_code)
LIMIT 300;

SELECT *
FROM org_2020_repair_candidate
WHERE issue_type = 'FUNCTIONAL_CODE_MAPPING_PRESENT'
ORDER BY source_code, display_code
LIMIT 300;

SELECT *
FROM org_2020_repair_candidate
WHERE issue_type = 'SOURCE_COUNTY_NAME_POLLUTION'
ORDER BY source_code, detail;
