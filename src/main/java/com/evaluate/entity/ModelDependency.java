package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模型前置依赖配置实体类
 * 替代 resolveCityComprehensiveSourceModelIds() 中的硬编码前置模型绑定
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_dependency")
public class ModelDependency {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("model_id")
    private Long modelId;

    @TableField("dependency_key")
    private String dependencyKey;

    @TableField("dependency_model_id")
    private Long dependencyModelId;

    @TableField("keyword_match")
    private String keywordMatch;

    @TableField("reuse_dependency_key")
    private String reuseDependencyKey;

    @TableField("region_code_strategy")
    private String regionCodeStrategy;

    @TableField("fallback_model_id")
    private Long fallbackModelId;

    @TableField("data_table_name")
    private String dataTableName;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
