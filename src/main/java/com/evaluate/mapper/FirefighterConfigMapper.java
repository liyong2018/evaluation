package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.FirefighterConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消防员配置Mapper接口
 *
 * @author System
 * @since 2025-01-01
 */
@Mapper
public interface FirefighterConfigMapper extends BaseMapper<FirefighterConfig> {

    /**
     * 根据行政区划代码查询消防员数量
     *
     * @param regionCode 行政区划代码
     * @return 消防员数量
     */
    @Select("SELECT firefighter_count FROM firefighter_config WHERE region_code = #{regionCode} AND status = 1 LIMIT 1")
    Integer getFirefighterCountByRegionCode(@Param("regionCode") String regionCode);

    /**
     * 根据行政区划代码前缀匹配并汇总消防员数量
     * 用于处理乡镇代码(9位)匹配社区级配置(12位)的场景
     *
     * @param regionCodePrefix 行政区划代码前缀（如 511402104）
     * @return 消防员数量总和
     */
    @Select("SELECT COALESCE(SUM(firefighter_count), 0) FROM firefighter_config WHERE region_code LIKE CONCAT(#{regionCodePrefix}, '%') AND status = 1")
    Integer sumFirefighterCountByRegionCodePrefix(@Param("regionCodePrefix") String regionCodePrefix);

    /**
     * 根据乡镇名称查询消防员数量
     *
     * @param townshipName 乡镇名称
     * @return 消防员数量
     */
    @Select("SELECT firefighter_count FROM firefighter_config WHERE township_name = #{townshipName} AND status = 1 LIMIT 1")
    Integer getFirefighterCountByTownshipName(@Param("townshipName") String townshipName);

    /**
     * 根据地理位置查询消防员配置列表
     *
     * @param provinceName 省名称
     * @param cityName 市名称
     * @param countyName 县名称
     * @param townshipName 乡镇名称（可为空）
     * @return 消防员配置列表
     */
    @Select("<script>" +
            "SELECT * FROM firefighter_config WHERE status = 1" +
            "<if test='provinceName != null and provinceName != \"\"'> AND province_name = #{provinceName}</if>" +
            "<if test='cityName != null and cityName != \"\"'> AND city_name = #{cityName}</if>" +
            "<if test='countyName != null and countyName != \"\"'> AND county_name = #{countyName}</if>" +
            "<if test='townshipName != null and townshipName != \"\"'> AND township_name = #{townshipName}</if>" +
            " ORDER BY region_code" +
            "</script>")
    List<FirefighterConfig> getByLocation(
            @Param("provinceName") String provinceName,
            @Param("cityName") String cityName,
            @Param("countyName") String countyName,
            @Param("townshipName") String townshipName);

    /**
     * 根据行政区划代码列表批量查询消防员配置
     *
     * @param regionCodes 行政区划代码列表
     * @return 消防员配置列表
     */
    @Select("<script>" +
            "SELECT * FROM firefighter_config WHERE status = 1 AND region_code IN " +
            "<foreach collection='regionCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " ORDER BY region_code" +
            "</script>")
    List<FirefighterConfig> getByRegionCodes(@Param("regionCodes") List<String> regionCodes);

    /**
     * 根据县名称查询所有乡镇的消防员配置
     *
     * @param countyName 县名称
     * @return 消防员配置列表
     */
    @Select("SELECT * FROM firefighter_config WHERE county_name = #{countyName} AND status = 1 ORDER BY township_name")
    List<FirefighterConfig> getByCountyName(@Param("countyName") String countyName);

    /**
     * 统计指定县的总消防员数量
     *
     * @param countyName 县名称
     * @return 总消防员数量
     */
    @Select("SELECT COALESCE(SUM(firefighter_count), 0) FROM firefighter_config WHERE county_name = #{countyName} AND status = 1")
    Integer sumFirefighterCountByCounty(@Param("countyName") String countyName);
}