package com.evaluate.service;

import java.util.List;
import java.util.Map;

/**
 * 模型执行服务接口
 * 负责按步骤执行QLExpress表达式并生成评估结果
 * 
 * @author System
 * @since 2025-01-01
 */
public interface ModelExecutionService {

    /**
     * 执行评估模型
     * 
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 执行结果（包含每个步骤的输出）
     */
    Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId);

    /**
     * 执行单个步骤
     * 
     * @param stepId 步骤ID
     * @param regionCodes 地区代码列表
     * @param inputData 输入数据
     * @return 步骤执行结果
     */
    Map<String, Object> executeStep(Long stepId, List<String> regionCodes, Map<String, Object> inputData);

    /**
     * 生成结果二维表
     * 
     * @param executionResults 执行结果
     * @return 二维表数据
     */
    List<Map<String, Object>> generateResultTable(Map<String, Object> executionResults);

    /**
     * 执行算法的单个步骤并返回2D表格结果
     * 
     * @param algorithmId 算法ID
     * @param stepOrder 步骤顺序（从1开始）
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 步骤执行结果，包含2D表格数据
     */
    Map<String, Object> executeAlgorithmStep(Long algorithmId, Integer stepOrder, List<String> regionCodes, Long weightConfigId);

    /**
     * 获取算法所有步骤的基本信息
     * 
     * @param algorithmId 算法ID
     * @return 算法步骤列表信息
     */
    Map<String, Object> getAlgorithmStepsInfo(Long algorithmId);

    /**
     * 批量执行算法步骤（直到指定步骤）
     * 
     * @param algorithmId 算法ID
     * @param upToStepOrder 执行到第几步（包含该步骤）
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 所有已执行步骤的结果
     */
    Map<String, Object> executeAlgorithmStepsUpTo(Long algorithmId, Integer upToStepOrder, List<String> regionCodes, Long weightConfigId);
}