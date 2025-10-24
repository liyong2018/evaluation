package com.evaluate.dto.topsis;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS修复结果数据模型
 * 
 * 记录TOPSIS计算问题的修复操作结果
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOPSISRepairResult {
    
    /**
     * 修复是否成功
     */
    private boolean success;
    
    /**
     * 修复操作列表
     */
    private List<String> repairActions;
    
    /**
     * 修复后的数据
     */
    private Map<String, Map<String, Double>> repairedData;
    
    /**
     * 修复前后对比
     */
    private Map<String, Object> beforeAfterComparison;
    
    /**
     * 修复消息
     */
    private String message;
    
    /**
     * 修复时间戳
     */
    private Long timestamp;
    
    /**
     * 仍存在的问题
     */
    private List<String> remainingIssues;
}