-- 最终约束修复脚本
-- 只修复社区数据表的约束问题

USE evaluate_db;

-- 1. 删除社区数据表的旧约束（基于 region_code 和 community_name）
ALTER TABLE community_disaster_reduction_capacity
DROP INDEX uk_region_community;

-- 2. 添加社区数据表的新约束（基于 region_code, community_name, year）
ALTER TABLE community_disaster_reduction_capacity
ADD CONSTRAINT uk_community_region_community_year
UNIQUE (region_code, community_name, year);

-- 3. 添加 survey_data 表的约束（如果不存在）
ALTER TABLE survey_data
ADD CONSTRAINT uk_survey_region_year
UNIQUE (region_code, year);

-- 4. 验证约束
SELECT '约束修复完成' AS message;
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) as columns
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'evaluate_db'
  AND TABLE_NAME IN ('survey_data', 'community_disaster_reduction_capacity')
  AND CONSTRAINT_NAME LIKE 'uk_%'
GROUP BY TABLE_NAME, CONSTRAINT_NAME;