package com.evaluate.service;

import com.evaluate.dto.topsis.IndicatorMetadata;

import java.util.List;
import java.util.Map;

/**
 * 指标分析服务接口
 * 
 * 提供指标数据的分析和元数据管理功能
 * 
 * @author System
 * @since 2025-01-01
 */
public interface IndicatorAnalysisService {
    
    /**
     * 获取模型的所有指标元数据
     * 
     * @param modelId 模型ID
     * @return 指标元数据列表
     */
    List<IndicatorMetadata> getIndicatorMetadata(Long modelId);
    
    /**
     * 分析指标数据质量
     * 
     * @param modelId 模型ID
     * @param columnName 指标列名
     * @return 指标元数据（包含质量分析）
     */
    IndicatorMetadata analyzeIndicatorQuality(Long modelId, String columnName);
    
    /**
     * 获取推荐的TOPSIS指标
     * 
     * @param modelId 模型ID
     * @param maxCount 最大推荐数量
     * @return 推荐指标列表
     */
    List<IndicatorMetadata> getRecommendedIndicators(Long modelId, int maxCount);
    
    /**
     * 获取可用指标列表
     * 
     * @param modelId 模型ID
     * @return 可用指标名称列表
     */
    List<String> getAvailableIndicators(long modelId);
    
    /**
     * 验证指标数据完整性
     * 
     * @param modelId 模型ID
     * @param indicators 指标列表
     * @return 验证结果
     */
    Map<String, Double> validateIndicatorCompleteness(Long modelId, List<String> indicators);
    
    /**
     * 计算指标相关性矩阵
     * 
     * @param modelId 模型ID
     * @param indicators 指标列表
     * @return 相关性矩阵
     */
    Map<String, Map<String, Double>> calculateCorrelationMatrix(Long modelId, List<String> indicators);
    
    /**
     * 检测指标异常值
     * 
     * @param modelId 模型ID
     * @param columnName 指标列名
     * @return 异常值检测结果
     */
    OutlierDetectionResult detectOutliers(Long modelId, String columnName);
    
    /**
     * 获取指标统计摘要
     * 
     * @param modelId 模型ID
     * @param columnName 指标列名
     * @return 统计摘要
     */
    StatisticalSummary getStatisticalSummary(Long modelId, String columnName);
    
    /**
     * 异常值检测结果
     */
    class OutlierDetectionResult {
        private List<Double> outliers;
        private double outlierPercentage;
        private String detectionMethod;
        private Map<String, Object> parameters;
        
        public OutlierDetectionResult(List<Double> outliers, double outlierPercentage, 
                                    String detectionMethod, Map<String, Object> parameters) {
            this.outliers = outliers;
            this.outlierPercentage = outlierPercentage;
            this.detectionMethod = detectionMethod;
            this.parameters = parameters;
        }
        
        public List<Double> getOutliers() { return outliers; }
        public double getOutlierPercentage() { return outlierPercentage; }
        public String getDetectionMethod() { return detectionMethod; }
        public Map<String, Object> getParameters() { return parameters; }
    }
    
    /**
     * 统计摘要
     */
    class StatisticalSummary {
        private Long count;
        private Long nonNullCount;
        private Double mean;
        private Double median;
        private Double mode;
        private Double standardDeviation;
        private Double variance;
        private Double min;
        private Double max;
        private Double q1;
        private Double q3;
        private Double skewness;
        private Double kurtosis;
        
        public StatisticalSummary(Long count, Long nonNullCount, Double mean, Double median, 
                                Double mode, Double standardDeviation, Double variance,
                                Double min, Double max, Double q1, Double q3, 
                                Double skewness, Double kurtosis) {
            this.count = count;
            this.nonNullCount = nonNullCount;
            this.mean = mean;
            this.median = median;
            this.mode = mode;
            this.standardDeviation = standardDeviation;
            this.variance = variance;
            this.min = min;
            this.max = max;
            this.q1 = q1;
            this.q3 = q3;
            this.skewness = skewness;
            this.kurtosis = kurtosis;
        }
        
        // Getters
        public Long getCount() { return count; }
        public Long getNonNullCount() { return nonNullCount; }
        public Double getMean() { return mean; }
        public Double getMedian() { return median; }
        public Double getMode() { return mode; }
        public Double getStandardDeviation() { return standardDeviation; }
        public Double getVariance() { return variance; }
        public Double getMin() { return min; }
        public Double getMax() { return max; }
        public Double getQ1() { return q1; }
        public Double getQ3() { return q3; }
        public Double getSkewness() { return skewness; }
        public Double getKurtosis() { return kurtosis; }
        
        public double getCompleteness() {
            return count > 0 ? (double) nonNullCount / count : 0.0;
        }
    }
}