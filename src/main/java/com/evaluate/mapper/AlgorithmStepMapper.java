package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.AlgorithmStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 算法步骤Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface AlgorithmStepMapper extends BaseMapper<AlgorithmStep> {

    /**
     * 根据算法ID查询步骤列表
     * 
     * @param algorithmId 算法ID
     * @return 算法步骤列表
     */
    @Select("SELECT id, algorithm_config_id, step_name, step_code, description, input_data, output_data, step_order, status, create_time FROM algorithm_step WHERE algorithm_config_id = #{algorithmId} ORDER BY step_order ASC")
    List<AlgorithmStep> selectByAlgorithmId(@Param("algorithmId") Long algorithmId);

    /**
     * 根据算法ID查询启用的步骤列表
     * 
     * @param algorithmId 算法ID
     * @return 启用的算法步骤列表
     */
    @Select("SELECT id, algorithm_config_id, step_name, step_code, description, input_data, output_data, step_order, status, create_time FROM algorithm_step WHERE algorithm_config_id = #{algorithmId} AND status = 1 ORDER BY step_order ASC")
    List<AlgorithmStep> selectEnabledByAlgorithmId(@Param("algorithmId") Long algorithmId);

    /**
     * 批量插入算法步骤
     * 
     * @param stepList 步骤列表
     * @return 插入数量
     */
    int batchInsert(@Param("stepList") List<AlgorithmStep> stepList);

    /**
     * 根据算法ID删除所有步骤
     * 
     * @param algorithmId 算法ID
     * @return 删除数量
     */
    int deleteByAlgorithmId(@Param("algorithmId") Long algorithmId);

    /**
     * 更新步骤顺序
     * 
     * @param id 步骤ID
     * @param stepOrder 步骤顺序
     * @return 更新数量
     */
    int updateStepOrder(@Param("id") Long id, @Param("stepOrder") Integer stepOrder);

    /**
     * 批量更新步骤启用状态
     * 
     * @param stepList 步骤列表
     * @return 更新数量
     */
    int batchUpdateEnabled(@Param("stepList") List<AlgorithmStep> stepList);
}