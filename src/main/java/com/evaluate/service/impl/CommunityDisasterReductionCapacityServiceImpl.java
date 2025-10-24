package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.mapper.CommunityDisasterReductionCapacityMapper;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import com.evaluate.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * 社区行政村减灾能力服务实现类
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class CommunityDisasterReductionCapacityServiceImpl
        extends ServiceImpl<CommunityDisasterReductionCapacityMapper, CommunityDisasterReductionCapacity>
        implements ICommunityDisasterReductionCapacityService {

    @Autowired
    private CommunityDisasterReductionCapacityMapper communityDisasterReductionCapacityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCommunityCapacityData(MultipartFile file) {
        log.info("开始导入社区行政村减灾能力数据，文件名: {}", file.getOriginalFilename());

        Map<String, Object> result = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            // 验证文件格式
            if (!ExcelUtil.isExcel(file)) {
                throw new RuntimeException("请上传Excel文件(.xlsx或.xls)");
            }

            // 读取Excel文件
            List<Map<String, Object>> dataList = readExcelData(file, errorMessages);
            log.info("从Excel中读取到 {} 条数据", dataList.size());

            // 批量保存数据
            for (Map<String, Object> data : dataList) {
                try {
                    CommunityDisasterReductionCapacity entity = convertToEntity(data);

                    // 检查是否已存在相同的数据
                    CommunityDisasterReductionCapacity existing = getByRegionAndCommunity(
                            entity.getRegionCode(), entity.getCommunityName());

                    if (existing != null) {
                        // 更新现有数据
                        entity.setId(existing.getId());
                        updateById(entity);
                        log.debug("更新社区减灾能力数据: {} - {}", entity.getRegionCode(), entity.getCommunityName());
                    } else {
                        // 插入新数据
                        save(entity);
                        log.debug("新增社区减灾能力数据: {} - {}", entity.getRegionCode(), entity.getCommunityName());
                    }
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("处理第%d行数据失败: %s", successCount + errorCount + 1, e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            result.put("success", true);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorMessages", errorMessages);
            result.put("message", String.format("导入完成，成功 %d 条，失败 %d 条", successCount, errorCount));

            log.info("社区行政村减灾能力数据导入完成，成功: {}, 失败: {}", successCount, errorCount);

        } catch (Exception e) {
            log.error("导入社区行政村减灾能力数据失败", e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            result.put("errorMessages", Arrays.asList(e.getMessage()));
        }

        return result;
    }

    @Override
    public Map<String, Object> getCommunityCapacityList(Integer page, Integer size, String regionCode, String communityName) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<CommunityDisasterReductionCapacity> pageParam = new Page<>(page, size);
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();

            if (regionCode != null && !regionCode.trim().isEmpty()) {
                queryWrapper.like("region_code", regionCode);
            }
            if (communityName != null && !communityName.trim().isEmpty()) {
                queryWrapper.like("community_name", communityName);
            }

            queryWrapper.orderByDesc("create_time");

            IPage<CommunityDisasterReductionCapacity> pageResult = page(pageParam, queryWrapper);

            result.put("success", true);
            result.put("data", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", pageResult.getPages());

        } catch (Exception e) {
            log.error("查询社区行政村减灾能力数据列表失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            return removeById(id);
        } catch (Exception e) {
            log.error("删除社区行政村减灾能力数据失败，ID: {}", id, e);
            throw new RuntimeException("删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        try {
            return removeByIds(ids);
        } catch (Exception e) {
            log.error("批量删除社区行政村减灾能力数据失败，IDs: {}", ids, e);
            throw new RuntimeException("批量删除失败: " + e.getMessage());
        }
    }

    @Override
    public CommunityDisasterReductionCapacity getByRegionAndCommunity(String regionCode, String communityName) {
        try {
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("region_code", regionCode)
                       .eq("community_name", communityName);
            return getOne(queryWrapper);
        } catch (Exception e) {
            log.error("根据行政区代码和社区名称查询数据失败: regionCode={}, communityName={}",
                     regionCode, communityName, e);
            return null;
        }
    }

    @Override
    public List<CommunityDisasterReductionCapacity> searchCommunityCapacity(String keyword, String regionCode, String communityName) {
        try {
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();

            // 关键词搜索：社区名称、乡镇名称、县名称、市名称、省名称
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = keyword.trim();
                queryWrapper.and(wrapper -> wrapper
                    .like("community_name", searchKeyword)
                    .or().like("township_name", searchKeyword)
                    .or().like("county_name", searchKeyword)
                    .or().like("city_name", searchKeyword)
                    .or().like("province_name", searchKeyword)
                    .or().like("region_code", searchKeyword)
                );
            }

            // 行政区代码精确匹配
            if (regionCode != null && !regionCode.trim().isEmpty()) {
                queryWrapper.like("region_code", regionCode.trim());
            }

            // 社区名称模糊匹配
            if (communityName != null && !communityName.trim().isEmpty()) {
                queryWrapper.like("community_name", communityName.trim());
            }

            queryWrapper.orderByDesc("create_time");

            return list(queryWrapper);
        } catch (Exception e) {
            log.error("搜索社区行政村减灾能力数据失败: keyword={}, regionCode={}, communityName={}",
                     keyword, regionCode, communityName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 读取Excel数据
     */
    private List<Map<String, Object>> readExcelData(MultipartFile file, List<String> errorMessages) throws Exception {
        List<Map<String, Object>> dataList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 获取标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件没有标题行");
            }

            // 读取数据行
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Map<String, Object> data = new HashMap<>();

                // 根据列索引读取数据
                data.put("regionCode", getCellStringValue(row.getCell(0)));
                data.put("provinceName", getCellStringValue(row.getCell(1)));
                data.put("cityName", getCellStringValue(row.getCell(2)));
                data.put("countyName", getCellStringValue(row.getCell(3)));
                data.put("townshipName", getCellStringValue(row.getCell(4)));
                data.put("communityName", getCellStringValue(row.getCell(5)));
                data.put("hasEmergencyPlan", getCellStringValue(row.getCell(6)));
                data.put("hasVulnerableGroupsList", getCellStringValue(row.getCell(7)));
                data.put("hasDisasterPointsList", getCellStringValue(row.getCell(8)));
                data.put("hasDisasterMap", getCellStringValue(row.getCell(9)));
                data.put("residentPopulation", getCellNumericValue(row.getCell(10)));
                data.put("lastYearFundingAmount", getCellDecimalValue(row.getCell(11)));
                data.put("materialsEquipmentValue", getCellDecimalValue(row.getCell(12)));
                data.put("medicalServiceCount", getCellNumericValue(row.getCell(13)));
                data.put("militiaReserveCount", getCellNumericValue(row.getCell(14)));
                data.put("registeredVolunteerCount", getCellNumericValue(row.getCell(15)));
                data.put("lastYearTrainingParticipants", getCellNumericValue(row.getCell(16)));
                data.put("lastYearDrillParticipants", getCellNumericValue(row.getCell(17)));
                data.put("emergencyShelterCapacity", getCellNumericValue(row.getCell(18)));

                // 验证必填字段
                if (data.get("regionCode") == null || data.get("regionCode").toString().trim().isEmpty()) {
                    errorMessages.add(String.format("第%d行：行政区代码不能为空", rowIndex + 1));
                    continue;
                }
                if (data.get("communityName") == null || data.get("communityName").toString().trim().isEmpty()) {
                    errorMessages.add(String.format("第%d行：社区（行政村）名称不能为空", rowIndex + 1));
                    continue;
                }

                dataList.add(data);
            }
        }

        return dataList;
    }

    /**
     * 转换为实体对象
     */
    private CommunityDisasterReductionCapacity convertToEntity(Map<String, Object> data) {
        CommunityDisasterReductionCapacity entity = new CommunityDisasterReductionCapacity();

        entity.setRegionCode(getStringValue(data.get("regionCode")));
        entity.setProvinceName(getStringValue(data.get("provinceName")));
        entity.setCityName(getStringValue(data.get("cityName")));
        entity.setCountyName(getStringValue(data.get("countyName")));
        entity.setTownshipName(getStringValue(data.get("townshipName")));
        entity.setCommunityName(getStringValue(data.get("communityName")));
        entity.setHasEmergencyPlan(normalizeYesNo(getStringValue(data.get("hasEmergencyPlan"))));
        entity.setHasVulnerableGroupsList(normalizeYesNo(getStringValue(data.get("hasVulnerableGroupsList"))));
        entity.setHasDisasterPointsList(normalizeYesNo(getStringValue(data.get("hasDisasterPointsList"))));
        entity.setHasDisasterMap(normalizeYesNo(getStringValue(data.get("hasDisasterMap"))));
        entity.setResidentPopulation(getIntegerValue(data.get("residentPopulation")));
        entity.setLastYearFundingAmount(getDecimalValue(data.get("lastYearFundingAmount")));
        entity.setMaterialsEquipmentValue(getDecimalValue(data.get("materialsEquipmentValue")));
        entity.setMedicalServiceCount(getIntegerValue(data.get("medicalServiceCount")));
        entity.setMilitiaReserveCount(getIntegerValue(data.get("militiaReserveCount")));
        entity.setRegisteredVolunteerCount(getIntegerValue(data.get("registeredVolunteerCount")));
        entity.setLastYearTrainingParticipants(getIntegerValue(data.get("lastYearTrainingParticipants")));
        entity.setLastYearDrillParticipants(getIntegerValue(data.get("lastYearDrillParticipants")));
        entity.setEmergencyShelterCapacity(getIntegerValue(data.get("emergencyShelterCapacity")));

        return entity;
    }

    // 辅助方法
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return cell.getBooleanCellValue() ? "是" : "否";
            default: return "";
        }
    }

    private Integer getCellNumericValue(Cell cell) {
        if (cell == null) return 0;
        try {
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            String value = getCellStringValue(cell);
            if (value.isEmpty()) return 0;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }

    private BigDecimal getCellDecimalValue(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
            String value = getCellStringValue(cell);
            if (value.isEmpty()) return BigDecimal.ZERO;
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException ex) {
                return BigDecimal.ZERO;
            }
        }
    }

    private String getStringValue(Object value) {
        return value != null ? value.toString().trim() : "";
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal getDecimalValue(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String normalizeYesNo(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "否";
        }
        String normalized = value.trim();
        return normalized.equals("是") || normalized.equalsIgnoreCase("yes") ||
               normalized.equals("1") || normalized.equalsIgnoreCase("true") ? "是" : "否";
    }
}