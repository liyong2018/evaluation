package com.evaluate.service;

import java.util.List;
import java.util.Map;

public interface IRegionDataService {

    /**
     * 根据数据类型获取省份列表
     * @param dataType 数据类型（township/community）
     * @return 省份列表
     */
    List<Map<String, Object>> getProvincesByDataType(String dataType);

    /**
     * 根据省份名称获取城市列表
     * @param dataType 数据类型（township/community）
     * @param provinceName 省份名称
     * @return 城市列表
     */
    List<Map<String, Object>> getCitiesByProvince(String dataType, String provinceName);

    /**
     * 根据城市名称获取区县列表
     * @param dataType 数据类型（township/community）
     * @param provinceName 省份名称
     * @param cityName 城市名称
     * @return 区县列表
     */
    List<Map<String, Object>> getCountiesByCity(String dataType, String provinceName, String cityName);

    /**
     * 根据选择的县获取对应的数据
     * @param dataType 数据类型（township/community）
     * @param provinceName 省份名称
     * @param cityName 城市名称
     * @param countyName 县名称
     * @return 数据列表
     */
    List<?> getDataByCounty(String dataType, String provinceName, String cityName, String countyName);
}