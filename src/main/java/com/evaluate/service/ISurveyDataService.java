package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.SurveyData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 调查数据服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface ISurveyDataService extends IService<SurveyData> {

    /**
     * 根据调查名称查询数据
     * 
     * @param surveyName 调查名称
     * @return 调查数据列表
     */
    List<SurveyData> getBySurveyName(String surveyName);

    /**
     * 根据调查地区查询数据
     * 
     * @param surveyRegion 调查地区
     * @return 调查数据列表
     */
    List<SurveyData> getBySurveyRegion(String surveyRegion);

    /**
     * 根据指标代码查询数据
     * 
     * @param indicatorCode 指标代码
     * @return 调查数据列表
     */
    List<SurveyData> getByIndicatorCode(String indicatorCode);

    /**
     * 批量保存调查数据
     * 
     * @param dataList 调查数据列表
     * @return 保存结果
     */
    boolean batchSave(List<SurveyData> dataList);

    /**
     * 从Excel文件导入调查数据
     * 
     * @param file Excel文件
     * @return 导入结果
     */
    boolean importFromExcel(MultipartFile file);

    /**
     * 导出调查数据到Excel
     * 
     * @param surveyName 调查名称
     * @return Excel文件字节数组
     */
    byte[] exportToExcel(String surveyName);

    /**
     * 获取所有指标代码
     * 
     * @return 指标代码列表
     */
    List<String> getAllIndicatorCodes();

    /**
     * 根据字符集属性查询数据
     * 
     * @param charsetAttribute 字符集属性
     * @return 调查数据列表
     */
    List<SurveyData> getByCharsetAttribute(String charsetAttribute);

    /**
     * 验证调查数据
     * 
     * @param surveyData 调查数据
     * @return 验证结果
     */
    boolean validateSurveyData(SurveyData surveyData);

    /**
     * 删除调查数据及相关结果
     * 
     * @param surveyName 调查名称
     * @return 删除结果
     */
    boolean deleteSurveyDataAndResults(String surveyName);
}