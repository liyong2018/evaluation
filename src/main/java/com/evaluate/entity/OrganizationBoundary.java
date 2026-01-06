package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织机构边界配置表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("organization_boundary")
public class OrganizationBoundary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 组织机构ID
     */
    @TableField("organization_id")
    private Long organizationId;

    /**
     * 年份
     */
    @TableField("year")
    private Integer year;

    /**
     * 边界坐标（GeoJSON或其他格式文本）
     */
    @TableField("boundary_coordinates")
    private String boundaryCoordinates;

    /**
     * 边界招标文件路径
     */
    @TableField("file_path")
    private String filePath;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
