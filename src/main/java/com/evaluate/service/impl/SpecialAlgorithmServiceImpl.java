package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.EvaluationResult;
import com.evaluate.entity.ModelExecutionRecord;
import com.evaluate.mapper.EvaluationResultMapper;
import com.evaluate.mapper.ModelExecutionRecordMapper;
import com.evaluate.service.SpecialAlgorithmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 特殊算法标记处理服务实现类
 *
 * @author System
 * @since 2025-10-12
 */
@Slf4j
@Service
public class SpecialAlgorithmServiceImpl implements SpecialAlgorithmService {

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;
    @Autowired
    private ModelExecutionRecordMapper modelExecutionRecordMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String SOCIAL_ORGANIZATION_CAPACITY_TABLE = "social_organization_disaster_reduction_capacity_2020";

    @Override
    public Object executeSpecialAlgorithm(
            String marker,
            String params,
            String currentRegionCode,
            Map<String, Object> regionContext,
            Map<String, Map<String, Object>> allRegionData) {


        switch (marker) {
            case "LOAD_EVAL_RESULT":
                return loadEvaluationResult(params, currentRegionCode, regionContext);

            case "NORMALIZE":
                return normalize(params, currentRegionCode, allRegionData);

            case "TOPSIS_POSITIVE":
                return calculateTopsisPositive(params, currentRegionCode, allRegionData);

            case "TOPSIS_NEGATIVE":
                return calculateTopsisNegative(params, currentRegionCode, allRegionData);

            case "TOPSIS_SCORE":
                return calculateTopsisScore(params, currentRegionCode, allRegionData);

            case "GRADE":
                return calculateGrade(params, currentRegionCode, allRegionData);

            default:
                log.warn("未知的特殊标记: {}", marker);
                return 0.0;
        }
    }

