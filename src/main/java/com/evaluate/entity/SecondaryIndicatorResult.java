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
    @TableField("survey_data_id")
    private Long surveyDataId;

    /**
     * 配置ID
     */
    @TableField("config_id")
    private Long configId;

    // 二级指标原始计算值
    @TableField("management_capability")
    private Double managementCapability;

    @TableField("risk_assessment_capability")
    private Double riskAssessmentCapability;

    @TableField("funding_capability")
    private Double fundingCapability;

    @TableField("material_capability")
    private Double materialCapability;

    @TableField("medical_capability")
    private Double medicalCapability;

    @TableField("self_rescue_capability")
    private Double selfRescueCapability;

    @TableField("public_avoidance_capability")
    private Double publicAvoidanceCapability;

    @TableField("relocation_capability")
    private Double relocationCapability;

    // 二级指标归一化值
    @TableField("management_normalized")
    private Double managementNormalized;

    @TableField("risk_assessment_normalized")
    private Double riskAssessmentNormalized;

    @TableField("funding_normalized")
    private Double fundingNormalized;

    @TableField("material_normalized")
    private Double materialNormalized;

    @TableField("medical_normalized")
    private Double medicalNormalized;

    @TableField("self_rescue_normalized")
    private Double selfRescueNormalized;

    @TableField("public_avoidance_normalized")
    private Double publicAvoidanceNormalized;

    @TableField("relocation_normalized")
    private Double relocationNormalized;

    /**
     * 计算时间
     */
    @TableField("calculate_time")
    private LocalDateTime calculateTime;
}