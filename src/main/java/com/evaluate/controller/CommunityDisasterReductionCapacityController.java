package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.lang.Integer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区行政村减灾能力控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/community-capacity")
public class CommunityDisasterReductionCapacityController {

    @Autowired
    private ICommunityDisasterReductionCapacityService communityDisasterReductionCapacityService;

    /**
     * 导入社区行政村减灾能力数据
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importCommunityCapacityData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year) {
        log.info("开始导入社区行政村减灾能力数据，文件名: {}, 年份: {}", file.getOriginalFilename(), year);
        try {
            Map<String, Object> result = communityDisasterReductionCapacityService.importCommunityCapacityData(file, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入社区行政村减灾能力数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询社区行政村减灾能力数据
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getCommunityCapacityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String communityName,
            @RequestParam(required = false) Integer year) {
        Map<String, Object> result = new HashMap<>();

        log.info("查询社区行政村减灾能力数据列表，页码: {}, 每页大小: {}, 行政区代码: {}, 社区名称: {}, 年份: {}",
                page, size, regionCode, communityName, year);
        try {
            result = communityDisasterReductionCapacityService.getCommunityCapacityList(
                    page, size, regionCode, communityName, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询社区行政村减灾能力数据列表失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 搜索社区行政村减灾能力数据
     */
    @GetMapping("/search")
    public Result<List<CommunityDisasterReductionCapacity>> searchCommunityCapacity(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String communityName,
            @RequestParam(required = false) Integer year) {
        log.info("搜索社区行政村减灾能力数据，关键词: {}, 行政区代码: {}, 社区名称: {}, 年份: {}",
                keyword, regionCode, communityName, year);
        try {
            List<CommunityDisasterReductionCapacity> result = communityDisasterReductionCapacityService.searchCommunityCapacity(
                    keyword, regionCode, communityName, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索社区行政村减灾能力数据失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取社区行政村减灾能力数据
     */
    @GetMapping("/{id}")
    public Result<CommunityDisasterReductionCapacity> getById(@PathVariable Long id) {
        log.info("根据ID获取社区行政村减灾能力数据，ID: {}", id);
        try {
            CommunityDisasterReductionCapacity data = communityDisasterReductionCapacityService.getById(id);
            if (data != null) {
                return Result.success(data);
            } else {
                return Result.error("数据不存在");
            }
        } catch (Exception e) {
            log.error("根据ID获取社区行政村减灾能力数据失败，ID: {}", id, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新社区行政村减灾能力数据
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateById(@PathVariable Long id, @RequestBody CommunityDisasterReductionCapacity data) {
        log.info("更新社区行政村减灾能力数据，ID: {}", id);
        try {
            data.setId(id);
            boolean result = communityDisasterReductionCapacityService.updateById(data);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新社区行政村减灾能力数据失败，ID: {}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除社区行政村减灾能力数据
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        log.info("删除社区行政村减灾能力数据，ID: {}", id);
        try {
            boolean result = communityDisasterReductionCapacityService.deleteById(id);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除社区行政村减灾能力数据失败，ID: {}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除社区行政村减灾能力数据
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        log.info("批量删除社区行政村减灾能力数据，IDs: {}", ids);
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的数据");
            }
            boolean result = communityDisasterReductionCapacityService.deleteByIds(ids);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除社区行政村减灾能力数据失败，IDs: {}", ids, e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据年份和组织机构删除所有社区减灾能力数据
     */
    @DeleteMapping("/delete-by-year-org")
    public Result<Long> deleteByYearAndOrg(
            @RequestParam Integer year,
            @RequestParam(required = false) String orgCode) {
        try {
            log.info("删除社区减灾能力数据 - year: {}, orgCode: {}", year, orgCode);
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CommunityDisasterReductionCapacity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("year", year);
            if (StringUtils.hasText(orgCode)) {
                wrapper.likeRight("region_code", orgCode.trim());
            }
            long count = communityDisasterReductionCapacityService.count(wrapper);
            if (count == 0) {
                return Result.success(0L);
            }
            boolean result = communityDisasterReductionCapacityService.remove(wrapper);
            return result ? Result.success(count) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除社区减灾能力数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 下载社区行政村减灾能力数据导入模板
     */
    @GetMapping("/template")
    public Result<String> downloadTemplate() {
        log.info("下载社区行政村减灾能力数据导入模板");
        try {
            // 这里可以返回模板文件的下载链接
            String templateUrl = "/templates/community-capacity-template.xlsx";
            return Result.success(templateUrl);
        } catch (Exception e) {
            log.error("下载社区行政村减灾能力数据导入模板失败", e);
            return Result.error("下载模板失败: " + e.getMessage());
        }
    }

    /**
     * 验证GPKG文件字段
     * 检查GPKG文件是否包含社区减灾能力数据所需的必要字段
     */
    @PostMapping("/validate-gpkg")
    public Result<GpkgFieldValidationResult> validateGpkgFile(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(value = "year", required = false) Integer year) {
        log.info("验证社区减灾能力数据GPKG文件: {}", file.getOriginalFilename());
        try {
            GpkgFieldValidationResult result = communityDisasterReductionCapacityService.validateGpkgFields(file, "community", year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证GPKG文件失败", e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 从GPKG文件导入社区减灾能力数据
     */
    @PostMapping("/import-gpkg")
    public Result<Map<String, Object>> importFromGpkg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year) {
        log.info("从GPKG文件导入{}年社区减灾能力数据", year);
        try {
            Map<String, Object> result = communityDisasterReductionCapacityService.importFromGpkg(file, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入GPKG文件失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出社区减灾能力数据
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String ids) {
        try {
            List<Long> idList = null;
            if (ids != null && !ids.trim().isEmpty()) {
                idList = Arrays.stream(ids.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }

            List<CommunityDisasterReductionCapacity> dataList;
            if (idList != null && !idList.isEmpty()) {
                dataList = communityDisasterReductionCapacityService.listByIds(idList);
            } else {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CommunityDisasterReductionCapacity> qw =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                if (year != null) {
                    qw.eq("year", year);
                }
                if (StringUtils.hasText(orgCode)) {
                    qw.likeRight("region_code", orgCode.trim());
                }
                qw.orderByAsc("region_code");
                dataList = communityDisasterReductionCapacityService.list(qw);
            }

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("社区减灾能力数据");

                String[] headers = {
                    "行政区代码", "省名称", "市名称", "县名称", "乡镇名称", "社区(行政村)名称",
                    "应急预案", "弱势人群清单", "地质灾害隐患点清单", "灾害类地图",
                    "常住人口", "资金投入(万元)", "物资价值(万元)", "医疗服务点数",
                    "民兵预备役", "志愿者人数", "培训参与人次", "演练参与人次", "避难场所容量",
                    "数据年份"
                };
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                for (int r = 0; r < dataList.size(); r++) {
                    CommunityDisasterReductionCapacity item = dataList.get(r);
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(r + 1);
                    int c = 0;
                    row.createCell(c++).setCellValue(item.getRegionCode() != null ? item.getRegionCode() : "");
                    row.createCell(c++).setCellValue(item.getProvinceName() != null ? item.getProvinceName() : "");
                    row.createCell(c++).setCellValue(item.getCityName() != null ? item.getCityName() : "");
                    row.createCell(c++).setCellValue(item.getCountyName() != null ? item.getCountyName() : "");
                    row.createCell(c++).setCellValue(item.getTownshipName() != null ? item.getTownshipName() : "");
                    row.createCell(c++).setCellValue(item.getCommunityName() != null ? item.getCommunityName() : "");
                    row.createCell(c++).setCellValue(item.getHasEmergencyPlan() != null ? item.getHasEmergencyPlan() : "");
                    row.createCell(c++).setCellValue(item.getHasVulnerableGroupsList() != null ? item.getHasVulnerableGroupsList() : "");
                    row.createCell(c++).setCellValue(item.getHasDisasterPointsList() != null ? item.getHasDisasterPointsList() : "");
                    row.createCell(c++).setCellValue(item.getHasDisasterMap() != null ? item.getHasDisasterMap() : "");
                    row.createCell(c++).setCellValue(item.getResidentPopulation() != null ? item.getResidentPopulation() : 0);
                    row.createCell(c++).setCellValue(item.getLastYearFundingAmount() != null ? item.getLastYearFundingAmount().doubleValue() : 0);
                    row.createCell(c++).setCellValue(item.getMaterialsEquipmentValue() != null ? item.getMaterialsEquipmentValue().doubleValue() : 0);
                    row.createCell(c++).setCellValue(item.getMedicalServiceCount() != null ? item.getMedicalServiceCount() : 0);
                    row.createCell(c++).setCellValue(item.getMilitiaReserveCount() != null ? item.getMilitiaReserveCount() : 0);
                    row.createCell(c++).setCellValue(item.getRegisteredVolunteerCount() != null ? item.getRegisteredVolunteerCount() : 0);
                    row.createCell(c++).setCellValue(item.getLastYearTrainingParticipants() != null ? item.getLastYearTrainingParticipants() : 0);
                    row.createCell(c++).setCellValue(item.getLastYearDrillParticipants() != null ? item.getLastYearDrillParticipants() : 0);
                    row.createCell(c++).setCellValue(item.getEmergencyShelterCapacity() != null ? item.getEmergencyShelterCapacity() : 0);
                    row.createCell(c++).setCellValue(item.getYear() != null ? item.getYear() : 0);
                }

                workbook.write(bos);
                byte[] bytes = bos.toByteArray();
                String fileName = URLEncoder.encode("社区减灾能力数据.xlsx", StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes);
            }
        } catch (Exception e) {
            log.error("导出社区减灾能力数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
