-- ============================================================
-- 模型配置化改造：新增模型依赖表、模型执行策略表
-- 将硬编码的模型类型、前置依赖、地区解析策略、数据校验策略迁移到数据库
-- ============================================================

SET NAMES utf8mb4;

-- 1. evaluation_model 表新增 model_type 字段
-- 替代 isGovernmentModel/isEnterpriseModel/... 等关键词匹配逻辑
ALTER TABLE `evaluation_model`
    ADD COLUMN `model_type` VARCHAR(64) DEFAULT NULL COMMENT '模型类型(GOVERNMENT/ENTERPRISE/SOCIAL_ORGANIZATION/FAMILY/COMMUNITY_DIRECT/COMMUNITY_TOWNSHIP/COMMUNITY_COUNTY_UNIT/TOWNSHIP_COUNTY_UNIT/LEGACY_COMPREHENSIVE/CITY_COMPREHENSIVE_2020/LEGACY_TOWNSHIP)'
    AFTER `model_code`;

ALTER TABLE `evaluation_model`
    ADD INDEX `idx_model_type` (`model_type`);

-- 2. evaluation_model 表新增 data_source_type 字段
-- 替代 modelId==4/8/17 的数据源选择硬编码
ALTER TABLE `evaluation_model`
    ADD COLUMN `data_source_type` VARCHAR(64) DEFAULT NULL COMMENT '数据源类型(government_table/enterprise_table/social_organization_table/family_table/community_table/survey_table/comprehensive_result)'
    AFTER `model_type`;

-- 3. evaluation_model 表新增 aggregation_type 字段
-- 替代 isDirectCommunityModel/isTownshipAggregationModel/isCountyAggregationModel
ALTER TABLE `evaluation_model`
    ADD COLUMN `aggregation_type` VARCHAR(64) DEFAULT NULL COMMENT '聚合类型(direct_community/township_aggregation/county_aggregation/none)'
    AFTER `data_source_type`;

-- 4. 创建模型前置依赖配置表
-- 替代 resolveCityComprehensiveSourceModelIds() 中的硬编码
DROP TABLE IF EXISTS `model_dependency`;
CREATE TABLE `model_dependency` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_id` BIGINT NOT NULL COMMENT '主模型ID(需要前置模型的模型)',
    `dependency_key` VARCHAR(64) NOT NULL COMMENT '依赖标识(如government/enterprise/socialOrganization/townshipCountyUnit/communityCountyUnit/family)',
    `dependency_model_id` BIGINT DEFAULT NULL COMMENT '依赖的模型ID(精确指定)',
    `keyword_match` VARCHAR(255) DEFAULT NULL COMMENT '模型名关键词匹配(逗号分隔,用于动态查找)',
    `reuse_dependency_key` VARCHAR(64) DEFAULT NULL COMMENT '复用其他依赖的模型ID(如socialOrganization复用enterprise)',
    `region_code_strategy` VARCHAR(64) DEFAULT 'direct' COMMENT '地区编码解析策略(direct/truncate_to_county/append_000)',
    `fallback_model_id` BIGINT DEFAULT NULL COMMENT '兜底模型ID(关键词匹配失败时使用)',
    `data_table_name` VARCHAR(128) DEFAULT NULL COMMENT '关联数据表名(用于数据校验)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` INT DEFAULT 1 COMMENT '状态(1启用/0禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_dependency_key` (`dependency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型前置依赖配置表';

-- 5. 创建模型执行策略配置表
-- 替代 resolveEffectiveRegionCodes/resolveRegionDataValidationError 中的硬编码策略分发
DROP TABLE IF EXISTS `model_execution_strategy`;
CREATE TABLE `model_execution_strategy` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `strategy_type` VARCHAR(64) NOT NULL COMMENT '策略类型(region_resolution/data_validation/data_source/result_display)',
    `strategy_key` VARCHAR(128) NOT NULL COMMENT '策略标识',
    `strategy_value` TEXT DEFAULT NULL COMMENT '策略值(JSON或简单字符串)',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '数据校验错误消息模板',
    `sort_order` INT DEFAULT 0 COMMENT '排序(同类型多策略时的优先级)',
    `status` INT DEFAULT 1 COMMENT '状态(1启用/0禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX `uk_model_strategy` (`model_id`, `strategy_type`, `strategy_key`),
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_strategy_type` (`strategy_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型执行策略配置表';

