package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模型步骤实体类
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_step")
public class ModelStep {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模型ID
     */
    @TableField("model_id")
    private Long modelId;

    /**
     * 步骤名称
     */
    @TableField("step_name")
    private String stepName;

    /**
     * 步骤编码
     */
    @TableField("step_code")
    private String stepCode;

    /**
     * 执行顺序
     */
    @TableField("step_order")
    private Integer stepOrder;

    /**
     * 步骤类型(CALCULATION/NORMALIZATION/WEIGHTING/TOPSIS/GRADING)
     */
    @TableField("step_type")
    private String stepType;

    /**
     * 步骤描述
     */
    @TableField("description")
    private String description;

    /**
     * 输入变量(JSON格式)
     */
    @TableField("input_variables")
    private String inputVariables;

    /**
     * 输出变量(JSON格式)
     */
    @TableField("output_variables")
    private String outputVariables;

    /**
     * 依赖步骤ID(逗号分隔)
     */
    @TableField("depends_on")
    private String dependsOn;

    /**
     * 状态(1-启用,0-禁用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}