package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.entity.MedicalInstitution;
import com.evaluate.mapper.GrassrootsOrganizationMapper;
import com.evaluate.mapper.MedicalInstitutionMapper;
import com.evaluate.service.IGrassrootsOrganizationService;
import com.evaluate.service.IMedicalInstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医疗卫生机构服务实现类
 *
 * @author system
 * @since 2024-11-24
 */
@Slf4j
@Service
public class MedicalInstitutionServiceImpl extends ServiceImpl<MedicalInstitutionMapper, MedicalInstitution> implements IMedicalInstitutionService {

    @Autowired(required = false)
    private IGrassrootsOrganizationService grassrootsOrganizationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importMedicalInstitutionData(MultipartFile file, Integer year) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<MedicalInstitution> medicalInstitutions = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    MedicalInstitution medicalInstitution = new MedicalInstitution();

                    medicalInstitution.setUniqueCode(getCellValueAsString(row.getCell(0)));
                    medicalInstitution.setVerificationStatus(getCellValueAsString(row.getCell(1)));
                    medicalInstitution.setUnifiedSocialCreditCode(getCellValueAsString(row.getCell(2)));
                    medicalInstitution.setCodeType(getCellValueAsString(row.getCell(3)));
                    medicalInstitution.setInstitutionName(getCellValueAsString(row.getCell(4)));
                    medicalInstitution.setInstitutionAddress(getCellValueAsString(row.getCell(5)));
                    medicalInstitution.setInstitutionCategoryCode(getCellValueAsString(row.getCell(6)));
                    medicalInstitution.setInstitutionTypeLarge(getCellValueAsString(row.getCell(7)));
                    medicalInstitution.setInstitutionTypeMedium(getCellValueAsString(row.getCell(8)));
                    medicalInstitution.setInstitutionTypeSpecialized(getCellValueAsString(row.getCell(9)));
                    medicalInstitution.setHospitalLevel(getCellValueAsString(row.getCell(10)));
                    medicalInstitution.setInstitutionNature(getCellValueAsString(row.getCell(11)));

                    medicalInstitution.setLandArea(getCellValueAsBigDecimal(row.getCell(12)));
                    medicalInstitution.setBuildingArea(getCellValueAsBigDecimal(row.getCell(13)));
                    medicalInstitution.setEquipmentCountAbove10k(getCellValueAsInteger(row.getCell(14)));
                    medicalInstitution.setTotalStaff(getCellValueAsInteger(row.getCell(15)));
                    medicalInstitution.setHealthTechnicalPersonnel(getCellValueAsInteger(row.getCell(16)));
                    medicalInstitution.setRegisteredNurses(getCellValueAsInteger(row.getCell(17)));
                    medicalInstitution.setLogisticsSkillPersonnel(getCellValueAsInteger(row.getCell(18)));
                    medicalInstitution.setAnnualTotalVisits(getCellValueAsInteger(row.getCell(19)));
                    medicalInstitution.setAnnualAdmissionCount(getCellValueAsInteger(row.getCell(20)));
                    medicalInstitution.setAnnualDischargeCount(getCellValueAsInteger(row.getCell(21)));
                    medicalInstitution.setActualHospitalBeds(getCellValueAsInteger(row.getCell(22)));
                    medicalInstitution.setNegativePressureBeds(getCellValueAsInteger(row.getCell(23)));
                    medicalInstitution.setIcuBeds(getCellValueAsInteger(row.getCell(24)));
                    medicalInstitution.setPreHospitalEmergencyPersonnel(getCellValueAsInteger(row.getCell(25)));
                    medicalInstitution.setEmergencyCommandVehicleCount(getCellValueAsInteger(row.getCell(26)));
                    medicalInstitution.setTransportAmbulanceCount(getCellValueAsInteger(row.getCell(27)));
                    medicalInstitution.setMonitorAmbulanceCount(getCellValueAsInteger(row.getCell(28)));
                    medicalInstitution.setNegativePressureAmbulanceCount(getCellValueAsInteger(row.getCell(29)));
                    medicalInstitution.setBloodCollectionVehicleCount(getCellValueAsInteger(row.getCell(30)));
                    medicalInstitution.setBloodDeliveryVehicleCount(getCellValueAsInteger(row.getCell(31)));
                    medicalInstitution.setSecurityPersonnelCount(getCellValueAsInteger(row.getCell(32)));

                    medicalInstitution.setEmergencyPowerSupply(getCellValueAsString(row.getCell(33)));
                    medicalInstitution.setEmergencyPowerSupplyOther(getCellValueAsString(row.getCell(34)));
                    medicalInstitution.setWaterSupplyMode(getCellValueAsString(row.getCell(35)));
                    medicalInstitution.setHeatingMode(getCellValueAsString(row.getCell(36)));
                    medicalInstitution.setEmergencyCommunicationMode(getCellValueAsString(row.getCell(37)));
                    medicalInstitution.setEmergencyCommunicationModeOther(getCellValueAsString(row.getCell(38)));
                    medicalInstitution.setDisasterHistoryType(getCellValueAsString(row.getCell(39)));
                    medicalInstitution.setDisasterHistoryTypeOther(getCellValueAsString(row.getCell(40)));
                    medicalInstitution.setEmergencyPlanType(getCellValueAsString(row.getCell(41)));
                    medicalInstitution.setEmergencyPlanTypeOther(getCellValueAsString(row.getCell(42)));
                    medicalInstitution.setUnitLeader(getCellValueAsString(row.getCell(43)));
                    medicalInstitution.setStatisticalLeader(getCellValueAsString(row.getCell(44)));
                    medicalInstitution.setFormFiller(getCellValueAsString(row.getCell(45)));
                    medicalInstitution.setContactPhone(getCellValueAsString(row.getCell(46)));

