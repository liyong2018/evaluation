package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.mapper.SurveyDataMapper;
import com.evaluate.mapper.CommunityDisasterReductionCapacityMapper;
import com.evaluate.service.IRegionDataService;
import com.evaluate.entity.SurveyData;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionDataServiceImpl implements IRegionDataService {

    @Autowired
    private SurveyDataMapper surveyDataMapper;

    @Autowired
    private CommunityDisasterReductionCapacityMapper communityCapacityMapper;

    @Override
    public List<Map<String, Object>> getProvincesByDataType(String dataType) {
        if ("community".equals(dataType)) {
            // 从社区数据表获取省份
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT province_name as name, province_name as code");
            wrapper.isNotNull("province_name");
            wrapper.ne("province_name", "");

            List<Map<String, Object>> result = communityCapacityMapper.selectMaps(wrapper);
            return result.stream()
                .map(map -> {
                    Map<String, Object> province = new LinkedHashMap<>();
                    province.put("name", map.get("name"));
                    province.put("code", map.get("code"));
                    return province;
                })
                .collect(Collectors.toList());
        } else {
            // 从调查数据表获取省份
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT province as name, province as code");
            wrapper.isNotNull("province");
            wrapper.ne("province", "");

            List<Map<String, Object>> result = surveyDataMapper.selectMaps(wrapper);
            return result.stream()
                .map(map -> {
                    Map<String, Object> province = new LinkedHashMap<>();
                    province.put("name", map.get("name"));
                    province.put("code", map.get("code"));
                    return province;
                })
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<Map<String, Object>> getCitiesByProvince(String dataType, String provinceName) {
        if ("community".equals(dataType)) {
            // 从社区数据表获取城市
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT city_name as name, city_name as code");
            wrapper.eq("province_name", provinceName);
            wrapper.isNotNull("city_name");
            wrapper.ne("city_name", "");

            List<Map<String, Object>> result = communityCapacityMapper.selectMaps(wrapper);
            return result.stream()
                .map(map -> {
                    Map<String, Object> city = new LinkedHashMap<>();
                    city.put("name", map.get("name"));
                    city.put("code", map.get("code"));
                    return city;
                })
                .collect(Collectors.toList());
        } else {
            // 从调查数据表获取城市
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT city as name, city as code");
            wrapper.eq("province", provinceName);
            wrapper.isNotNull("city");
            wrapper.ne("city", "");

            List<Map<String, Object>> result = surveyDataMapper.selectMaps(wrapper);
            return result.stream()
                .map(map -> {
                    Map<String, Object> city = new LinkedHashMap<>();
                    city.put("name", map.get("name"));
                    city.put("code", map.get("code"));
                    return city;
                })
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<Map<String, Object>> getCountiesByCity(String dataType, String provinceName, String cityName) {
        if ("community".equals(dataType)) {
            // 从社区数据表获取区县
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT county_name as name, county_name as code");
            wrapper.eq("province_name", provinceName);
            wrapper.eq("city_name", cityName);
            wrapper.isNotNull("county_name");
            wrapper.ne("county_name", "");

            List<Map<String, Object>> result = communityCapacityMapper.selectMaps(wrapper);
            return result.stream()
                .map(map -> {
                    Map<String, Object> county = new LinkedHashMap<>();
                    county.put("name", map.get("name"));
                    county.put("code", map.get("code"));
                    return county;
                })
                .collect(Collectors.toList());
        } else {
            // 从调查数据表获取区县
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT county as name, county as code");
            wrapper.eq("province", provinceName);
            wrapper.eq("city", cityName);
            wrapper.isNotNull("county");
            wrapper.ne("county", "");

            List<Map<String, Object>> result = surveyDataMapper.selectMaps(wrapper);
            return result.stream()
                .map(map -> {
                    Map<String, Object> county = new LinkedHashMap<>();
                    county.put("name", map.get("name"));
                    county.put("code", map.get("code"));
                    return county;
                })
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<?> getDataByCounty(String dataType, String provinceName, String cityName, String countyName) {
        if ("community".equals(dataType)) {
            // 从社区数据表获取数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("province_name", provinceName);
            wrapper.eq("city_name", cityName);
            wrapper.eq("county_name", countyName);
            wrapper.orderByAsc("id");

            return communityCapacityMapper.selectList(wrapper);
        } else {
            // 从调查数据表获取数据
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.eq("province", provinceName);
            wrapper.eq("city", cityName);
            wrapper.eq("county", countyName);
            wrapper.orderByAsc("id");

            return surveyDataMapper.selectList(wrapper);
        }
    }
}