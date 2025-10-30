package com.evaluate.service;

import com.evaluate.entity.EvaluationResult;
import com.evaluate.entity.ModelExecutionRecord;

import java.util.List;

/**
 * 评估结果服务接口
 *
 * @author admin
 * @since 2025-10-28
 */
public interface EvaluationResultService {

    /**
     * 保存评估结果
     *
     * @param evaluationResults 评估结果列表
     * @param executionRecordId 执行记录ID
     * @param createBy 创建人
     * @return 保存的评估结果ID列表
     */
    List<Long> saveEvaluationResults(List<EvaluationResult> evaluationResults, Long executionRecordId, String createBy);

    /**
     * 根据执行记录ID查询评估结果
     *
     * @param executionRecordId 执行记录ID
     * @return 评估结果列表
     */
    List<EvaluationResult> getResultsByExecutionRecordId(Long executionRecordId);

    /**
     * 根据模型ID和数据源查询评估结果
     *
     * @param modelId 模型ID
     * @param dataSource 数据源
     * @return 评估结果列表
     */
    List<EvaluationResult> getResultsByModelIdAndDataSource(Long modelId, String dataSource);

    /**
     * 根据地区代码查询评估结果
     *
     * @param regionCode 地区代码
     * @return 评估结果列表
     */
    List<EvaluationResult> getResultsByRegionCode(String regionCode);

    /**
     * 创建模型执行记录
     *
     * @param modelId 模型ID
     * @param regionIds 地区ID列表
     * @param weightConfigId 权重配置ID
     * @param createBy 创建人
     * @return 执行记录
     */
    ModelExecutionRecord createExecutionRecord(Long modelId, List<String> regionIds, Long weightConfigId, String createBy);

    /**
     * 更新执行记录状态
     *
     * @param executionRecordId 执行记录ID
     * @param executionStatus 执行状态
     * @param resultSummary 结果摘要
     * @param resultIds 结果ID列表
     * @param errorMessage 错误信息
     */
    void updateExecutionRecord(Long executionRecordId, String executionStatus, String resultSummary, List<Long> resultIds, String errorMessage);

    /**
     * 根据执行代码查询执行记录
     *
     * @param executionCode 执行代码
     * @return 执行记录
     */
    ModelExecutionRecord getExecutionRecordByCode(String executionCode);

    /**
     * 获取所有执行记录
     *
     * @return 执行记录列表
     */
    List<ModelExecutionRecord> getAllExecutionRecords();

    /**
     * 根据执行记录ID获取评估结果列表
     *
     * @param executionRecordId 执行记录ID
     * @return 评估结果列表
     */
    List<EvaluationResult> getEvaluationResultsByExecutionId(Long executionRecordId);

    /**
     * 获取所有评估结果
     *
     * @return 所有评估结果列表
     */
    List<EvaluationResult> getAllEvaluationResults();
}