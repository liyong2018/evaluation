package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;

import java.util.Map;

/**
 * TOPSIS结果验证器接口
 * 
 * 负责验证TOPSIS计算结果的合理性并提供自动修复机制
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISResultValidator {
    
    /**
     * 验证TOPSIS计算结果
     * 
     * @param topsisResults TOPSIS计算结果
     * @param algorithmConfig 算法配置
     * @return 验证结果
     */
    ValidationResult validateResults(
        Map<String, Map<String, Double>> topsisResults,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 修复异常的TOPSIS结果
     * 
     * @param topsisResults 原始TOPSIS结果
     * @param algorithmConfig 算法配置
     * @return 修复后的结果
     */
    Map<String, Map<String, Double>> repairResults(
        Map<String, Map<String, Double>> topsisResults,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 验证单个地区的结果
     * 
     * @param regionCode 地区代码
     * @param regionResult 地区结果
     * @param algorithmConfig 算法配置
     * @return 验证结果
     */
    RegionValidationResult validateRegionResult(
        String regionCode,
        Map<String, Double> regionResult,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 修复单个地区的异常结果
     * 
     * @param regionCode 地区代码
     * @param regionResult 地区结果
     * @param algorithmConfig 算法配置
     * @return 修复后的结果
     */
    Map<String, Double> repairRegionResult(
        String regionCode,
        Map<String, Double> regionResult,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 验证结果数据类
     */
    class ValidationResult {
        private boolean valid;
        private java.util.List<String> issues;
        private java.util.List<String> warnings;
        private java.util.Map<String, Object> statistics;
        private java.util.List<String> repairSuggestions;
        
        public ValidationResult(boolean valid, java.util.List<String> issues, 
                              java.util.List<String> warnings, java.util.Map<String, Object> statistics,
                              java.util.List<String> repairSuggestions) {
            this.valid = valid;
            this.issues = issues;
            this.warnings = warnings;
            this.statistics = statistics;
            this.repairSuggestions = repairSuggestions;
        }
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public java.util.List<String> getIssues() { return issues; }
        public void setIssues(java.util.List<String> issues) { this.issues = issues; }
        
        public java.util.List<String> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<String> warnings) { this.warnings = warnings; }
        
        public java.util.Map<String, Object> getStatistics() { return statistics; }
        public void setStatistics(java.util.Map<String, Object> statistics) { this.statistics = statistics; }
        
        public java.util.List<String> getRepairSuggestions() { return repairSuggestions; }
        public void setRepairSuggestions(java.util.List<String> repairSuggestions) { this.repairSuggestions = repairSuggestions; }
    }
    
    /**
     * 地区验证结果数据类
     */
    class RegionValidationResult {
        private String regionCode;
        private boolean valid;
        private java.util.List<String> issues;
        private java.util.List<String> warnings;
        private java.util.Map<String, Double> originalValues;
        private java.util.Map<String, Double> suggestedValues;
        
        public RegionValidationResult(String regionCode, boolean valid, 
                                    java.util.List<String> issues, java.util.List<String> warnings,
                                    java.util.Map<String, Double> originalValues, 
                                    java.util.Map<String, Double> suggestedValues) {
            this.regionCode = regionCode;
            this.valid = valid;
            this.issues = issues;
            this.warnings = warnings;
            this.originalValues = originalValues;
            this.suggestedValues = suggestedValues;
        }
        
        // Getters and setters
        public String getRegionCode() { return regionCode; }
        public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public java.util.List<String> getIssues() { return issues; }
        public void setIssues(java.util.List<String> issues) { this.issues = issues; }
        
        public java.util.List<String> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<String> warnings) { this.warnings = warnings; }
        
        public java.util.Map<String, Double> getOriginalValues() { return originalValues; }
        public void setOriginalValues(java.util.Map<String, Double> originalValues) { this.originalValues = originalValues; }
        
        public java.util.Map<String, Double> getSuggestedValues() { return suggestedValues; }
        public void setSuggestedValues(java.util.Map<String, Double> suggestedValues) { this.suggestedValues = suggestedValues; }
    }
}