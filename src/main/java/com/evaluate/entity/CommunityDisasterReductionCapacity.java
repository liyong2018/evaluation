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
     * 数据所属年份
     */
    @TableField("year")
    private Integer year;

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

    // ========== 新增字段 - 对应Excel导入结构 ==========

    /**
     * 唯一码
     */
    @TableField("unique_id")
    private String uniqueId;

    /**
     * 核实状态
     */
    @TableField("verification_status")
    private String verificationStatus;

    /**
     * 社区（行政村）地址
     */
    @TableField("community_address")
    private String communityAddress;

    /**
     * 总户数（户）
     */
    @TableField("total_households")
    private Integer totalHouseholds;

    /**
     * 0-14岁人数
     */
    @TableField("age_0_14_count")
    private Integer age0To14Count;

    /**
     * 65岁（含）以上人数
     */
    @TableField("age_65_plus_count")
    private Integer age65PlusCount;

    /**
     * 残障人员人数
     */
    @TableField("disabled_person_count")
    private Integer disabledPersonCount;

    /**
     * 是否为全国综合减灾示范社区（是/否）
     */
    @TableField("is_national_demo_community")
    private String isNationalDemoCommunity;

    /**
     * 是否为省级综合减灾示范社区（是/否）
     */
    @TableField("is_provincial_demo_community")
    private String isProvincialDemoCommunity;

    /**
     * 灾害信息员人数（人）
     */
    @TableField("disaster_info_staff_count")
    private Integer disasterInfoStaffCount;

    /**
     * 本级灾害应急避难场所数量（个或处）
     */
    @TableField("emergency_shelter_count")
    private Integer emergencyShelterCount;

    /**
     * 防灾减灾应急物资储备方式（多选）
     */
    @TableField("material_storage_method")
    private String materialStorageMethod;

    /**
     * 防灾减灾应急物资储备方式-其他项说明
     */
    @TableField("material_storage_method_other")
    private String materialStorageMethodOther;

    /**
     * 灾害预警信息接收方式（多选）
     */
    @TableField("warning_receive_method")
    private String warningReceiveMethod;

    /**
     * 灾害预警信息接收方式-其他项说明
     */
    @TableField("warning_receive_method_other")
    private String warningReceiveMethodOther;

    /**
     * 灾害预警信息传达方式（多选）
     */
    @TableField("warning_communication_method")
    private String warningCommunicationMethod;

    /**
     * 灾害预警信息传达方式-其他项说明
     */
    @TableField("warning_communication_method_other")
    private String warningCommunicationMethodOther;

    /**
     * 灾情信息上报方式（多选）
     */
    @TableField("disaster_report_method")
    private String disasterReportMethod;

    /**
     * 灾情信息上报方式-其他项说明
     */
    @TableField("disaster_report_method_other")
    private String disasterReportMethodOther;

    /**
     * 上一年度组织的防灾减灾培训活动次数（次）
     */
    @TableField("last_year_training_count")
    private Integer lastYearTrainingCount;

    /**
     * 上一年度组织的防灾减灾演练活动次数（次）
     */
    @TableField("last_year_drill_count")
    private Integer lastYearDrillCount;

    /**
     * 单位负责人
     */
    @TableField("unit_leader")
    private String unitLeader;

    /**
     * 统计负责人
     */
    @TableField("statistics_leader")
    private String statisticsLeader;

    /**
     * 填表人
     */
    @TableField("form_filler")
    private String formFiller;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 报出日期（年/月/日）
     */
    @TableField("report_date")
    private java.time.LocalDate reportDate;

    /**
     * 填写说明
     */
    @TableField("fill_instructions")
    private String fillInstructions;
}