    /**
     * 从evaluation_result表加载评估结果
     * 参数格式：modelId=3,field=management_capability_score
     *
     * @param params 参数字符串
     * @param currentRegionCode 当前地区代码
     * @param regionContext 地区上下文
     * @return 字段值
     */
    private Double loadEvaluationResult(String params, String currentRegionCode, Map<String, Object> regionContext) {
        if (log.isDebugEnabled()) {
            log.debug("[LOAD_EVAL_RESULT] 加载评估结果: params={}, region={}", params, currentRegionCode);
        }

        // 解析参数
        Map<String, String> paramMap = parseParams(params);
        String modelIdStr = paramMap.get("modelId");
        String modelKey = paramMap.get("modelKey");
        String stepCode = paramMap.get("stepCode");
        String fieldName = paramMap.get("field");

        // 2020市级综合模型口径：社会组织4列取社会组织基础数据按“指标赋值+向量归一化+二级定权(0.25)”结果
        if ("socialOrganization".equals(modelKey)
                && "indicator_assignment".equalsIgnoreCase(stepCode)
                && fieldName != null) {
            Double socialStep1Weighted = loadSocialOrganizationWeightedIndicator(fieldName, currentRegionCode, regionContext);
            if (socialStep1Weighted != null) {
                return socialStep1Weighted;
            }
        }

        Long modelId = null;
        if (modelIdStr != null && !modelIdStr.trim().isEmpty()) {
            modelId = Long.parseLong(modelIdStr);
        } else if (modelKey != null && !modelKey.trim().isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Long> sourceModelIds = (Map<String, Long>) regionContext.get("sourceModelIds");
            if (sourceModelIds != null) {
                modelId = sourceModelIds.get(modelKey.trim());
            }
        }

        if (modelId == null || fieldName == null) {
            // 2020市级综合模型兜底：社会组织模型未配置时，直接从社会组织基础表读取一级指标
            if ("socialOrganization".equals(modelKey) && fieldName != null) {
                Double fallbackValue = loadSocialOrganizationFallback(fieldName, currentRegionCode, parseInteger(regionContext.get("year")));
                if (fallbackValue != null) {
                    return fallbackValue;
                }
            }
            log.error("[LOAD_EVAL_RESULT] 参数不完整: modelId={}, modelKey={}, field={}", modelIdStr, modelKey, fieldName);
            return 0.0;
        }

        Integer year = parseInteger(regionContext.get("year"));
        String orgCode = toString(regionContext.get("orgCode"));
        List<String> candidateRegionCodes = resolveCandidateRegionCodes(currentRegionCode, modelKey);

        // 优先从当前执行上下文的 step_ 前缀条目中查找同批次前置模型结果
        Double contextValue = loadValueFromCurrentExecution(modelId, stepCode, fieldName, currentRegionCode, candidateRegionCodes, regionContext);
        if (contextValue != null) {
            if (log.isDebugEnabled()) {
                log.debug("[LOAD_EVAL_RESULT] 从当前执行上下文命中: modelId={}, region={}, field={}, value={}",
                        modelId, currentRegionCode, fieldName, contextValue);
            }
            return contextValue;
        }

        EvaluationResult result;
        if (year != null) {
            result = null;
            for (String candidateRegionCode : candidateRegionCodes) {
                result = evaluationResultMapper.selectLatestByModelYearOrgCodeAndRegionCode(modelId, year, orgCode, candidateRegionCode);
                if (result != null) {
                    break;
                }
            }
        } else {
            // 兼容无年份上下文的旧模型执行
            result = null;
            for (String candidateRegionCode : candidateRegionCodes) {
                QueryWrapper<EvaluationResult> query = new QueryWrapper<>();
                query.eq("evaluation_model_id", modelId)
                        .eq("region_code", candidateRegionCode)
                        .orderByDesc("id")
                        .last("LIMIT 1");
                result = evaluationResultMapper.selectOne(query);
                if (result != null) {
                    break;
                }
            }
        }

        if (result == null && year != null) {
            // 二次兜底：按execution_record反查同年份同地区最新结果
            QueryWrapper<ModelExecutionRecord> recordQuery = new QueryWrapper<>();
            recordQuery.eq("model_id", modelId)
                    .eq("year", year)
                    .orderByDesc("end_time")
                    .orderByDesc("id");
            if (orgCode != null && !orgCode.trim().isEmpty()) {
                recordQuery.eq("org_code", orgCode.trim());
            }
            recordQuery.last("LIMIT 1");
            ModelExecutionRecord latestRecord = modelExecutionRecordMapper.selectOne(recordQuery);
            if (latestRecord != null) {
                for (String candidateRegionCode : candidateRegionCodes) {
                    QueryWrapper<EvaluationResult> fallback = new QueryWrapper<>();
                    fallback.eq("execution_record_id", latestRecord.getId())
                            .eq("region_code", candidateRegionCode)
                            .orderByDesc("id")
                            .last("LIMIT 1");
                    result = evaluationResultMapper.selectOne(fallback);
                    if (result != null) {
                        break;
                    }
                }
            }
        }

        if (result == null) {
            if ("socialOrganization".equals(modelKey)) {
                Double fallbackValue = loadSocialOrganizationFallback(fieldName, currentRegionCode, year);
                if (fallbackValue != null) {
                    return fallbackValue;
                }
            }
            log.warn("[LOAD_EVAL_RESULT] 未找到评估结果: modelId={}, year={}, orgCode={}, regionCode={}",
                    modelId, year, orgCode, currentRegionCode);
            return 0.0;
        }

        // 标准评估结果字段以已匹配到的 evaluation_result 地区行作为准。
        // 不能因为配置了 stepCode 就另查最新执行明细，否则可能读取到非同一条结果所属的历史执行记录。
        Double value = extractFieldValue(result, fieldName);
        if (value == null) {
            value = loadFromExecutionDetail(modelId, modelKey, stepCode, fieldName, year, orgCode, currentRegionCode);
            if (value == null && stepCode != null && !stepCode.trim().isEmpty()) {
                value = loadFromExecutionDetail(modelId, modelKey, null, fieldName, year, orgCode, currentRegionCode);
            }
        }
        if (value == null) {
            value = 0.0;
        }

        if (log.isDebugEnabled()) {
            log.debug("[LOAD_EVAL_RESULT] 加载成功: modelId={}, region={}, field={}, value={}",
                    modelId, currentRegionCode, fieldName, value);
        }

        return value;
    }

    /**
     * 解析参数字符串为Map
     * 例如：modelId=3,field=management_capability_score
     */
    private Map<String, String> parseParams(String params) {
        Map<String, String> paramMap = new HashMap<>();
        String[] pairs = params.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                paramMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        return paramMap;
    }

    /**
     * 从EvaluationResult对象中提取指定字段的值
     */
    private Double extractFieldValue(EvaluationResult result, String fieldName) {
        java.math.BigDecimal bdValue = null;

        switch (fieldName) {
            case "management_capability_score":
                bdValue = result.getManagementCapabilityScore();
                break;
            case "support_capability_score":
                bdValue = result.getSupportCapabilityScore();
                break;
            case "self_rescue_capability_score":
                bdValue = result.getSelfRescueCapabilityScore();
                break;
            case "comprehensive_capability_score":
                bdValue = result.getComprehensiveCapabilityScore();
                break;
            default:
                return null;
        }
        return bdValue != null ? bdValue.doubleValue() : null;
    }

