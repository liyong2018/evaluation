package com.evaluate.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 模型配置化改造的数据库 Schema 自动迁移
 * 在应用启动时自动检查并添加 model_type / data_source_type / aggregation_type 列
 * 以及 model_dependency / model_execution_strategy 表
 *
 * 所有操作都是幂等的（IF NOT EXISTS），可安全重复执行
 */
@Component
@Order(1)
public class ModelConfigSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ModelConfigSchemaMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            migrateEvaluationModelColumns();
            createModelDependencyTable();
            createModelExecutionStrategyTable();
            seedModelTypeData();
            seedModelDependencyData();
            log.info("ModelConfigSchemaMigration completed successfully");
        } catch (Exception e) {
            log.error("ModelConfigSchemaMigration failed: {}", e.getMessage(), e);
        }
    }

    private void migrateEvaluationModelColumns() {
        // Check if model_type column already exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'evaluation_model' AND COLUMN_NAME = 'model_type'",
                Integer.class);
        if (count != null && count > 0) {
            log.info("evaluation_model.model_type column already exists, skipping ALTER");
            return;
        }

        log.info("Adding model_type / data_source_type / aggregation_type columns to evaluation_model...");
        jdbcTemplate.execute("ALTER TABLE evaluation_model "
                + "ADD COLUMN model_type VARCHAR(64) DEFAULT NULL COMMENT '模型类型' AFTER model_code, "
                + "ADD COLUMN data_source_type VARCHAR(64) DEFAULT NULL COMMENT '数据源类型' AFTER model_type, "
                + "ADD COLUMN aggregation_type VARCHAR(64) DEFAULT NULL COMMENT '聚合类型' AFTER data_source_type");
        jdbcTemplate.execute("ALTER TABLE evaluation_model ADD INDEX idx_model_type (model_type)");
        log.info("evaluation_model columns added successfully");
    }

    private void createModelDependencyTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'model_dependency'",
                Integer.class);
        if (count != null && count > 0) {
            log.info("model_dependency table already exists, skipping CREATE");
            return;
        }

        log.info("Creating model_dependency table...");
        jdbcTemplate.execute("CREATE TABLE model_dependency ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',"
                + "model_id BIGINT NOT NULL COMMENT '主模型ID',"
                + "dependency_key VARCHAR(64) NOT NULL COMMENT '依赖标识',"
                + "dependency_model_id BIGINT DEFAULT NULL COMMENT '依赖的模型ID',"
                + "keyword_match VARCHAR(255) DEFAULT NULL COMMENT '模型名关键词匹配',"
                + "reuse_dependency_key VARCHAR(64) DEFAULT NULL COMMENT '复用其他依赖的模型ID',"
                + "region_code_strategy VARCHAR(64) DEFAULT 'direct' COMMENT '地区编码解析策略',"
                + "fallback_model_id BIGINT DEFAULT NULL COMMENT '兜底模型ID',"
                + "data_table_name VARCHAR(128) DEFAULT NULL COMMENT '关联数据表名',"
                + "sort_order INT DEFAULT 0 COMMENT '排序',"
                + "status INT DEFAULT 1 COMMENT '状态',"
                + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',"
                + "INDEX idx_model_id (model_id),"
                + "INDEX idx_dependency_key (dependency_key)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型前置依赖配置表'");
        log.info("model_dependency table created successfully");
    }

    private void createModelExecutionStrategyTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'model_execution_strategy'",
                Integer.class);
        if (count != null && count > 0) {
            log.info("model_execution_strategy table already exists, skipping CREATE");
            return;
        }

        log.info("Creating model_execution_strategy table...");
        jdbcTemplate.execute("CREATE TABLE model_execution_strategy ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',"
                + "model_id BIGINT NOT NULL COMMENT '模型ID',"
                + "strategy_type VARCHAR(64) NOT NULL COMMENT '策略类型',"
                + "strategy_key VARCHAR(128) NOT NULL COMMENT '策略标识',"
                + "strategy_value TEXT DEFAULT NULL COMMENT '策略值',"
                + "error_message VARCHAR(500) DEFAULT NULL COMMENT '数据校验错误消息',"
                + "sort_order INT DEFAULT 0 COMMENT '排序',"
                + "status INT DEFAULT 1 COMMENT '状态',"
                + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',"
                + "UNIQUE INDEX uk_model_strategy (model_id, strategy_type, strategy_key),"
                + "INDEX idx_model_id (model_id),"
                + "INDEX idx_strategy_type (strategy_type)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型执行策略配置表'");
        log.info("model_execution_strategy table created successfully");
    }

    private void seedModelTypeData() {
        // Only seed if model_type is NULL for known models
        Integer nullCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM evaluation_model WHERE model_type IS NULL",
                Integer.class);
        if (nullCount == null || nullCount == 0) {
            log.info("model_type data already seeded, skipping");
            return;
        }

        log.info("Seeding model_type / data_source_type / aggregation_type data...");

        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='LEGACY_TOWNSHIP', data_source_type='survey_table', aggregation_type='none' WHERE id=3 AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='COMMUNITY_DIRECT', data_source_type='community_table', aggregation_type='direct_community' WHERE id=4 AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='COMMUNITY_TOWNSHIP', data_source_type='community_table', aggregation_type='township_aggregation' WHERE id=8 AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='LEGACY_COMPREHENSIVE', data_source_type='comprehensive_result', aggregation_type='none' WHERE id=11 AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='COMMUNITY_COUNTY_UNIT', data_source_type='community_table', aggregation_type='county_aggregation' WHERE id=17 AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='TOWNSHIP_COUNTY_UNIT', data_source_type='survey_table', aggregation_type='county_aggregation' WHERE id=19 AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='CITY_COMPREHENSIVE_2020', data_source_type='comprehensive_result', aggregation_type='none' WHERE id=20 AND model_type IS NULL");

        // Keyword-based fallback for dynamically added models
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='GOVERNMENT', data_source_type='government_table', aggregation_type='none' WHERE model_name LIKE '%政府减灾能力%' AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='ENTERPRISE', data_source_type='enterprise_table', aggregation_type='none' WHERE model_name LIKE '%企业减灾能力%' AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='SOCIAL_ORGANIZATION', data_source_type='social_organization_table', aggregation_type='none' WHERE model_name LIKE '%社会组织减灾能力%' AND model_type IS NULL");
        jdbcTemplate.execute("UPDATE evaluation_model SET model_type='FAMILY', data_source_type='family_table', aggregation_type='none' WHERE model_name LIKE '%家庭减灾能力%' AND model_type IS NULL");

        log.info("model_type data seeded successfully");
    }

    private void seedModelDependencyData() {
        // Only seed if model_dependency has no rows for model_id=20
        Integer depCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM model_dependency WHERE model_id = 20",
                Integer.class);
        if (depCount != null && depCount > 0) {
            log.info("model_dependency data already seeded, skipping");
            return;
        }

        log.info("Seeding model_dependency data for city comprehensive model...");
        jdbcTemplate.execute("INSERT INTO model_dependency (model_id, dependency_key, dependency_model_id, keyword_match, reuse_dependency_key, region_code_strategy, fallback_model_id, data_table_name, sort_order) VALUES "
                + "(20, 'government', NULL, '政府减灾能力', NULL, 'truncate_to_county', NULL, 'government_disaster_reduction_capacity_2020', 1),"
                + "(20, 'enterprise', NULL, '企业减灾能力', NULL, 'truncate_to_county', NULL, 'enterprise_disaster_reduction_capacity_2020', 2),"
                + "(20, 'socialOrganization', NULL, NULL, 'enterprise', 'truncate_to_county', NULL, 'social_organization_disaster_reduction_capacity_2020', 3),"
                + "(20, 'townshipCountyUnit', NULL, '乡镇,区县单元', NULL, 'truncate_to_county', 19, NULL, 4),"
                + "(20, 'communityCountyUnit', NULL, '社区,区县单元', NULL, 'append_000', 17, NULL, 5),"
                + "(20, 'family', NULL, '家庭减灾能力', NULL, 'truncate_to_county', NULL, 'family_disaster_reduction_capacity_2020', 6)");
        log.info("model_dependency data seeded successfully");
    }
}
