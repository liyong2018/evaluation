package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.entity.MedicalInstitution;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.SurveyDataMapper;
import com.evaluate.service.IOrganizationService;
import com.evaluate.service.ISurveyDataService;
import com.evaluate.service.IFirefighterConfigService;
import com.evaluate.service.IVolunteerMilitiaService;
import com.evaluate.service.IMedicalInstitutionService;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调查数据服务实现类
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SurveyDataServiceImpl extends ServiceImpl<SurveyDataMapper, SurveyData> implements ISurveyDataService {

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private IFirefighterConfigService firefighterConfigService;

    @Autowired
    private IVolunteerMilitiaService volunteerMilitiaService;

    @Autowired(required = false)
    private IMedicalInstitutionService medicalInstitutionService;

    @Override
    public List<SurveyData> getBySurveyName(String surveyName) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.eq("township", surveyName);
        return list(wrapper);
    }

    @Override
    public List<SurveyData> getBySurveyRegion(String surveyRegion) {
        log.info("开始查询地区数据，输入参数: '{}'", surveyRegion);
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.like("township", surveyRegion);
        return list(wrapper);
    }

    @Override
    public List<SurveyData> getByIndicatorCode(String indicatorCode) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        // 由于survey_data表没有indicator_code字段，这里可以返回空列表或根据其他条件查询
        return new ArrayList<>();
    }

    @Override
    public List<SurveyData> getByYearAndOrgCode(Integer year, String orgCode) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        if (year != null) {
            wrapper.eq("year", year);
        }
        if (StringUtils.hasText(orgCode)) {
            wrapper.likeRight("region_code", orgCode.trim());
        }
        return list(wrapper);
    }

    @Override
    public IPage<SurveyData> getByYearAndOrgCodePage(Integer year, String orgCode, int page, int pageSize) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        if (year != null) {
            wrapper.eq("year", year);
        }
        if (StringUtils.hasText(orgCode)) {
            wrapper.likeRight("region_code", orgCode.trim());
        }
        // 按创建时间倒序排列，确保最新数据在前
        wrapper.orderByDesc("create_time");

        Page<SurveyData> pageParam = new Page<>(page, pageSize);
        return page(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<SurveyData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }
        return saveBatch(dataList);
    }

    /**
     * 智能批量保存数据：根据 region_code 和 year 判断是否存在，存在则更新，不存在则插入
     * 这样可以避免不同年份的数据相互覆盖
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean smartBatchSave(List<SurveyData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("智能批量保存数据失败：数据列表为空");
            return false;
        }

        log.info("开始智能批量保存调查数据，共{}条记录", dataList.size());
        try {
            int successCount = 0;
            int updateCount = 0;
            int insertCount = 0;
            Map<String, Long> existingIdMap = loadExistingSurveyIdMap(dataList);

            for (int i = 0; i < dataList.size(); i++) {
                SurveyData data = dataList.get(i);
                try {
                    log.debug("处理第{}条数据，地区代码：{}，年份：{}", i+1, data.getRegionCode(), data.getYear());
                    String uniqueYearKey = buildSurveyUniqueYearKey(data.getRegionCode(), data.getYear());
                    Long existingId = uniqueYearKey == null ? null : existingIdMap.get(uniqueYearKey);

                    if (existingId != null) {
                        log.debug("更新现有记录，ID：{}，地区代码：{}，年份：{}", existingId, data.getRegionCode(), data.getYear());
                        data.setId(existingId);
                        boolean updateResult = updateById(data);
                        if (updateResult) {
                            organizationService.syncFromSurveyData(data);
                            updateCount++;
                            successCount++;
                        } else {
                            log.error("更新记录失败，ID：{}，地区代码：{}，年份：{}", existingId, data.getRegionCode(), data.getYear());
                        }
                    } else {
                        log.debug("插入新记录，地区代码：{}，年份：{}", data.getRegionCode(), data.getYear());
                        data.setId(null);
                        boolean saveResult = save(data);
                        if (saveResult) {
                            if (uniqueYearKey != null && data.getId() != null) {
                                existingIdMap.put(uniqueYearKey, data.getId());
                            }
                            organizationService.syncFromSurveyData(data);
                            insertCount++;
                            successCount++;
                        } else {
                            log.error("插入记录失败，地区代码：{}，年份：{}", data.getRegionCode(), data.getYear());
                        }
                    }
                } catch (Exception e) {
                    log.error("处理第{}条数据失败，地区代码：{}，年份：{}", i+1, data.getRegionCode(), data.getYear(), e);
                    throw e; // 重新抛出异常以触发事务回滚
                }
            }

            log.info("智能批量保存调查数据完成，成功{}条，其中插入{}条，更新{}条", successCount, insertCount, updateCount);
            return successCount == dataList.size();
        } catch (Exception e) {
            log.error("智能批量保存数据失败", e);
            throw e;
        }
    }

    private String buildSurveyUniqueYearKey(String regionCode, Integer year) {
        if (!StringUtils.hasText(regionCode) || year == null) {
            return null;
        }
        return regionCode.trim() + "|" + year;
    }

    private Map<String, Long> loadExistingSurveyIdMap(List<SurveyData> dataList) {
        Map<String, Long> existingIdMap = new HashMap<>();
        Set<String> regionCodes = new HashSet<>();
        Set<Integer> years = new HashSet<>();

        for (SurveyData item : dataList) {
            if (item == null) {
                continue;
            }
            if (StringUtils.hasText(item.getRegionCode())) {
                regionCodes.add(item.getRegionCode().trim());
            }
            if (item.getYear() != null) {
                years.add(item.getYear());
            }
        }

        if (regionCodes.isEmpty() || years.isEmpty()) {
            return existingIdMap;
        }

        List<String> allRegionCodes = new ArrayList<>(regionCodes);
        int batchSize = 1000;
        for (int start = 0; start < allRegionCodes.size(); start += batchSize) {
            int end = Math.min(start + batchSize, allRegionCodes.size());
            List<String> regionCodeBatch = allRegionCodes.subList(start, end);
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("id", "region_code", "year");
            wrapper.in("region_code", regionCodeBatch);
            wrapper.in("year", years);
            List<SurveyData> existingList = list(wrapper);
            for (SurveyData existing : existingList) {
                String key = buildSurveyUniqueYearKey(existing.getRegionCode(), existing.getYear());
                if (key != null && existing.getId() != null) {
                    existingIdMap.put(key, existing.getId());
                }
            }
        }

        return existingIdMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importFromExcel(MultipartFile file, Integer year) {
        return importFromExcel(file, year, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importFromExcel(MultipartFile file, Integer year, String orgCode) {
        if (file == null || file.isEmpty()) {
            log.error("Excel文件为空");
            return false;
        }

        if (year == null) {
            log.error("年份参数为空");
            return false;
        }

        try {
            assertMedicalInstitutionDataExists(year, orgCode);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        log.info("开始导入调查数据，文件名：{}，年份：{}，orgCode：{}", file.getOriginalFilename(), year, orgCode);

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            log.info("Excel文件共{}行数据", sheet.getLastRowNum() + 1);
            Map<String, Integer> columnIndexMap = buildExcelColumnIndexMap(sheet);
            boolean isCodeHeaderExcel = columnIndexMap.containsKey("dwmc")
                    && columnIndexMap.containsKey("address")
                    && (columnIndexMap.containsKey("jgdmry") || columnIndexMap.containsKey("code"));

            List<SurveyData> dataList = new ArrayList<>();
            int validRowCount = 0;
            int emptyRowCount = 0;

            int startRow = isCodeHeaderExcel ? 1 : 2;
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    emptyRowCount++;
                    continue;
                }
                if (isCodeHeaderExcel && isLikelyTownshipDescriptionRow(row, columnIndexMap)) {
                    continue;
                }

                try {
                    SurveyData data = parseRowToSurveyData(row, year, i, isCodeHeaderExcel, columnIndexMap);
                    if (data != null) {
                        data.setYear(year);
                        dataList.add(data);
                        validRowCount++;
                    }
                } catch (Exception e) {
                    log.error("解析第{}行数据失败", i + 1, e);
                    throw e; // 重新抛出异常以触发事务回滚
                }
            }

            log.info("Excel文件解析完成，有效数据{}行，空行{}行", validRowCount, emptyRowCount);

            if (dataList.isEmpty()) {
                log.warn("没有有效的调查数据可以导入");
                return false;
            }

            return smartBatchSave(dataList);
        } catch (Exception e) {
            log.error("导入调查数据失败，文件名：{}，年份：{}", file.getOriginalFilename(), year, e);
            throw new RuntimeException("导入调查数据失败: " + e.getMessage(), e);
        }
    }

    private void assertMedicalInstitutionDataExists(Integer year, String orgCode) {
        if (medicalInstitutionService == null) {
            throw new IllegalStateException("医疗卫生机构服务不可用，无法校验导入前置条件");
        }
        QueryWrapper<MedicalInstitution> wrapper = new QueryWrapper<>();
        wrapper.eq("year", year);
        if (StringUtils.hasText(orgCode)) {
            wrapper.likeRight("org_code", orgCode.trim());
        }
        long count = medicalInstitutionService.count(wrapper);
        if (count <= 0) {
            if (StringUtils.hasText(orgCode)) {
                throw new IllegalStateException("导入乡镇数据前，请先导入该组织机构在" + year + "年的医疗卫生机构数据（用于统计医院床位）");
            }
            throw new IllegalStateException("导入乡镇数据前，请先导入" + year + "年的医疗卫生机构数据（用于统计医院床位）");
        }
    }

    /**
     * 解析Excel行数据为SurveyData对象
     * 包含所有字段：唯一码和核实状态也会被处理
     */
    private SurveyData parseRowToSurveyData(Row row, Integer year) {
        return parseRowToSurveyData(row, year, -1, false, Collections.emptyMap());
    }

    /**
     * 解析Excel行数据为SurveyData对象
     * 包含所有字段：唯一码和核实状态也会被处理
     */
    private SurveyData parseRowToSurveyData(Row row, Integer year, int rowNumber) {
        return parseRowToSurveyData(row, year, rowNumber, false, Collections.emptyMap());
    }

    private SurveyData parseRowToSurveyData(Row row, Integer year, int rowNumber, boolean isCodeHeaderExcel, Map<String, Integer> columnIndexMap) {
        try {
            SurveyData data = new SurveyData();
            if (isCodeHeaderExcel) {
                data.setUniqueId(firstNonBlank(
                        getCellValueByColumnName(row, columnIndexMap, "id"),
                        getCellValueByColumnName(row, columnIndexMap, "fxpc_datai")
                ));
                data.setVerificationStatus(firstNonBlank(
                        getCellValueByColumnName(row, columnIndexMap, "verification_status"),
                        getCellValueByColumnName(row, columnIndexMap, "fxpc_sjzt_")
                ));
                data.setTownship(firstNonBlank(
                        getCellValueByColumnName(row, columnIndexMap, "dwmc"),
                        getCellValueByColumnName(row, columnIndexMap, "dzxiang")
                ));
                String townshipAddress = getCellValueByColumnName(row, columnIndexMap, "address");
                data.setTownshipAddress(townshipAddress);
                parseAddressToProvinceCityCounty(data, townshipAddress);

                String regionCode = firstNonBlank(
                        getCellValueByColumnName(row, columnIndexMap, "jgdmry"),
                        getCellValueByColumnName(row, columnIndexMap, "code")
                );
                data.setRegionCode(regionCode);
                data.setTotalHouseholds(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "zhs")));
                data.setPopulation(getCellNumericValue(getCellByColumnName(row, columnIndexMap, "nmczrksl")));
                data.setMainDisasterTypes(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yxxzjddzy1")));
                data.setDisasterTypesOther(getCellValueByColumnName(row, columnIndexMap, "yxxzjddzy2"));
                data.setManagementStaff(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "bjzhglgzry")));
                data.setDisasterInfoStaff(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "bjzhxxyrs")));
                data.setRiskAssessment(normalizeYesNo(getCellValueByColumnName(row, columnIndexMap, "sfkzxzjdz1")));
                data.setHasDisasterMap(normalizeYesNo(getCellValueByColumnName(row, columnIndexMap, "sfyxzjdzhl")));
                data.setWarningReceiveMethod(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "zhyjxxjsf1")));
                data.setWarningReceiveMethodOther(getCellValueByColumnName(row, columnIndexMap, "zhyjxxjsf2"));
                data.setWarningCommunicationMethod(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "zhyjxxcdf1")));
                data.setWarningCommunicationMethodOther(getCellValueByColumnName(row, columnIndexMap, "zhyjxxcdf2"));
                data.setDisasterReportMethod(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "zqxxsbfs")));
                data.setDisasterReportMethodOther(getCellValueByColumnName(row, columnIndexMap, "zqxxsbfs_q"));
                data.setEmergencyPlanCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "j3nbzhxdzr")));
                data.setEmergencyResponseCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "j3nzdzrzhq")));
                data.setTrainingDrillCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "syndzzdyj1")));
                data.setTrainingParticipants(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "syndzzdyj2")));
                data.setFundingSupportMethod(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "xzjdzhjzg1")));
                data.setFundingSupportMethodOther(getCellValueByColumnName(row, columnIndexMap, "xzjdzhjzg2"));
                data.setFundingAmount(getCellBigDecimalValue(getCellByColumnName(row, columnIndexMap, "syndfzjzjz")));
                data.setMaterialStorageMethod(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "jzwzcbfs")));
                data.setStoragePointCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "xyjzwzzbcb")));
                data.setStorageEquipmentCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "xyjzwzzbsl")));
                data.setEmergencyPowerCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "yjdyhyjfds")));
                data.setEmergencyCommunicationCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "yjtxsbsl")));
                data.setEmergencyWaterCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "yjgssbsl")));
                data.setEmergencyMedicalCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "yjylsbsl")));
                data.setMaterialValue(getCellBigDecimalValue(getCellByColumnName(row, columnIndexMap, "xycbwzzbzh")));
                data.setShelterCount(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "bjzhyjbnc1")));
                data.setShelterCapacity(getCellIntegerValue(getCellByColumnName(row, columnIndexMap, "bjzhyjbnc2")));
                data.setUnitLeader(getCellValueByColumnName(row, columnIndexMap, "dwfzr"));
                data.setStatisticsLeader(getCellValueByColumnName(row, columnIndexMap, "tjfzr"));
                data.setFormFiller(getCellValueByColumnName(row, columnIndexMap, "tbr"));
                data.setContactPhone(getCellValueByColumnName(row, columnIndexMap, "lxdh"));
                data.setReportDate(getDateValue(getCellValueByColumnName(row, columnIndexMap, "tbrq")));
                data.setFillInstructions(getCellValueByColumnName(row, columnIndexMap, "fxpc_dcdxb"));

                String province = getCellValueByColumnName(row, columnIndexMap, "dzsheng");
                String city = getCellValueByColumnName(row, columnIndexMap, "dzshi");
                String county = getCellValueByColumnName(row, columnIndexMap, "dzxian");
                String township = getCellValueByColumnName(row, columnIndexMap, "dzxiang");
                if (StringUtils.hasText(province)) data.setProvince(province.trim());
                if (StringUtils.hasText(city)) data.setCity(city.trim());
                if (StringUtils.hasText(county)) data.setCounty(county.trim());
                if (!StringUtils.hasText(data.getTownship()) && StringUtils.hasText(township)) data.setTownship(township.trim());

                if (isTownshipRowEmpty(data)) {
                    return null;
                }
                setEnhancedDataFromConfig(data, year);
                return data;
            }

            // 第0列：唯一码
            data.setUniqueId(getCellStringValue(row.getCell(0)));

            // 第1列：核实状态
            data.setVerificationStatus(getCellStringValue(row.getCell(1)));

            // 第2列：乡镇（街道）名称
            data.setTownship(getCellStringValue(row.getCell(2)));

            // 第3列：乡镇（街道）地址
            String townshipAddress = getCellStringValue(row.getCell(3));
            data.setTownshipAddress(townshipAddress);

            // 解析省市县信息
            parseAddressToProvinceCityCounty(data, townshipAddress);

            // 第4列：乡镇（街道）代码
            data.setRegionCode(getCellStringValue(row.getCell(4)));

            // 第5列：年末总户数
            data.setTotalHouseholds(getCellIntegerValue(row.getCell(5)));

            // 第6列：常住人口数量
            data.setPopulation(getCellNumericValue(row.getCell(6)));

            // 第7列：影响乡镇的主要灾害类型
            data.setMainDisasterTypes(getCellStringValue(row.getCell(7)));

            // 第8列：主要灾害类型-其他项说明
            data.setDisasterTypesOther(getCellStringValue(row.getCell(8)));

            // 第9列：本级灾害管理工作人员总数
            data.setManagementStaff(getCellIntegerValue(row.getCell(9)));

            // 第10列：本级灾害信息员人数
            data.setDisasterInfoStaff(getCellIntegerValue(row.getCell(10)));

            // 第11列：是否开展乡镇灾害风险评估
            data.setRiskAssessment(getCellStringValue(row.getCell(11)));

            // 第12列：是否有乡镇灾害类地图
            data.setHasDisasterMap(getCellStringValue(row.getCell(12)));

            // 第13列：灾害预警信息接收方式
            data.setWarningReceiveMethod(getCellStringValue(row.getCell(13)));

            // 第14列：灾害预警信息接收方式-其他项说明
            data.setWarningReceiveMethodOther(getCellStringValue(row.getCell(14)));

            // 第15列：灾害预警信息传达方式
            data.setWarningCommunicationMethod(getCellStringValue(row.getCell(15)));

            // 第16列：灾害预警信息传达方式-其他项说明
            data.setWarningCommunicationMethodOther(getCellStringValue(row.getCell(16)));

            // 第17列：灾情信息上报方式
            data.setDisasterReportMethod(getCellStringValue(row.getCell(17)));

            // 第18列：灾情信息上报方式-其他项说明
            data.setDisasterReportMethodOther(getCellStringValue(row.getCell(18)));

            // 第19列：近3年编制或修订自然灾害应急预案数量
            data.setEmergencyPlanCount(getCellIntegerValue(row.getCell(19)));

            // 第20列：近3年针对自然灾害启动应急响应次数
            data.setEmergencyResponseCount(getCellIntegerValue(row.getCell(20)));

            // 第21列：上一年度组织的应急管理培训和演练次数
            data.setTrainingDrillCount(getCellIntegerValue(row.getCell(21)));

            // 第22列：上一年度组织的应急管理培训和演练参与人次
            data.setTrainingParticipants(getCellIntegerValue(row.getCell(22)));

            // 第23列：乡镇综合减灾工作经费保障方式
            data.setFundingSupportMethod(getCellStringValue(row.getCell(23)));

            // 第24列：乡镇综合减灾工作经费保障方式-其他说明
            data.setFundingSupportMethodOther(getCellStringValue(row.getCell(24)));

            // 第25列：上一年度防灾减灾救灾资金投入总金额
            data.setFundingAmount(getCellBigDecimalValue(row.getCell(25)));

            // 第26列：救灾物资储备方式
            data.setMaterialStorageMethod(getCellStringValue(row.getCell(26)));

            // 第27列：救灾物资储备方式-其他项说明
            data.setMaterialStorageMethodOther(getCellStringValue(row.getCell(27)));

            // 第28列：本级救灾物资、装备储备点数量
            data.setStoragePointCount(getCellIntegerValue(row.getCell(28)));

            // 第29列：本级储备点救灾物资、装备数量
            data.setStorageEquipmentCount(getCellIntegerValue(row.getCell(29)));

            // 第30列：其中：应急电源或应急发电设备数量
            data.setEmergencyPowerCount(getCellIntegerValue(row.getCell(30)));

            // 第31列：应急通信设备数量
            data.setEmergencyCommunicationCount(getCellIntegerValue(row.getCell(31)));

            // 第32列：应急供水设备数量
            data.setEmergencyWaterCount(getCellIntegerValue(row.getCell(32)));

            // 第33列：应急医疗设备数量
            data.setEmergencyMedicalCount(getCellIntegerValue(row.getCell(33)));

            // 第34列：医院床位数 - 现在由医疗设施数据自动统计
            // 注意：这个字段会在setEnhancedDataFromConfig方法中被自动计算和覆盖
            // Excel中的值将被医疗机构的实际统计数据替代
            // data.setHospitalBeds(getCellIntegerValue(row.getCell(34)));

            // 全面调试：输出前50列的所有内容
            if (rowNumber <= 3) { // 只对前3行进行详细输出
                log.info("=== 行{}的Excel内容分析 ===", rowNumber);
                for (int col = 0; col <= 50; col++) {
                    Cell cell = row.getCell(col);
                    if (cell != null) {
                        String cellStr = getCellStringValue(cell);
                        Double cellDouble = getCellDoubleValue(cell);
                        Integer cellInt = getCellIntegerValue(cell);
                        log.info("列{}: 类型={}, 字符串='{}', 数值={}, 整数={}",
                            col, cell.getCellType(), cellStr, cellDouble, cellInt);
                    }
                }
                log.info("=== 行{}内容分析结束 ===", rowNumber);
            }

            // 智能检测物资价值列（通常包含"万元"、"现有储备物资"等关键词）
            int materialValueColumn = findMaterialValueColumn(row);
            Cell materialValueCell = row.getCell(materialValueColumn);
            BigDecimal materialValue = getCellBigDecimalValue(materialValueCell);
            data.setMaterialValue(materialValue);
            log.info("物资价值解析 - 行: {}, 检测到列: {}, 单元格类型: {}, 原始值: {}, 解析结果: {}",
                rowNumber, materialValueColumn, materialValueCell != null ? materialValueCell.getCellType() : "null",
                materialValueCell != null ? getCellStringValue(materialValueCell) : "null",
                materialValue);

            // 智能检测避难场所数量列（通常包含"数量"、"个"等关键词）
            int shelterCountColumn = findShelterCountColumn(row, materialValueColumn);
            Cell shelterCountCell = row.getCell(shelterCountColumn);
            Integer shelterCount = getCellIntegerValue(shelterCountCell);
            data.setShelterCount(shelterCount);
            log.debug("避难场所数量解析 - 行: {}, 检测到列: {}, 单元格类型: {}, 原始值: {}, 解析结果: {}",
                rowNumber, shelterCountColumn, shelterCountCell != null ? shelterCountCell.getCellType() : "null",
                shelterCountCell != null ? getCellStringValue(shelterCountCell) : "null",
                shelterCount);

            // 智能检测避难场所容量列（通常在避难场所数量列之后）
            int shelterCapacityColumn = findShelterCapacityColumn(row, shelterCountColumn);
            Cell shelterCapacityCell = row.getCell(shelterCapacityColumn);
            Integer shelterCapacity = getCellIntegerValue(shelterCapacityCell);
            data.setShelterCapacity(shelterCapacity);
            log.debug("避难场所容量解析最终结果 - 行: {}, 使用列: {}, 单元格类型: {}, 原始值: {}, 解析结果: {}",
                rowNumber, shelterCapacityColumn, shelterCapacityCell != null ? shelterCapacityCell.getCellType() : "null",
                shelterCapacityCell != null ? getCellStringValue(shelterCapacityCell) : "null",
                shelterCapacity);

            // 根据实际使用的列，动态调整后续字段的列索引
            int unitLeaderColumn = shelterCapacityColumn + 1;
            int statisticsLeaderColumn = shelterCapacityColumn + 2;
            int formFillerColumn = shelterCapacityColumn + 3;
            int contactPhoneColumn = shelterCapacityColumn + 4;
            int reportDateColumn = shelterCapacityColumn + 5;
            int fillInstructionsColumn = shelterCapacityColumn + 6;

            // 第38列（或调整后）：单位负责人
            data.setUnitLeader(getCellStringValue(row.getCell(unitLeaderColumn)));

            // 第39列（或调整后）：统计负责人
            data.setStatisticsLeader(getCellStringValue(row.getCell(statisticsLeaderColumn)));

            // 第40列（或调整后）：填表人
            data.setFormFiller(getCellStringValue(row.getCell(formFillerColumn)));

            // 第41列（或调整后）：联系电话
            data.setContactPhone(getCellStringValue(row.getCell(contactPhoneColumn)));

            // 第42列（或调整后）：报出日期
            Cell reportDateCell = row.getCell(reportDateColumn);
            if (reportDateCell != null) {
                try {
                    if (reportDateCell.getCellType() == CellType.STRING) {
                        String dateStr = reportDateCell.getStringCellValue();
                        if (StringUtils.hasText(dateStr)) {
                            data.setReportDate(getDateValue(dateStr));
                        }
                    } else if (reportDateCell.getCellType() == CellType.NUMERIC) {
                        data.setReportDate(getDateValue(reportDateCell.getDateCellValue()));
                    }
                } catch (Exception e) {
                    log.warn("解析报出日期失败: {}", e.getMessage());
                }
            }

            // 第43列（或调整后）：填写说明
            data.setFillInstructions(getCellStringValue(row.getCell(fillInstructionsColumn)));

            // 第44列（或调整后）：消防员数量 - 读取Excel中的值
            int firefighterColumn = fillInstructionsColumn + 1;
            data.setFirefightersCount(getCellIntegerValue(row.getCell(firefighterColumn)));
            log.debug("消防员数量解析 - 行: {}, 使用列: {}, 值: {}", rowNumber, firefighterColumn, data.getFirefightersCount());

            // 使用新的数据源设置志愿者、民兵预备役、医院床位数据（消防员已从Excel读取，不再覆盖）
            setEnhancedDataFromConfig(data, year);

            return data;
        } catch (Exception e) {
            log.error("解析Excel行数据失败", e);
            return null;
        }
    }

    private boolean isTownshipRowEmpty(SurveyData data) {
        return !StringUtils.hasText(data.getUniqueId())
                && !StringUtils.hasText(data.getTownship())
                && !StringUtils.hasText(data.getTownshipAddress())
                && !StringUtils.hasText(data.getRegionCode());
    }

    private boolean isLikelyTownshipDescriptionRow(Row row, Map<String, Integer> columnIndexMap) {
        String idDesc = getCellValueByColumnName(row, columnIndexMap, "id");
        String townshipDesc = getCellValueByColumnName(row, columnIndexMap, "dwmc");
        String addressDesc = getCellValueByColumnName(row, columnIndexMap, "address");
        String idText = idDesc == null ? "" : idDesc.trim();
        String townshipText = townshipDesc == null ? "" : townshipDesc.trim();
        String addressText = addressDesc == null ? "" : addressDesc.trim();
        return idText.contains("唯一标识")
                || townshipText.contains("乡镇（街道）名称")
                || addressText.contains("乡镇（街道）地址");
    }

    private String normalizeYesNo(String value) {
        String normalized = normalizeListText(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        String text = normalized.trim();
        return text.equals("是") || text.equalsIgnoreCase("yes")
                || text.equals("1") || text.equalsIgnoreCase("true") ? "是" : "否";
    }

    private String normalizeListText(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String text = value.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        text = text.replace("\"", "").replace("'", "").trim();
        if (text.contains(",")) {
            String[] parts = text.split(",");
            List<String> normalized = new ArrayList<>();
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    normalized.add(part.trim());
                }
            }
            if (!normalized.isEmpty()) {
                return String.join(";", normalized);
            }
        }
        return text;
    }

    private LocalDate getDateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        String str = String.valueOf(value).trim();
        if (!StringUtils.hasText(str)) {
            return null;
        }
        try {
            String normalized = str;
            if (normalized.matches("\\d{4}/\\d{2}/\\d{2}")) {
                normalized = normalized.replace('/', '-');
            }
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2}\\s+.*")) {
                normalized = normalized.split("\\s+")[0];
            }
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(normalized);
            }
            DateTimeFormatter englishDateTimeFormatter =
                    DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            return java.time.ZonedDateTime.parse(str, englishDateTimeFormatter).toLocalDate();
        } catch (Exception e) {
            log.warn("解析报出日期失败: {}", str);
            return null;
        }
    }

    private String getCellValueByColumnName(Row row, Map<String, Integer> columnIndexMap, String columnName) {
        Integer colIndex = columnIndexMap.get(columnName);
        if (colIndex == null) {
            return null;
        }
        String value = getCellStringValue(row.getCell(colIndex));
        return value == null ? null : value.trim();
    }

    private Cell getCellByColumnName(Row row, Map<String, Integer> columnIndexMap, String columnName) {
        Integer colIndex = columnIndexMap.get(columnName);
        if (colIndex == null) {
            return null;
        }
        return row.getCell(colIndex);
    }

    private String firstNonBlank(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private Map<String, Integer> buildExcelColumnIndexMap(Sheet sheet) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return columnIndexMap;
        }
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = getCellStringValue(cell);
                if (StringUtils.hasText(columnName)) {
                    columnIndexMap.put(columnName.trim().toLowerCase(), i);
                }
            }
        }
        return columnIndexMap;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return NumberToTextConverter.toText(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return NumberToTextConverter.toText(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }

    /**
     * 获取单元格数值（支持字符串格式的数字）
     */
    private Long getCellNumericValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) {
                        return null;
                    }
                    return Long.parseLong(value.replaceAll("[,，]", ""));
                case FORMULA:
                    return (long) cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("解析数值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格整数值（支持字符串格式的数字）
     */
    private Integer getCellIntegerValue(Cell cell) {
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
     * 获取单元格双精度浮点数值（支持字符串格式的数字）
     */
    private Double getCellDoubleValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) {
                        return null;
                    }
                    return Double.parseDouble(value.replaceAll("[,，]", ""));
                case FORMULA:
                    return cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("解析双精度数值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格BigDecimal值（支持字符串格式的数字，保留4位小数）
     */
    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    double value = cell.getNumericCellValue();
                    // 使用BigDecimal.valueOf避免精度问题，并设置4位小数精度
                    return BigDecimal.valueOf(value).setScale(4, BigDecimal.ROUND_HALF_UP);
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) {
                        return null;
                    }
                    double parsedValue = Double.parseDouble(strValue.replaceAll("[,，]", ""));
                    return BigDecimal.valueOf(parsedValue).setScale(4, BigDecimal.ROUND_HALF_UP);
                case FORMULA:
                    double formulaValue = cell.getNumericCellValue();
                    return BigDecimal.valueOf(formulaValue).setScale(4, BigDecimal.ROUND_HALF_UP);
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("解析BigDecimal数值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析地址字符串，提取省、市、县信息
     *
     * @param data SurveyData对象
     * @param address 完整地址字符串
     */
    private void parseAddressToProvinceCityCounty(SurveyData data, String address) {
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
                    remaining = remaining.replaceFirst(county, "");
                    break;
                }
            }

            // 提取乡镇名称
            String townshipName = extractTownshipName(remaining);
            if (townshipName != null && !townshipName.isEmpty()) {
                // 如果当前还没有设置乡镇名称，或者从地址中提取的更完整
                if (data.getTownship() == null || data.getTownship().isEmpty()) {
                    data.setTownship(townshipName);
                }
            }

            // 特殊处理：如果没找到市但找到了省和县，可能是一些特殊的行政区划
            if (province != null && city == null && county != null) {
                // 对于一些特殊情况，如"省+县"的结构
                if (county.contains("市")) {
                    city = county;
                    county = null;
                }
            }

            // 设置解析结果
            if (province != null) {
                data.setProvince(province);
            }
            if (city != null) {
                data.setCity(city);
            }
            if (county != null) {
                data.setCounty(county);
            }

            log.debug("地址解析结果 - 省份: {}, 城市: {}, 县: {}, 原地址: {}",
                province, city, county, address);

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

    @Override
    public byte[] exportToExcel(String surveyName) {
        List<SurveyData> dataList = getBySurveyName(surveyName);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("调查数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"行政区代码", "省名称", "市名称", "县名称", "乡镇名称",
                              "常住人口", "管理人员", "风险评估", "资金投入", "物资价值",
                              "灾害信息员", "应急预案数", "应急响应次数", "培训演练次数",
                              "培训参与人次", "避难场所容量"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (SurveyData data : dataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(data.getRegionCode() != null ? data.getRegionCode() : "");
                row.createCell(1).setCellValue(data.getProvince() != null ? data.getProvince() : "");
                row.createCell(2).setCellValue(data.getCity() != null ? data.getCity() : "");
                row.createCell(3).setCellValue(data.getCounty() != null ? data.getCounty() : "");
                row.createCell(4).setCellValue(data.getTownship() != null ? data.getTownship() : "");
                row.createCell(5).setCellValue(data.getPopulation() != null ? data.getPopulation() : 0);
                row.createCell(6).setCellValue(data.getManagementStaff() != null ? data.getManagementStaff() : 0);
                row.createCell(7).setCellValue(data.getRiskAssessment() != null ? data.getRiskAssessment() : "");
                row.createCell(8).setCellValue(data.getFundingAmount() != null ? data.getFundingAmount().doubleValue() : 0);
                row.createCell(9).setCellValue(data.getMaterialValue() != null ? data.getMaterialValue().doubleValue() : 0);
                row.createCell(10).setCellValue(data.getDisasterInfoStaff() != null ? data.getDisasterInfoStaff() : 0);
                row.createCell(11).setCellValue(data.getEmergencyPlanCount() != null ? data.getEmergencyPlanCount() : 0);
                row.createCell(12).setCellValue(data.getEmergencyResponseCount() != null ? data.getEmergencyResponseCount() : 0);
                row.createCell(13).setCellValue(data.getTrainingDrillCount() != null ? data.getTrainingDrillCount() : 0);
                row.createCell(14).setCellValue(data.getTrainingParticipants() != null ? data.getTrainingParticipants() : 0);
                row.createCell(15).setCellValue(data.getShelterCapacity() != null ? data.getShelterCapacity() : 0);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("导出Excel失败", e);
            return null;
        }
    }

    @Override
    public List<String> getAllIndicatorCodes() {
        // survey_data表没有indicator_code字段，返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<SurveyData> getByCharsetAttribute(String charsetAttribute) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        // 可以根据需要实现字符集属性查询
        return new ArrayList<>();
    }

    @Override
    public boolean validateSurveyData(SurveyData surveyData) {
        if (surveyData == null) {
            return false;
        }
        // 实现数据验证逻辑
        return StringUtils.hasText(surveyData.getTownship()) &&
               StringUtils.hasText(surveyData.getRegionCode());
    }

    @Override
    public boolean deleteSurveyDataAndResults(String surveyName) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.eq("township", surveyName);
        return remove(wrapper);
    }

    @Override
    public List<SurveyData> searchByKeyword(String keyword) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.like("township", keyword)
                .or().like("province", keyword)
                .or().like("city", keyword)
                .or().like("county", keyword));
        return list(wrapper);
    }

    @Override
    public byte[] exportAllToExcel() {
        List<SurveyData> dataList = list();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("调查数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"行政区代码", "省名称", "市名称", "县名称", "乡镇名称",
                              "常住人口", "管理人员", "风险评估", "资金投入", "物资价值",
                              "灾害信息员", "应急预案数", "应急响应次数", "培训演练次数",
                              "培训参与人次", "避难场所容量"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (SurveyData data : dataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(data.getRegionCode() != null ? data.getRegionCode() : "");
                row.createCell(1).setCellValue(data.getProvince() != null ? data.getProvince() : "");
                row.createCell(2).setCellValue(data.getCity() != null ? data.getCity() : "");
                row.createCell(3).setCellValue(data.getCounty() != null ? data.getCounty() : "");
                row.createCell(4).setCellValue(data.getTownship() != null ? data.getTownship() : "");
                row.createCell(5).setCellValue(data.getPopulation() != null ? data.getPopulation() : 0);
                row.createCell(6).setCellValue(data.getManagementStaff() != null ? data.getManagementStaff() : 0);
                row.createCell(7).setCellValue(data.getRiskAssessment() != null ? data.getRiskAssessment() : "");
                row.createCell(8).setCellValue(data.getFundingAmount() != null ? data.getFundingAmount().doubleValue() : 0.0);
                row.createCell(9).setCellValue(data.getMaterialValue() != null ? data.getMaterialValue().doubleValue() : 0.0);
                row.createCell(10).setCellValue(data.getDisasterInfoStaff() != null ? data.getDisasterInfoStaff() : 0);
                row.createCell(11).setCellValue(data.getEmergencyPlanCount() != null ? data.getEmergencyPlanCount() : 0);
                row.createCell(12).setCellValue(data.getEmergencyResponseCount() != null ? data.getEmergencyResponseCount() : 0);
                row.createCell(13).setCellValue(data.getTrainingDrillCount() != null ? data.getTrainingDrillCount() : 0);
                row.createCell(14).setCellValue(data.getTrainingParticipants() != null ? data.getTrainingParticipants() : 0);
                row.createCell(15).setCellValue(data.getShelterCapacity() != null ? data.getShelterCapacity() : 0);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] result = outputStream.toByteArray();
            log.info("Excel文件生成成功，大小: {} 字节", result.length);
            return result;

        } catch (Exception e) {
            log.error("导出Excel失败", e);
            return null;
        }
    }

    /**
     * 使用配置数据源设置消防员、志愿者、民兵预备役数据
     *
     * @param data SurveyData对象
     * @param year 数据年份
     */
    private void setEnhancedDataFromConfig(SurveyData data, Integer year) {
        // 获取行政区划代码
        String regionCode = data.getRegionCode();
        if (regionCode == null || regionCode.trim().isEmpty()) {
            log.warn("行政区划代码为空，无法设置增强数据");
            return;
        }

        try {

            // 1. 从消防员配置表获取消防员数量（只按组织编码精确匹配）
            // 注意：只有当配置表中有数据时，才使用配置表的值；否则保留Excel中导入的值
            try {
                Integer configuredFirefighters = firefighterConfigService.getFirefighterCountByRegionCode(regionCode);
                if (configuredFirefighters != null && configuredFirefighters > 0) {
                    // 配置表有数据，使用配置表的值
                    data.setFirefighters(configuredFirefighters);
                    log.debug("使用消防员配置数据，区域: {}, 数量: {}", regionCode, configuredFirefighters);
                } else {
                    // 配置表没有数据，保留Excel中导入的值（如果Excel中也没有，保持为0）
                    // 如果Excel中有值但firefighters字段为null，则从firefightersCount字段复制到firefighters字段
                    if (data.getFirefighters() == null && data.getFirefightersCount() != null) {
                        data.setFirefighters(data.getFirefightersCount());
                    }
                    if (data.getFirefighters() == null) {
                        data.setFirefighters(0);
                    }
                    log.debug("使用Excel导入的消防员数据，区域: {}, 数量: {}", regionCode, data.getFirefighters());
                }
            } catch (Exception e) {
                log.error("获取消防员配置失败，区域代码: {}", regionCode, e);
                // 出错时使用Excel中的值
                if (data.getFirefighters() == null && data.getFirefightersCount() != null) {
                    data.setFirefighters(data.getFirefightersCount());
                }
                if (data.getFirefighters() == null) {
                    data.setFirefighters(0);
                }
            }

            // 2. 统计志愿者人数 - 设置到独立的志愿者字段，不影响培训参与人次
            try {
                Integer volunteers = volunteerMilitiaService.sumVolunteersByRegion(regionCode, year);
                if (volunteers != null && volunteers > 0) {
                    // 使用向后兼容方法设置志愿者人数（会同时设置volunteersCount字段）
                    data.setVolunteers(volunteers);
                    log.debug("设置志愿者人数成功，区域: {}, 年份: {}, 数量: {}", regionCode, year, volunteers);
                }
            } catch (Exception e) {
                log.error("统计志愿者人数失败，区域代码: {}, 年份: {}", regionCode, year, e);
            }

            // 3. 统计民兵预备役人数
            try {
                Integer militiaReserve = volunteerMilitiaService.sumMilitiaReserveByRegion(regionCode, year);
                if (militiaReserve != null && militiaReserve > 0) {
                    // 使用向后兼容方法设置民兵预备役人数
                    data.setMilitiaReserve(militiaReserve);
                    log.debug("设置民兵预备役人数成功，区域: {}, 年份: {}, 数量: {}", regionCode, year, militiaReserve);
                }
            } catch (Exception e) {
                log.error("统计民兵预备役人数失败，区域代码: {}, 年份: {}", regionCode, year, e);
            }

            // 4. 统计医疗设施实有住院床位数
            try {
                // 检查医疗设施服务是否可用
                if (medicalInstitutionService == null) {
                    log.warn("医疗设施服务未注入，跳过医院床位统计，区域代码: {}, 年份: {}", regionCode, year);
                    data.setHospitalBeds(0);
                } else {
                    // 使用组织机构编码进行前缀匹配统计
                    Integer hospitalBeds = 0;

                    if (regionCode != null && !regionCode.trim().isEmpty()) {
                        hospitalBeds = medicalInstitutionService.sumActualHospitalBedsByRegionCode(regionCode, year);
                        log.info("设置医疗设施实有住院床位数 - 组织机构编码: {}, 乡镇: {}, 年份: {}, 床位数: {}",
                                 regionCode, data.getTownship(), year, hospitalBeds);
                    } else {
                        log.info("组织机构编码为空，设置医院床位数为0，乡镇: {}, 年份: {}", data.getTownship(), year);
                    }

                    data.setHospitalBeds(hospitalBeds != null ? hospitalBeds : 0);
                }
            } catch (Exception e) {
                log.error("统计医疗设施实有住院床位数失败，区域代码: {}, 年份: {}", regionCode, year, e);
                // 出错时设置为0
                data.setHospitalBeds(0);
            }

            // 记录增强数据设置结果
            log.info("增强数据设置完成，区域: {}, 消防员: {}, 志愿者: {}, 民兵预备役: {}, 医院床位: {}",
                regionCode,
                data.getFirefighters(),
                data.getVolunteers(),
                data.getMilitiaReserve(),
                data.getHospitalBeds());

        } catch (Exception e) {
            log.error("设置增强数据失败，区域代码: {}, 年份: {}", regionCode, year, e);
        }
    }

    @Override
    public List<SurveyData> getByConditions(String surveyName, String region, Integer year) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();

        // 基础条件：未删除的数据
        wrapper.eq("is_deleted", 0);

        // 按调查名称过滤
        if (surveyName != null && !surveyName.trim().isEmpty()) {
            wrapper.like("township", surveyName.trim());
        }

        // 按地区过滤（支持省、市、县、乡镇）
        if (region != null && !region.trim().isEmpty()) {
            String searchRegion = region.trim();
            wrapper.and(w -> w
                .like("province", searchRegion)
                .or()
                .like("city", searchRegion)
                .or()
                .like("county", searchRegion)
                .or()
                .like("township", searchRegion)
            );
        }

        // 按年份过滤
        if (year != null) {
            wrapper.eq("year", year);
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc("create_time");

        log.debug("查询条件: surveyName={}, region={}, year", surveyName, region, year);
        List<SurveyData> result = list(wrapper);
        log.debug("查询结果数量: {}", result.size());

        // 按region_code去重，保留最新记录
        if (result != null && !result.isEmpty()) {
            result = result.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(
                                    SurveyData::getRegionCode,
                                    Function.identity(),
                                    (existing, replacement) -> existing.getCreateTime().isAfter(replacement.getCreateTime()) ? existing : replacement
                            ),
                            map -> new ArrayList<>(map.values())
                    ));
            log.debug("去重后的结果数量: {}", result.size());
        }

        return result;
    }

    @Override
    @Transactional
    public int recalculateMedicalBedsForYear(Integer year) {
        log.info("开始重新计算{}年的医疗床位统计", year);

        // 查询指定年份的所有调查数据
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.eq("year", year);
        wrapper.eq("is_deleted", 0);
        List<SurveyData> surveyDataList = list(wrapper);

        int updatedCount = 0;
        for (SurveyData data : surveyDataList) {
            try {
                // 记录更新前的床位数量
                Integer originalBeds = data.getHospitalBeds();

                // 调用setEnhancedDataFromConfig方法重新计算医疗床位
                setEnhancedDataFromConfig(data, year);

                // 只有当床位数量发生变化时才更新数据库
                if (!Objects.equals(originalBeds, data.getHospitalBeds())) {
                    updateById(data);
                    updatedCount++;
                    log.info("更新医疗床位统计 - 区域: {}, 年份: {}, 原床位: {}, 新床位: {}",
                            data.getTownship(), year, originalBeds, data.getHospitalBeds());
                }
            } catch (Exception e) {
                log.error("更新区域{}的医疗床位统计失败", data.getTownship(), e);
            }
        }

        log.info("完成重新计算{}年医疗床位统计，总共处理了{}条记录，更新了{}条记录", year, surveyDataList.size(), updatedCount);
        return updatedCount;
    }

    /**
     * 智能检测物资价值列
     * 通过检查列中的数值特征来找到包含物资价值数据的列
     */
    private int findMaterialValueColumn(Row row) {
        log.debug("开始智能检测物资价值列");

        // 先尝试常见的列位置
        int[] commonColumns = {34, 35, 36, 37, 33, 32};
        for (int col : commonColumns) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                Double value = getCellDoubleValue(cell);
                String cellStr = getCellStringValue(cell);
                log.debug("检查列{}: 值={}, 字符串={}", col, value, cellStr);

                if (value != null && value > 0) {
                    // 检查是否为合理的物资价值范围（0.1-1000万元）
                    if (value >= 0.1 && value <= 1000) {
                        log.info("找到物资价值列: {}, 值: {}", col, value);
                        return col;
                    }
                }
            }
        }

        // 如果常见列都没找到，扩大搜索范围
        for (int col = 30; col <= 50; col++) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                Double value = getCellDoubleValue(cell);
                String cellStr = getCellStringValue(cell);
                log.debug("扩展搜索列{}: 值={}, 字符串={}", col, value, cellStr);

                if (value != null && value > 0) {
                    // 检查是否为合理的物资价值范围
                    if (value >= 0.1 && value <= 1000) {
                        log.info("扩展搜索找到物资价值列: {}, 值: {}", col, value);
                        return col;
                    }
                }
            }
        }

        // 如果没找到，返回默认的35列
        log.warn("未找到物资价值列，使用默认列35");
        return 35;
    }

    /**
     * 智能检测避难场所数量列
     * 通过检查列中的整数特征来找到包含避难场所数量的列
     */
    private int findShelterCountColumn(Row row, int materialValueColumn) {
        // 从物资价值列的下一列开始搜索
        for (int col = materialValueColumn + 1; col <= 50; col++) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                Integer value = getCellIntegerValue(cell);
                if (value != null && value >= 0) {
                    // 检查是否为合理的避难场所数量范围（0-100个）
                    if (value <= 100) {
                        log.debug("找到可能的避难场所数量列: {}, 值: {}", col, value);
                        return col;
                    }
                }
            }
        }
        // 如果没找到，使用物资价值列+1
        int defaultCol = materialValueColumn + 1;
        log.debug("未找到避难场所数量列，使用默认列: {}", defaultCol);
        return defaultCol;
    }

    /**
     * 智能检测避难场所容量列
     * 通过检查列中的数值特征来找到包含避难场所容量的列
     */
    private int findShelterCapacityColumn(Row row, int shelterCountColumn) {
        // 从避难场所数量列的下一列开始搜索
        for (int col = shelterCountColumn + 1; col <= 50; col++) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                Integer value = getCellIntegerValue(cell);
                if (value != null && value >= 0) {
                    // 检查是否为合理的避难场所容量范围（0-10000人）
                    if (value <= 10000) {
                        log.debug("找到可能的避难场所容量列: {}, 值: {}", col, value);
                        return col;
                    }
                }
            }
        }
        // 如果没找到，使用避难场所数量列+1
        int defaultCol = shelterCountColumn + 1;
        log.debug("未找到避难场所容量列，使用默认列: {}", defaultCol);
        return defaultCol;
    }

    @Override
    public com.evaluate.dto.ImportCheckResult checkImportPrerequisites(Integer year) {
        log.info("开始检查{}年导入前置条件", year);

        com.evaluate.dto.ImportCheckResult result =
            new com.evaluate.dto.ImportCheckResult();

        java.util.List<String> missingMedicalRegions = new java.util.ArrayList<>();
        java.util.List<String> missingFirefighterRegions = new java.util.ArrayList<>();
        boolean hasMedicalData = false;
        boolean hasFirefighterData = false;

        try {
            // 检查医疗设施数据
            if (medicalInstitutionService != null) {
                try {
                    // 检查是否有任何医疗设施数据
                    boolean anyMedicalData = medicalInstitutionService.hasAnyDataForYear(year);
                    if (anyMedicalData) {
                        hasMedicalData = true;
                        log.info("找到{}年医疗设施数据", year);
                    } else {
                        log.warn("未找到{}年医疗设施数据", year);
                    }
                } catch (Exception e) {
                    log.error("检查医疗设施数据时出错", e);
                }
            } else {
                log.warn("医疗设施服务未注入");
            }

            // 检查消防员配置数据
            try {
                boolean anyFirefighterData = firefighterConfigService.hasAnyData();
                if (anyFirefighterData) {
                    hasFirefighterData = true;
                    log.info("找到消防员配置数据");
                } else {
                    log.warn("未找到消防员配置数据");
                }
            } catch (Exception e) {
                log.error("检查消防员配置数据时出错", e);
            }

            // 构建结果消息
            StringBuilder message = new StringBuilder();

            if (!hasMedicalData) {
                message.append("❌ 缺少医疗设施数据：请先导入");
                message.append(year);
                message.append("年的医疗卫生机构数据（实有床位数）\\n");
                result.setCanImport(false);
            }

            if (!hasFirefighterData) {
                message.append("❌ 缺少消防员配置数据：请先在消防员配置表（firefighter_config）中配置各区域的消防员数量\\n");
                result.setCanImport(false);
            }

            if (hasMedicalData && hasFirefighterData) {
                message.append("✅ 导入前置条件检查通过，可以导入数据");
                result.setCanImport(true);
            }

            result.setMessage(message.toString());
            result.setHasMedicalData(hasMedicalData);
            result.setHasFirefighterData(hasFirefighterData);
            result.setMissingMedicalRegions(missingMedicalRegions);
            result.setMissingFirefighterRegions(missingFirefighterRegions);

            log.info("导入前置条件检查完成 - 可以导入: {}, 有医疗数据: {}, 有消防员数据: {}",
                result.isCanImport(), hasMedicalData, hasFirefighterData);

        } catch (Exception e) {
            log.error("检查导入前置条件失败", e);
            result.setCanImport(false);
            result.setMessage("检查导入前置条件时发生错误: " + e.getMessage());
        }

        return result;
    }

    @Override
    public GpkgFieldValidationResult validateGpkgFields(MultipartFile file, String dataType, Integer year) {
        return GpkgUtil.validateGpkgFields(file, dataType, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importFromGpkg(MultipartFile file, Integer year) {
        if (file == null || file.isEmpty() || year == null) {
            log.error("导入参数为空");
            return false;
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
                log.error("无法读取GPKG文件");
                return false;
            }

            try {
                // 获取类型名称
                String[] typeNames = dataStore.getTypeNames();
                if (typeNames == null || typeNames.length == 0) {
                    log.error("GPKG文件中没有找到任何图层");
                    return false;
                }

                // 使用第一个图层
                String layerName = typeNames[0];
                log.info("使用图层: {}", layerName);

                // 获取要素源
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource =
                        dataStore.getFeatureSource(layerName);

                // 获取字段映射（根据年份选择不同的 GPKG 字段映射）
                Map<String, String> fieldMapping = GpkgUtil.getFieldMapping("township", year);

                // 读取所有要素
                Query query = new Query(layerName);
                FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures(query);

                List<SurveyData> dataList = new ArrayList<>();
                try (FeatureIterator<SimpleFeature> features = collection.features()) {
                    while (features.hasNext()) {
                        SimpleFeature feature = features.next();
                        SurveyData data = parseGpkgFeatureToSurveyData(feature, fieldMapping, year);
                        if (data != null && validateSurveyData(data)) {
                            dataList.add(data);
                        }
                    }
                }

                log.info("从GPKG文件解析到{}条数据", dataList.size());

                // 智能批量保存
                return smartBatchSave(dataList);

            } finally {
                dataStore.dispose();
            }

        } catch (IOException e) {
            log.error("导入GPKG文件失败", e);
            return false;
        } finally {
            // 删除临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("删除临时文件失败", e);
                }
            }
        }
    }

    /**
     * 将GPKG要素解析为SurveyData对象
     */
    private SurveyData parseGpkgFeatureToSurveyData(SimpleFeature feature, Map<String, String> fieldMapping, Integer year) {
        try {
            SurveyData data = new SurveyData();
            data.setYear(year);

            // 根据字段映射从GPKG属性中获取值
            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                String gpkgField = entry.getKey();
                String dbField = entry.getValue();

                Object value = feature.getAttribute(gpkgField);
                if (value != null) {
                    setFieldValue(data, dbField, value);
                }
            }

            // 使用配置数据源设置消防员、志愿者、民兵预备役和医院床位数据
            setEnhancedDataFromConfig(data, year);

            return data;
        } catch (Exception e) {
            log.warn("解析GPKG要素失败: {}", feature.getID(), e);
            return null;
        }
    }

    /**
     * 设置字段值到SurveyData对象
     */
    private void setFieldValue(SurveyData data, String fieldName, Object value) {
        if (value == null) {
            return;
        }

        try {
            switch (fieldName) {
                case "township":
                    data.setTownship(getStringValue(value));
                    break;
                case "regionCode":
                    data.setRegionCode(getStringValue(value));
                    break;
                case "province":
                    data.setProvince(getStringValue(value));
                    break;
                case "city":
                    data.setCity(getStringValue(value));
                    break;
                case "county":
                    data.setCounty(getStringValue(value));
                    break;
                case "townshipAddress":
                    data.setTownshipAddress(getStringValue(value));
                    break;
                case "population":
                    data.setPopulation(getLongValue(value));
                    break;
                case "managementStaff":
                    data.setManagementStaff(getIntValue(value));
                    break;
                case "riskAssessment":
                    data.setRiskAssessment(getStringValue(value));
                    break;
                case "fundingAmount":
                    data.setFundingAmount(getBigDecimalValue(value));
                    break;
                case "materialValue":
                    data.setMaterialValue(getBigDecimalValue(value));
                    break;
                case "hospitalBeds":
                    data.setHospitalBeds(getIntValue(value));
                    break;
                case "firefightersCount":
                    data.setFirefightersCount(getIntValue(value));
                    break;
                case "volunteersCount":
                    data.setVolunteersCount(getIntValue(value));
                    break;
                case "militiaReserveCount":
                    data.setMilitiaReserveCount(getIntValue(value));
                    break;
                case "trainingParticipants":
                    data.setTrainingParticipants(getIntValue(value));
                    break;
                case "shelterCapacity":
                    data.setShelterCapacity(getIntValue(value));
                    break;
                default:
                    // 忽略未知字段
                    break;
            }
        } catch (Exception e) {
            log.warn("设置字段值失败: {} = {}", fieldName, value);
        }
    }

    /**
     * 获取字符串值
     */
    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * 获取整数值
     */
    private Integer getIntValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取长整数值
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取双精度浮点数值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取BigDecimal数值（保留4位小数）
     */
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue()).setScale(4, BigDecimal.ROUND_HALF_UP);
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(str).setScale(4, BigDecimal.ROUND_HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
