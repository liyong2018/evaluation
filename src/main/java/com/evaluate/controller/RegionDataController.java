package com.evaluate.controller;

import com.evaluate.entity.RegionData;
import com.evaluate.service.IRegionDataService;
import com.evaluate.entity.SurveyData;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/region")
@CrossOrigin
public class RegionDataController {

    @Autowired
    private IRegionDataService regionDataService;

    /**
     * 根据数据类型获取省份列表
     */
    @GetMapping("/provinces")
    public Result<List<Map<String, Object>>> getProvinces(@RequestParam String dataType) {
        try {
            List<Map<String, Object>> provinces = regionDataService.getProvincesByDataType(dataType);
            return Result.success(provinces);
        } catch (Exception e) {
            return Result.error("获取省份列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据省份名称获取城市列表
     */
    @GetMapping("/cities")
    public Result<List<Map<String, Object>>> getCities(
            @RequestParam String dataType,
            @RequestParam String provinceName) {
        try {
            List<Map<String, Object>> cities = regionDataService.getCitiesByProvince(dataType, provinceName);
            return Result.success(cities);
        } catch (Exception e) {
            return Result.error("获取城市列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据城市名称获取区县列表
     */
    @GetMapping("/counties")
    public Result<List<Map<String, Object>>> getCounties(
            @RequestParam String dataType,
            @RequestParam String provinceName,
            @RequestParam String cityName) {
        try {
            List<Map<String, Object>> counties = regionDataService.getCountiesByCity(dataType, provinceName, cityName);
            return Result.success(counties);
        } catch (Exception e) {
            return Result.error("获取区县列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据选择的县获取对应的数据
     */
    @GetMapping("/data")
    public Result<List<?>> getDataByCounty(
            @RequestParam String dataType,
            @RequestParam String provinceName,
            @RequestParam String cityName,
            @RequestParam String countyName) {
        try {
            List<?> data = regionDataService.getDataByCounty(dataType, provinceName, cityName, countyName);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取县数据失败：" + e.getMessage());
        }
    }
}