-- ============================================================
-- 初始化数据：为现有模型填充 model_type / data_source_type / aggregation_type
-- ============================================================

UPDATE `evaluation_model` SET `model_type` = 'LEGACY_TOWNSHIP',
    `data_source_type` = 'survey_table',
    `aggregation_type` = 'none'
WHERE `id` = 3;

UPDATE `evaluation_model` SET `model_type` = 'COMMUNITY_DIRECT',
    `data_source_type` = 'community_table',
    `aggregation_type` = 'direct_community'
WHERE `id` = 4;

UPDATE `evaluation_model` SET `model_type` = 'COMMUNITY_TOWNSHIP',
    `data_source_type` = 'community_table',
    `aggregation_type` = 'township_aggregation'
WHERE `id` = 8;

UPDATE `evaluation_model` SET `model_type` = 'LEGACY_COMPREHENSIVE',
    `data_source_type` = 'comprehensive_result',
    `aggregation_type` = 'none'
WHERE `id` = 11;

UPDATE `evaluation_model` SET `model_type` = 'COMMUNITY_COUNTY_UNIT',
    `data_source_type` = 'community_table',
    `aggregation_type` = 'none'
WHERE `id` = 17;

UPDATE `evaluation_model` SET `model_type` = 'TOWNSHIP_COUNTY_UNIT',
    `data_source_type` = 'survey_table',
    `aggregation_type` = 'county_aggregation'
WHERE `id` = 19;

UPDATE `evaluation_model` SET `model_type` = 'CITY_COMPREHENSIVE_2020',
    `data_source_type` = 'comprehensive_result',
    `aggregation_type` = 'none'
WHERE `id` = 20;

-- 政府减灾能力模型(关键词匹配)
UPDATE `evaluation_model` SET `model_type` = 'GOVERNMENT',
    `data_source_type` = 'government_table',
    `aggregation_type` = 'none'
WHERE `model_name` LIKE '%政府减灾能力%' AND `model_type` IS NULL;

-- 企业减灾能力模型(关键词匹配)
UPDATE `evaluation_model` SET `model_type` = 'ENTERPRISE',
    `data_source_type` = 'enterprise_table',
    `aggregation_type` = 'none'
WHERE `model_name` LIKE '%企业减灾能力%' AND `model_type` IS NULL;

-- 社会组织减灾能力模型(关键词匹配)
UPDATE `evaluation_model` SET `model_type` = 'SOCIAL_ORGANIZATION',
    `data_source_type` = 'social_organization_table',
    `aggregation_type` = 'none'
WHERE `model_name` LIKE '%社会组织减灾能力%' AND `model_type` IS NULL;

-- 家庭减灾能力模型(关键词匹配)
UPDATE `evaluation_model` SET `model_type` = 'FAMILY',
    `data_source_type` = 'family_table',
    `aggregation_type` = 'none'
WHERE `model_name` LIKE '%家庭减灾能力%' AND `model_type` IS NULL;

-- ============================================================
-- 初始化数据：2020市级综合模型(Model 20)的前置模型依赖配置
-- ============================================================

INSERT INTO `model_dependency` (`model_id`, `dependency_key`, `dependency_model_id`, `keyword_match`, `reuse_dependency_key`, `region_code_strategy`, `fallback_model_id`, `data_table_name`, `sort_order`) VALUES
(20, 'government', NULL, '政府减灾能力', NULL, 'truncate_to_county', NULL, 'government_disaster_reduction_capacity_2020', 1),
(20, 'enterprise', NULL, '企业减灾能力', NULL, 'truncate_to_county', NULL, 'enterprise_disaster_reduction_capacity_2020', 2),
(20, 'socialOrganization', NULL, NULL, 'enterprise', 'truncate_to_county', NULL, 'social_organization_disaster_reduction_capacity_2020', 3),
(20, 'townshipCountyUnit', NULL, '乡镇,区县单元', NULL, 'truncate_to_county', 19, NULL, 4),
(20, 'communityCountyUnit', NULL, '社区,区县单元', NULL, 'append_000', 17, NULL, 5),
(20, 'family', NULL, '家庭减灾能力', NULL, 'truncate_to_county', NULL, 'family_disaster_reduction_capacity_2020', 6);

