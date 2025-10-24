package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS配置验证服务接口
 * 
 * 提供TOPSIS配置的全面验证功能
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISConfigValidationService {
    
    /**
     * 验证TOPSIS配置的完整性和正确性
     * 
     * @param config TOPSIS配置
     * @return 验证结果
     */
    ConfigValidationResult validateConfig(TOPSISAlgorithmConfig config);
    
    /**
     * 验证指标列在实际数据中的存在性
     * 
     * @param modelId 模型ID
     * @param indicators 指标列表
     * @return 验证结果
     */
    DataExistenceValidationResult validateDataExistence(Long modelId, List<String> indicators);
    
    /**
     * 验证指标数据质量
     * 
     * @param modelId 模型ID
     * @param indicators 指标列表
     * @return 数据质量验证结果
     */
    DataQualityValidationResult validateDataQuality(Long modelId, List<String> indicators);
    
    /**
     * 验证指标数据完整性
     * 
     * @param modelId 模型ID
     * @param indicators 指标列表
     * @param minCompletenessThreshold 最小完整性阈值
     * @return 完整性验证结果
     */
    CompletenessValidationResult validateCompleteness(Long modelId, List<String> indicators, double minCompletenessThreshold);
    
    /**
     * 验证TOPSIS算法的可执行性
     * 
     * @param config TOPSIS配置
     * @return 可执行性验证结果
     */
    ExecutabilityValidationResult validateExecutability(TOPSISAlgorithmConfig config);
    
    /**
     * 综合验证TOPSIS配置
     * 
     * @param config TOPSIS配置
     * @return 综合验证结果
     */
    ComprehensiveValidationResult comprehensiveValidation(TOPSISAlgorithmConfig config);
    
    /**
     * 配置验证结果
     */
    class ConfigValidationResult {
        private boolean isValid;
        private List<String> errors;
        private List<String> warnings;
        private List<String> suggestions;
        
        public ConfigValidationResult(boolean isValid, List<String> errors, List<String> warnings, List<String> suggestions) {
            this.isValid = isValid;
            this.errors = errors;
            this.warnings = warnings;
            this.suggestions = suggestions;
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getSuggestions() { return suggestions; }
    }
    
    /**
     * 数据存在性验证结果
     */
    class DataExistenceValidationResult {
        private Map<String, Boolean> indicatorExistence;
        private List<String> missingIndicators;
        private List<String> availableIndicators;
        private double existenceRate;
        
        public DataExistenceValidationResult(Map<String, Boolean> indicatorExistence, 
                                           List<String> missingIndicators, 
                                           List<String> availableIndicators, 
                                           double existenceRate) {
            this.indicatorExistence = indicatorExistence;
            this.missingIndicators = missingIndicators;
            this.availableIndicators = availableIndicators;
            this.existenceRate = existenceRate;
        }
        
        public Map<String, Boolean> getIndicatorExistence() { return indicatorExistence; }
        public List<String> getMissingIndicators() { return missingIndicators; }
        public List<String> getAvailableIndicators() { return availableIndicators; }
        public double getExistenceRate() { return existenceRate; }
        public boolean isAllExist() { return missingIndicators.isEmpty(); }
    }
    
    /**
     * 数据质量验证结果
     */
    class DataQualityValidationResult {
        private Map<String, Double> qualityScores;
        private List<String> lowQualityIndicators;
        private List<String> highQualityIndicators;
        private double averageQualityScore;
        private List<String> qualityIssues;
        
        public DataQualityValidationResult(Map<String, Double> qualityScores,
                                         List<String> lowQualityIndicators,
                                         List<String> highQualityIndicators,
                                         double averageQualityScore,
                                         List<String> qualityIssues) {
            this.qualityScores = qualityScores;
            this.lowQualityIndicators = lowQualityIndicators;
            this.highQualityIndicators = highQualityIndicators;
            this.averageQualityScore = averageQualityScore;
            this.qualityIssues = qualityIssues;
        }
        
        public Map<String, Double> getQualityScores() { return qualityScores; }
        public List<String> getLowQualityIndicators() { return lowQualityIndicators; }
        public List<String> getHighQualityIndicators() { return highQualityIndicators; }
        public double getAverageQualityScore() { return averageQualityScore; }
        public List<String> getQualityIssues() { return qualityIssues; }
        public boolean isQualityAcceptable() { return averageQualityScore >= 0.7; }
    }
    
    /**
     * 完整性验证结果
     */
    class CompletenessValidationResult {
        private Map<String, Double> completenessScores;
        private List<String> incompleteIndicators;
        private List<String> completeIndicators;
        private double averageCompleteness;
        private boolean meetsThreshold;
        
        public CompletenessValidationResult(Map<String, Double> completenessScores,
                                          List<String> incompleteIndicators,
                                          List<String> completeIndicators,
                                          double averageCompleteness,
                                          boolean meetsThreshold) {
            this.completenessScores = completenessScores;
            this.incompleteIndicators = incompleteIndicators;
            this.completeIndicators = completeIndicators;
            this.averageCompleteness = averageCompleteness;
            this.meetsThreshold = meetsThreshold;
        }
        
        public Map<String, Double> getCompletenessScores() { return completenessScores; }
        public List<String> getIncompleteIndicators() { return incompleteIndicators; }
        public List<String> getCompleteIndicators() { return completeIndicators; }
        public double getAverageCompleteness() { return averageCompleteness; }
        public boolean isMeetsThreshold() { return meetsThreshold; }
    }
    
    /**
     * 可执行性验证结果
     */
    class ExecutabilityValidationResult {
        private boolean isExecutable;
        private List<String> executionBlockers;
        private List<String> executionWarnings;
        private Map<String, Object> executionContext;
        
        public ExecutabilityValidationResult(boolean isExecutable,
                                           List<String> executionBlockers,
                                           List<String> executionWarnings,
                                           Map<String, Object> executionContext) {
            this.isExecutable = isExecutable;
            this.executionBlockers = executionBlockers;
            this.executionWarnings = executionWarnings;
            this.executionContext = executionContext;
        }
        
        public boolean isExecutable() { return isExecutable; }
        public List<String> getExecutionBlockers() { return executionBlockers; }
        public List<String> getExecutionWarnings() { return executionWarnings; }
        public Map<String, Object> getExecutionContext() { return executionContext; }
    }
    
    /**
     * 综合验证结果
     */
    class ComprehensiveValidationResult {
        private boolean overallValid;
        private ConfigValidationResult configValidation;
        private DataExistenceValidationResult existenceValidation;
        private DataQualityValidationResult qualityValidation;
        private CompletenessValidationResult completenessValidation;
        private ExecutabilityValidationResult executabilityValidation;
        private List<String> criticalIssues;
        private List<String> recommendations;
        private double confidenceScore;
        
        public ComprehensiveValidationResult(boolean overallValid,
                                           ConfigValidationResult configValidation,
                                           DataExistenceValidationResult existenceValidation,
                                           DataQualityValidationResult qualityValidation,
                                           CompletenessValidationResult completenessValidation,
                                           ExecutabilityValidationResult executabilityValidation,
                                           List<String> criticalIssues,
                                           List<String> recommendations,
                                           double confidenceScore) {
            this.overallValid = overallValid;
            this.configValidation = configValidation;
            this.existenceValidation = existenceValidation;
            this.qualityValidation = qualityValidation;
            this.completenessValidation = completenessValidation;
            this.executabilityValidation = executabilityValidation;
            this.criticalIssues = criticalIssues;
            this.recommendations = recommendations;
            this.confidenceScore = confidenceScore;
        }
        
        public boolean isOverallValid() { return overallValid; }
        public ConfigValidationResult getConfigValidation() { return configValidation; }
        public DataExistenceValidationResult getExistenceValidation() { return existenceValidation; }
        public DataQualityValidationResult getQualityValidation() { return qualityValidation; }
        public CompletenessValidationResult getCompletenessValidation() { return completenessValidation; }
        public ExecutabilityValidationResult getExecutabilityValidation() { return executabilityValidation; }
        public List<String> getCriticalIssues() { return criticalIssues; }
        public List<String> getRecommendations() { return recommendations; }
        public double getConfidenceScore() { return confidenceScore; }
    }
}