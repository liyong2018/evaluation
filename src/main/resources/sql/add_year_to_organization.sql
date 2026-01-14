-- 为 organization 表添加 year 字段
-- 执行日期: 2026-01-14

-- 添加 year 字段
ALTER TABLE `organization` ADD COLUMN `year` INT NULL COMMENT '数据所属年份' AFTER `level`;

-- 为现有数据设置默认年份（可选，根据需要修改）
-- UPDATE `organization` SET `year` = 2022 WHERE `year` IS NULL;

-- 添加索引以提升查询性能
ALTER TABLE `organization` ADD INDEX `idx_year` (`year`);
ALTER TABLE `organization` ADD INDEX `idx_year_level` (`year`, `level`);
ALTER TABLE `organization` ADD INDEX `idx_year_code` (`year`, `code`);

-- 添加联合唯一索引，确保同一年份同一个组织机构编码只有一条记录
-- 注意：如果现有数据有重复，需要先清理数据再执行此语句
-- ALTER TABLE `organization` ADD UNIQUE INDEX `uk_year_code` (`year`, `code`);