                    String reportDateStr = getCellValueAsString(row.getCell(47));
                    if (reportDateStr != null && !reportDateStr.isEmpty()) {
                        try {
                            medicalInstitution.setReportDate(LocalDate.parse(reportDateStr, dateFormatter));
                        } catch (Exception e) {
                            log.warn("解析报出日期失败: {}", reportDateStr);
                        }
                    }

                    medicalInstitution.setFillingInstructions(getCellValueAsString(row.getCell(48)));

                    medicalInstitution.setYear(year);

                    medicalInstitutions.add(medicalInstitution);

                } catch (Exception e) {
                    log.error("解析第{}行数据失败: {}", i + 1, e.getMessage());
                }
            }

            workbook.close();

            if (!medicalInstitutions.isEmpty()) {
                boolean result = smartBatchSave(medicalInstitutions);
                log.info("成功导入{}条医疗卫生机构数据", medicalInstitutions.size());
                return result;
            }

        } catch (Exception e) {
            log.error("导入医疗卫生机构数据失败", e);
            throw new RuntimeException("导入数据失败: " + e.getMessage());
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.evaluate.dto.ImportResultDTO importMedicalInstitutionDataWithResult(MultipartFile file, Integer year) {
        com.evaluate.dto.ImportResultDTO result = new com.evaluate.dto.ImportResultDTO();
        result.setSuccess(false);

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<MedicalInstitution> medicalInstitutions = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    MedicalInstitution medicalInstitution = new MedicalInstitution();

                    medicalInstitution.setUniqueCode(getCellValueAsString(row.getCell(0)));
                    medicalInstitution.setVerificationStatus(getCellValueAsString(row.getCell(1)));
                    medicalInstitution.setUnifiedSocialCreditCode(getCellValueAsString(row.getCell(2)));
                    medicalInstitution.setCodeType(getCellValueAsString(row.getCell(3)));
                    medicalInstitution.setInstitutionName(getCellValueAsString(row.getCell(4)));
                    medicalInstitution.setInstitutionAddress(getCellValueAsString(row.getCell(5)));
                    medicalInstitution.setInstitutionCategoryCode(getCellValueAsString(row.getCell(6)));
                    medicalInstitution.setInstitutionTypeLarge(getCellValueAsString(row.getCell(7)));
                    medicalInstitution.setInstitutionTypeMedium(getCellValueAsString(row.getCell(8)));
                    medicalInstitution.setInstitutionTypeSpecialized(getCellValueAsString(row.getCell(9)));
                    medicalInstitution.setHospitalLevel(getCellValueAsString(row.getCell(10)));
                    medicalInstitution.setInstitutionNature(getCellValueAsString(row.getCell(11)));

                    medicalInstitution.setLandArea(getCellValueAsBigDecimal(row.getCell(12)));
                    medicalInstitution.setBuildingArea(getCellValueAsBigDecimal(row.getCell(13)));
                    medicalInstitution.setEquipmentCountAbove10k(getCellValueAsInteger(row.getCell(14)));
                    medicalInstitution.setTotalStaff(getCellValueAsInteger(row.getCell(15)));
                    medicalInstitution.setHealthTechnicalPersonnel(getCellValueAsInteger(row.getCell(16)));
                    medicalInstitution.setRegisteredNurses(getCellValueAsInteger(row.getCell(17)));
                    medicalInstitution.setLogisticsSkillPersonnel(getCellValueAsInteger(row.getCell(18)));
                    medicalInstitution.setAnnualTotalVisits(getCellValueAsInteger(row.getCell(19)));
                    medicalInstitution.setAnnualAdmissionCount(getCellValueAsInteger(row.getCell(20)));
                    medicalInstitution.setAnnualDischargeCount(getCellValueAsInteger(row.getCell(21)));
                    medicalInstitution.setActualHospitalBeds(getCellValueAsInteger(row.getCell(22)));
                    medicalInstitution.setNegativePressureBeds(getCellValueAsInteger(row.getCell(23)));
                    medicalInstitution.setIcuBeds(getCellValueAsInteger(row.getCell(24)));
                    medicalInstitution.setPreHospitalEmergencyPersonnel(getCellValueAsInteger(row.getCell(25)));
                    medicalInstitution.setEmergencyCommandVehicleCount(getCellValueAsInteger(row.getCell(26)));
                    medicalInstitution.setTransportAmbulanceCount(getCellValueAsInteger(row.getCell(27)));
                    medicalInstitution.setMonitorAmbulanceCount(getCellValueAsInteger(row.getCell(28)));
                    medicalInstitution.setNegativePressureAmbulanceCount(getCellValueAsInteger(row.getCell(29)));
                    medicalInstitution.setBloodCollectionVehicleCount(getCellValueAsInteger(row.getCell(30)));
                    medicalInstitution.setBloodDeliveryVehicleCount(getCellValueAsInteger(row.getCell(31)));
                    medicalInstitution.setSecurityPersonnelCount(getCellValueAsInteger(row.getCell(32)));

                    medicalInstitution.setEmergencyPowerSupply(getCellValueAsString(row.getCell(33)));
                    medicalInstitution.setEmergencyPowerSupplyOther(getCellValueAsString(row.getCell(34)));
                    medicalInstitution.setWaterSupplyMode(getCellValueAsString(row.getCell(35)));
                    medicalInstitution.setHeatingMode(getCellValueAsString(row.getCell(36)));
                    medicalInstitution.setEmergencyCommunicationMode(getCellValueAsString(row.getCell(37)));
                    medicalInstitution.setEmergencyCommunicationModeOther(getCellValueAsString(row.getCell(38)));
                    medicalInstitution.setDisasterHistoryType(getCellValueAsString(row.getCell(39)));
                    medicalInstitution.setDisasterHistoryTypeOther(getCellValueAsString(row.getCell(40)));
                    medicalInstitution.setEmergencyPlanType(getCellValueAsString(row.getCell(41)));
                    medicalInstitution.setEmergencyPlanTypeOther(getCellValueAsString(row.getCell(42)));
                    medicalInstitution.setUnitLeader(getCellValueAsString(row.getCell(43)));
                    medicalInstitution.setStatisticalLeader(getCellValueAsString(row.getCell(44)));
                    medicalInstitution.setFormFiller(getCellValueAsString(row.getCell(45)));
                    medicalInstitution.setContactPhone(getCellValueAsString(row.getCell(46)));

                    String reportDateStr = getCellValueAsString(row.getCell(47));
                    if (reportDateStr != null && !reportDateStr.isEmpty()) {
                        try {
                            medicalInstitution.setReportDate(LocalDate.parse(reportDateStr, dateFormatter));
                        } catch (Exception e) {
                            log.warn("解析报出日期失败: {}", reportDateStr);
                        }
                    }

                    medicalInstitution.setFillingInstructions(getCellValueAsString(row.getCell(48)));

                    medicalInstitution.setYear(year);

                    // 从地址中解析省市区信息
                    applyNamesFromAddress(medicalInstitution);

                    checkAddressParsing(medicalInstitution, result, i + 1, year);

                    medicalInstitutions.add(medicalInstitution);

                } catch (Exception e) {
                    log.error("解析第{}行数据失败: {}", i + 1, e.getMessage());
                    result.addWarning("第" + (i + 1) + "行数据解析失败: " + e.getMessage());
                }
            }

            workbook.close();

            result.setTotalCount(medicalInstitutions.size());

            // 如果有地址验证错误，终止导入
            if (result.hasErrors()) {
                result.setSuccess(false);
                result.addError("导入终止：存在地址解析或匹配错误，请修正后重新导入");
                log.warn("医疗卫生机构导入终止：存在地址验证错误");
                return result;
            }

            if (!medicalInstitutions.isEmpty()) {
                BatchSaveResult saveResult = smartBatchSaveWithResult(medicalInstitutions);
                result.setSuccess(saveResult.successCount == medicalInstitutions.size());
                result.setSuccessCount(saveResult.successCount);
                result.setInsertCount(saveResult.insertCount);
                result.setUpdateCount(saveResult.updateCount);
                log.info("成功导入{}条医疗卫生机构数据", medicalInstitutions.size());
            }

        } catch (Exception e) {
            log.error("导入医疗卫生机构数据失败", e);
            result.setSuccess(false);
            result.addWarning("导入失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 从地址中解析省市区信息
     */
    private void applyNamesFromAddress(MedicalInstitution item) {
        if (item == null) {
            return;
        }
        String address = normalizeForMatch(item.getInstitutionAddress());
        if (!StringUtils.hasText(address)) {
            return;
        }

        String provinceName = null;
        String cityName = null;
        String countyName = null;

        int provinceIdx = address.indexOf("省");
        if (provinceIdx >= 0) {
            provinceName = address.substring(0, provinceIdx + 1);
            address = address.substring(provinceIdx + 1);
        }

        int cityIdx = address.indexOf("市");
        if (cityIdx >= 0) {
            cityName = address.substring(0, cityIdx + 1);
            address = address.substring(cityIdx + 1);
        }

        int districtIdx = address.indexOf("区");
        int countyIdx = address.indexOf("县");
        if (districtIdx >= 0 && (countyIdx < 0 || districtIdx < countyIdx)) {
            countyName = address.substring(0, districtIdx + 1);
        } else if (countyIdx >= 0) {
            countyName = address.substring(0, countyIdx + 1);
        }

        if (!StringUtils.hasText(item.getProvinceName()) && StringUtils.hasText(provinceName)) {
            item.setProvinceName(provinceName);
        }
        if (!StringUtils.hasText(item.getCityName()) && StringUtils.hasText(cityName)) {
            item.setCityName(cityName);
        }
        if (!StringUtils.hasText(item.getCountyName()) && StringUtils.hasText(countyName)) {
            item.setCountyName(countyName);
        }

        // 同时保存到数据库列（用于精确查询匹配）
        if (StringUtils.hasText(provinceName)) {
            item.setProvince(provinceName);
        }
        if (StringUtils.hasText(cityName)) {
            item.setCity(cityName);
        }
        if (StringUtils.hasText(countyName)) {
            item.setCounty(countyName);
        }
    }

    /**
     * 标准化地址字符串（去除空格）
     */
    private String normalizeForMatch(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.replaceAll("\\s+", "");
    }

    private void checkAddressParsing(MedicalInstitution institution,
                                    com.evaluate.dto.ImportResultDTO result, int rowNum, Integer year) {
        String address = institution.getInstitutionAddress();
        if (!StringUtils.hasText(address)) {
            result.addError("第" + rowNum + "行: 机构地址为空");
            result.setSuccess(false);
            return;
        }

        String institutionName = StringUtils.hasText(institution.getInstitutionName())
            ? institution.getInstitutionName() : "未知机构";

        // 检查区县是否解析
        if (!StringUtils.hasText(institution.getCountyName())) {
            result.addError("第" + rowNum + "行 [" + institutionName + "]: 地址\"" + address +
                    "\"未能解析出区/县信息");
            result.setSuccess(false);
            return;
        }

        // 提取并验证街道/乡镇
        String townshipName = extractTownshipFromAddress(address);
        if (StringUtils.hasText(townshipName)) {
            institution.setTownshipName(townshipName);
            // 同时保存到数据库列（用于精确查询匹配）
            institution.setTownship(townshipName);
            // 验证街道/乡镇是否存在于grassroots_organization表中
            if (!isTownshipExists(townshipName, year)) {
                result.addError("第" + rowNum + "行 [" + institutionName + "]: 地址\"" + address +
                        "\"解析的街道/乡镇【" + townshipName + "】在系统中不存在，请先在组织机构管理中添加");
                result.setSuccess(false);
            }
        } else {
            result.addError("第" + rowNum + "行 [" + institutionName + "]: 地址\"" + address +
                    "\"未能解析出街道/乡镇信息");
            result.setSuccess(false);
        }

        // 尝试提取社区/行政村（不验证，仅用于记录）
        String communityName = extractCommunityFromAddress(address);
        if (StringUtils.hasText(communityName)) {
            institution.setCommunityName(communityName);
        }
    }

    /**
     * 从地址中提取街道/乡镇名称
     */
    private String extractTownshipFromAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        String normalizedAddress = address.replaceAll("\\s+", "");

        // 尝试匹配 "XX街道"、"XX镇"、"XX乡"、"XX办事处"
        // 使用正向否定断言确保后缀后面不是镇/乡/街道等字
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "([\\u4e00-\\u9fff]{2,10}?)(街道|镇|乡|办事处)(?![镇乡街办])");
        java.util.regex.Matcher matcher = pattern.matcher(normalizedAddress);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(2);
        }

        return null;
    }

    /**
     * 从地址中提取社区/行政村名称
     * 只提取村/社区名称本身，不包含前面的行政区划
     */
    private String extractCommunityFromAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        String normalizedAddress = address.replaceAll("\\s+", "");

        // 先找到乡镇的位置，在乡镇之后查找社区/村
        int townshipIdx = -1;
        String[] townshipMarkers = {"镇", "乡", "街道", "办事处"};
        for (String marker : townshipMarkers) {
            int idx = normalizedAddress.indexOf(marker);
            if (idx >= 0 && idx > townshipIdx) {
                townshipIdx = idx + marker.length();
            }
        }

        // 如果找到乡镇，从乡镇之后开始查找社区/村
        String searchArea = (townshipIdx > 0) ? normalizedAddress.substring(townshipIdx) : normalizedAddress;

        // 尝试匹配社区/村（在乡镇之后的部分）
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "([\\u4e00-\\u9fff]{2,10})(社区居民委员会|社区居委会|居民委员会|居委会|村民委员会|村委会|行政村|社区|村)");
        java.util.regex.Matcher matcher = pattern.matcher(searchArea);

        if (matcher.find()) {
            String base = matcher.group(1);
            String suffix = matcher.group(2);

            // 标准化后缀
            if (suffix.contains("社区")) {
                return base.endsWith("社区") ? base : base + "社区";
            } else if (suffix.contains("村")) {
                return base.endsWith("村") ? base : base + "村";
            }
            return base + suffix;
        }

        return null;
    }

    /**
     * 检查街道/乡镇是否存在于grassroots_organization表中
     * 支持匹配当年数据或基准数据(is_baseline=1)
     */
    private boolean isTownshipExists(String townshipName, Integer year) {
        if (grassrootsOrganizationService == null) {
            return true; // 服务未注入时跳过验证
        }
        try {
            QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
            wrapper.eq("level", 4); // 街道/乡镇级别
            wrapper.and(w -> w.eq("name", townshipName).or().eq("township_name", townshipName));
            // 匹配当年数据或基准数据
            wrapper.and(w -> w.eq("year", year).or().eq("is_baseline", 1));
            wrapper.and(w -> w.isNull("is_deleted").or().eq("is_deleted", 0));
            return grassrootsOrganizationService.count(wrapper) > 0;
        } catch (Exception e) {
            log.warn("检查街道/乡镇是否存在时出错: {}", townshipName, e);
            return false;
        }
    }

    /**
     * 检查社区/行政村是否存在于grassroots_organization表中
     * 支持匹配当年数据或基准数据(is_baseline=1)
     * 支持精确匹配和模糊匹配
     */
    private boolean isCommunityExists(String communityName, String townshipName, Integer year) {
        if (grassrootsOrganizationService == null) {
            return true; // 服务未注入时跳过验证
        }
        try {
            // 先尝试精确匹配
            QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
            wrapper.eq("level", 5); // 社区/行政村级别
            wrapper.and(w -> w.eq("name", communityName).or().eq("community_name", communityName));
            // 匹配当年数据或基准数据
            wrapper.and(w -> w.eq("year", year).or().eq("is_baseline", 1));
            wrapper.and(w -> w.isNull("is_deleted").or().eq("is_deleted", 0));
            long exactCount = grassrootsOrganizationService.count(wrapper);

            if (exactCount > 0) {
                log.debug("精确匹配到社区/行政村: {}", communityName);
                return true;
            }

            // 精确匹配失败，尝试模糊匹配（社区名称包含解析出的名称）
            QueryWrapper<GrassrootsOrganization> fuzzyWrapper = new QueryWrapper<>();
            fuzzyWrapper.eq("level", 5);
            fuzzyWrapper.and(w -> w.like("name", communityName).or().like("community_name", communityName));
            fuzzyWrapper.and(w -> w.eq("year", year).or().eq("is_baseline", 1));
            fuzzyWrapper.and(w -> w.isNull("is_deleted").or().eq("is_deleted", 0));
            long fuzzyCount = grassrootsOrganizationService.count(fuzzyWrapper);

            if (fuzzyCount > 0) {
                log.debug("模糊匹配到社区/行政村: {}", communityName);
                return true;
            }

            log.debug("未匹配到社区/行政村: {} (乡镇: {})", communityName, townshipName);
            return false;
        } catch (Exception e) {
            log.warn("检查社区/行政村是否存在时出错: {}", communityName, e);
            return false;
        }
    }

    private static class BatchSaveResult {
        int successCount = 0;
        int insertCount = 0;
        int updateCount = 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public BatchSaveResult smartBatchSaveWithResult(List<MedicalInstitution> dataList) {
        BatchSaveResult result = new BatchSaveResult();
        if (dataList == null || dataList.isEmpty()) {
            log.warn("智能批量保存医疗机构数据失败：数据列表为空");
            return result;
        }

        log.info("开始智能批量保存医疗机构数据，共{}条记录", dataList.size());
        try {
            for (int i = 0; i < dataList.size(); i++) {
                MedicalInstitution data = dataList.get(i);
                try {
                    log.debug("处理第{}条医疗机构数据，唯一码：{}，年份：{}", i+1, data.getUniqueCode(), data.getYear());

                    QueryWrapper<MedicalInstitution> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("unique_code", data.getUniqueCode())
                               .eq("year", data.getYear());

                    MedicalInstitution existingData = getOne(queryWrapper);

                    if (existingData != null) {
                        log.debug("更新现有医疗机构记录，ID：{}，唯一码：{}，年份：{}", existingData.getId(), data.getUniqueCode(), data.getYear());
                        data.setId(existingData.getId());
                        boolean updateResult = updateById(data);
                        if (updateResult) {
                            result.updateCount++;
                            result.successCount++;
                        }
                    } else {
                        log.debug("插入新医疗机构记录，唯一码：{}，年份：{}", data.getUniqueCode(), data.getYear());
                        data.setId(null);
                        boolean saveResult = save(data);
                        if (saveResult) {
                            result.insertCount++;
                            result.successCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("处理第{}条医疗机构数据失败，唯一码：{}，年份：{}", i+1, data.getUniqueCode(), data.getYear(), e);
                    throw e;
                }
            }

            log.info("智能批量保存医疗机构数据完成，成功{}条，其中插入{}条，更新{}条",
                    result.successCount, result.insertCount, result.updateCount);
        } catch (Exception e) {
            log.error("智能批量保存医疗机构数据失败", e);
            throw e;
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean smartBatchSave(List<MedicalInstitution> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("智能批量保存医疗机构数据失败：数据列表为空");
            return false;
        }

        log.info("开始智能批量保存医疗机构数据，共{}条记录", dataList.size());
        try {
            int successCount = 0;
            int updateCount = 0;
            int insertCount = 0;

            for (int i = 0; i < dataList.size(); i++) {
                MedicalInstitution data = dataList.get(i);
                try {
                    log.debug("处理第{}条医疗机构数据，唯一码：{}，年份：{}", i+1, data.getUniqueCode(), data.getYear());

                    QueryWrapper<MedicalInstitution> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("unique_code", data.getUniqueCode())
                               .eq("year", data.getYear());

                    MedicalInstitution existingData = getOne(queryWrapper);

                    if (existingData != null) {
                        log.debug("更新现有医疗机构记录，ID：{}，唯一码：{}，年份：{}", existingData.getId(), data.getUniqueCode(), data.getYear());
                        data.setId(existingData.getId());
                        boolean updateResult = updateById(data);
                        if (updateResult) {
                            updateCount++;
                            successCount++;
                        } else {
                            log.error("更新医疗机构记录失败，ID：{}，唯一码：{}，年份：{}", existingData.getId(), data.getUniqueCode(), data.getYear());
                        }
                    } else {
                        log.debug("插入新医疗机构记录，唯一码：{}，年份：{}", data.getUniqueCode(), data.getYear());
                        data.setId(null);
                        boolean saveResult = save(data);
                        if (saveResult) {
                            insertCount++;
                            successCount++;
                        } else {
                            log.error("插入医疗机构记录失败，唯一码：{}，年份：{}", data.getUniqueCode(), data.getYear());
                        }
                    }
                } catch (Exception e) {
                    log.error("处理第{}条医疗机构数据失败，唯一码：{}，年份：{}", i+1, data.getUniqueCode(), data.getYear(), e);
                    throw e;
                }
            }

            log.info("智能批量保存医疗机构数据完成，成功{}条，其中插入{}条，更新{}条", successCount, insertCount, updateCount);
            return successCount == dataList.size();
        } catch (Exception e) {
            log.error("智能批量保存医疗机构数据失败", e);
            throw e;
        }
    }

    @Override
    public List<MedicalInstitution> getMedicalInstitutionByYear(Integer year, String orgCode) {
        String trimmedOrgCode = StringUtils.hasText(orgCode) ? orgCode.trim() : null;
        List<MedicalInstitution> institutions = lambdaQuery()
                .eq(MedicalInstitution::getYear, year)
                .likeRight(trimmedOrgCode != null, MedicalInstitution::getOrgCode, trimmedOrgCode)
                .list();

        if (!institutions.isEmpty()) {
            for (MedicalInstitution institution : institutions) {
                extractCommunityFromAddress(institution);
            }
        }

        return institutions;
    }

    private void extractCommunityFromAddress(MedicalInstitution institution) {
        if (institution == null || !StringUtils.hasText(institution.getInstitutionAddress())) {
            return;
        }

        if (StringUtils.hasText(institution.getCommunityName())) {
            return;
        }

        String address = institution.getInstitutionAddress().trim().replaceAll("\\s+", "");
        if (!StringUtils.hasText(address)) {
            return;
        }

        String suffixGroup = "(社区居民委员会|社区居委会|居民委员会|居委会|村民委员会|村委会|行政村|社区|村)";

        String base = null;
        String suffix = null;

        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("(?:省|市|县|区|镇|乡|街道|办事处)([\\u4e00-\\u9fff]{2,20})" + suffixGroup);
        java.util.regex.Matcher m1 = p1.matcher(address);
        while (m1.find()) {
            base = m1.group(1);
            suffix = m1.group(2);
        }

        if (!StringUtils.hasText(base) || !StringUtils.hasText(suffix)) {
            java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("([\\u4e00-\\u9fff]{2,20})" + suffixGroup);
            java.util.regex.Matcher m2 = p2.matcher(address);
            while (m2.find()) {
                base = m2.group(1);
                suffix = m2.group(2);
            }
        }

        if (!StringUtils.hasText(base) || !StringUtils.hasText(suffix)) {
            return;
        }

        String normalized = normalizeCommunityOrVillageName(base, suffix);
        if (StringUtils.hasText(normalized)) {
            institution.setCommunityName(normalized);
        }
    }

    private String normalizeCommunityOrVillageName(String base, String suffix) {
        if (!StringUtils.hasText(base) || !StringUtils.hasText(suffix)) {
            return null;
        }

        String trimmedBase = base.trim();
        String[] separators = new String[] { "办事处", "街道", "镇", "乡", "区", "县", "市", "省" };
        for (String sep : separators) {
            int idx = trimmedBase.lastIndexOf(sep);
            if (idx >= 0) {
                trimmedBase = trimmedBase.substring(idx + sep.length()).trim();
            }
        }

        if (!StringUtils.hasText(trimmedBase)) {
            return null;
        }

        if ("社区居民委员会".equals(suffix) || "社区居委会".equals(suffix) || "居民委员会".equals(suffix) || "居委会".equals(suffix)) {
            return trimmedBase.endsWith("社区") ? trimmedBase : trimmedBase + "社区";
        }

        if ("村民委员会".equals(suffix) || "村委会".equals(suffix)) {
            return trimmedBase.endsWith("村") ? trimmedBase : trimmedBase + "村";
        }

        if ("行政村".equals(suffix)) {
            return trimmedBase.endsWith("村") ? trimmedBase : trimmedBase + "村";
        }

        if ("社区".equals(suffix)) {
            return trimmedBase.endsWith("社区") ? trimmedBase : trimmedBase + "社区";
        }

        if ("村".equals(suffix)) {
            return trimmedBase.endsWith("村") ? trimmedBase : trimmedBase + "村";
        }

        return trimmedBase + suffix;
    }

    @Override
    public List<MedicalInstitution> searchByInstitutionName(String institutionName) {
        return lambdaQuery()
                .like(MedicalInstitution::getInstitutionName, institutionName)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMedicalInstitution(MedicalInstitution medicalInstitution) {
        if (medicalInstitution == null || medicalInstitution.getId() == null) {
            return false;
        }
        return updateById(medicalInstitution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public void exportMedicalInstitutionData(Integer year, HttpServletResponse response) {
        try {
            List<MedicalInstitution> medicalInstitutions = getMedicalInstitutionByYear(year, null);

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("医疗卫生机构数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "唯一码", "核实状态", "统一社会信用代码/机构编码", "代码类型", "医疗卫生机构名称",
                "医疗卫生机构详细地址", "医疗卫生机构类别代码", "医疗机构类型（大类）", "医疗机构类型（中类）",
                "医疗机构类型（专科医院分类）", "医院等级", "医疗机构性质", "占地面积", "房屋建筑面积",
                "万元以上设备台数", "在岗职工人数", "卫生技术人员总数", "注册护士人数", "工勤技能人员数",
                "年度总诊疗人次数", "年度入院人数", "年度出院人数", "实有住院床位数", "负压病房床位数",
                "重症加强护理病房（ICU）床位数", "院前急救专业人员数", "急救指挥车数量", "运转型急救车数量",
                "监护型急救车数量", "负压急救车数量", "采血车数", "送血车数", "安全保卫人员数量",
                "应急供电能力", "应急供电能力-其他项说明", "供水方式", "供暖方式", "应急通信保障方式",
                "应急通信保障方式-其他项说明", "曾经遭受过的自然灾害类型", "曾经遭受过的自然灾害类型-其他说明",
                "已有自然灾害应急预案类型", "已有自然灾害应急预案类型-其他说明", "单位负责人", "统计负责人",
                "填表人", "联系电话", "报出日期", "填写说明", "数据年份"
            };

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 填充数据行
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 0; i < medicalInstitutions.size(); i++) {
                Row row = sheet.createRow(i + 1);
                MedicalInstitution item = medicalInstitutions.get(i);

                int col = 0;
                row.createCell(col++).setCellValue(item.getUniqueCode() != null ? item.getUniqueCode() : "");
                row.createCell(col++).setCellValue(item.getVerificationStatus() != null ? item.getVerificationStatus() : "");
                row.createCell(col++).setCellValue(item.getUnifiedSocialCreditCode() != null ? item.getUnifiedSocialCreditCode() : "");
                row.createCell(col++).setCellValue(item.getCodeType() != null ? item.getCodeType() : "");
                row.createCell(col++).setCellValue(item.getInstitutionName() != null ? item.getInstitutionName() : "");
                row.createCell(col++).setCellValue(item.getInstitutionAddress() != null ? item.getInstitutionAddress() : "");
                row.createCell(col++).setCellValue(item.getInstitutionCategoryCode() != null ? item.getInstitutionCategoryCode() : "");
                row.createCell(col++).setCellValue(item.getInstitutionTypeLarge() != null ? item.getInstitutionTypeLarge() : "");
                row.createCell(col++).setCellValue(item.getInstitutionTypeMedium() != null ? item.getInstitutionTypeMedium() : "");
                row.createCell(col++).setCellValue(item.getInstitutionTypeSpecialized() != null ? item.getInstitutionTypeSpecialized() : "");
                row.createCell(col++).setCellValue(item.getHospitalLevel() != null ? item.getHospitalLevel() : "");
                row.createCell(col++).setCellValue(item.getInstitutionNature() != null ? item.getInstitutionNature() : "");
                row.createCell(col++).setCellValue(item.getLandArea() != null ? item.getLandArea().doubleValue() : 0);
                row.createCell(col++).setCellValue(item.getBuildingArea() != null ? item.getBuildingArea().doubleValue() : 0);
                row.createCell(col++).setCellValue(item.getEquipmentCountAbove10k() != null ? item.getEquipmentCountAbove10k() : 0);
                row.createCell(col++).setCellValue(item.getTotalStaff() != null ? item.getTotalStaff() : 0);
                row.createCell(col++).setCellValue(item.getHealthTechnicalPersonnel() != null ? item.getHealthTechnicalPersonnel() : 0);
                row.createCell(col++).setCellValue(item.getRegisteredNurses() != null ? item.getRegisteredNurses() : 0);
                row.createCell(col++).setCellValue(item.getLogisticsSkillPersonnel() != null ? item.getLogisticsSkillPersonnel() : 0);
                row.createCell(col++).setCellValue(item.getAnnualTotalVisits() != null ? item.getAnnualTotalVisits() : 0);
                row.createCell(col++).setCellValue(item.getAnnualAdmissionCount() != null ? item.getAnnualAdmissionCount() : 0);
                row.createCell(col++).setCellValue(item.getAnnualDischargeCount() != null ? item.getAnnualDischargeCount() : 0);
                row.createCell(col++).setCellValue(item.getActualHospitalBeds() != null ? item.getActualHospitalBeds() : 0);
                row.createCell(col++).setCellValue(item.getNegativePressureBeds() != null ? item.getNegativePressureBeds() : 0);
                row.createCell(col++).setCellValue(item.getIcuBeds() != null ? item.getIcuBeds() : 0);
                row.createCell(col++).setCellValue(item.getPreHospitalEmergencyPersonnel() != null ? item.getPreHospitalEmergencyPersonnel() : 0);
                row.createCell(col++).setCellValue(item.getEmergencyCommandVehicleCount() != null ? item.getEmergencyCommandVehicleCount() : 0);
                row.createCell(col++).setCellValue(item.getTransportAmbulanceCount() != null ? item.getTransportAmbulanceCount() : 0);
                row.createCell(col++).setCellValue(item.getMonitorAmbulanceCount() != null ? item.getMonitorAmbulanceCount() : 0);
                row.createCell(col++).setCellValue(item.getNegativePressureAmbulanceCount() != null ? item.getNegativePressureAmbulanceCount() : 0);
                row.createCell(col++).setCellValue(item.getBloodCollectionVehicleCount() != null ? item.getBloodCollectionVehicleCount() : 0);
                row.createCell(col++).setCellValue(item.getBloodDeliveryVehicleCount() != null ? item.getBloodDeliveryVehicleCount() : 0);
                row.createCell(col++).setCellValue(item.getSecurityPersonnelCount() != null ? item.getSecurityPersonnelCount() : 0);
                row.createCell(col++).setCellValue(item.getEmergencyPowerSupply() != null ? item.getEmergencyPowerSupply() : "");
                row.createCell(col++).setCellValue(item.getEmergencyPowerSupplyOther() != null ? item.getEmergencyPowerSupplyOther() : "");
                row.createCell(col++).setCellValue(item.getWaterSupplyMode() != null ? item.getWaterSupplyMode() : "");
                row.createCell(col++).setCellValue(item.getHeatingMode() != null ? item.getHeatingMode() : "");
                row.createCell(col++).setCellValue(item.getEmergencyCommunicationMode() != null ? item.getEmergencyCommunicationMode() : "");
                row.createCell(col++).setCellValue(item.getEmergencyCommunicationModeOther() != null ? item.getEmergencyCommunicationModeOther() : "");
                row.createCell(col++).setCellValue(item.getDisasterHistoryType() != null ? item.getDisasterHistoryType() : "");
                row.createCell(col++).setCellValue(item.getDisasterHistoryTypeOther() != null ? item.getDisasterHistoryTypeOther() : "");
                row.createCell(col++).setCellValue(item.getEmergencyPlanType() != null ? item.getEmergencyPlanType() : "");
                row.createCell(col++).setCellValue(item.getEmergencyPlanTypeOther() != null ? item.getEmergencyPlanTypeOther() : "");
                row.createCell(col++).setCellValue(item.getUnitLeader() != null ? item.getUnitLeader() : "");
                row.createCell(col++).setCellValue(item.getStatisticalLeader() != null ? item.getStatisticalLeader() : "");
                row.createCell(col++).setCellValue(item.getFormFiller() != null ? item.getFormFiller() : "");
                row.createCell(col++).setCellValue(item.getContactPhone() != null ? item.getContactPhone() : "");
                row.createCell(col++).setCellValue(item.getReportDate() != null ? item.getReportDate().format(dateFormatter) : "");
                row.createCell(col++).setCellValue(item.getFillingInstructions() != null ? item.getFillingInstructions() : "");
                row.createCell(col++).setCellValue(item.getYear() != null ? item.getYear() : 0);
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "医疗卫生机构数据_" + year + "_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 写入响应流
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }

            workbook.close();

        } catch (Exception e) {
            log.error("导出医疗卫生机构数据失败", e);
            throw new RuntimeException("导出数据失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("医疗卫生机构导入模板");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "唯一码", "核实状态", "统一社会信用代码/机构编码", "代码类型", "医疗卫生机构名称",
                "医疗卫生机构详细地址", "医疗卫生机构类别代码", "医疗机构类型（大类）", "医疗机构类型（中类）",
                "医疗机构类型（专科医院分类）", "医院等级", "医疗机构性质", "占地面积", "房屋建筑面积",
                "万元以上设备台数", "在岗职工人数", "卫生技术人员总数", "注册护士人数", "工勤技能人员数",
                "年度总诊疗人次数", "年度入院人数", "年度出院人数", "实有住院床位数", "负压病房床位数",
                "重症加强护理病房（ICU）床位数", "院前急救专业人员数", "急救指挥车数量", "运转型急救车数量",
                "监护型急救车数量", "负压急救车数量", "采血车数", "送血车数", "安全保卫人员数量",
                "应急供电能力", "应急供电能力-其他项说明", "供水方式", "供暖方式", "应急通信保障方式",
                "应急通信保障方式-其他项说明", "曾经遭受过的自然灾害类型", "曾经遭受过的自然灾害类型-其他说明",
                "已有自然灾害应急预案类型", "已有自然灾害应急预案类型-其他说明", "单位负责人", "统计负责人",
                "填表人", "联系电话", "报出日期", "填写说明"
            };

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "医疗卫生机构导入模板.xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 写入响应流
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }

            workbook.close();

        } catch (Exception e) {
            log.error("下载导入模板失败", e);
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }

    // 辅助方法：获取单元格字符串值
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    // 辅助方法：获取单元格整数值
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : Integer.parseInt(str);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法：获取单元格BigDecimal值
    private java.math.BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return java.math.BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : new java.math.BigDecimal(str);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Integer sumActualHospitalBedsByTownship(String townshipAddress, Integer year) {
        return baseMapper.sumActualHospitalBedsByTownship(townshipAddress, year);
    }

    /**
     * 修改数据库唯一约束，支持多年度数据
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean fixUniqueConstraint() {
        try {
            log.info("开始修改医疗机构表唯一约束...");

            // 尝试添加新的复合唯一约束（如果不存在的话）
            // 使用IGNORE关键字避免约束冲突
            try {
                int result = baseMapper.addCompositeUniqueIndex();
                log.info("成功添加复合唯一约束");
            } catch (Exception e) {
                log.warn("添加复合唯一约束失败，尝试先删除旧约束: {}", e.getMessage());

                // 尝试删除旧的唯一约束（可能存在多种命名方式）
                try {
                    baseMapper.dropOldUniqueIndex();
                } catch (Exception e1) {
                    log.debug("删除unique_code约束失败: {}", e1.getMessage());
                }

                try {
                    baseMapper.dropOldUniqueIndex2();
                } catch (Exception e1) {
                    log.debug("删除medical_institution.unique_code约束失败: {}", e1.getMessage());
                }

                try {
                    baseMapper.dropOldUniqueIndex3();
                } catch (Exception e1) {
                    log.debug("删除uk_unique_code约束失败: {}", e1.getMessage());
                }

                // 再次尝试添加新的复合唯一约束
                int result = baseMapper.addCompositeUniqueIndex();
                log.info("重新添加复合唯一约束成功");
            }

            log.info("成功修改医疗机构表唯一约束，改为复合约束(unique_code, year)");
            return true;
        } catch (Exception e) {
            log.error("修改医疗机构表唯一约束失败", e);
            throw new RuntimeException("修改数据库约束失败: " + e.getMessage());
        }
    }

    @Override
    public boolean hasAnyDataForYear(Integer year) {
        try {
            QueryWrapper<MedicalInstitution> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year);
            long count = count(wrapper);
            return count > 0;
        } catch (Exception e) {
            log.error("检查{}年医疗设施数据时出错", year, e);
            return false;
        }
    }
}
