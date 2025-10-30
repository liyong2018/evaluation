package com.evaluate.service.adapter.impl;

import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.SurveyData;
import com.evaluate.service.adapter.DataSourceAdapter;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.ISurveyDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 乡镇数据源适配器
 * 基于survey_data表提供乡镇级别的数据访问
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Component("townshipDataSourceAdapter")
public class TownshipDataSourceAdapter implements DataSourceAdapter {

    @Autowired
    private ISurveyDataService surveyDataService;

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

  
    @Override
    public String getAdapterType() {
        return "TOWNSHIP";
    }

    @Override
    public Map<String, Object> getRawData(List<String> regionCodes, Long weightConfigId) {
        log.info("获取乡镇原始数据, regionCodes={}, weightConfigId={}", regionCodes, weightConfigId);

        Map<String, Object> rawData = new HashMap<>();

        try {
            // 根据地区代码获取调查数据
            List<SurveyData> surveyDataList = surveyDataService.list();

            // 筛选指定地区的数据
            Map<String, SurveyData> regionDataMap = surveyDataList.stream()
                .filter(data -> regionCodes.contains(data.getRegionCode()))
                .collect(Collectors.toMap(
                    SurveyData::getRegionCode,
                    data -> data,
                    (existing, replacement) -> existing
                ));

            rawData.put("surveyData", regionDataMap);
            rawData.put("regionCodes", regionCodes);
            rawData.put("weightConfigId", weightConfigId);

            log.info("成功获取乡镇原始数据，共{}个地区", regionDataMap.size());

        } catch (Exception e) {
            log.error("获取乡镇原始数据失败", e);
            throw new RuntimeException("获取乡镇原始数据失败: " + e.getMessage(), e);
        }

        return rawData;
    }

    @Override
    public Map<String, Map<String, Double>> getIndicatorData(List<String> regionCodes, Long weightConfigId) {
        log.info("获取乡镇指标数据, regionCodes={}, weightConfigId={}", regionCodes, weightConfigId);

        Map<String, Map<String, Double>> indicatorData = new HashMap<>();

        try {
            // 获取权重配置中的所有指标
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(weightConfigId);

            // 获取调查数据
            List<SurveyData> surveyDataList = surveyDataService.list();

            // 为每个地区构建指标数据
            for (String regionCode : regionCodes) {
                Map<String, Double> regionIndicators = new HashMap<>();

                // 查找该地区的调查数据
                Optional<SurveyData> surveyDataOpt = surveyDataList.stream()
                    .filter(data -> regionCode.equals(data.getRegionCode()))
                    .findFirst();

                if (surveyDataOpt.isPresent()) {
                    SurveyData surveyData = surveyDataOpt.get();

                    // 使用反射从SurveyData对象中获取指标值
                    for (IndicatorWeight weight : indicatorWeights) {
                        String indicatorCode = weight.getIndicatorCode();
                        try {
                            // 获取指标值
                            Double value = getIndicatorValue(surveyData, indicatorCode);
                            if (value != null) {
                                regionIndicators.put(indicatorCode, value);
                            }
                        } catch (Exception e) {
                            log.warn("获取指标{}的值失败: {}", indicatorCode, e.getMessage());
                        }
                    }
                } else {
                    log.warn("未找到地区{}的调查数据", regionCode);
                }

                indicatorData.put(regionCode, regionIndicators);
            }

            log.info("成功获取乡镇指标数据，共{}个地区，每个地区包含{}个指标",
                indicatorData.size(), indicatorWeights.size());

        } catch (Exception e) {
            log.error("获取乡镇指标数据失败", e);
            throw new RuntimeException("获取乡镇指标数据失败: " + e.getMessage(), e);
        }

        return indicatorData;
    }

    @Override
    public Map<String, String> getRegionNames(List<String> regionCodes) {
        log.info("获取乡镇地区名称, regionCodes={}", regionCodes);

        Map<String, String> regionNames = new HashMap<>();

        try {
            List<SurveyData> surveyDataList = surveyDataService.list();

            for (String regionCode : regionCodes) {
                Optional<SurveyData> surveyDataOpt = surveyDataList.stream()
                    .filter(data -> regionCode.equals(data.getRegionCode()))
                    .findFirst();

                if (surveyDataOpt.isPresent()) {
                    SurveyData surveyData = surveyDataOpt.get();
                    // 构建地区名称：省+市+县+乡镇
                    StringBuilder regionName = new StringBuilder();
                    if (surveyData.getProvince() != null && !surveyData.getProvince().trim().isEmpty()) {
                        regionName.append(surveyData.getProvince());
                    }
                    if (surveyData.getCity() != null && !surveyData.getCity().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(surveyData.getCity());
                    }
                    if (surveyData.getCounty() != null && !surveyData.getCounty().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(surveyData.getCounty());
                    }
                    if (surveyData.getTownship() != null && !surveyData.getTownship().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(surveyData.getTownship());
                    }

                    String finalRegionName = regionName.length() > 0 ? regionName.toString() : regionCode;
                    regionNames.put(regionCode, finalRegionName);
                } else {
                    log.warn("未找到地区{}的名称信息", regionCode);
                    regionNames.put(regionCode, regionCode); // 使用代码作为后备
                }
            }

            log.info("成功获取{}个乡镇地区的名称", regionNames.size());

        } catch (Exception e) {
            log.error("获取乡镇地区名称失败", e);
            throw new RuntimeException("获取乡镇地区名称失败: " + e.getMessage(), e);
        }

        return regionNames;
    }

