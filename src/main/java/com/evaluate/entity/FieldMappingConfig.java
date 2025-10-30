package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字段映射配置实体类
 *
 * @author system
 * @since 2025-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("field_mapping_config")
public class FieldMappingConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评估模型ID
     */
    private Long modelId;

    /**
     * 算法输出字段名
     */
    private String algorithmOutputField;

    /**
     * 数据库字段名
     */
    private String databaseField;

    /**
     * 字段类型：SCORE-分数，LEVEL-等级
     */
    private String fieldType;

    /**
     * 字段分类：MANAGEMENT-管理能力，SUPPORT-支持能力，SELF_RESCUE-自救能力，COMPREHENSIVE-综合能力
     */
    private String fieldCategory;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 是否启用：1-启用，0-禁用
     */
    private Boolean isActive;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除：1-已删除，0-未删除
     */
    private Boolean isDeleted;
}