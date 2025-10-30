package com.evaluate.dto.topsis;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TOPSIS算法配置数据模型
 * 
 * 包含TOPSIS算法执行所需的配置参数
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOPSISAlgorithmConfig {
    
    /**
     * 步骤ID
     */
    private Long stepId;
    
    /**
     * 算法代码
     */
    private String algorithmCode;
    
    /**
     * 指标列名列表（从ql_expression解析）
     */
    private List<String> indicators;
    
    /**
     * 输出参数名
     */
    private String outputParam;
    
    /**
     * 是否为正理想解距离计算
     * true=正理想解距离, false=负理想解距离
     */
    private boolean isPositiveDistance;
    
    /**
     * 原始ql_expression表达式
     */
    private String originalExpression;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 步骤代码
     */
    private String stepCode;
    
    /**
     * 算法类型（TOPSIS_POSITIVE 或 TOPSIS_NEGATIVE）
     */
    private String algorithmType;
    
    /**
     * 是否启用单区域特殊处理
     */
    private boolean enableSingleRegionHandling;
    
    /**
     * 单区域处理时的理论基准值比例
     */
    private double theoreticalBaselineRatio;
    
    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        return stepId != null && 
               algorithmCode != null && !algorithmCode.trim().isEmpty() &&
               indicators != null && !indicators.isEmpty() &&
               outputParam != null && !outputParam.trim().isEmpty();
    }
    
    /**
     * 获取指标数量
     * 
     * @return 指标数量
     */
    public int getIndicatorCount() {
        return indicators != null ? indicators.size() : 0;
    }
}