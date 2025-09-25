package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 一级指标结果表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("primary_indicator_result")
public class PrimaryIndicatorResult implements Serializable {

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
     * 一级指标代码
     */
    @TableField("indicator_code")
    private String indicatorCode;

    /**
     * 一级指标名称
     */
    @TableField("indicator_name")
    private String indicatorName;

    /**
     * 计算值
     */
    @TableField("calculated_value")
    private Double calculatedValue;

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