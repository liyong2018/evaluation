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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.commons.io.FileUtils;

/**
 * 2024 年组织机构数据导入工具
 * 专门用于从 2024 年 GeoJSON 文件导入乡镇数据，并与 2020 年基准数据对比生成变更
 */
@Slf4j
@Component
public class GeojsonImportUtil {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private GrassrootsOrganizationMapper grassrootsOrganizationMapper;

    private static final int LEVEL_TOWNSHIP = 4;
    private static final int BASELINE_YEAR = 2020;

    /**
     * 组织机构记录
     */
    @Data
    public static class OrgRecord {
        private String code;
        private String name;
        private Integer level;
        private String provinceName;
        private String cityName;
        private String countyName;
        private String townshipName;
    }

    /**
     * 从 GeoJSON 文件导入乡镇数据（对比上一年数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult import2024Townships(String geojsonPath, Integer year) throws IOException {
        log.info("开始导入 {} 年乡镇数据，文件：{}, 年份：{}", year, geojsonPath, year);

        ImportResult result = new ImportResult();

        // 读取 GeoJSON 文件
        String content = FileUtils.readFileToString(Paths.get(geojsonPath).toFile(), StandardCharsets.UTF_8);
        Map<String, Object> geojson = parseGeojson(content);

        // 获取上一年数据（从 grassroots_organization 表）
        Integer prevYear = year - 1;
        Map<String, OrgRecord> prevYearData = getPrevYearTownshipData(prevYear);
        log.info("获取到 {} 年乡镇数据 {} 条", prevYear, prevYearData.size());

        // 解析 GeoJSON 中的乡镇数据
        Map<String, OrgRecord> geojsonData = parseTownshipFeatures(geojson);
        log.info("从 GeoJSON 解析到乡镇数据 {} 条", geojsonData.size());

        // 对比生成变更（对比上一年数据）
        compareTownshipChangesWithPrevYear(prevYearData, geojsonData, year, result);
        log.info("数据对比完成：新增={}, 删除={}, 变更={}",
                result.getAddedCount(), result.getRemovedCount(), result.getChangedCount());

        // 应用变更到数据库
        applyTownshipChangesToDatabase(result, year);

        return result;
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
     * 获取上一年乡镇数据（从 grassroots_organization 表）
     */
    private Map<String, OrgRecord> getPrevYearTownshipData(Integer year) {
        Map<String, OrgRecord> result = new HashMap<>();

        QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
        wrapper.eq("year", year);
        wrapper.eq("level", LEVEL_TOWNSHIP);
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
            result.put(org.getCode(), record);
        }

