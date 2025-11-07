package com.evaluate.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 完整数据库迁移程序
 * 迁移所有13个表到 Supabase PostgreSQL
 * 启用: migration.enabled=true 和 migration.full.enabled=true
 */
@Component
@Conditional(FullMigrationCondition.class)
public class FullDatabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FullDatabaseMigrationRunner.class);

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUser;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${supabase.jdbc.url}")
    private String pgUrl;

    @Value("${supabase.jdbc.user}")
    private String pgUser;

    @Value("${supabase.jdbc.password}")
    private String pgPassword;

    @Value("${migration.truncate:false}")
    private boolean truncate;

    @Value("${migration.exitOnFinish:false}")
    private boolean exitOnFinish;

    // 表迁移统计
    private final Map<String, Long> migrationCounts = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("开始完整数据库迁移: MySQL -> Supabase");
        log.info("========================================");

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPassword)) {

            pg.setAutoCommit(false);

            // 创建所有表
            log.info("步骤 1: 创建表结构");
            createAllTables(pg);

            // 清空表（如果需要）
            if (truncate) {
                log.info("步骤 2: 清空现有数据");
                truncateAllTables(pg);
            }

            // 迁移数据
            log.info("步骤 3: 迁移数据");
            migrateAllData(mysql, pg);

            // 对齐序列
            log.info("步骤 4: 对齐序列");
            alignAllSequences(pg);

            pg.commit();

            // 输出迁移统计
            printMigrationSummary();

        } catch (Exception e) {
            log.error("迁移失败", e);
            throw e;
        } finally {
            if (exitOnFinish) {
                log.info("迁移完成，退出应用");
                System.exit(0);
            }
        }
    }

    private void createAllTables(Connection pg) throws SQLException {
        // 1. organization 表
        exec(pg, "DROP TABLE IF EXISTS public.organization CASCADE;" +
                "CREATE TABLE public.organization (" +
                " id bigserial PRIMARY KEY," +
                " parent_id bigint," +
                " code varchar(32) UNIQUE NOT NULL," +
                " name varchar(128) NOT NULL," +
                " level smallint NOT NULL," +
                " data_source varchar(32) NOT NULL," +
                " province_name varchar(128)," +
                " city_name varchar(128)," +
                " county_name varchar(128)," +
                " township_name varchar(128)," +
                " community_name varchar(128)," +
                " create_time timestamptz NOT NULL," +
                " update_time timestamptz NOT NULL," +
                " is_deleted smallint NOT NULL DEFAULT 0" +
                ")");

        // 2. evaluation_model 表
        exec(pg, "DROP TABLE IF EXISTS public.evaluation_model CASCADE;" +
                "CREATE TABLE public.evaluation_model (" +
                " id bigserial PRIMARY KEY," +
                " model_name varchar(100) NOT NULL," +
                " model_code varchar(50) UNIQUE NOT NULL," +
                " description text," +
                " version varchar(20) DEFAULT '1.0'," +
                " status int DEFAULT 1," +
                " is_default boolean DEFAULT false," +
                " create_time timestamptz DEFAULT now()," +
                " update_time timestamptz DEFAULT now()," +
                " create_by varchar(50)," +
                " update_by varchar(50)" +
                ")");

        // 3. algorithm_config 表
        exec(pg, "DROP TABLE IF EXISTS public.algorithm_config CASCADE;" +
                "CREATE TABLE public.algorithm_config (" +
                " id bigserial PRIMARY KEY," +
                " config_name varchar(100) NOT NULL," +
                " description varchar(255)," +
                " version varchar(20) DEFAULT '1.0'," +
                " status int DEFAULT 1," +
                " create_time timestamptz DEFAULT now()" +
                ")");

        // 4. weight_config 表
        exec(pg, "DROP TABLE IF EXISTS public.weight_config CASCADE;" +
                "CREATE TABLE public.weight_config (" +
                " id bigserial PRIMARY KEY," +
                " config_name varchar(100) NOT NULL," +
                " description varchar(255)," +
                " orgcode varchar(32)," +
                " data_source varchar(20) DEFAULT 'township'," +
                " create_time timestamptz DEFAULT now()," +
                " update_time timestamptz DEFAULT now()," +
                " is_deleted int DEFAULT 0" +
                ")");

        // 5. indicator_weight 表
        exec(pg, "DROP TABLE IF EXISTS public.indicator_weight CASCADE;" +
                "CREATE TABLE public.indicator_weight (" +
                " id bigserial PRIMARY KEY," +
                " config_id bigint NOT NULL," +
                " indicator_code varchar(50) NOT NULL," +
                " indicator_name varchar(100) NOT NULL," +
                " indicator_level int NOT NULL," +
                " weight decimal(5,4) NOT NULL," +
                " parent_id bigint," +
                " sort_order int DEFAULT 0," +
                " create_time timestamptz DEFAULT now()" +
                ")");

        // 6. survey_data 表
        exec(pg, "DROP TABLE IF EXISTS public.survey_data CASCADE;" +
                "CREATE TABLE public.survey_data (" +
                " id bigserial PRIMARY KEY," +
                " region_code varchar(20) NOT NULL," +
                " province varchar(50) NOT NULL," +
                " city varchar(50) NOT NULL," +
                " county varchar(50) NOT NULL," +
                " township varchar(100) NOT NULL," +
                " year int NOT NULL," +
                " population bigint NOT NULL," +
                " management_staff int NOT NULL," +
                " risk_assessment varchar(10) NOT NULL," +
                " funding_amount decimal(15,2) NOT NULL," +
                " material_value decimal(15,2) NOT NULL," +
                " hospital_beds int NOT NULL," +
                " firefighters int NOT NULL," +
                " volunteers int NOT NULL," +
                " militia_reserve int NOT NULL," +
                " training_participants int NOT NULL," +
                " shelter_capacity int NOT NULL," +
                " create_time timestamptz DEFAULT now()," +
                " update_time timestamptz DEFAULT now()," +
                " is_deleted int DEFAULT 0" +
                ")");

        // 7. community_disaster_reduction_capacity 表
        exec(pg, "DROP TABLE IF EXISTS public.community_disaster_reduction_capacity CASCADE;" +
                "CREATE TABLE public.community_disaster_reduction_capacity (" +
                " id bigserial PRIMARY KEY," +
                " region_code varchar(50) NOT NULL," +
                " province_name varchar(100)," +
                " city_name varchar(100)," +
                " county_name varchar(100)," +
                " township_name varchar(100)," +
                " community_name varchar(100) NOT NULL," +
                " year int NOT NULL," +
                " resident_population int DEFAULT 0," +
                " last_year_funding_amount decimal(12,2) DEFAULT 0.00," +
                " materials_equipment_value decimal(12,2) DEFAULT 0.00," +
                " medical_service_count int DEFAULT 0," +
                " militia_reserve_count int DEFAULT 0," +
                " registered_volunteer_count int DEFAULT 0," +
                " last_year_training_participants int DEFAULT 0," +
                " last_year_drill_participants int DEFAULT 0," +
                " emergency_shelter_capacity int DEFAULT 0," +
                " create_time timestamptz," +
                " update_time timestamptz" +
                ")");

        // 8. model_step 表 - 关键修正: 包含缺失字段
        exec(pg, "DROP TABLE IF EXISTS public.model_step CASCADE;" +
                "CREATE TABLE public.model_step (" +
                " id bigserial PRIMARY KEY," +
                " model_id bigint NOT NULL," +
                " step_name varchar(100) NOT NULL," +
                " step_code varchar(50) NOT NULL," +
                " step_order int NOT NULL," +
                " step_type varchar(20) NOT NULL," +
                " description text," +
                " input_variables text," +
                " output_variables text," +
                " depends_on varchar(255)," +
                " status int DEFAULT 1," +
                " create_time timestamptz DEFAULT now()" +
                ")");

        // 9. algorithm_step 表
        exec(pg, "DROP TABLE IF EXISTS public.algorithm_step CASCADE;" +
                "CREATE TABLE public.algorithm_step (" +
                " id bigint NOT NULL," +
                " algorithm_config_id bigint NOT NULL," +
                " step_name varchar(100) NOT NULL," +
                " step_code varchar(50) NOT NULL," +
                " description text NOT NULL," +
                " input_data bytea," +
                " output_data bytea," +
                " step_order int NOT NULL," +
                " status int DEFAULT 1," +
                " create_time timestamptz DEFAULT now()" +
                ")");

        // 10. step_algorithm 表
        exec(pg, "DROP TABLE IF EXISTS public.step_algorithm CASCADE;" +
                "CREATE TABLE public.step_algorithm (" +
                " id bigserial PRIMARY KEY," +
                " step_id bigint NOT NULL," +
                " algorithm_name varchar(100) NOT NULL," +
                " algorithm_code varchar(50) NOT NULL," +
                " algorithm_order int NOT NULL," +
                " ql_expression text NOT NULL," +
                " input_params text," +
                " output_param varchar(100)," +
                " description text," +
                " status int DEFAULT 1," +
                " create_time timestamptz DEFAULT now()" +
                ")");

        // 11. step_execution_result 表
        exec(pg, "DROP TABLE IF EXISTS public.step_execution_result CASCADE;" +
                "CREATE TABLE public.step_execution_result (" +
                " id bigserial PRIMARY KEY," +
                " execution_record_id bigint NOT NULL," +
                " step_id bigint NOT NULL," +
                " region_code varchar(20) NOT NULL," +
                " step_input text," +
                " step_output text," +
                " execution_time timestamptz DEFAULT now()," +
                " duration_ms bigint," +
                " status varchar(20) DEFAULT 'SUCCESS'," +
                " error_message text" +
                ")");

        // 12. report 表
        exec(pg, "DROP TABLE IF EXISTS public.report CASCADE;" +
                "CREATE TABLE public.report (" +
                " id bigserial PRIMARY KEY," +
                " primary_result_id bigint NOT NULL," +
                " report_name varchar(100) NOT NULL," +
                " report_type varchar(20) NOT NULL," +
                " file_path varchar(255)," +
                " map_image_path varchar(255)," +
                " generate_time timestamptz DEFAULT now()" +
                ")");

        // 13. evaluation_result 表 (已存在，但确保结构正确)
        exec(pg, "DROP TABLE IF EXISTS public.evaluation_result CASCADE;" +
                "CREATE TABLE public.evaluation_result (" +
                " id bigserial PRIMARY KEY," +
                " region_code text NOT NULL," +
                " region_name text," +
                " org_code text," +
                " management_capability_score numeric(18,6)," +
                " support_capability_score numeric(18,6)," +
                " self_rescue_capability_score numeric(18,6)," +
                " comprehensive_capability_score numeric(18,6)," +
                " management_capability_level text," +
                " support_capability_level text," +
                " self_rescue_capability_level text," +
                " comprehensive_capability_level text," +
                " evaluation_model_id bigint," +
                " data_source text," +
                " execution_record_id bigint," +
                " create_by text," +
                " create_time timestamptz DEFAULT now()," +
                " update_by text," +
                " update_time timestamptz DEFAULT now()," +
                " is_deleted integer DEFAULT 0" +
                ")");

        // 14. model_execution_record 表 (已存在，但确保结构正确)
        exec(pg, "DROP TABLE IF EXISTS public.model_execution_record CASCADE;" +
                "CREATE TABLE public.model_execution_record (" +
                " id bigserial PRIMARY KEY," +
                " model_id bigint," +
                " execution_code text UNIQUE," +
                " region_ids text," +
                " weight_config_id bigint," +
                " execution_status text," +
                " start_time timestamptz DEFAULT now()," +
                " end_time timestamptz," +
                " error_message text," +
                " result_summary text," +
                " result_ids text," +
                " result_count integer," +
                " create_by text," +
                " org_code text," +
                " year integer" +
                ")");

        pg.commit();
        log.info("所有表创建完成");
    }

    private void truncateAllTables(Connection pg) throws SQLException {
        String[] tables = {
            "evaluation_result", "model_execution_record", "organization", "evaluation_model",
            "algorithm_config", "weight_config", "indicator_weight", "survey_data",
            "community_disaster_reduction_capacity", "model_step", "algorithm_step",
            "step_algorithm", "step_execution_result", "report"
        };

        for (String table : tables) {
            exec(pg, "TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
        }
        pg.commit();
        log.info("所有表已清空");
    }

    private void migrateAllData(Connection mysql, Connection pg) throws SQLException {
        // 按依赖关系迁移表
        migrationCounts.put("organization", migrateOrganization(mysql, pg));
        migrationCounts.put("evaluation_model", migrateEvaluationModel(mysql, pg));
        migrationCounts.put("algorithm_config", migrateAlgorithmConfig(mysql, pg));
        migrationCounts.put("weight_config", migrateWeightConfig(mysql, pg));
        migrationCounts.put("indicator_weight", migrateIndicatorWeight(mysql, pg));
        migrationCounts.put("survey_data", migrateSurveyData(mysql, pg));
        migrationCounts.put("community_disaster_reduction_capacity", migrateCommunityCapacity(mysql, pg));
        migrationCounts.put("model_step", migrateModelStep(mysql, pg));
        migrationCounts.put("algorithm_step", migrateAlgorithmStep(mysql, pg));
        migrationCounts.put("step_algorithm", migrateStepAlgorithm(mysql, pg));
        migrationCounts.put("step_execution_result", migrateStepExecutionResult(mysql, pg));
        migrationCounts.put("report", migrateReport(mysql, pg));
        migrationCounts.put("evaluation_result", migrateEvaluationResult(mysql, pg));
        migrationCounts.put("model_execution_record", migrateModelExecutionRecord(mysql, pg));
    }

    // 以下是各个表的迁移方法的具体实现...
    // 由于代码较长，这里只展示方法签名和部分实现

    private long migrateOrganization(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 organization 表...");
        return migrateTable(mysql, pg, "organization",
            "id, parent_id, code, name, level, data_source, province_name, city_name, county_name, township_name, community_name, create_time, update_time, is_deleted",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateEvaluationModel(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 evaluation_model 表...");
        return migrateTable(mysql, pg, "evaluation_model",
            "id, model_name, model_code, description, version, status, is_default, create_time, update_time, create_by, update_by",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateAlgorithmConfig(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 algorithm_config 表...");
        return migrateTable(mysql, pg, "algorithm_config",
            "id, config_name, description, version, status, create_time",
            "?, ?, ?, ?, ?, ?");
    }

    private long migrateWeightConfig(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 weight_config 表...");
        return migrateTable(mysql, pg, "weight_config",
            "id, config_name, description, orgcode, data_source, create_time, update_time, is_deleted",
            "?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateIndicatorWeight(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 indicator_weight 表...");
        return migrateTable(mysql, pg, "indicator_weight",
            "id, config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time",
            "?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateSurveyData(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 survey_data 表...");
        return migrateTable(mysql, pg, "survey_data",
            "id, region_code, province, city, county, township, year, population, management_staff, risk_assessment, funding_amount, material_value, hospital_beds, firefighters, volunteers, militia_reserve, training_participants, shelter_capacity, create_time, update_time, is_deleted",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateCommunityCapacity(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 community_disaster_reduction_capacity 表...");
        return migrateTable(mysql, pg, "community_disaster_reduction_capacity",
            "id, region_code, province_name, city_name, county_name, township_name, community_name, year, resident_population, last_year_funding_amount, materials_equipment_value, medical_service_count, militia_reserve_count, registered_volunteer_count, last_year_training_participants, last_year_drill_participants, emergency_shelter_capacity, create_time, update_time",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateModelStep(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 model_step 表...");
        return migrateTable(mysql, pg, "model_step",
            "id, model_id, step_name, step_code, step_order, step_type, description, input_variables, output_variables, depends_on, status, create_time",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateAlgorithmStep(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 algorithm_step 表...");
        return migrateTable(mysql, pg, "algorithm_step",
            "id, algorithm_config_id, step_name, step_code, description, input_data, output_data, step_order, status, create_time",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateStepAlgorithm(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 step_algorithm 表...");
        return migrateTable(mysql, pg, "step_algorithm",
            "id, step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateStepExecutionResult(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 step_execution_result 表...");
        return migrateTable(mysql, pg, "step_execution_result",
            "id, execution_record_id, step_id, region_code, step_input, step_output, execution_time, duration_ms, status, error_message",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateReport(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 report 表...");
        return migrateTable(mysql, pg, "report",
            "id, primary_result_id, report_name, report_type, file_path, map_image_path, generate_time",
            "?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateEvaluationResult(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 evaluation_result 表...");
        return migrateTable(mysql, pg, "evaluation_result",
            "id, region_code, region_name, org_code, management_capability_score, support_capability_score, self_rescue_capability_score, comprehensive_capability_score, management_capability_level, support_capability_level, self_rescue_capability_level, comprehensive_capability_level, evaluation_model_id, data_source, execution_record_id, create_by, create_time, update_by, update_time, is_deleted",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    private long migrateModelExecutionRecord(Connection mysql, Connection pg) throws SQLException {
        log.info("迁移 model_execution_record 表...");
        return migrateTable(mysql, pg, "model_execution_record",
            "id, model_id, execution_code, region_ids, weight_config_id, execution_status, start_time, end_time, error_message, result_summary, result_ids, result_count, create_by, org_code, year",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    }

    /**
     * 通用的表迁移方法
     */
    private long migrateTable(Connection mysql, Connection pg, String tableName, String selectFields, String placeholders) throws SQLException {
        String select = "SELECT " + selectFields + " FROM " + tableName;
        // 添加ON CONFLICT DO NOTHING以处理重复主键
        String insert = "INSERT INTO " + tableName + " (" + selectFields + ") VALUES (" + placeholders + ") ON CONFLICT (id) DO NOTHING";

        long count = 0;
        try (Statement st = mysql.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            try { st.setFetchSize(1000); } catch (Exception ignore) {}
            try (ResultSet rs = st.executeQuery(select);
                 PreparedStatement ps = pg.prepareStatement(insert)) {
                int batch = 0;
                while (rs.next()) {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        Object value = rs.getObject(i);
                        if (value == null) {
                            ps.setObject(i, null);
                        } else if (value instanceof Boolean) {
                            // Boolean类型转换为Short (0或1)
                            // MySQL的tinyint(1)被转换为Boolean，需要转换为数字类型
                            ps.setShort(i, (short) (((Boolean) value) ? 1 : 0));
                        } else {
                            // 其他类型直接传递，让JDBC处理
                            ps.setObject(i, value);
                        }
                    }
                    ps.addBatch();
                    batch++;
                    count++;
                    if (batch >= 1000) {
                        ps.executeBatch();
                        pg.commit();
                        batch = 0;
                    }
                }
                if (batch > 0) {
                    ps.executeBatch();
                    pg.commit();
                }
            }
        }
        log.info("迁移 {}: {} 行", tableName, count);
        return count;
    }

    private void alignAllSequences(Connection pg) throws SQLException {
        String[] tables = {
            "organization", "evaluation_model", "algorithm_config", "weight_config",
            "indicator_weight", "survey_data", "community_disaster_reduction_capacity",
            "model_step", "step_algorithm", "step_execution_result", "report",
            "evaluation_result", "model_execution_record"
        };

        for (String table : tables) {
            exec(pg, "SELECT setval(pg_get_serial_sequence('" + table + "','id'), COALESCE((SELECT MAX(id) FROM " + table + "),0)+1, false)");
        }
        pg.commit();
        log.info("所有序列已对齐");
    }

    private void exec(Connection conn, String sql) throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute(sql);
        }
    }

    private void printMigrationSummary() {
        log.info("========================================");
        log.info("迁移完成统计:");
        log.info("========================================");
        long total = 0;
        for (Map.Entry<String, Long> entry : migrationCounts.entrySet()) {
            log.info("{}: {} 行", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }
        log.info("--------------------------------------");
        log.info("总计: {} 行", total);
        log.info("========================================");
    }
}
