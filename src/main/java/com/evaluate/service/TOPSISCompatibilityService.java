package com.evaluate.service;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS兼容性服务接口
 * 
 * 提供新旧TOPSIS计算器的切换机制和向后兼容性支持
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISCompatibilityService {
    
    /**
     * 计算TOPSIS距离（自动选择计算器）
     * 
     * @param weightedData 定权数据
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return TOPSIS距离结果
     */
    Map<String, Map<String, Double>> calculateDistances(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode);
    
    /**
     * 使用原始TOPSIS计算器
     * 
     * @param weightedData 定权数据
     * @return TOPSIS距离结果
     */
    Map<String, Map<String, Double>> calculateDistancesLegacy(
            Map<String, Map<String, Double>> weightedData);
    
    /**
     * 使用统一TOPSIS计算器
     * 
     * @param weightedData 定权数据
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return TOPSIS距离结果
     */
    Map<String, Map<String, Double>> calculateDistancesUnified(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode);
    
    /**
     * 检查是否应该使用统一计算器
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 是否使用统一计算器
     */
    boolean shouldUseUnifiedCalculator(Long modelId, String stepCode);
    
    /**
     * 检查兼容性
     * 
     * @param modelId 模型ID
     * @return 兼容性检查结果
     */
    Map<String, Object> checkCompatibility(Long modelId);
    
    /**
     * 获取兼容性问题
     * 
     * @param modelId 模型ID
     * @return 兼容性问题列表
     */
    List<String> getCompatibilityIssues(Long modelId);
    
    /**
     * 设置计算器选择策略
     * 
     * @param strategy 策略类型：AUTO, LEGACY, UNIFIED
     */
    void setCalculatorStrategy(CalculatorStrategy strategy);
    
    /**
     * 获取当前计算器选择策略
     * 
     * @return 当前策略
     */
    CalculatorStrategy getCalculatorStrategy();
    
    /**
     * 验证两个计算器的结果一致性
     * 
     * @param weightedData 定权数据
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 一致性验证结果
     */
    Map<String, Object> validateCalculatorConsistency(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode);
    
    /**
     * 计算器选择策略枚举
     */
    enum CalculatorStrategy {
        AUTO,    // 自动选择（默认）
        LEGACY,  // 强制使用原始计算器
        UNIFIED  // 强制使用统一计算器
    }
}