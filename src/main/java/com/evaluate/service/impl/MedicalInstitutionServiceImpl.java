package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.MedicalInstitution;
import com.evaluate.mapper.MedicalInstitutionMapper;
import com.evaluate.service.IMedicalInstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

/**
 * 医疗卫生机构服务实现类
 *
 * @author system
 * @since 2024-11-24
 */
@Slf4j
@Service
public class MedicalInstitutionServiceImpl extends ServiceImpl<MedicalInstitutionMapper, MedicalInstitution> implements IMedicalInstitutionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importMedicalInstitutionData(MultipartFile file, Integer year) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<MedicalInstitution> medicalInstitutions = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 跳过前两行（标题行和说明行），从第3行开始读取数据
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    MedicalInstitution medicalInstitution = new MedicalInstitution();

                    // 解析每一列的数据（根据Excel模板的列顺序）
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

                    // 数值类型字段
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

                    // 文本类型字段
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

                    // 日期类型字段
                    String reportDateStr = getCellValueAsString(row.getCell(47));
                    if (reportDateStr != null && !reportDateStr.isEmpty()) {
                        try {
                            medicalInstitution.setReportDate(LocalDate.parse(reportDateStr, dateFormatter));
                        } catch (Exception e) {
                            log.warn("解析报出日期失败: {}", reportDateStr);
                        }
                    }

                    medicalInstitution.setFillingInstructions(getCellValueAsString(row.getCell(48)));

                    // 设置年份
                    medicalInstitution.setYear(year);

                    medicalInstitutions.add(medicalInstitution);

                } catch (Exception e) {
                    log.error("解析第{}行数据失败: {}", i + 1, e.getMessage());
                    // 可以选择跳过错误行或抛出异常
                }
            }

            workbook.close();

            // 智能批量保存数据
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

    /**
     * 智能批量保存数据：根据 unique_code 和 year 判断是否存在，存在则更新，不存在则插入
     * 这样可以避免不同年份的数据相互覆盖
     */
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

                    // 根据唯一码和年份查找现有记录
                    QueryWrapper<MedicalInstitution> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("unique_code", data.getUniqueCode())
                               .eq("year", data.getYear());

                    MedicalInstitution existingData = getOne(queryWrapper);

                    if (existingData != null) {
                        // 记录已存在，更新现有记录
                        log.debug("更新现有医疗机构记录，ID：{}，唯一码：{}，年份：{}", existingData.getId(), data.getUniqueCode(), data.getYear());
                        data.setId(existingData.getId()); // 保持原有的ID
                        boolean updateResult = updateById(data);
                        if (updateResult) {
                            updateCount++;
                            successCount++;
                        } else {
                            log.error("更新医疗机构记录失败，ID：{}，唯一码：{}，年份：{}", existingData.getId(), data.getUniqueCode(), data.getYear());
                        }
                    } else {
                        // 记录不存在，插入新记录（ID为null，会自动生成）
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
                    throw e; // 重新抛出异常以触发事务回滚
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
        return lambdaQuery()
                .eq(MedicalInstitution::getYear, year)
                .likeRight(trimmedOrgCode != null, MedicalInstitution::getOrgCode, trimmedOrgCode)
                .list();
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
