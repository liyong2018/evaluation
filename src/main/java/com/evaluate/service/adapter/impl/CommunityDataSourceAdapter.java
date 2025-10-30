package com.evaluate.service.adapter.impl;

import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.adapter.DataSourceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 社区数据源适配器
 * 基于community_disaster_reduction_capacity表提供社区级别的数据访问
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Component("communityDataSourceAdapter")
public class CommunityDataSourceAdapter implements DataSourceAdapter {

    @Autowired
    private ICommunityDisasterReductionCapacityService communityService;

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    @Override
    public String getAdapterType() {
        return "COMMUNITY";
    }

    @Override
    public Map<String, Object> getRawData(List<String> regionCodes, Long weightConfigId) {
        log.info("获取社区原始数据, regionCodes={}, weightConfigId={}", regionCodes, weightConfigId);

        Map<String, Object> rawData = new HashMap<>();

        try {
            // 根据地区代码获取社区减灾能力数据
            List<CommunityDisasterReductionCapacity> communityDataList = communityService.list();

            // 筛选指定地区的数据
            Map<String, CommunityDisasterReductionCapacity> regionDataMap = communityDataList.stream()
                .filter(data -> regionCodes.contains(data.getRegionCode()))
                .collect(Collectors.toMap(
                    CommunityDisasterReductionCapacity::getRegionCode,
                    data -> data,
                    (existing, replacement) -> existing
                ));

            rawData.put("communityData", regionDataMap);
            rawData.put("regionCodes", regionCodes);
            rawData.put("weightConfigId", weightConfigId);

            log.info("成功获取社区原始数据，共{}个地区", regionDataMap.size());

        } catch (Exception e) {
            log.error("获取社区原始数据失败", e);
            throw new RuntimeException("获取社区原始数据失败: " + e.getMessage(), e);
        }

        return rawData;
    }

    @Override
    public Map<String, Map<String, Double>> getIndicatorData(List<String> regionCodes, Long weightConfigId) {
        log.info("获取社区指标数据, regionCodes={}, weightConfigId={}", regionCodes, weightConfigId);

        Map<String, Map<String, Double>> indicatorData = new HashMap<>();

        try {
            // 获取权重配置中的所有指标
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(weightConfigId);

            // 获取社区减灾能力数据
            List<CommunityDisasterReductionCapacity> communityDataList = communityService.list();

            // 为每个地区构建指标数据
            for (String regionCode : regionCodes) {
                Map<String, Double> regionIndicators = new HashMap<>();

                // 查找该地区的社区数据
                Optional<CommunityDisasterReductionCapacity> communityDataOpt = communityDataList.stream()
                    .filter(data -> regionCode.equals(data.getRegionCode()))
                    .findFirst();

                if (communityDataOpt.isPresent()) {
                    CommunityDisasterReductionCapacity communityData = communityDataOpt.get();

                    // 使用反射从CommunityDisasterReductionCapacity对象中获取指标值
                    for (IndicatorWeight weight : indicatorWeights) {
                        String indicatorCode = weight.getIndicatorCode();
                        try {
                            // 获取指标值
                            Double value = getIndicatorValue(communityData, indicatorCode);
                            if (value != null) {
                                regionIndicators.put(indicatorCode, value);
                            }
                        } catch (Exception e) {
                            log.warn("获取指标{}的值失败: {}", indicatorCode, e.getMessage());
                        }
                    }
                } else {
                    log.warn("未找到地区{}的社区数据", regionCode);
                }

                indicatorData.put(regionCode, regionIndicators);
            }

            log.info("成功获取社区指标数据，共{}个地区，每个地区包含{}个指标",
                indicatorData.size(), indicatorWeights.size());

        } catch (Exception e) {
            log.error("获取社区指标数据失败", e);
            throw new RuntimeException("获取社区指标数据失败: " + e.getMessage(), e);
        }

        return indicatorData;
    }

