package com.evaluate.service;

import java.util.List;
import java.util.Map;

/**
 * 志愿者和民兵预备役统计服务接口
 *
 * @author System
 * @since 2025-01-01
 */
public interface IVolunteerMilitiaService {

    /**
     * 根据行政区划代码和年份统计志愿者人数
     *
     * @param regionCode 行政区划代码
     * @param year 年份
     * @return 志愿者人数，如果没有数据返回0
     */
    Integer sumVolunteersByRegion(String regionCode, Integer year);

    /**
     * 根据行政区划代码和年份统计民兵预备役人数
     *
     * @param regionCode 行政区划代码
     * @param year 年份
     * @return 民兵预备役人数，如果没有数据返回0
     */
    Integer sumMilitiaReserveByRegion(String regionCode, Integer year);

    /**
     * 根据县名称和年份统计志愿者人数
     *
     * @param countyName 县名称
     * @param year 年份
     * @return 志愿者人数
     */
    Integer sumVolunteersByCounty(String countyName, Integer year);

    /**
     * 根据县名称和年份统计民兵预备役人数
     *
     * @param countyName 县名称
     * @param year 年份
     * @return 民兵预备役人数
     */
    Integer sumMilitiaReserveByCounty(String countyName, Integer year);

    /**
     * 批量统计多个行政区划的志愿者和民兵预备役人数
     *
     * @param regionCodes 行政区划代码列表
     * @param year 年份
     * @return 统计结果Map，key为行政区划代码，value为[志愿者人数, 民兵预备役人数]
     */
    Map<String, int[]> batchSumVolunteerMilitia(List<String> regionCodes, Integer year);

    /**
     * 根据行政区划代码获取下属社区列表
     *
     * @param regionCode 行政区划代码
     * @return 社区代码列表
     */
    List<String> getCommunityCodesByRegion(String regionCode);

    /**
     * 根据县名称获取所有社区列表
     *
     * @param countyName 县名称
     * @return 社区代码列表
     */
    List<String> getCommunityCodesByCounty(String countyName);

    /**
     * 验证行政区划代码的有效性
     *
     * @param regionCode 行政区划代码
     * @return 是否有效
     */
    boolean isValidRegionCode(String regionCode);

    /**
     * 获取指定年份的所有统计数据摘要
     *
     * @param year 年份
     * @return 统计摘要信息
     */
    Map<String, Object> getStatisticsSummary(Integer year);
}