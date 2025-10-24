package com.evaluate.dto.topsis;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS诊断报告数据模型
 * 
 * 包含TOPSIS计算问题的诊断信息和修复建议
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOPSISDiagnosticReport {
    
    /**
     * 是否存在问题
     */
    private boolean hasIssues;
    
    /**
     * 问题列表
     */
    private List<String> issues;
    
    /**
     * 输入数据摘要
     */
    private Map<String, Object> inputDataSummary;
    
    /**
     * 计算详情
     */
    private Map<String, Object> calculationDetails;
    
    /**
     * 修复建议
     */
    private List<String> recommendations;
    
    /**
     * 计算指标
     */
    private TOPSISCalculationMetrics metrics;
    
    /**
     * 诊断时间戳
     */
    private Long timestamp;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 步骤代码
     */
    private String stepCode;
    
    /**
     * 区域代码列表
     */
    private List<String> regionCodes;
}