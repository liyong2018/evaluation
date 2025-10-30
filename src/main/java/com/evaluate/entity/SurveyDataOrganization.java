package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调查数据组织机构表
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("survey_data")
public class SurveyDataOrganization implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行政区代码 - 用作组织机构代码
     */
    @TableField("region_code")
    private String code;

    /**
     * 机构名称 - 根据不同级别使用不同字段
     */
    @TableField(exist = false)
    private String name;

    /**
     * 省名称
     */
    @TableField("province")
    private String province;

    /**
     * 市名称
     */
    @TableField("city")
    private String city;

    /**
     * 县名称
     */
    @TableField("county")
    private String county;

    /**
     * 乡镇名称
     */
    @TableField("township")
    private String township;

    /**
     * 机构级别 (1-省, 2-市, 3-县, 4-乡镇)
     */
    @TableField(exist = false)
    private Integer level;

    /**
     * 父级代码
     */
    @TableField(exist = false)
    private String parentCode;

    /**
     * 排序字段
     */
    @TableField(exist = false)
    private Integer sort;

    /**
     * 状态 (1-启用, 0-禁用)
     */
    @TableField(exist = false)
    private Integer status;

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
}