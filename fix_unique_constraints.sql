-- 修复唯一约束脚本
-- 删除旧的约束并添加包含年份的新约束

USE evaluate_db;

-- 1. 删除社区数据表的旧约束（只基于 region_code 和 community_name）
ALTER TABLE community_disaster_reduction_capacity
DROP INDEX uk_region_community;

-- 2. 添加社区数据表的新约束（基于 region_code, community_name, year）
ALTER TABLE community_disaster_reduction_capacity
ADD CONSTRAINT uk_community_region_community_year
UNIQUE (region_code, community_name, year);

-- 3. 检查 survey_data 表是否有类似问题
-- 如果没有包含年份的唯一约束，也需要修复
ALTER TABLE survey_data
DROP INDEX uk_survey_region_year;

-- 4. 添加 survey_data 表的正确约束
ALTER TABLE survey_data
ADD CONSTRAINT uk_survey_region_year
UNIQUE (region_code, year);

-- 5. 验证约束是否正确添加
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    information_schema.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'evaluate_db'
    AND TABLE_NAME IN ('survey_data', 'community_disaster_reduction_capacity')
    AND CONSTRAINT_NAME LIKE 'uk_%'
ORDER BY
    TABLE_NAME, CONSTRAINT_NAME;

SELECT '唯一约束修复完成' AS status;
SELECT '现在可以导入不同年份的数据了' AS message;