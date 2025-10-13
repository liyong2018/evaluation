package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.DynamicRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 动态规则Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface DynamicRuleMapper extends BaseMapper<DynamicRule> {

    /**
     * 根据规则类型查询规则列表
     * 
     * @param ruleType 规则类型
     * @return 规则列表
     */
    @Select("SELECT * FROM dynamic_rule WHERE rule_type = #{ruleType} AND status = 1 ORDER BY execution_order")
    List<DynamicRule> selectByRuleType(@Param("ruleType") String ruleType);

    /**
     * 根据规则编码查询规则
     * 
     * @param ruleCode 规则编码
     * @return 规则信息
     */
    @Select("SELECT * FROM dynamic_rule WHERE rule_code = #{ruleCode} AND status = 1")
    DynamicRule selectByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 查询启用的规则列表
     * 
     * @return 启用的规则列表
     */
    @Select("SELECT * FROM dynamic_rule WHERE status = 1 ORDER BY execution_order")
    List<DynamicRule> selectEnabledRules();

    /**
     * 根据算法步骤ID查询关联的规则
     * 
     * @param algorithmStepId 算法步骤ID
     * @return 规则列表
     */
    @Select("SELECT dr.* FROM dynamic_rule dr " +
            "INNER JOIN algorithm_rule_mapping arm ON dr.id = arm.dynamic_rule_id " +
            "WHERE arm.algorithm_step_id = #{algorithmStepId} AND dr.status = 1 AND arm.status = 1 " +
            "ORDER BY arm.mapping_order")
    List<DynamicRule> selectByAlgorithmStepId(@Param("algorithmStepId") Long algorithmStepId);
}