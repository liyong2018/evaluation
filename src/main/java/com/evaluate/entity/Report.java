package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报告表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 一级指标结果ID
     */
    @TableField("primary_result_id")
    private Long primaryResultId;

    /**
     * 报告名称
     */
    @TableField("report_name")
    private String reportName;

    /**
     * 报告类型(PDF/WORD/MAP)
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 报告文件路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 专题图路径
     */
    @TableField("map_image_path")
    private String mapImagePath;

    /**
     * 生成时间
     */
    @TableField("generate_time")
    private LocalDateTime generateTime;
}