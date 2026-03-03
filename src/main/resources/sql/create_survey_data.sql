-- ============================================================
-- 调查数据表 (乡镇评估版本)
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `survey_data`;
CREATE TABLE `survey_data` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `unique_id` VARCHAR(50) DEFAULT '' COMMENT '唯一码',
    `region_code` VARCHAR(20) NOT NULL COMMENT '区代码',
    `verification_status` VARCHAR(20) DEFAULT '' COMMENT '核实状态',

    `province` VARCHAR(50) DEFAULT '' COMMENT '省名称',
    `city` VARCHAR(50) DEFAULT '' COMMENT '市名称',
    `county` VARCHAR(50) DEFAULT '' COMMENT '县名称',
    `township` VARCHAR(100) DEFAULT '' COMMENT '乡镇名称',
    `township_address` VARCHAR(200) DEFAULT '' COMMENT '乡镇（街道）地址',

    `year` INT NOT NULL COMMENT '数据所属年份',

    `population` BIGINT DEFAULT 0 COMMENT '常住人口数量',
    `total_households` INT DEFAULT 0 COMMENT '年末总户数(户)',

    `main_disaster_types` VARCHAR(200) DEFAULT '' COMMENT '影响乡镇（街道）的主要灾害类型',
    `disaster_types_other` VARCHAR(200) DEFAULT '' COMMENT '影响乡镇（街道）的主要灾害类型-其他项说明',

    `management_staff` INT DEFAULT 0 COMMENT '本级灾害管理工作人员总数',
    `disaster_info_staff` INT DEFAULT 0 COMMENT '本级灾害信息员人数',

    `risk_assessment` VARCHAR(10) DEFAULT '' COMMENT '是否开展乡镇（街道）灾害风险评估',
    `has_disaster_map` VARCHAR(10) DEFAULT '' COMMENT '是否有乡镇（街道）灾害类地图',

    `warning_receive_method` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息接收方式',
    `warning_receive_method_other` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息接收方式-其他项说明',

    `warning_communication_method` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息传达方式',
    `warning_communication_method_other` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息传达方式-其他项说明',

    `disaster_report_method` VARCHAR(200) DEFAULT '' COMMENT '灾情信息上报方式',
    `disaster_report_method_other` VARCHAR(200) DEFAULT '' COMMENT '灾情信息上报方式-其他项说明',

    `emergency_plan_count` INT DEFAULT 0 COMMENT '近3年编制或修订自然灾害应急预案数量(个)',
    `emergency_response_count` INT DEFAULT 0 COMMENT '近3年针对自然灾害启动应急响应次数(次)',

    `training_drill_count` INT DEFAULT 0 COMMENT '上一年度组织的应急管理培训和演练次数(次)',
    `training_participants` INT DEFAULT 0 COMMENT '上一年度组织的应急管理培训和演练参与人次',

    `volunteers` INT DEFAULT 0 COMMENT '志愿者数量',
    `firefighters` INT DEFAULT 0 COMMENT '消防员数量',
    `militia_reserve` INT DEFAULT 0 COMMENT '民兵预备役数量',

    `funding_support_method` VARCHAR(200) DEFAULT '' COMMENT '乡镇（街道）综合减灾工作经费保障方式',
    `funding_support_method_other` VARCHAR(200) DEFAULT '' COMMENT '乡镇（街道）综合减灾工作经费保障方式-其他说明',
    `funding_amount` DOUBLE DEFAULT 0 COMMENT '上一年度防灾减灾救灾资金投入总金额(万元)',

    `material_storage_method` VARCHAR(200) DEFAULT '' COMMENT '救灾物资储备方式',
    `material_storage_method_other` VARCHAR(200) DEFAULT '' COMMENT '救灾物资储备方式-其他项说明',

    `storage_point_count` INT DEFAULT 0 COMMENT '本级救灾物资、装备储备点数量(个)',
    `storage_equipment_count` INT DEFAULT 0 COMMENT '本级储备点救灾物资、装备数量(套/个/件)',

    `emergency_power_count` INT DEFAULT 0 COMMENT '其中：应急电源或应急发电设备数量(套或件)',
    `emergency_communication_count` INT DEFAULT 0 COMMENT '应急通信设备数量(套或件)',
    `emergency_water_count` INT DEFAULT 0 COMMENT '应急供水设备数量(套或件)',

    `hospital_beds` INT DEFAULT 0 COMMENT '医院床位数',
    `emergency_medical_count` INT DEFAULT 0 COMMENT '应急医疗设备数量(套或件)',

    `material_value` DOUBLE DEFAULT 0 COMMENT '现有储备物资、装备折合金额(万元)',

    `shelter_count` INT DEFAULT 0 COMMENT '本级灾害应急避难场所数量(个或处)',
    `shelter_capacity` INT DEFAULT 0 COMMENT '本级灾害应急避难场所容量',

    `unit_leader` VARCHAR(50) DEFAULT '' COMMENT '单位负责人',
    `statistics_leader` VARCHAR(50) DEFAULT '' COMMENT '统计负责人',
    `form_filler` VARCHAR(50) DEFAULT '' COMMENT '填表人',
    `contact_phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    `report_date` DATE DEFAULT NULL COMMENT '报出日期(年/月/日)',
    `fill_instructions` TEXT COMMENT '填写说明',

    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除，1-已删除)',

    INDEX `idx_region_code` (`region_code`),
    INDEX `idx_year` (`year`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调查数据表';

SET FOREIGN_KEY_CHECKS = 1;
