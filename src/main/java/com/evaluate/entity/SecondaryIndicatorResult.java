package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 二级指标结果表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("secondary_indicator_result")
public class SecondaryIndicatorResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 调查数据ID
     */
    @TableField("survey_id")
    private Long surveyId;

    /**
     * 算法配置ID
     */
    @TableField("algorithm_id")
    private Long algorithmId;

    /**
     * 权重配置ID
     */
    @TableField("weight_config_id")
    private Long weightConfigId;

    /**
     * 二级指标代码
     */
    @TableField("indicator_code")
    private String indicatorCode;

    /**
     * 二级指标名称
     */
    @TableField("indicator_name")
    private String indicatorName;

    /**
     * 原始值
     */
    @TableField("original_value")
    private Double originalValue;

    /**
     * 归一化值
     */
    @TableField("normalized_value")
    private Double normalizedValue;

    /**
     * 权重值
     */
    @TableField("weight_value")
    private Double weightValue;

    /**
     * 加权值
     */
    @TableField("weighted_value")
    private Double weightedValue;

    /**
     * 计算过程数据(JSON格式)
     */
    @TableField("process_data")
    private String processData;

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

    /**
     * 是否删除(0-未删除，1-已删除)
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}