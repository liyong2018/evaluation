package com.evaluate.service;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS配置分析器接口
 * 
 * 负责分析和对比不同模型的TOPSIS配置差异
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISConfigurationAnalyzer {
    
    /**
     * 对比两个模型的TOPSIS配置
     * 
     * @param modelId1 模型1 ID
     * @param modelId2 模型2 ID
     * @param stepCode 步骤代码
     * @return 配置差异报告
     */
    Map<String, Object> compareModelConfigurations(Long modelId1, Long modelId2, String stepCode);
    
    /**
     * 分析单个模型的TOPSIS配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 配置分析报告
     */
    Map<String, Object> analyzeModelConfiguration(Long modelId, String stepCode);
    
    /**
     * 获取模型的所有TOPSIS相关配置
     * 
     * @param modelId 模型ID
     * @return TOPSIS配置列表
     */
    List<Map<String, Object>> getTOPSISConfigurations(Long modelId);
    
    /**
     * 验证TOPSIS配置的有效性
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 验证结果
     */
    Map<String, Object> validateConfiguration(Long modelId, String stepCode);
    
    /**
     * 生成配置差异详细报告
     * 
     * @param modelIds 模型ID列表
     * @param stepCode 步骤代码
     * @return 详细差异报告
     */
    Map<String, Object> generateDetailedComparisonReport(List<Long> modelIds, String stepCode);
    
    /**
     * 分析配置历史变更
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 历史变更分析
     */
    Map<String, Object> analyzeConfigurationHistory(Long modelId, String stepCode);
    
    /**
     * 推荐配置优化建议
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 优化建议
     */
    List<String> recommendConfigurationOptimizations(Long modelId, String stepCode);
}