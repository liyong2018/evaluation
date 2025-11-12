-- 安全修复唯一约束脚本
-- 包含错误处理和检查

USE evaluate_db;

-- 1. 首先删除社区数据表的旧约束（忽略错误）
SET @sql = 'ALTER TABLE community_disaster_reduction_capacity DROP INDEX uk_region_community';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加社区数据表的新约束
ALTER TABLE community_disaster_reduction_capacity
ADD CONSTRAINT uk_community_region_community_year
UNIQUE (region_code, community_name, year);

-- 3. 检查 survey_data 表的约束
-- 先尝试删除可能存在的旧约束
SET @sql = 'ALTER TABLE survey_data DROP INDEX uk_survey_region_year';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 添加 survey_data 表的正确约束
ALTER TABLE survey_data
ADD CONSTRAINT uk_survey_region_year
UNIQUE (region_code, year);

-- 5. 验证约束是否正确添加
SELECT
    '约束验证结果' as operation,
    TABLE_NAME,
    CONSTRAINT_NAME,
    COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'evaluate_db'
  AND TABLE_NAME IN ('survey_data', 'community_disaster_reduction_capacity')
  AND CONSTRAINT_NAME LIKE 'uk_%'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;