package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.SurveyDataMapper;
import com.evaluate.service.ISurveyDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 调查数据服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SurveyDataServiceImpl extends ServiceImpl<SurveyDataMapper, SurveyData> implements ISurveyDataService {

    @Override
    public List<SurveyData> getBySurveyName(String surveyName) {
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.eq("township", surveyName);
        return list(wrapper);
    }

    @Override
    public List<SurveyData> getBySurveyRegion(String surveyRegion) {
        log.info("开始查询地区数据，输入参数: '{}'", surveyRegion);
        
        // 首先查看数据库中所有的township值，用于调试
        QueryWrapper<SurveyData> debugWrapper = new QueryWrapper<>();
        debugWrapper.select("DISTINCT township").isNotNull("township");
        List<SurveyData> allTownships = list(debugWrapper);
        log.info("数据库中所有township值: {}", 
            allTownships.stream().map(SurveyData::getTownship).collect(java.util.stream.Collectors.toList()));
        
        // 优先进行精确匹配乡镇名称
        QueryWrapper<SurveyData> exactWrapper = new QueryWrapper<>();
        exactWrapper.eq("township", surveyRegion);
        
        List<SurveyData> result = list(exactWrapper);
        log.info("精确匹配乡镇名称 '{}' 查询到 {} 条调查数据", surveyRegion, result.size());
        
        // 如果精确匹配没有找到数据，尝试模糊匹配
        if (result.isEmpty()) {
            QueryWrapper<SurveyData> likeWrapper = new QueryWrapper<>();
            likeWrapper.like("township", surveyRegion)
                      .or().like("county", surveyRegion)
                      .or().like("city", surveyRegion)
                      .or().like("province", surveyRegion);
            result = list(likeWrapper);
            log.info("模糊匹配地区名称 '{}' 查询到 {} 条调查数据", surveyRegion, result.size());
        }
        
        return result;
    }

    @Override
    public List<SurveyData> getByIndicatorCode(String indicatorCode) {
        // 由于survey_data表没有indicator_code字段，返回空列表
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<SurveyData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }
        
        // 验证数据
        for (SurveyData data : dataList) {
            if (!validateSurveyData(data)) {
                log.error("调查数据验证失败: {}", data);
                return false;
            }
        }
        
        return saveBatch(dataList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("Excel文件为空");
            return false;
        }
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<SurveyData> dataList = new ArrayList<>();
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                SurveyData data = parseRowToSurveyData(row);
                if (data != null) {
                    dataList.add(data);
                }
            }
            
            return batchSave(dataList);
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            return false;
        }
    }

    @Override
    public byte[] exportToExcel(String surveyName) {
        List<SurveyData> dataList = getBySurveyName(surveyName);
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("调查数据");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"行政区代码", "省名称", "市名称", "县名称", "乡镇名称", 
                              "常住人口", "管理人员", "风险评估", "资金投入", "物资价值"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                SurveyData data = dataList.get(i);
                
                row.createCell(0).setCellValue(data.getRegionCode());
                row.createCell(1).setCellValue(data.getProvince());
                row.createCell(2).setCellValue(data.getCity());
                row.createCell(3).setCellValue(data.getCounty());
                row.createCell(4).setCellValue(data.getTownship());
                row.createCell(5).setCellValue(data.getPopulation() != null ? data.getPopulation() : 0);
                row.createCell(6).setCellValue(data.getManagementStaff() != null ? data.getManagementStaff() : 0);
                row.createCell(7).setCellValue(data.getRiskAssessment());
                row.createCell(8).setCellValue(data.getFundingAmount() != null ? data.getFundingAmount() : 0);
                row.createCell(9).setCellValue(data.getMaterialValue() != null ? data.getMaterialValue() : 0);
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
        // survey_data表没有charset_attribute字段，返回空列表
        return new ArrayList<>();
    }

    @Override
    public boolean validateSurveyData(SurveyData surveyData) {
        if (surveyData == null) {
            return false;
        }
        
        // 验证必填字段
        if (!StringUtils.hasText(surveyData.getRegionCode()) ||
            !StringUtils.hasText(surveyData.getProvince()) ||
            !StringUtils.hasText(surveyData.getTownship()) ||
            surveyData.getPopulation() == null) {
            return false;
        }
        
        // 验证人口数量范围
        if (surveyData.getPopulation() < 0) {
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSurveyDataAndResults(String surveyName) {
        // 删除调查数据
        QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
        wrapper.eq("township", surveyName);
        return remove(wrapper);
    }

    /**
     * 解析Excel行数据为SurveyData对象
     */
    private SurveyData parseRowToSurveyData(Row row) {
        try {
            SurveyData data = new SurveyData();
            
            data.setRegionCode(getCellStringValue(row.getCell(0)));
            data.setProvince(getCellStringValue(row.getCell(1)));
            data.setCity(getCellStringValue(row.getCell(2)));
            data.setCounty(getCellStringValue(row.getCell(3)));
            data.setTownship(getCellStringValue(row.getCell(4)));
            
            Cell populationCell = row.getCell(5);
            if (populationCell != null) {
                data.setPopulation((long) populationCell.getNumericCellValue());
            }
            
            Cell managementCell = row.getCell(6);
            if (managementCell != null) {
                data.setManagementStaff((int) managementCell.getNumericCellValue());
            }
            
            data.setRiskAssessment(getCellStringValue(row.getCell(7)));
            
            Cell fundingCell = row.getCell(8);
            if (fundingCell != null) {
                data.setFundingAmount(fundingCell.getNumericCellValue());
            }
            
            Cell materialCell = row.getCell(9);
            if (materialCell != null) {
                data.setMaterialValue(materialCell.getNumericCellValue());
            }
            
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
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}