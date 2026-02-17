-- ============================================================
-- 综合减灾能力评估系统 - 数据库初始化脚本
-- Consolidated Database Initialization Script
--
-- 包含以下模块:
-- 1. RBAC权限系统 (用户、角色、菜单、权限)
-- 2. 组织机构表 (省、市、县)
-- 3. 基层组织机构表 (乡镇、社区)
-- 4. 医疗卫生机构表
-- 5. 消防员配置表
-- 6. 角色机构关联表
-- 7. 数据库索引优化
-- 8. 权限修复
--
-- 创建时间: 2025-01-26
-- 版本: 1.0
-- ============================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一部分: RBAC权限系统
-- ============================================================

-- 1.1 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
    `status` INT DEFAULT 1 COMMENT '状态(1:正常,0:禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.2 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT '' COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 1.3 菜单/权限表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(200) DEFAULT '' COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT '' COMMENT '组件路径',
    `perms` VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    `icon` VARCHAR(100) DEFAULT '' COMMENT '图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `menu_type` INT DEFAULT 1 COMMENT '类型(0:目录,1:菜单,2:按钮)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 1.4 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 1.5 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 1.6 初始化用户数据
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`)
VALUES ('admin', '$2a$10$u.bP1mZcpRpU2zb/C7MbwO6TaeIXHPJUW8xJ1mOL4qWke1Hhu27ca', '管理员', 1);

-- 1.7 初始化角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'ROLE_ADMIN', '系统管理员'),
('普通用户', 'ROLE_USER', '普通用户');

-- 1.8 关联管理员用户和角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u, `sys_role` r
WHERE u.username = 'admin' AND r.role_code = 'ROLE_ADMIN';

-- ============================================================
-- 第二部分: 组织机构表 (省、市、县)
-- ============================================================

DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT NULL COMMENT '父级机构ID',
    `code` VARCHAR(32) NOT NULL COMMENT '机构编码（行政区划代码）',
    `name` VARCHAR(128) NOT NULL COMMENT '机构名称',
    `level` TINYINT NOT NULL COMMENT '级别：1省、2市、3县、4乡镇、5社区',
    `year` INT DEFAULT NULL COMMENT '数据所属年份',
    `data_source` VARCHAR(32) NOT NULL COMMENT '来源：COMMUNITY/TOWNSHIP 等',
    `province_name` VARCHAR(128) NULL COMMENT '省名称',
    `city_name` VARCHAR(128) NULL COMMENT '市名称',
    `county_name` VARCHAR(128) NULL COMMENT '县名称',
    `township_name` VARCHAR(128) NULL COMMENT '乡镇名称',
    `community_name` VARCHAR(128) NULL COMMENT '社区名称',
    `is_baseline` TINYINT NOT NULL DEFAULT 0 COMMENT '是否基线数据 0-否 1-是',
    `baseline_code` VARCHAR(32) NULL COMMENT '基线代码',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_organization_code_year` (`code`, `year`),
    KEY `idx_organization_parent` (`parent_id`),
    KEY `idx_organization_level` (`level`),
    KEY `idx_organization_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织机构表（省、市、县）';

-- 2.1 插入省级数据（level=1）
INSERT INTO `organization` (`code`, `name`, `level`, `year`, `data_source`, `province_name`, `is_baseline`) VALUES
('51', '四川省', 1, 2020, 'BASELINE', '四川省', 1);

-- 2.2 插入市级数据（level=2）
INSERT INTO `organization` (`code`, `name`, `level`, `year`, `data_source`, `province_name`, `city_name`, `parent_id`, `is_baseline`) VALUES
('5101', '成都市', 2, 2020, 'BASELINE', '四川省', '成都市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5103', '自贡市', 2, 2020, 'BASELINE', '四川省', '自贡市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5104', '攀枝花市', 2, 2020, 'BASELINE', '四川省', '攀枝花市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5105', '泸州市', 2, 2020, 'BASELINE', '四川省', '泸州市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5106', '德阳市', 2, 2020, 'BASELINE', '四川省', '德阳市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5107', '绵阳市', 2, 2020, 'BASELINE', '四川省', '绵阳市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5108', '广元市', 2, 2020, 'BASELINE', '四川省', '广元市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5109', '遂宁市', 2, 2020, 'BASELINE', '四川省', '遂宁市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5110', '内江市', 2, 2020, 'BASELINE', '四川省', '内江市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5111', '乐山市', 2, 2020, 'BASELINE', '四川省', '乐山市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5113', '南充市', 2, 2020, 'BASELINE', '四川省', '南充市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5114', '眉山市', 2, 2020, 'BASELINE', '四川省', '眉山市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5115', '宜宾市', 2, 2020, 'BASELINE', '四川省', '宜宾市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5116', '广安市', 2, 2020, 'BASELINE', '四川省', '广安市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5117', '达州市', 2, 2020, 'BASELINE', '四川省', '达州市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5118', '雅安市', 2, 2020, 'BASELINE', '四川省', '雅安市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5119', '巴中市', 2, 2020, 'BASELINE', '四川省', '巴中市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5120', '资阳市', 2, 2020, 'BASELINE', '四川省', '资阳市', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5132', '阿坝藏族羌族自治州', 2, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5133', '甘孜藏族自治州', 2, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1),
('5134', '凉山彝族自治州', 2, 2020, 'BASELINE', '四川省', '凉山彝族自治州', (SELECT id FROM (SELECT id FROM organization WHERE code='51') AS tmp), 1);

-- 2.3 插入区县级数据（level=3）- 成都市区县
INSERT INTO `organization` (`code`, `name`, `level`, `year`, `data_source`, `province_name`, `city_name`, `county_name`, `parent_id`, `is_baseline`) VALUES
('510104', '锦江区', 3, 2020, 'BASELINE', '四川省', '成都市', '锦江区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510105', '青羊区', 3, 2020, 'BASELINE', '四川省', '成都市', '青羊区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510106', '金牛区', 3, 2020, 'BASELINE', '四川省', '成都市', '金牛区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510107', '武侯区', 3, 2020, 'BASELINE', '四川省', '成都市', '武侯区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510108', '成华区', 3, 2020, 'BASELINE', '四川省', '成都市', '成华区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510112', '龙泉驿区', 3, 2020, 'BASELINE', '四川省', '成都市', '龙泉驿区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510113', '青白江区', 3, 2020, 'BASELINE', '四川省', '成都市', '青白江区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510114', '新都区', 3, 2020, 'BASELINE', '四川省', '成都市', '新都区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510115', '温江区', 3, 2020, 'BASELINE', '四川省', '成都市', '温江区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510116', '双流区', 3, 2020, 'BASELINE', '四川省', '成都市', '双流区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510117', '郫都区', 3, 2020, 'BASELINE', '四川省', '成都市', '郫都区', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510121', '金堂县', 3, 2020, 'BASELINE', '四川省', '成都市', '金堂县', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510129', '大邑县', 3, 2020, 'BASELINE', '四川省', '成都市', '大邑县', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510131', '蒲江县', 3, 2020, 'BASELINE', '四川省', '成都市', '蒲江县', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510181', '都江堰市', 3, 2020, 'BASELINE', '四川省', '成都市', '都江堰市', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510182', '彭州市', 3, 2020, 'BASELINE', '四川省', '成都市', '彭州市', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510183', '邛崃市', 3, 2020, 'BASELINE', '四川省', '成都市', '邛崃市', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510184', '崇州市', 3, 2020, 'BASELINE', '四川省', '成都市', '崇州市', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1),
('510185', '简阳市', 3, 2020, 'BASELINE', '四川省', '成都市', '简阳市', (SELECT id FROM (SELECT id FROM organization WHERE code='5101') AS tmp), 1);

-- 2.4 插入更多区县（眉山市）
INSERT INTO `organization` (`code`, `name`, `level`, `year`, `data_source`, `province_name`, `city_name`, `county_name`, `parent_id`, `is_baseline`) VALUES
('511402', '东坡区', 3, 2020, 'BASELINE', '四川省', '眉山市', '东坡区', (SELECT id FROM (SELECT id FROM organization WHERE code='5114') AS tmp), 1),
('511403', '彭山区', 3, 2020, 'BASELINE', '四川省', '眉山市', '彭山区', (SELECT id FROM (SELECT id FROM organization WHERE code='5114') AS tmp), 1),
('511421', '仁寿县', 3, 2020, 'BASELINE', '四川省', '眉山市', '仁寿县', (SELECT id FROM (SELECT id FROM organization WHERE code='5114') AS tmp), 1),
('511423', '洪雅县', 3, 2020, 'BASELINE', '四川省', '眉山市', '洪雅县', (SELECT id FROM (SELECT id FROM organization WHERE code='5114') AS tmp), 1),
('511424', '丹棱县', 3, 2020, 'BASELINE', '四川省', '眉山市', '丹棱县', (SELECT id FROM (SELECT id FROM organization WHERE code='5114') AS tmp), 1),
('511425', '青神县', 3, 2020, 'BASELINE', '四川省', '眉山市', '青神县', (SELECT id FROM (SELECT id FROM organization WHERE code='5114') AS tmp), 1);

-- ============================================================
-- 第三部分: 基层组织机构表 (乡镇、社区)
-- ============================================================

DROP TABLE IF EXISTS `grassroots_organization`;
CREATE TABLE `grassroots_organization` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `county_id` BIGINT NOT NULL COMMENT '所属区县ID（关联organization表）',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父级机构ID（乡镇的parent_id指向区县，社区的parent_id指向乡镇）',
    `code` VARCHAR(32) NOT NULL COMMENT '机构编码（行政区划代码）',
    `name` VARCHAR(128) NOT NULL COMMENT '机构名称',
    `level` TINYINT NOT NULL COMMENT '级别：4乡镇、5社区',
    `year` INT DEFAULT NULL COMMENT '数据所属年份',
    `data_source` VARCHAR(32) NOT NULL COMMENT '来源：COMMUNITY/TOWNSHIP/IMPORT 等',
    `province_name` VARCHAR(128) DEFAULT NULL COMMENT '省名称',
    `city_name` VARCHAR(128) DEFAULT NULL COMMENT '市名称',
    `county_name` VARCHAR(128) DEFAULT NULL COMMENT '县名称',
    `township_name` VARCHAR(128) DEFAULT NULL COMMENT '乡镇名称',
    `community_name` VARCHAR(128) DEFAULT NULL COMMENT '社区名称',
    `is_baseline` TINYINT NOT NULL DEFAULT 0 COMMENT '是否基线数据 0-否 1-是',
    `baseline_code` VARCHAR(32) NULL COMMENT '基线代码',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_grassroots_code_year` (`code`, `year`),
    KEY `idx_grassroots_county` (`county_id`),
    KEY `idx_grassroots_parent` (`parent_id`),
    KEY `idx_grassroots_level` (`level`),
    KEY `idx_grassroots_year` (`year`),
    CONSTRAINT `fk_grassroots_county` FOREIGN KEY (`county_id`) REFERENCES `organization` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基层组织机构表（乡镇和社区）';

