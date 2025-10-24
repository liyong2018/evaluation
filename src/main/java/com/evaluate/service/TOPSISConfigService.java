package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;

import java.util.List;

/**
 * TOPSIS配置服务接口
 * 
 * 提供TOPSIS算法配置的管理功能，包括读取、更新和验证
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISConfigService {
    
    /**
     * 获取模型的TOPSIS配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return TOPSIS配置，如果不存在则返回null
     */
    TOPSISAlgorithmConfig getTOPSISConfig(Long modelId, String stepCode);
    
    /**
     * 根据步骤ID获取TOPSIS配置
     * 
     * @param stepId 步骤ID
     * @return TOPSIS配置，如果不存在则返回null
     */
    TOPSISAlgorithmConfig getTOPSISConfigByStepId(Long stepId);
    
    /**
     * 更新TOPSIS配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @param indicators 指标列名列表
     * @param algorithmType 算法类型（TOPSIS_POSITIVE 或 TOPSIS_NEGATIVE）
     * @return 更新是否成功
     */
    boolean updateTOPSISConfig(Long modelId, String stepCode, List<String> indicators, String algorithmType);
    
    /**
     * 根据步骤ID更新TOPSIS配置
     * 
     * @param stepId 步骤ID
     * @param indicators 指标列名列表
     * @param algorithmType 算法类型（TOPSIS_POSITIVE 或 TOPSIS_NEGATIVE）
     * @return 更新是否成功
     */
    boolean updateTOPSISConfigByStepId(Long stepId, List<String> indicators, String algorithmType);
    
    /**
     * 获取模型的可用指标列
     * 
     * @param modelId 模型ID
     * @return 可用指标列表
     */
    List<String> getAvailableIndicators(Long modelId);
    
    /**
     * 验证指标列是否存在于模型数据中
     * 
     * @param modelId 模型ID
     * @param indicators 指标列名列表
     * @return 验证结果，包含存在和不存在的指标列表
     */
    IndicatorValidationResult validateIndicators(Long modelId, List<String> indicators);
    
    /**
     * 获取所有TOPSIS相关的算法配置
     * 
     * @param modelId 模型ID
     * @return TOPSIS算法配置列表
     */
    List<TOPSISAlgorithmConfig> getAllTOPSISConfigs(Long modelId);
    
    /**
     * 检查TOPSIS配置是否存在
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 配置是否存在
     */
    boolean hasTOPSISConfig(Long modelId, String stepCode);
    
    /**
     * 创建新的TOPSIS配置
     * 
     * @param config TOPSIS配置
     * @return 创建是否成功
     */
    boolean createTOPSISConfig(TOPSISAlgorithmConfig config);
    
    /**
     * 删除TOPSIS配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 删除是否成功
     */
    boolean deleteTOPSISConfig(Long modelId, String stepCode);
    
    /**
     * 验证TOPSIS配置的完整性和正确性
     * 
     * @param config TOPSIS配置
     * @return 验证结果
     */
    ValidationResult validateTOPSISConfig(TOPSISAlgorithmConfig config);
    
    /**
     * 验证结果类
     */
    class ValidationResult {
        private boolean isValid;
        private List<String> errors;
        private List<String> warnings;
        private List<String> suggestions;
        private double confidenceScore;
        
        public ValidationResult(boolean isValid, List<String> errors, List<String> warnings, 
                              List<String> suggestions, double confidenceScore) {
            this.isValid = isValid;
            this.errors = errors;
            this.warnings = warnings;
            this.suggestions = suggestions;
            this.confidenceScore = confidenceScore;
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getSuggestions() { return suggestions; }
        public double getConfidenceScore() { return confidenceScore; }
    }
    
    /**
     * 指标验证结果
     */
    class IndicatorValidationResult {
        private List<String> validIndicators;
        private List<String> invalidIndicators;
        private List<String> warnings;
        
        public IndicatorValidationResult(List<String> validIndicators, List<String> invalidIndicators, List<String> warnings) {
            this.validIndicators = validIndicators;
            this.invalidIndicators = invalidIndicators;
            this.warnings = warnings;
        }
        
        public List<String> getValidIndicators() {
            return validIndicators;
        }
        
        public List<String> getInvalidIndicators() {
            return invalidIndicators;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public boolean isValid() {
            return invalidIndicators == null || invalidIndicators.isEmpty();
        }
    }
}