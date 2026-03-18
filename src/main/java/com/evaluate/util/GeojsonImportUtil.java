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
        private String countyCode;
        private String cityCode;
        private String provinceCode;
        private String townshipName;
    }

    @Transactional(rollbackFor = Exception.class)
    public ImportResult import2024Townships(String geojsonPath, Integer year) throws IOException {
        log.info("开始导入 {} 年乡镇数据，文件：{}, 年份：{}", year, geojsonPath, year);

        ImportResult result = new ImportResult();

        String content = FileUtils.readFileToString(Paths.get(geojsonPath).toFile(), StandardCharsets.UTF_8);
        Map<String, Object> geojson = parseGeojson(content);

        // 获取上一年数据（从 grassroots_organization 表）
        Integer prevYear = year != null ? year - 1 : null;
        Map<String, OrgRecord> prevYearData = getEffectiveYearTownshipData(prevYear);
        log.info("获取到 {} 年乡镇数据 {} 条", prevYear, prevYearData.size());

        Map<String, OrgRecord> geojsonData = parseTownshipFeatures(geojson);
        log.info("从 GeoJSON 解析到乡镇数据 {} 条", geojsonData.size());

        compareTownshipChangesWithPrevYear(prevYearData, geojsonData, year, result);
        log.info("数据对比完成：新增={}, 删除={}, 变更={}",
                result.getAddedCount(), result.getRemovedCount(), result.getChangedCount());

        applyTownshipChangesToDatabase(result, year, true);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public ImportResult importTownshipsComparePrevYear(String geojsonPath, Integer year) throws IOException {
        log.info("开始导入 {} 年乡镇数据（对比上一年），文件：{}", year, geojsonPath);

        ImportResult result = new ImportResult();

        String content = FileUtils.readFileToString(Paths.get(geojsonPath).toFile(), StandardCharsets.UTF_8);
        Map<String, Object> geojson = parseGeojson(content);

        Integer prevYear = year != null ? year - 1 : null;
        Map<String, OrgRecord> prevYearData = getEffectiveYearTownshipData(prevYear);
        log.info("获取到 {} 年乡镇数据 {} 条", prevYear, prevYearData.size());

        Map<String, OrgRecord> geojsonData = parseTownshipFeatures(geojson);
        log.info("从 GeoJSON 解析到乡镇数据 {} 条", geojsonData.size());

        compareTownshipChangesWithPrevYear(prevYearData, geojsonData, year, result);
        log.info("数据对比完成：新增={}, 删除={}, 变更={}",
                result.getAddedCount(), result.getRemovedCount(), result.getChangedCount());

        applyTownshipChangesToDatabase(result, year, true);

        return result;
    }

    private Map<String, OrgRecord> getEffectiveYearTownshipData(Integer year) {
        Map<String, OrgRecord> result = new HashMap<>();
        if (year == null) {
            return result;
        }

        QueryWrapper<GrassrootsOrganization> baselineWrapper = new QueryWrapper<>();
        baselineWrapper.eq("year", BASELINE_YEAR);
        baselineWrapper.eq("level", LEVEL_TOWNSHIP);
        baselineWrapper.eq("is_deleted", 0);
        List<GrassrootsOrganization> baselineList = grassrootsOrganizationMapper.selectList(baselineWrapper);
        for (GrassrootsOrganization org : baselineList) {
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

        int effectiveTargetYear = Math.max(year, BASELINE_YEAR);
        for (int checkYear = BASELINE_YEAR + 1; checkYear <= effectiveTargetYear; checkYear++) {
            QueryWrapper<GrassrootsOrganization> yearWrapper = new QueryWrapper<>();
            yearWrapper.eq("year", checkYear);
            yearWrapper.eq("level", LEVEL_TOWNSHIP);
            yearWrapper.and(w -> w.eq("is_baseline", 0).or().isNull("is_baseline"));
            List<GrassrootsOrganization> yearList = grassrootsOrganizationMapper.selectList(yearWrapper);
            for (GrassrootsOrganization org : yearList) {
                if (org.getIsDeleted() != null && org.getIsDeleted() == 1) {
                    result.remove(org.getCode());
                    continue;
                }
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
        }

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
                } else {
                    // 名称相同，记录为无变化
                    result.getUnchanged().add(geojsonRecord);
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
            String provinceCode = normalizeRegionCode(getStringValue(properties, "dcsheng"), 1);
            String cityCode = normalizeRegionCode(getStringValue(properties, "dcshi"), 2);
            String countyCode = normalizeRegionCode(getStringValue(properties, "dcxian"), 3);

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
            if (StringUtils.hasText(provinceName) && isLikelyCode(provinceName)) {
                String normalized = normalizeRegionCode(provinceName, 1);
                String resolved = resolveRegionNameByCode(normalized, 1);
                if (StringUtils.hasText(resolved)) {
                    provinceCode = normalized;
                    provinceName = resolved;
                }
            }
            if (StringUtils.hasText(cityName) && isLikelyCode(cityName)) {
                String normalized = normalizeRegionCode(cityName, 2);
                String resolved = resolveRegionNameByCode(normalized, 2);
                if (StringUtils.hasText(resolved)) {
                    cityCode = normalized;
                    cityName = resolved;
                }
            }
            if (StringUtils.hasText(countyName) && isLikelyCode(countyName)) {
                String normalized = normalizeRegionCode(countyName, 3);
                countyCode = normalized;
                Organization countyOrg = findOrganizationByCode(normalized, BASELINE_YEAR, 3);
                if (countyOrg != null) {
                    countyName = countyOrg.getName();
                    if (!StringUtils.hasText(provinceName) || isLikelyCode(provinceName)) {
                        provinceName = countyOrg.getProvinceName();
                    }
                    if (!StringUtils.hasText(cityName) || isLikelyCode(cityName)) {
                        cityName = countyOrg.getCityName();
                    }
                }
            }
            if (!StringUtils.hasText(countyName) || isLikelyCode(countyName)) {
                String address = getStringValue(properties, "address");
                String extracted = extractCountyNameFromAddress(address);
                if (StringUtils.hasText(extracted)) {
                    countyName = extracted;
                }
            }
            if (!StringUtils.hasText(provinceName) && StringUtils.hasText(provinceCode)) {
                String resolved = resolveRegionNameByCode(provinceCode, 1);
                if (StringUtils.hasText(resolved)) {
                    provinceName = resolved;
                }
            }
            if (!StringUtils.hasText(cityName) && StringUtils.hasText(cityCode)) {
                String resolved = resolveRegionNameByCode(cityCode, 2);
                if (StringUtils.hasText(resolved)) {
                    cityName = resolved;
                }
            }
            if (!StringUtils.hasText(countyName) && StringUtils.hasText(countyCode)) {
                String resolved = resolveRegionNameByCode(countyCode, 3);
                if (StringUtils.hasText(resolved)) {
                    countyName = resolved;
                }
            }

            record.setProvinceName(provinceName);
            record.setCityName(cityName);
            record.setCountyName(countyName);
            record.setProvinceCode(provinceCode);
            record.setCityCode(cityCode);
            record.setCountyCode(countyCode);
            record.setTownshipName(townshipName);
            record.setLevel(LEVEL_TOWNSHIP);

            result.put(code, record);
        }

        return result;
    }

    private GrassrootsOrganization findBaselineTownshipByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        wrapper.eq("year", BASELINE_YEAR);
        wrapper.eq("level", LEVEL_TOWNSHIP);
        wrapper.eq("is_deleted", 0);
        return grassrootsOrganizationMapper.selectOne(wrapper);
    }

    private GrassrootsOrganization findBaselineTownshipByName(String name, String countyName) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
        wrapper.eq("year", BASELINE_YEAR);
        wrapper.eq("level", LEVEL_TOWNSHIP);
        wrapper.eq("is_deleted", 0);
        wrapper.eq("name", name.trim());
        if (StringUtils.hasText(countyName) && !isLikelyCode(countyName)) {
            wrapper.eq("county_name", countyName.trim());
        }
        wrapper.last("LIMIT 2");
        List<GrassrootsOrganization> candidates = grassrootsOrganizationMapper.selectList(wrapper);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        if (StringUtils.hasText(countyName) && !isLikelyCode(countyName)) {
            for (GrassrootsOrganization candidate : candidates) {
                if (countyName.trim().equals(candidate.getCountyName())) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private GrassrootsOrganization findTownshipByCodeAndYear(String code, Integer year) {
        if (!StringUtils.hasText(code) || year == null) {
            return null;
        }
        QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        wrapper.eq("year", year);
        wrapper.eq("level", LEVEL_TOWNSHIP);
        wrapper.eq("is_deleted", 0);
        return grassrootsOrganizationMapper.selectOne(wrapper);
    }

    private GrassrootsOrganization findLatestTownshipByCode(String code, Integer year) {
        if (!StringUtils.hasText(code) || year == null) {
            return null;
        }
        int targetYear = Math.max(year, BASELINE_YEAR);
        for (int checkYear = targetYear; checkYear >= BASELINE_YEAR; checkYear--) {
            GrassrootsOrganization org = findTownshipByCodeAndYear(code, checkYear);
            if (org != null) {
                return org;
            }
        }
        return null;
    }

    private GrassrootsOrganization buildYearRecordFromSource(GrassrootsOrganization source, Integer year, LocalDateTime now) {
        GrassrootsOrganization copy = new GrassrootsOrganization();
        copy.setCode(source.getCode());
        copy.setName(source.getName());
        copy.setLevel(source.getLevel());
        copy.setYear(year);
        copy.setIsBaseline(0);
        copy.setCreateTime(now);
        copy.setUpdateTime(now);
        copy.setIsDeleted(0);
        copy.setTownshipName(source.getTownshipName());
        copy.setProvinceName(source.getProvinceName());
        copy.setCityName(source.getCityName());
        copy.setCountyName(source.getCountyName());
        copy.setCommunityName(source.getCommunityName());
        copy.setParentId(source.getParentId());
        copy.setCountyId(source.getCountyId());
        copy.setDataSource(source.getDataSource());
        copy.setBaselineCode(StringUtils.hasText(source.getBaselineCode()) ? source.getBaselineCode() : source.getCode());
        return copy;
    }

    private Organization findOrCreateCounty(String countyCode, OrgRecord record, Integer year, LocalDateTime now) {
        if (!StringUtils.hasText(countyCode)) {
            return null;
        }
        Organization county = findOrganizationByCode(countyCode, year, 3);
        if (county != null) {
            String desiredName = record != null ? record.getCountyName() : null;
            if (StringUtils.hasText(desiredName) && !isLikelyCode(desiredName)
                    && (isLikelyCode(county.getName()) || !StringUtils.hasText(county.getName()))) {
                county.setName(desiredName);
                county.setProvinceName(record.getProvinceName());
                county.setCityName(record.getCityName());
                county.setCountyName(desiredName);
                county.setUpdateTime(now);
                organizationMapper.updateById(county);
            }
            return county;
        }
        Organization baseline = findOrganizationByCode(countyCode, BASELINE_YEAR, 3);
        if (baseline != null) {
            return baseline;
        }
        Organization created = new Organization();
        created.setCode(countyCode);
        String countyName = record != null ? record.getCountyName() : null;
        created.setName(StringUtils.hasText(countyName) && !isLikelyCode(countyName) ? countyName : countyCode);
        created.setLevel(3);
        created.setYear(year);
        created.setDataSource("IMPORT");
        created.setProvinceName(record != null ? record.getProvinceName() : null);
        created.setCityName(record != null ? record.getCityName() : null);
        created.setCountyName(record != null ? record.getCountyName() : null);
        created.setIsBaseline(0);
        created.setBaselineCode(countyCode);
        created.setCreateTime(now);
        created.setUpdateTime(now);
        created.setIsDeleted(0);
        organizationMapper.insert(created);
        return created;
    }

    /**
     * 应用乡镇变更到数据库（只更新 grassroots_organization 表）
     */
    @Transactional(rollbackFor = Exception.class)
    private void applyTownshipChangesToDatabase(ImportResult result, Integer year, boolean copyUnchanged) {
        LocalDateTime now = LocalDateTime.now();

        for (OrgRecord record : result.getAdded()) {
            try {
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
                GrassrootsOrganization matchedBaseline = findBaselineTownshipByName(record.getName(), record.getCountyName());
                grassroots.setBaselineCode(matchedBaseline != null ? matchedBaseline.getCode() : record.getCode());

                String resolvedCountyCode = resolveCountyCode(record, year);
                if (StringUtils.hasText(resolvedCountyCode)) {
                    Organization county = findOrCreateCounty(resolvedCountyCode, record, year, now);
                    if (county != null) {
                        grassroots.setParentId(county.getId());
                        grassroots.setCountyId(county.getId());
                        if (!StringUtils.hasText(record.getCountyName()) || isLikelyCode(record.getCountyName())) {
                            grassroots.setCountyName(county.getName());
                        }
                        if (!StringUtils.hasText(record.getCityName()) || isLikelyCode(record.getCityName())) {
                            grassroots.setCityName(county.getCityName());
                        }
                        if (!StringUtils.hasText(record.getProvinceName()) || isLikelyCode(record.getProvinceName())) {
                            grassroots.setProvinceName(county.getProvinceName());
                        }
                    }
                }

                grassrootsOrganizationMapper.insert(grassroots);
                log.info("插入 grassroots_organization 记录：{}", grassroots.getCode());

            } catch (Exception e) {
                log.error("插入记录失败：{}", record.getCode(), e);
            }
        }

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
                } else {
                    GrassrootsOrganization source = findLatestTownshipByCode(record.getCode(), year - 1);
                    if (source != null) {
                        GrassrootsOrganization deletedCopy = buildYearRecordFromSource(source, year, now);
                        deletedCopy.setIsDeleted(1);
                        grassrootsOrganizationMapper.insert(deletedCopy);
                        log.info("插入删除标记记录：{}", record.getCode());
                    }
                }

            } catch (Exception e) {
                log.error("删除记录失败：{}", record.getCode(), e);
            }
        }

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
                } else {
                    GrassrootsOrganization source = findLatestTownshipByCode(change.getCode(), year - 1);
                    if (source != null) {
                        GrassrootsOrganization changedCopy = buildYearRecordFromSource(source, year, now);
                        changedCopy.setName(change.getNewName());
                        changedCopy.setTownshipName(change.getNewName());
                        grassrootsOrganizationMapper.insert(changedCopy);
                        log.info("插入变更记录：{} - {}", change.getCode(), change.getNewName());
                    }
                }

            } catch (Exception e) {
                log.error("更新记录失败：{}", change.getCode(), e);
            }
        }

        if (!copyUnchanged) {
            return;
        }

        for (OrgRecord record : result.getUnchanged()) {
            try {
                QueryWrapper<GrassrootsOrganization> checkWrapper = new QueryWrapper<>();
                checkWrapper.eq("code", record.getCode());
                checkWrapper.eq("year", year);
                checkWrapper.eq("is_deleted", 0);
                GrassrootsOrganization existing = grassrootsOrganizationMapper.selectOne(checkWrapper);
                if (existing != null) {
                    log.debug("记录已存在，跳过：{} ({})", record.getCode(), record.getName());
                    continue;
                }

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

                String resolvedCountyCode = resolveCountyCode(record, year);
                if (StringUtils.hasText(resolvedCountyCode)) {
                    Organization county = findOrCreateCounty(resolvedCountyCode, record, year, now);
                    if (county != null) {
                        grassroots.setParentId(county.getId());
                        grassroots.setCountyId(county.getId());
                    }
                }

                grassrootsOrganizationMapper.insert(grassroots);
                log.info("复制无变化记录到 {} 年：{} - {}", year, grassroots.getCode(), grassroots.getName());

            } catch (Exception e) {
                log.error("复制无变化记录失败：{}", record.getCode(), e);
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

    private String resolveRegionNameByCode(String code, Integer level) {
        if (!StringUtils.hasText(code) || level == null) {
            return null;
        }
        String normalized = normalizeRegionCode(code, level);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        QueryWrapper<Organization> wrapper = new QueryWrapper<>();
        wrapper.eq("code", normalized);
        wrapper.eq("level", level);
        wrapper.eq("year", BASELINE_YEAR);
        wrapper.eq("is_deleted", 0);
        Organization org = organizationMapper.selectOne(wrapper);
        return org != null ? org.getName() : null;
    }

    private String normalizeRegionCode(String code, Integer level) {
        if (!StringUtils.hasText(code) || level == null) {
            return null;
        }
        String trimmed = code.trim();
        if (!trimmed.matches("\\d+")) {
            return trimmed;
        }
        if (level == 1) {
            if (trimmed.length() >= 2) {
                return trimmed.substring(0, 2);
            }
            return trimmed;
        }
        if (level == 2) {
            if (trimmed.length() >= 4) {
                return trimmed.substring(0, 4);
            }
            return trimmed;
        }
        if (level == 3) {
            if (trimmed.length() >= 6) {
                return trimmed.substring(0, 6);
            }
            return trimmed;
        }
        return trimmed;
    }

    private String resolveCountyCode(OrgRecord record, Integer year) {
        if (record == null || !StringUtils.hasText(record.getCode()) || record.getCode().length() < 6) {
            return null;
        }
        if (StringUtils.hasText(record.getCountyCode()) && isLikelyCode(record.getCountyCode())) {
            String candidate = record.getCountyCode().substring(0, 6);
            if (findOrganizationByCode(candidate, year, 3) != null || findOrganizationByCode(candidate, BASELINE_YEAR, 3) != null) {
                return candidate;
            }
        }
        return record.getCode().substring(0, 6);
    }

    private boolean isLikelyCode(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return value.trim().matches("\\d{6,}");
    }

    private String extractCountyNameFromAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        int markerIndex = -1;
        String[] cityMarkers = {"市", "州", "盟", "地区"};
        for (String marker : cityMarkers) {
            int idx = address.lastIndexOf(marker);
            if (idx > markerIndex) {
                markerIndex = idx;
            }
        }
        if (markerIndex < 0 || markerIndex + 1 >= address.length()) {
            return null;
        }
        String tail = address.substring(markerIndex + 1);
        String[] countyMarkers = {"县", "区", "市", "旗"};
        int endIndex = -1;
        for (String marker : countyMarkers) {
            int idx = tail.indexOf(marker);
            if (idx >= 0 && (endIndex == -1 || idx < endIndex)) {
                endIndex = idx;
            }
        }
        if (endIndex < 0) {
            return null;
        }
        return tail.substring(0, endIndex + 1);
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
        private List<OrgRecord> unchanged = new ArrayList<>();

        public int getAddedCount() {
            return added.size();
        }

        public int getRemovedCount() {
            return removed.size();
        }

        public int getChangedCount() {
            return changed.size();
        }

        public int getUnchangedCount() {
            return unchanged.size();
        }

        public String getSummary() {
            return String.format("年份：%d, 类型：%s, 新增：%d, 删除：%d, 变更：%d, 无变化：%d",
                    year, dataType, getAddedCount(), getRemovedCount(), getChangedCount(), getUnchangedCount());
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