-- ============================================================
-- 第四部分: 角色机构关联表
-- ============================================================

DROP TABLE IF EXISTS `sys_role_organization`;
CREATE TABLE `sys_role_organization` (
    `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
    `organization_id` BIGINT(20) NOT NULL COMMENT '机构ID',
    PRIMARY KEY (`role_id`, `organization_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_organization_id` (`organization_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-机构关联表';

-- ============================================================
-- 第五部分: 医疗卫生机构表
-- ============================================================

DROP TABLE IF EXISTS `medical_institution`;
CREATE TABLE `medical_institution` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `unique_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '唯一码',
    `verification_status` VARCHAR(20) NOT NULL DEFAULT '待核实' COMMENT '核实状态',
    `unified_social_credit_code` VARCHAR(50) COMMENT '统一社会信用代码/机构编码',
    `code_type` VARCHAR(50) COMMENT '代码类型',
    `institution_name` VARCHAR(500) NOT NULL COMMENT '医疗卫生机构名称',
    `institution_address` VARCHAR(1000) COMMENT '医疗卫生机构详细地址',
    `institution_category_code` VARCHAR(10) COMMENT '医疗卫生机构类别代码',
    `institution_type_large` VARCHAR(100) COMMENT '医疗机构类型（大类）',
    `institution_type_medium` VARCHAR(100) COMMENT '医疗机构类型（中类）',
    `institution_type_specialized` VARCHAR(100) COMMENT '医疗机构类型（专科医院分类）',
    `hospital_level` VARCHAR(100) COMMENT '医院等级',
    `institution_nature` VARCHAR(50) COMMENT '医疗机构性质',
    `land_area` DECIMAL(10,2) COMMENT '占地面积（平方米）',
    `building_area` DECIMAL(10,2) COMMENT '房屋建筑面积（平方米）',
    `equipment_count_above_10k` INT COMMENT '万元以上设备台数',
    `total_staff` INT COMMENT '在岗职工人数',
    `health_technical_personnel` INT COMMENT '卫生技术人员总数',
    `registered_nurses` INT COMMENT '注册护士人数',
    `logistics_skill_personnel` INT COMMENT '工勤技能人员数',
    `annual_total_visits` INT COMMENT '年度总诊疗人次数',
    `annual_admission_count` INT COMMENT '年度入院人数',
    `annual_discharge_count` INT COMMENT '年度出院人数',
    `actual_hospital_beds` INT COMMENT '实有住院床位数',
    `negative_pressure_beds` INT COMMENT '负压病房床位数',
    `icu_beds` INT COMMENT '重症加强护理病房（ICU）床位数',
    `pre_hospital_emergency_personnel` INT COMMENT '院前急救专业人员数',
    `emergency_command_vehicle_count` INT COMMENT '急救指挥车数量',
    `transport_ambulance_count` INT COMMENT '运转型急救车数量',
    `monitor_ambulance_count` INT COMMENT '监护型急救车数量',
    `negative_pressure_ambulance_count` INT COMMENT '负压急救车数量',
    `blood_collection_vehicle_count` INT COMMENT '采血车数',
    `blood_delivery_vehicle_count` INT COMMENT '送血车数',
    `security_personnel_count` INT COMMENT '安全保卫人员数量',
    `emergency_power_supply` VARCHAR(100) COMMENT '应急供电能力',
    `emergency_power_supply_other` VARCHAR(500) COMMENT '应急供电能力-其他项说明',
    `water_supply_mode` VARCHAR(100) COMMENT '供水方式',
    `heating_mode` VARCHAR(100) COMMENT '供暖方式',
    `emergency_communication_mode` VARCHAR(100) COMMENT '应急通信保障方式',
    `emergency_communication_mode_other` VARCHAR(500) COMMENT '应急通信保障方式-其他项说明',
    `disaster_history_type` VARCHAR(200) COMMENT '曾经遭受过的自然灾害类型',
    `disaster_history_type_other` VARCHAR(500) COMMENT '曾经遭受过的自然灾害类型-其他说明',
    `emergency_plan_type` VARCHAR(200) COMMENT '已有自然灾害应急预案类型',
    `emergency_plan_type_other` VARCHAR(500) COMMENT '已有自然灾害应急预案类型-其他说明',
    `unit_leader` VARCHAR(100) COMMENT '单位负责人',
    `statistical_leader` VARCHAR(100) COMMENT '统计负责人',
    `form_filler` VARCHAR(100) COMMENT '填表人',
    `contact_phone` VARCHAR(50) COMMENT '联系电话',
    `report_date` DATE COMMENT '报出日期',
    `filling_instructions` TEXT COMMENT '填写说明',
    `year` INT NOT NULL COMMENT '数据年份',
    `org_code` VARCHAR(50) COMMENT '组织机构代码',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `update_by` VARCHAR(100) COMMENT '更新人'
) COMMENT='医疗卫生机构表';

-- ============================================================
-- 第六部分: 消防员配置表
-- ============================================================

DROP TABLE IF EXISTS `firefighter_config`;
CREATE TABLE `firefighter_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `region_code` VARCHAR(20) NOT NULL COMMENT '行政区划代码',
    `province_name` VARCHAR(100) COMMENT '省名称',
    `city_name` VARCHAR(100) COMMENT '市名称',
    `county_name` VARCHAR(100) COMMENT '县名称',
    `township_name` VARCHAR(100) COMMENT '乡镇名称',
    `firefighter_count` INT DEFAULT 0 COMMENT '消防员数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 0-无效',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_region_code` (`region_code`),
    KEY `idx_province` (`province_name`),
    KEY `idx_city` (`city_name`),
    KEY `idx_county` (`county_name`)
) COMMENT='消防员配置表';

-- ============================================================
-- 第七部分: 数据库索引优化
-- ============================================================

-- 7.1 医疗机构表索引
CREATE INDEX `idx_medical_institution_year_org` ON `medical_institution`(`year`, `org_code`);
CREATE INDEX `idx_medical_institution_year` ON `medical_institution`(`year`);
CREATE INDEX `idx_medical_institution_org_code` ON `medical_institution`(`org_code`);
CREATE INDEX `idx_medical_institution_create_time` ON `medical_institution`(`create_time`);

-- 7.2 组织机构表索引
CREATE INDEX `idx_organization_code` ON `organization`(`code`);
CREATE INDEX `idx_organization_year_level` ON `organization`(`year`, `level`);

-- 7.3 基层组织机构表索引
CREATE INDEX `idx_grassroots_code` ON `grassroots_organization`(`code`);
CREATE INDEX `idx_grassroots_year_level` ON `grassroots_organization`(`year`, `level`);

-- 7.4 分析表以更新统计信息
ANALYZE TABLE `medical_institution`;
ANALYZE TABLE `organization`;
ANALYZE TABLE `grassroots_organization`;

-- ============================================================
-- 第八部分: 权限修复
-- ============================================================

-- 修复 admin 用户组织权限（admin可以访问所有组织）
DELETE FROM `sys_role_organization` WHERE `role_id` = 1;

-- ============================================================
-- 第九部分: 验证数据
-- ============================================================

SELECT '=== 数据初始化完成统计 ===' AS info;
SELECT '组织机构表数据分布:' AS info;
SELECT level, COUNT(*) as count FROM `organization` GROUP BY level ORDER BY level;

SELECT '总记录数' AS info, COUNT(*) AS total FROM `organization`;

SELECT 'RBAC系统用户数:' AS info, COUNT(*) AS user_count FROM `sys_user`;
SELECT 'RBAC系统角色数:' AS info, COUNT(*) AS role_count FROM `sys_role`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 脚本执行完成
-- ============================================================
