package com.evaluate.dto.topsis;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 指标元数据
 * 
 * 包含指标的详细信息，用于TOPSIS配置管理
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorMetadata {
    
    /**
     * 指标列名
     */
    private String columnName;
    
    /**
     * 指标显示名称
     */
    private String displayName;
    
    /**
     * 指标描述
     */
    private String description;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 指标类别（primary/secondary）
     */
    private String category;
    
    /**
     * 是否为数值型指标
     */
    private boolean isNumeric;
    
    /**
     * 是否推荐用于TOPSIS计算
     */
    private boolean recommended;
    
    /**
     * 数据完整性（0-1之间的值）
     */
    private double completeness;
    
    /**
     * 数据质量评分（0-1之间的值）
     */
    private double qualityScore;
    
    /**
     * 最小值
     */
    private Double minValue;
    
    /**
     * 最大值
     */
    private Double maxValue;
    
    /**
     * 平均值
     */
    private Double avgValue;
    
    /**
     * 标准差
     */
    private Double stdDev;
    
    /**
     * 样本数量
     */
    private Long sampleCount;
    
    /**
     * 非空值数量
     */
    private Long nonNullCount;
    
    /**
     * 指标权重（如果有）
     */
    private Double weight;
    
    /**
     * 是否为效益型指标（true=效益型，false=成本型）
     */
    private boolean isBenefitType;
    
    /**
     * 指标单位
     */
    private String unit;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 计算完整性百分比
     */
    public double getCompletenessPercentage() {
        return completeness * 100;
    }
    
    /**
     * 计算质量评分百分比
     */
    public double getQualityScorePercentage() {
        return qualityScore * 100;
    }
    
    /**
     * 判断指标是否适合用于TOPSIS计算
     */
    public boolean isSuitableForTOPSIS() {
        return isNumeric && completeness >= 0.8 && qualityScore >= 0.7;
    }
    
    /**
     * 获取指标的风险等级
     */
    public String getRiskLevel() {
        if (completeness < 0.5 || qualityScore < 0.5) {
            return "HIGH";
        } else if (completeness < 0.8 || qualityScore < 0.7) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}