package com.evaluate.service;

import java.util.Map;

/**
 * TOPSIS计算器接口（兼容性接口）
 * 
 * 为了保持向后兼容性而保留的接口
 * 实际实现已迁移到UnifiedTOPSISCalculator
 * 
 * @author System
 * @since 2025-01-01
 * @deprecated 请使用 UnifiedTOPSISCalculator
 */
@Deprecated
public interface TOPSISCalculator {
    
    /**
     * 计算TOPSIS距离
     * 
     * @param weightedData 定权数据
     * @return 距离结果
     */
    Map<String, Map<String, Double>> calculateDistances(Map<String, Map<String, Double>> weightedData);
    
    /**
     * 计算理想解
     * 
     * @param weightedData 定权数据
     * @return 理想解
     */
    IdealSolution calculateIdealSolutions(Map<String, Map<String, Double>> weightedData);
    
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
    }
}