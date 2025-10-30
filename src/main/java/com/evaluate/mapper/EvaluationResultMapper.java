package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.EvaluationResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评估结果表 Mapper 接口
 *
 * @author admin
 * @since 2025-10-28
 */
@Mapper
public interface EvaluationResultMapper extends BaseMapper<EvaluationResult> {

    /**
     * 根据执行记录ID查询评估结果
     *
     * @param executionRecordId 执行记录ID
     * @return 评估结果列表
     */
    List<EvaluationResult> selectByExecutionRecordId(@Param("executionRecordId") Long executionRecordId);

    /**
     * 根据模型ID和数据源查询评估结果
     *
     * @param modelId 模型ID
     * @param dataSource 数据源
     * @return 评估结果列表
     */
    List<EvaluationResult> selectByModelIdAndDataSource(@Param("modelId") Long modelId,
                                                      @Param("dataSource") String dataSource);

    /**
     * 根据地区代码查询评估结果
     *
     * @param regionCode 地区代码
     * @return 评估结果列表
     */
    List<EvaluationResult> selectByRegionCode(@Param("regionCode") String regionCode);

    /**
     * 批量插入评估结果
     *
     * @param evaluationResults 评估结果列表
     * @return 插入的记录数
     */
    int insertBatch(@Param("evaluationResults") List<EvaluationResult> evaluationResults);
}