package com.evaluate.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleOrganizationMapper {

    @Select("SELECT organization_id FROM sys_role_organization WHERE role_id = #{roleId}")
    List<Long> selectOrgIdsByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_organization (role_id, organization_id) VALUES (#{roleId}, #{orgId})")
    int insertRoleOrg(@Param("roleId") Long roleId, @Param("orgId") Long orgId);

    @Delete("DELETE FROM sys_role_organization WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Select("<script>" +
            "SELECT o.code FROM sys_role_organization ro " +
            "LEFT JOIN organization o ON ro.organization_id = o.id " +
            "WHERE ro.role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "</script>")
    List<String> selectOrgCodesByRoleIds(@Param("roleIds") List<Long> roleIds);
}
