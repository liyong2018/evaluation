-- ============================================================
-- 评估模型相关表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 评估模型表
DROP TABLE IF EXISTS `evaluation_model`;
CREATE TABLE `evaluation_model` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_code` VARCHAR(50) NOT NULL COMMENT '模型编码',
    `description` VARCHAR(500) DEFAULT '' COMMENT '模型描述',
    `version` VARCHAR(20) DEFAULT '' COMMENT '模型版本',
    `status` INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认模型',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT '' COMMENT '更新人',
    INDEX `idx_model_code` (`model_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估模型表';

-- 模型步骤表
DROP TABLE IF EXISTS `model_step`;
CREATE TABLE `model_step` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `step_name` VARCHAR(100) NOT NULL COMMENT '步骤名称',
    `step_code` VARCHAR(50) NOT NULL COMMENT '步骤编码',
    `step_order` INT DEFAULT 0 COMMENT '执行顺序',
    `step_type` VARCHAR(50) DEFAULT '' COMMENT '步骤类型(CALCULATION/NORMALIZATION/WEIGHTING/TOPSIS/GRADING)',
    `description` VARCHAR(500) DEFAULT '' COMMENT '步骤描述',
    `input_variables` TEXT COMMENT '输入变量(JSON格式)',
    `output_variables` TEXT COMMENT '输出变量(JSON格式)',
    `depends_on` VARCHAR(200) DEFAULT '' COMMENT '依赖步骤ID(逗号分隔)',
    `status` INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_step_order` (`step_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型步骤表';

-- 步骤算法表
DROP TABLE IF EXISTS `step_algorithm`;
CREATE TABLE `step_algorithm` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `step_id` BIGINT NOT NULL COMMENT '步骤ID',
    `algorithm_name` VARCHAR(100) NOT NULL COMMENT '算法名称',
    `algorithm_code` VARCHAR(50) NOT NULL COMMENT '算法编码',
    `algorithm_order` INT DEFAULT 0 COMMENT '算法执行顺序',
    `ql_expression` TEXT COMMENT 'QLExpress表达式',
    `input_params` TEXT COMMENT '输入参数定义(JSON格式)',
    `output_param` VARCHAR(100) DEFAULT '' COMMENT '输出参数名',
    `description` VARCHAR(500) DEFAULT '' COMMENT '算法描述',
    `status` INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_step_id` (`step_id`),
    INDEX `idx_algorithm_order` (`algorithm_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='步骤算法表';