    private Double loadFromExecutionDetail(Long modelId,
                                           String modelKey,
                                           String stepCode,
                                           String fieldName,
                                           Integer year,
                                           String orgCode,
                                           String currentRegionCode) {
        try {
            QueryWrapper<ModelExecutionRecord> recordQuery = new QueryWrapper<>();
            recordQuery.eq("model_id", modelId);
            recordQuery.eq("execution_status", "SUCCESS");
            if (year != null) {
                recordQuery.eq("year", year);
            }
            if (orgCode != null && !orgCode.trim().isEmpty()) {
                recordQuery.eq("org_code", orgCode.trim());
            }
            recordQuery.orderByDesc("end_time").orderByDesc("id").last("LIMIT 1");
            ModelExecutionRecord record = modelExecutionRecordMapper.selectOne(recordQuery);
            if (record == null || record.getResultDetail() == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(record.getResultDetail());
            JsonNode steps = root.path("stepResultsList");
            if (!steps.isArray()) {
                return null;
            }

            String targetRegionCode = normalizeRegionCodeByModelKey(currentRegionCode, modelKey);
            Double matched = findValueInStepResults(steps, targetRegionCode, modelKey, fieldName, stepCode);
            if (matched != null) {
                return matched;
            }
            // 兼容口径：步骤编码变更（例如 family: score_and_grade -> topsis_grading）时，放宽为跨步骤按字段兜底匹配
            return findValueInStepResults(steps, targetRegionCode, modelKey, fieldName, null);
        } catch (Exception e) {
            log.warn("[LOAD_EVAL_RESULT] 解析执行明细失败: modelId={}, modelKey={}, field={}, region={}, error={}",
                    modelId, modelKey, fieldName, currentRegionCode, e.getMessage());
        }
        return null;
    }

    private Double findValueInStepResults(JsonNode steps,
                                          String targetRegionCode,
                                          String modelKey,
                                          String fieldName,
                                          String stepCodeFilter) {
        if (steps == null || !steps.isArray()) {
            return null;
        }
        for (JsonNode step : steps) {
            if (stepCodeFilter != null && !stepCodeFilter.trim().isEmpty()) {
                String currentStepCode = step.path("stepCode").asText("");
                if (!stepCodeFilter.trim().equalsIgnoreCase(currentStepCode)) {
                    continue;
                }
            }
            JsonNode regionNode = findRegionNode(step.path("regionResults"), targetRegionCode);
            if (regionNode == null || regionNode.isMissingNode()) {
                continue;
            }
            Double direct = nodeToDouble(regionNode.get(fieldName));
            if (direct != null) {
                return direct;
            }
            Double byModelKey = extractByModelKey(modelKey, fieldName, regionNode);
            if (byModelKey != null) {
                return byModelKey;
            }
        }
        return null;
    }

    private JsonNode findRegionNode(JsonNode regionResults, String regionCode) {
        if (regionResults == null || !regionResults.isObject() || regionCode == null || regionCode.trim().isEmpty()) {
            return null;
        }
        String normalized = regionCode.trim();
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(normalized);
        if (normalized.length() == 6) {
            candidates.add(normalized + "000");
            candidates.add(normalized + "000000");
        } else if (normalized.length() == 9) {
            candidates.add(normalized + "000");
            candidates.add(normalized.substring(0, 6));
        } else if (normalized.length() == 12) {
            candidates.add(normalized.substring(0, 9));
            candidates.add(normalized.substring(0, 6));
        }
        for (String candidate : candidates) {
            JsonNode node = regionResults.get(candidate);
            if (node != null && !node.isMissingNode()) {
                return node;
            }
        }
        return null;
    }

    private Double extractByModelKey(String modelKey, String fieldName, JsonNode regionNode) {
        if ("government".equals(modelKey)) {
            return extractGovernmentScore(fieldName, regionNode);
        }
        if ("enterprise".equals(modelKey) || "socialOrganization".equals(modelKey)) {
            return extractEnterpriseScore(fieldName, regionNode);
        }
        if ("family".equals(modelKey)) {
            switch (fieldName) {
                case "family_vulnerability_score":
                    return nodeToDouble(regionNode.get("l1_vul_score"));
                case "family_material_score":
                    return nodeToDouble(regionNode.get("l1_mat_score"));
                case "family_information_score":
                    return nodeToDouble(regionNode.get("l1_info_score"));
                case "family_self_rescue_score":
                    return nodeToDouble(regionNode.get("l1_self_score"));
                default:
                    return null;
            }
        }
        return null;
    }

    private Double extractGovernmentScore(String fieldName, JsonNode regionNode) {
        switch (fieldName) {
            case "management_capability":
            case "gov_management_score":
                return nodeToDouble(regionNode.get("management_capability"));
            case "engineering_defense_capability":
            case "gov_engineering_score":
                return nodeToDouble(regionNode.get("engineering_defense_capability"));
            case "monitoring_warning_capability":
            case "gov_monitoring_score":
                return nodeToDouble(regionNode.get("monitoring_warning_capability"));
            case "material_reserve_capability":
            case "gov_material_score":
                return nodeToDouble(regionNode.get("material_reserve_capability"));
            case "professional_rescue_capability":
            case "gov_rescue_team_score":
                return nodeToDouble(regionNode.get("professional_rescue_capability"));
            case "relocation_resettlement_capability":
            case "gov_relocation_score":
                return nodeToDouble(regionNode.get("relocation_resettlement_capability"));
            default:
                return null;
        }
    }

    private Double extractEnterpriseScore(String fieldName, JsonNode regionNode) {
        Double engineering = nodeToDouble(regionNode.get("engineering_rescue_capacity"));
        Double insurance = nodeToDouble(regionNode.get("insurance_reinsurance_capacity"));
        switch (fieldName) {
            case "engineering_rescue_capacity":
            case "management_capability_score":
                return engineering;
            case "insurance_reinsurance_capacity":
            case "support_capability_score":
                return insurance;
            case "self_rescue_capability_score":
                return engineering;
            case "comprehensive_capability_score":
                return insurance;
            default:
                return null;
        }
    }

    private String normalizeRegionCodeByModelKey(String currentRegionCode, String modelKey) {
        if (currentRegionCode == null) {
            return null;
        }
        String normalized = currentRegionCode.trim();
        if (normalized.length() > 6) {
            return normalized.substring(0, 6);
        }
        return normalized;
    }

    private Double nodeToDouble(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.doubleValue();
        }
        try {
            return Double.parseDouble(node.asText().trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从当前执行上下文（step_ 前缀条目）中查找前置模型的实时计算结果，
     * 避免读取数据库中历史残留的旧结果。
     */
    @SuppressWarnings("unchecked")
    private Double loadValueFromCurrentExecution(Long targetModelId, String stepCode,
                                                  String fieldName, String currentRegionCode,
                                                  List<String> candidateRegionCodes,
                                                  Map<String, Object> regionContext) {
        if (regionContext == null || targetModelId == null || fieldName == null) {
            return null;
        }

        // 遍历所有 step_ 前缀的条目，查找包含匹配 regionCode 的 regionResults
        for (Map.Entry<String, Object> entry : regionContext.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("step_")) {
                continue;
            }
            if (!(entry.getValue() instanceof Map)) {
                continue;
            }
            Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
            Object modelIdObj = stepResult.get("modelId");
            if (modelIdObj != null && targetModelId.equals(((Number) modelIdObj).longValue())) {
                // 匹配 modelId 后，从 regionResults 中查找
                Object rrObj = stepResult.get("regionResults");
                if (!(rrObj instanceof Map)) {
                    continue;
                }
                Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) rrObj;
                for (String candidateCode : candidateRegionCodes) {
                    Map<String, Object> outputs = regionResults.get(candidateCode);
                    if (outputs != null && outputs.containsKey(fieldName)) {
                        return toDouble(outputs.get(fieldName));
                    }
                }
            }
        }
        return null;
    }

    private List<String> resolveCandidateRegionCodes(String currentRegionCode, String modelKey) {
        if (currentRegionCode == null || currentRegionCode.trim().isEmpty()) {
            return Collections.singletonList(currentRegionCode);
        }
        String normalized = currentRegionCode.trim();
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(normalized);

        if ("communityCountyUnit".equals(modelKey)) {
            if (normalized.length() == 6) {
                candidates.add(normalized + "000");
            } else if (normalized.length() > 6) {
                candidates.add(normalized.substring(0, 6));
            }
        } else if (normalized.length() > 6) {
            candidates.add(normalized.substring(0, 6));
        }

        return new ArrayList<>(candidates);
    }

    private Double loadSocialOrganizationFallback(String fieldName, String currentRegionCode, Integer year) {
        if (currentRegionCode == null || currentRegionCode.trim().isEmpty()) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT emergency_equipment_material_value, passenger_vehicle_count, freight_vehicle_count, ");
        sql.append("special_operation_vehicle_count, last_year_science_education_audience, population ");
        sql.append("FROM ").append(SOCIAL_ORGANIZATION_CAPACITY_TABLE).append(" WHERE region_code LIKE ? ");
        List<Object> params = new ArrayList<>();
        params.add(currentRegionCode.trim() + "%");
        if (year != null) {
            sql.append("AND year = ? ");
            params.add(year);
        }
        sql.append("ORDER BY CHAR_LENGTH(region_code) DESC, id DESC LIMIT 1");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Map<String, Object> row = rows.get(0);
        double material = toDoubleValue(row.get("emergency_equipment_material_value"));
        double passenger = toDoubleValue(row.get("passenger_vehicle_count"));
        double freight = toDoubleValue(row.get("freight_vehicle_count"));
        double special = toDoubleValue(row.get("special_operation_vehicle_count"));
        double audience = toDoubleValue(row.get("last_year_science_education_audience"));
        double population = toDoubleValue(row.get("population"));

        switch (fieldName) {
            case "management_capability_score":
                return material;
            case "support_capability_score":
                return passenger + freight;
            case "self_rescue_capability_score":
                return special;
            case "comprehensive_capability_score":
                return population > 0 ? (audience / population) : audience;
            default:
                return null;
        }
    }

    private Double loadSocialOrganizationWeightedIndicator(String fieldName,
                                                           String currentRegionCode,
                                                           Map<String, Object> regionContext) {
        Integer year = parseInteger(regionContext.get("year"));
        String current = normalizeCountyCode(currentRegionCode);
        if (year == null || current == null) {
            return null;
        }

        List<String> regionCodes = extractRegionCodesFromContext(regionContext);
        if (regionCodes.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT region_code, population, emergency_equipment_material_value, ");
        sql.append("passenger_vehicle_count, freight_vehicle_count, special_operation_vehicle_count, ");
        sql.append("last_year_science_education_audience ");
        sql.append("FROM ").append(SOCIAL_ORGANIZATION_CAPACITY_TABLE).append(" ");
        sql.append("WHERE year = ? AND region_code IN (");
        for (int i = 0; i < regionCodes.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        List<Object> params = new ArrayList<>();
        params.add(year);
        params.addAll(regionCodes);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Map<String, Double> metricByRegion = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String region = normalizeCountyCode(toString(row.get("region_code")));
            if (region == null) {
                continue;
            }
            double population = toDoubleValue(row.get("population"));
            if (population <= 0) {
                metricByRegion.put(region, 0.0);
                continue;
            }
            double value;
            switch (fieldName) {
                case "large_excavator_owning_rate":
                    value = toDoubleValue(row.get("emergency_equipment_material_value")) / population;
                    break;
                case "large_truck_crane_owning_rate":
                    value = (toDoubleValue(row.get("passenger_vehicle_count"))
                            + toDoubleValue(row.get("freight_vehicle_count"))) / population * 10000.0;
                    break;
                case "large_loader_owning_rate":
                    value = toDoubleValue(row.get("special_operation_vehicle_count")) / population * 10000.0;
                    break;
                case "disaster_insurance_claim_capacity":
                    value = toDoubleValue(row.get("last_year_science_education_audience")) / population;
                    break;
                default:
                    return null;
            }
            metricByRegion.put(region, value);
        }

        if (!metricByRegion.containsKey(current)) {
            return 0.0;
        }

        double normBase = 0.0;
        for (Double val : metricByRegion.values()) {
            if (val != null) {
                normBase += val * val;
            }
        }
        normBase = Math.sqrt(normBase);
        if (normBase <= 0.0) {
            return 0.0;
        }

        double normalized = metricByRegion.getOrDefault(current, 0.0) / normBase;
        return normalized * 0.25;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRegionCodesFromContext(Map<String, Object> regionContext) {
        Object raw = regionContext.get("regionCodes");
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Object item : (List<Object>) raw) {
            String code = normalizeCountyCode(toString(item));
            if (code != null) {
                set.add(code);
            }
        }
        return new ArrayList<>(set);
    }

    private String normalizeCountyCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.length() >= 6) {
            return trimmed.substring(0, 6);
        }
        return trimmed;
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String toString(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString();
        return s == null ? null : s.trim();
    }

    private double toDoubleValue(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public Double normalize(
            String indicatorName,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {



        // 1. 收集所有区域的指标值
        List<Double> allValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(indicatorName);


            // 特别为 riskAssessment 添加详细调试
            if ("riskAssessment".equals(indicatorName) && log.isDebugEnabled()) {
                log.debug("[DEBUG-RISK] 地区={}的完整数据keys: {}", entry.getKey(), entry.getValue().keySet());
                log.debug("[DEBUG-RISK] 是否包含riskAssessment: {}", entry.getValue().containsKey("riskAssessment"));
                log.debug("[DEBUG-RISK] riskAssessment值: {}", value);
            }

            if (value != null) {
                allValues.add(toDouble(value));
            }
        }



        if (allValues.isEmpty()) {
            log.warn("未找到任何指标值: {}", indicatorName);
            return 0.0;
        }

        // 2. 计算平方和的平方根：SQRT(SUMSQ(all_values))
        double sumSquares = allValues.stream()
                .mapToDouble(v -> v * v)
                .sum();
        double denominator = Math.sqrt(sumSquares);

        if (denominator == 0) {
            log.warn("分母为0，所有值都是0或接近0，直接返回当前值: indicator={}", indicatorName);
            // 当分母为0时，说明所有地区的该指标值都是0或接近0
            // 这种情况下，归一化没有意义，直接返回当前区域的值
            Map<String, Object> currentData = allRegionData.get(currentRegionCode);
            if (currentData != null) {
                Object currentValue = currentData.get(indicatorName);
                if (currentValue != null) {
                    return toDouble(currentValue);
                }
            }
            return 0.0;
        }

        // 3. 获取当前区域的值
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }

        Object currentValue = currentData.get(indicatorName);
        if (currentValue == null) {
            log.warn("未找到当前区域指标值: region={}, indicator={}", currentRegionCode, indicatorName);
            return 0.0;
        }

        // 4. 计算归一化值
        double normalized = toDouble(currentValue) / denominator;

        return normalized;
    }

    @Override
    public Double calculateTopsisPositive(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {

        log.debug("TOPSIS优解计算: indicators={}, region={}", indicators, currentRegionCode);

        // 检查是否为单区域情况
        if (allRegionData.size() == 1) {
            log.info("[TOPSIS-DEBUG] 单区域情况，计算优解距离");
            // 对于单区域情况，计算与理论最优值的距离
            return calculateSingleRegionPositiveDistance(indicators, currentRegionCode, allRegionData);
        }

        // 1. 解析指标列表
        String[] indicatorArray = indicators.split(",");

        // 2. 获取当前区域数据
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }

        // 3. 计算每个指标的 (max_value - current_value)^2
        double sumSquares = 0.0;

        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();

            // 收集所有区域该指标的值
            List<Double> allValues = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
                Object value = entry.getValue().get(trimmedIndicator);
                if (value != null) {
                    allValues.add(toDouble(value));
                }
            }

            if (allValues.isEmpty()) {
                log.warn("未找到指标值: {}", trimmedIndicator);
                continue;
            }

            // 找到最大值（正理想解）
            double maxValue = allValues.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            // 获取当前值
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue == null) {
                log.warn("当前区域未找到指标: region={}, indicator={}", currentRegionCode, trimmedIndicator);
                continue;
            }

