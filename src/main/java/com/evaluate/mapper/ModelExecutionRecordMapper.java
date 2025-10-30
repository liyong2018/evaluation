package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.ModelExecutionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型执行记录表 Mapper 接口
 *
 * @author admin
 * @since 2025-10-28
 */
@Mapper
public interface ModelExecutionRecordMapper extends BaseMapper<ModelExecutionRecord> {

    /**
     * 根据模型ID查询执行记录
     *
     * @param modelId 模型ID
     * @return 执行记录列表
     */
    List<ModelExecutionRecord> selectByModelId(@Param("modelId") Long modelId);

    /**
     * 根据执行状态查询执行记录
     *
     * @param executionStatus 执行状态
     * @return 执行记录列表
     */
    List<ModelExecutionRecord> selectByExecutionStatus(@Param("executionStatus") String executionStatus);

    /**
     * 根据执行代码查询执行记录
     *
     * @param executionCode 执行代码
     * @return 执行记录
     */
    ModelExecutionRecord selectByExecutionCode(@Param("executionCode") String executionCode);

    /**
     * 更新执行记录状态和结果信息
     *
     * @param id 记录ID
     * @param executionStatus 执行状态
     * @param resultSummary 结果摘要
     * @param resultIds 结果ID列表
     * @param resultCount 结果数量
     * @param errorMessage 错误信息
     * @return 更新的记录数
     */
    int updateExecutionResult(@Param("id") Long id,
                             @Param("executionStatus") String executionStatus,
                             @Param("resultSummary") String resultSummary,
                             @Param("resultIds") String resultIds,
                             @Param("resultCount") Integer resultCount,
                             @Param("errorMessage") String errorMessage);

    /**
     * 查询所有执行记录，按开始时间倒序
     *
     * @return 执行记录列表
     */
    List<ModelExecutionRecord> selectAllRecords();
}