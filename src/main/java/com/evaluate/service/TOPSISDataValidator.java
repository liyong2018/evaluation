package com.evaluate.service;

import java.util.Map;

/**
 * TOPSIS数据验证器接口
 * 
 * 负责验证TOPSIS计算输入数据的质量和完整性
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISDataValidator {
    
    /**
     * 验证定权数据
     * 
     * @param weightedData 定权数据
     * @return 验证结果
     */
    Map<String, Object> validateWeightedData(Map<String, Map<String, Double>> weightedData);
    
    /**
     * 检测数据异常
     * 
     * @param weightedData 定权数据
     * @return 异常检测结果
     */
    Map<String, Object> detectAnomalies(Map<String, Map<String, Double>> weightedData);
    
    /**
     * 验证数据完整性
     * 
     * @param weightedData 定权数据
     * @return 完整性验证结果
     */
    Map<String, Object> validateDataCompleteness(Map<String, Map<String, Double>> weightedData);
    
    /**
     * 验证数据一致性
     * 
     * @param weightedData 定权数据
     * @return 一致性验证结果
     */
    Map<String, Object> validateDataConsistency(Map<String, Map<String, Double>> weightedData);
    
    /**
     * 生成数据质量报告
     * 
     * @param weightedData 定权数据
     * @return 数据质量报告
     */
    Map<String, Object> generateDataQualityReport(Map<String, Map<String, Double>> weightedData);
}