            double current = toDouble(currentValue);
            double diff = maxValue - current;
            sumSquares += diff * diff;

            log.debug("指标 {}: max={}, current={}, diff^2={}", trimmedIndicator, maxValue, current, diff * diff);
        }

        // 4. 返回距离：SQRT(sumSquares)
        double distance = Math.sqrt(sumSquares);

        log.debug("TOPSIS优解距离: region={}, distance={}", currentRegionCode, distance);

        return distance;
    }

    @Override
    public Double calculateTopsisNegative(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {

        log.debug("TOPSIS劣解计算: indicators={}, region={}", indicators, currentRegionCode);

        // 检查是否为单区域情况
        if (allRegionData.size() == 1) {
            log.info("[TOPSIS-DEBUG] 单区域情况，计算劣解距离");
            // 对于单区域情况，计算与理论最差值的距离
            // 这里使用指标权重的平方和作为基准
            return calculateSingleRegionNegativeDistance(indicators, currentRegionCode, allRegionData);
        }

        // 1. 解析指标列表
        String[] indicatorArray = indicators.split(",");

        // 2. 获取当前区域数据
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }

        // 3. 计算每个指标的 (min_value - current_value)^2
        double sumSquares = 0.0;

        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();

            // 收集所有区域该指标的值
            List<Double> allValues = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
                Object value = entry.getValue().get(trimmedIndicator);
                if (value != null) {
                    allValues.add(toDouble(value));
                }
            }

            if (allValues.isEmpty()) {
                log.warn("未找到指标值: {}", trimmedIndicator);
                continue;
            }

            // 找到最小值（负理想解）
            double minValue = allValues.stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0);

            // 获取当前值
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue == null) {
                log.warn("当前区域未找到指标: region={}, indicator={}", currentRegionCode, trimmedIndicator);
                continue;
            }

            double current = toDouble(currentValue);
            double diff = minValue - current;
            sumSquares += diff * diff;

            log.debug("指标 {}: min={}, current={}, diff^2={}", trimmedIndicator, minValue, current, diff * diff);
        }

        // 4. 返回距离：SQRT(sumSquares)
        double distance = Math.sqrt(sumSquares);

        log.debug("TOPSIS劣解距离: region={}, distance={}", currentRegionCode, distance);

        return distance;
    }

    /**
     * 计算TOPSIS得分
     * 公式：TOPSIS_SCORE = D- / (D+ + D-)
     *
     * @param params 参数格式："POSITIVE_IDEAL_FIELD,NEGATIVE_IDEAL_FIELD"
     * @param currentRegionCode 当前区域代码
     * @param allRegionData 所有区域数据
     * @return TOPSIS得分（0-1之间）
     */
    public Double calculateTopsisScore(
            String params,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {


        // 1. 解析参数：正理想解字段名,负理想解字段名
        String[] fields = params.split(",");
        if (fields.length != 2) {
            return 0.0;
        }

        String positiveField = fields[0].trim();
        String negativeField = fields[1].trim();

        // 2. 获取当前区域数据
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }

        // 3. 获取正理想解距离 D+
        Object positiveValue = currentData.get(positiveField);
        if (positiveValue == null) {
            log.warn("未找到正理想解距离: region={}, field={}", currentRegionCode, positiveField);
            return 0.0;
        }
        double dPositive = toDouble(positiveValue);

        // 4. 获取负理想解距离 D-
        Object negativeValue = currentData.get(negativeField);
        if (negativeValue == null) {
            log.warn("未找到负理想解距离: region={}, field={}", currentRegionCode, negativeField);
            return 0.0;
        }
        double dNegative = toDouble(negativeValue);

        // 5. 计算TOPSIS得分：D- / (D+ + D-)
        double denominator = dPositive + dNegative;
        if (denominator == 0) {
            // 所有方案在该指标上完全一致时，D+=D-=0，按无差异满分处理
            if (Math.abs(dPositive) < 1e-12 && Math.abs(dNegative) < 1e-12) {
                return 1.0;
            }
            return 0.0;
        }

        double score = dNegative / denominator;
        return score;
    }

    @Override
    public String calculateGrade(
            String scoreField,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {

        if (log.isDebugEnabled()) {
            log.debug("[@GRADE] 开始分级计算: scoreField={}, currentRegionCode={}, totalRegions={}",
                    scoreField, currentRegionCode, allRegionData.size());
        }

        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return "中等";
        }

        @SuppressWarnings("unchecked")
        Map<String, double[]> gradeStats = (Map<String, double[]>) currentData.get("gradeStats");
        double mean = 0.0;
        double stdev = 0.0;
        int n = 0;

        if (gradeStats != null && gradeStats.containsKey(scoreField)) {
            double[] stats = gradeStats.get(scoreField);
            if (stats != null && stats.length >= 3) {
                mean = stats[0];
                stdev = stats[1];
                n = (int) stats[2];
            }
        } else {
            List<Double> allScores = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
                Object value = entry.getValue().get(scoreField);
                if (log.isDebugEnabled()) {
                    log.debug("[@GRADE] 地区 {} 的 {} = {}", entry.getKey(), scoreField, value);
                }
                if (value != null) {
                    double scoreValue = toDouble(value);
                    allScores.add(scoreValue);
                    log.debug("[分级调试] 地区 {} 的 {} = {}", entry.getKey(), scoreField, scoreValue);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("[@GRADE] 地区 {} 的 {} 为 NULL，可用的键: {}", entry.getKey(), scoreField,
                                String.join(", ", entry.getValue().keySet().stream().limit(10).collect(java.util.stream.Collectors.toList())));
                    }
                }
            }

            if (allScores.isEmpty()) {
                log.error("[@GRADE] 未找到任何分数值: scoreField={}, currentRegionCode={}", scoreField, currentRegionCode);
                return "中等";
            }

            mean = allScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            n = allScores.size();
            if (n <= 1) {
                log.warn("样本数量不足，无法计算标准差: {}", n);
                return handleSingleRegionGrading(scoreField, currentRegionCode, allRegionData);
            }
            double sumSquaredDiff = 0.0;
            double finalMean = mean;
            for (Double v : allScores) {
                double diff = v - finalMean;
                sumSquaredDiff += diff * diff;
            }
            stdev = Math.sqrt(sumSquaredDiff / (n - 1));
        }

        if (n <= 1) {
            log.warn("样本数量不足，无法计算标准差: {}", n);
            return handleSingleRegionGrading(scoreField, currentRegionCode, allRegionData);
        }

        Object currentValue = currentData.get(scoreField);
        if (currentValue == null) {
            log.warn("未找到当前区域分数: region={}, field={}", currentRegionCode, scoreField);
            return "中等";
        }

        double score = toDouble(currentValue);

        // 5. 根据分级规则计算等级
        String grade = determineGrade(score, mean, stdev);

        if (log.isDebugEnabled()) {
            log.debug("[@GRADE] 分级完成: region={}, scoreField={}, score={}, mean={}, stdev={}, grade={}",
                    currentRegionCode, scoreField, String.format("%.4f", score),
                    String.format("%.4f", mean), String.format("%.4f", stdev), grade);
        }

        return grade;
    }

    /**
     * 计算单区域优解距离
     * 对于单区域情况，我们计算与理论最优值的距离
     */
    private Double calculateSingleRegionPositiveDistance(String indicators, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        String[] indicatorArray = indicators.split(",");
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);

        double sumSquares = 0.0;
        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue != null) {
                double current = toDouble(currentValue);
                // 假设理论最优值是当前值的120%（还有上升空间）
                double theoreticalMax = current * 1.2;
                double diff = theoreticalMax - current;
                sumSquares += diff * diff;
                log.debug("[单区域优解] 指标 {}: current={}, theoreticalMax={}, diff^2={}", trimmedIndicator, current, theoreticalMax, diff * diff);
            }
        }

        double distance = Math.sqrt(sumSquares);
        log.debug("[单区域优解] 距离: {}", distance);
        return distance;
    }

    /**
     * 计算单区域劣解距离
     * 对于单区域情况，我们计算与理论最差值的距离
     */
    private Double calculateSingleRegionNegativeDistance(String indicators, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        String[] indicatorArray = indicators.split(",");
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);

        double sumSquares = 0.0;
        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue != null) {
                double current = toDouble(currentValue);
                // 假设理论最差值是0（或当前值的20%）
                double theoreticalMin = Math.max(0, current * 0.2);
                double diff = theoreticalMin - current;
                sumSquares += diff * diff;
                log.debug("[单区域劣解] 指标 {}: current={}, theoreticalMin={}, diff^2={}", trimmedIndicator, current, theoreticalMin, diff * diff);
            }
        }

        double distance = Math.sqrt(sumSquares);
        log.debug("[单区域劣解] 距离: {}", distance);
        return distance;
    }

    /**
     * 处理单区域分级情况
     * 对于单区域情况，由于无法进行统计分析，我们基于实际值进行分级
     * 对于瑞峰镇（511425108），根据之前的batch计算结果，其综合减灾能力值为0.766，属于高水平
     */
    private String handleSingleRegionGrading(String scoreField, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        // 获取当前分数
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            return "中等";
        }

        Object scoreValue = currentData.get(scoreField);
        if (scoreValue == null) {
            return "中等";
        }

        double score = toDouble(scoreValue);

        // 如果分数为NaN（TOPSIS计算失败），给予保守分级
        if (Double.isNaN(score)) {
            return "中等";
        }

        // 对于单区域情况，基于分数的绝对值进行分级
        String grade;
        if (score >= 0.8) {
            grade = "强";
        } else if (score >= 0.6) {
            grade = "较强";
        } else if (score >= 0.4) {
            grade = "中等";
        } else if (score >= 0.2) {
            grade = "较弱";
        } else {
            grade = "弱";
        }

        log.info("[单区域分级] {} 分数={} 等级={}", scoreField, String.format("%.4f", score), grade);
        return grade;
    }

    /**
     * 根据分级规则确定等级
     *
     * 规则：
     * 如果 μ <= 0.5σ:
     *   value >= μ+1.5σ → 强
     *   value >= μ+0.5σ → 较强
     *   否则 → 中等
     *
     * 如果 μ <= 1.5σ:
     *   value >= μ+1.5σ → 强
     *   value >= μ+0.5σ → 较强
     *   value >= μ-0.5σ → 中等
     *   否则 → 较弱
     *
     * 否则:
     *   value >= μ+1.5σ → 强
     *   value >= μ+0.5σ → 较强
     *   value >= μ-0.5σ → 中等
     *   value >= μ-1.5σ → 较弱
     *   否则 → 弱
     */
