package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.SurveyData;
import com.evaluate.service.ISurveyDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 调查数据控制器
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/survey-data")
public class SurveyDataController {

    @Autowired
    private ISurveyDataService surveyDataService;

    @GetMapping
    public Result<List<SurveyData>> getAllSurveyData() {
        try {
            List<SurveyData> list = surveyDataService.list();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取调查数据列表失败", e);
            return Result.error("获取调查数据列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SurveyData> getSurveyDataById(@PathVariable Long id) {
        try {
            SurveyData surveyData = surveyDataService.getById(id);
            if (surveyData == null) {
                return Result.error("调查数据不存在");
            }
            return Result.success(surveyData);
        } catch (Exception e) {
            log.error("获取调查数据详情失败", e);
            return Result.error("获取调查数据详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/survey/{surveyName}")
    public Result<List<SurveyData>> getSurveyDataBySurveyName(@PathVariable String surveyName) {
        try {
            List<SurveyData> list = surveyDataService.getBySurveyName(surveyName);
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据调查名称获取数据失败", e);
            return Result.error("根据调查名称获取数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/region/{region}")
    public Result<List<SurveyData>> getSurveyDataByRegion(@PathVariable String region) {
        try {
            List<SurveyData> list = surveyDataService.getBySurveyRegion(region);
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据地区获取调查数据失败", e);
            return Result.error("根据地区获取调查数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<SurveyData>> searchSurveyData(
            @RequestParam(required = false) String surveyName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String year) {
        try {
            // 简化搜索逻辑，根据surveyName查询
            List<SurveyData> list;
            if (surveyName != null && !surveyName.isEmpty()) {
                list = surveyDataService.getBySurveyName(surveyName);
            } else if (region != null && !region.isEmpty()) {
                list = surveyDataService.getBySurveyRegion(region);
            } else {
                list = surveyDataService.list();
            }
            return Result.success(list);
        } catch (Exception e) {
            log.error("搜索调查数据失败", e);
            return Result.error("搜索调查数据失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> createSurveyData(@RequestBody SurveyData surveyData) {
        try {
            boolean result = surveyDataService.save(surveyData);
            return result ? Result.success(true) : Result.error("创建调查数据失败");
        } catch (Exception e) {
            log.error("创建调查数据失败", e);
            return Result.error("创建调查数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public Result<Boolean> batchCreateSurveyData(@RequestBody List<SurveyData> surveyDataList) {
        try {
            boolean result = surveyDataService.saveBatch(surveyDataList);
            return result ? Result.success(true) : Result.error("批量创建调查数据失败");
        } catch (Exception e) {
            log.error("批量创建调查数据失败", e);
            return Result.error("批量创建调查数据失败: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> updateSurveyData(@RequestBody SurveyData surveyData) {
        try {
            boolean result = surveyDataService.updateById(surveyData);
            return result ? Result.success(true) : Result.error("更新调查数据失败");
        } catch (Exception e) {
            log.error("更新调查数据失败", e);
            return Result.error("更新调查数据失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSurveyData(@PathVariable Long id) {
        try {
            boolean result = surveyDataService.removeById(id);
            return result ? Result.success(true) : Result.error("删除调查数据失败");
        } catch (Exception e) {
            log.error("删除调查数据失败", e);
            return Result.error("删除调查数据失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/survey/{surveyName}")
    public Result<Boolean> deleteSurveyDataBySurveyName(@PathVariable String surveyName) {
        try {
            boolean result = surveyDataService.deleteSurveyDataAndResults(surveyName);
            return result ? Result.success(true) : Result.error("删除调查数据失败");
        } catch (Exception e) {
            log.error("根据调查名称删除数据失败", e);
            return Result.error("根据调查名称删除数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<Boolean> importSurveyData(@RequestParam MultipartFile file) {
        try {
            boolean result = surveyDataService.importFromExcel(file);
            return result ? Result.success(true) : Result.error("导入调查数据失败");
        } catch (Exception e) {
            log.error("导入调查数据失败", e);
            return Result.error("导入调查数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/export/{surveyName}")
    public Result<byte[]> exportSurveyData(@PathVariable String surveyName) {
        try {
            byte[] data = surveyDataService.exportToExcel(surveyName);
            return Result.success(data);
        } catch (Exception e) {
            log.error("导出调查数据失败", e);
            return Result.error("导出调查数据失败: " + e.getMessage());
        }
    }
}