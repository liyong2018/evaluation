package com.evaluate.service;

import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.service.ModelExecutionService;
import com.evaluate.service.adapter.DataSourceAdapter;
import com.evaluate.service.adapter.impl.CommunityDataSourceAdapter;
import com.evaluate.service.adapter.impl.TownshipDataSourceAdapter;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统一评估引擎
 * 整合不同数据源，提供统一的评估计算接口
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class UnifiedEvaluationEngine {

    @Autowired
    private TownshipDataSourceAdapter townshipDataSourceAdapter;

    @Autowired
    private CommunityDataSourceAdapter communityDataSourceAdapter;

    @Autowired
    private ModelExecutionService modelExecutionService;

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    /**
     * 支持的数据源类型
     */
    private static final Map<String, String> DATA_SOURCE_TYPES = new HashMap<>();
    static {
        DATA_SOURCE_TYPES.put("TOWNSHIP", "乡镇评估模型");
        DATA_SOURCE_TYPES.put("COMMUNITY", "社区评估模型");
        DATA_SOURCE_TYPES.put("STANDARD", "标准评估模型");
    }

    /**
     * 执行统一评估计算
     *
     * @param request 评估请求参数
     * @return 评估结果
     */
    public Map<String, Object> executeEvaluation(EvaluationRequest request) {
        log.info("开始执行统一评估: {}", request);

        try {
            // 1. 验证请求参数
            validateRequest(request);

            // 2. 获取数据源适配器
            DataSourceAdapter adapter = getDataSourceAdapter(request.getDataSourceType());
            if (adapter == null) {
                throw new IllegalArgumentException("不支持的数据源类型: " + request.getDataSourceType());
            }

            // 3. 验证数据源
            if (!adapter.validateDataSource(request.getRegionCodes(), request.getWeightConfigId())) {
                throw new IllegalArgumentException("数据源验证失败");
            }

            // 4. 获取算法配置
            Map<String, Object> algorithmDetail = modelExecutionService.getAlgorithmStepsInfo(request.getAlgorithmId());
            if (algorithmDetail == null || !Boolean.TRUE.equals(algorithmDetail.get("success"))) {
                throw new IllegalArgumentException("算法配置不存在");
            }

            // 5. 获取地区名称映射
            Map<String, String> regionNames = adapter.getRegionNames(request.getRegionCodes());

            // 6. 获取权重配置
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(request.getWeightConfigId());
            Map<String, Double> weightMap = indicatorWeights.stream()
                .collect(Collectors.toMap(IndicatorWeight::getIndicatorCode, IndicatorWeight::getWeight));

            // 7. 执行评估计算
            Map<String, Object> result = performEvaluation(adapter, algorithmDetail, request, regionNames, weightMap);

            log.info("统一评估执行完成: {}", result.get("summary"));
            return result;

        } catch (Exception e) {
            log.error("统一评估执行失败", e);
            throw new RuntimeException("统一评估执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行步骤评估计算
     *
     * @param request 步骤评估请求参数
     * @return 步骤评估结果
     */
    public Map<String, Object> executeStepEvaluation(StepEvaluationRequest request) {
        log.info("开始执行步骤评估: {}", request);

        try {
            // 1. 验证请求参数
            validateStepRequest(request);

            // 2. 获取数据源适配器
            DataSourceAdapter adapter = getDataSourceAdapter(request.getDataSourceType());
            if (adapter == null) {
                throw new IllegalArgumentException("不支持的数据源类型: " + request.getDataSourceType());
            }

            // 3. 验证数据源
            if (!adapter.validateDataSource(request.getRegionCodes(), request.getWeightConfigId())) {
                throw new IllegalArgumentException("数据源验证失败");
            }

            // 4. 获取算法配置
            Map<String, Object> algorithmDetail = modelExecutionService.getAlgorithmStepsInfo(request.getAlgorithmId());
            if (algorithmDetail == null || !Boolean.TRUE.equals(algorithmDetail.get("success"))) {
                throw new IllegalArgumentException("算法配置不存在");
            }

            // 5. 获取地区名称映射
            Map<String, String> regionNames = adapter.getRegionNames(request.getRegionCodes());

            // 6. 获取权重配置
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigId(request.getWeightConfigId());
            Map<String, Double> weightMap = indicatorWeights.stream()
                .collect(Collectors.toMap(IndicatorWeight::getIndicatorCode, IndicatorWeight::getWeight));

            // 7. 执行步骤计算
            Map<String, Object> result = performStepEvaluation(adapter, algorithmDetail, request, regionNames, weightMap);

            log.info("步骤评估执行完成: stepOrder={}", request.getStepOrder());
            return result;

        } catch (Exception e) {
            log.error("步骤评估执行失败", e);
            throw new RuntimeException("步骤评估执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取支持的数据源类型
     */
    public Map<String, String> getSupportedDataSourceTypes() {
        return new HashMap<>(DATA_SOURCE_TYPES);
    }

    /**
     * 获取数据源适配器
     */
    private DataSourceAdapter getDataSourceAdapter(String dataSourceType) {
        switch (dataSourceType.toUpperCase()) {
            case "TOWNSHIP":
                return townshipDataSourceAdapter;
            case "COMMUNITY":
                return communityDataSourceAdapter;
            default:
                return null;
        }
    }

    /**
     * 验证评估请求参数
     */
    private void validateRequest(EvaluationRequest request) {
        if (request.getDataSourceType() == null || request.getDataSourceType().trim().isEmpty()) {
            throw new IllegalArgumentException("数据源类型不能为空");
        }
        if (request.getAlgorithmId() == null) {
            throw new IllegalArgumentException("算法ID不能为空");
        }
        if (request.getWeightConfigId() == null) {
            throw new IllegalArgumentException("权重配置ID不能为空");
        }
        if (request.getRegionCodes() == null || request.getRegionCodes().isEmpty()) {
            throw new IllegalArgumentException("地区代码列表不能为空");
        }
    }

    /**
     * 验证步骤评估请求参数
     */
    private void validateStepRequest(StepEvaluationRequest request) {
        validateRequest(request);
        if (request.getStepOrder() == null || request.getStepOrder() <= 0) {
            throw new IllegalArgumentException("步骤顺序必须是正整数");
        }
    }

    /**
     * 执行完整评估计算
     */
    private Map<String, Object> performEvaluation(
            DataSourceAdapter adapter,
            Map<String, Object> algorithmDetail,
            EvaluationRequest request,
            Map<String, String> regionNames,
            Map<String, Double> weightMap) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取算法步骤
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> steps = (List<Map<String, Object>>) algorithmDetail.get("steps");

            // 获取指标数据
            Map<String, Map<String, Double>> indicatorData = adapter.getIndicatorData(
                request.getRegionCodes(), request.getWeightConfigId());

            // 存储所有步骤的结果
            Map<Integer, Map<String, Object>> stepResults = new LinkedHashMap<>();

            // 创建QLExpress运行器
            ExpressRunner runner = new ExpressRunner();

            // 按顺序执行每个步骤
            for (Map<String, Object> step : steps) {
                Integer stepOrder = (Integer) step.get("stepOrder");
                String stepName = (String) step.get("stepName");

                // 获取步骤算法（这里简化处理，暂时使用步骤信息本身）
                // TODO: 后续需要根据实际需求完善步骤算法的获取
                List<StepAlgorithm> stepAlgorithms = new ArrayList<>();

                // 执行步骤计算
                Map<String, Object> stepResult = executeStep(
                    runner, stepOrder, stepName, stepAlgorithms, indicatorData, weightMap, regionNames);

                stepResults.put(stepOrder, stepResult);

                // 将当前步骤的结果作为下一步骤的输入
                updateIndicatorDataForNextStep(indicatorData, stepResult);
            }

            // 构建最终结果
            result.put("success", true);
            result.put("dataSourceType", request.getDataSourceType());
            result.put("algorithmId", request.getAlgorithmId());
            result.put("weightConfigId", request.getWeightConfigId());
            result.put("regionCodes", request.getRegionCodes());
            result.put("regionNames", regionNames);
            result.put("stepResults", stepResults);
            result.put("finalScores", extractFinalScores(stepResults));
            result.put("summary", buildSummary(request, stepResults));

            return result;

        } catch (Exception e) {
            log.error("执行评估计算失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 执行单步骤评估计算
     */
    private Map<String, Object> performStepEvaluation(
            DataSourceAdapter adapter,
            Map<String, Object> algorithmDetail,
            StepEvaluationRequest request,
            Map<String, String> regionNames,
            Map<String, Double> weightMap) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取指定步骤
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> steps = (List<Map<String, Object>>) algorithmDetail.get("steps");
            Optional<Map<String, Object>> stepOpt = steps.stream()
                .filter(step -> request.getStepOrder().equals(step.get("stepOrder")))
                .findFirst();

            if (!stepOpt.isPresent()) {
                throw new IllegalArgumentException("步骤 " + request.getStepOrder() + " 不存在");
            }

            Map<String, Object> step = stepOpt.get();
            String stepName = (String) step.get("stepName");

            // 获取步骤算法（简化处理）
            // TODO: 后续需要根据实际需求完善步骤算法的获取
            List<StepAlgorithm> stepAlgorithms = new ArrayList<>();

            // 获取指标数据
            Map<String, Map<String, Double>> indicatorData = adapter.getIndicatorData(
                request.getRegionCodes(), request.getWeightConfigId());

            // 创建QLExpress运行器
            ExpressRunner runner = new ExpressRunner();

            // 执行步骤计算
            Map<String, Object> stepResult = executeStep(
                runner, request.getStepOrder(), stepName, stepAlgorithms, indicatorData, weightMap, regionNames);

            // 构建结果
            result.put("success", true);
            result.put("dataSourceType", request.getDataSourceType());
            result.put("algorithmId", request.getAlgorithmId());
            result.put("stepOrder", request.getStepOrder());
            result.put("stepName", stepName);
            result.put("regionCodes", request.getRegionCodes());
            result.put("regionNames", regionNames);
            result.put("stepResult", stepResult);
            result.put("summary", String.format("步骤 %s (%d) 计算完成，共处理 %d 个地区",
                stepName, request.getStepOrder(), stepResult.size()));

            return result;

        } catch (Exception e) {
            log.error("执行步骤评估计算失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 执行单个步骤的计算
     */
    private Map<String, Object> executeStep(
            ExpressRunner runner,
            Integer stepOrder,
            String stepName,
            List<StepAlgorithm> stepAlgorithms,
            Map<String, Map<String, Double>> indicatorData,
            Map<String, Double> weightMap,
            Map<String, String> regionNames) {

        Map<String, Object> stepResult = new LinkedHashMap<>();

        try {
            // 为每个地区执行计算
            for (Map.Entry<String, Map<String, Double>> entry : indicatorData.entrySet()) {
                String regionCode = entry.getKey();
                Map<String, Double> regionData = entry.getValue();

                // 创建QLExpress上下文
                IExpressContext<String, Object> context = new DefaultContext<>();

                // 添加地区数据到上下文
                regionData.forEach(context::put);

                // 添加权重到上下文
                weightMap.forEach(context::put);

                // 执行算法表达式
                Map<String, Object> regionResult = new HashMap<>();
                regionResult.put("regionCode", regionCode);
                regionResult.put("regionName", regionNames.get(regionCode));
                regionResult.put("stepOrder", stepOrder);
                regionResult.put("stepName", stepName);

                // 如果有步骤算法，执行算法表达式
                if (!stepAlgorithms.isEmpty()) {
                    for (StepAlgorithm algorithm : stepAlgorithms) {
                        try {
                            Object value = runner.execute(algorithm.getQlExpression(), context, null, true, false);
                            regionResult.put(algorithm.getOutputParam(), value);
                        } catch (Exception e) {
                            log.warn("执行算法表达式失败: algorithm={}, expression={}, error={}",
                                algorithm.getAlgorithmName(), algorithm.getQlExpression(), e.getMessage());
                            regionResult.put(algorithm.getOutputParam(), 0.0); // 默认值
                        }
                    }
                } else {
                    // 如果没有具体算法，执行基础的加权求和计算
                    double weightedSum = 0.0;
                    for (Map.Entry<String, Double> dataEntry : regionData.entrySet()) {
                        String indicatorCode = dataEntry.getKey();
                        Double indicatorValue = dataEntry.getValue();
                        Double weight = weightMap.get(indicatorCode);

                        if (indicatorValue != null && weight != null) {
                            weightedSum += indicatorValue * weight;
                        }
                    }

                    // 生成步骤结果
                    String stepOutputKey = "STEP_" + stepOrder + "_SCORE";
                    regionResult.put(stepOutputKey, weightedSum);

                    // 如果是最后一步，也生成总分
                    if (stepOrder != null && stepOrder == 5) {
                        regionResult.put("FINAL_SCORE", weightedSum);
                    }
                }

                stepResult.put(regionCode, regionResult);
            }

        } catch (Exception e) {
            log.error("执行步骤计算失败: stepOrder={}, stepName={}", stepOrder, stepName, e);
            throw new RuntimeException("步骤计算失败: " + e.getMessage(), e);
        }

        return stepResult;
    }

    /**
     * 更新下一步骤的输入数据
     */
    private void updateIndicatorDataForNextStep(
            Map<String, Map<String, Double>> indicatorData,
            Map<String, Object> stepResult) {

        for (Map.Entry<String, Object> entry : stepResult.entrySet()) {
            String regionCode = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> regionResult = (Map<String, Object>) entry.getValue();

            Map<String, Double> regionIndicators = indicatorData.get(regionCode);
            if (regionIndicators == null) {
                regionIndicators = new HashMap<>();
                indicatorData.put(regionCode, regionIndicators);
            }

            // 将步骤结果中的数值添加到指标数据中
            for (Map.Entry<String, Object> resultEntry : regionResult.entrySet()) {
                String key = resultEntry.getKey();
                Object value = resultEntry.getValue();

                // 跳过非数值字段
                if ("regionCode".equals(key) || "regionName".equals(key) ||
                    "stepOrder".equals(key) || "stepName".equals(key)) {
                    continue;
                }

                if (value instanceof Number) {
                    regionIndicators.put(key, ((Number) value).doubleValue());
                }
            }
        }
    }

    /**
     * 提取最终得分
     */
    private Map<String, Double> extractFinalScores(Map<Integer, Map<String, Object>> stepResults) {
        Map<String, Double> finalScores = new HashMap<>();

        // 获取最后一步的结果
        if (!stepResults.isEmpty()) {
            Integer lastStepOrder = Collections.max(stepResults.keySet());
            Map<String, Object> lastStepResult = stepResults.get(lastStepOrder);

            for (Map.Entry<String, Object> entry : lastStepResult.entrySet()) {
                String regionCode = entry.getKey();
                @SuppressWarnings("unchecked")
                Map<String, Object> regionResult = (Map<String, Object>) entry.getValue();

                // 查找得分字段（通常命名为SCORE或类似）
                Double score = null;
                for (Map.Entry<String, Object> resultEntry : regionResult.entrySet()) {
                    if (resultEntry.getKey().toUpperCase().contains("SCORE") &&
                        resultEntry.getValue() instanceof Number) {
                        score = ((Number) resultEntry.getValue()).doubleValue();
                        break;
                    }
                }

                if (score != null) {
                    finalScores.put(regionCode, score);
                }
            }
        }

        return finalScores;
    }

    /**
     * 构建评估摘要
     */
    private String buildSummary(EvaluationRequest request, Map<Integer, Map<String, Object>> stepResults) {
        StringBuilder summary = new StringBuilder();

        summary.append(String.format("统一评估完成: 数据源=%s, 算法=%d, 权重配置=%d, ",
            request.getDataSourceType(), request.getAlgorithmId(), request.getWeightConfigId()));
        summary.append(String.format("地区数量=%d, 步骤数量=%d",
            request.getRegionCodes().size(), stepResults.size()));

        return summary.toString();
    }

    /**
     * 评估请求参数类
     */
    public static class EvaluationRequest {
        private String dataSourceType;
        private Long algorithmId;
        private Long weightConfigId;
        private List<String> regionCodes;
        private Map<String, Object> additionalParams;

        // Getters and Setters
        public String getDataSourceType() { return dataSourceType; }
        public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }

        public Long getAlgorithmId() { return algorithmId; }
        public void setAlgorithmId(Long algorithmId) { this.algorithmId = algorithmId; }

        public Long getWeightConfigId() { return weightConfigId; }
        public void setWeightConfigId(Long weightConfigId) { this.weightConfigId = weightConfigId; }

        public List<String> getRegionCodes() { return regionCodes; }
        public void setRegionCodes(List<String> regionCodes) { this.regionCodes = regionCodes; }

        public Map<String, Object> getAdditionalParams() { return additionalParams; }
        public void setAdditionalParams(Map<String, Object> additionalParams) { this.additionalParams = additionalParams; }

        @Override
        public String toString() {
            return String.format("EvaluationRequest{dataSourceType='%s', algorithmId=%d, weightConfigId=%d, regionCount=%d}",
                dataSourceType, algorithmId, weightConfigId,
                regionCodes != null ? regionCodes.size() : 0);
        }
    }

    /**
     * 步骤评估请求参数类
     */
    public static class StepEvaluationRequest extends EvaluationRequest {
        private Integer stepOrder;

        public Integer getStepOrder() { return stepOrder; }
        public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

        @Override
        public String toString() {
            return String.format("StepEvaluationRequest{dataSourceType='%s', algorithmId=%d, weightConfigId=%d, stepOrder=%d, regionCount=%d}",
                getDataSourceType(), getAlgorithmId(), getWeightConfigId(), stepOrder,
                getRegionCodes() != null ? getRegionCodes().size() : 0);
        }
    }
}