package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/enterprise-capacity")
public class EnterpriseDisasterReductionCapacityController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "enterprise_disaster_reduction_capacity_2020";

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) Integer year) {
        try {
            int currentPage = page == null || page < 1 ? 1 : page;
            int pageSize = size == null || size < 1 ? 20 : Math.min(size, 500);

            StringBuilder where = new StringBuilder(" WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            if (year != null) {
                where.append(" AND year = ? ");
                params.add(year);
            }
            if (StringUtils.hasText(orgCode)) {
                where.append(" AND region_code LIKE ? ");
                params.add(orgCode.trim() + "%");
            }

            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
                    new Object[]{TABLE_NAME},
                    String.class
            );
            List<String> preferredColumns = buildDisplayColumns();
            List<String> dataColumns = preferredColumns.stream()
                    .filter(columns::contains)
                    .collect(Collectors.toList());

            Long total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM " + TABLE_NAME + where,
                    params.toArray(),
                    Long.class
            );

            String selectColumns = dataColumns.stream()
                    .map(column -> column + " AS " + toCamelCase(column))
                    .collect(Collectors.joining(", "));

            List<Object> listParams = new ArrayList<>(params);
            listParams.add((currentPage - 1) * pageSize);
            listParams.add(pageSize);

            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT id, " + selectColumns + " FROM " + TABLE_NAME + where +
                            " ORDER BY region_code ASC LIMIT ?, ?",
                    listParams.toArray()
            );

            Map<String, String> labelMap = buildColumnLabelMap();
            List<Map<String, Object>> columnDefs = new ArrayList<>();
            for (String column : dataColumns) {
                String prop = toCamelCase(column);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("prop", prop);
                item.put("label", labelMap.getOrDefault(column, column));
                columnDefs.add(item);
            }

            Map<String, Object> result = new HashMap<>();
            long safeTotal = total == null ? 0L : total;
            result.put("records", records);
            result.put("columns", columnDefs);
            result.put("total", safeTotal);
            result.put("current", currentPage);
            result.put("pages", pageSize == 0 ? 0 : (safeTotal + pageSize - 1) / pageSize);
            result.put("size", pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询企业减灾能力数据失败", e);
            return Result.error("查询企业减灾能力数据失败: " + e.getMessage());
        }
    }

    private String toCamelCase(String value) {
        if (!StringUtils.hasText(value) || !value.contains("_")) {
            return value;
        }
        String[] parts = value.split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!StringUtils.hasText(parts[i])) {
                continue;
            }
            builder.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return builder.toString();
    }

    private Map<String, String> buildColumnLabelMap() {
        Map<String, String> map = new HashMap<>();
        map.put("region_code", "行政区代码");
        map.put("province_name", "省名称");
        map.put("city_name", "市名称");
        map.put("county_name", "县名称");
        map.put("large_excavator_count", "大型挖掘机数量（台）");
        map.put("large_truck_crane_count", "大型汽车式起重机数量（台）");
        map.put("large_loader_count", "大型装载机数量（辆）");
        map.put("large_crawler_bulldozer_count", "大型履带式推土机数量（台）");
        map.put("population", "区域总人口（人）");
        map.put("professional_underwriter_count", "专业核保人员数（人）");
        map.put("last_year_disaster_premium_income", "上一年度涉灾险类保费收入（亿元）");
        map.put("professional_claim_settler_count", "专业理赔人员数（人）");
        map.put("last_year_claim_payout", "上一年度赔付支出（亿元）");
        map.put("last_year_insurance_reinsurance_income", "上一年度保险/再保险业务收入额（亿元）");
        map.put("year", "年份");
        return map;
    }

    private List<String> buildDisplayColumns() {
        return Arrays.asList(
                "region_code", "province_name", "city_name", "county_name",
                "large_excavator_count", "large_truck_crane_count", "large_loader_count", "large_crawler_bulldozer_count",
                "population", "professional_underwriter_count", "last_year_disaster_premium_income",
                "professional_claim_settler_count", "last_year_claim_payout", "last_year_insurance_reinsurance_income",
                "year"
        );
    }

    // ====================== CRUD ======================

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            int rows = jdbcTemplate.update("DELETE FROM " + TABLE_NAME + " WHERE id = ?", id);
            return Result.success(rows > 0);
        } catch (Exception e) {
            log.error("删除企业减灾能力数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的数据");
            }
            String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
            int rows = jdbcTemplate.update(
                    "DELETE FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")",
                    ids.toArray());
            return Result.success(rows > 0);
        } catch (Exception e) {
            log.error("批量删除企业减灾能力数据失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-by-year-org")
    public Result<Long> deleteByYearOrg(@RequestParam Integer year,
                                        @RequestParam(required = false) String orgCode) {
        try {
            StringBuilder sql = new StringBuilder("DELETE FROM " + TABLE_NAME + " WHERE year = ?");
            List<Object> params = new ArrayList<>();
            params.add(year);
            if (StringUtils.hasText(orgCode)) {
                sql.append(" AND region_code LIKE ?");
                params.add(orgCode.trim() + "%");
            }
            int rows = jdbcTemplate.update(sql.toString(), params.toArray());
            return Result.success((long) rows);
        } catch (Exception e) {
            log.error("按年份删除企业减灾能力数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "year", required = false) Integer year) {
        log.info("导入企业减灾能力数据，文件名: {}", file != null ? file.getOriginalFilename() : "null");
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return Result.error("Excel文件中没有工作表");
            }
            Map<String, String> labelToColumn = new LinkedHashMap<>();
            Map<String, String> labelMap = buildColumnLabelMap();
            for (Map.Entry<String, String> entry : labelMap.entrySet()) {
                labelToColumn.put(entry.getValue(), entry.getKey());
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return Result.error("Excel文件中没有表头行");
            }
            Map<Integer, String> colIndexToName = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = ExcelUtil.getCellStringValue(cell);
                if (header != null) {
                    header = header.trim();
                    String colName = labelToColumn.get(header);
                    if (colName != null) {
                        colIndexToName.put(i, colName);
                    }
                }
            }
            if (colIndexToName.isEmpty()) {
                return Result.error("无法识别Excel表头，请检查文件格式");
            }
            int insertCount = 0;
            List<String> errors = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Map<String, Object> rowValues = new LinkedHashMap<>();
                    for (Map.Entry<Integer, String> entry : colIndexToName.entrySet()) {
                        Cell cell = row.getCell(entry.getKey());
                        String val = ExcelUtil.getCellStringValue(cell);
                        if (val != null && !val.trim().isEmpty()) {
                            rowValues.put(entry.getValue(), val.trim());
                        }
                    }
                    if (rowValues.isEmpty()) continue;
                    if (year != null) {
                        rowValues.put("year", year);
                    }
                    if (!rowValues.containsKey("region_code") || rowValues.get("region_code") == null) continue;
                    String columns = String.join(", ", rowValues.keySet());
                    String placeholders = rowValues.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
                    jdbcTemplate.update("INSERT INTO " + TABLE_NAME + " (" + columns + ") VALUES (" + placeholders + ")", rowValues.values().toArray());
                    insertCount++;
                } catch (Exception e) {
                    log.error("导入第{}行失败", i + 1, e);
                    errors.add("第" + (i + 1) + "行导入失败: " + e.getMessage());
                }
            }
            result.put("success", true);
            result.put("insertCount", insertCount);
            result.put("message", "成功导入" + insertCount + "条数据");
            if (!errors.isEmpty()) result.put("errorMessages", errors);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入企业减灾能力数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String ids) {
        try {
            StringBuilder where = new StringBuilder(" WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            if (year != null) {
                where.append(" AND year = ? ");
                params.add(year);
            }
            if (StringUtils.hasText(orgCode)) {
                where.append(" AND region_code LIKE ? ");
                params.add(orgCode.trim() + "%");
            }
            if (StringUtils.hasText(ids)) {
                String[] idArr = ids.split(",");
                where.append(" AND id IN (");
                for (int i = 0; i < idArr.length; i++) {
                    if (i > 0) where.append(",");
                    where.append("?");
                    params.add(Long.parseLong(idArr[i].trim()));
                }
                where.append(") ");
            }
            List<String> dataColumns = buildDisplayColumns();
            String selectColumns = String.join(", ", dataColumns);
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT " + selectColumns + " FROM " + TABLE_NAME + where + " ORDER BY region_code ASC",
                    params.toArray());
            Map<String, String> labelMap = buildColumnLabelMap();
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("企业减灾能力数据");
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < dataColumns.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(labelMap.getOrDefault(dataColumns.get(i), dataColumns.get(i)));
                }
                for (int r = 0; r < records.size(); r++) {
                    Row dataRow = sheet.createRow(r + 1);
                    Map<String, Object> record = records.get(r);
                    for (int c = 0; c < dataColumns.size(); c++) {
                        Cell cell = dataRow.createCell(c);
                        Object val = record.get(dataColumns.get(c));
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }
                workbook.write(bos);
                byte[] bytes = bos.toByteArray();
                String fileName = URLEncoder.encode("企业减灾能力数据.xlsx", StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes);
            }
        } catch (Exception e) {
            log.error("导出企业减灾能力数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
