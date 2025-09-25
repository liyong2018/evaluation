package com.evaluate.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel工具类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class ExcelUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 从Excel输入流读取数据
     * 
     * @param inputStream Excel输入流
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 数据列表
     */
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> clazz) {
        List<T> dataList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 获取标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.error("Excel文件标题行为空");
                return dataList;
            }
            
            // 获取字段映射
            Field[] fields = clazz.getDeclaredFields();
            
            // 从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                T instance = createInstance(row, clazz, fields);
                if (instance != null) {
                    dataList.add(instance);
                }
            }
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
        }
        
        return dataList;
    }

    /**
     * 将数据写入Excel
     * 
     * @param dataList 数据列表
     * @param headers 表头
     * @param fieldNames 字段名称数组
     * @param <T> 泛型类型
     * @return Excel字节数组
     */
    public static <T> byte[] writeExcel(List<T> dataList, String[] headers, String[] fieldNames) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("数据");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // 设置标题样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                T data = dataList.get(i);
                
                for (int j = 0; j < fieldNames.length; j++) {
                    Cell cell = row.createCell(j);
                    Object value = getFieldValue(data, fieldNames[j]);
                    setCellValue(cell, value);
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("写入Excel文件失败", e);
            return null;
        }
    }

    /**
     * 获取单元格字符串值
     * 
     * @param cell 单元格
     * @return 字符串值
     */
    public static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_TIME_FORMATTER.format(cell.getLocalDateTimeCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，返回整数格式
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 获取单元格数值
     * 
     * @param cell 单元格
     * @return 数值
     */
    public static Double getCellNumericValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String stringValue = cell.getStringCellValue().trim();
                if (StringUtils.hasText(stringValue)) {
                    try {
                        return Double.parseDouble(stringValue);
                    } catch (NumberFormatException e) {
                        log.warn("无法将字符串转换为数值: {}", stringValue);
                        return null;
                    }
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * 获取单元格日期时间值
     * 
     * @param cell 单元格
     * @return 日期时间
     */
    public static LocalDateTime getCellDateTimeValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue();
                }
                return null;
            case STRING:
                String stringValue = cell.getStringCellValue().trim();
                if (StringUtils.hasText(stringValue)) {
                    try {
                        return LocalDateTime.parse(stringValue, DATE_TIME_FORMATTER);
                    } catch (Exception e) {
                        log.warn("无法将字符串转换为日期时间: {}", stringValue);
                        return null;
                    }
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * 设置单元格值
     * 
     * @param cell 单元格
     * @param value 值
     */
    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DATE_TIME_FORMATTER));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 创建实例对象
     * 
     * @param row Excel行
     * @param clazz 目标类型
     * @param fields 字段数组
     * @param <T> 泛型类型
     * @return 实例对象
     */
    private static <T> T createInstance(Row row, Class<T> clazz, Field[] fields) {
        try {
            T instance = clazz.newInstance();
            
            // 这里需要根据具体的字段映射来设置值
            // 由于没有注解支持，暂时使用硬编码方式
            // 实际项目中建议使用注解来标识字段映射关系
            
            return instance;
        } catch (Exception e) {
            log.error("创建实例失败", e);
            return null;
        }
    }

    /**
     * 获取字段值
     * 
     * @param obj 对象
     * @param fieldName 字段名
     * @return 字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            log.error("获取字段值失败: {}", fieldName, e);
            return null;
        }
    }

    /**
     * 验证Excel文件格式
     * 
     * @param fileName 文件名
     * @return 是否为有效的Excel文件
     */
    public static boolean isValidExcelFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".xlsx") || lowerCaseName.endsWith(".xls");
    }

    /**
     * 获取Excel文件的工作表数量
     * 
     * @param inputStream Excel输入流
     * @return 工作表数量
     */
    public static int getSheetCount(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            return workbook.getNumberOfSheets();
        } catch (IOException e) {
            log.error("获取工作表数量失败", e);
            return 0;
        }
    }
}