    @Override
    public Map<String, String> getRegionNames(List<String> regionCodes) {
        log.info("获取社区地区名称, regionCodes={}", regionCodes);

        Map<String, String> regionNames = new HashMap<>();

        try {
            List<CommunityDisasterReductionCapacity> communityDataList = communityService.list();

            for (String regionCode : regionCodes) {
                Optional<CommunityDisasterReductionCapacity> communityDataOpt = communityDataList.stream()
                    .filter(data -> regionCode.equals(data.getRegionCode()))
                    .findFirst();

                if (communityDataOpt.isPresent()) {
                    CommunityDisasterReductionCapacity communityData = communityDataOpt.get();
                    // 构建地区名称：省+市+县+乡镇+社区
                    StringBuilder regionName = new StringBuilder();
                    if (communityData.getProvinceName() != null && !communityData.getProvinceName().trim().isEmpty()) {
                        regionName.append(communityData.getProvinceName());
                    }
                    if (communityData.getCityName() != null && !communityData.getCityName().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(communityData.getCityName());
                    }
                    if (communityData.getCountyName() != null && !communityData.getCountyName().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(communityData.getCountyName());
                    }
                    if (communityData.getTownshipName() != null && !communityData.getTownshipName().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(communityData.getTownshipName());
                    }
                    if (communityData.getCommunityName() != null && !communityData.getCommunityName().trim().isEmpty()) {
                        if (regionName.length() > 0) regionName.append("-");
                        regionName.append(communityData.getCommunityName());
                    }

                    String finalRegionName = regionName.length() > 0 ? regionName.toString() : regionCode;
                    regionNames.put(regionCode, finalRegionName);
                } else {
                    log.warn("未找到地区{}的名称信息", regionCode);
                    regionNames.put(regionCode, regionCode); // 使用代码作为后备
                }
            }

            log.info("成功获取{}个社区地区的名称", regionNames.size());

        } catch (Exception e) {
            log.error("获取社区地区名称失败", e);
            throw new RuntimeException("获取社区地区名称失败: " + e.getMessage(), e);
        }

        return regionNames;
    }

