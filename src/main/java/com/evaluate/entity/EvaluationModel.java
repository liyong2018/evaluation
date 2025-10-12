package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评估模型实体类
 * 
 * @author System
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("evaluation_model")
public class EvaluationModel {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 模型编码
     */
    @TableField("model_code")
    private String modelCode;

    /**
     * 模型描述
     */
    @TableField("description")
    private String description;

    /**
     * 模型版本
     */
    @TableField("version")
    private String version;

    /**
     * 状态(1-启用,0-禁用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否默认模型
     */
    @TableField("is_default")
    private Boolean isDefault;

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
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;
}