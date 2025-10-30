package com.evaluate.service;

import java.util.Map;

/**
 * TOPSIS计算日志记录器接口
 * 
 * 负责记录TOPSIS计算过程中的详细信息
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISCalculationLogger {
    
    /**
     * 记录计算开始
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @param inputData 输入数据
     */
    void logCalculationStart(Long modelId, String stepCode, Map<String, Map<String, Double>> inputData);
    
    /**
     * 记录理想解计算
     * 
     * @param positiveIdeal 正理想解
     * @param negativeIdeal 负理想解
     */
    void logIdealSolutionCalculation(Map<String, Double> positiveIdeal, Map<String, Double> negativeIdeal);
    
    /**
     * 记录距离计算
     * 
     * @param regionCode 区域代码
     * @param regionData 区域数据
     * @param positiveDistance 正理想解距离
     * @param negativeDistance 负理想解距离
     */
    void logDistanceCalculation(String regionCode, Map<String, Double> regionData, 
                               double positiveDistance, double negativeDistance);
    
    /**
     * 记录数据验证结果
     * 
     * @param validationResult 验证结果
     */
    void logDataValidation(Map<String, Object> validationResult);
    
    /**
     * 记录异常情况
     * 
     * @param step 计算步骤
     * @param exception 异常信息
     * @param context 上下文信息
     */
    void logException(String step, Exception exception, Map<String, Object> context);
    
    /**
     * 记录计算完成
     * 
     * @param results 计算结果
     * @param calculationTimeMs 计算耗时
     */
    void logCalculationComplete(Map<String, Map<String, Double>> results, long calculationTimeMs);
    
    /**
     * 获取当前会话的日志
     * 
     * @return 日志记录
     */
    Map<String, Object> getCurrentSessionLog();
    
    /**
     * 清理当前会话日志
     */
    void clearCurrentSessionLog();
}