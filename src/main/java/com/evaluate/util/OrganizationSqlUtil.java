package com.evaluate.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.entity.Organization;
import com.evaluate.mapper.GrassrootsOrganizationMapper;
import com.evaluate.mapper.OrganizationMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

/**
 * 组织机构 SQL 生成工具
 * 用于生成任意年份组织机构数据的 SQL 脚本
 */
@Slf4j
@Component
public class OrganizationSqlUtil {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private GrassrootsOrganizationMapper grassrootsOrganizationMapper;

    private static final int LEVEL_TOWNSHIP = 4;
    private static final int BASELINE_YEAR = 2020;

    /**
     * 生成任意年份乡镇数据变更 SQL（对比 2020 年基准数据）
     */
    public SqlScriptResult generateTownshipChangeSql(String geojsonPath, Integer year) throws IOException {
        log.info("开始生成 {} 年乡镇数据变更 SQL，文件：{}", year, geojsonPath);

        SqlScriptResult result = new SqlScriptResult();
        StringBuilder sql = new StringBuilder();

        // 添加文件头注释
        sql.append("-- ============================================\n");
        sql.append("-- ").append(year).append(" 年乡镇组织机构数据变更 SQL\n");
        sql.append("-- 生成时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sql.append("-- 目标年份：").append(year).append("\n");
        sql.append("-- 基准年份：").append(BASELINE_YEAR).append("\n");
        sql.append("-- ============================================\n\n");

        // 读取并解析 GeoJSON 文件
        String content = FileUtils.readFileToString(Paths.get(geojsonPath).toFile(), StandardCharsets.UTF_8);
        Map<String, Object> geojson = parseGeojson(content);

        // 获取 2020 年乡镇基准数据（用于对比变化）
        Map<String, OrgRecord> baselineTownshipData = getBaselineTownshipData();
        log.info("获取到 2020 年乡镇基准数据 {} 条", baselineTownshipData.size());

        // 备份 2020 年基准数据 SQL
        sql.append("-- ============================================\n");
        sql.append("-- 第一部分：备份 2020 年基准数据（只读，不执行变更）\n");
        sql.append("-- ============================================\n\n");
        result.setBackupSql(generateBackupSql(baselineTownshipData, sql));

        // 解析 GeoJSON 中的乡镇数据
        Map<String, OrgRecord> geojsonData = parseTownshipFeatures(geojson);
        log.info("从 GeoJSON 解析到乡镇数据 {} 条", geojsonData.size());

        // 对比生成变更 SQL（对比 2020 年基准数据，找出真正的变化）
        sql.append("-- ============================================\n");
        sql.append("-- 第二部分：数据变更 SQL\n");
        sql.append("-- ============================================\n\n");
        generateChangeSql(baselineTownshipData, geojsonData, year, sql, result);

        result.setSql(sql.toString());
        return result;
    }

    /**
     * 生成 2024 年乡镇数据变更 SQL（兼容旧方法）
     */
    @Deprecated
    public SqlScriptResult generate2024TownshipChangeSql(String geojsonPath, Integer year) throws IOException {
        return generateTownshipChangeSql(geojsonPath, year);
    }

    /**
     * 解析 GeoJSON 内容
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGeojson(String content) throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(content, Map.class);
    }

    /**
     * 获取 2020 年基准数据（所有级别，用于查找父级）
     */
    private Map<String, OrgRecord> getTownshipBaselineData() {
        Map<String, OrgRecord> result = new HashMap<>();

        // 优先从 grassroots_organization 表获取 2020 年基准数据
        QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
        wrapper.eq("year", BASELINE_YEAR);
        wrapper.eq("is_deleted", 0);

        List<GrassrootsOrganization> organizations = grassrootsOrganizationMapper.selectList(wrapper);
        for (GrassrootsOrganization org : organizations) {
            OrgRecord record = new OrgRecord();
            record.setCode(org.getCode());
            record.setName(org.getName());
            record.setLevel(org.getLevel());
            record.setProvinceName(org.getProvinceName());
            record.setCityName(org.getCityName());
            record.setCountyName(org.getCountyName());
            record.setTownshipName(org.getTownshipName());
            record.setParentId(org.getParentId());
            record.setId(org.getId());
            result.put(org.getCode(), record);
        }

        // 如果 grassroots_organization 表没有数据，尝试从 organization 表获取
        if (result.isEmpty()) {
            QueryWrapper<Organization> orgWrapper = new QueryWrapper<>();
            orgWrapper.eq("year", BASELINE_YEAR);
            orgWrapper.eq("is_deleted", 0);

            List<Organization> orgs = organizationMapper.selectList(orgWrapper);
            for (Organization org : orgs) {
                OrgRecord record = new OrgRecord();
                record.setCode(org.getCode());
                record.setName(org.getName());
                record.setLevel(org.getLevel());
                record.setProvinceName(org.getProvinceName());
                record.setCityName(org.getCityName());
                record.setCountyName(org.getCountyName());
                record.setTownshipName(org.getTownshipName());
                record.setParentId(org.getParentId());
                record.setId(org.getId());
                result.put(org.getCode(), record);
            }
        }

        log.info("获取到 2020 年基准数据共 {} 条（所有级别）", result.size());
        return result;
    }

    /**
     * 获取 2020 年乡镇基准数据（只获取 level=4 的乡镇数据，用于对比变化）
     */
    private Map<String, OrgRecord> getBaselineTownshipData() {
        Map<String, OrgRecord> result = new HashMap<>();

        // 优先从 grassroots_organization 表获取 2020 年乡镇数据
        QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
        wrapper.eq("year", BASELINE_YEAR);
        wrapper.eq("level", LEVEL_TOWNSHIP);
        wrapper.eq("is_deleted", 0);

        List<GrassrootsOrganization> organizations = grassrootsOrganizationMapper.selectList(wrapper);
        for (GrassrootsOrganization org : organizations) {
            OrgRecord record = new OrgRecord();
            record.setCode(org.getCode());
            record.setName(org.getName());
            record.setLevel(org.getLevel());
            record.setParentId(org.getParentId());
            record.setId(org.getId());
            result.put(org.getCode(), record);
        }

        // 如果 grassroots_organization 表没有数据，尝试从 organization 表获取
        if (result.isEmpty()) {
            QueryWrapper<Organization> orgWrapper = new QueryWrapper<>();
            orgWrapper.eq("year", BASELINE_YEAR);
            orgWrapper.eq("level", LEVEL_TOWNSHIP);
            orgWrapper.eq("is_deleted", 0);

            List<Organization> orgs = organizationMapper.selectList(orgWrapper);
            for (Organization org : orgs) {
                OrgRecord record = new OrgRecord();
                record.setCode(org.getCode());
                record.setName(org.getName());
                record.setLevel(org.getLevel());
                record.setParentId(org.getParentId());
                record.setId(org.getId());
                result.put(org.getCode(), record);
            }
        }

        log.info("获取到 2020 年乡镇基准数据共 {} 条", result.size());
        return result;
    }

    /**
     * 获取当前年份乡镇已有数据
     */
    private Map<String, OrgRecord> getCurrentYearTownshipData(Integer year) {
        Map<String, OrgRecord> result = new HashMap<>();

        if (year == null || year <= BASELINE_YEAR) {
            return result;
        }

        QueryWrapper<Organization> wrapper = new QueryWrapper<>();
        wrapper.eq("year", year);
        wrapper.eq("level", LEVEL_TOWNSHIP);
        wrapper.eq("is_deleted", 0);

        List<Organization> organizations = organizationMapper.selectList(wrapper);
        for (Organization org : organizations) {
            OrgRecord record = new OrgRecord();
            record.setCode(org.getCode());
            record.setName(org.getName());
            record.setLevel(org.getLevel());
            record.setParentId(org.getParentId());
            record.setId(org.getId());
            result.put(org.getCode(), record);
        }

        return result;
    }

    /**
     * 解析 GeoJSON 中的乡镇要素
     */
    @SuppressWarnings("unchecked")
    private Map<String, OrgRecord> parseTownshipFeatures(Map<String, Object> geojson) {
        Map<String, OrgRecord> result = new HashMap<>();
        List<Map<String, Object>> features = (List<Map<String, Object>>) geojson.get("features");

        if (features == null || features.isEmpty()) {
            log.warn("GeoJSON 文件中没有找到任何要素");
            return result;
        }

        for (Map<String, Object> feature : features) {
            Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
            if (properties == null) {
                continue;
            }

            OrgRecord record = new OrgRecord();

            // 获取行政区划代码
            String code = getStringValue(properties, "code");
            if (!StringUtils.hasText(code)) {
                code = getStringValue(properties, "codery");
            }
            if (!StringUtils.hasText(code)) {
                code = getStringValue(properties, "fxpc_xzqhbmd_sjgl");
            }

            if (!StringUtils.hasText(code)) {
                log.warn("跳过缺少 code 字段的记录");
                continue;
            }

            record.setCode(code);

            // 获取机构名称
            String name = getStringValue(properties, "dwmc");
            record.setName(name);

            // 获取行政区划名称
            String provinceName = getStringValue(properties, "dzsheng");
            String cityName = getStringValue(properties, "dzshi");
            String countyName = getStringValue(properties, "dzxian");
            String townshipName = getStringValue(properties, "dzxiang");

            if (!StringUtils.hasText(provinceName)) {
                provinceName = getStringValue(properties, "fxpc_xzqhbma_sjgl");
            }
            if (!StringUtils.hasText(cityName)) {
                cityName = getStringValue(properties, "fxpc_xzqhbmb_sjgl");
            }
            if (!StringUtils.hasText(countyName)) {
                countyName = getStringValue(properties, "fxpc_xzqhbmc_sjgl");
            }
            if (!StringUtils.hasText(townshipName)) {
                townshipName = name;
            }

            record.setProvinceName(provinceName);
            record.setCityName(cityName);
            record.setCountyName(countyName);
            record.setTownshipName(townshipName);
            record.setLevel(LEVEL_TOWNSHIP);

            result.put(code, record);
        }

        return result;
    }

    /**
     * 生成备份 SQL（只备份 2020 年乡镇数据）
     */
    private String generateBackupSql(Map<String, OrgRecord> data, StringBuilder sql) {
        sql.append("-- 备份 organization 表中的 2020 年乡镇数据\n");
        sql.append("-- 执行以下命令可备份数据到文件：\n");
        sql.append("-- mysqldump -u 用户名 -p 数据库名 organization --where=\"year=2020 AND level=4\" > backup_organization_2020_township.sql\n\n");

        sql.append("-- 备份 grassroots_organization 表中的 2020 年乡镇数据\n");
        sql.append("-- mysqldump -u 用户名 -p 数据库名 grassroots_organization --where=\"year=2020 AND level=4\" > backup_grassroots_organization_2020_township.sql\n\n");

        // 只备份乡镇级别的数据（level=4）
        sql.append("-- ============================================\n");
        sql.append("-- 2020 年 organization 表备份数据（乡镇级别）\n");
        sql.append("-- ============================================\n");

        for (OrgRecord record : data.values()) {
            if (record.getLevel() == LEVEL_TOWNSHIP) {
                sql.append("INSERT INTO `organization` (`parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`) VALUES\n");
                sql.append(String.format("  (%s, '%s', '%s', %d, %d, '%s', '%s', '%s', '%s', 1, 0);\n",
                        record.getParentId() != null ? record.getParentId() : "NULL",
                        escapeSql(record.getCode()),
                        escapeSql(record.getName()),
                        record.getLevel(),
                        BASELINE_YEAR,
                        escapeSql(record.getProvinceName()),
                        escapeSql(record.getCityName()),
                        escapeSql(record.getCountyName()),
                        escapeSql(record.getTownshipName())));
            }
        }
        sql.append("\n");

        return sql.toString();
    }

    /**
     * 生成变更 SQL（对比 2020 年基准数据和 GeoJSON 数据）
     */
    private void generateChangeSql(
            Map<String, OrgRecord> baselineData,
            Map<String, OrgRecord> geojsonData,
            Integer year,
            StringBuilder sql,
            SqlScriptResult result) {

        List<OrgRecord> added = new ArrayList<>();
        List<OrgRecord> removed = new ArrayList<>();
        List<ChangeRecord> changed = new ArrayList<>();

        // 当前时间
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 1. 新增（GeoJSON 中有但 2020 年基准没有的）
        sql.append("-- ============================================\n");
        sql.append("-- 2.1 新增数据（GeoJSON 中有但 2020 年基准没有的）\n");
        sql.append("-- 说明：乡镇/街道数据只插入 grassroots_organization 表\n");
        sql.append("--       organization 表存储省市县数据（来自 2020 年基准，变化概率低）\n");
        sql.append("-- ============================================\n\n");

        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (!baselineData.containsKey(code)) {
                added.add(geojsonRecord);

                // 查找父级 ID（区县 ID）
                Long parentId = findParentId(baselineData, geojsonRecord);
                // 查找区县 ID（前 6 位）
                Long countyId = findCountyId(baselineData, geojsonRecord);

                sql.append("-- 新增：").append(code).append(" - ").append(geojsonRecord.getName()).append("\n");
                // 只插入 grassroots_organization 表（乡镇/街道数据）
                sql.append("INSERT INTO `grassroots_organization` (`county_id`, `parent_id`, `code`, `name`, `level`, `year`, `province_name`, `city_name`, `county_name`, `township_name`, `is_baseline`, `is_deleted`, `create_time`, `update_time`) VALUES\n");
                sql.append(String.format("  (%s, %s, '%s', '%s', %d, %d, '%s', '%s', '%s', '%s', 0, 0, '%s', '%s');\n\n",
                        countyId != null ? countyId : "NULL",
                        parentId != null ? parentId : "NULL",
                        escapeSql(code),
                        escapeSql(geojsonRecord.getName()),
                        geojsonRecord.getLevel(),
                        year,
                        escapeSql(geojsonRecord.getProvinceName()),
                        escapeSql(geojsonRecord.getCityName()),
                        escapeSql(geojsonRecord.getCountyName()),
                        escapeSql(geojsonRecord.getTownshipName()),
                        now, now));
            }
        }

        // 2. 删除（2020 年基准有但 GeoJSON 中没有的）
        sql.append("-- ============================================\n");
        sql.append("-- 2.2 删除数据（2020 年基准有但 GeoJSON 中没有的）\n");
        sql.append("-- ============================================\n\n");

        for (Map.Entry<String, OrgRecord> entry : baselineData.entrySet()) {
            String code = entry.getKey();
            if (!geojsonData.containsKey(code)) {
                removed.add(entry.getValue());

                sql.append("-- 删除：").append(code).append(" - ").append(entry.getValue().getName()).append("\n");
                sql.append("UPDATE `grassroots_organization` SET `is_deleted` = 1 WHERE `code` = '").append(escapeSql(code)).append("' AND `year` = ").append(year).append(";\n\n");
            }
        }

        // 3. 变更（代码相同但名称发生变化的）
        sql.append("-- ============================================\n");
        sql.append("-- 2.3 变更数据（名称发生变化的）\n");
        sql.append("-- ============================================\n\n");

        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (baselineData.containsKey(code)) {
                OrgRecord baselineRecord = baselineData.get(code);
                if (!Objects.equals(geojsonRecord.getName(), baselineRecord.getName())) {
                    changed.add(new ChangeRecord(code, baselineRecord.getName(), geojsonRecord.getName()));

                    sql.append("-- 变更：").append(code).append(" - ").append(baselineRecord.getName()).append(" -> ").append(geojsonRecord.getName()).append("\n");
                    sql.append("UPDATE `grassroots_organization` SET `name` = '").append(escapeSql(geojsonRecord.getName()))
                            .append("', `township_name` = '").append(escapeSql(geojsonRecord.getName()))
                            .append("', `update_time` = '").append(now).append("'")
                            .append(" WHERE `code` = '").append(escapeSql(code)).append("' AND `year` = ").append(year).append(";\n\n");
                }
            }
        }

