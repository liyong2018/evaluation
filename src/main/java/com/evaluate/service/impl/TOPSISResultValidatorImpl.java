package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.service.TOPSISResultValidator;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * TOPSIS结果验证器实现类
 * 
 * 验证计算结果的合理性并提供自动修复机制：
 * 1. 验证距离值非负数且非NaN
 * 2. 验证综合能力值在[0,1]范围内
 * 3. 实现异常情况的自动修复
 * 4. 提供详细的验证报告和修复建议
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISResultValidatorImpl implements TOPSISResultValidator {

    private static final Logger log = LoggerFactory.getLogger(TOPSISResultValidatorImpl.class);

    
    private static final double DEFAULT_COMPREHENSIVE_SCORE = 0.5; // 默认综合能力值
    private static final double MIN_DISTANCE_THRESHOLD = 1e-10; // 最小距离阈值
    private static final double MAX_DISTANCE_RATIO = 1000.0; // 最大距离比例
    
    @Override
    public ValidationResult validateResults(
            Map<String, Map<String, Double>> topsisResults,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        log.debug("验证TOPSIS结果 - 地区数量: {}", topsisResults != null ? topsisResults.size() : 0);
        
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> statistics = new HashMap<>();
        List<String> repairSuggestions = new ArrayList<>();
        
        if (topsisResults == null || topsisResults.isEmpty()) {
            issues.add("TOPSIS结果为空");
            return new ValidationResult(false, issues, warnings, statistics, repairSuggestions);
        }
        
        if (algorithmConfig == null || !algorithmConfig.isValid()) {
            issues.add("算法配置无效");
            return new ValidationResult(false, issues, warnings, statistics, repairSuggestions);
        }
        
        String outputParam = algorithmConfig.getOutputParam();
        
        // 统计信息
        int totalRegions = topsisResults.size();
        int validRegions = 0;
        int invalidDistances = 0;
        int invalidScores = 0;
        int zeroDistances = 0;
        
        List<Double> positiveDistances = new ArrayList<>();
        List<Double> negativeDistances = new ArrayList<>();
        List<Double> comprehensiveScores = new ArrayList<>();
        
        // 验证每个地区的结果
        for (Map.Entry<String, Map<String, Double>> entry : topsisResults.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionResult = entry.getValue();
            
            RegionValidationResult regionValidation = validateRegionResult(regionCode, regionResult, algorithmConfig);
            
            if (!regionValidation.isValid()) {
                issues.addAll(regionValidation.getIssues());
                
                // 统计无效结果
                Double positiveDistance = regionResult.get(outputParam + "_positive");
                Double negativeDistance = regionResult.get(outputParam + "_negative");
                Double comprehensiveScore = regionResult.get(outputParam);
                
                if (positiveDistance != null && (Double.isNaN(positiveDistance) || Double.isInfinite(positiveDistance) || positiveDistance < 0)) {
                    invalidDistances++;
                }
                
                if (negativeDistance != null && (Double.isNaN(negativeDistance) || Double.isInfinite(negativeDistance) || negativeDistance < 0)) {
                    invalidDistances++;
                }
                
                if (comprehensiveScore != null && (Double.isNaN(comprehensiveScore) || Double.isInfinite(comprehensiveScore) || 
                    comprehensiveScore < 0 || comprehensiveScore > 1)) {
                    invalidScores++;
                }
                
                if ((positiveDistance != null && positiveDistance == 0) && (negativeDistance != null && negativeDistance == 0)) {
                    zeroDistances++;
                }
            } else {
                validRegions++;
                
                // 收集有效值用于统计
                Double positiveDistance = regionResult.get(outputParam + "_positive");
                Double negativeDistance = regionResult.get(outputParam + "_negative");
                Double comprehensiveScore = regionResult.get(outputParam);
                
                if (positiveDistance != null && !Double.isNaN(positiveDistance) && !Double.isInfinite(positiveDistance)) {
                    positiveDistances.add(positiveDistance);
                }
                
                if (negativeDistance != null && !Double.isNaN(negativeDistance) && !Double.isInfinite(negativeDistance)) {
                    negativeDistances.add(negativeDistance);
                }
                
                if (comprehensiveScore != null && !Double.isNaN(comprehensiveScore) && !Double.isInfinite(comprehensiveScore)) {
                    comprehensiveScores.add(comprehensiveScore);
                }
            }
            
            // 添加警告
            warnings.addAll(regionValidation.getWarnings());
        }
        
        // 计算统计信息
        statistics.put("totalRegions", totalRegions);
        statistics.put("validRegions", validRegions);
        statistics.put("invalidRegions", totalRegions - validRegions);
        statistics.put("invalidDistances", invalidDistances);
        statistics.put("invalidScores", invalidScores);
        statistics.put("zeroDistances", zeroDistances);
        
        if (!positiveDistances.isEmpty()) {
            statistics.put("positiveDistanceStats", calculateStatistics(positiveDistances));
        }
        
        if (!negativeDistances.isEmpty()) {
            statistics.put("negativeDistanceStats", calculateStatistics(negativeDistances));
        }
        
        if (!comprehensiveScores.isEmpty()) {
            statistics.put("comprehensiveScoreStats", calculateStatistics(comprehensiveScores));
        }
        
        // 生成修复建议
        if (invalidDistances > 0) {
            repairSuggestions.add("修复 " + invalidDistances + " 个无效距离值");
        }
        
        if (invalidScores > 0) {
            repairSuggestions.add("修复 " + invalidScores + " 个无效综合能力值");
        }
        
        if (zeroDistances > 0) {
            repairSuggestions.add("处理 " + zeroDistances + " 个零距离情况");
        }
        
        if (validRegions < totalRegions * 0.8) {
            repairSuggestions.add("有效地区比例过低（" + (validRegions * 100 / totalRegions) + "%），建议检查输入数据质量");
        }
        
        // 检查区分度
        if (!comprehensiveScores.isEmpty()) {
            double minScore = comprehensiveScores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double maxScore = comprehensiveScores.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            
            if (maxScore - minScore < 0.01) {
                warnings.add("综合能力值区分度过低（范围: " + String.format("%.4f", maxScore - minScore) + "）");
                repairSuggestions.add("检查指标选择和权重配置以提高区分度");
            }
        }
        
        boolean isValid = issues.isEmpty();
        
        log.info("TOPSIS结果验证完成 - 总地区: {}, 有效地区: {}, 问题数: {}, 警告数: {}", 
                totalRegions, validRegions, issues.size(), warnings.size());
        
        return new ValidationResult(isValid, issues, warnings, statistics, repairSuggestions);
    }
    
    @Override
    public Map<String, Map<String, Double>> repairResults(
            Map<String, Map<String, Double>> topsisResults,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        log.info("修复TOPSIS结果 - 地区数量: {}", topsisResults != null ? topsisResults.size() : 0);
        
        if (topsisResults == null || topsisResults.isEmpty()) {
            return new HashMap<>();
        }
        
        if (algorithmConfig == null || !algorithmConfig.isValid()) {
            log.error("算法配置无效，无法修复结果");
            return topsisResults;
        }
        
        Map<String, Map<String, Double>> repairedResults = new HashMap<>();
        int repairedCount = 0;
        
        for (Map.Entry<String, Map<String, Double>> entry : topsisResults.entrySet()) {
            String regionCode = entry.getKey();
            Map<String, Double> regionResult = entry.getValue();
            
            RegionValidationResult validation = validateRegionResult(regionCode, regionResult, algorithmConfig);
            
            if (validation.isValid()) {
                // 结果有效，直接使用
                repairedResults.put(regionCode, new HashMap<>(regionResult));
            } else {
                // 结果无效，进行修复
                Map<String, Double> repairedRegionResult = repairRegionResult(regionCode, regionResult, algorithmConfig);
                repairedResults.put(regionCode, repairedRegionResult);
                repairedCount++;
            }
        }
        
        log.info("TOPSIS结果修复完成 - 修复地区数: {}/{}", repairedCount, topsisResults.size());
        
        return repairedResults;
    }
    
    @Override
    public RegionValidationResult validateRegionResult(
            String regionCode,
            Map<String, Double> regionResult,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Double> originalValues = new HashMap<>();
        Map<String, Double> suggestedValues = new HashMap<>();
        
        if (regionResult == null || regionResult.isEmpty()) {
            issues.add("地区 " + regionCode + " 结果为空");
            return new RegionValidationResult(regionCode, false, issues, warnings, originalValues, suggestedValues);
        }
        
        String outputParam = algorithmConfig.getOutputParam();
        
        // 验证距离值
        Double positiveDistance = regionResult.get(outputParam + "_positive");
        Double negativeDistance = regionResult.get(outputParam + "_negative");
        Double comprehensiveScore = regionResult.get(outputParam);
        
        // 记录原始值
        if (positiveDistance != null) originalValues.put(outputParam + "_positive", positiveDistance);
        if (negativeDistance != null) originalValues.put(outputParam + "_negative", negativeDistance);
        if (comprehensiveScore != null) originalValues.put(outputParam, comprehensiveScore);
        
        // 验证正理想解距离
        if (positiveDistance == null) {
            issues.add("地区 " + regionCode + " 缺少正理想解距离");
            suggestedValues.put(outputParam + "_positive", 0.0);
        } else if (Double.isNaN(positiveDistance)) {
            issues.add("地区 " + regionCode + " 正理想解距离为NaN");
            suggestedValues.put(outputParam + "_positive", 0.0);
        } else if (Double.isInfinite(positiveDistance)) {
            issues.add("地区 " + regionCode + " 正理想解距离为无穷");
            suggestedValues.put(outputParam + "_positive", 1.0);
        } else if (positiveDistance < 0) {
            issues.add("地区 " + regionCode + " 正理想解距离为负数: " + positiveDistance);
            suggestedValues.put(outputParam + "_positive", Math.abs(positiveDistance));
        }
        
        // 验证负理想解距离
        if (negativeDistance == null) {
            issues.add("地区 " + regionCode + " 缺少负理想解距离");
            suggestedValues.put(outputParam + "_negative", 0.0);
        } else if (Double.isNaN(negativeDistance)) {
            issues.add("地区 " + regionCode + " 负理想解距离为NaN");
            suggestedValues.put(outputParam + "_negative", 0.0);
        } else if (Double.isInfinite(negativeDistance)) {
            issues.add("地区 " + regionCode + " 负理想解距离为无穷");
            suggestedValues.put(outputParam + "_negative", 1.0);
        } else if (negativeDistance < 0) {
            issues.add("地区 " + regionCode + " 负理想解距离为负数: " + negativeDistance);
            suggestedValues.put(outputParam + "_negative", Math.abs(negativeDistance));
        }
        
        // 验证综合能力值
        if (comprehensiveScore == null) {
            issues.add("地区 " + regionCode + " 缺少综合能力值");
            suggestedValues.put(outputParam, DEFAULT_COMPREHENSIVE_SCORE);
        } else if (Double.isNaN(comprehensiveScore)) {
            issues.add("地区 " + regionCode + " 综合能力值为NaN");
            suggestedValues.put(outputParam, DEFAULT_COMPREHENSIVE_SCORE);
        } else if (Double.isInfinite(comprehensiveScore)) {
            issues.add("地区 " + regionCode + " 综合能力值为无穷");
            suggestedValues.put(outputParam, DEFAULT_COMPREHENSIVE_SCORE);
        } else if (comprehensiveScore < 0) {
            issues.add("地区 " + regionCode + " 综合能力值为负数: " + comprehensiveScore);
            suggestedValues.put(outputParam, 0.0);
        } else if (comprehensiveScore > 1) {
            issues.add("地区 " + regionCode + " 综合能力值超过1: " + comprehensiveScore);
            suggestedValues.put(outputParam, 1.0);
        }
        
        // 检查特殊情况和警告
        if (positiveDistance != null && negativeDistance != null) {
            if (positiveDistance == 0 && negativeDistance == 0) {
                warnings.add("地区 " + regionCode + " 到正负理想解距离都为0");
                suggestedValues.put(outputParam, DEFAULT_COMPREHENSIVE_SCORE);
            }
            
            if (positiveDistance > 0 && negativeDistance > 0) {
                double ratio = Math.max(positiveDistance, negativeDistance) / Math.min(positiveDistance, negativeDistance);
                if (ratio > MAX_DISTANCE_RATIO) {
                    warnings.add("地区 " + regionCode + " 距离比例过大: " + String.format("%.2f", ratio));
                }
            }
            
            // 验证综合能力值计算的一致性
            if (comprehensiveScore != null && positiveDistance + negativeDistance > 0) {
                double expectedScore = negativeDistance / (negativeDistance + positiveDistance);
                double diff = Math.abs(comprehensiveScore - expectedScore);
                
                if (diff > 0.001) {
                    warnings.add("地区 " + regionCode + " 综合能力值计算不一致，期望: " + 
                               String.format("%.4f", expectedScore) + ", 实际: " + String.format("%.4f", comprehensiveScore));
                    suggestedValues.put(outputParam, expectedScore);
                }
            }
        }
        
        boolean isValid = issues.isEmpty();
        
        return new RegionValidationResult(regionCode, isValid, issues, warnings, originalValues, suggestedValues);
    }
    
    @Override
    public Map<String, Double> repairRegionResult(
            String regionCode,
            Map<String, Double> regionResult,
            TOPSISAlgorithmConfig algorithmConfig) {
        
        log.debug("修复地区 {} 的TOPSIS结果", regionCode);
        
        if (regionResult == null) {
            regionResult = new HashMap<>();
        }
        
        Map<String, Double> repairedResult = new HashMap<>(regionResult);
        String outputParam = algorithmConfig.getOutputParam();
        
        // 获取验证结果和修复建议
        RegionValidationResult validation = validateRegionResult(regionCode, regionResult, algorithmConfig);
        
        if (validation.isValid()) {
            return repairedResult; // 无需修复
        }
        
        // 应用修复建议
        Map<String, Double> suggestedValues = validation.getSuggestedValues();
        
        for (Map.Entry<String, Double> entry : suggestedValues.entrySet()) {
            String key = entry.getKey();
            Double suggestedValue = entry.getValue();
            
            Double originalValue = repairedResult.get(key);
            repairedResult.put(key, suggestedValue);
            
            log.debug("修复地区 {} 的 {} 值: {} -> {}", regionCode, key, originalValue, suggestedValue);
        }
        
        // 重新计算综合能力值以确保一致性
        Double positiveDistance = repairedResult.get(outputParam + "_positive");
        Double negativeDistance = repairedResult.get(outputParam + "_negative");
        
        if (positiveDistance != null && negativeDistance != null && 
            positiveDistance >= 0 && negativeDistance >= 0) {
            
            if (positiveDistance + negativeDistance > MIN_DISTANCE_THRESHOLD) {
                double comprehensiveScore = negativeDistance / (negativeDistance + positiveDistance);
                repairedResult.put(outputParam, comprehensiveScore);
                
                log.debug("重新计算地区 {} 综合能力值: {}", regionCode, comprehensiveScore);
            } else {
                // 距离和太小，使用默认值
                repairedResult.put(outputParam, DEFAULT_COMPREHENSIVE_SCORE);
                log.debug("地区 {} 距离和过小，使用默认综合能力值: {}", regionCode, DEFAULT_COMPREHENSIVE_SCORE);
            }
        }
        
        log.info("地区 {} TOPSIS结果修复完成", regionCode);
        
        return repairedResult;
    }
    
    /**
     * 计算数值列表的统计信息
     */
    private Map<String, Double> calculateStatistics(List<Double> values) {
        if (values.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, Double> stats = new HashMap<>();
        
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        
        stats.put("min", min);
        stats.put("max", max);
        stats.put("avg", avg);
        stats.put("sum", sum);
        stats.put("count", (double) values.size());
        stats.put("range", max - min);
        
        // 计算标准差
        if (values.size() > 1) {
            double variance = values.stream()
                    .mapToDouble(v -> Math.pow(v - avg, 2))
                    .average()
                    .orElse(0.0);
            stats.put("stdDev", Math.sqrt(variance));
        }
        
        return stats;
    }
}
