package com.evaluate.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;

/**
 * SupabaseMigrationRunner
 *
 * Run automatic migration from MySQL to Supabase(PostgreSQL)
 * Enable with property: migration.enabled=true
 * Configure Supabase via: supabase.jdbc.url, supabase.jdbc.user, supabase.jdbc.password
 * Optional: migration.truncate=true to clear target tables first
 * Optional: migration.exitOnFinish=true to terminate app after migration
 */
@Component
@Conditional(SupabaseMigrationRunnerEnabledCondition.class)
public class SupabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SupabaseMigrationRunner.class);

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting MySQL -> Supabase migration");
        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPassword)) {

            pg.setAutoCommit(false);

            createTablesIfMissing(pg);

            if (truncate) {
                exec(pg, "TRUNCATE TABLE evaluation_result RESTART IDENTITY CASCADE");
                exec(pg, "TRUNCATE TABLE model_execution_record RESTART IDENTITY CASCADE");
                pg.commit();
            }

            long er = migrateEvaluationResult(mysql, pg);
            long mr = migrateModelExecutionRecord(mysql, pg);

            alignSequences(pg);
            pg.commit();

            log.info("Migration finished. evaluation_result={}, model_execution_record={}", er, mr);
        } catch (Exception e) {
            log.error("Migration failed", e);
            throw e;
        } finally {
            if (exitOnFinish) {
                log.info("Exiting application after migration as configured.");
                System.exit(0);
            }
        }
    }

    private void createTablesIfMissing(Connection pg) throws SQLException {
        String ddlEvaluationResult = "" +
                "CREATE TABLE IF NOT EXISTS public.evaluation_result (" +
                " id bigserial PRIMARY KEY," +
                " region_code text NOT NULL," +
                " region_name text," +
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
                ")";

        String ddlModelExecution = "" +
                "CREATE TABLE IF NOT EXISTS public.model_execution_record (" +
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
                " create_by text" +
                ")";

        exec(pg, ddlEvaluationResult);
        exec(pg, ddlModelExecution);
        pg.commit();
        log.info("Ensured target tables exist in Supabase");
    }

    private long migrateEvaluationResult(Connection mysql, Connection pg) throws SQLException {
        String select = "SELECT id, region_code, region_name, " +
                "management_capability_score, support_capability_score, self_rescue_capability_score, comprehensive_capability_score, " +
                "management_capability_level, support_capability_level, self_rescue_capability_level, comprehensive_capability_level, " +
                "evaluation_model_id, data_source, execution_record_id, create_by, create_time, update_by, update_time, is_deleted " +
                "FROM evaluation_result";

        String insert = "INSERT INTO evaluation_result (" +
                "id, region_code, region_name, management_capability_score, support_capability_score, self_rescue_capability_score, comprehensive_capability_score, " +
                "management_capability_level, support_capability_level, self_rescue_capability_level, comprehensive_capability_level, " +
                "evaluation_model_id, data_source, execution_record_id, create_by, create_time, update_by, update_time, is_deleted" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO NOTHING";

        long count = 0;
        try (Statement st = mysql.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            try { st.setFetchSize(1000); } catch (Exception ignore) {}
            try (ResultSet rs = st.executeQuery(select);
                 PreparedStatement ps = pg.prepareStatement(insert)) {
                int batch = 0;
                while (rs.next()) {
                    int i = 1;
                    ps.setLong(i++, rs.getLong("id"));
                    ps.setString(i++, rs.getString("region_code"));
                    ps.setString(i++, rs.getString("region_name"));
                    setNumeric(ps, i++, rs.getBigDecimal("management_capability_score"));
                    setNumeric(ps, i++, rs.getBigDecimal("support_capability_score"));
                    setNumeric(ps, i++, rs.getBigDecimal("self_rescue_capability_score"));
                    setNumeric(ps, i++, rs.getBigDecimal("comprehensive_capability_score"));
                    ps.setString(i++, rs.getString("management_capability_level"));
                    ps.setString(i++, rs.getString("support_capability_level"));
                    ps.setString(i++, rs.getString("self_rescue_capability_level"));
                    ps.setString(i++, rs.getString("comprehensive_capability_level"));
                    setNullableLong(ps, i++, rs, "evaluation_model_id");
                    ps.setString(i++, rs.getString("data_source"));
                    setNullableLong(ps, i++, rs, "execution_record_id");
                    ps.setString(i++, rs.getString("create_by"));
                    setTimestamp(ps, i++, rs.getTimestamp("create_time"));
                    ps.setString(i++, rs.getString("update_by"));
                    setTimestamp(ps, i++, rs.getTimestamp("update_time"));
                    ps.setObject(i++, rs.getObject("is_deleted"));

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
        log.info("Migrated evaluation_result rows: {}", count);
        return count;
    }

    private long migrateModelExecutionRecord(Connection mysql, Connection pg) throws SQLException {
        String select = "SELECT id, model_id, execution_code, region_ids, weight_config_id, execution_status, start_time, end_time, " +
                "error_message, result_summary, result_ids, result_count, create_by FROM model_execution_record";

        String insert = "INSERT INTO model_execution_record (" +
                "id, model_id, execution_code, region_ids, weight_config_id, execution_status, start_time, end_time, " +
                "error_message, result_summary, result_ids, result_count, create_by" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO NOTHING";

        long count = 0;
        try (Statement st = mysql.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            try { st.setFetchSize(1000); } catch (Exception ignore) {}
            try (ResultSet rs = st.executeQuery(select);
                 PreparedStatement ps = pg.prepareStatement(insert)) {
                int batch = 0;
                while (rs.next()) {
                    int i = 1;
                    ps.setLong(i++, rs.getLong("id"));
                    setNullableLong(ps, i++, rs, "model_id");
                    ps.setString(i++, rs.getString("execution_code"));
                    ps.setString(i++, rs.getString("region_ids"));
                    setNullableLong(ps, i++, rs, "weight_config_id");
                    ps.setString(i++, rs.getString("execution_status"));
                    setTimestamp(ps, i++, rs.getTimestamp("start_time"));
                    setTimestamp(ps, i++, rs.getTimestamp("end_time"));
                    ps.setString(i++, rs.getString("error_message"));
                    ps.setString(i++, rs.getString("result_summary"));
                    ps.setString(i++, rs.getString("result_ids"));
                    setNullableInt(ps, i++, rs, "result_count");
                    ps.setString(i++, rs.getString("create_by"));

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
        log.info("Migrated model_execution_record rows: {}", count);
        return count;
    }

    private void alignSequences(Connection pg) throws SQLException {
        exec(pg, "SELECT setval(pg_get_serial_sequence('evaluation_result','id'), COALESCE((SELECT MAX(id) FROM evaluation_result),0)+1, false)");
        exec(pg, "SELECT setval(pg_get_serial_sequence('model_execution_record','id'), COALESCE((SELECT MAX(id) FROM model_execution_record),0)+1, false)");
        pg.commit();
    }

    private void exec(Connection pg, String sql) throws SQLException {
        try (Statement s = pg.createStatement()) {
            s.execute(sql);
        }
    }

    private void setNumeric(PreparedStatement ps, int idx, BigDecimal val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.NUMERIC);
        else ps.setBigDecimal(idx, val);
    }

    private void setTimestamp(PreparedStatement ps, int idx, Timestamp ts) throws SQLException {
        if (ts == null) ps.setNull(idx, Types.TIMESTAMP);
        else ps.setTimestamp(idx, ts);
    }

    private void setNullableLong(PreparedStatement ps, int idx, ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        if (rs.wasNull()) ps.setNull(idx, Types.BIGINT); else ps.setLong(idx, v);
    }

    private void setNullableInt(PreparedStatement ps, int idx, ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        if (rs.wasNull()) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, v);
    }
}

