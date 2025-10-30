package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.dto.topsis.TOPSISCalculationMetrics;
import com.evaluate.dto.topsis.TOPSISDiagnosticReport;
import com.evaluate.service.UnifiedTOPSISCalculator;
import com.evaluate.service.TOPSISCalculationLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统一TOPSIS计算器实现类
 * 
 * 支持动态列配置的TOPSIS计算，包括：
 * 1. 动态指标列的理想解计算
 * 2. 欧几里得距离计算
 * 3. 单区域和多区域的不同计算策略
 * 4. 详细的计算过程日志记录
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class UnifiedTOPSISCalculatorImpl implements UnifiedTOPSISCalculator {

    private static final Logger log = LoggerFactory.getLogger(UnifiedTOPSISCalculatorImpl.class);

    
    @Autowired
    private TOPSISCalculationLogger calculationLogger;
    
    @Override
    public Map<String, Map<String, Double>> calculateDistances(
            Map<String, Map<String, Double>> weightedData,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始统一TOPSIS距离计算 - 地区数量: {}, 指标数量: {}", 
                    weightedData != null ? weightedData.size() : 0,
                    algorithmConfig != null ? algorithmConfig.getIndicatorCount() : 0);
            
            // 记录计算开始
            calculationLogger.logCalculationStart(
                    algorithmConfig != null ? algorithmConfig.getModelId() : null,
                    algorithmConfig != null ? algorithmConfig.getStepCode() : "unknown",
                    weightedData
            );
            
            // 1. 验证输入参数
            if (weightedData == null || weightedData.isEmpty()) {
                log.warn("定权数据为空，返回空结果");
                return new HashMap<>();
            }
            
            if (algorithmConfig == null || !algorithmConfig.isValid()) {
                log.error("TOPSIS算法配置无效");
                throw new IllegalArgumentException("TOPSIS算法配置无效");
            }
            
            // 2. 数据验证和预处理
            Map<String, Map<String, Double>> validatedData = validateAndPreprocessData(weightedData, algorithmConfig);
            
            if (validatedData.isEmpty()) {
                log.warn("验证后的数据为空");
                return new HashMap<>();
            }
            
            // 3. 处理单区域情况
            if (validatedData.size() == 1 && algorithmConfig.isEnableSingleRegionHandling()) {
                log.info("检测到单区域情况，使用特殊处理逻辑");
                String regionCode = validatedData.keySet().iterator().next();
                Map<String, Double> regionData = validatedData.get(regionCode);
                Map<String, Double> singleRegionResult = calculateSingleRegionTopsis(regionData, algorithmConfig);
                
                Map<String, Map<String, Double>> result = new HashMap<>();
                result.put(regionCode, singleRegionResult);
                
                long calculationTime = System.currentTimeMillis() - startTime;
                calculationLogger.logCalculationComplete(result, calculationTime);
                
                return result;
            }
            
            // 4. 计算理想解
            IdealSolution idealSolution = calculateIdealSolutions(validatedData, algorithmConfig);
            
            if (!idealSolution.isValid()) {
                log.error("理想解计算失败");
                throw new RuntimeException("理想解计算失败");
            }
            
            // 记录理想解计算
            calculationLogger.logIdealSolutionCalculation(
                    idealSolution.getPositiveIdeal(),
                    idealSolution.getNegativeIdeal()
            );
            
            log.info("理想解计算完成 - 正理想解: {}, 负理想解: {}", 
                    idealSolution.getPositiveIdeal(), idealSolution.getNegativeIdeal());
            
            // 5. 计算每个地区到理想解的距离
            Map<String, Map<String, Double>> results = new HashMap<>();
            
            for (Map.Entry<String, Map<String, Double>> entry : validatedData.entrySet()) {
                String regionCode = entry.getKey();
                Map<String, Double> regionData = entry.getValue();
                
                try {
                    // 计算到正理想解的距离
                    double positiveDistance = calculateEuclideanDistance(
                            regionData, 
                            idealSolution.getPositiveIdeal(),
                            algorithmConfig.getIndicators()
                    );
                    
                    // 计算到负理想解的距离
                    double negativeDistance = calculateEuclideanDistance(
                            regionData, 
                            idealSolution.getNegativeIdeal(),
                            algorithmConfig.getIndicators()
                    );
                    
                    // 构建结果
                    Map<String, Double> distances = new HashMap<>();
                    distances.put(algorithmConfig.getOutputParam() + "_positive", positiveDistance);
                    distances.put(algorithmConfig.getOutputParam() + "_negative", negativeDistance);
                    
                    // 计算综合能力值
                    if (positiveDistance + negativeDistance > 0) {
                        double comprehensiveScore = negativeDistance / (negativeDistance + positiveDistance);
                        distances.put(algorithmConfig.getOutputParam(), comprehensiveScore);
                    } else {
                        distances.put(algorithmConfig.getOutputParam(), 0.5); // 默认中等水平
                    }
                    
                    results.put(regionCode, distances);
                    
                    // 记录距离计算
                    calculationLogger.logDistanceCalculation(regionCode, regionData, positiveDistance, negativeDistance);
                    
                    log.debug("地区 {} TOPSIS距离 - 正理想解: {}, 负理想解: {}, 综合能力: {}", 
                            regionCode, positiveDistance, negativeDistance, distances.get(algorithmConfig.getOutputParam()));
                            
                } catch (Exception e) {
                    log.error("计算地区 {} 的TOPSIS距离时发生异常", regionCode, e);
                    Map<String, Object> context = new HashMap<>();
                    context.put("regionCode", regionCode);
                    context.put("regionData", regionData);
                    calculationLogger.logException("距离计算", e, context);
                }
            }
            
            // 6. 验证结果合理性
            Map<String, Object> validation = validateTopsisResults(results, algorithmConfig);
            if (!(Boolean) validation.get("valid")) {
                log.warn("TOPSIS结果验证失败: {}", validation.get("message"));
                calculationLogger.logException("结果验证", 
                        new RuntimeException("TOPSIS结果验证失败"), validation);
            }
            
            long calculationTime = System.currentTimeMillis() - startTime;
            
            // 记录计算完成
            calculationLogger.logCalculationComplete(results, calculationTime);
            
            log.info("统一TOPSIS距离计算完成，处理了 {} 个地区，耗时 {}ms", results.size(), calculationTime);
            return results;
            
        } catch (Exception e) {
            long calculationTime = System.currentTimeMillis() - startTime;
            log.error("统一TOPSIS计算过程中发生异常，耗时 {}ms", calculationTime, e);
            
            Map<String, Object> context = new HashMap<>();
            context.put("inputSize", weightedData != null ? weightedData.size() : 0);
            context.put("calculationTime", calculationTime);
            calculationLogger.logException("整体计算", e, context);
            
            // 记录失败的计算完成
            calculationLogger.logCalculationComplete(new HashMap<>(), calculationTime);
            
            throw e;
        }
    }
    
    @Override
    public IdealSolution calculateIdealSolutions(
            Map<String, Map<String, Double>> weightedData,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        log.debug("计算动态指标理想解 - 指标列表: {}", algorithmConfig.getIndicators());
        
        if (weightedData == null || weightedData.isEmpty()) {
            return new IdealSolution(new HashMap<>(), new HashMap<>());
        }
        
        if (algorithmConfig == null || algorithmConfig.getIndicators() == null || algorithmConfig.getIndicators().isEmpty()) {
            log.warn("算法配置或指标列表为空");
            return new IdealSolution(new HashMap<>(), new HashMap<>());
        }
        
        Map<String, Double> positiveIdeal = new HashMap<>();
        Map<String, Double> negativeIdeal = new HashMap<>();
        
        // 对配置中指定的每个指标计算理想解
        for (String indicator : algorithmConfig.getIndicators()) {
            List<Double> values = new ArrayList<>();
            
            // 收集该指标的所有有效值
            for (Map<String, Double> regionData : weightedData.values()) {
                Double value = regionData.get(indicator);
                if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                    values.add(value);
                }
            }
            
            if (!values.isEmpty()) {
                // 正理想解：最大值（因为所有指标都是效益型）
                double maxValue = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                // 负理想解：最小值
                double minValue = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                
                positiveIdeal.put(indicator, maxValue);
                negativeIdeal.put(indicator, minValue);
                
                log.debug("指标 {} 理想解 - 正: {}, 负: {} (基于 {} 个有效值)", 
                        indicator, maxValue, minValue, values.size());
            } else {
                log.warn("指标 {} 没有有效值", indicator);
                positiveIdeal.put(indicator, 0.0);
                negativeIdeal.put(indicator, 0.0);
            }
        }
        
        return new IdealSolution(positiveIdeal, negativeIdeal);
    }
    
    @Override
    public double calculateEuclideanDistance(
            Map<String, Double> regionData, 
            Map<String, Double> idealSolution,
            List<String> indicators) {
        
        double sumSquares = 0.0;
        int validIndicators = 0;
        
        // 只计算配置中指定的指标
        for (String indicator : indicators) {
            Double idealValue = idealSolution.get(indicator);
            Double regionValue = regionData.get(indicator);
            
            if (regionValue != null && idealValue != null && 
                !Double.isNaN(regionValue) && !Double.isInfinite(regionValue) &&
                !Double.isNaN(idealValue) && !Double.isInfinite(idealValue)) {
                
                double diff = regionValue - idealValue;
                sumSquares += diff * diff;
                validIndicators++;
                
                log.trace("指标 {} 距离计算 - 地区值: {}, 理想值: {}, 差值平方: {}", 
                        indicator, regionValue, idealValue, diff * diff);
            } else {
                log.trace("指标 {} 跳过计算 - 地区值: {}, 理想值: {}", indicator, regionValue, idealValue);
            }
        }
        
        if (validIndicators == 0) {
            log.warn("没有有效指标用于距离计算");
            return 0.0;
        }
        
        double distance = Math.sqrt(sumSquares);
        log.trace("欧几里得距离计算结果: {} (基于 {} 个指标)", distance, validIndicators);
        
        return distance;
    }
    
    @Override
    public Map<String, Double> calculateSingleRegionTopsis(
            Map<String, Double> regionData,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        log.debug("计算单区域TOPSIS - 区域数据: {}", regionData);
        
        if (regionData == null || regionData.isEmpty()) {
            return new HashMap<>();
        }
        
        if (algorithmConfig == null || !algorithmConfig.isValid()) {
            log.error("算法配置无效");
            return new HashMap<>();
        }
        
        try {
            // 为单区域情况构造理论基准值
            Map<String, Double> theoreticalPositive = new HashMap<>();
            Map<String, Double> theoreticalNegative = new HashMap<>();
            
            double baselineRatio = algorithmConfig.getTheoreticalBaselineRatio();
            
            for (String indicator : algorithmConfig.getIndicators()) {
                Double value = regionData.get(indicator);
                
                if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                    // 正理想解：当前值的120%（或配置的比例）
                    theoreticalPositive.put(indicator, value * (1 + baselineRatio));
                    // 负理想解：当前值的80%（或配置的比例）
                    theoreticalNegative.put(indicator, value * (1 - baselineRatio));
                } else {
                    theoreticalPositive.put(indicator, 1.0);
                    theoreticalNegative.put(indicator, 0.0);
                }
            }
            
            // 计算到理论理想解的距离
            double positiveDistance = calculateEuclideanDistance(
                    regionData, theoreticalPositive, algorithmConfig.getIndicators());
            double negativeDistance = calculateEuclideanDistance(
                    regionData, theoreticalNegative, algorithmConfig.getIndicators());
            
            // 构建结果
            Map<String, Double> result = new HashMap<>();
            result.put(algorithmConfig.getOutputParam() + "_positive", positiveDistance);
            result.put(algorithmConfig.getOutputParam() + "_negative", negativeDistance);
            
            // 计算综合能力值
            if (positiveDistance + negativeDistance > 0) {
                double comprehensiveScore = negativeDistance / (negativeDistance + positiveDistance);
                result.put(algorithmConfig.getOutputParam(), comprehensiveScore);
            } else {
                result.put(algorithmConfig.getOutputParam(), 0.5); // 默认中等水平
            }
            
            log.info("单区域TOPSIS计算完成 - 正理想解距离: {}, 负理想解距离: {}, 综合能力: {}", 
                    positiveDistance, negativeDistance, result.get(algorithmConfig.getOutputParam()));
            
            return result;
            
        } catch (Exception e) {
            log.error("单区域TOPSIS计算时发生异常", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public TOPSISDiagnosticReport diagnoseCalculation(
            Map<String, Map<String, Double>> weightedData,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        log.debug("诊断TOPSIS计算");
        
        List<String> issues = new ArrayList<>();
        Map<String, Object> inputDataSummary = new HashMap<>();
        Map<String, Object> calculationDetails = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            // 1. 验证输入数据
            if (weightedData == null || weightedData.isEmpty()) {
                issues.add("定权数据为空");
                recommendations.add("检查数据源和数据处理流程");
            } else {
                inputDataSummary.put("regionCount", weightedData.size());
                
                // 统计数据质量
                int totalValues = 0;
                int nanCount = 0;
                int infiniteCount = 0;
                int zeroCount = 0;
                Set<String> allIndicators = new HashSet<>();
                
                for (Map<String, Double> regionData : weightedData.values()) {
                    if (regionData != null) {
                        allIndicators.addAll(regionData.keySet());
                        for (Double value : regionData.values()) {
                            totalValues++;
                            if (value == null) {
                                nanCount++;
                            } else if (Double.isNaN(value)) {
                                nanCount++;
                            } else if (Double.isInfinite(value)) {
                                infiniteCount++;
                            } else if (value == 0.0) {
                                zeroCount++;
                            }
                        }
                    }
                }
                
                inputDataSummary.put("totalValues", totalValues);
                inputDataSummary.put("nanCount", nanCount);
                inputDataSummary.put("infiniteCount", infiniteCount);
                inputDataSummary.put("zeroCount", zeroCount);
                inputDataSummary.put("indicatorCount", allIndicators.size());
                
                if (nanCount > 0) {
                    issues.add("存在 " + nanCount + " 个NaN值");
                    recommendations.add("清理或填充NaN值");
                }
                
                if (infiniteCount > 0) {
                    issues.add("存在 " + infiniteCount + " 个无穷值");
                    recommendations.add("检查计算过程中的除零操作");
                }
            }
            
            // 2. 验证算法配置
            if (algorithmConfig == null) {
                issues.add("TOPSIS算法配置为空");
                recommendations.add("检查step_algorithm表中的配置");
            } else if (!algorithmConfig.isValid()) {
                issues.add("TOPSIS算法配置无效");
                recommendations.add("验证ql_expression格式和指标列表");
            } else {
                calculationDetails.put("algorithmType", algorithmConfig.getAlgorithmType());
                calculationDetails.put("indicatorCount", algorithmConfig.getIndicatorCount());
                calculationDetails.put("indicators", algorithmConfig.getIndicators());
                
                // 验证指标是否存在于数据中
                if (weightedData != null && !weightedData.isEmpty()) {
                    Set<String> dataIndicators = new HashSet<>();
                    for (Map<String, Double> regionData : weightedData.values()) {
                        if (regionData != null) {
                            dataIndicators.addAll(regionData.keySet());
                        }
                    }
                    
                    for (String indicator : algorithmConfig.getIndicators()) {
                        if (!dataIndicators.contains(indicator)) {
                            issues.add("配置的指标 " + indicator + " 在数据中不存在");
                            recommendations.add("检查指标名称是否正确或更新算法配置");
                        }
                    }
                }
            }
            
            // 3. 构建诊断报告
            TOPSISCalculationMetrics metrics = TOPSISCalculationMetrics.builder()
                    .totalRegions(weightedData != null ? weightedData.size() : 0)
                    .validIndicators(algorithmConfig != null ? algorithmConfig.getIndicatorCount() : 0)
                    .inputDataRows(weightedData != null ? weightedData.size() : 0)
                    .inputDataColumns(algorithmConfig != null ? algorithmConfig.getIndicatorCount() : 0)
                    .nanValueCount((Integer) inputDataSummary.getOrDefault("nanCount", 0))
                    .infiniteValueCount((Integer) inputDataSummary.getOrDefault("infiniteCount", 0))
                    .zeroValueCount((Integer) inputDataSummary.getOrDefault("zeroCount", 0))
                    .build();
            
            return TOPSISDiagnosticReport.builder()
                    .hasIssues(!issues.isEmpty())
                    .issues(issues)
                    .inputDataSummary(inputDataSummary)
                    .calculationDetails(calculationDetails)
                    .recommendations(recommendations)
                    .metrics(metrics)
                    .timestamp(System.currentTimeMillis())
                    .modelId(algorithmConfig != null ? algorithmConfig.getModelId() : null)
                    .stepCode(algorithmConfig != null ? algorithmConfig.getStepCode() : null)
                    .build();
            
        } catch (Exception e) {
            log.error("诊断TOPSIS计算时发生异常", e);
            issues.add("诊断过程中发生异常: " + e.getMessage());
            
            return TOPSISDiagnosticReport.builder()
                    .hasIssues(true)
                    .issues(issues)
                    .inputDataSummary(inputDataSummary)
                    .calculationDetails(calculationDetails)
                    .recommendations(recommendations)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
    
    @Override
    public Map<String, Object> validateTopsisResults(
            Map<String, Map<String, Double>> topsisResults,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        Map<String, Object> validation = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        if (topsisResults == null || topsisResults.isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "TOPSIS结果为空");
            return validation;
        }
        
        String outputParam = algorithmConfig != null ? algorithmConfig.getOutputParam() : "comprehensive";
        
        // 验证每个地区的结果
        for (Map.Entry<String, Map<String, Double>> entry : topsisResults.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> distances = entry.getValue();
            
            Double positiveDistance = distances.get(outputParam + "_positive");
            Double negativeDistance = distances.get(outputParam + "_negative");
            Double comprehensiveScore = distances.get(outputParam);
            
            // 检查距离值的有效性
            if (positiveDistance == null || negativeDistance == null) {
                issues.add("地区 " + regionCode + " 缺少距离值");
                continue;
            }
            
            if (Double.isNaN(positiveDistance) || Double.isInfinite(positiveDistance)) {
                issues.add("地区 " + regionCode + " 正理想解距离无效: " + positiveDistance);
            }
            
            if (Double.isNaN(negativeDistance) || Double.isInfinite(negativeDistance)) {
                issues.add("地区 " + regionCode + " 负理想解距离无效: " + negativeDistance);
            }
            
            if (positiveDistance < 0 || negativeDistance < 0) {
                issues.add("地区 " + regionCode + " 距离值为负数");
            }
            
            // 验证综合能力值
            if (comprehensiveScore != null) {
                if (Double.isNaN(comprehensiveScore) || Double.isInfinite(comprehensiveScore)) {
                    issues.add("地区 " + regionCode + " 综合能力值无效: " + comprehensiveScore);
                } else if (comprehensiveScore < 0 || comprehensiveScore > 1) {
                    issues.add("地区 " + regionCode + " 综合能力值超出[0,1]范围: " + comprehensiveScore);
                }
            }
            
            // 检查特殊情况
            if (positiveDistance == 0 && negativeDistance == 0) {
                issues.add("地区 " + regionCode + " 到正负理想解距离都为0（可能所有指标值相同）");
            }
        }
        
        boolean isValid = issues.isEmpty();
        validation.put("valid", isValid);
        validation.put("issues", issues);
        validation.put("message", isValid ? "TOPSIS结果验证通过" : "发现 " + issues.size() + " 个问题");
        
        if (!isValid) {
            log.warn("TOPSIS结果验证失败: {}", issues);
        } else {
            log.info("TOPSIS结果验证通过");
        }
        
        return validation;
    }
    
    /**
     * 验证和预处理输入数据
     */
    private Map<String, Map<String, Double>> validateAndPreprocessData(
            Map<String, Map<String, Double>> weightedData,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        Map<String, Map<String, Double>> validatedData = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : weightedData.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionData = entry.getValue();
            
            if (regionData == null || regionData.isEmpty()) {
                log.warn("地区 {} 数据为空，跳过", regionCode);
                continue;
            }
            
            // 只保留配置中指定的指标
            Map<String, Double> filteredData = new HashMap<>();
            boolean hasValidData = false;
            
            for (String indicator : algorithmConfig.getIndicators()) {
                Double value = regionData.get(indicator);
                
                if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                    filteredData.put(indicator, value);
                    hasValidData = true;
                } else {
                    log.debug("地区 {} 指标 {} 值无效: {}", regionCode, indicator, value);
                }
            }
            
            if (hasValidData) {
                validatedData.put(regionCode, filteredData);
            } else {
                log.warn("地区 {} 没有有效的指标数据，跳过", regionCode);
            }
        }
        
        log.info("数据验证完成 - 原始地区数: {}, 有效地区数: {}", 
                weightedData.size(), validatedData.size());
        
        return validatedData;
    }
}
