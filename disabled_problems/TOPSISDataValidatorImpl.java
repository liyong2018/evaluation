package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.service.TOPSISDataValidator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TOPSIS数据验证器实现类
 * 
 * 实现TOPSIS计算输入数据的验证和异常检测功能
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISDataValidatorImpl implements TOPSISDataValidator {

    private static final Logger log = LoggerFactory.getLogger(TOPSISDataValidatorImpl.class);

    
    @Override
    public Map<String, Object> validateWeightedData(Map<String, Map<String, Double>> weightedData) {
        log.debug("开始验证定权数据");
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 基本验证
        if (weightedData == null) {
            errors.add("定权数据为null");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }
        
        if (weightedData.isEmpty()) {
            errors.add("定权数据为空");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }
        
        // 验证数据完整性
        Map<String, Object> completenessResult = validateDataCompleteness(weightedData);
        if (!(Boolean) completenessResult.get("valid")) {
            errors.addAll((List<String>) completenessResult.get("errors"));
        }
        warnings.addAll((List<String>) completenessResult.getOrDefault("warnings", new ArrayList<>()));
        
        // 验证数据一致性
        Map<String, Object> consistencyResult = validateDataConsistency(weightedData);
        if (!(Boolean) consistencyResult.get("valid")) {
            errors.addAll((List<String>) consistencyResult.get("errors"));
        }
        warnings.addAll((List<String>) consistencyResult.getOrDefault("warnings", new ArrayList<>()));
        
        // 检测数据异常
        Map<String, Object> anomalyResult = detectAnomalies(weightedData);
        warnings.addAll((List<String>) anomalyResult.getOrDefault("anomalies", new ArrayList<>()));
        
        boolean isValid = errors.isEmpty();
        
        result.put("valid", isValid);
        result.put("errors", errors);
        result.put("warnings", warnings);
        result.put("completenessResult", completenessResult);
        result.put("consistencyResult", consistencyResult);
        result.put("anomalyResult", anomalyResult);
        
        log.debug("定权数据验证完成: valid={}, errors={}, warnings={}", 
                isValid, errors.size(), warnings.size());
        
        return result;
    }
    
    @Override
    public Map<String, Object> detectAnomalies(Map<String, Map<String, Double>> weightedData) {
        log.debug("开始检测数据异常");
        
        Map<String, Object> result = new HashMap<>();
        List<String> anomalies = new ArrayList<>();
        Map<String, Object> statistics = new HashMap<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            result.put("anomalies", anomalies);
            return result;
        }
        
        // 收集所有数值进行统计分析
        Map<String, List<Double>> indicatorValues = new HashMap<>();
        int totalValues = 0;
        int nanCount = 0;
        int infiniteCount = 0;
        int zeroCount = 0;
        int negativeCount = 0;
        
        for (Map.Entry<String, Map<String, Double>> regionEntry : weightedData.entrySet()) {
            String regionCode = regionEntry.getKey();
            Map<String, Double> regionData = regionEntry.getValue();
            
            if (regionData == null) {
                anomalies.add("地区 " + regionCode + " 数据为null");
                continue;
            }
            
            for (Map.Entry<String, Double> dataEntry : regionData.entrySet()) {
                String indicator = dataEntry.getKey();
                Double value = dataEntry.getValue();
                
                totalValues++;
                
                if (value == null) {
                    anomalies.add("地区 " + regionCode + " 指标 " + indicator + " 值为null");
                    continue;
                }
                
                if (Double.isNaN(value)) {
                    nanCount++;
                    anomalies.add("地区 " + regionCode + " 指标 " + indicator + " 值为NaN");
                    continue;
                }
                
                if (Double.isInfinite(value)) {
                    infiniteCount++;
                    anomalies.add("地区 " + regionCode + " 指标 " + indicator + " 值为无穷: " + value);
                    continue;
                }
                
                if (value == 0.0) {
                    zeroCount++;
                }
                
                if (value < 0.0) {
                    negativeCount++;
                    anomalies.add("地区 " + regionCode + " 指标 " + indicator + " 值为负数: " + value);
                }
                
                // 收集指标值用于统计分析
                indicatorValues.computeIfAbsent(indicator, k -> new ArrayList<>()).add(value);
            }
        }
        
        // 检测极端值
        detectOutliers(indicatorValues, anomalies);
        
        // 检测数据分布异常
        detectDistributionAnomalies(indicatorValues, anomalies);
        
        statistics.put("totalValues", totalValues);
        statistics.put("nanCount", nanCount);
        statistics.put("infiniteCount", infiniteCount);
        statistics.put("zeroCount", zeroCount);
        statistics.put("negativeCount", negativeCount);
        statistics.put("anomalyCount", anomalies.size());
        
        result.put("anomalies", anomalies);
        result.put("statistics", statistics);
        
        log.debug("数据异常检测完成: 发现 {} 个异常", anomalies.size());
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateDataCompleteness(Map<String, Map<String, Double>> weightedData) {
        log.debug("开始验证数据完整性");
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            errors.add("数据为空，无法验证完整性");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }
        
        // 收集所有指标
        Set<String> allIndicators = new HashSet<>();
        for (Map<String, Double> regionData : weightedData.values()) {
            if (regionData != null) {
                allIndicators.addAll(regionData.keySet());
            }
        }
        
        if (allIndicators.isEmpty()) {
            errors.add("没有找到任何指标");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }
        
        // 检查每个地区的数据完整性
        int totalRegions = weightedData.size();
        int completeRegions = 0;
        Map<String, Integer> indicatorMissingCount = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : weightedData.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionData = entry.getValue();
            
            if (regionData == null || regionData.isEmpty()) {
                errors.add("地区 " + regionCode + " 数据完全缺失");
                continue;
            }
            
            boolean isComplete = true;
            for (String indicator : allIndicators) {
                if (!regionData.containsKey(indicator) || regionData.get(indicator) == null) {
                    isComplete = false;
                    indicatorMissingCount.merge(indicator, 1, Integer::sum);
                    warnings.add("地区 " + regionCode + " 缺少指标 " + indicator);
                }
            }
            
            if (isComplete) {
                completeRegions++;
            }
        }
        
        // 计算完整性统计
        double completenessRate = (double) completeRegions / totalRegions;
        
        // 检查指标缺失情况
        for (Map.Entry<String, Integer> entry : indicatorMissingCount.entrySet()) {
            String indicator = entry.getKey();
            int missingCount = entry.getValue();
            double missingRate = (double) missingCount / totalRegions;
            
            if (missingRate > 0.5) {
                errors.add("指标 " + indicator + " 缺失率过高: " + String.format("%.1f%%", missingRate * 100));
            } else if (missingRate > 0.1) {
                warnings.add("指标 " + indicator + " 存在缺失: " + String.format("%.1f%%", missingRate * 100));
            }
        }
        
        boolean isValid = errors.isEmpty() && completenessRate >= 0.8;
        
        result.put("valid", isValid);
        result.put("errors", errors);
        result.put("warnings", warnings);
        result.put("totalRegions", totalRegions);
        result.put("completeRegions", completeRegions);
        result.put("completenessRate", completenessRate);
        result.put("totalIndicators", allIndicators.size());
        result.put("indicatorMissingCount", indicatorMissingCount);
        
        log.debug("数据完整性验证完成: valid={}, completenessRate={:.2%}", isValid, completenessRate);
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateDataConsistency(Map<String, Map<String, Double>> weightedData) {
        log.debug("开始验证数据一致性");
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            errors.add("数据为空，无法验证一致性");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }
        
        // 检查指标名称一致性
        Set<String> firstRegionIndicators = null;
        String firstRegionCode = null;
        
        for (Map.Entry<String, Map<String, Double>> entry : weightedData.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionData = entry.getValue();
            
            if (regionData == null || regionData.isEmpty()) {
                continue;
            }
            
            if (firstRegionIndicators == null) {
                firstRegionIndicators = new HashSet<>(regionData.keySet());
                firstRegionCode = regionCode;
            } else {
                Set<String> currentIndicators = regionData.keySet();
                
                // 检查指标数量是否一致
                if (currentIndicators.size() != firstRegionIndicators.size()) {
                    warnings.add("地区 " + regionCode + " 指标数量与地区 " + firstRegionCode + " 不一致");
                }
                
                // 检查指标名称是否一致
                Set<String> missingIndicators = new HashSet<>(firstRegionIndicators);
                missingIndicators.removeAll(currentIndicators);
                
                Set<String> extraIndicators = new HashSet<>(currentIndicators);
                extraIndicators.removeAll(firstRegionIndicators);
                
                if (!missingIndicators.isEmpty()) {
                    warnings.add("地区 " + regionCode + " 缺少指标: " + missingIndicators);
                }
                
                if (!extraIndicators.isEmpty()) {
                    warnings.add("地区 " + regionCode + " 多余指标: " + extraIndicators);
                }
            }
        }
        
        // 检查数据类型一致性
        validateDataTypes(weightedData, warnings);
        
        // 检查数据范围一致性
        validateDataRanges(weightedData, warnings);
        
        boolean isValid = errors.isEmpty();
        
        result.put("valid", isValid);
        result.put("errors", errors);
        result.put("warnings", warnings);
        
        log.debug("数据一致性验证完成: valid={}, warnings={}", isValid, warnings.size());
        
        return result;
    }
    
    @Override
    public Map<String, Object> generateDataQualityReport(Map<String, Map<String, Double>> weightedData) {
        log.info("生成数据质量报告");
        
        Map<String, Object> report = new HashMap<>();
        
        // 基本统计
        Map<String, Object> basicStats = generateBasicStatistics(weightedData);
        report.put("basicStatistics", basicStats);
        
        // 完整性分析
        Map<String, Object> completenessAnalysis = validateDataCompleteness(weightedData);
        report.put("completenessAnalysis", completenessAnalysis);
        
        // 一致性分析
        Map<String, Object> consistencyAnalysis = validateDataConsistency(weightedData);
        report.put("consistencyAnalysis", consistencyAnalysis);
        
        // 异常检测
        Map<String, Object> anomalyDetection = detectAnomalies(weightedData);
        report.put("anomalyDetection", anomalyDetection);
        
        // 指标分析
        Map<String, Object> indicatorAnalysis = analyzeIndicators(weightedData);
        report.put("indicatorAnalysis", indicatorAnalysis);
        
        // 生成质量评分
        double qualityScore = calculateQualityScore(completenessAnalysis, consistencyAnalysis, anomalyDetection);
        report.put("qualityScore", qualityScore);
        
        // 生成建议
        List<String> recommendations = generateRecommendations(completenessAnalysis, consistencyAnalysis, anomalyDetection);
        report.put("recommendations", recommendations);
        
        report.put("timestamp", System.currentTimeMillis());
        
        log.info("数据质量报告生成完成，质量评分: {:.2f}", qualityScore);
        
        return report;
    }
    
    /**
     * 检测极端值
     */
    private void detectOutliers(Map<String, List<Double>> indicatorValues, List<String> anomalies) {
        for (Map.Entry<String, List<Double>> entry : indicatorValues.entrySet()) {
            String indicator = entry.getKey();
            List<Double> values = entry.getValue();
            
            if (values.size() < 3) {
                continue; // 数据太少，无法检测极端值
            }
            
            // 计算四分位数
            List<Double> sortedValues = values.stream().sorted().collect(Collectors.toList());
            int n = sortedValues.size();
            
            double q1 = sortedValues.get(n / 4);
            double q3 = sortedValues.get(3 * n / 4);
            double iqr = q3 - q1;
            
            double lowerBound = q1 - 1.5 * iqr;
            double upperBound = q3 + 1.5 * iqr;
            
            // 检测极端值
            for (Double value : values) {
                if (value < lowerBound || value > upperBound) {
                    anomalies.add("指标 " + indicator + " 存在极端值: " + value + 
                            " (正常范围: " + String.format("%.2f", lowerBound) + 
                            " - " + String.format("%.2f", upperBound) + ")");
                }
            }
        }
    }
    
    /**
     * 检测分布异常
     */
    private void detectDistributionAnomalies(Map<String, List<Double>> indicatorValues, List<String> anomalies) {
        for (Map.Entry<String, List<Double>> entry : indicatorValues.entrySet()) {
            String indicator = entry.getKey();
            List<Double> values = entry.getValue();
            
            if (values.size() < 2) {
                continue;
            }
            
            // 检查是否所有值都相同
            long distinctCount = values.stream().distinct().count();
            if (distinctCount == 1) {
                anomalies.add("指标 " + indicator + " 所有值都相同: " + values.get(0));
                continue;
            }
            
            // 检查方差是否过小
            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = values.stream()
                    .mapToDouble(v -> Math.pow(v - mean, 2))
                    .average()
                    .orElse(0.0);
            
            if (variance < 1e-10) {
                anomalies.add("指标 " + indicator + " 方差过小，可能缺乏区分度: " + variance);
            }
            
            // 检查偏度（简单检查）
            double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            
            if (max - min < 1e-6) {
                anomalies.add("指标 " + indicator + " 取值范围过小: " + (max - min));
            }
        }
    }
    
    /**
     * 验证数据类型
     */
    private void validateDataTypes(Map<String, Map<String, Double>> weightedData, List<String> warnings) {
        // 检查是否有非数值类型的数据（在Double类型下主要检查特殊值）
        for (Map.Entry<String, Map<String, Double>> regionEntry : weightedData.entrySet()) {
            String regionCode = regionEntry.getKey();
            Map<String, Double> regionData = regionEntry.getValue();
            
            if (regionData == null) continue;
            
            for (Map.Entry<String, Double> dataEntry : regionData.entrySet()) {
                String indicator = dataEntry.getKey();
                Double value = dataEntry.getValue();
                
                if (value != null && (Double.isNaN(value) || Double.isInfinite(value))) {
                    warnings.add("地区 " + regionCode + " 指标 " + indicator + " 包含特殊数值: " + value);
                }
            }
        }
    }
    
    /**
     * 验证数据范围
     */
    private void validateDataRanges(Map<String, Map<String, Double>> weightedData, List<String> warnings) {
        Map<String, Double> indicatorMins = new HashMap<>();
        Map<String, Double> indicatorMaxs = new HashMap<>();
        
        // 收集每个指标的最小值和最大值
        for (Map<String, Double> regionData : weightedData.values()) {
            if (regionData == null) continue;
            
            for (Map.Entry<String, Double> entry : regionData.entrySet()) {
                String indicator = entry.getKey();
                Double value = entry.getValue();
                
                if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                    indicatorMins.merge(indicator, value, Double::min);
                    indicatorMaxs.merge(indicator, value, Double::max);
                }
            }
        }
        
        // 检查范围异常
        for (String indicator : indicatorMins.keySet()) {
            Double min = indicatorMins.get(indicator);
            Double max = indicatorMaxs.get(indicator);
            
            if (min != null && max != null) {
                if (min.equals(max)) {
                    warnings.add("指标 " + indicator + " 所有值都相同: " + min);
                } else if (max - min < 1e-10) {
                    warnings.add("指标 " + indicator + " 取值范围过小: [" + min + ", " + max + "]");
                } else if (max / min > 1000 && min > 0) {
                    warnings.add("指标 " + indicator + " 取值范围过大: [" + min + ", " + max + "]");
                }
            }
        }
    }
    
    /**
     * 生成基本统计信息
     */
    private Map<String, Object> generateBasicStatistics(Map<String, Map<String, Double>> weightedData) {
        Map<String, Object> stats = new HashMap<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            stats.put("empty", true);
            return stats;
        }
        
        int totalRegions = weightedData.size();
        Set<String> allIndicators = new HashSet<>();
        int totalValues = 0;
        int validValues = 0;
        
        for (Map<String, Double> regionData : weightedData.values()) {
            if (regionData != null) {
                allIndicators.addAll(regionData.keySet());
                for (Double value : regionData.values()) {
                    totalValues++;
                    if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                        validValues++;
                    }
                }
            }
        }
        
        stats.put("totalRegions", totalRegions);
        stats.put("totalIndicators", allIndicators.size());
        stats.put("totalValues", totalValues);
        stats.put("validValues", validValues);
        stats.put("validValueRate", totalValues > 0 ? (double) validValues / totalValues : 0.0);
        stats.put("indicators", new ArrayList<>(allIndicators));
        
        return stats;
    }
    
    /**
     * 分析指标
     */
    private Map<String, Object> analyzeIndicators(Map<String, Map<String, Double>> weightedData) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (weightedData == null || weightedData.isEmpty()) {
            return analysis;
        }
        
        Map<String, List<Double>> indicatorValues = new HashMap<>();
        
        // 收集指标值
        for (Map<String, Double> regionData : weightedData.values()) {
            if (regionData != null) {
                for (Map.Entry<String, Double> entry : regionData.entrySet()) {
                    String indicator = entry.getKey();
                    Double value = entry.getValue();
                    
                    if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                        indicatorValues.computeIfAbsent(indicator, k -> new ArrayList<>()).add(value);
                    }
                }
            }
        }
        
        // 分析每个指标
        Map<String, Map<String, Object>> indicatorStats = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : indicatorValues.entrySet()) {
            String indicator = entry.getKey();
            List<Double> values = entry.getValue();
            
            Map<String, Object> stats = new HashMap<>();
            if (!values.isEmpty()) {
                double sum = values.stream().mapToDouble(Double::doubleValue).sum();
                double mean = sum / values.size();
                double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                
                double variance = values.stream()
                        .mapToDouble(v -> Math.pow(v - mean, 2))
                        .average()
                        .orElse(0.0);
                double stdDev = Math.sqrt(variance);
                
                stats.put("count", values.size());
                stats.put("mean", mean);
                stats.put("min", min);
                stats.put("max", max);
                stats.put("range", max - min);
                stats.put("stdDev", stdDev);
                stats.put("variance", variance);
                stats.put("coefficientOfVariation", mean != 0 ? stdDev / Math.abs(mean) : 0.0);
            }
            
            indicatorStats.put(indicator, stats);
        }
        
        analysis.put("indicatorStatistics", indicatorStats);
        analysis.put("totalIndicators", indicatorValues.size());
        
        return analysis;
    }
    
    /**
     * 计算质量评分
     */
    private double calculateQualityScore(Map<String, Object> completenessAnalysis, 
                                       Map<String, Object> consistencyAnalysis,
                                       Map<String, Object> anomalyDetection) {
        double score = 100.0;
        
        // 完整性评分 (40%)
        Double completenessRate = (Double) completenessAnalysis.get("completenessRate");
        if (completenessRate != null) {
            score *= 0.6 + 0.4 * completenessRate;
        }
        
        // 一致性评分 (30%)
        @SuppressWarnings("unchecked")
        List<String> consistencyErrors = (List<String>) consistencyAnalysis.get("errors");
        if (consistencyErrors != null && !consistencyErrors.isEmpty()) {
            score *= 0.7; // 有一致性错误扣30分
        }
        
        // 异常检测评分 (30%)
        @SuppressWarnings("unchecked")
        List<String> anomalies = (List<String>) anomalyDetection.get("anomalies");
        if (anomalies != null) {
            int anomalyCount = anomalies.size();
            if (anomalyCount > 0) {
                double anomalyPenalty = Math.min(0.3, anomalyCount * 0.05); // 每个异常扣5分，最多扣30分
                score *= (1.0 - anomalyPenalty);
            }
        }
        
        return Math.max(0.0, Math.min(100.0, score));
    }
    
    /**
     * 生成建议
     */
    private List<String> generateRecommendations(Map<String, Object> completenessAnalysis,
                                                Map<String, Object> consistencyAnalysis,
                                                Map<String, Object> anomalyDetection) {
        List<String> recommendations = new ArrayList<>();
        
        // 完整性建议
        Double completenessRate = (Double) completenessAnalysis.get("completenessRate");
        if (completenessRate != null && completenessRate < 0.9) {
            recommendations.add("数据完整性较低(" + String.format("%.1f%%", completenessRate * 100) + 
                    ")，建议补充缺失数据或使用插值方法");
        }
        
        // 一致性建议
        @SuppressWarnings("unchecked")
        List<String> consistencyErrors = (List<String>) consistencyAnalysis.get("errors");
        if (consistencyErrors != null && !consistencyErrors.isEmpty()) {
            recommendations.add("存在数据一致性问题，建议统一数据格式和指标定义");
        }
        
        // 异常处理建议
        @SuppressWarnings("unchecked")
        List<String> anomalies = (List<String>) anomalyDetection.get("anomalies");
        if (anomalies != null && !anomalies.isEmpty()) {
            if (anomalies.stream().anyMatch(a -> a.contains("NaN") || a.contains("无穷"))) {
                recommendations.add("存在无效数值(NaN/无穷)，建议进行数据清洗");
            }
            if (anomalies.stream().anyMatch(a -> a.contains("极端值"))) {
                recommendations.add("存在极端值，建议检查数据来源或使用异常值处理方法");
            }
            if (anomalies.stream().anyMatch(a -> a.contains("所有值都相同"))) {
                recommendations.add("部分指标缺乏区分度，建议重新评估指标选择");
            }
        }
        
        return recommendations;
    }
}
