package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.Report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报告服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface IReportService extends IService<Report> {

    /**
     * 根据调查ID查询报告
     * 
     * @param surveyId 调查ID
     * @return 报告列表
     */
    List<Report> getBySurveyId(Long surveyId);

    /**
     * 根据报告类型查询报告
     * 
     * @param reportType 报告类型
     * @return 报告列表
     */
    List<Report> getByReportType(String reportType);

    /**
     * 根据评估等级查询报告
     * 
     * @param evaluationGrade 评估等级
     * @return 报告列表
     */
    List<Report> getByEvaluationGrade(String evaluationGrade);

    /**
     * 根据生成人查询报告
     * 
     * @param generator 生成人
     * @return 报告列表
     */
    List<Report> getByGenerator(String generator);

    /**
     * 根据时间范围查询报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 报告列表
     */
    List<Report> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据条件查询报告
     * 
     * @param surveyId 调查ID
     * @param algorithmId 算法ID
     * @param weightConfigId 权重配置ID
     * @return 报告
     */
    Report getByConditions(Long surveyId, Long algorithmId, Long weightConfigId);

    /**
     * 根据得分范围查询报告
     * 
     * @param minScore 最小得分
     * @param maxScore 最大得分
     * @return 报告列表
     */
    List<Report> getByScoreRange(Double minScore, Double maxScore);

    /**
     * 统计报告数量按类型分组
     * 
     * @return 统计结果
     */
    List<Map<String, Object>> countByReportType();

    /**
     * 统计报告数量按等级分组
     * 
     * @return 统计结果
     */
    List<Map<String, Object>> countByEvaluationGrade();

    /**
     * 根据调查ID删除报告
     * 
     * @param surveyId 调查ID
     * @return 删除结果
     */
    boolean deleteBySurveyId(Long surveyId);

    /**
     * 批量保存报告
     * 
     * @param reportList 报告列表
     * @return 保存结果
     */
    boolean batchSave(List<Report> reportList);

    /**
     * 生成报告摘要
     * 
     * @param report 报告
     * @return 报告摘要
     */
    String generateReportSummary(Report report);

    /**
     * 导出报告为PDF
     * 
     * @param reportId 报告ID
     * @return PDF字节数组
     */
    byte[] exportToPdf(Long reportId);

    /**
     * 导出报告为Word
     * 
     * @param reportId 报告ID
     * @return Word字节数组
     */
    byte[] exportToWord(Long reportId);

    /**
     * 验证报告数据
     * 
     * @param report 报告
     * @return 验证结果
     */
    boolean validateReport(Report report);
}