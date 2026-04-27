package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模型执行策略配置实体类
 * 替代 resolveEffectiveRegionCodes / resolveRegionDataValidationError 中的硬编码策略
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_execution_strategy")
public class ModelExecutionStrategy {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("model_id")
    private Long modelId;

    @TableField("strategy_type")
    private String strategyType;

    @TableField("strategy_key")
    private String strategyKey;

    @TableField("strategy_value")
    private String strategyValue;

    @TableField("error_message")
    private String errorMessage;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
