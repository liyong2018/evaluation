package com.evaluate.controller;

import com.evaluate.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                    "SELECT " + selectColumns + " FROM " + TABLE_NAME + where +
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
}
