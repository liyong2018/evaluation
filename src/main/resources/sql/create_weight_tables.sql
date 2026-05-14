-- ============================================================
-- 权重管理相关表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 权重配置表
DROP TABLE IF EXISTS `weight_config`;
CREATE TABLE `weight_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `description` VARCHAR(500) DEFAULT '' COMMENT '配置描述',
    `orgcode` VARCHAR(20) NOT NULL COMMENT '组织机构编码（行政区划代码）',
    `data_source` VARCHAR(50) DEFAULT '' COMMENT '数据来源',
    `year` INT DEFAULT NULL COMMENT '年份',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除，1-已删除)',
    INDEX `idx_orgcode_year` (`orgcode`, `year`),
    INDEX `idx_config_name` (`config_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权重配置表';

-- 指标权重表
DROP TABLE IF EXISTS `indicator_weight`;
CREATE TABLE `indicator_weight` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `config_id` BIGINT NOT NULL COMMENT '权重配置ID',
    `indicator_code` VARCHAR(50) NOT NULL COMMENT '指标代码',
    `indicator_name` VARCHAR(100) NOT NULL COMMENT '指标名称',
    `indicator_level` INT NOT NULL COMMENT '指标级别(1-一级指标，2-二级指标)',
    `weight` DECIMAL(10,8) NOT NULL DEFAULT 0 COMMENT '权重值',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父指标ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_config_id` (`config_id`),
    INDEX `idx_indicator_code` (`indicator_code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标权重表';

-- 专家权重打分记录表
DROP TABLE IF EXISTS `indicator_weight_score`;
CREATE TABLE `indicator_weight_score` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `config_id` BIGINT NOT NULL COMMENT '权重配置ID',
    `orgcode` VARCHAR(20) NOT NULL COMMENT '组织机构编码（行政区划代码）',
    `indicator_code` VARCHAR(50) NOT NULL COMMENT '指标代码',
    `weight` DOUBLE DEFAULT 0 COMMENT '专家建议的权重值（0-1之间）',
    `expert_name` VARCHAR(50) NOT NULL COMMENT '专家姓名',
    `expert_phone` VARCHAR(20) DEFAULT '' COMMENT '专家电话',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打分时间',
    INDEX `idx_config_id` (`config_id`),
    INDEX `idx_orgcode` (`orgcode`),
    INDEX `idx_indicator_code` (`indicator_code`),
    INDEX `idx_expert_name` (`expert_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家权重打分记录表';

SET FOREIGN_KEY_CHECKS = 1;