        // 统计信息
        result.setAddedCount(added.size());
        result.setRemovedCount(removed.size());
        result.setChangedCount(changed.size());
    }

    /**
     * 查找父级 ID（根据区县代码查找区县 ID）
     */
    private Long findParentId(Map<String, OrgRecord> baselineData, OrgRecord record) {
        if (record.getCode().length() >= 6) {
            String countyCode = record.getCode().substring(0, 6);
            OrgRecord county = baselineData.get(countyCode);
            if (county != null) {
                return county.getId();
            }
        }
        return null;
    }

    /**
     * 查找区县 ID（根据区县代码查找区县 ID，用于 county_id 字段）
     */
    private Long findCountyId(Map<String, OrgRecord> baselineData, OrgRecord record) {
        if (record.getCode().length() >= 6) {
            String countyCode = record.getCode().substring(0, 6);
            OrgRecord county = baselineData.get(countyCode);
            if (county != null) {
                return county.getId();
            }
        }
        return null;
    }

    /**
     * 转义 SQL 字符串
     */
    private String escapeSql(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }

    /**
     * 从 Map 中获取字符串值
     */
    @SuppressWarnings("unchecked")
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 组织机构记录
     */
    @Data
    public static class OrgRecord {
        private Long id;
        private String code;
        private String name;
        private Integer level;
        private Long parentId;
        private String provinceName;
        private String cityName;
        private String countyName;
        private String townshipName;
    }

    /**
     * 变更记录
     */
    @Data
    public static class ChangeRecord {
        private String code;
        private String oldName;
        private String newName;

        public ChangeRecord(String code, String oldName, String newName) {
            this.code = code;
            this.oldName = oldName;
            this.newName = newName;
        }
    }

    /**
     * SQL 脚本结果
     */
    @Data
    public static class SqlScriptResult {
        private String sql;
        private String backupSql;
        private int addedCount;
        private int removedCount;
        private int changedCount;

        public String getSummary() {
            return String.format("新增：%d, 删除：%d, 变更：%d", addedCount, removedCount, changedCount);
        }
    }
}
