package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.Organization;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 组织机构 Mapper
 */
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 根据 code 和 year 查询组织记录（包含已删除记录）
     * 此方法绕过 @TableLogic 的自动过滤，可以查询到 is_deleted=1 的记录
     */
    @Select("SELECT * FROM organization WHERE code = #{code} AND year = #{year} LIMIT 1")
    Organization selectByCodeAndYearIncludeDeleted(@Param("code") String code, @Param("year") Integer year);

    /**
     * 根据 code 和 year 查询组织记录列表（包含已删除记录）
     */
    @Select("SELECT * FROM organization WHERE code = #{code} AND year = #{year}")
    List<Organization> selectListByCodeAndYearIncludeDeleted(@Param("code") String code, @Param("year") Integer year);

    /**
     * 标记组织记录为已删除（绕过 @TableLogic）
     */
    @Update("UPDATE organization SET is_deleted = 1 WHERE id = #{id}")
    int markAsDeleted(@Param("id") Long id);
}
