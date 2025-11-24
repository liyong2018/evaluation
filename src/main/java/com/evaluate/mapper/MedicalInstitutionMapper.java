package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.MedicalInstitution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 医疗卫生机构Mapper接口
 *
 * @author system
 * @since 2024-11-24
 */
@Mapper
public interface MedicalInstitutionMapper extends BaseMapper<MedicalInstitution> {

    /**
     * 根据乡镇地址统计实有住院床位数总和
     *
     * @param townshipAddress 乡镇地址（支持模糊匹配）
     * @param year 数据年份
     * @return 实有住院床位数总和
     */
    @Select("SELECT COALESCE(SUM(actual_hospital_beds), 0) FROM medical_institution " +
            "WHERE (institution_address LIKE CONCAT('%', #{arg0}, '%') " +
            "OR institution_name LIKE CONCAT('%', #{arg0}, '%')) " +
            "AND year = #{arg1}")
    Integer sumActualHospitalBedsByTownship(String townshipAddress, Integer year);

}