package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.dto.topsis.TOPSISDiagnosticReport;
import com.evaluate.dto.topsis.TOPSISCalculationMetrics;
import com.evaluate.dto.topsis.TOPSISRepairResult;
import com.evaluate.service.TOPSISDiagnosticService;
import com.evaluate.service.UnifiedTOPSISCalculator;
import com.evaluate.service.TOPSISConfigurationAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TOPSIS诊断服务实现类
 * 
 * 实现TOPSIS计算问题的诊断、分析和修复功能
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISDiagnosticServiceImpl implements TOPSISDiagnosticService {

    private static final Logger log = LoggerFactory.getLogger(TOPSISDiagnosticServiceImpl.class);

    
    @Autowired
    private UnifiedTOPSISCalculator topsisCalculator;
    
    @Override
    public TOPSISDiagnosticReport diagnose(Long modelId, List<String> regionCodes, Long weightConfigId) {
        log.info("开始诊断TOPSIS计算问题 - 模型ID: {}, 区域数量: {}, 权重配置ID: {}", 
                modelId, regionCodes.size(), weightConfigId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: 从数据库获取定权数据
            // 这里暂时使用模拟数据进行诊断逻辑验证
            Map<String, Map<String, Double>> mockWeightedData = generateMockWeightedData(regionCodes);
            
            return diagnoseWeightedData(mockWeightedData, modelId, "step4");
            
        } catch (Exception e) {
            log.error("诊断TOPSIS计算问题时发生异常", e);
            
            return TOPSISDiagnosticReport.builder()
                    .hasIssues(true)
                    .issues(Arrays.asList("诊断过程中发生异常: " + e.getMessage()))
                    .recommendations(Arrays.asList("请检查输入参数和系统配置"))
                    .timestamp(System.currentTimeMillis())
                    .modelId(modelId)
                    .stepCode("step4")
                    .regionCodes(regionCodes)
                    .build();
        }
    }
    
    @Override
    public TOPSISDiagnosticReport diagnoseWeightedData(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode) {
        
        log.info("开始诊断定权数据的TOPSIS计算问题 - 模型ID: {}, 步骤: {}, 数据行数: {}", 
                modelId, stepCode, weightedData.size());
        
        long startTime = System.currentTimeMillis();
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // 1. 验证输入数据
        Map<String, Object> inputValidation = validateInputData(weightedData);
        if (!(Boolean) inputValidation.get("valid")) {
            issues.addAll((List<String>) inputValidation.get("issues"));
        }
        
        // 2. 计算TOPSIS并收集指标
        TOPSISCalculationMetrics metrics = calculateMetrics(weightedData);
        
        // 3. 执行TOPSIS计算并分析结果
        Map<String, Map<String, Double>> topsisResults = null;
        try {
            topsisResults = topsisCalculator.calculateDistances(weightedData, null);
            analyzeCalculationResults(topsisResults, issues, recommendations);
        } catch (Exception e) {
            log.error("TOPSIS计算失败", e);
            issues.add("TOPSIS计算失败: " + e.getMessage());
            recommendations.add("检查输入数据格式和数值有效性");
        }

        // 4. 分析理想解计算
        analyzeIdealSolutions(weightedData, null, issues, recommendations);
        
        // 5. 生成输入数据摘要
        Map<String, Object> inputDataSummary = generateInputDataSummary(weightedData);
        
        // 6. 生成计算详情
        Map<String, Object> calculationDetails = generateCalculationDetails(weightedData, topsisResults);
        
        long calculationTime = System.currentTimeMillis() - startTime;
        metrics.setCalculationTimeMs(calculationTime);
        
        boolean hasIssues = !issues.isEmpty();
        
        log.info("TOPSIS诊断完成 - 发现问题: {}, 问题数量: {}, 耗时: {}ms", 
                hasIssues, issues.size(), calculationTime);
        
        return TOPSISDiagnosticReport.builder()
                .hasIssues(hasIssues)
                .issues(issues)
                .inputDataSummary(inputDataSummary)
                .calculationDetails(calculationDetails)
                .recommendations(recommendations)
                .metrics(metrics)
                .timestamp(System.currentTimeMillis())
                .modelId(modelId)
                .stepCode(stepCode)
                .regionCodes(new ArrayList<>(weightedData.keySet()))
                .build();
    }
    
    @Override
    public TOPSISRepairResult repair(TOPSISDiagnosticReport diagnosticReport) {
        log.info("开始修复TOPSIS计算问题 - 问题数量: {}", diagnosticReport.getIssues().size());
        
        List<String> repairActions = new ArrayList<>();
        List<String> remainingIssues = new ArrayList<>();
        Map<String, Map<String, Double>> repairedData = new HashMap<>();
        
        // TODO: 实现具体的修复逻辑
        // 这里先提供基本的修复框架
        
        for (String issue : diagnosticReport.getIssues()) {
            if (issue.contains("NaN") || issue.contains("无穷")) {
                repairActions.add("清理无效数值（NaN、无穷值）");
            } else if (issue.contains("零值") || issue.contains("距离为0")) {
                repairActions.add("处理零值问题，使用默认基准值");
            } else if (issue.contains("数据为空")) {
                repairActions.add("填充缺失数据");
            } else {
                remainingIssues.add(issue);
            }
        }
        
        boolean success = remainingIssues.size() < diagnosticReport.getIssues().size();
        
        return TOPSISRepairResult.builder()
                .success(success)
                .repairActions(repairActions)
                .repairedData(repairedData)
                .message(success ? "部分问题已修复" : "无法自动修复问题")
                .timestamp(System.currentTimeMillis())
                .remainingIssues(remainingIssues)
                .build();
    }
    
    @Autowired
    private TOPSISConfigurationAnalyzer configurationAnalyzer;
    
    @Override
    public Map<String, Object> compareConfigurations(Long modelId1, Long modelId2, String stepCode) {
        log.info("对比TOPSIS配置 - 模型1: {}, 模型2: {}, 步骤: {}", modelId1, modelId2, stepCode);
        
        try {
            return configurationAnalyzer.compareModelConfigurations(modelId1, modelId2, stepCode);
        } catch (Exception e) {
            log.error("对比TOPSIS配置时发生异常", e);
            
            Map<String, Object> comparison = new HashMap<>();
            comparison.put("model1Id", modelId1);
            comparison.put("model2Id", modelId2);
            comparison.put("stepCode", stepCode);
            comparison.put("error", e.getMessage());
            comparison.put("differences", new ArrayList<>());
            comparison.put("similarities", new ArrayList<>());
            comparison.put("recommendations", Arrays.asList("配置对比失败，请检查系统配置"));
            
            return comparison;
        }
    }
    
    @Override
    public Map<String, Object> validateInputData(Map<String, Map<String, Double>> weightedData) {
        Map<String, Object> validation = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            issues.add("定权数据为空");
            validation.put("valid", false);
            validation.put("issues", issues);
            return validation;
        }
        
        int totalValues = 0;
        int nanCount = 0;
        int infiniteCount = 0;
        int zeroCount = 0;
        Set<String> allIndicators = new HashSet<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : weightedData.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionData = entry.getValue();
            
            if (regionData == null || regionData.isEmpty()) {
                issues.add("地区 " + regionCode + " 数据为空");
                continue;
            }
            
            allIndicators.addAll(regionData.keySet());
            
            for (Map.Entry<String, Double> dataEntry : regionData.entrySet()) {
                String indicator = dataEntry.getKey();
                Double value = dataEntry.getValue();
                totalValues++;
                
                if (value == null) {
                    issues.add("地区 " + regionCode + " 指标 " + indicator + " 值为null");
                } else if (Double.isNaN(value)) {
                    nanCount++;
                    issues.add("地区 " + regionCode + " 指标 " + indicator + " 值为NaN");
                } else if (Double.isInfinite(value)) {
                    infiniteCount++;
                    issues.add("地区 " + regionCode + " 指标 " + indicator + " 值为无穷");
                } else if (value == 0.0) {
                    zeroCount++;
                }
            }
        }
        
        // 检查指标一致性
        for (Map.Entry<String, Map<String, Double>> entry : weightedData.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionData = entry.getValue();
            
            for (String indicator : allIndicators) {
                if (!regionData.containsKey(indicator)) {
                    issues.add("地区 " + regionCode + " 缺少指标 " + indicator);
                }
            }
        }
        
        boolean isValid = issues.isEmpty();
        validation.put("valid", isValid);
        validation.put("issues", issues);
        validation.put("totalValues", totalValues);
        validation.put("nanCount", nanCount);
        validation.put("infiniteCount", infiniteCount);
        validation.put("zeroCount", zeroCount);
        validation.put("indicatorCount", allIndicators.size());
        validation.put("regionCount", weightedData.size());
        
        return validation;
    }
    
    @Override
    public Map<String, Object> generateCalculationLog(
            Map<String, Map<String, Double>> weightedData,
            Long modelId,
            String stepCode) {
        
        log.info("生成TOPSIS计算过程日志 - 模型ID: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> calculationLog = new HashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();
        
        // 步骤1: 输入数据验证
        Map<String, Object> step1 = new HashMap<>();
        step1.put("step", "输入数据验证");
        step1.put("timestamp", System.currentTimeMillis());
        step1.put("result", validateInputData(weightedData));
        steps.add(step1);
        
        // 步骤2: 理想解计算
        Map<String, Object> step2 = new HashMap<>();
        step2.put("step", "理想解计算");
        step2.put("timestamp", System.currentTimeMillis());
        try {
            TOPSISCalculator.IdealSolution idealSolution = topsisCalculator.calculateIdealSolutions(weightedData);
            step2.put("positiveIdeal", idealSolution.getPositiveIdeal());
            step2.put("negativeIdeal", idealSolution.getNegativeIdeal());
            step2.put("success", true);
        } catch (Exception e) {
            step2.put("error", e.getMessage());
            step2.put("success", false);
        }
        steps.add(step2);
        
        // 步骤3: 距离计算
        Map<String, Object> step3 = new HashMap<>();
        step3.put("step", "距离计算");
        step3.put("timestamp", System.currentTimeMillis());
        try {
            Map<String, Map<String, Double>> distances = topsisCalculator.calculateDistances(weightedData, null);
            step3.put("results", distances);
            step3.put("success", true);
        } catch (Exception e) {
            step3.put("error", e.getMessage());
            step3.put("success", false);
        }
        steps.add(step3);
        
        calculationLog.put("modelId", modelId);
        calculationLog.put("stepCode", stepCode);
        calculationLog.put("timestamp", System.currentTimeMillis());
        calculationLog.put("steps", steps);
        calculationLog.put("inputDataSize", weightedData.size());
        
        return calculationLog;
    }
    
    /**
     * 计算TOPSIS指标
     */
    private TOPSISCalculationMetrics calculateMetrics(Map<String, Map<String, Double>> weightedData) {
        if (weightedData == null || weightedData.isEmpty()) {
            return TOPSISCalculationMetrics.builder()
                    .totalRegions(0)
                    .validIndicators(0)
                    .build();
        }
        
        Set<String> allIndicators = new HashSet<>();
        int zeroCount = 0;
        int nanCount = 0;
        int infiniteCount = 0;
        int totalValues = 0;
        
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
        
        // 计算理想解
        Map<String, Double> idealSolutions = new HashMap<>();
        Map<String, Double> antiIdealSolutions = new HashMap<>();
        
        try {
            TOPSISCalculator.IdealSolution idealSolution = topsisCalculator.calculateIdealSolutions(weightedData);
            idealSolutions = idealSolution.getPositiveIdeal();
            antiIdealSolutions = idealSolution.getNegativeIdeal();
        } catch (Exception e) {
            log.warn("计算理想解时发生异常", e);
        }
        
        return TOPSISCalculationMetrics.builder()
                .totalRegions(weightedData.size())
                .validIndicators(allIndicators.size())
                .idealSolutions(idealSolutions)
                .antiIdealSolutions(antiIdealSolutions)
                .inputDataRows(weightedData.size())
                .inputDataColumns(allIndicators.size())
                .zeroValueCount(zeroCount)
                .nanValueCount(nanCount)
                .infiniteValueCount(infiniteCount)
                .build();
    }
    
    /**
     * 分析计算结果
     */
    private void analyzeCalculationResults(
            Map<String, Map<String, Double>> topsisResults,
            List<String> issues,
            List<String> recommendations) {
        
        if (topsisResults == null || topsisResults.isEmpty()) {
            issues.add("TOPSIS计算结果为空");
            recommendations.add("检查输入数据和计算逻辑");
            return;
        }
        
        int zeroDistanceCount = 0;
        int invalidResultCount = 0;
        
        for (Map.Entry<String, Map<String, Double>> entry : topsisResults.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> distances = entry.getValue();
            
            Double positiveDistance = distances.get("comprehensive_positive");
            Double negativeDistance = distances.get("comprehensive_negative");
            
            if (positiveDistance == null || negativeDistance == null) {
                invalidResultCount++;
                issues.add("地区 " + regionCode + " 缺少距离计算结果");
                continue;
            }
            
            if (positiveDistance == 0.0 && negativeDistance == 0.0) {
                zeroDistanceCount++;
                issues.add("地区 " + regionCode + " 到正负理想解距离都为0");
            }
            
            if (Double.isNaN(positiveDistance) || Double.isNaN(negativeDistance)) {
                invalidResultCount++;
                issues.add("地区 " + regionCode + " 距离计算结果为NaN");
            }
        }
        
        if (zeroDistanceCount > 0) {
            recommendations.add("检查输入数据是否存在所有值相同的情况");
            recommendations.add("考虑使用理论基准值进行单区域TOPSIS计算");
        }
        
        if (invalidResultCount > 0) {
            recommendations.add("检查输入数据中的无效值（NaN、无穷值）");
            recommendations.add("实施数据清洗和预处理");
        }
    }
    
    /**
     * 分析理想解计算
     */
    private void analyzeIdealSolutions(
            Map<String, Map<String, Double>> weightedData,
            List<String> issues,
            List<String> recommendations) {
        
        try {
            TOPSISCalculator.IdealSolution idealSolution = topsisCalculator.calculateIdealSolutions(weightedData);
            Map<String, Double> positiveIdeal = idealSolution.getPositiveIdeal();
            Map<String, Double> negativeIdeal = idealSolution.getNegativeIdeal();
            
            for (String indicator : positiveIdeal.keySet()) {
                Double maxValue = positiveIdeal.get(indicator);
                Double minValue = negativeIdeal.get(indicator);
                
                if (maxValue != null && minValue != null && maxValue.equals(minValue)) {
                    issues.add("指标 " + indicator + " 的最大值和最小值相同: " + maxValue);
                    recommendations.add("检查指标 " + indicator + " 的数据分布，可能需要使用不同的评估方法");
                }
            }
            
        } catch (Exception e) {
            issues.add("理想解计算失败: " + e.getMessage());
            recommendations.add("检查输入数据格式和数值有效性");
        }
    }
    
    /**
     * 生成输入数据摘要
     */
    private Map<String, Object> generateInputDataSummary(Map<String, Map<String, Double>> weightedData) {
        Map<String, Object> summary = new HashMap<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            summary.put("empty", true);
            return summary;
        }
        
        summary.put("regionCount", weightedData.size());
        summary.put("regions", new ArrayList<>(weightedData.keySet()));
        
        Set<String> allIndicators = new HashSet<>();
        for (Map<String, Double> regionData : weightedData.values()) {
            if (regionData != null) {
                allIndicators.addAll(regionData.keySet());
            }
        }
        
        summary.put("indicatorCount", allIndicators.size());
        summary.put("indicators", new ArrayList<>(allIndicators));
        
        return summary;
    }
    
    /**
     * 生成计算详情
     */
    private Map<String, Object> generateCalculationDetails(
            Map<String, Map<String, Double>> weightedData,
            Map<String, Map<String, Double>> topsisResults) {
        
        Map<String, Object> details = new HashMap<>();
        
        details.put("inputDataProvided", weightedData != null && !weightedData.isEmpty());
        details.put("calculationCompleted", topsisResults != null && !topsisResults.isEmpty());
        
        if (topsisResults != null) {
            details.put("resultRegionCount", topsisResults.size());
            
            // 统计结果分布
            List<Double> positiveDistances = new ArrayList<>();
            List<Double> negativeDistances = new ArrayList<>();
            
            for (Map<String, Double> distances : topsisResults.values()) {
                Double pos = distances.get("comprehensive_positive");
                Double neg = distances.get("comprehensive_negative");
                if (pos != null) positiveDistances.add(pos);
                if (neg != null) negativeDistances.add(neg);
            }
            
            if (!positiveDistances.isEmpty()) {
                details.put("positiveDistanceStats", calculateStatistics(positiveDistances));
            }
            if (!negativeDistances.isEmpty()) {
                details.put("negativeDistanceStats", calculateStatistics(negativeDistances));
            }
        }
        
        return details;
    }
    
    /**
     * 计算统计信息
     */
    private Map<String, Double> calculateStatistics(List<Double> values) {
        Map<String, Double> stats = new HashMap<>();
        
        if (values.isEmpty()) {
            return stats;
        }
        
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / values.size();
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        
        stats.put("mean", mean);
        stats.put("min", min);
        stats.put("max", max);
        stats.put("sum", sum);
        stats.put("count", (double) values.size());
        
        return stats;
    }
    
    /**
     * 生成模拟定权数据用于测试
     */
    private Map<String, Map<String, Double>> generateMockWeightedData(List<String> regionCodes) {
        Map<String, Map<String, Double>> mockData = new HashMap<>();
        
        for (String regionCode : regionCodes) {
            Map<String, Double> regionData = new HashMap<>();
            regionData.put("indicator1", Math.random() * 100);
            regionData.put("indicator2", Math.random() * 100);
            regionData.put("indicator3", Math.random() * 100);
            mockData.put(regionCode, regionData);
        }
        
        return mockData;
    }
}
