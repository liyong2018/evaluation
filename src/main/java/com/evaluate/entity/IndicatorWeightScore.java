package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专家权重打分记录表
 * 用于存储多个专家对权重配置的打分记录
 *
 * @author System
 * @since 2024-11-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("indicator_weight_score")
public class IndicatorWeightScore implements Serializable {

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
     * 组织机构编码（行政区划代码）
     */
    @TableField("orgcode")
    private String orgcode;

    /**
     * 指标代码
     */
    @TableField("indicator_code")
    private String indicatorCode;

    /**
     * 专家建议的权重值（0-1之间）
     */
    @TableField("weight")
    private Double weight;

    /**
     * 专家姓名
     */
    @TableField("expert_name")
    private String expertName;

    /**
     * 专家电话
     */
    @TableField("expert_phone")
    private String expertPhone;

    /**
     * 打分时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
