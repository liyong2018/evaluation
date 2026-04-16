package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家庭减灾能力实体类
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("family_disaster_reduction_capacity_2020")
public class FamilyDisasterReductionCapacity implements Serializable {

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
     * 乡镇（街道）名称
     */
    @TableField("town_name")
    private String townName;

    /**
     * 社区（行政村）名称
     */
    @TableField("village_name")
    private String villageName;

    /**
     * 0-10岁人数（人）
     */
    @TableField("age_0_10_count")
    private Integer age0To10Count;

    /**
     * 65岁（含）以上人数（人）
     */
    @TableField("age_65_plus_count")
    private Integer age65PlusCount;

    /**
     * 残障人数（人）
     */
    @TableField("disabled_count")
    private Integer disabledCount;

    /**
     * 家庭总人数（人）
     */
    @TableField("total_people")
    private Integer totalPeople;

    /**
     * 患有慢性病、需要长期服药的人数（人）
     */
    @TableField("chronic_disease_count")
    private Integer chronicDiseaseCount;

    /**
     * 您家里有以下哪些应急物品？(JSON)
     */
    @TableField("emergency_supplies")
    private String emergencySupplies;

    /**
     * 出现因灾断水的情况下，您家里的干净饮用水储量能支撑全家人多久？
     */
    @TableField("water_reserve_days")
    private String waterReserveDays;

    /**
     * 出现因灾无法供给食物的情况下，您家里存储的方便食品能支撑全家人多久？
     */
    @TableField("food_reserve_days")
    private String foodReserveDays;

    /**
     * 您家是否有人在社区（村）微信群或QQ群中?
     */
    @TableField("in_community_group")
    private String inCommunityGroup;

    /**
     * 您是否知道社区（村）或社区（村）工作人员联系方式？
     */
    @TableField("know_staff_contact")
    private String knowStaffContact;

    /**
     * 您收到过哪些类型灾害的预警信息？(JSON)
     */
    @TableField("received_warning_types")
    private String receivedWarningTypes;

    /**
     * 您的家庭是否了解紧急避难路线？
     */
    @TableField("know_evacuation_route")
    private String knowEvacuationRoute;

    /**
     * 您近三年参加过几次社区（村）组织的应急演练？
     */
    @TableField("drill_participation_count")
    private String drillParticipationCount;

    /**
     * 您是否参加过急救培训？
     */
    @TableField("first_aid_training")
    private String firstAidTraining;

    /**
     * 您掌握下面哪些急救方法？(JSON)
     */
    @TableField("mastered_first_aid_skills")
    private String masteredFirstAidSkills;

    /**
     * 每人或每户的权数
     */
    @TableField("weight")
    private BigDecimal weight;

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
