package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.GrassrootsOrganization;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 基层组织机构 Mapper
 */
public interface GrassrootsOrganizationMapper extends BaseMapper<GrassrootsOrganization> {

    /**
     * 标记记录为已删除（绕过 @TableLogic）
     * 用于增量存储中按年份删除的场景
     */
    @Update("UPDATE grassroots_organization SET is_deleted = 1 WHERE id = #{id}")
    int markAsDeleted(@Param("id") Long id);

    /**
     * 根据code和year标记记录为已删除（绕过 @TableLogic）
     * 用于增量存储中按年份删除的场景
     */
    @Update("UPDATE grassroots_organization SET is_deleted = 1 WHERE code = #{code} AND year = #{year}")
    int markAsDeletedByCodeAndYear(@Param("code") String code, @Param("year") Integer year);

    /**
     * 根据code和year查询记录（包含已删除记录，绕过 @TableLogic）
     * 用于更新时查找年份记录
     */
    @Select("SELECT * FROM grassroots_organization WHERE code = #{code} AND year = #{year} LIMIT 1")
    GrassrootsOrganization selectByCodeAndYearIncludeDeleted(@Param("code") String code, @Param("year") Integer year);

    /**
     * 根据code和year取消删除标记（绕过 @TableLogic）
     * 用于恢复已删除的记录
     */
    @Update("UPDATE grassroots_organization SET is_deleted = 0 WHERE code = #{code} AND year = #{year}")
    int markAsUndeletedByCodeAndYear(@Param("code") String code, @Param("year") Integer year);

    /**
     * 综合更新：取消删除标记并更新所有字段（绕过 @TableLogic）
     * 用于恢复已删除记录并同时更新其内容
     */
    @Update("UPDATE grassroots_organization SET is_deleted = 0, name = #{name}, level = #{level}, " +
            "county_id = #{countyId}, parent_id = #{parentId}, " +
            "province_name = #{provinceName}, city_name = #{cityName}, county_name = #{countyName}, " +
            "township_name = #{townshipName}, community_name = #{communityName} " +
            "WHERE code = #{code} AND year = #{year}")
    int restoreAndUpdateYearRecord(@Param("code") String code, @Param("year") Integer year,
                                   @Param("name") String name, @Param("level") Integer level,
                                   @Param("countyId") Long countyId, @Param("parentId") Long parentId,
                                   @Param("provinceName") String provinceName, @Param("cityName") String cityName,
                                   @Param("countyName") String countyName, @Param("townshipName") String townshipName,
                                   @Param("communityName") String communityName);

    /**
     * 查询某区县下的基准基层组织记录（包含已删除记录）
     */
    @Select("SELECT * FROM grassroots_organization WHERE county_id = #{countyId} AND is_baseline = 1")
    java.util.List<GrassrootsOrganization> selectByCountyIdBaselineIncludeDeleted(@Param("countyId") Long countyId);

    /**
     * 查询某区县下目标年份及之前的基层组织记录（包含已删除记录）
     */
    @Select("SELECT * FROM grassroots_organization " +
            "WHERE county_id = #{countyId} " +
            "AND (is_baseline = 1 " +
            "OR (year >= 2020 AND year <= #{year} AND (is_baseline = 0 OR is_baseline IS NULL)))")
    java.util.List<GrassrootsOrganization> selectByCountyIdUpToYearIncludeDeleted(@Param("countyId") Long countyId,
                                                                                   @Param("year") Integer year);

    @Select("SELECT DISTINCT county_id FROM grassroots_organization " +
            "WHERE county_id IS NOT NULL " +
            "AND year = #{year} " +
            "AND (is_baseline = 0 OR is_baseline IS NULL)")
    List<Long> selectCountyIdsWithYearRecordsIncludeDeleted(@Param("year") Integer year);

    @Select("SELECT county_id AS countyId, MAX(year) AS maxYear FROM grassroots_organization " +
            "WHERE county_id IS NOT NULL " +
            "AND year IS NOT NULL " +
            "AND year > 2020 " +
            "AND year <= #{year} " +
            "AND (is_baseline = 0 OR is_baseline IS NULL) " +
            "AND is_deleted = 0 " +
            "GROUP BY county_id")
    List<Map<String, Object>> selectCountyMaxYearUpToIncludeDeleted(@Param("year") Integer year);
}