-- ============================================================
-- 初始化数据：模型执行策略配置
-- ============================================================

-- 地区编码解析策略
INSERT INTO `model_execution_strategy` (`model_id`, `strategy_type`, `strategy_key`, `strategy_value`, `sort_order`) VALUES
-- 政府减灾能力：按政府数据表解析
((SELECT id FROM evaluation_model WHERE model_type = 'GOVERNMENT' LIMIT 1), 'region_resolution', 'strategy', 'government', 1),
-- 企业减灾能力：按企业数据表解析
((SELECT id FROM evaluation_model WHERE model_type = 'ENTERPRISE' LIMIT 1), 'region_resolution', 'strategy', 'enterprise', 1),
-- 社会组织减灾能力：按社会组织数据表解析
((SELECT id FROM evaluation_model WHERE model_type = 'SOCIAL_ORGANIZATION' LIMIT 1), 'region_resolution', 'strategy', 'social_organization', 1),
-- 家庭减灾能力：按家庭数据表解析
((SELECT id FROM evaluation_model WHERE model_type = 'FAMILY' LIMIT 1), 'region_resolution', 'strategy', 'family', 1),
-- 社区-行政村(4)/社区-乡镇(8)/社区区县单元(17)：社区地区解析
(4, 'region_resolution', 'strategy', 'community', 1),
(8, 'region_resolution', 'strategy', 'community', 1),
(17, 'region_resolution', 'strategy', 'community', 1),
-- 乡镇区县单元(19)：乡镇区县单元解析
(19, 'region_resolution', 'strategy', 'township_county_unit', 1),
-- 2020综合模型(20)：综合地区解析
(20, 'region_resolution', 'strategy', 'city_comprehensive', 1);

-- 数据校验策略
INSERT INTO `model_execution_strategy` (`model_id`, `strategy_type`, `strategy_key`, `strategy_value`, `error_message`, `sort_order`) VALUES
-- 政府减灾能力
((SELECT id FROM evaluation_model WHERE model_type = 'GOVERNMENT' LIMIT 1), 'data_validation', 'check_type', 'table_data', '所选年份无政府减灾能力数据，无法进行政府减灾能力评估', 1),
-- 企业减灾能力
((SELECT id FROM evaluation_model WHERE model_type = 'ENTERPRISE' LIMIT 1), 'data_validation', 'check_type', 'table_data', '所选年份无企业减灾能力数据，无法进行企业减灾能力评估', 1),
-- 社会组织减灾能力
((SELECT id FROM evaluation_model WHERE model_type = 'SOCIAL_ORGANIZATION' LIMIT 1), 'data_validation', 'check_type', 'table_data', '所选年份无社会组织减灾能力数据，无法进行社会组织减灾能力评估', 1),
-- 社区模型(4, 8)：社区数据表校验
(4, 'data_validation', 'check_type', 'community_table', '所选年份无社区数据，无法进行社区-行政村/乡镇减灾能力评估', 1),
(8, 'data_validation', 'check_type', 'community_table', '所选年份无社区数据，无法进行社区-行政村/乡镇减灾能力评估', 1),
-- 区县单元模型(17, 19)：调查数据表校验
(17, 'data_validation', 'check_type', 'survey_table', '所选年份无乡镇数据，无法进行当前评估模型', 1),
(19, 'data_validation', 'check_type', 'survey_table', '所选年份无乡镇数据，无法进行当前评估模型', 1),
-- 综合模型(11)：依赖前置评估结果
(11, 'data_validation', 'check_type', 'result_dependency', NULL, 1),
(11, 'data_validation', 'dependency_models', '{"legacyTownship": 3, "communityTownship": 8}', '综合减灾能力评估需要乡镇减灾能力评估结果和社区-乡镇减灾能力评估结果', 2),
-- 2020综合模型(20)：依赖前置模型结果(通过model_dependency配置)
(20, 'data_validation', 'check_type', 'model_dependency', NULL, 1);
