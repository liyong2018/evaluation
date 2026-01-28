package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 调查数据表 (乡镇评估版本)
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("survey_data")
public class SurveyData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 唯一码
     */
    @TableField("unique_id")
    private String uniqueId;

    /**
     * 行政区代码
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 核实状态
     */
    @TableField("verification_status")
    private String verificationStatus;

    /**
     * 省名称
     */
    @TableField("province")
    private String province;

    /**
     * 市名称
     */
    @TableField("city")
    private String city;

    /**
     * 县名称
     */
    @TableField("county")
    private String county;

    /**
     * 乡镇名称
     */
    @TableField("township")
    private String township;

    /**
     * 乡镇（街道）地址
     */
    @TableField("township_address")
    private String townshipAddress;

    /**
     * 数据所属年份
     */
    @TableField("year")
    private Integer year;

    /**
     * 常住人口数量
     */
    @TableField("population")
    private Long population;

    /**
     * 年末总户数(户)
     */
    @TableField("total_households")
    private Integer totalHouseholds;

    /**
     * 影响乡镇（街道）的主要灾害类型
     */
    @TableField("main_disaster_types")
    private String mainDisasterTypes;

    /**
     * 影响乡镇（街道）的主要灾害类型-其他项说明
     */
    @TableField("disaster_types_other")
    private String disasterTypesOther;

    /**
     * 本级灾害管理工作人员总数
     */
    @TableField("management_staff")
    private Integer managementStaff;

    /**
     * 本级灾害信息员人数
     */
    @TableField("disaster_info_staff")
    private Integer disasterInfoStaff;

    /**
     * 是否开展乡镇（街道）灾害风险评估
     */
    @TableField("risk_assessment")
    private String riskAssessment;

    /**
     * 是否有乡镇（街道）灾害类地图
     */
    @TableField("has_disaster_map")
    private String hasDisasterMap;

    /**
     * 灾害预警信息接收方式
     */
    @TableField("warning_receive_method")
    private String warningReceiveMethod;

    /**
     * 灾害预警信息接收方式-其他项说明
     */
    @TableField("warning_receive_method_other")
    private String warningReceiveMethodOther;

    /**
     * 灾害预警信息传达方式
     */
    @TableField("warning_communication_method")
    private String warningCommunicationMethod;

    /**
     * 灾害预警信息传达方式-其他项说明
     */
    @TableField("warning_communication_method_other")
    private String warningCommunicationMethodOther;

    /**
     * 灾情信息上报方式
     */
    @TableField("disaster_report_method")
    private String disasterReportMethod;

    /**
     * 灾情信息上报方式-其他项说明
     */
    @TableField("disaster_report_method_other")
    private String disasterReportMethodOther;

    /**
     * 近3年编制或修订自然灾害应急预案数量(个)
     */
    @TableField("emergency_plan_count")
    private Integer emergencyPlanCount;

    /**
     * 近3年针对自然灾害启动应急响应次数(次)
     */
    @TableField("emergency_response_count")
    private Integer emergencyResponseCount;

    /**
     * 上一年度组织的应急管理培训和演练次数(次)
     */
    @TableField("training_drill_count")
    private Integer trainingDrillCount;

    /**
     * 上一年度组织的应急管理培训和演练参与人次
     */
    @TableField("training_participants")
    private Integer trainingParticipants;

    /**
     * 志愿者数量
     */
    @TableField("volunteers")
    private Integer volunteersCount;

    /**
     * 消防员数量
     */
    @TableField("firefighters")
    private Integer firefightersCount;

    /**
     * 民兵预备役数量
     */
    @TableField("militia_reserve")
    private Integer militiaReserveCount;

    /**
     * 乡镇（街道）综合减灾工作经费保障方式
     */
    @TableField("funding_support_method")
    private String fundingSupportMethod;

    /**
     * 乡镇（街道）综合减灾工作经费保障方式-其他说明
     */
    @TableField("funding_support_method_other")
    private String fundingSupportMethodOther;

    /**
     * 上一年度防灾减灾救灾资金投入总金额(万元)
     */
    @TableField("funding_amount")
    private Double fundingAmount;

    /**
     * 救灾物资储备方式
     */
    @TableField("material_storage_method")
    private String materialStorageMethod;

    /**
     * 救灾物资储备方式-其他项说明
     */
    @TableField("material_storage_method_other")
    private String materialStorageMethodOther;

    /**
     * 本级救灾物资、装备储备点数量(个)
     */
    @TableField("storage_point_count")
    private Integer storagePointCount;

    /**
     * 本级储备点救灾物资、装备数量(套/个/件)
     */
    @TableField("storage_equipment_count")
    private Integer storageEquipmentCount;

    /**
     * 其中：应急电源或应急发电设备数量(套或件)
     */
    @TableField("emergency_power_count")
    private Integer emergencyPowerCount;

    /**
     * 应急通信设备数量(套或件)
     */
    @TableField("emergency_communication_count")
    private Integer emergencyCommunicationCount;

    /**
     * 应急供水设备数量(套或件)
     */
    @TableField("emergency_water_count")
    private Integer emergencyWaterCount;

    /**
     * 医院床位数
     */
    @TableField("hospital_beds")
    private Integer hospitalBeds;

    /**
     * 应急医疗设备数量(套或件)
     */
    @TableField("emergency_medical_count")
    private Integer emergencyMedicalCount;

    /**
     * 现有储备物资、装备折合金额(万元)
     */
    @TableField("material_value")
    private Double materialValue;

    /**
     * 本级灾害应急避难场所数量(个或处)
     */
    @TableField("shelter_count")
    private Integer shelterCount;

    /**
     * 本级灾害应急避难场所容量
     */
    @TableField("shelter_capacity")
    private Integer shelterCapacity;

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
     * 报出日期(年/月/日)
     */
    @TableField("report_date")
    private LocalDate reportDate;

    /**
     * 填写说明
     */
    @TableField("fill_instructions")
    private String fillInstructions;

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

    /**
     * 是否删除(0-未删除，1-已删除)
     */
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Integer isDeleted;

    // ========== 向后兼容方法 - 保持原有字段名以支持现有公式计算 ==========

    /**
     * 获取医院床位数 - 向后兼容方法
     *
     * @return 医院床位数
     */
    public Integer getHospitalBeds() {
        return hospitalBeds;
    }

    /**
     * 设置医院床位数 - 向后兼容方法
     *
     * @param hospitalBeds 医院床位数
     * @return 当前对象
     */
    public SurveyData setHospitalBeds(Integer hospitalBeds) {
        this.hospitalBeds = hospitalBeds;
        return this;
    }

    /**
     * 获取消防员数量 - 向后兼容方法
     *
     *
     * @return 消防员数量
     */
    public Integer getFirefighters() {
        return firefightersCount;
    }

    /**
     * 设置消防员数量 - 向后兼容方法
     *
     * @param firefighters 消防员数量
     * @return 当前对象
     */
    public SurveyData setFirefighters(Integer firefighters) {
        this.firefightersCount = firefighters;
        return this;
    }

    /**
     * 获取志愿者数量 - 向后兼容方法
     * 现在使用独立的志愿者字段
     *
     * @return 志愿者数量
     */
    public Integer getVolunteers() {
        return volunteersCount;
    }

    /**
     * 设置志愿者数量 - 向后兼容方法
     * 现在使用独立的志愿者字段
     *
     * @param volunteers 志愿者数量
     * @return 当前对象
     */
    public SurveyData setVolunteers(Integer volunteers) {
        this.volunteersCount = volunteers;
        return this;
    }

    /**
     * 获取志愿者数量 - 新的独立方法
     *
     * @return 志愿者数量
     */
    public Integer getVolunteersCount() {
        return volunteersCount;
    }

    /**
     * 设置志愿者数量 - 新的独立方法
     *
     * @param volunteersCount 志愿者数量
     * @return 当前对象
     */
    public SurveyData setVolunteersCount(Integer volunteersCount) {
        this.volunteersCount = volunteersCount;
        return this;
    }

    /**
     * 获取民兵预备役数量 - 向后兼容方法
     *
     * @return 民兵预备役数量
     */
    public Integer getMilitiaReserve() {
        return militiaReserveCount;
    }

    /**
     * 设置民兵预备役数量 - 向后兼容方法
     *
     * @param militiaReserve 民兵预备役数量
     * @return 当前对象
     */
    public SurveyData setMilitiaReserve(Integer militiaReserve) {
        this.militiaReserveCount = militiaReserve;
        return this;
    }
}