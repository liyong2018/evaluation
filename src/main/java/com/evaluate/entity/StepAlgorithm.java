package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 步骤算法实体类
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("step_algorithm")
public class StepAlgorithm {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 步骤ID
     */
    @TableField("step_id")
    private Long stepId;

    /**
     * 算法名称
     */
    @TableField("algorithm_name")
    private String algorithmName;

    /**
     * 算法编码
     */
    @TableField("algorithm_code")
    private String algorithmCode;

    /**
     * 算法执行顺序
     */
    @TableField("algorithm_order")
    private Integer algorithmOrder;

    /**
     * QLExpress表达式
     */
    @TableField("ql_expression")
    private String qlExpression;

    /**
     * 输入参数定义(JSON格式)
     */
    @TableField("input_params")
    private String inputParams;

    /**
     * 输出参数名
     */
    @TableField("output_param")
    private String outputParam;

    /**
     * 算法描述
     */
    @TableField("description")
    private String description;

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