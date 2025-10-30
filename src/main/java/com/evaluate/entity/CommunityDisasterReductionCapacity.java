package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社区行政村减灾能力实体类
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_disaster_reduction_capacity")
public class CommunityDisasterReductionCapacity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行政区代码
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
     * 社区（行政村）名称
     */
    @TableField("community_name")
    private String communityName;

    /**
     * 是否有社区（行政村）应急预案（是/否）
     */
    @TableField("has_emergency_plan")
    private String hasEmergencyPlan;

    /**
     * 是否有本辖区弱势人群清单（是/否）
     */
    @TableField("has_vulnerable_groups_list")
    private String hasVulnerableGroupsList;

    /**
     * 是否有本辖区地质灾害等隐患点清单（是/否）
     */
    @TableField("has_disaster_points_list")
    private String hasDisasterPointsList;

    /**
     * 是否有社区（行政村）灾害类地图（是/否）
     */
    @TableField("has_disaster_map")
    private String hasDisasterMap;

    /**
     * 常住人口数量（人）
     */
    @TableField("resident_population")
    private Integer residentPopulation;

    /**
     * 上一年度防灾减灾救灾资金投入总金额（万元）
     */
    @TableField("last_year_funding_amount")
    private BigDecimal lastYearFundingAmount;

    /**
     * 现有储备物资、装备折合金额（万元）
     */
    @TableField("materials_equipment_value")
    private BigDecimal materialsEquipmentValue;

    /**
     * 社区医疗卫生服务站或村卫生室数量（个）
     */
    @TableField("medical_service_count")
    private Integer medicalServiceCount;

    /**
     * 民兵预备役人数（人）
     */
    @TableField("militia_reserve_count")
    private Integer militiaReserveCount;

    /**
     * 登记注册志愿者人数（人）
     */
    @TableField("registered_volunteer_count")
    private Integer registeredVolunteerCount;

    /**
     * 上一年度防灾减灾培训活动培训人次（人次）
     */
    @TableField("last_year_training_participants")
    private Integer lastYearTrainingParticipants;

    /**
     * 参与上一年度组织的防灾减灾演练活动的居民(人次)
     */
    @TableField("last_year_drill_participants")
    private Integer lastYearDrillParticipants;

    /**
     * 本级灾害应急避难场所容量（人）
     */
    @TableField("emergency_shelter_capacity")
    private Integer emergencyShelterCapacity;

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
}