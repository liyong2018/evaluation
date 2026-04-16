-- ============================================================
-- 2020年家庭减灾能力数据表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `family_disaster_reduction_capacity_2020`;
CREATE TABLE `family_disaster_reduction_capacity_2020` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `region_code` VARCHAR(20) DEFAULT '' COMMENT '行政区代码',
    `province_name` VARCHAR(50) DEFAULT '' COMMENT '省名称',
    `city_name` VARCHAR(50) DEFAULT '' COMMENT '市名称',
    `county_name` VARCHAR(50) DEFAULT '' COMMENT '县名称',
    `town_name` VARCHAR(100) DEFAULT '' COMMENT '乡镇（街道）名称',
    `village_name` VARCHAR(100) DEFAULT '' COMMENT '社区（行政村）名称',
    `age_0_10_count` INT DEFAULT 0 COMMENT '0-10岁人数（人）',
    `age_65_plus_count` INT DEFAULT 0 COMMENT '65岁（含）以上人数（人）',
    `disabled_count` INT DEFAULT 0 COMMENT '残障人数（人）',
    `total_people` INT DEFAULT 0 COMMENT '家庭总人数（人）',
    `chronic_disease_count` INT DEFAULT 0 COMMENT '患有慢性病、需要长期服药的人数（人）',
    `emergency_supplies` TEXT COMMENT '您家里有以下哪些应急物品？',
    `water_reserve_days` VARCHAR(50) DEFAULT '' COMMENT '出现因灾断水的情况下，您家里的干净饮用水储量能支撑全家人多久？',
    `food_reserve_days` VARCHAR(50) DEFAULT '' COMMENT '出现因灾无法供给食物的情况下，您家里存储的方便食品能支撑全家人多久？',
    `in_community_group` VARCHAR(10) DEFAULT '' COMMENT '您家是否有人在社区（村）微信群或QQ群中?',
    `know_staff_contact` VARCHAR(10) DEFAULT '' COMMENT '您是否知道社区（村）或社区（村）工作人员联系方式？',
    `received_warning_types` TEXT COMMENT '您收到过哪些类型灾害的预警信息？',
    `know_evacuation_route` VARCHAR(20) DEFAULT '' COMMENT '您的家庭是否了解紧急避难路线？',
    `drill_participation_count` VARCHAR(20) DEFAULT '' COMMENT '您近三年参加过几次社区（村）组织的应急演练？',
    `first_aid_training` VARCHAR(10) DEFAULT '' COMMENT '您是否参加过急救培训？',
    `mastered_first_aid_skills` TEXT COMMENT '您掌握下面哪些急救方法？',
    `weight` DECIMAL(10,4) DEFAULT 1.0000 COMMENT '每人或每户的权数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_region_code` (`region_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2020年家庭减灾能力数据表';

SET FOREIGN_KEY_CHECKS = 1;
