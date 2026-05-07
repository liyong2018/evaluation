package com.evaluate.batch;

import com.evaluate.service.ModelExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "batch.evaluate.2020.enabled", havingValue = "true")
public class Batch2020CountyEvaluationRunner implements ApplicationRunner {

    static final int TARGET_YEAR = 2020;
    static final String CREATE_BY = "batch-2020-county-evaluation";
    static final List<Long> MODEL_IDS = Collections.unmodifiableList(Arrays.asList(3L, 4L, 8L, 11L));

    private static final String COUNTY_QUERY =
            "SELECT code, name, city_name FROM organization "
                    + "WHERE year = 2020 AND level = 3 AND is_deleted = 0 "
                    + "ORDER BY city_name ASC, code ASC";
    private static final String MODEL_11_DEPENDENCY_VALUE = "{\"legacyTownship\":3,\"communityTownship\":8}";
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final JdbcTemplate jdbcTemplate;
    private final ModelExecutionService modelExecutionService;
    private final Path logDirectory;

    @Autowired
    public Batch2020CountyEvaluationRunner(JdbcTemplate jdbcTemplate, ModelExecutionService modelExecutionService) {
        this(jdbcTemplate, modelExecutionService, Paths.get("logs"));
    }

    Batch2020CountyEvaluationRunner(JdbcTemplate jdbcTemplate, ModelExecutionService modelExecutionService,
                                    Path logDirectory) {
        this.jdbcTemplate = jdbcTemplate;
        this.modelExecutionService = modelExecutionService;
        this.logDirectory = logDirectory;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<County> counties = loadCounties();
        ensureLegacyComprehensiveDependencyStrategy();
        BatchLogWriter batchLogWriter = BatchLogWriter.create(logDirectory);
        try {
            write(batchLogWriter, "START", "", "", "", "counties=" + counties.size());
            List<String> countyCodes = counties.stream().map(County::getCode).collect(Collectors.toList());
            clearExistingResults(countyCodes, batchLogWriter);
            runCounties(counties, batchLogWriter);
            write(batchLogWriter, "FINISHED", "", "", "", "counties=" + counties.size());
        } finally {
            batchLogWriter.close();
        }
    }

