package com.evaluate.dto.topsis;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS计算指标数据模型
 * 
 * 记录TOPSIS计算过程中的关键指标和统计信息
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOPSISCalculationMetrics {
    
    /**
     * 总区域数量
     */
    private int totalRegions;
    
    /**
     * 有效指标数量
     */
    private int validIndicators;
    
    /**
     * 正理想解
     */
    private Map<String, Double> idealSolutions;
    
    /**
     * 负理想解
     */
    private Map<String, Double> antiIdealSolutions;
    
    /**
     * 距离统计信息
     */
    private Map<String, Double> distanceStatistics;
    
    /**
     * 数据质量问题
     */
    private List<String> dataQualityIssues;
    
    /**
     * 计算耗时（毫秒）
     */
    private Long calculationTimeMs;
    
    /**
     * 输入数据行数
     */
    private int inputDataRows;
    
    /**
     * 输入数据列数
     */
    private int inputDataColumns;
    
    /**
     * 零值数量
     */
    private int zeroValueCount;
    
    /**
     * NaN值数量
     */
    private int nanValueCount;
    
    /**
     * 无穷值数量
     */
    private int infiniteValueCount;
}