        return result;
    }

    /**
     * 获取 2020 年乡镇基准数据（从 grassroots_organization 表）
     */
    @Deprecated
    private Map<String, OrgRecord> getBaselineTownshipData() {
        return getPrevYearTownshipData(BASELINE_YEAR);
    }

    /**
     * 获取 2020 年乡镇基准数据（废弃，保留用于兼容）
     */
    @Deprecated
    private Map<String, OrgRecord> getTownshipBaselineData() {
        return getBaselineTownshipData();
    }

    /**
     * 对比乡镇数据生成变更（对比当前年份）
     */
    @Deprecated
    private void compareTownshipChanges(
            Map<String, OrgRecord> baselineData,
            Map<String, OrgRecord> currentYearData,
            Map<String, OrgRecord> geojsonData,
            Integer year,
            ImportResult result) {

        result.setYear(year);
        result.setDataType("township");

        // 1. 查找新增：geojson 中有但当前年份没有的
        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (!currentYearData.containsKey(code)) {
                result.getAdded().add(geojsonRecord);
                log.info("新增：{} - {}", code, geojsonRecord.getName());
            }
        }

        // 2. 查找删除：当前年份有但 geojson 中没有的
        for (Map.Entry<String, OrgRecord> entry : currentYearData.entrySet()) {
            String code = entry.getKey();
            if (!geojsonData.containsKey(code)) {
                result.getRemoved().add(entry.getValue());
                log.info("删除：{} - {}", code, entry.getValue().getName());
            }
        }

        // 3. 查找变更：geojson 和当前年份都有，但名称不同
        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (currentYearData.containsKey(code)) {
                OrgRecord currentRecord = currentYearData.get(code);
                if (!Objects.equals(geojsonRecord.getName(), currentRecord.getName())) {
                    ChangeRecord change = new ChangeRecord();
                    change.setCode(code);
                    change.setOldName(currentRecord.getName());
                    change.setNewName(geojsonRecord.getName());
                    result.getChanged().add(change);
                    log.info("名称变更：{} - {} -> {}", code, currentRecord.getName(), geojsonRecord.getName());
                }
            }
        }
    }

    /**
     * 对比乡镇数据生成变更（对比上一年数据）
     */
    private void compareTownshipChangesWithPrevYear(
            Map<String, OrgRecord> prevYearData,
            Map<String, OrgRecord> geojsonData,
            Integer year,
            ImportResult result) {

        result.setYear(year);
        result.setDataType("township");

        // 1. 查找新增：GeoJSON 中有但上一年没有的
        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (!prevYearData.containsKey(code)) {
                result.getAdded().add(geojsonRecord);
                log.info("新增：{} - {}", code, geojsonRecord.getName());
            }
        }

        // 2. 查找删除：上一年有但 GeoJSON 中没有的
        for (Map.Entry<String, OrgRecord> entry : prevYearData.entrySet()) {
            String code = entry.getKey();
            if (!geojsonData.containsKey(code)) {
                result.getRemoved().add(entry.getValue());
                log.info("删除：{} - {}", code, entry.getValue().getName());
            }
        }

        // 3. 查找变更：代码相同但名称发生变化的
        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (prevYearData.containsKey(code)) {
                OrgRecord prevYearRecord = prevYearData.get(code);
                if (!Objects.equals(geojsonRecord.getName(), prevYearRecord.getName())) {
                    ChangeRecord change = new ChangeRecord();
                    change.setCode(code);
                    change.setOldName(prevYearRecord.getName());
                    change.setNewName(geojsonRecord.getName());
                    result.getChanged().add(change);
                    log.info("名称变更：{} - {} -> {}", code, prevYearRecord.getName(), geojsonRecord.getName());
                }
            }
        }
    }

    /**
     * 对比乡镇数据生成变更（对比 2020 年基准数据）
     */
    @Deprecated
    private void compareTownshipChangesWithBaseline(
            Map<String, OrgRecord> baselineData,
            Map<String, OrgRecord> geojsonData,
            Integer year,
            ImportResult result) {

        result.setYear(year);
        result.setDataType("township");

        // 1. 查找新增：GeoJSON 中有但 2020 年基准没有的
        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (!baselineData.containsKey(code)) {
                result.getAdded().add(geojsonRecord);
                log.info("新增：{} - {}", code, geojsonRecord.getName());
            }
        }

        // 2. 查找删除：2020 年基准有但 GeoJSON 中没有的
        for (Map.Entry<String, OrgRecord> entry : baselineData.entrySet()) {
            String code = entry.getKey();
            if (!geojsonData.containsKey(code)) {
                result.getRemoved().add(entry.getValue());
                log.info("删除：{} - {}", code, entry.getValue().getName());
            }
        }

        // 3. 查找变更：代码相同但名称发生变化的
        for (Map.Entry<String, OrgRecord> entry : geojsonData.entrySet()) {
            String code = entry.getKey();
            OrgRecord geojsonRecord = entry.getValue();

            if (baselineData.containsKey(code)) {
                OrgRecord baselineRecord = baselineData.get(code);
                if (!Objects.equals(geojsonRecord.getName(), baselineRecord.getName())) {
                    ChangeRecord change = new ChangeRecord();
                    change.setCode(code);
                    change.setOldName(baselineRecord.getName());
                    change.setNewName(geojsonRecord.getName());
                    result.getChanged().add(change);
                    log.info("名称变更：{} - {} -> {}", code, baselineRecord.getName(), geojsonRecord.getName());
                }
            }
        }
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

            // 获取行政区划代码（优先顺序：code > codery > fxpc_xzqhbmd_sjgl）
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

            // 获取行政区划名称（优先使用 dz 开头字段）
            String provinceName = getStringValue(properties, "dzsheng");
            String cityName = getStringValue(properties, "dzshi");
            String countyName = getStringValue(properties, "dzxian");
            String townshipName = getStringValue(properties, "dzxiang");

            // 如果 dz 开头字段为空，使用 fxpc 字段
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
                townshipName = name; // 使用 dwmc 作为乡镇名称
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
     * 应用乡镇变更到数据库（只更新 grassroots_organization 表）
     */
    @Transactional(rollbackFor = Exception.class)
    private void applyTownshipChangesToDatabase(ImportResult result, Integer year) {
        LocalDateTime now = LocalDateTime.now();

        // 处理新增（只插入 grassroots_organization 表）
        for (OrgRecord record : result.getAdded()) {
            try {
                // 插入 grassroots_organization 表
                GrassrootsOrganization grassroots = new GrassrootsOrganization();
                grassroots.setCode(record.getCode());
                grassroots.setName(record.getName());
                grassroots.setLevel(LEVEL_TOWNSHIP);
                grassroots.setYear(year);
                grassroots.setIsBaseline(0);
                grassroots.setCreateTime(now);
                grassroots.setUpdateTime(now);
                grassroots.setIsDeleted(0);
                grassroots.setTownshipName(record.getName());
                grassroots.setProvinceName(record.getProvinceName());
                grassroots.setCityName(record.getCityName());
                grassroots.setCountyName(record.getCountyName());

                // 设置父级 ID 和区县 ID（根据代码前 6 位查找区县）
                // 优先使用 targetYear 查找，如果找不到再使用 BASELINE_YEAR
                if (record.getCode().length() >= 6) {
                    String countyCode = record.getCode().substring(0, 6);
                    Organization county = findOrganizationByCode(countyCode, year, 3);
                    if (county == null) {
                        county = findOrganizationByCode(countyCode, BASELINE_YEAR, 3);
                    }
                    if (county != null) {
                        grassroots.setParentId(county.getId());
                        grassroots.setCountyId(county.getId());
                    }
                }

                grassrootsOrganizationMapper.insert(grassroots);
                log.info("插入 grassroots_organization 记录：{}", grassroots.getCode());

            } catch (Exception e) {
                log.error("插入记录失败：{}", record.getCode(), e);
            }
        }

        // 处理删除（软删除，只更新 grassroots_organization 表）
        for (OrgRecord record : result.getRemoved()) {
            try {
                QueryWrapper<GrassrootsOrganization> gwWrapper = new QueryWrapper<>();
                gwWrapper.eq("code", record.getCode());
                gwWrapper.eq("year", year);
                gwWrapper.eq("is_deleted", 0);

                GrassrootsOrganization gwExisting = grassrootsOrganizationMapper.selectOne(gwWrapper);
                if (gwExisting != null) {
                    gwExisting.setIsDeleted(1);
                    gwExisting.setUpdateTime(now);
                    grassrootsOrganizationMapper.updateById(gwExisting);
                    log.info("软删除 grassroots_organization 记录：{}", record.getCode());
                }

            } catch (Exception e) {
                log.error("删除记录失败：{}", record.getCode(), e);
            }
        }

        // 处理变更（只更新 grassroots_organization 表）
        for (ChangeRecord change : result.getChanged()) {
            try {
                QueryWrapper<GrassrootsOrganization> gwWrapper = new QueryWrapper<>();
                gwWrapper.eq("code", change.getCode());
                gwWrapper.eq("year", year);
                gwWrapper.eq("is_deleted", 0);

                GrassrootsOrganization gwExisting = grassrootsOrganizationMapper.selectOne(gwWrapper);
                if (gwExisting != null) {
                    gwExisting.setName(change.getNewName());
                    gwExisting.setTownshipName(change.getNewName());
                    gwExisting.setUpdateTime(now);
                    grassrootsOrganizationMapper.updateById(gwExisting);
                    log.info("更新 grassroots_organization 记录：{} - {}", change.getCode(), change.getNewName());
                }

            } catch (Exception e) {
                log.error("更新记录失败：{}", change.getCode(), e);
            }
        }
    }

    /**
     * 根据代码查找组织机构
     */
    private Organization findOrganizationByCode(String code, Integer year, Integer level) {
        QueryWrapper<Organization> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        wrapper.eq("year", year);
        wrapper.eq("level", level);
        wrapper.eq("is_deleted", 0);
        return organizationMapper.selectOne(wrapper);
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
     * 导入结果
     */
    @Data
    public static class ImportResult {
        private Integer year;
        private String dataType;
        private List<OrgRecord> added = new ArrayList<>();
        private List<OrgRecord> removed = new ArrayList<>();
        private List<ChangeRecord> changed = new ArrayList<>();

        public int getAddedCount() {
            return added.size();
        }

        public int getRemovedCount() {
            return removed.size();
        }

        public int getChangedCount() {
            return changed.size();
        }

        public String getSummary() {
            return String.format("年份：%d, 类型：%s, 新增：%d, 删除：%d, 变更：%d",
                    year, dataType, getAddedCount(), getRemovedCount(), getChangedCount());
        }
    }

    /**
     * 变更记录
     */
    @Data
    public static class ChangeRecord {
        private String code;
        private String oldName;
        private String newName;
    }
}