private String determineGrade(double value, double mean, double stdev) {
        // 计算关键节点
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = Math.min(1.0, mean + oneAndHalfStdev);
        double meanMinusHalf = mean - halfStdev;
        double meanMinusOneAndHalf = mean - oneAndHalfStdev;



        // 确保值不小于0（根据规则中的[0,...)区间)
        value = Math.max(0, value);

        if (mean <= halfStdev) {
            // 情况1：μ ≤ 0.5σ，分为3级
            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else {
                return "中等";
            }
        } else if (mean <= oneAndHalfStdev) {
            // 情况2：0.5σ < μ ≤ 1.5σ，分为4级

            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else if (value >= meanMinusHalf) {
                return "中等";
            } else {
                return "较弱";
            }
        } else {
            // 情况3：μ > 1.5σ，默认情况，使用5级分类
            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else if (value >= meanMinusHalf) {
                return "中等";
            } else if (value >= meanMinusOneAndHalf) {
                return "较弱";
            } else {
                return "弱";
            }
        }
    }

    /**
     * 将对象转换为Double
     */
    private Double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            double v = ((Number) value).doubleValue();
            return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v;
        }
        if (value instanceof String) {
            try {
                double v = Double.parseDouble((String) value);
                return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v;
            } catch (NumberFormatException e) {
                log.warn("无法将字符串转换为数字: {}", value);
                return 0.0;
            }
        }
        log.warn("无法转换为Double的类型: {}", value.getClass());
        return 0.0;
    }
}
