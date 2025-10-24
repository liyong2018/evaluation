package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.dto.topsis.IndicatorMetadata;
import com.evaluate.service.TOPSISConfigValidationService;
import com.evaluate.service.IndicatorAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TOPSIS配置验证服务实现类
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISConfigValidationServiceImpl implements TOPSISConfigValidationService {

    private static final Logger log = LoggerFactory.getLogger(TOPSISConfigValidationServiceImpl.class);


    @Autowired
    private IndicatorAnalysisService indicatorAnalysisService;

    // 验证阈值常量
    private static final double MIN_QUALITY_THRESHOLD = 0.7;
    private static final double MIN_COMPLETENESS_THRESHOLD = 0.8;
    private static final int MIN_INDICATOR_COUNT = 2;
    private static final int MAX_INDICATOR_COUNT = 15;

    @Override
    public ConfigValidationResult validateConfig(TOPSISAlgorithmConfig config) {
        log.debug("验证TOPSIS配置: {}", config);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        // 基本配置验证
        if (config == null) {
            errors.add("TOPSIS配置不能为空");
            return new ConfigValidationResult(false, errors, warnings, suggestions);
        }
        
        // 验证必填字段
        if (config.getStepId() == null) {
            errors.add("步骤ID不能为空");
        }
        
        if (!StringUtils.hasText(config.getAlgorithmCode())) {
            errors.add("算法代码不能为空");
        }
        
        if (!StringUtils.hasText(config.getOutputParam())) {
            errors.add("输出参数不能为空");
        }
        
        // 验证指标列表
        if (config.getIndicators() == null || config.getIndicators().isEmpty()) {
            errors.add("指标列表不能为空");
        } else {
            validateIndicatorList(config.getIndicators(), errors, warnings, suggestions);
        }
        
        // 验证算法类型
        if (StringUtils.hasText(config.getAlgorithmType())) {
            if (!config.getAlgorithmType().equals("TOPSIS_POSITIVE") && 
                !config.getAlgorithmType().equals("TOPSIS_NEGATIVE")) {
                errors.add("算法类型必须是 TOPSIS_POSITIVE 或 TOPSIS_NEGATIVE");
            }
        } else {
            warnings.add("未指定算法类型，将使用默认值");
        }
        
        // 验证表达式格式
        if (StringUtils.hasText(config.getOriginalExpression())) {
            validateExpressionFormat(config.getOriginalExpression(), errors, warnings);
        }
        
        // 添加建议
        if (config.getIndicators() != null && config.getIndicators().size() > 10) {
            suggestions.add("考虑减少指标数量以提高计算效率");
        }
        
        if (config.getIndicators() != null && config.getIndicators().size() < 3) {
            suggestions.add("建议增加更多指标以提高评估的全面性");
        }
        
        boolean isValid = errors.isEmpty();
        return new ConfigValidationResult(isValid, errors, warnings, suggestions);
    }

    @Override
    public DataExistenceValidationResult validateDataExistence(Long modelId, List<String> indicators) {
        log.debug("验证数据存在性: modelId={}, indicators={}", modelId, indicators);
        
        Map<String, Boolean> indicatorExistence = new HashMap<>();
        List<String> missingIndicators = new ArrayList<>();
        List<String> availableIndicators = new ArrayList<>();
        
        try {
            // 获取所有可用指标
            List<IndicatorMetadata> allMetadata = indicatorAnalysisService.getIndicatorMetadata(modelId);
            Set<String> availableIndicatorSet = allMetadata.stream()
                    .map(IndicatorMetadata::getColumnName)
                    .collect(Collectors.toSet());
            
            // 检查每个指标的存在性
            for (String indicator : indicators) {
                boolean exists = availableIndicatorSet.contains(indicator);
                indicatorExistence.put(indicator, exists);
                
                if (exists) {
                    availableIndicators.add(indicator);
                } else {
                    missingIndicators.add(indicator);
                }
            }
            
            double existenceRate = indicators.isEmpty() ? 0.0 : 
                    (double) availableIndicators.size() / indicators.size();
            
            return new DataExistenceValidationResult(indicatorExistence, missingIndicators, 
                    availableIndicators, existenceRate);
                    
        } catch (Exception e) {
            log.error("验证数据存在性失败: modelId={}", modelId, e);
            
            // 返回失败结果
            for (String indicator : indicators) {
                indicatorExistence.put(indicator, false);
                missingIndicators.add(indicator);
            }
            
            return new DataExistenceValidationResult(indicatorExistence, missingIndicators, 
                    availableIndicators, 0.0);
        }
    }

    @Override
    public DataQualityValidationResult validateDataQuality(Long modelId, List<String> indicators) {
        log.debug("验证数据质量: modelId={}, indicators={}", modelId, indicators);
        
        Map<String, Double> qualityScores = new HashMap<>();
        List<String> lowQualityIndicators = new ArrayList<>();
        List<String> highQualityIndicators = new ArrayList<>();
        List<String> qualityIssues = new ArrayList<>();
        
        try {
            double totalQuality = 0.0;
            int validCount = 0;
            
            for (String indicator : indicators) {
                IndicatorMetadata metadata = indicatorAnalysisService.analyzeIndicatorQuality(modelId, indicator);
                
                if (metadata != null) {
                    double qualityScore = metadata.getQualityScore();
                    qualityScores.put(indicator, qualityScore);
                    totalQuality += qualityScore;
                    validCount++;
                    
                    if (qualityScore >= MIN_QUALITY_THRESHOLD) {
                        highQualityIndicators.add(indicator);
                    } else {
                        lowQualityIndicators.add(indicator);
                        qualityIssues.add(String.format("指标 %s 质量评分较低: %.2f", 
                                metadata.getDisplayName(), qualityScore));
                    }
                    
                    // 检查具体质量问题
                    if (metadata.getCompleteness() < 0.9) {
                        qualityIssues.add(String.format("指标 %s 数据完整性不足: %.1f%%", 
                                metadata.getDisplayName(), metadata.getCompletenessPercentage()));
                    }
                    
                    if (metadata.getStdDev() != null && metadata.getAvgValue() != null && 
                        metadata.getAvgValue() != 0) {
                        double cv = metadata.getStdDev() / Math.abs(metadata.getAvgValue());
                        if (cv > 1.0) {
                            qualityIssues.add(String.format("指标 %s 数据变异性过大: CV=%.2f", 
                                    metadata.getDisplayName(), cv));
                        }
                    }
                } else {
                    qualityScores.put(indicator, 0.0);
                    lowQualityIndicators.add(indicator);
                    qualityIssues.add(String.format("无法获取指标 %s 的质量信息", indicator));
                }
            }
            
            double averageQualityScore = validCount > 0 ? totalQuality / validCount : 0.0;
            
            return new DataQualityValidationResult(qualityScores, lowQualityIndicators, 
                    highQualityIndicators, averageQualityScore, qualityIssues);
                    
        } catch (Exception e) {
            log.error("验证数据质量失败: modelId={}", modelId, e);
            
            // 返回失败结果
            for (String indicator : indicators) {
                qualityScores.put(indicator, 0.0);
                lowQualityIndicators.add(indicator);
            }
            qualityIssues.add("数据质量验证过程中发生错误");
            
            return new DataQualityValidationResult(qualityScores, lowQualityIndicators, 
                    Collections.emptyList(), 0.0, qualityIssues);
        }
    }

    @Override
    public CompletenessValidationResult validateCompleteness(Long modelId, List<String> indicators, 
                                                           double minCompletenessThreshold) {
        log.debug("验证数据完整性: modelId={}, indicators={}, threshold={}", 
                modelId, indicators, minCompletenessThreshold);
        
        Map<String, Double> completenessScores = indicatorAnalysisService.validateIndicatorCompleteness(modelId, indicators);
        
        List<String> incompleteIndicators = new ArrayList<>();
        List<String> completeIndicators = new ArrayList<>();
        
        double totalCompleteness = 0.0;
        int validCount = 0;
        
        for (Map.Entry<String, Double> entry : completenessScores.entrySet()) {
            String indicator = entry.getKey();
            Double completeness = entry.getValue();
            
            if (completeness != null) {
                totalCompleteness += completeness;
                validCount++;
                
                if (completeness >= minCompletenessThreshold) {
                    completeIndicators.add(indicator);
                } else {
                    incompleteIndicators.add(indicator);
                }
            } else {
                incompleteIndicators.add(indicator);
            }
        }
        
        double averageCompleteness = validCount > 0 ? totalCompleteness / validCount : 0.0;
        boolean meetsThreshold = averageCompleteness >= minCompletenessThreshold;
        
        return new CompletenessValidationResult(completenessScores, incompleteIndicators, 
                completeIndicators, averageCompleteness, meetsThreshold);
    }

    @Override
    public ExecutabilityValidationResult validateExecutability(TOPSISAlgorithmConfig config) {
        log.debug("验证TOPSIS可执行性: {}", config);
        
        List<String> executionBlockers = new ArrayList<>();
        List<String> executionWarnings = new ArrayList<>();
        Map<String, Object> executionContext = new HashMap<>();
        
        // 检查基本可执行性
        if (config == null) {
            executionBlockers.add("配置为空，无法执行");
            return new ExecutabilityValidationResult(false, executionBlockers, executionWarnings, executionContext);
        }
        
        // 检查必要参数
        if (config.getIndicators() == null || config.getIndicators().isEmpty()) {
            executionBlockers.add("缺少指标列表，无法执行TOPSIS计算");
        } else if (config.getIndicators().size() < MIN_INDICATOR_COUNT) {
            executionBlockers.add(String.format("指标数量不足，至少需要%d个指标", MIN_INDICATOR_COUNT));
        }
        
        if (!StringUtils.hasText(config.getOutputParam())) {
            executionBlockers.add("缺少输出参数定义");
        }
        
        // 检查算法类型
        if (!StringUtils.hasText(config.getAlgorithmType()) || 
            (!config.getAlgorithmType().equals("TOPSIS_POSITIVE") && 
             !config.getAlgorithmType().equals("TOPSIS_NEGATIVE"))) {
            executionWarnings.add("算法类型未正确设置，可能影响计算结果");
        }
        
        // 检查指标名称有效性
        if (config.getIndicators() != null) {
            for (String indicator : config.getIndicators()) {
                if (!StringUtils.hasText(indicator)) {
                    executionBlockers.add("发现空的指标名称");
                } else if (indicator.contains(" ") || indicator.contains(",")) {
                    executionWarnings.add(String.format("指标名称 '%s' 包含特殊字符，可能导致解析错误", indicator));
                }
            }
        }
        
        // 设置执行上下文
        executionContext.put("indicatorCount", config.getIndicators() != null ? config.getIndicators().size() : 0);
        executionContext.put("algorithmType", config.getAlgorithmType());
        executionContext.put("hasValidOutput", StringUtils.hasText(config.getOutputParam()));
        
        boolean isExecutable = executionBlockers.isEmpty();
        return new ExecutabilityValidationResult(isExecutable, executionBlockers, executionWarnings, executionContext);
    }

    @Override
    public ComprehensiveValidationResult comprehensiveValidation(TOPSISAlgorithmConfig config) {
        log.info("执行TOPSIS配置综合验证: {}", config);
        
        List<String> criticalIssues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // 1. 配置验证
        ConfigValidationResult configValidation = validateConfig(config);
        
        // 2. 数据存在性验证
        DataExistenceValidationResult existenceValidation = null;
        if (config != null && config.getModelId() != null && config.getIndicators() != null) {
            existenceValidation = validateDataExistence(config.getModelId(), config.getIndicators());
        }
        
        // 3. 数据质量验证
        DataQualityValidationResult qualityValidation = null;
        if (config != null && config.getModelId() != null && config.getIndicators() != null) {
            qualityValidation = validateDataQuality(config.getModelId(), config.getIndicators());
        }
        
        // 4. 完整性验证
        CompletenessValidationResult completenessValidation = null;
        if (config != null && config.getModelId() != null && config.getIndicators() != null) {
            completenessValidation = validateCompleteness(config.getModelId(), config.getIndicators(), MIN_COMPLETENESS_THRESHOLD);
        }
        
        // 5. 可执行性验证
        ExecutabilityValidationResult executabilityValidation = validateExecutability(config);
        
        // 分析关键问题
        if (configValidation != null && !configValidation.isValid()) {
            criticalIssues.addAll(configValidation.getErrors());
        }
        
        if (existenceValidation != null && !existenceValidation.isAllExist()) {
            criticalIssues.add(String.format("有%d个指标在数据中不存在", existenceValidation.getMissingIndicators().size()));
        }
        
        if (qualityValidation != null && !qualityValidation.isQualityAcceptable()) {
            criticalIssues.add(String.format("数据质量不达标，平均质量评分: %.2f", qualityValidation.getAverageQualityScore()));
        }
        
        if (completenessValidation != null && !completenessValidation.isMeetsThreshold()) {
            criticalIssues.add(String.format("数据完整性不足，平均完整性: %.1f%%", completenessValidation.getAverageCompleteness() * 100));
        }
        
        if (executabilityValidation != null && !executabilityValidation.isExecutable()) {
            criticalIssues.addAll(executabilityValidation.getExecutionBlockers());
        }
        
        // 生成建议
        generateRecommendations(configValidation, existenceValidation, qualityValidation, 
                              completenessValidation, executabilityValidation, recommendations);
        
        // 计算置信度评分
        double confidenceScore = calculateConfidenceScore(configValidation, existenceValidation, 
                                                        qualityValidation, completenessValidation, executabilityValidation);
        
        // 判断整体有效性
        boolean overallValid = criticalIssues.isEmpty() && confidenceScore >= 0.7;
        
        return new ComprehensiveValidationResult(overallValid, configValidation, existenceValidation, 
                qualityValidation, completenessValidation, executabilityValidation, 
                criticalIssues, recommendations, confidenceScore);
    }

    /**
     * 验证指标列表
     */
    private void validateIndicatorList(List<String> indicators, List<String> errors, 
                                     List<String> warnings, List<String> suggestions) {
        if (indicators.size() < MIN_INDICATOR_COUNT) {
            errors.add(String.format("指标数量不足，至少需要%d个指标", MIN_INDICATOR_COUNT));
        }
        
        if (indicators.size() > MAX_INDICATOR_COUNT) {
            warnings.add(String.format("指标数量过多(%d个)，可能影响计算性能", indicators.size()));
        }
        
        // 检查重复指标
        Set<String> uniqueIndicators = new HashSet<>(indicators);
        if (uniqueIndicators.size() < indicators.size()) {
            warnings.add("发现重复的指标");
        }
        
        // 检查空指标
        long emptyCount = indicators.stream().filter(s -> !StringUtils.hasText(s)).count();
        if (emptyCount > 0) {
            errors.add(String.format("发现%d个空的指标名称", emptyCount));
        }
    }

    /**
     * 验证表达式格式
     */
    private void validateExpressionFormat(String expression, List<String> errors, List<String> warnings) {
        if (!expression.startsWith("@TOPSIS_")) {
            errors.add("表达式格式错误，应以@TOPSIS_开头");
        }
        
        if (!expression.contains(":")) {
            errors.add("表达式格式错误，缺少冒号分隔符");
        }
        
        if (expression.endsWith(":") || expression.endsWith(",")) {
            warnings.add("表达式格式可能不完整");
        }
    }

    /**
     * 生成建议
     */
    private void generateRecommendations(ConfigValidationResult configValidation,
                                       DataExistenceValidationResult existenceValidation,
                                       DataQualityValidationResult qualityValidation,
                                       CompletenessValidationResult completenessValidation,
                                       ExecutabilityValidationResult executabilityValidation,
                                       List<String> recommendations) {
        
        if (configValidation != null && configValidation.getSuggestions() != null) {
            recommendations.addAll(configValidation.getSuggestions());
        }
        
        if (existenceValidation != null && !existenceValidation.isAllExist()) {
            recommendations.add("建议移除不存在的指标或检查指标名称拼写");
        }
        
        if (qualityValidation != null && !qualityValidation.getLowQualityIndicators().isEmpty()) {
            recommendations.add("建议替换低质量指标或进行数据清洗");
        }
        
        if (completenessValidation != null && !completenessValidation.getIncompleteIndicators().isEmpty()) {
            recommendations.add("建议补充缺失数据或使用完整性更高的指标");
        }
        
        if (executabilityValidation != null && !executabilityValidation.getExecutionWarnings().isEmpty()) {
            recommendations.add("建议修复执行警告以确保计算稳定性");
        }
    }

    /**
     * 计算置信度评分
     */
    private double calculateConfidenceScore(ConfigValidationResult configValidation,
                                          DataExistenceValidationResult existenceValidation,
                                          DataQualityValidationResult qualityValidation,
                                          CompletenessValidationResult completenessValidation,
                                          ExecutabilityValidationResult executabilityValidation) {
        
        double score = 0.0;
        
        // 配置有效性 (20%)
        if (configValidation != null && configValidation.isValid()) {
            score += 0.2;
        }
        
        // 数据存在性 (20%)
        if (existenceValidation != null) {
            score += 0.2 * existenceValidation.getExistenceRate();
        }
        
        // 数据质量 (25%)
        if (qualityValidation != null) {
            score += 0.25 * qualityValidation.getAverageQualityScore();
        }
        
        // 数据完整性 (25%)
        if (completenessValidation != null) {
            score += 0.25 * completenessValidation.getAverageCompleteness();
        }
        
        // 可执行性 (10%)
        if (executabilityValidation != null && executabilityValidation.isExecutable()) {
            score += 0.1;
        }
        
        return Math.min(1.0, Math.max(0.0, score));
    }
}
