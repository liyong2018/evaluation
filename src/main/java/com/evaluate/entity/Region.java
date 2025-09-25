package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地区组织机构实体类
 */
@Data
@TableName("region")
public class Region {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 地区代码
     */
    private String code;
    
    /**
     * 地区名称
     */
    private String name;
    
    /**
     * 父级地区ID
     */
    private Long parentId;
    
    /**
     * 地区级别（1-省，2-市，3-县，4-镇）
     */
    private Integer level;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态（1-启用，0-禁用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 子级地区列表（用于树形结构）
     */
    @TableField(exist = false)
    private List<Region> children;
}