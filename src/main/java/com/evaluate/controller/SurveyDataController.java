package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evaluate.common.Result;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.dto.ImportCheckResult;
import com.evaluate.entity.SurveyData;
import com.evaluate.service.ISurveyDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.Integer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Result<Map<String, Object>> getAllSurveyData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer pageSize) {
        try {
            // 分页参数校验
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 500) pageSize = 50;

            // 使用分页查询
            IPage<SurveyData> pageResult = surveyDataService.getByYearAndOrgCodePage(year, orgCode, page, pageSize);

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("current", pageResult.getCurrent());
            result.put("pages", pageResult.getPages());
            result.put("size", pageResult.getSize());

            log.debug("查询调查数据 - year: {}, orgCode: {}, page: {}, pageSize: {}, total: {}",
                    year, orgCode, page, pageSize, pageResult.getTotal());

            return Result.success(result);
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
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keyword) {
        try {
            List<SurveyData> list;
            if (keyword != null && !keyword.isEmpty()) {
                // 支持按关键词模糊搜索多个字段
                list = surveyDataService.searchByKeyword(keyword);
            } else {
                // 使用复合查询条件
                list = surveyDataService.getByConditions(surveyName, region, year);
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

    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteSurveyData(@RequestBody List<Long> ids) {
        try {
            log.info("批量删除调查数据，IDs: {}", ids);
            boolean result = surveyDataService.removeByIds(ids);
            return result ? Result.success(true) : Result.error("批量删除调查数据失败");
        } catch (Exception e) {
            log.error("批量删除调查数据失败，IDs: {}", ids, e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据年份和组织机构删除所有调查数据
     */
    @DeleteMapping("/delete-by-year-org")
    public Result<Long> deleteByYearAndOrg(
            @RequestParam Integer year,
            @RequestParam(required = false) String orgCode) {
        try {
            log.info("删除调查数据 - year: {}, orgCode: {}", year, orgCode);
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year);
            if (StringUtils.hasText(orgCode)) {
                wrapper.likeRight("region_code", orgCode.trim());
            }
            long count = surveyDataService.count(wrapper);
            boolean result = surveyDataService.remove(wrapper);
            return result ? Result.success(count) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除调查数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<Boolean> importSurveyData(@RequestParam MultipartFile file, @RequestParam Integer year,
                                           @RequestParam(required = false) String orgCode) {
        try {
            boolean result = surveyDataService.importFromExcel(file, year, orgCode);
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

    @GetMapping("/export/all")
    public Result<byte[]> exportAllSurveyData() {
        try {
            log.info("开始导出所有调查数据");
            byte[] data = surveyDataService.exportAllToExcel();
            log.info("导出完成，文件大小: {} 字节", data != null ? data.length : 0);
            if (data == null || data.length == 0) {
                log.warn("导出的文件数据为空");
                return Result.error("导出的文件数据为空");
            }
            return Result.success(data);
        } catch (Exception e) {
            log.error("导出所有调查数据失败", e);
            return Result.error("导出所有调查数据失败: " + e.getMessage());
        }
    }

    /**
     * 重新计算指定年份的所有调查数据的医疗床位统计
     */
    @PostMapping("/recalculate-medical-beds/{year}")
    public Result<Integer> recalculateMedicalBeds(@PathVariable Integer year) {
        try {
            log.info("开始重新计算{}年的医疗床位统计", year);
            int updatedCount = surveyDataService.recalculateMedicalBedsForYear(year);
            log.info("完成重新计算{}年医疗床位统计，更新了{}条记录", year, updatedCount);
            return Result.success(updatedCount);
        } catch (Exception e) {
            log.error("重新计算{}年医疗床位统计失败", year, e);
            return Result.error("重新计算医疗床位统计失败: " + e.getMessage());
        }
    }

    /**
     * 检查导入前置条件
     * 检查医疗床位和消防员配置数据是否完整
     */
    @PostMapping("/check-import-prerequisites")
    public Result<ImportCheckResult> checkImportPrerequisites(@RequestParam Integer year) {
        try {
            log.info("检查{}年导入前置条件", year);
            ImportCheckResult result = surveyDataService.checkImportPrerequisites(year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查导入前置条件失败", e);
            return Result.error("检查导入前置条件失败: " + e.getMessage());
        }
    }

    /**
     * 验证GPKG文件字段
     * 检查GPKG文件是否包含乡镇评估数据所需的必要字段
     */
    @PostMapping("/validate-gpkg")
    public Result<GpkgFieldValidationResult> validateGpkgFile(@RequestParam MultipartFile file,
                                                               @RequestParam(value = "year", required = false) Integer year) {
        try {
            log.info("验证乡镇评估数据GPKG文件: {}", file.getOriginalFilename());
            GpkgFieldValidationResult result = surveyDataService.validateGpkgFields(file, "township", year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证GPKG文件失败", e);
            return Result.error("验证GPKG文件失败: " + e.getMessage());
        }
    }

    /**
     * 从GPKG文件导入乡镇评估数据
     */
    @PostMapping("/import-gpkg")
    public Result<Boolean> importFromGpkg(@RequestParam MultipartFile file, @RequestParam Integer year) {
        try {
            log.info("从GPKG文件导入{}年乡镇评估数据", year);
            boolean result = surveyDataService.importFromGpkg(file, year);
            return result ? Result.success(true) : Result.error("导入GPKG文件失败");
        } catch (Exception e) {
            log.error("导入GPKG文件失败", e);
            return Result.error("导入GPKG文件失败: " + e.getMessage());
        }
    }
}
