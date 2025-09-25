package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公式配置表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("formula_config")
public class FormulaConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公式名称
     */
    @TableField("formula_name")
    private String formulaName;

    /**
     * 算法步骤ID（支持多个，用逗号分隔）
     */
    @TableField("algorithm_step_id")
    private String algorithmStepId;

    /**
     * 公式表达式
     */
    @TableField("formula_expression")
    private String formulaExpression;

    /**
     * 输入变量列表(JSON格式)
     */
    @TableField("input_variables")
    private String inputVariables;

    /**
     * 输出变量名
     */
    @TableField("output_variable")
    private String outputVariable;

    /**
     * 公式描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态（1-启用，0-禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}