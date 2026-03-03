package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权重配置表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("weight_config")
public class WeightConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 组织机构编码（行政区划代码）
     */
    @TableField("orgcode")
    private String orgcode;

    @TableField("data_source")
    private String dataSource;

    /**
     * 关联的模型ID（用于替代 configName 进行模型关联）
     */
    @TableField("model_id")
    private Long modelId;

    @TableField("year")
    private Integer year;

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
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Integer isDeleted;

    /**
     * 实际数据来源组织机构编码（非持久化字段，用于显示数据继承关系）
     * 当当前区县没有自己的配置时，此字段记录配置实际所属的父级组织机构
     */
    @TableField(exist = false)
    private String actualOrgcode;

    /**
     * 实际数据来源组织机构名称（非持久化字段，用于显示）
     */
    @TableField(exist = false)
    private String actualOrgName;

    /**
     * 实际数据年份（非持久化字段，用于显示）
     * 当请求的年份数据不存在时，此字段记录实际使用的数据年份（如2020基准年）
     */
    @TableField(exist = false)
    private Integer actualYear;
}