-- 评估结果表
DROP TABLE IF EXISTS `evaluation_result`;
CREATE TABLE `evaluation_result` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `region_code` VARCHAR(20) DEFAULT '' COMMENT '区代码',
    `region_name` VARCHAR(100) DEFAULT '' COMMENT '地区名称',
    `management_capability_score` DECIMAL(10,4) DEFAULT 0 COMMENT '管理能力得分',
    `support_capability_score` DECIMAL(10,4) DEFAULT 0 COMMENT '支持能力得分',
    `self_rescue_capability_score` DECIMAL(10,4) DEFAULT 0 COMMENT '自救能力得分',
    `comprehensive_capability_score` DECIMAL(10,4) DEFAULT 0 COMMENT '综合能力得分',
    `management_capability_level` VARCHAR(20) DEFAULT '' COMMENT '管理能力等级',
    `support_capability_level` VARCHAR(20) DEFAULT '' COMMENT '支持能力等级',
    `self_rescue_capability_level` VARCHAR(20) DEFAULT '' COMMENT '自救能力等级',
    `comprehensive_capability_level` VARCHAR(20) DEFAULT '' COMMENT '综合能力等级',
    `evaluation_model_id` BIGINT DEFAULT NULL COMMENT '评估模型ID',
    `data_source` VARCHAR(50) DEFAULT '' COMMENT '数据来源',
    `execution_record_id` BIGINT DEFAULT NULL COMMENT '执行记录ID',
    `org_code` VARCHAR(20) DEFAULT '' COMMENT '所属组织机构代码',
    `create_by` VARCHAR(50) DEFAULT '' COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT '' COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除，1-已删除)',
    INDEX `idx_region_code` (`region_code`),
    INDEX `idx_evaluation_model_id` (`evaluation_model_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估结果表';

-- 模型执行记录表
DROP TABLE IF EXISTS `model_execution_record`;
CREATE TABLE `model_execution_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_id` BIGINT DEFAULT NULL COMMENT '模型ID',
    `execution_code` VARCHAR(50) DEFAULT '' COMMENT '执行编码',
    `region_ids` VARCHAR(500) DEFAULT '' COMMENT '地区ID列表',
    `weight_config_id` BIGINT DEFAULT NULL COMMENT '权重配置ID',
    `execution_status` VARCHAR(20) DEFAULT '' COMMENT '执行状态',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `error_message` TEXT COMMENT '错误信息',
    `result_summary` VARCHAR(500) DEFAULT '' COMMENT '结果摘要',
    `result_detail` TEXT COMMENT '结果详情',
    `create_by` VARCHAR(50) DEFAULT '' COMMENT '创建人',
    `year` INT DEFAULT NULL COMMENT '年份',
    `org_code` VARCHAR(20) DEFAULT '' COMMENT '组织机构代码',
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_execution_code` (`execution_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型执行记录表';

-- 社区减灾能力表
DROP TABLE IF EXISTS `community_disaster_reduction_capacity`;
CREATE TABLE `community_disaster_reduction_capacity` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `region_code` VARCHAR(20) DEFAULT '' COMMENT '行政区代码',
    `province_name` VARCHAR(50) DEFAULT '' COMMENT '省名称',
    `city_name` VARCHAR(50) DEFAULT '' COMMENT '市名称',
    `county_name` VARCHAR(50) DEFAULT '' COMMENT '县名称',
    `township_name` VARCHAR(100) DEFAULT '' COMMENT '乡镇名称',
    `community_name` VARCHAR(100) DEFAULT '' COMMENT '社区（行政村）名称',
    `year` INT DEFAULT NULL COMMENT '数据所属年份',
    `has_emergency_plan` VARCHAR(10) DEFAULT '' COMMENT '是否有社区（行政村）应急预案（是/否）',
    `has_vulnerable_groups_list` VARCHAR(10) DEFAULT '' COMMENT '是否有本辖区弱势人群清单（是/否）',
    `has_disaster_points_list` VARCHAR(10) DEFAULT '' COMMENT '是否有本辖区地质灾害等隐患点清单（是/否）',
    `has_disaster_map` VARCHAR(10) DEFAULT '' COMMENT '是否有社区（行政村）灾害类地图（是/否）',
    `resident_population` INT DEFAULT 0 COMMENT '常住人口数量（人）',
    `last_year_funding_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '上一年度防灾减灾救灾资金投入总金额（万元）',
    `materials_equipment_value` DECIMAL(10,2) DEFAULT 0 COMMENT '现有储备物资、装备折合金额（万元）',
    `medical_service_count` INT DEFAULT 0 COMMENT '社区医疗卫生服务站或村卫生室数量（个）',
    `militia_reserve_count` INT DEFAULT 0 COMMENT '民兵预备役人数（人）',
    `registered_volunteer_count` INT DEFAULT 0 COMMENT '登记注册志愿者人数（人）',
    `last_year_training_participants` INT DEFAULT 0 COMMENT '上一年度防灾减灾培训活动培训人次（人次）',
    `last_year_drill_participants` INT DEFAULT 0 COMMENT '参与上一年度组织的防灾减灾演练活动的居民(人次)',
    `emergency_shelter_capacity` INT DEFAULT 0 COMMENT '本级灾害应急避难场所容量（人）',
    `unique_id` VARCHAR(50) DEFAULT '' COMMENT '唯一码',
    `verification_status` VARCHAR(20) DEFAULT '' COMMENT '核实状态',
    `community_address` VARCHAR(200) DEFAULT '' COMMENT '社区（行政村）地址',
    `total_households` INT DEFAULT 0 COMMENT '总户数（户）',
    `age_0_14_count` INT DEFAULT 0 COMMENT '0-14岁人数',
    `age_65_plus_count` INT DEFAULT 0 COMMENT '65岁（含）以上人数',
    `disabled_person_count` INT DEFAULT 0 COMMENT '残障人员人数',
    `is_national_demo_community` VARCHAR(10) DEFAULT '' COMMENT '是否为全国综合减灾示范社区（是/否）',
    `is_provincial_demo_community` VARCHAR(10) DEFAULT '' COMMENT '是否为省级综合减灾示范社区（是/否）',
    `disaster_info_staff_count` INT DEFAULT 0 COMMENT '灾害信息员人数（人）',
    `emergency_shelter_count` INT DEFAULT 0 COMMENT '本级灾害应急避难场所数量（个或处）',
    `material_storage_method` VARCHAR(200) DEFAULT '' COMMENT '防灾减灾应急物资储备方式（多选）',
    `material_storage_method_other` VARCHAR(200) DEFAULT '' COMMENT '防灾减灾应急物资储备方式-其他项说明',
    `warning_receive_method` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息接收方式（多选）',
    `warning_receive_method_other` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息接收方式-其他项说明',
    `warning_communication_method` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息传达方式（多选）',
    `warning_communication_method_other` VARCHAR(200) DEFAULT '' COMMENT '灾害预警信息传达方式-其他项说明',
    `disaster_report_method` VARCHAR(200) DEFAULT '' COMMENT '灾情信息上报方式（多选）',
    `disaster_report_method_other` VARCHAR(200) DEFAULT '' COMMENT '灾情信息上报方式-其他项说明',
    `last_year_training_count` INT DEFAULT 0 COMMENT '上一年度组织的防灾减灾培训活动次数（次）',
    `last_year_drill_count` INT DEFAULT 0 COMMENT '上一年度组织的防灾减灾演练活动次数（次）',
    `unit_leader` VARCHAR(50) DEFAULT '' COMMENT '单位负责人',
    `statistics_leader` VARCHAR(50) DEFAULT '' COMMENT '统计负责人',
    `form_filler` VARCHAR(50) DEFAULT '' COMMENT '填表人',
    `contact_phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    `report_date` DATE DEFAULT NULL COMMENT '报出日期（年/月/日）',
    `fill_instructions` TEXT COMMENT '填写说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_region_code` (`region_code`),
    INDEX `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区减灾能力表';

-- 组织机构边界配置表
DROP TABLE IF EXISTS `organization_boundary`;
CREATE TABLE `organization_boundary` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `organization_id` BIGINT DEFAULT NULL COMMENT '组织机构ID',
    `year` INT DEFAULT NULL COMMENT '年份',
    `boundary_coordinates` TEXT COMMENT '边界坐标（GeoJSON或其他格式文本）',
    `file_path` VARCHAR(500) DEFAULT '' COMMENT '边界招标文件路径',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_organization_id` (`organization_id`),
    INDEX `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织机构边界配置表';

-- 地区数据表
DROP TABLE IF EXISTS `region_data`;
CREATE TABLE `region_data` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `code` VARCHAR(20) NOT NULL COMMENT '地区编码',
    `name` VARCHAR(100) NOT NULL COMMENT '地区名称',
    `level` INT DEFAULT NULL COMMENT '地区级别：1-省份，2-城市，3-区县',
    `parent_code` VARCHAR(20) DEFAULT '' COMMENT '父级地区编码',
    `parent_name` VARCHAR(100) DEFAULT '' COMMENT '父级地区名称',
    `pinyin` VARCHAR(200) DEFAULT '' COMMENT '拼音（可选）',
    `longitude` VARCHAR(50) DEFAULT '' COMMENT '经度',
    `latitude` VARCHAR(50) DEFAULT '' COMMENT '纬度',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `status` INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_code` (`code`),
    INDEX `idx_parent_code` (`parent_code`),
    INDEX `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区数据表';

-- 报告表
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `primary_result_id` BIGINT DEFAULT NULL COMMENT '一级指标结果ID',
    `report_name` VARCHAR(200) DEFAULT '' COMMENT '报告名称',
    `report_type` VARCHAR(20) DEFAULT '' COMMENT '报告类型(PDF/WORD/MAP)',
    `file_path` VARCHAR(500) DEFAULT '' COMMENT '报告文件路径',
    `map_image_path` VARCHAR(500) DEFAULT '' COMMENT '专题图路径',
    `generate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    INDEX `idx_primary_result_id` (`primary_result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告表';

-- 字段映射配置表
SET FOREIGN_KEY_CHECKS = 1;
