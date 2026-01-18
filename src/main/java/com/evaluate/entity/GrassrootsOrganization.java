package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基层组织机构表（乡镇和社区）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("grassroots_organization")
public class GrassrootsOrganization implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("county_id")
    private Long countyId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("level")
    private Integer level;

    @TableField("year")
    private Integer year;

    @TableField(value = "data_source", insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private String dataSource = "";  // 设置默认值避免数据库字段无默认值错误

    @TableField("province_name")
    private String provinceName;

    @TableField("city_name")
    private String cityName;

    @TableField("county_name")
    private String countyName;

    @TableField("township_name")
    private String townshipName;

    @TableField("community_name")
    private String communityName;

    @TableField("is_baseline")
    private Integer isBaseline;

    @TableField("baseline_code")
    private String baselineCode;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
