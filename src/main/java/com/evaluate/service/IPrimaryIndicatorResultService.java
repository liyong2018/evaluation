package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.PrimaryIndicatorResult;

import java.util.List;

/**
 * 一级指标结果服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface IPrimaryIndicatorResultService extends IService<PrimaryIndicatorResult> {

    /**
     * 根据调查ID查询一级指标结果
     * 
     * @param surveyId 调查ID
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> getBySurveyId(Long surveyId);

    /**
     * 根据调查ID和算法ID查询一级指标结果
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> getBySurveyIdAndAlgorithmId(Long surveyId, Long algorithmId);

    /**
     * 根据指标代码查询结果
     * 
     * @param indicatorCode 指标代码
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> getByIndicatorCode(String indicatorCode);

    /**
     * 根据条件查询结果
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> getByConditions(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 批量保存一级指标结果
     * 
     * @param resultList 结果列表
     * @return 保存结果
     */
    boolean batchSave(List<PrimaryIndicatorResult> resultList);

    /**
     * 批量更新一级指标结果
     * 
     * @param resultList 结果列表
     * @return 更新结果
     */
    boolean batchUpdate(List<PrimaryIndicatorResult> resultList);

    /**
     * 根据调查ID删除结果
     * 
     * @param surveyId 调查ID
     * @return 删除结果
     */
    boolean deleteBySurveyId(Long surveyId);

    /**
     * 根据条件删除结果
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 删除结果
     */
    boolean deleteByConditions(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 统计一级指标结果数量
     * 
     * @param surveyId 调查ID
     * @return 结果数量
     */
    int countBySurveyId(Long surveyId);

    /**
     * 计算综合得分
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 综合得分
     */
    Double calculateTotalScore(Long surveyId, Long algorithmId, Long weightConfigId);
}