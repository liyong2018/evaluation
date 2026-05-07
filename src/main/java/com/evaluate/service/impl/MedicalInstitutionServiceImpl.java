package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.dto.ImportResultDTO;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.entity.MedicalInstitution;
import com.evaluate.mapper.GrassrootsOrganizationMapper;
import com.evaluate.mapper.MedicalInstitutionMapper;
import com.evaluate.service.IGrassrootsOrganizationService;
import com.evaluate.service.IMedicalInstitutionService;
import com.evaluate.util.ChengduFunctionalDistrictCodeMapper;
import com.evaluate.util.GpkgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    private static final String IMPORT_CACHE_EMPTY_VALUE = "__EMPTY__";

    @Autowired(required = false)
    private IGrassrootsOrganizationService grassrootsOrganizationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importMedicalInstitutionData(MultipartFile file, Integer year) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 读取表头，建立列名到索引的映射
            Map<String, Integer> columnIndexMap = buildExcelColumnIndexMap(sheet);

            List<MedicalInstitution> medicalInstitutions = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    MedicalInstitution medicalInstitution = new MedicalInstitution();

                    medicalInstitution.setUniqueCode(getCellValueByColumnName(row, columnIndexMap, "id"));
                    medicalInstitution.setVerificationStatus(getCellValueByColumnName(row, columnIndexMap, "verification_status"));
                    medicalInstitution.setUnifiedSocialCreditCode(getCellValueByColumnName(row, columnIndexMap, "tyshxydm"));
                    medicalInstitution.setCodeType(getCellValueByColumnName(row, columnIndexMap, "dmlx"));
                    medicalInstitution.setInstitutionName(getCellValueByColumnName(row, columnIndexMap, "dwmc"));
                    medicalInstitution.setInstitutionAddress(getCellValueByColumnName(row, columnIndexMap, "address"));
                    medicalInstitution.setInstitutionCategoryCode(getCellValueByColumnName(row, columnIndexMap, "ylwsjglbdm"));
                    medicalInstitution.setInstitutionTypeLarge(getCellValueByColumnName(row, columnIndexMap, "yljglxdl"));
                    medicalInstitution.setInstitutionTypeMedium(getCellValueByColumnName(row, columnIndexMap, "yljglxzl"));
                    medicalInstitution.setInstitutionTypeSpecialized(getCellValueByColumnName(row, columnIndexMap, "yljglxzky1"));
                    medicalInstitution.setHospitalLevel(getCellValueByColumnName(row, columnIndexMap, "yydj"));
                    medicalInstitution.setInstitutionNature(getCellValueByColumnName(row, columnIndexMap, "yljgxz"));

                    medicalInstitution.setLandArea(getCellValueAsBigDecimal(getCellByColumnName(row, columnIndexMap, "zdmj")));
                    medicalInstitution.setBuildingArea(getCellValueAsBigDecimal(getCellByColumnName(row, columnIndexMap, "fwjzmj")));
                    medicalInstitution.setEquipmentCountAbove10k(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "wyyssbts")));
                    medicalInstitution.setTotalStaff(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "zgzgrs")));
                    medicalInstitution.setHealthTechnicalPersonnel(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "wsjsryzs")));
                    medicalInstitution.setRegisteredNurses(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "zchsrs")));
                    medicalInstitution.setLogisticsSkillPersonnel(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "gqjnrys")));
                    medicalInstitution.setAnnualTotalVisits(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "ndzzlrcs")));
                    medicalInstitution.setAnnualAdmissionCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "ndryrs")));
                    medicalInstitution.setAnnualDischargeCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "ndcyrs")));
                    medicalInstitution.setActualHospitalBeds(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "syzycws")));
                    medicalInstitution.setNegativePressureBeds(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "fybfcws")));
                    medicalInstitution.setIcuBeds(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "zzjqhlbfcw")));
                    medicalInstitution.setPreHospitalEmergencyPersonnel(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "yqjjzyrys")));
                    medicalInstitution.setEmergencyCommandVehicleCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "jjzhcsl")));
                    medicalInstitution.setTransportAmbulanceCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "yzxjjcsl")));
                    medicalInstitution.setMonitorAmbulanceCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "jhxjjcsl")));
                    medicalInstitution.setNegativePressureAmbulanceCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "fyjjcsl")));
                    medicalInstitution.setBloodCollectionVehicleCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "cxcs")));
                    medicalInstitution.setBloodDeliveryVehicleCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "sxcs")));
                    medicalInstitution.setSecurityPersonnelCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "aqbwrysl")));

                    medicalInstitution.setEmergencyPowerSupply(getCellValueByColumnName(row, columnIndexMap, "yjgdnl"));
                    medicalInstitution.setEmergencyPowerSupplyOther(getCellValueByColumnName(row, columnIndexMap, "yjgdnl_qts"));
                    medicalInstitution.setWaterSupplyMode(getCellValueByColumnName(row, columnIndexMap, "gsfs"));
                    medicalInstitution.setHeatingMode(getCellValueByColumnName(row, columnIndexMap, "gnfs"));
                    medicalInstitution.setEmergencyCommunicationMode(getCellValueByColumnName(row, columnIndexMap, "yjtxbzfs"));
                    medicalInstitution.setEmergencyCommunicationModeOther(getCellValueByColumnName(row, columnIndexMap, "yjtxbzfs_q"));
                    medicalInstitution.setDisasterHistoryType(getCellValueByColumnName(row, columnIndexMap, "cjzsgdzrz1"));
                    medicalInstitution.setDisasterHistoryTypeOther(getCellValueByColumnName(row, columnIndexMap, "cjzsgdzrz2"));
                    medicalInstitution.setEmergencyPlanType(getCellValueByColumnName(row, columnIndexMap, "yyzrzhyjy1"));
                    medicalInstitution.setEmergencyPlanTypeOther(getCellValueByColumnName(row, columnIndexMap, "yyzrzhyjy2"));
                    medicalInstitution.setUnitLeader(getCellValueByColumnName(row, columnIndexMap, "dwfzr"));
                    medicalInstitution.setStatisticalLeader(getCellValueByColumnName(row, columnIndexMap, "tjfzr"));
                    medicalInstitution.setFormFiller(getCellValueByColumnName(row, columnIndexMap, "tbr"));
                    medicalInstitution.setContactPhone(getCellValueByColumnName(row, columnIndexMap, "lxdh"));

                    String reportDateStr = getCellValueByColumnName(row, columnIndexMap, "tbrq");
                    medicalInstitution.setReportDate(getDateValue(reportDateStr));

                    medicalInstitution.setFillingInstructions(getCellValueByColumnName(row, columnIndexMap, "xgqksm"));

                    // 读取行政区划代码（code 字段），用于识别乡镇
                    String codeValue = getCellValueByColumnName(row, columnIndexMap, "code");
                    if (StringUtils.hasText(codeValue)) {
                        medicalInstitution.setOrgCode(codeValue);
                    }

                    // 读取行政区划字段（2020 年及 2024 年格式：dzsheng, dzshi, dzxian, dzxiang）
                    String province = getCellValueByColumnName(row, columnIndexMap, "dzsheng");
                    String city = getCellValueByColumnName(row, columnIndexMap, "dzshi");
                    String county = getCellValueByColumnName(row, columnIndexMap, "dzxian");
                    String township = getCellValueByColumnName(row, columnIndexMap, "dzxiang");

                    if (StringUtils.hasText(province)) {
                        medicalInstitution.setProvince(province);
                        medicalInstitution.setProvinceName(province);
                    }
                    if (StringUtils.hasText(city)) {
                        medicalInstitution.setCity(city);
                        medicalInstitution.setCityName(city);
                    }
                    if (StringUtils.hasText(county)) {
                        medicalInstitution.setCounty(county);
                        medicalInstitution.setCountyName(county);
                    }
                    if (StringUtils.hasText(township)) {
                        medicalInstitution.setTownship(township);
                        medicalInstitution.setTownshipName(township);
                    }

                    medicalInstitution.setYear(year);
                    applyChengduFunctionalDistrictMapping(medicalInstitution, year);

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
        ImportLookupCache importLookupCache = new ImportLookupCache();

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> columnIndexMap = buildExcelColumnIndexMap(sheet);
            boolean isCodeHeaderExcel = columnIndexMap.containsKey("dwmc") && columnIndexMap.containsKey("address");

            List<MedicalInstitution> medicalInstitutions = new ArrayList<>();
            int startRow = isCodeHeaderExcel ? 1 : 2;

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    MedicalInstitution medicalInstitution = new MedicalInstitution();
                    if (isCodeHeaderExcel) {
                        String uniqueCode = firstNonBlank(
                                getCellValueByColumnName(row, columnIndexMap, "id"),
                                getCellValueByColumnName(row, columnIndexMap, "fxpc_datai")
                        );
                        String institutionName = getCellValueByColumnName(row, columnIndexMap, "dwmc");
                        String institutionAddress = getCellValueByColumnName(row, columnIndexMap, "address");
                        if (isLikelyMedicalDescriptionRow(uniqueCode, institutionName, institutionAddress)) {
                            continue;
                        }
                        if (!StringUtils.hasText(uniqueCode) && !StringUtils.hasText(institutionName) && !StringUtils.hasText(institutionAddress)) {
                            continue;
                        }

                        medicalInstitution.setUniqueCode(uniqueCode);
                        medicalInstitution.setVerificationStatus(firstNonBlank(
                                getCellValueByColumnName(row, columnIndexMap, "verification_status"),
                                getCellValueByColumnName(row, columnIndexMap, "fxpc_sjzt_")
                        ));
                        medicalInstitution.setUnifiedSocialCreditCode(firstNonBlank(
                                getCellValueByColumnName(row, columnIndexMap, "tyshxydm"),
                                getCellValueByColumnName(row, columnIndexMap, "jgbm")
                        ));
                        medicalInstitution.setCodeType(getCellValueByColumnName(row, columnIndexMap, "dmlx"));
                        medicalInstitution.setInstitutionName(institutionName);
                        medicalInstitution.setInstitutionAddress(institutionAddress);
                        medicalInstitution.setInstitutionCategoryCode(getCellValueByColumnName(row, columnIndexMap, "ylwsjglbdm"));
                        medicalInstitution.setInstitutionTypeLarge(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yljglxdl")));
                        medicalInstitution.setInstitutionTypeMedium(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yljglxzl")));
                        medicalInstitution.setInstitutionTypeSpecialized(normalizeListText(firstNonBlank(
                                getCellValueByColumnName(row, columnIndexMap, "yljglxzky1"),
                                getCellValueByColumnName(row, columnIndexMap, "yljglxzky2")
                        )));
                        medicalInstitution.setHospitalLevel(normalizeHospitalLevel(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yydj"))));
                        medicalInstitution.setInstitutionNature(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yljgxz")));

                        medicalInstitution.setLandArea(getCellValueAsBigDecimal(getCellByColumnName(row, columnIndexMap, "zdmj")));
                        medicalInstitution.setBuildingArea(getCellValueAsBigDecimal(getCellByColumnName(row, columnIndexMap, "fwjzmj")));
                        medicalInstitution.setEquipmentCountAbove10k(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "wyyssbts")));
                        medicalInstitution.setTotalStaff(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "zgzgrs")));
                        medicalInstitution.setHealthTechnicalPersonnel(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "wsjsryzs")));
                        medicalInstitution.setRegisteredNurses(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "zchsrs")));
                        medicalInstitution.setLogisticsSkillPersonnel(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "gqjnrys")));
                        medicalInstitution.setAnnualTotalVisits(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "ndzzlrcs")));
                        medicalInstitution.setAnnualAdmissionCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "ndryrs")));
                        medicalInstitution.setAnnualDischargeCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "ndcyrs")));
                        medicalInstitution.setActualHospitalBeds(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "syzycws")));
                        medicalInstitution.setNegativePressureBeds(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "fybfcws")));
                        medicalInstitution.setIcuBeds(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "zzjqhlbfcw")));
                        medicalInstitution.setPreHospitalEmergencyPersonnel(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "yqjjzyrys")));
                        medicalInstitution.setEmergencyCommandVehicleCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "jjzhcsl")));
                        medicalInstitution.setTransportAmbulanceCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "yzxjjcsl")));
                        medicalInstitution.setMonitorAmbulanceCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "jhxjjcsl")));
                        medicalInstitution.setNegativePressureAmbulanceCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "fyjjcsl")));
                        medicalInstitution.setBloodCollectionVehicleCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "cxcs")));
                        medicalInstitution.setBloodDeliveryVehicleCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "sxcs")));
                        medicalInstitution.setSecurityPersonnelCount(getCellValueAsInteger(getCellByColumnName(row, columnIndexMap, "aqbwrysl")));

                        medicalInstitution.setEmergencyPowerSupply(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yjgdnl")));
                        medicalInstitution.setEmergencyPowerSupplyOther(getCellValueByColumnName(row, columnIndexMap, "yjgdnl_qts"));
                        medicalInstitution.setWaterSupplyMode(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "gsfs")));
                        medicalInstitution.setHeatingMode(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "gnfs")));
                        medicalInstitution.setEmergencyCommunicationMode(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yjtxbzfs")));
                        medicalInstitution.setEmergencyCommunicationModeOther(getCellValueByColumnName(row, columnIndexMap, "yjtxbzfs_q"));
                        medicalInstitution.setDisasterHistoryType(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "cjzsgdzrz1")));
                        medicalInstitution.setDisasterHistoryTypeOther(getCellValueByColumnName(row, columnIndexMap, "cjzsgdzrz2"));
                        medicalInstitution.setEmergencyPlanType(normalizeListText(getCellValueByColumnName(row, columnIndexMap, "yyzrzhyjy1")));
                        medicalInstitution.setEmergencyPlanTypeOther(getCellValueByColumnName(row, columnIndexMap, "yyzrzhyjy2"));
                        medicalInstitution.setUnitLeader(getCellValueByColumnName(row, columnIndexMap, "dwfzr"));
                        medicalInstitution.setStatisticalLeader(getCellValueByColumnName(row, columnIndexMap, "tjfzr"));
                        medicalInstitution.setFormFiller(getCellValueByColumnName(row, columnIndexMap, "tbr"));
                        medicalInstitution.setContactPhone(getCellValueByColumnName(row, columnIndexMap, "lxdh"));
                        medicalInstitution.setOrgCode(getCellValueByColumnName(row, columnIndexMap, "code"));

                        String province = getCellValueByColumnName(row, columnIndexMap, "dzsheng");
                        String city = getCellValueByColumnName(row, columnIndexMap, "dzshi");
                        String county = getCellValueByColumnName(row, columnIndexMap, "dzxian");
                        String township = getCellValueByColumnName(row, columnIndexMap, "dzxiang");
                        if (StringUtils.hasText(province)) {
                            medicalInstitution.setProvince(province);
                            medicalInstitution.setProvinceName(province);
                        }
                        if (StringUtils.hasText(city)) {
                            medicalInstitution.setCity(city);
                            medicalInstitution.setCityName(city);
                        }
                        if (StringUtils.hasText(county)) {
                            medicalInstitution.setCounty(county);
                            medicalInstitution.setCountyName(county);
                        }
                        if (StringUtils.hasText(township)) {
                            medicalInstitution.setTownship(township);
                            medicalInstitution.setTownshipName(township);
                        }

                        String reportDateStr = getCellValueByColumnName(row, columnIndexMap, "tbrq");
                        medicalInstitution.setReportDate(getDateValue(reportDateStr));
                        medicalInstitution.setFillingInstructions(getCellValueByColumnName(row, columnIndexMap, "xgqksm"));
                    } else {
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
                        medicalInstitution.setReportDate(getDateValue(reportDateStr));
                        medicalInstitution.setFillingInstructions(getCellValueAsString(row.getCell(48)));
                    }

                    medicalInstitution.setYear(year);

                    ensureUniqueCode(medicalInstitution, year, i + 1);
                    if (!StringUtils.hasText(medicalInstitution.getInstitutionName())) {
                        result.addWarning("第" + (i + 1) + "行数据缺少医疗机构名称，已跳过");
                        continue;
                    }

                    // 从地址中解析省市区信息
                    applyNamesFromAddress(medicalInstitution);
                    applyChengduFunctionalDistrictMapping(medicalInstitution, year);

                    checkAddressParsing(medicalInstitution, result, i + 1, year, importLookupCache);
                    applyChengduFunctionalDistrictMapping(medicalInstitution, year);

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
                BatchSaveResult saveResult;
                try {
                    saveResult = smartBatchSaveWithResult(medicalInstitutions);
                } catch (Exception saveException) {
                    if (isUniqueConstraintConflict(saveException)) {
                        log.warn("检测到医疗机构唯一约束与多年份导入冲突，尝试自动修复并重试");
                        fixUniqueConstraint();
                        saveResult = smartBatchSaveWithResult(medicalInstitutions);
                    } else {
                        throw saveException;
                    }
                }
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

    private void ensureUniqueCode(MedicalInstitution institution, Integer year, int rowNum) {
        if (institution == null) {
            return;
        }
        if (StringUtils.hasText(institution.getUniqueCode())) {
            institution.setUniqueCode(institution.getUniqueCode().trim());
            return;
        }
        String name = institution.getInstitutionName() == null ? "" : institution.getInstitutionName().trim();
        String address = institution.getInstitutionAddress() == null ? "" : institution.getInstitutionAddress().trim();
        String seed = String.valueOf(year) + "|" + name + "|" + address + "|" + rowNum;
        String generated = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "")
                .toUpperCase(Locale.ROOT);
        institution.setUniqueCode("AUTO_" + year + "_" + generated);
    }

    private boolean isUniqueConstraintConflict(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (StringUtils.hasText(message)) {
                String lower = message.toLowerCase(Locale.ROOT);
                if ((lower.contains("duplicate entry") || lower.contains("duplicate key"))
                        && (lower.contains("unique_code") || lower.contains("uk_unique_code"))) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isLikelyMedicalDescriptionRow(String uniqueCode, String institutionName, String institutionAddress) {
        String idText = String.valueOf(uniqueCode == null ? "" : uniqueCode).trim();
        String nameText = String.valueOf(institutionName == null ? "" : institutionName).trim();
        String addressText = String.valueOf(institutionAddress == null ? "" : institutionAddress).trim();
        return idText.contains("唯一标识")
                || nameText.contains("医疗卫生机构名称")
                || addressText.contains("医疗卫生机构详细地址");
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
            List<String> normalized = Arrays.stream(parts)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            if (!normalized.isEmpty()) {
                return String.join(";", normalized);
            }
        }
        return text;
    }

    private String normalizeHospitalLevel(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String normalized = value.trim();
        if (!normalized.contains(";")) {
            return normalized;
        }
        String[] parts = normalized.split(";");
        if (parts.length != 2) {
            return normalized;
        }
        String first = parts[0].trim();
        String second = parts[1].trim();
        Set<String> classSet = new HashSet<>(Arrays.asList("一级", "二级", "三级", "未定级"));
        Set<String> gradeSet = new HashSet<>(Arrays.asList("特等", "甲等", "乙等", "丙等", "未定等"));
        if (classSet.contains(first) && gradeSet.contains(second)) {
            return second + ";" + first;
        }
        return normalized;
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
                                    com.evaluate.dto.ImportResultDTO result, int rowNum, Integer year,
                                    ImportLookupCache importLookupCache) {
        String address = institution.getInstitutionAddress();
        if (!StringUtils.hasText(address)) {
            result.addWarning("第" + rowNum + "行: 机构地址为空");
            return;
        }

        String institutionName = StringUtils.hasText(institution.getInstitutionName())
            ? institution.getInstitutionName() : "未知机构";

        // 检查区县是否解析
        if (!StringUtils.hasText(institution.getCountyName())) {
            result.addWarning("第" + rowNum + "行 [" + institutionName + "]: 地址\"" + address +
                    "\"未能解析出区/县信息");
        }

        // 提取并验证街道/乡镇
        // 优先通过 org_code 查找乡镇
        String townshipName = null;
        if (StringUtils.hasText(institution.getOrgCode())) {
            townshipName = extractTownshipFromOrgCodeCached(institution.getOrgCode(), year, importLookupCache);
        }

        // 如果通过 org_code 找不到，再通过地址解析
        if (!StringUtils.hasText(townshipName)) {
            townshipName = extractTownshipFromAddress(address);
        }

        if (StringUtils.hasText(townshipName)) {
            institution.setTownshipName(townshipName);
            // 同时保存到数据库列（用于精确查询匹配）
            institution.setTownship(townshipName);
            // 验证街道/乡镇是否存在于grassroots_organization表中
            if (!isTownshipExistsCached(townshipName, year, importLookupCache)) {
                result.addWarning("第" + rowNum + "行 [" + institutionName + "]: 地址\"" + address +
                        "\"解析的街道/乡镇【" + townshipName + "】在系统中不存在，请先在组织机构管理中添加");
            } else if (!StringUtils.hasText(institution.getOrgCode())) {
                String resolvedOrgCode = resolveTownshipOrgCodeCached(townshipName, institution.getCountyName(), year, importLookupCache);
                if (StringUtils.hasText(resolvedOrgCode)) {
                    institution.setOrgCode(resolvedOrgCode);
                }
            }
        } else {
            result.addWarning("第" + rowNum + "行 [" + institutionName + "]: 地址\"" + address +
                    "\"未能解析出街道/乡镇信息");
        }

        // 尝试提取社区/行政村（不验证，仅用于记录）
        String communityName = extractCommunityFromAddress(address);
        if (StringUtils.hasText(communityName)) {
            institution.setCommunityName(communityName);
        }
    }

    private String extractTownshipFromOrgCodeCached(String orgCode, Integer year, ImportLookupCache importLookupCache) {
        if (!StringUtils.hasText(orgCode)) {
            return null;
        }
        String key = orgCode.trim() + "|" + String.valueOf(year);
        if (importLookupCache.orgCodeToTownshipName.containsKey(key)) {
            String cached = importLookupCache.orgCodeToTownshipName.get(key);
            return IMPORT_CACHE_EMPTY_VALUE.equals(cached) ? null : cached;
        }
        String resolved = extractTownshipFromOrgCode(orgCode, year);
        importLookupCache.orgCodeToTownshipName.put(key, StringUtils.hasText(resolved) ? resolved : IMPORT_CACHE_EMPTY_VALUE);
        return resolved;
    }

    private boolean isTownshipExistsCached(String townshipName, Integer year, ImportLookupCache importLookupCache) {
        if (!StringUtils.hasText(townshipName)) {
            return false;
        }
        String key = townshipName.trim() + "|" + String.valueOf(year);
        if (importLookupCache.townshipExists.containsKey(key)) {
            return importLookupCache.townshipExists.get(key);
        }
        boolean exists = isTownshipExists(townshipName, year);
        importLookupCache.townshipExists.put(key, exists);
        return exists;
    }

    private String resolveTownshipOrgCodeCached(String townshipName, String countyName, Integer year, ImportLookupCache importLookupCache) {
        if (!StringUtils.hasText(townshipName)) {
            return null;
        }
        String normalizedCounty = StringUtils.hasText(countyName) ? countyName.trim() : "";
        String key = townshipName.trim() + "|" + normalizedCounty + "|" + String.valueOf(year);
        if (importLookupCache.townshipToOrgCode.containsKey(key)) {
            String cached = importLookupCache.townshipToOrgCode.get(key);
            return IMPORT_CACHE_EMPTY_VALUE.equals(cached) ? null : cached;
        }
        String resolved = resolveTownshipOrgCode(townshipName, countyName, year);
        importLookupCache.townshipToOrgCode.put(key, StringUtils.hasText(resolved) ? resolved : IMPORT_CACHE_EMPTY_VALUE);
        return resolved;
    }

    /**
     * 从 org_code 中提取乡镇名称
     * 通过查询 grassroots_organization 表获取
     */
    private String extractTownshipFromOrgCode(String orgCode, Integer year) {
        if (!StringUtils.hasText(orgCode)) {
            return null;
        }

        try {
            // 首先尝试直接通过 org_code 查找（带年份）
            GrassrootsOrganization org = grassrootsOrganizationService.getByCode(orgCode.trim(), year);
            if (org != null && StringUtils.hasText(org.getName())) {
                log.debug("通过 org_code 精确匹配到乡镇：code={}, year={}, name={}",
                    orgCode, year, org.getName());
                return org.getName();
            }

            // 如果带年份查找失败，尝试不带年份查找（乡镇代码通常稳定）
            if (year != null) {
                org = grassrootsOrganizationService.getByCode(orgCode.trim(), null);
                if (org != null && StringUtils.hasText(org.getName())) {
                    log.warn("通过 org_code 匹配到乡镇（非指定年份）: code={}, foundYear={}, name={}",
                        orgCode, org.getYear(), org.getName());
                    return org.getName();
                }
            }

            // 如果直接查找失败，尝试通过 likeRight 查找（匹配前缀）
            // 例如：org_code=510132，查找 code 以 510132 开头的乡镇记录
            QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
            wrapper.eq("code", orgCode.trim());  // 先精确匹配 code
            wrapper.eq("level", 4);  // 乡镇级别为 4
            // 不限制年份，查找任意年份的匹配记录
            wrapper.last("LIMIT 1");

            GrassrootsOrganization matchedOrg = grassrootsOrganizationService.getOne(wrapper);
            if (matchedOrg != null && StringUtils.hasText(matchedOrg.getName())) {
                log.warn("通过 org_code 精确匹配到乡镇（非指定年份）: code={}, year={}, name={}",
                    orgCode, matchedOrg.getYear(), matchedOrg.getName());
                return matchedOrg.getName();
            }

            // 如果精确匹配失败，尝试 likeRight 匹配（处理代码长度不一致的情况）
            wrapper = new QueryWrapper<>();
            wrapper.likeRight("code", orgCode.trim());
            wrapper.eq("level", 4);
            wrapper.last("LIMIT 1");

            matchedOrg = grassrootsOrganizationService.getOne(wrapper);
            if (matchedOrg != null && StringUtils.hasText(matchedOrg.getName())) {
                log.warn("通过 org_code 前缀匹配到乡镇：code={}, matchedCode={}, name={}",
                    orgCode, matchedOrg.getCode(), matchedOrg.getName());
                return matchedOrg.getName();
            }

            // 所有匹配都失败，记录警告
            log.warn("通过 org_code 未找到匹配的乡镇：code={}, year={}", orgCode, year);

        } catch (Exception e) {
            log.warn("通过 org_code 查找乡镇失败: {}", orgCode, e);
        }

        return null;
    }

    /**
     * 从地址中提取街道/乡镇名称
     */
    private String extractTownshipFromAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        String normalizedAddress = address.replaceAll("\\s+", "");

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "([\\u4e00-\\u9fff]{2,20})(街道|镇|乡|办事处)");
        java.util.regex.Matcher matcher = pattern.matcher(normalizedAddress);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1) + matcher.group(2);
        }
        return normalizeTownshipCandidate(lastMatch);
    }

    private String normalizeTownshipCandidate(String townshipName) {
        if (!StringUtils.hasText(townshipName)) {
            return null;
        }
        String candidate = townshipName.trim();
        if (!candidate.matches(".*[\\u4e00-\\u9fa5]+.*")) {
            return null;
        }
        int idx = candidate.lastIndexOf('县');
        if (idx >= 0 && idx + 1 < candidate.length()) {
            return candidate.substring(idx + 1);
        }
        idx = candidate.lastIndexOf('区');
        if (idx >= 0 && idx + 1 < candidate.length()) {
            return candidate.substring(idx + 1);
        }
        return candidate;
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

    private String resolveTownshipOrgCode(String townshipName, String countyName, Integer year) {
        if (grassrootsOrganizationService == null || !StringUtils.hasText(townshipName)) {
            return null;
        }
        try {
            QueryWrapper<GrassrootsOrganization> wrapper = new QueryWrapper<>();
            wrapper.select("code");
            wrapper.eq("level", 4);
            wrapper.and(w -> w.eq("name", townshipName).or().eq("township_name", townshipName));
            if (StringUtils.hasText(countyName)) {
                wrapper.eq("county_name", countyName.trim());
            }
            wrapper.and(w -> w.eq("year", year).or().eq("is_baseline", 1));
            wrapper.and(w -> w.isNull("is_deleted").or().eq("is_deleted", 0));
            wrapper.orderByDesc("year");
            wrapper.orderByAsc("is_baseline");
            wrapper.last("LIMIT 1");
            GrassrootsOrganization org = grassrootsOrganizationService.getOne(wrapper);
            return org == null ? null : org.getCode();
        } catch (Exception e) {
            log.warn("解析乡镇org_code失败: {}", townshipName, e);
            return null;
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

    private static class ImportLookupCache {
        private final Map<String, String> orgCodeToTownshipName = new HashMap<>();
        private final Map<String, Boolean> townshipExists = new HashMap<>();
        private final Map<String, String> townshipToOrgCode = new HashMap<>();
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
            Map<String, Long> existingIdMap = loadExistingRecordIdMap(dataList);
            for (int i = 0; i < dataList.size(); i++) {
                MedicalInstitution data = dataList.get(i);
                try {
                    log.debug("处理第{}条医疗机构数据，唯一码：{}，年份：{}", i+1, data.getUniqueCode(), data.getYear());

                    String uniqueYearKey = buildInstitutionUniqueYearKey(data.getUniqueCode(), data.getYear());
                    Long existingId = uniqueYearKey == null ? null : existingIdMap.get(uniqueYearKey);

                    if (existingId != null) {
                        log.debug("更新现有医疗机构记录，ID：{}，唯一码：{}，年份：{}", existingId, data.getUniqueCode(), data.getYear());
                        data.setId(existingId);
                        boolean updateResult = updateById(data);
                        if (updateResult) {
                            result.updateCount++;
                            result.successCount++;
                        }
                    } else {
                        log.info("插入新医疗机构记录，唯一码：{}，年份：{}", data.getUniqueCode(), data.getYear());
                        data.setId(null);
                        boolean saveResult = save(data);
                        if (saveResult) {
                            if (uniqueYearKey != null && data.getId() != null) {
                                existingIdMap.put(uniqueYearKey, data.getId());
                            }
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

    private String buildInstitutionUniqueYearKey(String uniqueCode, Integer year) {
        if (!StringUtils.hasText(uniqueCode) || year == null) {
            return null;
        }
        return uniqueCode.trim() + "|" + year;
    }

    private Map<String, Long> loadExistingRecordIdMap(List<MedicalInstitution> dataList) {
        Map<String, Long> existingIdMap = new HashMap<>();
        Set<String> uniqueCodes = new HashSet<>();
        Set<Integer> years = new HashSet<>();

        for (MedicalInstitution item : dataList) {
            if (item == null) {
                continue;
            }
            if (StringUtils.hasText(item.getUniqueCode())) {
                uniqueCodes.add(item.getUniqueCode().trim());
            }
            if (item.getYear() != null) {
                years.add(item.getYear());
            }
        }

        if (uniqueCodes.isEmpty() || years.isEmpty()) {
            return existingIdMap;
        }

        List<String> allUniqueCodes = new ArrayList<>(uniqueCodes);
        int batchSize = 1000;
        for (int start = 0; start < allUniqueCodes.size(); start += batchSize) {
            int end = Math.min(start + batchSize, allUniqueCodes.size());
            List<String> uniqueCodeBatch = allUniqueCodes.subList(start, end);
            QueryWrapper<MedicalInstitution> wrapper = new QueryWrapper<>();
            wrapper.select("id", "unique_code", "year");
            wrapper.in("unique_code", uniqueCodeBatch);
            wrapper.in("year", years);
            List<MedicalInstitution> existingList = list(wrapper);
            for (MedicalInstitution existing : existingList) {
                String key = buildInstitutionUniqueYearKey(existing.getUniqueCode(), existing.getYear());
                if (key != null && existing.getId() != null) {
                    existingIdMap.put(key, existing.getId());
                }
            }
        }

        return existingIdMap;
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
    public void exportMedicalInstitutionData(Integer year, HttpServletResponse response, List<Long> ids) {
        try {
            List<MedicalInstitution> medicalInstitutions = getMedicalInstitutionByYear(year, null);
            if (ids != null && !ids.isEmpty()) {
                medicalInstitutions = medicalInstitutions.stream()
                        .filter(inst -> ids.contains(inst.getId()))
                        .collect(java.util.stream.Collectors.toList());
            }

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

    @Override
    public Integer sumActualHospitalBedsByRegionCode(String regionCode, Integer year) {
        return baseMapper.sumActualHospitalBedsByRegionCode(regionCode, year);
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

    @Override
    public GpkgFieldValidationResult validateGpkgFields(MultipartFile file, String dataType, Integer year) {
        return GpkgUtil.validateGpkgFields(file, dataType, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importFromGpkg(MultipartFile file, Integer year) {
        long startTime = System.currentTimeMillis();
        ImportResultDTO result = new ImportResultDTO();
        result.setSuccess(false);

        if (file == null || file.isEmpty() || year == null) {
            result.addError("导入参数为空");
            return result;
        }

        log.info("开始导入医疗卫生机构GPKG文件，年份：{}，文件大小：{} bytes", year, file.getSize());

        Path tempFile = null;
        try {
            // 创建临时文件
            tempFile = Files.createTempFile("gpkg_", ".gpkg");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("临时文件创建完成：{}", tempFile);

            // 创建数据存储
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "geopkg");
            params.put("database", tempFile.toAbsolutePath().toString());

            DataStore dataStore = DataStoreFinder.getDataStore(params);
            if (dataStore == null) {
                result.addError("无法读取GPKG文件");
                return result;
            }

            try {
                // 获取类型名称
                String[] typeNames = dataStore.getTypeNames();
                if (typeNames == null || typeNames.length == 0) {
                    result.addError("GPKG文件中没有找到任何图层");
                    return result;
                }

                // 使用第一个图层
                String layerName = typeNames[0];
                log.info("使用图层: {}", layerName);

                // 获取要素源
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource =
                        dataStore.getFeatureSource(layerName);

                // 获取字段映射（根据年份选择不同的 GPKG 字段映射）
                Map<String, String> fieldMapping = GpkgUtil.getFieldMapping("medical", year);

                // 读取所有要素
                Query query = new Query(layerName);
                FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures(query);

                List<MedicalInstitution> dataList = new ArrayList<>();
                List<String> invalidOrgCodeRecords = new ArrayList<>();  // 记录无法识别乡镇的机构
                int totalFeatureCount = 0;
                int parsedSuccessCount = 0;
                int parsedFailCount = 0;

                try (FeatureIterator<SimpleFeature> features = collection.features()) {
                    while (features.hasNext()) {
                        totalFeatureCount++;
                        SimpleFeature feature = features.next();
                        MedicalInstitution data = parseGpkgFeatureToMedicalInstitution(feature, fieldMapping, year);
                        if (data != null) {
                            parsedSuccessCount++;
                            // 检查 org_code 是否有效
                            if (!StringUtils.hasText(data.getOrgCode())) {
                                invalidOrgCodeRecords.add(data.getInstitutionName() != null ?
                                    data.getInstitutionName() : "未知机构");
                            }
                            dataList.add(data);
                        } else {
                            parsedFailCount++;
                            log.warn("解析失败（返回null）: Feature ID={}", feature.getID());
                        }
                    }
                }

                if (parsedFailCount > 0) {
                    throw new RuntimeException("GPKG解析失败，共" + parsedFailCount + "条记录解析异常，已中断导入");
                }

                log.info("===== GPKG解析统计 =====");
                log.info("GPKG文件总要素数: {}", totalFeatureCount);
                log.info("解析成功: {}", parsedSuccessCount);
                log.info("解析失败: {}", parsedFailCount);
                log.info("dataList大小: {}", dataList.size());
                log.info("======================");

                // 如果有无法识别乡镇的记录，终止导入并返回错误
                if (!invalidOrgCodeRecords.isEmpty()) {
                    result.addError("未能识别到以下数据的所属乡镇（code 和 fxpc_xzqhbmd_sjgl 字段都无法识别乡镇）：");
                    for (String name : invalidOrgCodeRecords) {
                        result.addError("  - " + name);
                    }
                    result.addError("请检查 GPKG 文件中这些记录的 code 和 fxpc_xzqhbmd_sjgl 字段，确保至少有一个字段值长度≥9位。");
                    return result;
                }

                log.info("从GPKG文件解析到{}条医疗卫生机构数据", dataList.size());
                result.setTotalCount(dataList.size());

                // 先批量查询已存在的记录（一次性查询所有uniqueCode）
                Set<String> uniqueCodes = new HashSet<>();
                for (MedicalInstitution item : dataList) {
                    if (StringUtils.hasText(item.getUniqueCode())) {
                        uniqueCodes.add(item.getUniqueCode().trim());
                    }
                }

                // 批量查询已存在的记录
                Map<String, MedicalInstitution> existingByUniqueCode = new HashMap<>();

                if (!uniqueCodes.isEmpty()) {
                    QueryWrapper<MedicalInstitution> wrapper = new QueryWrapper<>();
                    wrapper.in("unique_code", uniqueCodes);
                    wrapper.eq("year", year);
                    list(wrapper).forEach(item -> {
                        if (StringUtils.hasText(item.getUniqueCode())) {
                            existingByUniqueCode.put(normalizeCodeKey(item.getUniqueCode()), item);
                        }
                    });
                }
                log.info("已查询到{}条已存在记录（按唯一码）", existingByUniqueCode.size());

                // 批量保存
                int insertCount = 0;
                int updateCount = 0;
                int processedCount = 0;

                for (MedicalInstitution item : dataList) {
                    if (!StringUtils.hasText(item.getUniqueCode())) {
                        item.setUniqueCode(buildFallbackUniqueCode(year));
                    }
                    item.setYear(year);

                    String uniqueCodeKey = normalizeCodeKey(item.getUniqueCode());
                    while (StringUtils.hasText(uniqueCodeKey) && existingByUniqueCode.containsKey(uniqueCodeKey)) {
                        item.setUniqueCode(buildFallbackUniqueCode(year));
                        uniqueCodeKey = normalizeCodeKey(item.getUniqueCode());
                    }

                    log.info("插入新记录: 机构名称={}, uniqueCode={}, year={}",
                        item.getInstitutionName(), item.getUniqueCode(), item.getYear());
                    if (save(item)) {
                        log.info("✓ 插入成功: 机构名称={}, uniqueCode={}, ID={}",
                            item.getInstitutionName(), item.getUniqueCode(), item.getId());
                        existingByUniqueCode.put(uniqueCodeKey, item);
                        insertCount++;
                    } else {
                        throw new RuntimeException("导入中断：插入失败，机构=" + item.getInstitutionName()
                                + "，uniqueCode=" + item.getUniqueCode() + "，年份=" + year);
                    }

                    processedCount++;
                    if (processedCount % 100 == 0) {
                        log.info("已处理 {}/{} 条记录，新增{}条，更新{}条",
                            processedCount, dataList.size(), insertCount, updateCount);
                    }
                }

                log.info("导入完成：共处理{}条，新增{}条，更新{}条", dataList.size(), insertCount, updateCount);

                result.setInsertCount(insertCount);
                result.setUpdateCount(updateCount);
                result.setSuccessCount(insertCount + updateCount);
                result.setSuccess(true);

                long endTime = System.currentTimeMillis();
                log.info("医疗卫生机构GPKG导入成功！总耗时：{}秒", (endTime - startTime) / 1000.0);

            } finally {
                dataStore.dispose();
            }

        } catch (IOException e) {
            log.error("导入GPKG文件失败", e);
            throw new RuntimeException("导入GPKG文件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("导入GPKG文件失败", e);
            throw new RuntimeException("导入中断: " + e.getMessage(), e);
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

        return result;
    }

    /**
     * 将GPKG要素解析为MedicalInstitution对象
     */
    private MedicalInstitution parseGpkgFeatureToMedicalInstitution(SimpleFeature feature, Map<String, String> fieldMapping, Integer year) {
        try {
            MedicalInstitution data = new MedicalInstitution();
            data.setYear(year);

            // 临时存储备用乡镇代码
            String townshipCodeFromFxpc = null;

            // 根据字段映射从GPKG属性中获取值
            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                String gpkgField = entry.getKey();
                String dbField = entry.getValue();

                Object value = feature.getAttribute(gpkgField);
                if (value != null) {
                    // 特殊处理备用乡镇代码
                    if ("townshipCodeFromFxpc".equals(dbField)) {
                        townshipCodeFromFxpc = getStringValue(value);
                    } else {
                        setMedicalFieldValue(data, dbField, value);
                    }
                }
            }

            // 处理 org_code：优先使用 code，如果识别不出乡镇则使用 fxpc_xzqhbmd_sjgl
            String finalOrgCode = resolveOrgCode(data.getOrgCode(), townshipCodeFromFxpc,
                data.getInstitutionName(), data.getProvince(), data.getCity(), data.getCounty());
            data.setOrgCode(finalOrgCode);
            applyChengduFunctionalDistrictMapping(data, year);

            // 存储备用乡镇代码，用于后续乡镇名称识别
            if (StringUtils.hasText(townshipCodeFromFxpc)) {
                data.setTownshipCodeFromFxpc(townshipCodeFromFxpc);
            }

            // 直接通过备用乡镇代码查找并设置乡镇名称
            if (StringUtils.hasText(townshipCodeFromFxpc)) {
                log.debug("尝试通过 fxpc_xzqhbmd_sjgl 查找乡镇：code={}, 机构={}",
                    townshipCodeFromFxpc, data.getInstitutionName());
                String townshipNameFromFxpc = extractTownshipFromOrgCode(townshipCodeFromFxpc, year);
                if (StringUtils.hasText(townshipNameFromFxpc)) {
                    log.debug("通过 fxpc_xzqhbmd_sjgl 找到乡镇：{}, 机构={}",
                        townshipNameFromFxpc, data.getInstitutionName());
                    data.setTownshipName(townshipNameFromFxpc);
                    data.setTownship(townshipNameFromFxpc);
                    applyChengduFunctionalDistrictMapping(data, year);
                } else {
                    log.warn("通过 fxpc_xzqhbmd_sjgl 未找到乡镇：code={}, 机构={}, 省={}/{}/{}",
                        townshipCodeFromFxpc, data.getInstitutionName(),
                        data.getProvince(), data.getCity(), data.getCounty());
                }
            }

            data.setUniqueCode(buildUniqueCodeFromFeature(feature, year));
            if (!StringUtils.hasText(data.getUnifiedSocialCreditCode())) {
                data.setUnifiedSocialCreditCode(getStringValue(feature.getAttribute("tyshxydm")));
            }
            if (!StringUtils.hasText(data.getUnifiedSocialCreditCode())) {
                String codeFromJgbm = getStringValue(feature.getAttribute("jgbm"));
                if (isValidCreditCodeValue(codeFromJgbm)) {
                    data.setUnifiedSocialCreditCode(codeFromJgbm);
                }
            }
            if (!StringUtils.hasText(data.getUnifiedSocialCreditCode())) {
                String codeFromJgdmry = getStringValue(feature.getAttribute("jgdmry"));
                if (isValidCreditCodeValue(codeFromJgdmry)) {
                    data.setUnifiedSocialCreditCode(codeFromJgdmry);
                }
            }
            if (!StringUtils.hasText(data.getCodeType())) {
                data.setCodeType(getStringValue(feature.getAttribute("dmlx")));
            }

            return data;
        } catch (Exception e) {
            log.error("解析GPKG要素失败: Feature ID={}, 错误信息: {}", feature.getID(), e.getMessage(), e);
            // 尝试获取关键字段信息用于诊断
            try {
                String name = feature.getAttribute("dwmc") != null ? feature.getAttribute("dwmc").toString() : "未知";
                String code = feature.getAttribute("code") != null ? feature.getAttribute("code").toString() : "空";
                log.error("  - 机构名称: {}, code: {}", name, code);
            } catch (Exception ex) {
                // 忽略
            }
            throw new RuntimeException("解析GPKG要素失败: Feature ID=" + feature.getID() + ", " + e.getMessage(), e);
        }
    }

    /**
     * 解析 org_code：优先使用 code，如果识别不出乡镇则使用 fxpc_xzqhbmd_sjgl
     * @return 最终的 org_code，如果都无效则返回 null
     */
    private String resolveOrgCode(String primaryCode, String fallbackCode,
            String institutionName, String province, String city, String county) {
        String result = null;

        // 优先使用 primaryCode（来自 code 字段）
        if (StringUtils.hasText(primaryCode)) {
            String code = primaryCode.trim();
            // 检查代码长度是否足以识别乡镇（至少9位）
            if (code.length() >= 9) {
                result = code;
                log.debug("使用 code 字段作为 org_code: {} (机构: {})", code, institutionName);
            } else {
                log.info("code 字段长度不足9位，无法识别乡镇: {} (机构: {})", code, institutionName);
            }
        }

        // 如果 primaryCode 无效，尝试使用 fallbackCode（来自 fxpc_xzqhbmd_sjgl 字段）
        if (result == null && StringUtils.hasText(fallbackCode)) {
            String code = fallbackCode.trim();
            if (code.length() >= 9) {
                result = code;
                log.info("使用 fxpc_xzqhbmd_sjgl 字段作为 org_code: {} (机构: {})", code, institutionName);
            } else {
                log.warn("fxpc_xzqhbmd_sjgl 字段长度也不足9位: {} (机构: {})", code, institutionName);
            }
        }

        // 如果都无效，返回 null（调用方会处理）
        if (result == null) {
            log.warn("未能识别到该数据的所属乡镇，机构名称: {}, 省: {}, 市: {}, 县: {}",
                institutionName, province, city, county);
        }

        return result;
    }

    private void applyChengduFunctionalDistrictMapping(MedicalInstitution data, Integer year) {
        if (data == null || year == null || year < 2025) {
            return;
        }
        String lookupCode = StringUtils.hasText(data.getOrgCode()) ? data.getOrgCode() : data.getTownshipCodeFromFxpc();
        ChengduFunctionalDistrictCodeMapper.Mapping mapping =
                ChengduFunctionalDistrictCodeMapper.findByAnyCode(lookupCode);
        if (mapping == null) {
            return;
        }

        data.setOrgCode(ChengduFunctionalDistrictCodeMapper.normalizeCode(lookupCode));
        data.setProvince("四川省");
        data.setProvinceName("四川省");
        String cityName = mapping.getCountyCode().startsWith("5103") ? "自贡市" : "成都市";
        data.setCity(cityName);
        data.setCityName(cityName);
        data.setCounty(mapping.getCountyName());
        data.setCountyName(mapping.getCountyName());
        data.setTownship(mapping.getTownshipName());
        data.setTownshipName(mapping.getTownshipName());
    }

    /**
     * 设置字段值到MedicalInstitution对象
     */
    private void setMedicalFieldValue(MedicalInstitution data, String fieldName, Object value) {
        if (value == null) {
            return;
        }

        try {
            switch (fieldName) {
                // 基本信息
                case "institutionName":
                    data.setInstitutionName(getStringValue(value));
                    break;
                case "institutionAddress":
                    data.setInstitutionAddress(getStringValue(value));
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
                case "township":
                    data.setTownship(getStringValue(value));
                    break;
                case "villageName":
                    data.setCommunityName(getStringValue(value));
                    break;
                case "contactPhone":
                    if (StringUtils.hasText(data.getContactPhone())) {
                        // 已有联系电话，则拼接地址街号（dzjh）
                        String existing = data.getContactPhone();
                        data.setContactPhone(existing + " " + getStringValue(value));
                    } else {
                        data.setContactPhone(getStringValue(value));
                    }
                    break;
                case "uniqueCode":
                    data.setUniqueCode(getStringValue(value));
                    break;
                case "orgCode":
                    data.setOrgCode(getStringValue(value));
                    break;
                case "codeType":
                    data.setCodeType(getStringValue(value));
                    break;
                case "unifiedSocialCreditCode":
                    data.setUnifiedSocialCreditCode(getStringValue(value));
                    break;

                // 机构分类
                case "institutionCategoryCode":
                    data.setInstitutionCategoryCode(getStringValue(value));
                    break;
                case "institutionTypeLarge":
                    data.setInstitutionTypeLarge(getStringValue(value));
                    break;
                case "institutionTypeMedium":
                    data.setInstitutionTypeMedium(getStringValue(value));
                    break;
                case "institutionTypeSpecialized":
                    data.setInstitutionTypeSpecialized(getStringValue(value));
                    break;
                case "hospitalLevel":
                    data.setHospitalLevel(getStringValue(value));
                    break;
                case "institutionNature":
                    data.setInstitutionNature(getStringValue(value));
                    break;

                // 场地与设备
                case "landArea":
                    data.setLandArea(getDecimalValue(value));
                    break;
                case "buildingArea":
                    data.setBuildingArea(getDecimalValue(value));
                    break;
                case "equipmentCountAbove10k":
                    data.setEquipmentCountAbove10k(getIntValue(value));
                    break;

                // 人员统计
                case "totalStaff":
                    data.setTotalStaff(getIntValue(value));
                    break;
                case "healthTechnicalPersonnel":
                    data.setHealthTechnicalPersonnel(getIntValue(value));
                    break;
                case "registeredNurses":
                    data.setRegisteredNurses(getIntValue(value));
                    break;
                case "logisticsSkillPersonnel":
                    data.setLogisticsSkillPersonnel(getIntValue(value));
                    break;
                case "securityPersonnelCount":
                    data.setSecurityPersonnelCount(getIntValue(value));
                    break;
                case "preHospitalEmergencyPersonnel":
                    data.setPreHospitalEmergencyPersonnel(getIntValue(value));
                    break;

                // 诊疗统计
                case "annualTotalVisits":
                    data.setAnnualTotalVisits(getIntValue(value));
                    break;
                case "annualAdmissionCount":
                    data.setAnnualAdmissionCount(getIntValue(value));
                    break;
                case "annualDischargeCount":
                    data.setAnnualDischargeCount(getIntValue(value));
                    break;

                // 床位统计
                case "actualHospitalBeds":
                    data.setActualHospitalBeds(getIntValue(value));
                    break;
                case "negativePressureBeds":
                    data.setNegativePressureBeds(getIntValue(value));
                    break;
                case "icuBeds":
                    data.setIcuBeds(getIntValue(value));
                    break;

                // 车辆统计
                case "emergencyCommandVehicleCount":
                    data.setEmergencyCommandVehicleCount(getIntValue(value));
                    break;
                case "transportAmbulanceCount":
                    data.setTransportAmbulanceCount(getIntValue(value));
                    break;
                case "monitorAmbulanceCount":
                    data.setMonitorAmbulanceCount(getIntValue(value));
                    break;
                case "negativePressureAmbulanceCount":
                    data.setNegativePressureAmbulanceCount(getIntValue(value));
                    break;
                case "bloodCollectionVehicleCount":
                    data.setBloodCollectionVehicleCount(getIntValue(value));
                    break;
                case "bloodDeliveryVehicleCount":
                    data.setBloodDeliveryVehicleCount(getIntValue(value));
                    break;

                // 应急保障
                case "emergencyPowerSupply":
                    data.setEmergencyPowerSupply(getStringValue(value));
                    break;
                case "emergencyPowerSupplyOther":
                    data.setEmergencyPowerSupplyOther(getStringValue(value));
                    break;
                case "waterSupplyMode":
                    data.setWaterSupplyMode(getStringValue(value));
                    break;
                case "heatingMode":
                    data.setHeatingMode(getStringValue(value));
                    break;
                case "emergencyCommunicationMode":
                    data.setEmergencyCommunicationMode(getStringValue(value));
                    break;
                case "emergencyCommunicationModeOther":
                    data.setEmergencyCommunicationModeOther(getStringValue(value));
                    break;

                // 灾害历史与预案
                case "disasterHistoryType":
                    data.setDisasterHistoryType(getStringValue(value));
                    break;
                case "disasterHistoryTypeOther":
                    data.setDisasterHistoryTypeOther(getStringValue(value));
                    break;
                case "emergencyPlanType":
                    data.setEmergencyPlanType(getStringValue(value));
                    break;
                case "emergencyPlanTypeOther":
                    data.setEmergencyPlanTypeOther(getStringValue(value));
                    break;

                // 负责人信息
                case "unitLeader":
                    data.setUnitLeader(getStringValue(value));
                    break;
                case "statisticalLeader":
                    data.setStatisticalLeader(getStringValue(value));
                    break;
                case "formFiller":
                    data.setFormFiller(getStringValue(value));
                    break;
                case "reportDate":
                    data.setReportDate(getDateValue(value));
                    break;
                case "fillingInstructions":
                    data.setFillingInstructions(getStringValue(value));
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

    private String normalizeCodeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String buildUniqueCodeFromFeature(SimpleFeature feature, Integer year) {
        String ogcFid = getStringValue(feature.getAttribute("ogc_fid"));
        if (StringUtils.hasText(ogcFid)) {
            return "OGC_" + year + "_" + ogcFid;
        }
        return buildFallbackUniqueCode(year);
    }

    private String buildFallbackUniqueCode(Integer year) {
        return "GUID_" + year + "_" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private boolean isValidCreditCodeValue(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String normalized = value.trim();
        return !"无".equals(normalized)
                && !"NULL".equalsIgnoreCase(normalized)
                && !"-".equals(normalized);
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
     * 获取小数值（用于 BigDecimal 字段）
     */
    private java.math.BigDecimal getDecimalValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return java.math.BigDecimal.valueOf(((Number) value).doubleValue());
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取日期值
     */
    private java.time.LocalDate getDateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.time.LocalDate) {
            return (java.time.LocalDate) value;
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            String normalized = str;
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2}\\s+.*")) {
                normalized = normalized.split("\\s+")[0];
            }
            if (normalized.matches("\\d{4}/\\d{2}/\\d{2}")) {
                normalized = normalized.replace('/', '-');
            }
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return java.time.LocalDate.parse(normalized);
            }
            java.time.format.DateTimeFormatter englishDateTimeFormatter =
                    java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.ENGLISH);
            return java.time.ZonedDateTime.parse(str, englishDateTimeFormatter).toLocalDate();
        } catch (Exception e) {
            log.warn("日期解析失败: {}", str);
            return null;
        }
    }

    /**
     * 根据 Excel 列名获取单元格值（字符串）
     * @param row 行
     * @param columnIndexMap 列名到索引的映射
     * @param columnName 列名（Excel 表头中的实际列名，如 code、dzsheng 等）
     * @return 单元格值
     */
    private String getCellValueByColumnName(Row row, Map<String, Integer> columnIndexMap, String columnName) {
        Integer colIndex = columnIndexMap.get(columnName);
        if (colIndex == null) {
            return null;
        }
        return getCellValueAsString(row.getCell(colIndex));
    }

    /**
     * 根据 Excel 列名获取单元格
     * @param row 行
     * @param columnIndexMap 列名到索引的映射
     * @param columnName 列名
     * @return 单元格
     */
    private Cell getCellByColumnName(Row row, Map<String, Integer> columnIndexMap, String columnName) {
        Integer colIndex = columnIndexMap.get(columnName);
        if (colIndex == null) {
            return null;
        }
        return row.getCell(colIndex);
    }

    /**
     * 构建 Excel 列名到索引的映射（从表头行读取）
     * @param sheet Excel sheet
     * @return 列名到索引的映射
     */
    private Map<String, Integer> buildExcelColumnIndexMap(Sheet sheet) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            log.warn("Excel 表头行为空");
            return columnIndexMap;
        }

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = getCellValueAsString(cell);
                if (columnName != null && !columnName.isEmpty()) {
                    // 去除空格和特殊字符，统一转为小写
                    columnName = columnName.trim().toLowerCase();
                    columnIndexMap.put(columnName, i);
                }
            }
        }
        log.info("Excel 列头映射：共识别到 {} 列", columnIndexMap.size());
        return columnIndexMap;
    }
}
