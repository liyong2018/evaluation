package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

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
     * 模型类型(GOVERNMENT/ENTERPRISE/SOCIAL_ORGANIZATION/FAMILY/COMMUNITY_DIRECT/COMMUNITY_TOWNSHIP/COMMUNITY_COUNTY_UNIT/TOWNSHIP_COUNTY_UNIT/LEGACY_COMPREHENSIVE/CITY_COMPREHENSIVE_2020/LEGACY_TOWNSHIP)
     */
    @TableField("model_type")
    private String modelType;

    /**
     * 数据源类型(government_table/enterprise_table/social_organization_table/family_table/community_table/survey_table/comprehensive_result)
     */
    @TableField("data_source_type")
    private String dataSourceType;

    /**
     * 聚合类型(direct_community/township_aggregation/county_aggregation/none)
     */
    @TableField("aggregation_type")
    private String aggregationType;

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
    private OffsetDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateTime;

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