    private List<County> loadCounties() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(COUNTY_QUERY);
        List<County> counties = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String code = stringValue(row.get("code"));
            if (!StringUtils.hasText(code)) {
                continue;
            }
            counties.add(new County(
                    code,
                    stringValue(row.get("name")),
                    stringValue(row.get("city_name"))
            ));
        }
        return counties;
    }

    private void ensureLegacyComprehensiveDependencyStrategy() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM model_execution_strategy "
                        + "WHERE model_id = ? AND strategy_type = ? AND strategy_key = ? AND status = 1",
                Integer.class,
                11L,
                "data_validation",
                "dependency_models"
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO model_execution_strategy "
                        + "(model_id, strategy_type, strategy_key, strategy_value, sort_order, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                11L,
                "data_validation",
                "dependency_models",
                MODEL_11_DEPENDENCY_VALUE,
                1,
                1
        );
        log.info("已补齐模型11前置依赖配置: {}", MODEL_11_DEPENDENCY_VALUE);
    }

    private void clearExistingResults(List<String> countyCodes, BatchLogWriter batchLogWriter) throws IOException {
        if (countyCodes == null || countyCodes.isEmpty()) {
            return;
        }
        List<Long> executionRecordIds = findExecutionRecordIds(countyCodes);
        write(batchLogWriter, "CLEANUP_FOUND", "", "", "", "executionRecordIds=" + executionRecordIds);
        if (executionRecordIds.isEmpty()) {
            return;
        }
        List<Object[]> chunks = chunks(executionRecordIds, 500);
        for (Object[] chunk : chunks) {
            String placeholders = placeholders(chunk.length);
            int deletedResults = jdbcTemplate.update(
                    "DELETE FROM evaluation_result WHERE execution_record_id IN (" + placeholders + ")",
                    chunk
            );
            int deletedRecords = jdbcTemplate.update(
                    "DELETE FROM model_execution_record WHERE id IN (" + placeholders + ")",
                    chunk
            );
            write(batchLogWriter, "CLEANUP_DELETED", "", "", "",
                    "deletedResults=" + deletedResults + ", deletedRecords=" + deletedRecords);
        }
    }

    private List<Long> findExecutionRecordIds(List<String> countyCodes) {
        List<Object> params = new ArrayList<>();
        params.add(TARGET_YEAR);
        params.addAll(MODEL_IDS);
        params.addAll(countyCodes);
        String sql = "SELECT id FROM model_execution_record "
                + "WHERE year = ? "
                + "AND model_id IN (" + placeholders(MODEL_IDS.size()) + ") "
                + "AND org_code IN (" + placeholders(countyCodes.size()) + ") "
                + "ORDER BY id ASC";
        return jdbcTemplate.queryForList(sql, params.toArray(), Long.class);
    }

    private void runCounties(List<County> counties, BatchLogWriter batchLogWriter) throws IOException {
        for (County county : counties) {
            if (!hasSurveyData(county.getCode()) || !hasCommunityData(county.getCode())) {
                write(batchLogWriter, "SKIPPED_NO_DATA", county, null, null, "missing survey or community data");
                log.warn("SKIPPED_NO_DATA countyCode={}, countyName={}", county.getCode(), county.getName());
                continue;
            }
            for (Long modelId : MODEL_IDS) {
                runModel(county, modelId, batchLogWriter);
            }
        }
    }

    private void runModel(County county, Long modelId, BatchLogWriter batchLogWriter) throws IOException {
        long start = System.nanoTime();
        try {
            Map<String, Object> result = modelExecutionService.executeModel(
                    modelId,
                    Collections.singletonList(county.getCode()),
                    null,
                    TARGET_YEAR,
                    county.getCode(),
                    CREATE_BY
            );
            Long executionRecordId = asLong(result == null ? null : result.get("executionRecordId"));
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
            write(batchLogWriter, "SUCCESS", county, modelId, executionRecordId, "durationMs=" + millis);
            log.info("SUCCESS countyCode={}, countyName={}, modelId={}, executionRecordId={}, durationMs={}",
                    county.getCode(), county.getName(), modelId, executionRecordId, millis);
        } catch (Exception e) {
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
            write(batchLogWriter, "FAILED", county, modelId, null,
                    "durationMs=" + millis + ", error=" + sanitize(e.getMessage()));
            log.error("FAILED countyCode={}, countyName={}, modelId={}", county.getCode(), county.getName(), modelId, e);
        }
    }

    private boolean hasSurveyData(String countyCode) {
        return countByPrefix("survey_data", countyCode) > 0;
    }

    private boolean hasCommunityData(String countyCode) {
        return countByPrefix("community_disaster_reduction_capacity", countyCode) > 0;
    }

    private long countByPrefix(String tableName, String countyCode) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM " + tableName + " WHERE year = ? AND region_code LIKE ?",
                Long.class,
                TARGET_YEAR,
                countyCode + "%"
        );
        return count == null ? 0L : count;
    }

    private void write(BatchLogWriter writer, String status, County county, Long modelId,
                       Long executionRecordId, String message) throws IOException {
        write(writer, status, county.getCityName(), county.getCode(), county.getName(),
                "modelId=" + value(modelId)
                        + ", executionRecordId=" + value(executionRecordId)
                        + ", " + message);
    }

    private void write(BatchLogWriter writer, String status, String cityName, String countyCode,
                       String countyName, String message) throws IOException {
        writer.write(status, cityName, countyCode, countyName, message);
    }

    private static String placeholders(int size) {
        return String.join(",", Collections.nCopies(size, "?"));
    }

    private static List<Object[]> chunks(List<Long> values, int chunkSize) {
        List<Object[]> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i += chunkSize) {
            List<Long> chunk = values.subList(i, Math.min(i + chunkSize, values.size()));
            result.add(chunk.toArray(new Object[0]));
        }
        return result;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String && StringUtils.hasText((String) value)) {
            return Long.parseLong(((String) value).trim());
        }
        return null;
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String sanitize(String message) {
        if (message == null) {
            return "";
        }
        return message.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
    }

    private static final class County {
        private final String code;
        private final String name;
        private final String cityName;

        private County(String code, String name, String cityName) {
            this.code = code;
            this.name = name;
            this.cityName = cityName;
        }

        private String getCode() {
            return code;
        }

        private String getName() {
            return name;
        }

        private String getCityName() {
            return cityName;
        }
    }

    private static final class BatchLogWriter implements AutoCloseable {
        private final Path path;
        private final BufferedWriter writer;

        private BatchLogWriter(Path path, BufferedWriter writer) {
            this.path = path;
            this.writer = writer;
        }

        private static BatchLogWriter create(Path logDir) throws IOException {
            Files.createDirectories(logDir);
            Path path = logDir.resolve("batch-evaluation-2020-county-"
                    + LocalDateTime.now().format(LOG_TIME_FORMATTER) + ".log");
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            BatchLogWriter batchLogWriter = new BatchLogWriter(path, writer);
            batchLogWriter.write("status", "cityName", "countyCode", "countyName", "message");
            log.info("2020年区县批量评估日志文件: {}", path.toAbsolutePath());
            return batchLogWriter;
        }

        private void write(String status, String cityName, String countyCode, String countyName, String message)
                throws IOException {
            writer.write(String.join("\t",
                    timestamp(),
                    sanitize(status),
                    sanitize(cityName),
                    sanitize(countyCode),
                    sanitize(countyName),
                    sanitize(message)));
            writer.newLine();
            writer.flush();
        }

        @Override
        public void close() throws IOException {
            writer.close();
            log.info("2020年区县批量评估日志写入完成: {}", path.toAbsolutePath());
        }

        private static String timestamp() {
            return LocalDateTime.now().toString();
        }
    }
}
