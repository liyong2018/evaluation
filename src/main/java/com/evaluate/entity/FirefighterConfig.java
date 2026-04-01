package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消防员配置实体类
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("firefighter_config")
public class FirefighterConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行政区划代码
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 省名称
     */
    @TableField("province_name")
    private String provinceName;

    /**
     * 市名称
     */
    @TableField("city_name")
    private String cityName;

    /**
     * 县名称
     */
    @TableField("county_name")
    private String countyName;

    /**
     * 乡镇名称
     */
    @TableField("township_name")
    private String townshipName;

    /**
     * 消防员数量（人）
     */
    @TableField("firefighter_count")
    private Integer firefighterCount;

    /**
     * 状态（1-启用，0-禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}