package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估结果表
 *
 * @author admin
 * @since 2025-10-28
 */
@Data
@Accessors(chain = true)
@TableName("evaluation_result")
public class EvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String regionCode;

    private String regionName;

    private BigDecimal managementCapabilityScore;

    private BigDecimal supportCapabilityScore;

    private BigDecimal selfRescueCapabilityScore;

    private BigDecimal comprehensiveCapabilityScore;

    private String managementCapabilityLevel;

    private String supportCapabilityLevel;

    private String selfRescueCapabilityLevel;

    private String comprehensiveCapabilityLevel;

    private Long evaluationModelId;

    private String dataSource;

    private Long executionRecordId;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}