package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.SurveyDataMapper;
import com.evaluate.service.IOrganizationService;
import com.evaluate.service.ISurveyDataService;
import com.evaluate.service.IFirefighterConfigService;
import com.evaluate.service.IVolunteerMilitiaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<SurveyData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }
        return saveBatch(dataList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importFromExcel(MultipartFile file, Integer year) {
        if (file == null || file.isEmpty()) {
            log.error("Excel文件为空");
            return false;
        }

        if (year == null) {
            log.error("年份参数为空");
            return false;
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<SurveyData> dataList = new ArrayList<>();

            // 跳过前两行表头，从第三行开始读取
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                SurveyData data = parseRowToSurveyData(row, year);
                if (data != null) {
                    // 设置年份
                    data.setYear(year);
                    dataList.add(data);
                }
            }

            return batchSave(dataList);
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            return false;
        }
    }

    /**
     * 解析Excel行数据为SurveyData对象
     * 包含所有字段：唯一码和核实状态也会被处理
     */
    private SurveyData parseRowToSurveyData(Row row, Integer year) {
        try {
            SurveyData data = new SurveyData();

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
            data.setFundingAmount(getCellDoubleValue(row.getCell(25)));

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

            // 处理医院床位数据 - 如果Excel中有hospital_beds列，需要确认列号
            // 暂时注释掉，等确认Excel模板中hospital_beds列的具体位置后再启用
            // data.setHospitalBeds(getCellIntegerValue(row.getCell(医院床位列号)));

            // 第34列：现有储备物资、装备折合金额
            data.setMaterialValue(getCellDoubleValue(row.getCell(34)));

            // 第35列：本级灾害应急避难场所数量
            data.setShelterCount(getCellIntegerValue(row.getCell(35)));

            // 第36列：本级灾害应急避难场所容量
            data.setShelterCapacity(getCellIntegerValue(row.getCell(36)));

            // 第37列：单位负责人
            data.setUnitLeader(getCellStringValue(row.getCell(37)));

            // 第38列：统计负责人
            data.setStatisticsLeader(getCellStringValue(row.getCell(38)));

            // 第39列：填表人
            data.setFormFiller(getCellStringValue(row.getCell(39)));

            // 第40列：联系电话
            data.setContactPhone(getCellStringValue(row.getCell(40)));

            // 第41列：报出日期
            Cell reportDateCell = row.getCell(41);
            if (reportDateCell != null) {
                try {
                    if (reportDateCell.getCellType() == CellType.STRING) {
                        String dateStr = reportDateCell.getStringCellValue();
                        if (StringUtils.hasText(dateStr)) {
                            data.setReportDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                        }
                    } else if (reportDateCell.getCellType() == CellType.NUMERIC) {
                        data.setReportDate(reportDateCell.getLocalDateTimeCellValue().toLocalDate());
                    }
                } catch (Exception e) {
                    log.warn("解析报出日期失败: {}", e.getMessage());
                }
            }

            // 第42列：填写说明
            data.setFillInstructions(getCellStringValue(row.getCell(42)));

            // 使用新的数据源设置消防员、志愿者、民兵预备役数据
            setEnhancedDataFromConfig(data, year);

            return data;
        } catch (Exception e) {
            log.error("解析Excel行数据失败", e);
            return null;
        }
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
                row.createCell(8).setCellValue(data.getFundingAmount() != null ? data.getFundingAmount() : 0);
                row.createCell(9).setCellValue(data.getMaterialValue() != null ? data.getMaterialValue() : 0);
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
                row.createCell(8).setCellValue(data.getFundingAmount() != null ? data.getFundingAmount() : 0.0);
                row.createCell(9).setCellValue(data.getMaterialValue() != null ? data.getMaterialValue() : 0.0);
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

            // 1. 从消防员配置表获取消防员数量
            try {
                Integer firefighters = firefighterConfigService.getFirefighterCountByRegionCode(regionCode);
                if (firefighters != null) {
                    // 使用向后兼容方法设置消防员数量（包括0）
                    data.setFirefighters(firefighters);
                    log.debug("设置消防员数量成功，区域: {}, 数量: {}", regionCode, firefighters);
                }
            } catch (Exception e) {
                log.error("获取消防员配置失败，区域代码: {}", regionCode, e);
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

            // 记录增强数据设置结果
            log.info("增强数据设置完成，区域: {}, 消防员: {}, 志愿者: {}, 民兵预备役: {}",
                regionCode,
                data.getFirefighters(),
                data.getVolunteers(),
                data.getMilitiaReserve());

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
}