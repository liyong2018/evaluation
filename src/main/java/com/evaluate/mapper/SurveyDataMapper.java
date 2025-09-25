package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.SurveyData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 调查数据Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface SurveyDataMapper extends BaseMapper<SurveyData> {

    /**
     * 根据调查名称查询数据
     * 
     * @param surveyName 调查名称
     * @return 调查数据列表
     */
    List<SurveyData> selectBySurveyName(@Param("surveyName") String surveyName);

    /**
     * 根据调查地区查询数据
     * 
     * @param surveyRegion 调查地区
     * @return 调查数据列表
     */
    List<SurveyData> selectBySurveyRegion(@Param("surveyRegion") String surveyRegion);

    /**
     * 根据指标代码查询数据
     * 
     * @param indicatorCode 指标代码
     * @return 调查数据列表
     */
    List<SurveyData> selectByIndicatorCode(@Param("indicatorCode") String indicatorCode);

    /**
     * 批量插入调查数据
     * 
     * @param dataList 调查数据列表
     * @return 插入数量
     */
    int batchInsert(@Param("dataList") List<SurveyData> dataList);

    /**
     * 获取所有指标代码
     * 
     * @return 指标代码列表
     */
    List<String> selectAllIndicatorCodes();

    /**
     * 根据字符集属性查询数据
     * 
     * @param charsetAttribute 字符集属性
     * @return 调查数据列表
     */
    List<SurveyData> selectByCharsetAttribute(@Param("charsetAttribute") String charsetAttribute);
}