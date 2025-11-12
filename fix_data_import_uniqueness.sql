-- 修复数据导入时覆盖问题的数据库约束脚本
-- 添加复合唯一约束，确保不同年份的数据不会被意外覆盖

USE evaluate_db;

-- 1. 为 survey_data 表添加复合唯一约束
-- 防止同一地区代码在不同年份的数据冲突
ALTER TABLE survey_data
ADD CONSTRAINT uk_survey_region_year UNIQUE (region_code, year);

-- 2. 为 community_disaster_reduction_capacity 表添加复合唯一约束
-- 防止同一地区代码和社区名称在不同年份的数据冲突
ALTER TABLE community_disaster_reduction_capacity
ADD CONSTRAINT uk_community_region_community_year UNIQUE (region_code, community_name, year);

-- 3. 检查并清理可能存在的重复数据
-- 显示乡镇数据中的重复记录（相同地区代码和年份，不同ID）
SELECT
    region_code,
    year,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id ORDER BY id) as duplicate_ids
FROM survey_data
GROUP BY region_code, year
HAVING COUNT(*) > 1;

-- 显示社区数据中的重复记录（相同地区代码、社区名称和年份，不同ID）
SELECT
    region_code,
    community_name,
    year,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id ORDER BY id) as duplicate_ids
FROM community_disaster_reduction_capacity
GROUP BY region_code, community_name, year
HAVING COUNT(*) > 1;

-- 4. 提供清理重复数据的建议语句（请在执行前仔细检查！）
-- 注意：以下语句会删除重复记录，只保留最早创建的记录

-- 清理 survey_data 中的重复数据（保留最早创建的记录）
-- DELETE s1 FROM survey_data s1
-- INNER JOIN survey_data s2
-- WHERE s1.region_code = s2.region_code
--   AND s1.year = s2.year
--   AND s1.id > s2.id;

-- 清理 community_disaster_reduction_capacity 中的重复数据（保留最早创建的记录）
-- DELETE c1 FROM community_disaster_reduction_capacity c1
-- INNER JOIN community_disaster_reduction_capacity c2
-- WHERE c1.region_code = c2.region_code
--   AND c1.community_name = c2.community_name
--   AND c1.year = c2.year
--   AND c1.id > c2.id;

SELECT '数据库约束修复脚本已准备就绪' AS status;
SELECT '请在检查重复数据后，根据需要执行清理语句' AS reminder;