package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模型执行记录表
 *
 * @author admin
 * @since 2025-10-28
 */
@Data
@Accessors(chain = true)
@TableName("model_execution_record")
public class ModelExecutionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long modelId;

    private String executionCode;

    private String regionIds;

    private Long weightConfigId;

    private String executionStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    private String resultSummary;

    private String resultIds;

    @TableField(exist = false)
    private Integer resultCount;

    private String createBy;
}