-- =====================================================
-- 检查您选择的区域是否有数据
-- 请将下面的区域代码替换为您实际选择的区域代码！
-- =====================================================

-- ⚠️ 重要：请修改这里的区域代码列表！
-- 将逗号分隔的区域代码列表替换为您在前端选择的区域
SET @selected_regions = '511425108,511425109,511425110';  -- 👈 修改这里！

-- 1. 显示您选择的区域
SELECT '=== 您选择的区域 ===' AS info;
SELECT @selected_regions AS 您选择的区域代码;

-- 2. 检查模型3（乡镇评估）是否有这些区域的数据
SELECT '=== 模型3（乡镇评估）数据检查 ===' AS info;

SELECT
    region_code AS 区域代码,
    region_name AS 区域名称,
    management_capability_score AS 灾害管理能力值,
    support_capability_score AS 灾害备灾能力值,
    self_rescue_capability_score AS 自救转移能力值,
    create_time AS 评估时间
FROM evaluation_result
WHERE evaluation_model_id = 3
  AND FIND_IN_SET(region_code, @selected_regions) > 0
ORDER BY create_time DESC;

-- 统计
SELECT
    COUNT(*) AS 记录数,
    COUNT(DISTINCT region_code) AS 区域数
FROM evaluation_result
WHERE evaluation_model_id = 3
  AND FIND_IN_SET(region_code, @selected_regions) > 0;

-- 3. 检查模型8（社区-乡镇评估）是否有这些区域的数据
SELECT '=== 模型8（社区-乡镇评估）数据检查 ===' AS info;

SELECT
    region_code AS 区域代码,
    region_name AS 区域名称,
    management_capability_score AS 灾害管理能力值,
    support_capability_score AS 灾害备灾能力值,
    self_rescue_capability_score AS 自救转移能力值,
    create_time AS 评估时间
FROM evaluation_result
WHERE evaluation_model_id = 8
  AND FIND_IN_SET(region_code, @selected_regions) > 0
ORDER BY create_time DESC;

-- 统计
SELECT
    COUNT(*) AS 记录数,
    COUNT(DISTINCT region_code) AS 区域数
FROM evaluation_result
WHERE evaluation_model_id = 8
  AND FIND_IN_SET(region_code, @selected_regions) > 0;

-- 4. 对比分析：哪些区域缺失数据
SELECT '=== 数据完整性分析 ===' AS info;

WITH selected_regions AS (
    SELECT
        SUBSTRING_INDEX(SUBSTRING_INDEX(@selected_regions, ',', numbers.n), ',', -1) AS region_code
    FROM (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers
    WHERE CHAR_LENGTH(@selected_regions) - CHAR_LENGTH(REPLACE(@selected_regions, ',', '')) >= numbers.n - 1
)
SELECT
    sr.region_code AS 区域代码,
    CASE WHEN er3.region_code IS NOT NULL THEN '✓ 有数据' ELSE '✗ 无数据' END AS 模型3状态,
    CASE WHEN er8.region_code IS NOT NULL THEN '✓ 有数据' ELSE '✗ 无数据' END AS 模型8状态,
    CASE
        WHEN er3.region_code IS NULL THEN '❌ 需要先运行乡镇评估模型'
        WHEN er8.region_code IS NULL THEN '❌ 需要先运行社区-乡镇评估模型'
        ELSE '✅ 数据完整，可以运行综合减灾模型'
    END AS 建议
FROM selected_regions sr
LEFT JOIN (
    SELECT DISTINCT region_code FROM evaluation_result WHERE evaluation_model_id = 3
) er3 ON sr.region_code = er3.region_code
LEFT JOIN (
    SELECT DISTINCT region_code FROM evaluation_result WHERE evaluation_model_id = 8
) er8 ON sr.region_code = er8.region_code;

-- 5. 如果数据存在，显示最新的6个指标值
SELECT '=== 最新的6个指标值（用于综合减灾模型） ===' AS info;

SELECT
    COALESCE(t3.region_code, t8.region_code) AS 区域代码,
    COALESCE(t3.region_name, t8.region_name) AS 区域名称,
    t3.management_capability_score AS 乡镇_灾害管理,
    t3.support_capability_score AS 乡镇_灾害备灾,
    t3.self_rescue_capability_score AS 乡镇_自救转移,
    t8.management_capability_score AS 社区_灾害管理,
    t8.support_capability_score AS 社区_灾害备灾,
    t8.self_rescue_capability_score AS 社区_自救转移
FROM (
    SELECT region_code, region_name,
           management_capability_score,
           support_capability_score,
           self_rescue_capability_score,
           ROW_NUMBER() OVER (PARTITION BY region_code ORDER BY create_time DESC) AS rn
    FROM evaluation_result
    WHERE evaluation_model_id = 3
      AND FIND_IN_SET(region_code, @selected_regions) > 0
) t3
LEFT JOIN (
    SELECT region_code, region_name,
           management_capability_score,
           support_capability_score,
           self_rescue_capability_score,
           ROW_NUMBER() OVER (PARTITION BY region_code ORDER BY create_time DESC) AS rn
    FROM evaluation_result
    WHERE evaluation_model_id = 8
      AND FIND_IN_SET(region_code, @selected_regions) > 0
) t8 ON t3.region_code = t8.region_code AND t8.rn = 1
WHERE t3.rn = 1;
