package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织机构表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("organization")
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    @TableField("data_source")
    private String dataSource;

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

    @com.baomidou.mybatisplus.annotation.TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Integer isDeleted;
}
