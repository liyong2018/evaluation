package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.MedicalInstitution;
import org.apache.ibatis.annotations.Mapper;
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
     * 根据乡镇地址统计实有住院床位数总和
     *
     * 使用精确的乡镇名称匹配，确保不会错误匹配其他乡镇的医疗机构
     * 匹配规则：乡镇名称后必须跟特定后缀（社区/村/路/巷/号）或地址结束
     *
     * @param townshipAddress 乡镇地址（如：新中镇、天元街道）
     * @param year 数据年份
     * @return 实有住院床位数总和
     */
    @Select("SELECT COALESCE(SUM(actual_hospital_beds), 0) FROM medical_institution " +
            "WHERE (institution_address REGEXP CONCAT(#{arg0}, '([社区村路巷号]|$)') " +
            "OR institution_name REGEXP CONCAT(#{arg0}, '([院校所中心站]|$)')) " +
            "AND year = #{arg1}")
    Integer sumActualHospitalBedsByTownship(String townshipAddress, Integer year);

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