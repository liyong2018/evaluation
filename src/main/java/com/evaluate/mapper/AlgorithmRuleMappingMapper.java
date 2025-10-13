package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.AlgorithmRuleMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 算法规则映射Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface AlgorithmRuleMappingMapper extends BaseMapper<AlgorithmRuleMapping> {

    /**
     * 根据算法配置ID查询规则映射
     * 
     * @param algorithmConfigId 算法配置ID
     * @return 规则映射列表
     */
    @Select("SELECT * FROM algorithm_rule_mapping WHERE algorithm_config_id = #{algorithmConfigId} AND status = 1 ORDER BY mapping_order")
    List<AlgorithmRuleMapping> selectByAlgorithmConfigId(@Param("algorithmConfigId") Long algorithmConfigId);

    /**
     * 根据算法步骤ID查询规则映射
     * 
     * @param algorithmStepId 算法步骤ID
     * @return 规则映射列表
     */
    @Select("SELECT * FROM algorithm_rule_mapping WHERE algorithm_step_id = #{algorithmStepId} AND status = 1 ORDER BY mapping_order")
    List<AlgorithmRuleMapping> selectByAlgorithmStepId(@Param("algorithmStepId") Long algorithmStepId);

    /**
     * 根据动态规则ID查询规则映射
     * 
     * @param dynamicRuleId 动态规则ID
     * @return 规则映射列表
     */
    @Select("SELECT * FROM algorithm_rule_mapping WHERE dynamic_rule_id = #{dynamicRuleId} AND status = 1")
    List<AlgorithmRuleMapping> selectByDynamicRuleId(@Param("dynamicRuleId") Long dynamicRuleId);
}