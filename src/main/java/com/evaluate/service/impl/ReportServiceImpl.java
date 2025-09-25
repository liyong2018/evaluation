package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.Report;
import com.evaluate.mapper.ReportMapper;
import com.evaluate.service.IReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 报告服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    @Override
    public List<Report> getBySurveyId(Long surveyId) {
        // 由于数据库表结构变更，report表没有survey_id字段
        // 需要通过primary_result_id关联查询
        // 暂时返回空列表，需要根据实际业务需求调整
        return new ArrayList<>();
    }

    @Override
    public List<Report> getByReportType(String reportType) {
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_type", reportType);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<Report> getByEvaluationGrade(String evaluationGrade) {
        // 由于数据库表结构变更，evaluation_grade字段不存在
        // 暂时返回空列表，需要根据实际业务需求调整
        return new ArrayList<>();
    }

    @Override
    public List<Report> getByGenerator(String generator) {
        // 由于数据库表结构变更，generator字段不存在
        // 暂时返回空列表，需要根据实际业务需求调整
        return new ArrayList<>();
    }

    @Override
    public List<Report> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("generate_time", startTime, endTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Report getByConditions(Long surveyId, Long algorithmId, Long weightConfigId) {
        // 由于数据库表结构变更，这些字段不存在于report表中
        // 暂时返回null，需要根据实际业务需求调整
        return null;
    }

    @Override
    public List<Report> getByScoreRange(Double minScore, Double maxScore) {
        // 由于数据库表结构变更，total_score字段不存在
        // 暂时返回空列表，需要根据实际业务需求调整
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> countByReportType() {
        // 使用MyBatis Plus的方式实现统计
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("report_type", "count(*) as count")
                   .groupBy("report_type");
        return baseMapper.selectMaps(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> countByEvaluationGrade() {
        // 使用MyBatis Plus的方式实现统计
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("evaluation_grade", "count(*) as count")
                   .groupBy("evaluation_grade");
        return baseMapper.selectMaps(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySurveyId(Long surveyId) {
        try {
            // 由于数据库表结构变更，survey_id字段不存在
            // 暂时返回true，需要根据实际业务需求调整
            return true;
        } catch (Exception e) {
            log.error("根据调查ID删除报告失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<Report> reportList) {
        if (reportList == null || reportList.isEmpty()) {
            return false;
        }
        
        try {
            return saveBatch(reportList);
        } catch (Exception e) {
            log.error("批量保存报告失败", e);
            return false;
        }
    }

    @Override
    public String generateReportSummary(Report report) {
        if (report == null) {
            return "";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("报告名称：").append(report.getReportName()).append("\n");
        summary.append("报告类型：").append(getReportTypeDescription(report.getReportType())).append("\n");
        summary.append("生成时间：").append(report.getGenerateTime()).append("\n");
        
        return summary.toString();
    }

    @Override
    public byte[] exportToPdf(Long reportId) {
        // TODO: 实现PDF导出功能
        // 这里需要使用PDF生成库如iText或Apache PDFBox
        log.warn("PDF导出功能尚未实现");
        return null;
    }

    @Override
    public byte[] exportToWord(Long reportId) {
        // TODO: 实现Word导出功能
        // 这里需要使用Apache POI的XWPF组件
        log.warn("Word导出功能尚未实现");
        return null;
    }

    @Override
    public boolean validateReport(Report report) {
        if (report == null) {
            return false;
        }
        
        // 验证必填字段（基于新的数据库结构）
        if (!StringUtils.hasText(report.getReportName()) ||
            !StringUtils.hasText(report.getReportType())) {
            return false;
        }
        
        // 验证报告类型
        if (!isValidReportType(report.getReportType())) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取报告类型描述
     */
    private String getReportTypeDescription(String reportType) {
        if (reportType == null) {
            return "未知类型";
        }
        
        switch (reportType) {
            case "PDF":
                return "PDF报告";
            case "WORD":
                return "Word报告";
            case "MAP":
                return "专题图报告";
            default:
                return "其他类型";
        }
    }

    /**
     * 获取等级描述
     */
    private String getGradeDescription(String grade) {
        if (grade == null) {
            return "未知等级";
        }
        
        switch (grade) {
            case "A":
                return "优秀";
            case "B":
                return "良好";
            case "C":
                return "一般";
            case "D":
                return "较差";
            case "E":
                return "很差";
            default:
                return "未知等级";
        }
    }

    /**
     * 验证评估等级是否有效
     */
    private boolean isValidGrade(String grade) {
        return "A".equals(grade) || "B".equals(grade) || "C".equals(grade) || 
               "D".equals(grade) || "E".equals(grade);
    }

    /**
     * 验证报告类型是否有效
     */
    private boolean isValidReportType(String reportType) {
        return "PDF".equals(reportType) || "WORD".equals(reportType) || 
               "MAP".equals(reportType);
    }
}