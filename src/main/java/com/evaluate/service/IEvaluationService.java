package com.evaluate.service;

import com.evaluate.entity.PrimaryIndicatorResult;
import com.evaluate.entity.SecondaryIndicatorResult;
import com.evaluate.entity.Report;

import java.util.List;
import java.util.Map;

/**
 * 评估计算服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface IEvaluationService {

    /**
     * 执行完整的评估计算
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 计算结果
     */
    Map<String, Object> performEvaluation(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 计算二级指标结果
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 二级指标结果列表
     */
    List<SecondaryIndicatorResult> calculateSecondaryIndicators(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 计算一级指标结果
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @param secondaryResults 二级指标结果
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> calculatePrimaryIndicators(Long surveyId, Long algorithmId, Long weightConfigId, List<SecondaryIndicatorResult> secondaryResults);

    /**
     * 生成评估报告
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @param primaryResults 一级指标结果
     * @param generator 生成人
     * @return 评估报告
     */
    Report generateEvaluationReport(Long surveyId, Long algorithmId, Long weightConfigId, List<PrimaryIndicatorResult> primaryResults, String generator);

    /**
     * 数据归一化处理
     * 
     * @param originalValues 原始数据值
     * @param formulaId 归一化公式ID
     * @return 归一化后的值
     */
    List<Double> normalizeData(List<Double> originalValues, Long formulaId);

    /**
     * 计算综合得分
     * 
     * @param primaryResults 一级指标结果
     * @return 综合得分
     */
    Double calculateTotalScore(List<PrimaryIndicatorResult> primaryResults);

    /**
     * 确定评估等级
     * 
     * @param totalScore 综合得分
     * @return 评估等级
     */
    String determineEvaluationGrade(Double totalScore);

    /**
     * 获取算法执行过程数据
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 过程数据
     */
    Map<String, Object> getAlgorithmProcessData(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 验证评估参数
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 验证结果
     */
    boolean validateEvaluationParams(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 重新计算评估结果
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 重新计算结果
     */
    Map<String, Object> recalculateEvaluation(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 批量评估计算
     * 
     * @param surveyIds 调查数据ID列表
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 批量计算结果
     */
    List<Map<String, Object>> batchEvaluation(List<Long> surveyIds, Long algorithmId, Long weightConfigId);

    /**
     * 对比评估结果
     * 
     * @param surveyIds 调查数据ID列表
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 对比结果
     */
    Map<String, Object> compareEvaluationResults(List<Long> surveyIds, Long algorithmId, Long weightConfigId);

    /**
     * 获取评估历史记录
     * 
     * @param surveyId 调查数据ID
     * @return 历史记录
     */
    List<Map<String, Object>> getEvaluationHistory(Long surveyId);

    /**
     * 删除评估结果
     * 
     * @param surveyId 调查数据ID
     * @param algorithmId 算法配置ID
     * @param weightConfigId 权重配置ID
     * @return 删除结果
     */
    boolean deleteEvaluationResults(Long surveyId, Long algorithmId, Long weightConfigId);
}