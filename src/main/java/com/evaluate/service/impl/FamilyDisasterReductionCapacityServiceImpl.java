package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.FamilyDisasterReductionCapacity;
import com.evaluate.mapper.FamilyDisasterReductionCapacityMapper;
import com.evaluate.service.IFamilyDisasterReductionCapacityService;
import com.evaluate.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * 家庭减灾能力服务实现类
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class FamilyDisasterReductionCapacityServiceImpl
        extends ServiceImpl<FamilyDisasterReductionCapacityMapper, FamilyDisasterReductionCapacity>
        implements IFamilyDisasterReductionCapacityService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFamilyCapacityData(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", "上传文件为空");
            return result;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.put("success", false);
                result.put("message", "Excel文件中没有工作表");
                return result;
            }

            // 构建列名映射
            Map<String, Integer> colMap = buildColumnMap(sheet);
            if (colMap.isEmpty()) {
                result.put("success", false);
                result.put("message", "无法识别Excel表头，请检查文件格式");
                return result;
            }

            List<FamilyDisasterReductionCapacity> dataList = new ArrayList<>();
            List<String> errorMessages = new ArrayList<>();
            int successCount = 0;
            int insertCount = 0;
            int updateCount = 0;

            // 从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    FamilyDisasterReductionCapacity data = parseRow(row, colMap);
                    
                    // 简单校验
                    if (data == null || !StringUtils.hasText(data.getRegionCode())) {
                        continue;
                    }

                    dataList.add(data);
                } catch (Exception e) {
                    log.error("解析第{}行数据失败", i + 1, e);
                    errorMessages.add("第" + (i + 1) + "行解析失败: " + e.getMessage());
                }
            }

            // 保存数据
            for (FamilyDisasterReductionCapacity data : dataList) {
                try {
                    // 可以根据regionCode和其他条件判断是否已存在
                    // 假设家庭数据以regionCode等信息作为判断，或者这里只是简单插入/更新
                    // 由于没有明显的唯一标识（除非每行有一个主键），这里简单根据regionCode尝试覆盖（这可能不准确）
                    // 在真实的业务场景中，家庭调查问卷可能每次导入都全量新增，或者通过unique id判断
                    // 考虑到题目没明确uniqueId，我们这里直接执行新增
                    save(data);
                    insertCount++;
                } catch (Exception e) {
                    log.error("保存数据失败，regionCode={}", data.getRegionCode(), e);
                    errorMessages.add("保存regionCode为 " + data.getRegionCode() + " 的数据失败");
                }
            }

            result.put("success", true);
            result.put("successCount", insertCount + updateCount);
            result.put("insertCount", insertCount);
            result.put("updateCount", updateCount);
            if (!errorMessages.isEmpty()) {
                result.put("errorMessages", errorMessages);
            }
            return result;

        } catch (Exception e) {
            log.error("导入家庭减灾能力数据失败", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
            return result;
        }
    }

    private Map<String, Integer> buildColumnMap(Sheet sheet) {
        Map<String, Integer> colMap = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return colMap;

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String header = ExcelUtil.getCellStringValue(cell);
                if (header != null) {
                    header = header.trim();
                    if (header.contains("行政区代码")) colMap.put("regionCode", i);
                    else if (header.contains("省名称")) colMap.put("provinceName", i);
                    else if (header.contains("市名称")) colMap.put("cityName", i);
                    else if (header.contains("县名称")) colMap.put("countyName", i);
                    else if (header.contains("乡镇") || header.contains("街道")) colMap.put("townName", i);
                    else if (header.contains("社区") || header.contains("行政村")) colMap.put("villageName", i);
                    else if (header.contains("0-10岁")) colMap.put("age0To10Count", i);
                    else if (header.contains("65岁")) colMap.put("age65PlusCount", i);
                    else if (header.contains("残障人数")) colMap.put("disabledCount", i);
                    else if (header.contains("家庭总人数")) colMap.put("totalPeople", i);
                    else if (header.contains("慢性病")) colMap.put("chronicDiseaseCount", i);
                    else if (header.contains("应急物品")) colMap.put("emergencySupplies", i);
                    else if (header.contains("因灾断水")) colMap.put("waterReserveDays", i);
                    else if (header.contains("供给食物")) colMap.put("foodReserveDays", i);
                    else if (header.contains("微信群或QQ群")) colMap.put("inCommunityGroup", i);
                    else if (header.contains("联系方式")) colMap.put("knowStaffContact", i);
                    else if (header.contains("预警信息")) colMap.put("receivedWarningTypes", i);
                    else if (header.contains("紧急避难路线")) colMap.put("knowEvacuationRoute", i);
                    else if (header.contains("应急演练")) colMap.put("drillParticipationCount", i);
                    else if (header.contains("急救培训")) colMap.put("firstAidTraining", i);
                    else if (header.contains("急救方法")) colMap.put("masteredFirstAidSkills", i);
                    else if (header.contains("权数")) colMap.put("weight", i);
                }
            }
        }
        return colMap;
    }

    private FamilyDisasterReductionCapacity parseRow(Row row, Map<String, Integer> colMap) {
        FamilyDisasterReductionCapacity data = new FamilyDisasterReductionCapacity();

        data.setRegionCode(getStringValue(row, colMap, "regionCode"));
        data.setProvinceName(getStringValue(row, colMap, "provinceName"));
        data.setCityName(getStringValue(row, colMap, "cityName"));
        data.setCountyName(getStringValue(row, colMap, "countyName"));
        data.setTownName(getStringValue(row, colMap, "townName"));
        data.setVillageName(getStringValue(row, colMap, "villageName"));

        data.setAge0To10Count(getIntegerValue(row, colMap, "age0To10Count"));
        data.setAge65PlusCount(getIntegerValue(row, colMap, "age65PlusCount"));
        data.setDisabledCount(getIntegerValue(row, colMap, "disabledCount"));
        data.setTotalPeople(getIntegerValue(row, colMap, "totalPeople"));
        data.setChronicDiseaseCount(getIntegerValue(row, colMap, "chronicDiseaseCount"));

        data.setEmergencySupplies(normalizeJsonArray(getStringValue(row, colMap, "emergencySupplies")));
        data.setWaterReserveDays(getStringValue(row, colMap, "waterReserveDays"));
        data.setFoodReserveDays(getStringValue(row, colMap, "foodReserveDays"));
        data.setInCommunityGroup(getStringValue(row, colMap, "inCommunityGroup"));
        data.setKnowStaffContact(getStringValue(row, colMap, "knowStaffContact"));
        data.setReceivedWarningTypes(normalizeJsonArray(getStringValue(row, colMap, "receivedWarningTypes")));
        data.setKnowEvacuationRoute(getStringValue(row, colMap, "knowEvacuationRoute"));
        data.setDrillParticipationCount(getStringValue(row, colMap, "drillParticipationCount"));
        data.setFirstAidTraining(getStringValue(row, colMap, "firstAidTraining"));
        data.setMasteredFirstAidSkills(normalizeJsonArray(getStringValue(row, colMap, "masteredFirstAidSkills")));

        data.setWeight(getBigDecimalValue(row, colMap, "weight"));

        return data;
    }

    private String getStringValue(Row row, Map<String, Integer> colMap, String key) {
        Integer idx = colMap.get(key);
        if (idx == null) return null;
        return ExcelUtil.getCellStringValue(row.getCell(idx));
    }

    private Integer getIntegerValue(Row row, Map<String, Integer> colMap, String key) {
        String valStr = getStringValue(row, colMap, key);
        if (!StringUtils.hasText(valStr)) return 0;
        try {
            return (int) Double.parseDouble(valStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal getBigDecimalValue(Row row, Map<String, Integer> colMap, String key) {
        String valStr = getStringValue(row, colMap, key);
        if (!StringUtils.hasText(valStr)) return BigDecimal.ZERO;
        try {
            return new BigDecimal(valStr.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将可能为逗号/顿号分隔的文本或者原本就是 JSON 的字符串格式化为标准 JSON 数组
     */
    private String normalizeJsonArray(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "[]";
        }
        try {
            if (text.trim().startsWith("[")) {
                // 已经是合法的 JSON 数组，直接返回
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(text);
                return text;
            }
        } catch (Exception e) {
            // 继续往下解析
        }

        // 替换中文逗号、顿号、分号为英文逗号
        String normalized = text.replaceAll("[，、；;]", ",");
        String[] items = Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (items.length == 0) {
            return "[]";
        }

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }
}