    @Override
    public boolean validateDataSource(List<String> regionCodes, Long weightConfigId) {
        log.info("验证乡镇数据源, regionCodes={}, weightConfigId={}", regionCodes, weightConfigId);

        try {
            // 检查权重配置是否存在
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(weightConfigId);
            if (indicatorWeights.isEmpty()) {
                log.error("权重配置{}不存在或没有指标", weightConfigId);
                return false;
            }

            // 检查地区数据是否存在
            List<SurveyData> surveyDataList = surveyDataService.list();
            Set<String> availableRegions = surveyDataList.stream()
                .map(SurveyData::getRegionCode)
                .collect(Collectors.toSet());

            for (String regionCode : regionCodes) {
                if (!availableRegions.contains(regionCode)) {
                    log.warn("地区{}在乡镇数据中不存在", regionCode);
                    return false;
                }
            }

            log.info("乡镇数据源验证通过");
            return true;

        } catch (Exception e) {
            log.error("乡镇数据源验证失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", getAdapterType());
        metadata.put("description", getDescription());
        metadata.put("dataSource", "survey_data");
        metadata.put("supportedLevels", Arrays.asList("township", "乡镇"));
        metadata.put("indicators", getCommonIndicators());
        return metadata;
    }

    @Override
    public List<String> getSupportedIndicators(Long weightConfigId) {
        try {
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(weightConfigId);
            return indicatorWeights.stream()
                .map(IndicatorWeight::getIndicatorCode)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取支持的指标列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isRegionSupported(String regionCode) {
        try {
            List<SurveyData> surveyDataList = surveyDataService.list();
            return surveyDataList.stream()
                .anyMatch(data -> regionCode.equals(data.getRegionCode()));
        } catch (Exception e) {
            log.error("检查地区支持状态失败", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "乡镇级别数据源适配器，基于survey_data表提供乡镇级别的灾害评估数据";
    }

    /**
     * 从SurveyData对象中获取指标值
     * 这里使用反射来动态获取字段值
     */
    private Double getIndicatorValue(SurveyData surveyData, String indicatorCode) {
        try {
            // 将指标代码转换为可能的字段名
            String fieldName = convertIndicatorCodeToFieldName(indicatorCode);

            java.lang.reflect.Field field = surveyData.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(surveyData);

            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value != null) {
                return Double.valueOf(value.toString());
            }

        } catch (NoSuchFieldException e) {
            // 如果没有找到对应字段，尝试其他可能的字段名
            return tryAlternativeFieldNames(surveyData, indicatorCode);
        } catch (Exception e) {
            log.warn("获取指标值失败: indicatorCode={}, error={}", indicatorCode, e.getMessage());
        }

        return null;
    }

    /**
     * 将指标代码转换为字段名
     */
    private String convertIndicatorCodeToFieldName(String indicatorCode) {
        // 假设指标代码格式为 "INDICATOR_NAME"，转换为 "indicatorName"
        String[] parts = indicatorCode.toLowerCase().split("_");
        StringBuilder fieldName = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                fieldName.append(parts[i]);
            } else {
                fieldName.append(Character.toUpperCase(parts[i].charAt(0)))
                         .append(parts[i].substring(1));
            }
        }

        return fieldName.toString();
    }

    /**
     * 尝试其他可能的字段名
     */
    private Double tryAlternativeFieldNames(SurveyData surveyData, String indicatorCode) {
        // 尝试一些常见的字段名模式
        String[] alternatives = {
            indicatorCode.toLowerCase(),
            indicatorCode,
            "ind_" + indicatorCode.toLowerCase(),
            indicatorCode.toLowerCase() + "_value"
        };

        for (String fieldName : alternatives) {
            try {
                java.lang.reflect.Field field = surveyData.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(surveyData);

                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value != null) {
                    return Double.valueOf(value.toString());
                }
            } catch (Exception e) {
                // 继续尝试下一个字段名
            }
        }

        return null;
    }

    /**
     * 获取常见指标列表
     */
    private List<String> getCommonIndicators() {
        return Arrays.asList(
            "PLAN_CONSTRUCTION",        // 预案编制建设
            "RISK_ASSESSMENT",          // 风险隐患排查
            "MONITORING_EARLY_WARNING", // 监测预警
            "EMERGENCY_SHELTER",        // 应急避难场所
            "MATERIAL_RESERVE",         // 物资储备
            "EMERGENCY_TEAM",           // 应急队伍
            "PROPAGANDA_EDUCATION",     // 宣传教育
            "EMERGENCY_DRILL",          // 应急演练
            "RESILIENT_CAPACITY",       // 恢复重建能力
            "ORGANIZATIONAL_MANAGEMENT" // 组织管理
        );
    }
}