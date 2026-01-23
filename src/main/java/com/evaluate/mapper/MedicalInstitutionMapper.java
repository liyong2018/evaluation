package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.MedicalInstitution;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 医疗卫生机构Mapper接口
 *
 * @author system
 * @since 2024-11-24
 */
@Mapper
public interface MedicalInstitutionMapper extends BaseMapper<MedicalInstitution> {

    /**
     * 显式插入方法，包含province/city/county/township字段
     * 绕过MyBatis-Plus自动生成的INSERT语句
     */
    @Insert("INSERT INTO medical_institution (" +
            "unique_code, verification_status, unified_social_credit_code, code_type, " +
            "institution_name, institution_address, province, city, county, township, " +
            "institution_category_code, institution_type_large, institution_type_medium, institution_type_specialized, " +
            "hospital_level, institution_nature, land_area, building_area, equipment_count_above_10k, " +
            "total_staff, health_technical_personnel, registered_nurses, logistics_skill_personnel, " +
            "annual_total_visits, annual_admission_count, annual_discharge_count, actual_hospital_beds, " +
            "negative_pressure_beds, icu_beds, pre_hospital_emergency_personnel, " +
            "emergency_command_vehicle_count, transport_ambulance_count, monitor_ambulance_count, " +
            "negative_pressure_ambulance_count, blood_collection_vehicle_count, blood_delivery_vehicle_count, " +
            "security_personnel_count, emergency_power_supply, water_supply_mode, heating_mode, " +
            "emergency_communication_mode, disaster_history_type, disaster_history_type_other, " +
            "emergency_plan_type, emergency_plan_type_other, unit_leader, statistical_leader, " +
            "form_filler, contact_phone, report_date, filling_instructions, year, org_code, " +
            "create_time, update_time, create_by, update_by" +
            ") VALUES (" +
            "#{uniqueCode}, #{verificationStatus}, #{unifiedSocialCreditCode}, #{codeType}, " +
            "#{institutionName}, #{institutionAddress}, #{province}, #{city}, #{county}, #{township}, " +
            "#{institutionCategoryCode}, #{institutionTypeLarge}, #{institutionTypeMedium}, #{institutionTypeSpecialized}, " +
            "#{hospitalLevel}, #{institutionNature}, #{landArea}, #{buildingArea}, #{equipmentCountAbove10k}, " +
            "#{totalStaff}, #{healthTechnicalPersonnel}, #{registeredNurses}, #{logisticsSkillPersonnel}, " +
            "#{annualTotalVisits}, #{annualAdmissionCount}, #{annualDischargeCount}, #{actualHospitalBeds}, " +
            "#{negativePressureBeds}, #{icuBeds}, #{preHospitalEmergencyPersonnel}, " +
            "#{emergencyCommandVehicleCount}, #{transportAmbulanceCount}, #{monitorAmbulanceCount}, " +
            "#{negativePressureAmbulanceCount}, #{bloodCollectionVehicleCount}, #{bloodDeliveryVehicleCount}, " +
            "#{securityPersonnelCount}, #{emergencyPowerSupply}, #{waterSupplyMode}, #{heatingMode}, " +
            "#{emergencyCommunicationMode}, #{disasterHistoryType}, #{disasterHistoryTypeOther}, " +
            "#{emergencyPlanType}, #{emergencyPlanTypeOther}, #{unitLeader}, #{statisticalLeader}, " +
            "#{formFiller}, #{contactPhone}, #{reportDate}, #{fillingInstructions}, #{year}, #{orgCode}, " +
            "#{createTime}, #{updateTime}, #{createBy}, #{updateBy}" +
            ")")
    int insertWithAddressFields(MedicalInstitution institution);

    /**
     * 根据乡镇名称统计实有住院床位数总和
     *
     * 使用精确的 township 列匹配，确保准确统计各乡镇的医疗机构床位数
     * 匹配规则：township 字段必须完全等于传入的乡镇名称
     *
     * @param townshipName 乡镇名称（如：新中镇、天元街道）
     * @param year 数据年份
     * @return 实有住院床位数总和
     */
    @Select("SELECT COALESCE(SUM(actual_hospital_beds), 0) FROM medical_institution " +
            "WHERE township = #{arg0} " +
            "AND year = #{arg1}")
    Integer sumActualHospitalBedsByTownship(String townshipName, Integer year);

    /**
     * 更新地址字段（用于在INSERT后更新township等字段）
     */
    @Update("UPDATE medical_institution SET province = #{province}, city = #{city}, " +
            "county = #{county}, township = #{township} WHERE id = #{id}")
    int updateAddressFields(@Param("id") Long id, @Param("province") String province,
                         @Param("city") String city, @Param("county") String county,
                         @Param("township") String township);

    /**
     * 修改医疗机构表唯一约束：从单字段改为复合字段
     * @return 修改结果
     */
    @Update("ALTER TABLE medical_institution DROP INDEX unique_code")
    int dropOldUniqueIndex();

    @Update("ALTER TABLE medical_institution DROP INDEX `medical_institution.unique_code`")
    int dropOldUniqueIndex2();

    @Update("ALTER TABLE medical_institution DROP INDEX uk_unique_code")
    int dropOldUniqueIndex3();

    @Update("ALTER TABLE medical_institution ADD CONSTRAINT uk_unique_code_year UNIQUE (unique_code, year)")
    int addCompositeUniqueIndex();

}