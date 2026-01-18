package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Medical Institution Entity
 *
 * @author system
 * @since 2024-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("medical_institution")
public class MedicalInstitution implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Unique Code
     */
    @TableField("unique_code")
    private String uniqueCode;

    /**
     * Verification Status
     */
    @TableField("verification_status")
    private String verificationStatus;

    /**
     * Unified Social Credit Code / Institution Code
     */
    @TableField("unified_social_credit_code")
    private String unifiedSocialCreditCode;

    /**
     * Code Type
     */
    @TableField("code_type")
    private String codeType;

    /**
     * Institution Name
     */
    @TableField("institution_name")
    private String institutionName;

    /**
     * Institution Address
     */
    @TableField("institution_address")
    private String institutionAddress;

    /**
     * Institution Category Code
     */
    @TableField("institution_category_code")
    private String institutionCategoryCode;

    /**
     * Institution Type (Large Category)
     */
    @TableField("institution_type_large")
    private String institutionTypeLarge;

    /**
     * Institution Type (Medium Category)
     */
    @TableField("institution_type_medium")
    private String institutionTypeMedium;

    /**
     * Institution Type (Specialized Hospital Classification)
     */
    @TableField("institution_type_specialized")
    private String institutionTypeSpecialized;

    /**
     * Hospital Level
     */
    @TableField("hospital_level")
    private String hospitalLevel;

    /**
     * Institution Nature
     */
    @TableField("institution_nature")
    private String institutionNature;

    /**
     * Land Area (Square Meters)
     */
    @TableField("land_area")
    private BigDecimal landArea;

    /**
     * Building Area (Square Meters)
     */
    @TableField("building_area")
    private BigDecimal buildingArea;

    /**
     * Equipment Count Above 10k
     */
    @TableField("equipment_count_above_10k")
    private Integer equipmentCountAbove10k;

    /**
     * Total Staff
     */
    @TableField("total_staff")
    private Integer totalStaff;

    /**
     * Health Technical Personnel
     */
    @TableField("health_technical_personnel")
    private Integer healthTechnicalPersonnel;

    /**
     * Registered Nurses
     */
    @TableField("registered_nurses")
    private Integer registeredNurses;

    /**
     * Logistics Skill Personnel
     */
    @TableField("logistics_skill_personnel")
    private Integer logisticsSkillPersonnel;

    /**
     * Annual Total Visits
     */
    @TableField("annual_total_visits")
    private Integer annualTotalVisits;

    /**
     * Annual Admission Count
     */
    @TableField("annual_admission_count")
    private Integer annualAdmissionCount;

    /**
     * Annual Discharge Count
     */
    @TableField("annual_discharge_count")
    private Integer annualDischargeCount;

    /**
     * Actual Hospital Beds
     */
    @TableField("actual_hospital_beds")
    private Integer actualHospitalBeds;

    /**
     * Negative Pressure Beds
     */
    @TableField("negative_pressure_beds")
    private Integer negativePressureBeds;

    /**
     * ICU Beds
     */
    @TableField("icu_beds")
    private Integer icuBeds;

    /**
     * Pre-hospital Emergency Personnel
     */
    @TableField("pre_hospital_emergency_personnel")
    private Integer preHospitalEmergencyPersonnel;

    /**
     * Emergency Command Vehicle Count
     */
    @TableField("emergency_command_vehicle_count")
    private Integer emergencyCommandVehicleCount;

    /**
     * Transport Ambulance Count
     */
    @TableField("transport_ambulance_count")
    private Integer transportAmbulanceCount;

    /**
     * Monitor Ambulance Count
     */
    @TableField("monitor_ambulance_count")
    private Integer monitorAmbulanceCount;

    /**
     * Negative Pressure Ambulance Count
     */
    @TableField("negative_pressure_ambulance_count")
    private Integer negativePressureAmbulanceCount;

    /**
     * Blood Collection Vehicle Count
     */
    @TableField("blood_collection_vehicle_count")
    private Integer bloodCollectionVehicleCount;

    /**
     * Blood Delivery Vehicle Count
     */
    @TableField("blood_delivery_vehicle_count")
    private Integer bloodDeliveryVehicleCount;

    /**
     * Security Personnel Count
     */
    @TableField("security_personnel_count")
    private Integer securityPersonnelCount;

    /**
     * Emergency Power Supply
     */
    @TableField("emergency_power_supply")
    private String emergencyPowerSupply;

    /**
     * Emergency Power Supply - Other Description
     */
    @TableField("emergency_power_supply_other")
    private String emergencyPowerSupplyOther;

    /**
     * Water Supply Mode
     */
    @TableField("water_supply_mode")
    private String waterSupplyMode;

    /**
     * Heating Mode
     */
    @TableField("heating_mode")
    private String heatingMode;

    /**
     * Emergency Communication Mode
     */
    @TableField("emergency_communication_mode")
    private String emergencyCommunicationMode;

    /**
     * Emergency Communication Mode - Other Description
     */
    @TableField("emergency_communication_mode_other")
    private String emergencyCommunicationModeOther;

    /**
     * Disaster History Type
     */
    @TableField("disaster_history_type")
    private String disasterHistoryType;

    /**
     * Disaster History Type - Other Description
     */
    @TableField("disaster_history_type_other")
    private String disasterHistoryTypeOther;

    /**
     * Emergency Plan Type
     */
    @TableField("emergency_plan_type")
    private String emergencyPlanType;

    /**
     * Emergency Plan Type - Other Description
     */
    @TableField("emergency_plan_type_other")
    private String emergencyPlanTypeOther;

    /**
     * Unit Leader
     */
    @TableField("unit_leader")
    private String unitLeader;

    /**
     * Statistical Leader
     */
    @TableField("statistical_leader")
    private String statisticalLeader;

    /**
     * Form Filler
     */
    @TableField("form_filler")
    private String formFiller;

    /**
     * Contact Phone
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * Report Date
     */
    @TableField("report_date")
    private LocalDate reportDate;

    /**
     * Filling Instructions
     */
    @TableField("filling_instructions")
    private String fillingInstructions;

    /**
     * Data Year
     */
    @TableField("year")
    private Integer year;

    /**
     * Organization Code
     */
    @TableField("org_code")
    private String orgCode;

    @TableField(exist = false)
    private String provinceName;

    @TableField(exist = false)
    private String cityName;

    @TableField(exist = false)
    private String countyName;

    @TableField(exist = false)
    private String townshipName;

    @TableField(exist = false)
    private String communityName;

    /**
     * Create Time
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Update Time
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * Created By
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * Updated By
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
