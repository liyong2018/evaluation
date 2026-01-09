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
     * 执行评估模型（同步）
     *
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param year 评估年份
     * @param orgCode 机构代码
     * @param createBy 操作人
     * @return 执行结果（包含每个步骤的输出）
     */
    Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId, Integer year, String orgCode, String createBy);

    /**
     * 异步执行评估模型
     * 立即返回执行记录ID，实际计算在后台进行
     *
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param year 评估年份
     * @param orgCode 机构代码
     * @param createBy 操作人
     * @return 执行记录ID
     */
    Long executeModelAsync(Long modelId, List<String> regionCodes, Long weightConfigId, Integer year, String orgCode, String createBy);

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
    Map<String, Object> executeAlgorithmStep(Long algorithmId, Integer stepOrder, List<String> regionCodes, Long weightConfigId, Integer year);

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
    Map<String, Object> executeAlgorithmStepsUpTo(Long algorithmId, Integer upToStepOrder, List<String> regionCodes, Long weightConfigId, Integer year);

    /**
     * 获取执行记录详情
     *
     * @param executionRecordId 执行记录ID
     * @return 执行记录详情
     */
    Map<String, Object> getExecutionRecordDetail(Long executionRecordId);

    /**
     * 获取评估历史列表（分页）
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param modelId 模型ID（可选）
     * @param executionStatus 执行状态（可选）
     * @return 分页的评估历史列表
     */
    Map<String, Object> getEvaluationHistoryList(Integer page, Integer size, Long modelId, String executionStatus);

    /**
     * 获取评估历史列表（分页，支持更多筛选条件）
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param modelId 模型ID（可选）
     * @param executionStatus 执行状态（可选）
     * @param year 评估年份（可选）
     * @param county 区县名称（可选）
     * @return 分页的评估历史列表
     */
    Map<String, Object> getEvaluationHistoryList(Integer page, Integer size, Long modelId, String executionStatus, Integer year, String county);

    /**
     * 检查评估数据是否存在
     *
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param year 评估年份
     * @param orgCode 机构代码
     * @return 检查结果，包含是否存在和错误信息
     */
    Map<String, Object> checkEvaluationData(Long modelId, List<String> regionCodes, Integer year, String orgCode);

    /**
     * 删除评估历史记录（同时删除关联的 evaluation_result 记录）
     *
     * @param executionRecordId 执行记录ID
     * @return 是否删除成功
     */
    boolean deleteEvaluationHistory(Long executionRecordId);
}
