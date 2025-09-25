package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.FormulaConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公式配置Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface FormulaConfigMapper extends BaseMapper<FormulaConfig> {

    /**
     * 根据公式类型查询公式列表
     * 
     * @param formulaType 公式类型
     * @return 公式配置列表
     */
    List<FormulaConfig> selectByFormulaType(@Param("formulaType") String formulaType);

    /**
     * 根据公式类型查询默认公式
     * 
     * @param formulaType 公式类型
     * @return 默认公式配置
     */
    FormulaConfig selectDefaultByType(@Param("formulaType") String formulaType);

    /**
     * 根据公式名称查询
     * 
     * @param formulaName 公式名称
     * @return 公式配置
     */
    FormulaConfig selectByFormulaName(@Param("formulaName") String formulaName);

    /**
     * 查询启用状态的公式列表
     * 
     * @return 公式配置列表
     */
    List<FormulaConfig> selectEnabledConfigs();

    /**
     * 根据创建人查询公式列表
     * 
     * @param creator 创建人
     * @return 公式配置列表
     */
    List<FormulaConfig> selectByCreator(@Param("creator") String creator);

    /**
     * 更新默认公式状态
     * 
     * @param id 公式ID
     * @param isDefault 是否默认
     * @return 更新数量
     */
    int updateDefaultStatus(@Param("id") Long id, @Param("isDefault") Integer isDefault);

    /**
     * 根据公式类型清除默认状态
     * 
     * @param formulaType 公式类型
     * @return 更新数量
     */
    int clearDefaultStatusByType(@Param("formulaType") String formulaType);

    /**
     * 验证公式表达式
     * 
     * @param formulaExpression 公式表达式
     * @return 验证结果
     */
    int validateFormula(@Param("formulaExpression") String formulaExpression);

    /**
     * 批量插入公式配置
     * 
     * @param formulaList 公式列表
     * @return 插入数量
     */
    int batchInsert(@Param("formulaList") List<FormulaConfig> formulaList);

    /**
     * 根据算法步骤ID查询公式配置
     * 支持单个步骤ID或多个步骤ID（逗号分隔）
     * 
     * @param stepId 步骤ID
     * @return 公式配置列表
     */
    @Select("SELECT * FROM formula_config WHERE FIND_IN_SET(#{stepId}, algorithm_step_id) > 0 ORDER BY create_time DESC")
    List<FormulaConfig> selectByStepId(@Param("stepId") String stepId);

    /**
     * 根据算法步骤ID查询公式配置（精确匹配）
     * 
     * @param stepId 步骤ID
     * @return 公式配置列表
     */
    @Select("SELECT * FROM formula_config WHERE algorithm_step_id = #{stepId} ORDER BY create_time DESC")
    List<FormulaConfig> selectByExactStepId(@Param("stepId") String stepId);
}