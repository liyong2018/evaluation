package com.evaluate.service;

import java.util.List;
import java.util.Map;

public interface IRegionDataService {

    /**
     * 根据数据类型获取省份列表
     * @param dataType 数据类型（township/community）
     * @param year 年份（可选）
     * @return 省份列表
     */
    List<Map<String, Object>> getProvincesByDataType(String dataType, Integer year);

    /**
     * 根据省份名称获取城市列表
     * @param dataType 数据类型（township/community）
     * @param provinceName 省份名称
     * @param year 年份（可选）
     * @return 城市列表
     */
    List<Map<String, Object>> getCitiesByProvince(String dataType, String provinceName, Integer year);

    /**
     * 根据城市名称获取区县列表
     * @param dataType 数据类型（township/community）
     * @param provinceName 省份名称
     * @param cityName 城市名称
     * @param year 年份（可选）
     * @return 区县列表
     */
    List<Map<String, Object>> getCountiesByCity(String dataType, String provinceName, String cityName, Integer year);

    /**
     * 根据选择的县获取对应的数据
     * @param dataType 数据类型（township/community）
     * @param provinceName 省份名称
     * @param cityName 城市名称
     * @param countyName 县名称
     * @param year 年份（可选）
     * @return 数据列表
     */
    List<?> getDataByCounty(String dataType, String provinceName, String cityName, String countyName, Integer year);
}
