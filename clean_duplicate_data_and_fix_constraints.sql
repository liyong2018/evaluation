-- 清理重复数据并修复约束脚本

USE evaluate_db;

-- 1. 首先查看重复数据情况
SELECT 'survey_data 表中的重复数据' as table_info;
SELECT
    region_code,
    year,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id ORDER BY id) as duplicate_ids,
    MIN(create_time) as earliest_time,
    MAX(create_time) as latest_time
FROM survey_data
GROUP BY region_code, year
HAVING COUNT(*) > 1
ORDER BY region_code, year;

SELECT 'community_disaster_reduction_capacity 表中的重复数据' as table_info;
SELECT
    region_code,
    community_name,
    year,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id ORDER BY id) as duplicate_ids,
    MIN(create_time) as earliest_time,
    MAX(create_time) as latest_time
FROM community_disaster_reduction_capacity
GROUP BY region_code, community_name, year
HAVING COUNT(*) > 1
ORDER BY region_code, community_name, year;

-- 2. 清理 survey_data 表中的重复数据（保留最早创建的记录）
DELETE s1 FROM survey_data s1
INNER JOIN survey_data s2 ON (
    s1.region_code = s2.region_code
    AND s1.year = s2.year
    AND s1.id > s2.id
);

-- 3. 清理 community_disaster_reduction_capacity 表中的重复数据（保留最早创建的记录）
DELETE c1 FROM community_disaster_reduction_capacity c1
INNER JOIN community_disaster_reduction_capacity c2 ON (
    c1.region_code = c2.region_code
    AND c1.community_name = c2.community_name
    AND c1.year = c2.year
    AND c1.id > c2.id
);

-- 4. 现在可以安全地添加唯一约束
ALTER TABLE survey_data
ADD CONSTRAINT uk_survey_region_year
UNIQUE (region_code, year);

-- 确保 community_disaster_reduction_capacity 表有正确的约束
-- 如果约束已存在，这个命令可能会失败，但没关系
ALTER TABLE community_disaster_reduction_capacity
ADD CONSTRAINT uk_community_region_community_year
UNIQUE (region_code, community_name, year);

-- 5. 验证约束是否正确添加
SELECT '约束修复完成，验证结果：' as verification;
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) as columns
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'evaluate_db'
  AND TABLE_NAME IN ('survey_data', 'community_disaster_reduction_capacity')
  AND CONSTRAINT_NAME LIKE 'uk_%'
GROUP BY TABLE_NAME, CONSTRAINT_NAME;