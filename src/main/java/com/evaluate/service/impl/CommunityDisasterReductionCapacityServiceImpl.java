package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.mapper.CommunityDisasterReductionCapacityMapper;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import com.evaluate.service.IOrganizationService;
import com.evaluate.util.ExcelUtil;
import com.evaluate.util.GpkgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private IOrganizationService organizationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCommunityCapacityData(MultipartFile file, Integer year) {
        log.info("开始导入社区行政村减灾能力数据，文件名: {}, 年份: {}", file.getOriginalFilename(), year);

        Map<String, Object> result = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            // 验证年份参数
            if (year == null) {
                throw new RuntimeException("年份参数不能为空");
            }

            // 验证文件格式
            if (!ExcelUtil.isExcel(file)) {
                throw new RuntimeException("请上传Excel文件(.xlsx或.xls)");
            }

            // 读取Excel文件（使用新的解析逻辑）
            List<CommunityDisasterReductionCapacity> dataList = readExcelDataWithNewFormat(file, year, errorMessages);
            log.info("从Excel中读取到 {} 条数据", dataList.size());

            // 批量保存数据
            for (CommunityDisasterReductionCapacity entity : dataList) {
                try {
                    // 检查是否已存在相同地区代码、社区名称和年份的数据
                    CommunityDisasterReductionCapacity existing = getByRegionAndCommunityAndYear(
                            entity.getRegionCode(), entity.getCommunityName(), entity.getYear());

                    if (existing != null) {
                        // 更新现有数据（相同地区代码、社区名称和年份）
                        entity.setId(existing.getId());
                        updateById(entity);
                        log.debug("更新社区减灾能力数据: {} - {} ({}年)", entity.getRegionCode(), entity.getCommunityName(), entity.getYear());
                    } else {
                        // 插入新数据（新的年份或新的地区）
                        entity.setId(null);
                        save(entity);
                        log.debug("新增社区减灾能力数据: {} - {} ({}年)", entity.getRegionCode(), entity.getCommunityName(), entity.getYear());
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
    public Map<String, Object> getCommunityCapacityList(Integer page, Integer size, String regionCode, String communityName, Integer year) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<CommunityDisasterReductionCapacity> pageParam = new Page<>(page, size);
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();

            if (year != null) {
                queryWrapper.eq("year", year);
            }
            if (regionCode != null && !regionCode.trim().isEmpty()) {
                queryWrapper.likeRight("region_code", regionCode.trim());
            }
            if (communityName != null && !communityName.trim().isEmpty()) {
                queryWrapper.like("community_name", communityName);
            }

            queryWrapper.orderByDesc("create_time");

            IPage<CommunityDisasterReductionCapacity> pageResult = page(pageParam, queryWrapper);

            // 返回与乡镇数据一致的分页格式
            result.put("success", true);
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("current", pageResult.getCurrent());
            result.put("pages", pageResult.getPages());
            result.put("size", pageResult.getSize());

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

    /**
     * 根据行政区代码、社区名称和年份获取数据
     * 用于智能导入时判断是否存在
     */
    public CommunityDisasterReductionCapacity getByRegionAndCommunityAndYear(String regionCode, String communityName, Integer year) {
        try {
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("region_code", regionCode)
                       .eq("community_name", communityName)
                       .eq("year", year);
            return getOne(queryWrapper);
        } catch (Exception e) {
            log.error("根据行政区代码、社区名称和年份查询数据失败: regionCode={}, communityName={}, year={}",
                     regionCode, communityName, year, e);
            return null;
        }
    }

    @Override
    public List<CommunityDisasterReductionCapacity> searchCommunityCapacity(String keyword, String regionCode, String communityName, Integer year) {
        try {
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();

            // 年份过滤
            if (year != null) {
                queryWrapper.eq("year", year);
            }

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
            log.error("搜索社区行政村减灾能力数据失败: keyword={}, regionCode={}, communityName={}, year={}",
                     keyword, regionCode, communityName, year, e);
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
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 使用 NumberToTextConverter 避免科学计数法
                // 这对于地区编码等长数字字符串非常重要
                String numStr = NumberToTextConverter.toText(cell.getNumericCellValue());
                // 如果是整数，去掉小数点和后面的0
                if (numStr.contains(".")) {
                    try {
                        double d = Double.parseDouble(numStr);
                        if (d == Math.floor(d)) {
                            return String.valueOf((long) d);
                        }
                    } catch (NumberFormatException e) {
                        // 如果转换失败，直接返回原始字符串
                    }
                }
                return numStr.trim();
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "是" : "否";
            default:
                return "";
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

    /**
     * 使用新格式读取Excel数据（跳过前两行表头）
     */
    private List<CommunityDisasterReductionCapacity> readExcelDataWithNewFormat(MultipartFile file, Integer year, List<String> errorMessages) throws Exception {
        List<CommunityDisasterReductionCapacity> dataList = new ArrayList<>();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            // 跳过前两行表头，从第三行开始读取数据
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                CommunityDisasterReductionCapacity data = parseRowToCommunityData(row);
                if (data != null) {
                    // 设置年份
                    data.setYear(year);
                    dataList.add(data);
                } else {
                    errorMessages.add(String.format("第%d行：数据解析失败", i + 1));
                }
            }

            return dataList;
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 解析Excel行数据为CommunityDisasterReductionCapacity对象
     * 跳过前两行表头，从第3行开始解析数据
     */
    private CommunityDisasterReductionCapacity parseRowToCommunityData(org.apache.poi.ss.usermodel.Row row) {
        try {
            CommunityDisasterReductionCapacity data = new CommunityDisasterReductionCapacity();

            // 第0列：唯一码
            data.setUniqueId(getCellStringValue(row.getCell(0)));

            // 第1列：核实状态
            data.setVerificationStatus(getCellStringValue(row.getCell(1)));

            // 第2列：社区（行政村）名称
            data.setCommunityName(getCellStringValue(row.getCell(2)));

            // 第3列：社区（行政村）地址
            String communityAddress = getCellStringValue(row.getCell(3));
            data.setCommunityAddress(communityAddress);

            // 解析省市县信息
            parseAddressToProvinceCityCounty(data, communityAddress);

            // 第4列：行政区划代码
            data.setRegionCode(getCellStringValue(row.getCell(4)));

            // 第5列：总户数
            data.setTotalHouseholds(getCellIntegerValue(row.getCell(5)));

            // 第6列：常住人口数量
            data.setResidentPopulation(getCellIntegerValue(row.getCell(6)));

            // 第7列：0-14岁人数
            data.setAge0To14Count(getCellIntegerValue(row.getCell(7)));

            // 第8列：65岁（含）以上人数
            data.setAge65PlusCount(getCellIntegerValue(row.getCell(8)));

            // 第9列：残障人员人数
            data.setDisabledPersonCount(getCellIntegerValue(row.getCell(9)));

            // 第10列：社区医疗卫生服务站或村卫生室数量
            data.setMedicalServiceCount(getCellIntegerValue(row.getCell(10)));

            // 第11列：是否为全国综合减灾示范社区
            data.setIsNationalDemoCommunity(normalizeYesNo(getCellStringValue(row.getCell(11))));

            // 第12列：是否为省级综合减灾示范社区
            data.setIsProvincialDemoCommunity(normalizeYesNo(getCellStringValue(row.getCell(12))));

            // 第13列：是否有本辖区地质灾害等隐患点清单
            data.setHasDisasterPointsList(normalizeYesNo(getCellStringValue(row.getCell(13))));

            // 第14列：是否有本辖区弱势人群清单
            data.setHasVulnerableGroupsList(normalizeYesNo(getCellStringValue(row.getCell(14))));

            // 第15列：是否有社区（行政村）灾害类地图
            data.setHasDisasterMap(normalizeYesNo(getCellStringValue(row.getCell(15))));

            // 第16列：是否有社区（行政村）应急预案
            data.setHasEmergencyPlan(normalizeYesNo(getCellStringValue(row.getCell(16))));

            // 第17列：上一年度防灾减灾救灾资金投入总金额
            BigDecimal fundingAmount = getCellBigDecimalValue(row.getCell(17));
            if (fundingAmount != null) {
                data.setLastYearFundingAmount(fundingAmount);
            }

            // 第18列：灾害信息员人数
            data.setDisasterInfoStaffCount(getCellIntegerValue(row.getCell(18)));

            // 第19列：登记注册志愿者人数
            data.setRegisteredVolunteerCount(getCellIntegerValue(row.getCell(19)));

            // 第20列：民兵预备役人数
            data.setMilitiaReserveCount(getCellIntegerValue(row.getCell(20)));

            // 第21列：本级灾害应急避难场所数量
            data.setEmergencyShelterCount(getCellIntegerValue(row.getCell(21)));

            // 第22列：本级灾害应急避难场所容量
            data.setEmergencyShelterCapacity(getCellIntegerValue(row.getCell(22)));

            // 第23列：防灾减灾应急物资储备方式
            data.setMaterialStorageMethod(getCellStringValue(row.getCell(23)));

            // 第24列：防灾减灾应急物资储备方式-其他项说明
            data.setMaterialStorageMethodOther(getCellStringValue(row.getCell(24)));

            // 第25列：现有储备物资、装备折合金额（实物储备时填写）
            BigDecimal materialValue = getCellBigDecimalValue(row.getCell(25));
            if (materialValue != null) {
                data.setMaterialsEquipmentValue(materialValue);
            }

            // 第26列：灾害预警信息接收方式
            data.setWarningReceiveMethod(getCellStringValue(row.getCell(26)));

            // 第27列：灾害预警信息接收方式-其他项说明
            data.setWarningReceiveMethodOther(getCellStringValue(row.getCell(27)));

            // 第28列：灾害预警信息传达方式
            data.setWarningCommunicationMethod(getCellStringValue(row.getCell(28)));

            // 第29列：灾害预警信息传达方式-其他项说明
            data.setWarningCommunicationMethodOther(getCellStringValue(row.getCell(29)));

            // 第30列：灾情信息上报方式
            data.setDisasterReportMethod(getCellStringValue(row.getCell(30)));

            // 第31列：灾情信息上报方式-其他项说明
            data.setDisasterReportMethodOther(getCellStringValue(row.getCell(31)));

            // 第32列：上一年度组织的防灾减灾培训活动次数
            data.setLastYearTrainingCount(getCellIntegerValue(row.getCell(32)));

            // 第33列：上一年度防灾减灾培训活动培训人次
            data.setLastYearTrainingParticipants(getCellIntegerValue(row.getCell(33)));

            // 第34列：上一年度组织的防灾减灾演练活动次数
            data.setLastYearDrillCount(getCellIntegerValue(row.getCell(34)));

            // 第35列：参与上一年度组织的防灾减灾演练活动的居民人次
            data.setLastYearDrillParticipants(getCellIntegerValue(row.getCell(35)));

            // 第36列：单位负责人
            data.setUnitLeader(getCellStringValue(row.getCell(36)));

            // 第37列：统计负责人
            data.setStatisticsLeader(getCellStringValue(row.getCell(37)));

            // 第38列：填表人
            data.setFormFiller(getCellStringValue(row.getCell(38)));

            // 第39列：联系电话
            data.setContactPhone(getCellStringValue(row.getCell(39)));

            // 第40列：报出日期
            org.apache.poi.ss.usermodel.Cell reportDateCell = row.getCell(40);
            if (reportDateCell != null) {
                try {
                    if (reportDateCell.getCellType() == CellType.STRING) {
                        String dateStr = reportDateCell.getStringCellValue();
                        if (dateStr != null && !dateStr.trim().isEmpty()) {
                            data.setReportDate(LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                        }
                    } else if (reportDateCell.getCellType() == CellType.NUMERIC) {
                        data.setReportDate(reportDateCell.getLocalDateTimeCellValue().toLocalDate());
                    }
                } catch (Exception e) {
                    log.warn("解析报出日期失败: {}", e.getMessage());
                }
            }

            // 第41列：填写说明
            data.setFillInstructions(getCellStringValue(row.getCell(41)));

            return data;
        } catch (Exception e) {
            log.error("解析Excel行数据失败", e);
            return null;
        }
    }

    /**
     * 解析地址字符串，提取省、市、县信息
     */
    private void parseAddressToProvinceCityCounty(CommunityDisasterReductionCapacity data, String address) {
        if (address == null || address.trim().isEmpty()) {
            return;
        }

        try {
            String addr = address.trim();

            // 省级行政区匹配模式
            String[] provincePatterns = {
                "(北京|天津|上海|重庆)", // 直辖市
                "(河北省|山西省|辽宁省|吉林省|黑龙江省|江苏省|浙江省|安徽省|福建省|江西省|山东省|河南省|湖北省|湖南省|广东省|海南省|四川省|贵州省|云南省|陕西省|甘肃省|青海省|台湾省)", // 省
                "(内蒙古自治区|广西壮族自治区|西藏自治区|宁夏回族自治区|新疆维吾尔自治区)", // 自治区
                "(香港特别行政区|澳门特别行政区)", // 特别行政区
                "(.+省)" // 通用省份模式
            };

            // 市级行政区匹配模式
            String[] cityPatterns = {
                "(.*市)", // 地级市
                "(.*自治州)", // 自治州
                "(.*地区)", // 地区
                "(.*盟)" // 盟
            };

            // 县级行政区匹配模式
            String[] countyPatterns = {
                "(.*县)",
                "(.*区)",
                "(.*县级市)",
                "(.*自治县)",
                "(.*旗)",
                "(.*自治旗)"
            };

            String province = null;
            String city = null;
            String county = null;
            String remaining = addr;

            // 提取省份
            for (String pattern : provincePatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(remaining);
                if (m.find()) {
                    province = m.group(1);
                    // 如果是直辖市，省级和市级可以合并处理
                    if (province.matches("(北京|天津|上海|重庆)")) {
                        city = province + "市";
                        remaining = remaining.replaceFirst(province, "");
                        break;
                    }
                    remaining = remaining.replaceFirst(province, "");
                    break;
                }
            }

            // 提取市级
            for (String pattern : cityPatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(remaining);
                if (m.find()) {
                    city = m.group(1);
                    remaining = remaining.replaceFirst(city, "");
                    break;
                }
            }

            // 提取县级
            for (String pattern : countyPatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(remaining);
                if (m.find()) {
                    county = m.group(1);
                    break;
                }
            }

            // 设置解析结果
            if (province != null) {
                data.setProvinceName(province);
            }
            if (city != null) {
                data.setCityName(city);
            }
            if (county != null) {
                data.setCountyName(county);
            }

            // 尝试从地址中提取乡镇名称
            String townshipName = extractTownshipName(remaining);
            if (townshipName != null) {
                data.setTownshipName(townshipName);
            }

            log.debug("地址解析结果 - 省份: {}, 城市: {}, 县: {}, 乡镇: {}, 原地址: {}",
                province, city, county, townshipName, address);

        } catch (Exception e) {
            log.warn("地址解析失败: {}, 地址: {}", e.getMessage(), address);
        }
    }

    /**
     * 从剩余地址中提取乡镇名称（去掉前面的县名称）
     */
    private String extractTownshipName(String remaining) {
        if (remaining == null || remaining.trim().isEmpty()) {
            return null;
        }

        // 乡镇名称匹配模式
        String[] townshipPatterns = {
            "(.*?镇)",
            "(.*?乡)",
            "(.*?街道)",
            "(.*?办事处)"
        };

        for (String pattern : townshipPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(remaining);
            if (m.find()) {
                String townshipName = m.group(1);

                // 去掉前面可能包含的县名称
                // 例如："青神县西龙镇" → "西龙镇"
                townshipName = townshipName.replaceAll("^.*?(县|区|县级市|市|旗)", "");

                // 再次匹配，确保只获取乡镇部分
                java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("(.*?镇|.*?乡|.*?街道|.*?办事处)");
                java.util.regex.Matcher m2 = p2.matcher(townshipName);
                if (m2.find()) {
                    return m2.group(1);
                }

                return townshipName;
            }
        }

        return null;
    }

    /**
     * 获取单元格整数值（支持字符串格式的数字）
     */
    private Integer getCellIntegerValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) {
                        return null;
                    }
                    return Integer.parseInt(value.replaceAll("[,，]", ""));
                case FORMULA:
                    return (int) cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("解析整数值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格BigDecimal值（支持字符串格式的数字）
     */
    private BigDecimal getCellBigDecimalValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) {
                        return null;
                    }
                    return new BigDecimal(value.replaceAll("[,，]", ""));
                case FORMULA:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("解析BigDecimal值失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public GpkgFieldValidationResult validateGpkgFields(MultipartFile file, String dataType) {
        return GpkgUtil.validateGpkgFields(file, dataType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromGpkg(MultipartFile file, Integer year) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (file == null || file.isEmpty() || year == null) {
            result.put("message", "导入参数为空");
            return result;
        }

        Path tempFile = null;
        try {
            // 创建临时文件
            tempFile = Files.createTempFile("gpkg_", ".gpkg");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 创建数据存储
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "geopkg");
            params.put("database", tempFile.toAbsolutePath().toString());

            DataStore dataStore = DataStoreFinder.getDataStore(params);
            if (dataStore == null) {
                result.put("message", "无法读取GPKG文件");
                return result;
            }

            try {
                // 获取类型名称
                String[] typeNames = dataStore.getTypeNames();
                if (typeNames == null || typeNames.length == 0) {
                    result.put("message", "GPKG文件中没有找到任何图层");
                    return result;
                }

                // 使用第一个图层
                String layerName = typeNames[0];
                log.info("使用图层: {}", layerName);

                // 获取要素源
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource =
                        dataStore.getFeatureSource(layerName);

                // 获取字段映射
                Map<String, String> fieldMapping = GpkgUtil.getFieldMapping("community");

                // 读取所有要素
                Query query = new Query(layerName);
                FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures(query);

                List<CommunityDisasterReductionCapacity> dataList = new ArrayList<>();
                try (FeatureIterator<SimpleFeature> features = collection.features()) {
                    while (features.hasNext()) {
                        SimpleFeature feature = features.next();
                        CommunityDisasterReductionCapacity data = parseGpkgFeatureToCommunityCapacity(feature, fieldMapping, year);
                        if (data != null) {
                            dataList.add(data);
                        }
                    }
                }

                log.info("从GPKG文件解析到{}条社区减灾能力数据", dataList.size());
                result.put("totalCount", dataList.size());

                // 批量保存
                int insertCount = 0;
                int updateCount = 0;

                for (CommunityDisasterReductionCapacity item : dataList) {
                    try {
                        // 检查是否已存在（根据regionCode、year和communityName）
                        QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
                        wrapper.eq("region_code", item.getRegionCode());
                        wrapper.eq("year", year);
                        wrapper.eq("community_name", item.getCommunityName());

                        CommunityDisasterReductionCapacity existing = getOne(wrapper);
                        if (existing != null) {
                            item.setId(existing.getId());
                            updateById(item);
                            updateCount++;
                        } else {
                            item.setYear(year);
                            save(item);
                            insertCount++;
                        }
                    } catch (Exception e) {
                        log.warn("保存社区减灾能力数据失败: {}", item.getCommunityName(), e);
                    }
                }

                result.put("insertCount", insertCount);
                result.put("updateCount", updateCount);
                result.put("successCount", insertCount + updateCount);
                result.put("success", true);
                result.put("message", String.format("导入成功！共处理%d条数据，新增%d条，更新%d条",
                        dataList.size(), insertCount, updateCount));

            } finally {
                dataStore.dispose();
            }

        } catch (Exception e) {
            log.error("导入GPKG文件失败", e);
            result.put("message", "导入GPKG文件失败: " + e.getMessage());
        } finally {
            // 删除临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    log.warn("删除临时文件失败", e);
                }
            }
        }

        return result;
    }

    /**
     * 将GPKG要素解析为CommunityDisasterReductionCapacity对象
     */
    private CommunityDisasterReductionCapacity parseGpkgFeatureToCommunityCapacity(
            SimpleFeature feature, Map<String, String> fieldMapping, Integer year) {
        try {
            CommunityDisasterReductionCapacity data = new CommunityDisasterReductionCapacity();
            data.setYear(year);

            // 根据字段映射从GPKG属性中获取值
            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                String gpkgField = entry.getKey();
                String dbField = entry.getValue();

                Object value = feature.getAttribute(gpkgField);
                if (value != null) {
                    setCommunityFieldValue(data, dbField, value);
                }
            }

            return data;
        } catch (Exception e) {
            log.warn("解析GPKG要素失败: {}", feature.getID(), e);
            return null;
        }
    }

    /**
     * 设置字段值到CommunityDisasterReductionCapacity对象
     */
    private void setCommunityFieldValue(CommunityDisasterReductionCapacity data, String fieldName, Object value) {
        if (value == null) {
            return;
        }

        try {
            switch (fieldName) {
                case "provinceName":
                    data.setProvinceName(getStringValue(value));
                    break;
                case "cityName":
                    data.setCityName(getStringValue(value));
                    break;
                case "countyName":
                    data.setCountyName(getStringValue(value));
                    break;
                case "townshipName":
                    data.setTownshipName(getStringValue(value));
                    break;
                case "communityName":
                    data.setCommunityName(getStringValue(value));
                    break;
                case "regionCode":
                    data.setRegionCode(getStringValue(value));
                    break;
                case "residentPopulation":
                    data.setResidentPopulation(getIntegerValue(value));
                    break;
                case "hasEmergencyPlan":
                    data.setHasEmergencyPlan(getStringValue(value));
                    break;
                case "hasVulnerableGroupsList":
                    data.setHasVulnerableGroupsList(getStringValue(value));
                    break;
                case "hasDisasterPointsList":
                    data.setHasDisasterPointsList(getStringValue(value));
                    break;
                case "hasDisasterMap":
                    data.setHasDisasterMap(getStringValue(value));
                    break;
                case "lastYearFundingAmount":
                    data.setLastYearFundingAmount(getDecimalValue(value));
                    break;
                case "materialsEquipmentValue":
                    data.setMaterialsEquipmentValue(getDecimalValue(value));
                    break;
                case "medicalServiceCount":
                    data.setMedicalServiceCount(getIntegerValue(value));
                    break;
                case "registeredVolunteerCount":
                    data.setRegisteredVolunteerCount(getIntegerValue(value));
                    break;
                case "militiaReserveCount":
                    data.setMilitiaReserveCount(getIntegerValue(value));
                    break;
                case "lastYearTrainingParticipants":
                    data.setLastYearTrainingParticipants(getIntegerValue(value));
                    break;
                case "lastYearDrillParticipants":
                    data.setLastYearDrillParticipants(getIntegerValue(value));
                    break;
                case "emergencyShelterCapacity":
                    data.setEmergencyShelterCapacity(getIntegerValue(value));
                    break;
                default:
                    // 忽略未知字段
                    break;
            }
        } catch (Exception e) {
            log.warn("设置字段值失败: {} = {}", fieldName, value);
        }
    }
}
