package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 指标权重表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("indicator_weight")
public class IndicatorWeight implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权重配置ID
     */
    @TableField("config_id")
    private Long configId;

    /**
     * 指标代码
     */
    @TableField("indicator_code")
    private String indicatorCode;

    /**
     * 指标名称
     */
    @TableField("indicator_name")
    private String indicatorName;

    /**
     * 指标级别(1-一级指标，2-二级指标)
     */
    @TableField("indicator_level")
    private Integer indicatorLevel;

    /**
     * 权重值
     */
    @TableField("weight")
    private Double weight;

    /**
     * 父指标ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("min_value")
    private Double minValue;

    @TableField("max_value")
    private Double maxValue;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}