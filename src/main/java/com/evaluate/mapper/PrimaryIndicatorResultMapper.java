package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.PrimaryIndicatorResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 一级指标结果Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface PrimaryIndicatorResultMapper extends BaseMapper<PrimaryIndicatorResult> {

    /**
     * 根据调查ID查询一级指标结果
     * 
     * @param surveyId 调查ID
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> selectBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 根据调查ID和算法ID查询一级指标结果
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> selectBySurveyIdAndAlgorithmId(@Param("surveyId") Long surveyId, @Param("algorithmId") Long algorithmId);

    /**
     * 根据指标代码查询结果
     * 
     * @param indicatorCode 指标代码
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> selectByIndicatorCode(@Param("indicatorCode") String indicatorCode);

    /**
     * 根据调查ID、算法ID和权重配置ID查询结果
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 一级指标结果列表
     */
    List<PrimaryIndicatorResult> selectByConditions(@Param("surveyId") Long surveyId, 
                                                    @Param("algorithmId") Long algorithmId, 
                                                    @Param("weightConfigId") Long weightConfigId);

    /**
     * 批量插入一级指标结果
     * 
     * @param resultList 结果列表
     * @return 插入数量
     */
    int batchInsert(@Param("resultList") List<PrimaryIndicatorResult> resultList);

    /**
     * 根据调查ID删除结果
     * 
     * @param surveyId 调查ID
     * @return 删除数量
     */
    int deleteBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 根据条件删除结果
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 删除数量
     */
    int deleteByConditions(@Param("surveyId") Long surveyId, 
                          @Param("algorithmId") Long algorithmId, 
                          @Param("weightConfigId") Long weightConfigId);

    /**
     * 统计一级指标结果数量
     * 
     * @param surveyId 调查ID
     * @return 结果数量
     */
    int countBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 批量更新一级指标结果
     * 
     * @param resultList 结果列表
     * @return 更新数量
     */
    int batchUpdate(@Param("resultList") List<PrimaryIndicatorResult> resultList);

    /**
     * 计算综合得分
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 综合得分
     */
    Double calculateTotalScore(@Param("surveyId") Long surveyId, 
                              @Param("algorithmId") Long algorithmId, 
                              @Param("weightConfigId") Long weightConfigId);
}