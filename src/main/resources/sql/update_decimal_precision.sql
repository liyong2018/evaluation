-- 更新资金投入和物资价值字段为4位小数精度
-- 执行时间: 2025-02-27

-- 1. 更新 survey_data 表 (乡镇数据)
ALTER TABLE `survey_data`
MODIFY COLUMN `funding_amount` DECIMAL(15,4) DEFAULT NULL COMMENT '上一年度防灾减灾救灾资金投入总金额(万元)';

ALTER TABLE `survey_data`
MODIFY COLUMN `material_value` DECIMAL(15,4) DEFAULT NULL COMMENT '现有储备物资、装备折合金额(万元)';

-- 2. 更新 community_disaster_reduction_capacity 表 (社区数据)
ALTER TABLE `community_disaster_reduction_capacity`
MODIFY COLUMN `last_year_funding_amount` DECIMAL(15,4) DEFAULT 0 COMMENT '上一年度防灾减灾救灾资金投入总金额（万元）';

ALTER TABLE `community_disaster_reduction_capacity`
MODIFY COLUMN `materials_equipment_value` DECIMAL(15,4) DEFAULT 0 COMMENT '现有储备物资、装备折合金额（万元）';