    @Override
    public boolean validateDataSource(List<String> regionCodes, Long weightConfigId) {
        log.info("验证社区数据源, regionCodes={}, weightConfigId={}", regionCodes, weightConfigId);

        try {
            // 检查权重配置是否存在
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(weightConfigId);
            if (indicatorWeights.isEmpty()) {
                log.error("权重配置{}不存在或没有指标", weightConfigId);
                return false;
            }

            // 检查地区数据是否存在
            List<CommunityDisasterReductionCapacity> communityDataList = communityService.list();
            Set<String> availableRegions = communityDataList.stream()
                .map(CommunityDisasterReductionCapacity::getRegionCode)
                .collect(Collectors.toSet());

            for (String regionCode : regionCodes) {
                if (!availableRegions.contains(regionCode)) {
                    log.warn("地区{}在社区数据中不存在", regionCode);
                    return false;
                }
            }

            log.info("社区数据源验证通过");
            return true;

        } catch (Exception e) {
            log.error("社区数据源验证失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", getAdapterType());
        metadata.put("description", getDescription());
        metadata.put("dataSource", "community_disaster_reduction_capacity");
        metadata.put("supportedLevels", Arrays.asList("community", "社区", "行政村"));
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
            List<CommunityDisasterReductionCapacity> communityDataList = communityService.list();
            return communityDataList.stream()
                .anyMatch(data -> regionCode.equals(data.getRegionCode()));
        } catch (Exception e) {
            log.error("检查地区支持状态失败", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "社区级别数据源适配器，基于community_disaster_reduction_capacity表提供社区级别的灾害减灾能力评估数据";
    }

    /**
     * 从CommunityDisasterReductionCapacity对象中获取指标值
     * 这里使用反射来动态获取字段值
     */
    private Double getIndicatorValue(CommunityDisasterReductionCapacity communityData, String indicatorCode) {
        try {
            // 将指标代码转换为可能的字段名
            String fieldName = convertIndicatorCodeToFieldName(indicatorCode);

            java.lang.reflect.Field field = communityData.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(communityData);

            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof BigDecimal) {
                return ((BigDecimal) value).doubleValue();
            } else if (value != null && !value.toString().trim().isEmpty()) {
                // 对于是/否类型的字段，转换为数值
                String strValue = value.toString().trim();
                if (strValue.equals("是") || strValue.equalsIgnoreCase("yes") || strValue.equals("1")) {
                    return 1.0;
                } else if (strValue.equals("否") || strValue.equalsIgnoreCase("no") || strValue.equals("0")) {
                    return 0.0;
                } else {
                    return Double.valueOf(strValue);
                }
            }

        } catch (NoSuchFieldException e) {
            // 如果没有找到对应字段，尝试其他可能的字段名
            Double alternativeValue = tryAlternativeFieldNames(communityData, indicatorCode);
            if (alternativeValue != null) {
                return alternativeValue;
            }
        } catch (Exception e) {
            log.warn("获取指标值失败: indicatorCode={}, error={}", indicatorCode, e.getMessage());
        }

        // 如果所有方法都无法获取值，返回默认值以避免QLExpress中的NullPointerException
        return getDefaultIndicatorValue(indicatorCode);
    }

    /**
     * 将指标代码转换为字段名
     */
    private String convertIndicatorCodeToFieldName(String indicatorCode) {
        // 特殊字段映射：处理乡镇模型指标代码到社区模型字段的映射
        Map<String, String> specialMapping = new HashMap<>();

        // 管理人员相关：乡镇模型的management_staff映射到社区的民兵预备役或志愿者
        specialMapping.put("managementStaff", "militiaReserveCount");
        specialMapping.put("management_staff", "militiaReserveCount");

        // 人口相关：乡镇模型的population映射到社区的resident_population
        specialMapping.put("population", "residentPopulation");
        specialMapping.put("resident_population", "residentPopulation");

        // 资金相关：乡镇模型的funding_amount映射到社区的last_year_funding_amount
        specialMapping.put("fundingAmount", "lastYearFundingAmount");
        specialMapping.put("funding_amount", "lastYearFundingAmount");
        specialMapping.put("lastYearFundingAmount", "lastYearFundingAmount");

        // 物资相关：乡镇模型的material_value映射到社区的materials_equipment_value
        specialMapping.put("materialValue", "materialsEquipmentValue");
        specialMapping.put("material_value", "materialsEquipmentValue");
        specialMapping.put("materialsEquipmentValue", "materialsEquipmentValue");

        // 医疗相关：乡镇模型的hospital_beds映射到社区的medical_service_count
        specialMapping.put("hospitalBeds", "medicalServiceCount");
        specialMapping.put("hospital_beds", "medicalServiceCount");
        specialMapping.put("medicalServiceCount", "medicalServiceCount");

        // 志愿者相关
        specialMapping.put("volunteers", "registeredVolunteerCount");
        specialMapping.put("volunteerCount", "registeredVolunteerCount");
        specialMapping.put("registered_volunteer_count", "registeredVolunteerCount");
        specialMapping.put("registeredVolunteerCount", "registeredVolunteerCount");

        // 民兵相关
        specialMapping.put("militiaReserve", "militiaReserveCount");
        specialMapping.put("militia_reserve", "militiaReserveCount");
        specialMapping.put("militiaReserveCount", "militiaReserveCount");

        // 培训相关
        specialMapping.put("trainingParticipants", "lastYearTrainingParticipants");
        specialMapping.put("training_participants", "lastYearTrainingParticipants");
        specialMapping.put("lastYearTrainingParticipants", "lastYearTrainingParticipants");

        // 避难场所相关
        specialMapping.put("shelterCapacity", "emergencyShelterCapacity");
        specialMapping.put("shelter_capacity", "emergencyShelterCapacity");
        specialMapping.put("emergencyShelterCapacity", "emergencyShelterCapacity");

        // 检查是否有特殊映射
        if (specialMapping.containsKey(indicatorCode)) {
            return specialMapping.get(indicatorCode);
        }

        // 默认转换：指标代码格式为 "INDICATOR_NAME"，转换为 "indicatorName"
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
    private Double tryAlternativeFieldNames(CommunityDisasterReductionCapacity communityData, String indicatorCode) {
        // 尝试一些常见的字段名模式
        String[] alternatives = {
            indicatorCode.toLowerCase(),
            indicatorCode,
            "has_" + indicatorCode.toLowerCase(),
            indicatorCode.toLowerCase() + "_value"
        };

        for (String fieldName : alternatives) {
            try {
                java.lang.reflect.Field field = communityData.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(communityData);

                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof BigDecimal) {
                    return ((BigDecimal) value).doubleValue();
                } else if (value != null && !value.toString().trim().isEmpty()) {
                    // 对于是/否类型的字段，转换为数值
                    String strValue = value.toString().trim();
                    if (strValue.equals("是") || strValue.equalsIgnoreCase("yes") || strValue.equals("1")) {
                        return 1.0;
                    } else if (strValue.equals("否") || strValue.equalsIgnoreCase("no") || strValue.equals("0")) {
                        return 0.0;
                    } else {
                        return Double.valueOf(strValue);
                    }
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
            "HAS_EMERGENCY_PLAN",               // 应急预案
            "HAS_VULNERABLE_GROUPS_LIST",       // 弱势人群清单
            "HAS_DISASTER_POINTS_LIST",         // 隐患点清单
            "HAS_DISASTER_MAP",                 // 灾害地图
            "RESIDENT_POPULATION",              // 常住人口
            "LAST_YEAR_FUNDING_AMOUNT",         // 防灾减灾资金投入
            "MATERIALS_EQUIPMENT_VALUE",        // 储备物资装备价值
            "MEDICAL_SERVICE_COUNT",            // 医疗卫生服务站数量
            "MILITIA_RESERVE_COUNT",            // 民兵预备役人数
            "REGISTERED_VOLUNTEER_COUNT",       // 注册志愿者人数
            "LAST_YEAR_TRAINING_PARTICIPANTS",  // 防灾减灾培训人次
            "LAST_YEAR_DRILL_PARTICIPANTS",     // 防灾减灾演练人次
            "EMERGENCY_SHELTER_CAPACITY"        // 应急避难场所容量
        );
    }

    /**
     * 获取指标默认值，避免QLExpress中的NullPointerException
     */
    private Double getDefaultIndicatorValue(String indicatorCode) {
        // 为关键指标提供合理的默认值
        if (indicatorCode.toLowerCase().contains("population")) {
            return 1000.0; // 默认人口基数
        }
        if (indicatorCode.toLowerCase().contains("management") ||
            indicatorCode.toLowerCase().contains("staff") ||
            indicatorCode.toLowerCase().contains("militia")) {
            return 1.0; // 默认管理人员数量
        }
        if (indicatorCode.toLowerCase().contains("funding") ||
            indicatorCode.toLowerCase().contains("amount")) {
            return 10.0; // 默认资金投入（万元）
        }
        if (indicatorCode.toLowerCase().contains("material") ||
            indicatorCode.toLowerCase().contains("equipment")) {
            return 5.0; // 默认物资价值（万元）
        }
        if (indicatorCode.toLowerCase().contains("medical") ||
            indicatorCode.toLowerCase().contains("hospital")) {
            return 1.0; // 默认医疗设施数量
        }
        if (indicatorCode.toLowerCase().contains("volunteer")) {
            return 10.0; // 默认志愿者数量
        }
        if (indicatorCode.toLowerCase().contains("training") ||
            indicatorCode.toLowerCase().contains("drill")) {
            return 50.0; // 默认培训演练人次
        }
        if (indicatorCode.toLowerCase().contains("shelter")) {
            return 100.0; // 默认避难场所容量
        }

        // 其他指标默认返回0.0
        return 0.0;
    }
}