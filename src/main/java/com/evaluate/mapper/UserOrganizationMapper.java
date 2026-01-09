package com.evaluate.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserOrganizationMapper {

    @Select("SELECT organization_id FROM sys_user_organization WHERE user_id = #{userId}")
    List<Long> selectOrgIdsByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_organization (user_id, organization_id) VALUES (#{userId}, #{orgId})")
    int insertUserOrg(@Param("userId") Long userId, @Param("orgId") Long orgId);

    @Delete("DELETE FROM sys_user_organization WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT o.code FROM sys_user_organization uo " +
            "LEFT JOIN organization o ON uo.organization_id = o.id " +
            "WHERE uo.user_id = #{userId}" +
            "</script>")
    List<String> selectOrgCodesByUserId(@Param("userId") Long userId);
}
