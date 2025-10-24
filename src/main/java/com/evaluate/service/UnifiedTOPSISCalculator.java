package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.dto.topsis.TOPSISDiagnosticReport;

import java.util.Map;

/**
 * 统一TOPSIS计算器接口
 * 
 * 提供支持动态列配置的通用TOPSIS计算功能
 * 
 * @author System
 * @since 2025-01-01
 */
public interface UnifiedTOPSISCalculator {
    
    /**
     * 计算TOPSIS距离（支持动态列配置）
     * 
     * @param weightedData 定权数据，格式：Map<区域代码, Map<指标名, 指标值>>
     * @param algorithmConfig 算法配置（包含指标列名）
     * @return TOPSIS距离结果，格式：Map<区域代码, Map<距离类型, 距离值>>
     */
    Map<String, Map<String, Double>> calculateDistances(
        Map<String, Map<String, Double>> weightedData,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 计算理想解（支持动态列配置）
     * 
     * @param weightedData 定权数据
     * @param algorithmConfig 算法配置
     * @return 理想解，包含正理想解和负理想解
     */
    IdealSolution calculateIdealSolutions(
        Map<String, Map<String, Double>> weightedData,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 计算欧几里得距离
     * 
     * @param regionData 地区数据
     * @param idealSolution 理想解
     * @param indicators 参与计算的指标列表
     * @return 距离值
     */
    double calculateEuclideanDistance(
        Map<String, Double> regionData, 
        Map<String, Double> idealSolution,
        java.util.List<String> indicators
    );
    
    /**
     * 诊断TOPSIS计算问题
     * 
     * @param weightedData 定权数据
     * @param algorithmConfig 算法配置
     * @return 诊断报告
     */
    TOPSISDiagnosticReport diagnoseCalculation(
        Map<String, Map<String, Double>> weightedData,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 验证TOPSIS结果的合理性
     * 
     * @param topsisResults TOPSIS结果
     * @param algorithmConfig 算法配置
     * @return 验证结果
     */
    Map<String, Object> validateTopsisResults(
        Map<String, Map<String, Double>> topsisResults,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 处理单区域情况的特殊计算
     * 
     * @param regionData 单个区域数据
     * @param algorithmConfig 算法配置
     * @return 单区域TOPSIS结果
     */
    Map<String, Double> calculateSingleRegionTopsis(
        Map<String, Double> regionData,
        TOPSISAlgorithmConfig algorithmConfig
    );
    
    /**
     * 理想解数据类
     */
    class IdealSolution {
        private Map<String, Double> positiveIdeal;
        private Map<String, Double> negativeIdeal;
        
        public IdealSolution(Map<String, Double> positiveIdeal, Map<String, Double> negativeIdeal) {
            this.positiveIdeal = positiveIdeal;
            this.negativeIdeal = negativeIdeal;
        }
        
        public Map<String, Double> getPositiveIdeal() {
            return positiveIdeal;
        }
        
        public void setPositiveIdeal(Map<String, Double> positiveIdeal) {
            this.positiveIdeal = positiveIdeal;
        }
        
        public Map<String, Double> getNegativeIdeal() {
            return negativeIdeal;
        }
        
        public void setNegativeIdeal(Map<String, Double> negativeIdeal) {
            this.negativeIdeal = negativeIdeal;
        }
        
        /**
         * 检查理想解是否有效
         * 
         * @return 是否有效
         */
        public boolean isValid() {
            return positiveIdeal != null && !positiveIdeal.isEmpty() &&
                   negativeIdeal != null && !negativeIdeal.isEmpty();
        }
        
        /**
         * 获取指标数量
         * 
         * @return 指标数量
         */
        public int getIndicatorCount() {
            return positiveIdeal != null ? positiveIdeal.size() : 0;
        }
    }
}