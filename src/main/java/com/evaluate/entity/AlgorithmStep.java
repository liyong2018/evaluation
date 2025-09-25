package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 算法步骤表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("algorithm_step")
public class AlgorithmStep implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 算法配置ID
     */
    @TableField("algorithm_config_id")
    private Long algorithmConfigId;

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
     * 步骤描述
     */
    @TableField("description")
    private String stepDescription;

    /**
     * 输入数据描述
     */
    @TableField("input_data")
    private String inputData;

    /**
     * 输出数据描述
     */
    @TableField("output_data")
    private String outputData;

    /**
     * 步骤顺序
     */
    @TableField("step_order")
    private Integer stepOrder;

    /**
     * 状态(0-禁用，1-启用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 关联的公式配置列表(非数据库字段)
     */
    @TableField(exist = false)
    private List<FormulaConfig> formulas;

}