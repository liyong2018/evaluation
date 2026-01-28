-- 为 survey_data 表添加索引以提升查询性能
-- 添加 (year, region_code) 复合索引，加速按年份和区划代码的查询

-- 检查索引是否存在，如果不存在则创建
-- 复合索引：year + region_code (支持 eq + likeRight 查询)
CREATE INDEX IF NOT EXISTS idx_survey_data_year_region
ON survey_data(year, region_code);

-- 单独为 region_code 创建索引（如果没有的话）
CREATE INDEX IF NOT EXISTS idx_survey_data_region_code
ON survey_data(region_code);

-- 为常用查询字段创建索引
CREATE INDEX IF NOT EXISTS idx_survey_data_township
ON survey_data(township);

-- 分析表以更新统计信息
ANALYZE TABLE survey_data;
