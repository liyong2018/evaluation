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
     * 评估记录ID
     */
    @TableField("evaluation_id")
    private Long evaluationId;

    /**
     * 二级指标结果ID
     */
    @TableField("secondary_result_id")
    private Long secondaryResultId;

    /**
     * 灾害管理能力
     */
    @TableField("level1_management")
    private Double level1Management;

    /**
     * 灾害备灾能力
     */
    @TableField("level1_preparation")
    private Double level1Preparation;

    /**
     * 自救转移能力
     */
    @TableField("level1_self_rescue")
    private Double level1SelfRescue;

    /**
     * 灾害管理能力分级
     */
    @TableField("management_grade")
    private String managementGrade;

    /**
     * 灾害备灾能力分级
     */
    @TableField("preparation_grade")
    private String preparationGrade;

    /**
     * 自救转移能力分级
     */
    @TableField("self_rescue_grade")
    private String selfRescueGrade;

    /**
     * 综合减灾能力数值
     */
    @TableField("overall_capability")
    private Double overallCapability;

    /**
     * 综合减灾能力分级
     */
    @TableField("overall_grade")
    private String overallGrade;

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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}