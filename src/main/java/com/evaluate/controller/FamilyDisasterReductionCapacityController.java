package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.entity.FamilyDisasterReductionCapacity;
import com.evaluate.common.Result;
import com.evaluate.service.IFamilyDisasterReductionCapacityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 家庭减灾能力控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/family-capacity")
public class FamilyDisasterReductionCapacityController {

    @Autowired
    private IFamilyDisasterReductionCapacityService familyDisasterReductionCapacityService;

    /**
     * 分页查询家庭减灾能力数据
     */
    @GetMapping("/list")
    public Result<Page<FamilyDisasterReductionCapacity>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "orgCode", required = false) String orgCode,
            @RequestParam(value = "year", required = false) Integer year) {
        Page<FamilyDisasterReductionCapacity> pageParam = new Page<>(page, size);
        QueryWrapper<FamilyDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(orgCode)) {
            queryWrapper.likeRight("region_code", orgCode);
        }
        if (year != null) {
            queryWrapper.eq("year", year);
        }
        
        Page<FamilyDisasterReductionCapacity> result = familyDisasterReductionCapacityService.page(pageParam, queryWrapper);
        return Result.success(result);
    }

    /**
     * 批量导入家庭减灾能力数据
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importFamilyCapacityData(@RequestParam("file") MultipartFile file) {
        log.info("导入家庭减灾能力数据，文件名: {}", file != null ? file.getOriginalFilename() : "null");
        
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            Map<String, Object> result = familyDisasterReductionCapacityService.importFamilyCapacityData(file);
            if ((Boolean) result.getOrDefault("success", false)) {
                return Result.success(result);
            } else {
                return Result.error((String) result.getOrDefault("message", "导入失败"));
            }
        } catch (Exception e) {
            log.error("导入家庭减灾能力数据异常", e);
            return Result.error("导入异常：" + e.getMessage());
        }
    }

    /**
     * 导出家庭减灾能力数据
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

            List<FamilyDisasterReductionCapacity> dataList;
            if (idList != null && !idList.isEmpty()) {
                dataList = familyDisasterReductionCapacityService.listByIds(idList);
            } else {
                QueryWrapper<FamilyDisasterReductionCapacity> qw = new QueryWrapper<>();
                if (year != null) {
                    qw.eq("year", year);
                }
                if (StringUtils.hasText(orgCode)) {
                    qw.likeRight("region_code", orgCode.trim());
                }
                qw.orderByAsc("region_code");
                dataList = familyDisasterReductionCapacityService.list(qw);
            }

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("家庭减灾能力数据");

                String[] headers = {
                    "行政区代码", "省名称", "市名称", "县名称", "乡镇(街道)", "社区(行政村)",
                    "家庭总人数(人)", "0-10岁人数(人)", "65岁(含)以上人数(人)", "残障人数(人)",
                    "患有慢性病人数(人)", "应急物品", "饮用水储量天数", "食品储量天数",
                    "是否在社区群中", "是否知道联系方式", "收到预警类型",
                    "是否了解避难路线", "演练参与次数", "是否参加过急救培训",
                    "掌握急救方法", "权数"
                };
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                for (int r = 0; r < dataList.size(); r++) {
                    FamilyDisasterReductionCapacity item = dataList.get(r);
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(r + 1);
                    int c = 0;
                    row.createCell(c++).setCellValue(item.getRegionCode() != null ? item.getRegionCode() : "");
                    row.createCell(c++).setCellValue(item.getProvinceName() != null ? item.getProvinceName() : "");
                    row.createCell(c++).setCellValue(item.getCityName() != null ? item.getCityName() : "");
                    row.createCell(c++).setCellValue(item.getCountyName() != null ? item.getCountyName() : "");
                    row.createCell(c++).setCellValue(item.getTownName() != null ? item.getTownName() : "");
                    row.createCell(c++).setCellValue(item.getVillageName() != null ? item.getVillageName() : "");
                    row.createCell(c++).setCellValue(item.getTotalPeople() != null ? item.getTotalPeople() : 0);
                    row.createCell(c++).setCellValue(item.getAge0To10Count() != null ? item.getAge0To10Count() : 0);
                    row.createCell(c++).setCellValue(item.getAge65PlusCount() != null ? item.getAge65PlusCount() : 0);
                    row.createCell(c++).setCellValue(item.getDisabledCount() != null ? item.getDisabledCount() : 0);
                    row.createCell(c++).setCellValue(item.getChronicDiseaseCount() != null ? item.getChronicDiseaseCount() : 0);
                    row.createCell(c++).setCellValue(item.getEmergencySupplies() != null ? item.getEmergencySupplies() : "");
                    row.createCell(c++).setCellValue(item.getWaterReserveDays() != null ? item.getWaterReserveDays() : "");
                    row.createCell(c++).setCellValue(item.getFoodReserveDays() != null ? item.getFoodReserveDays() : "");
                    row.createCell(c++).setCellValue(item.getInCommunityGroup() != null ? item.getInCommunityGroup() : "");
                    row.createCell(c++).setCellValue(item.getKnowStaffContact() != null ? item.getKnowStaffContact() : "");
                    row.createCell(c++).setCellValue(item.getReceivedWarningTypes() != null ? item.getReceivedWarningTypes() : "");
                    row.createCell(c++).setCellValue(item.getKnowEvacuationRoute() != null ? item.getKnowEvacuationRoute() : "");
                    row.createCell(c++).setCellValue(item.getDrillParticipationCount() != null ? item.getDrillParticipationCount() : "");
                    row.createCell(c++).setCellValue(item.getFirstAidTraining() != null ? item.getFirstAidTraining() : "");
                    row.createCell(c++).setCellValue(item.getMasteredFirstAidSkills() != null ? item.getMasteredFirstAidSkills() : "");
                    row.createCell(c++).setCellValue(item.getWeight() != null ? item.getWeight().doubleValue() : 0);
                }

                workbook.write(bos);
                byte[] bytes = bos.toByteArray();
                String fileName = URLEncoder.encode("家庭减灾能力数据.xlsx", StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes);
            }
        } catch (Exception e) {
            log.error("导出家庭减灾能力数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
