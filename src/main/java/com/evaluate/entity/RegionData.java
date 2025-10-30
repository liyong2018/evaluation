package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("region_data")
public class RegionData {

    @TableId
    private Long id;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 地区级别：1-省份，2-城市，3-区县
     */
    private Integer level;

    /**
     * 父级地区编码
     */
    private String parentCode;

    /**
     * 父级地区名称
     */
    private String parentName;

    /**
     * 拼音（可选）
     */
    private String pinyin;

    /**
     * 经纬度（可选）
     */
    private String longitude;

    private String latitude;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}