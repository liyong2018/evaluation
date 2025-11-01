-- 为survey_data表添加year字段
ALTER TABLE `survey_data` ADD COLUMN `year` INT NOT NULL COMMENT '数据所属年份' AFTER `township`;

-- 为community_disaster_reduction_capacity表添加year字段
ALTER TABLE `community_disaster_reduction_capacity` ADD COLUMN `year` INT NOT NULL COMMENT '数据所属年份' AFTER `community_name`;

-- 为两个表添加年份索引，方便按年份查询
CREATE INDEX `idx_year` ON `survey_data` (`year`);
CREATE INDEX `idx_year` ON `community_disaster_reduction_capacity` (`year`);
