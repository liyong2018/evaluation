package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISDiagnosticReport;
import com.evaluate.dto.topsis.TOPSISRepairResult;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS诊断服务接口
 * 
 * 负责诊断TOPSIS计算问题并提供修复建议
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISDiagnosticService {
    
    /**
     * 诊断TOPSIS计算问题
     * 
     * @param modelId 模型ID
     * @param regionCodes 区域代码列表
     * @param weightConfigId 权重配置ID
     * @return 诊断报告
     */
    TOPSISDiagnosticReport diagnose(Long modelId, List<String> regionCodes, Long weightConfigId);
    
    /**
     * 诊断定权数据的TOPSIS计算问题
     * 
     * @param weightedData 定权数据
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 诊断报告
     */
    TOPSISDiagnosticReport diagnoseWeightedData(
        Map<String, Map<String, Double>> weightedData, 
        Long modelId, 
        String stepCode
    );
    
    /**
     * 修复TOPSIS计算问题
     * 
     * @param diagnosticReport 诊断报告
     * @return 修复结果
     */
    TOPSISRepairResult repair(TOPSISDiagnosticReport diagnosticReport);
    
    /**
     * 对比不同模型的TOPSIS配置
     * 
     * @param modelId1 模型1 ID
     * @param modelId2 模型2 ID
     * @param stepCode 步骤代码
     * @return 配置差异报告
     */
    Map<String, Object> compareConfigurations(Long modelId1, Long modelId2, String stepCode);
    
    /**
     * 验证TOPSIS输入数据质量
     * 
     * @param weightedData 定权数据
     * @return 数据质量报告
     */
    Map<String, Object> validateInputData(Map<String, Map<String, Double>> weightedData);
    
    /**
     * 生成TOPSIS计算过程日志
     * 
     * @param weightedData 定权数据
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 计算过程日志
     */
    Map<String, Object> generateCalculationLog(
        Map<String, Map<String, Double>> weightedData,
        Long modelId,
        String stepCode
    );
}