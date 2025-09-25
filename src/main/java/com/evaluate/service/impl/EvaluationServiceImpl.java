package com.evaluate.service.impl;

import com.evaluate.entity.*;
import com.evaluate.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评估计算服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class EvaluationServiceImpl implements IEvaluationService {

    @Autowired
    private ISurveyDataService surveyDataService;
    
    @Autowired
    private IWeightConfigService weightConfigService;
    
    @Autowired
    private IIndicatorWeightService indicatorWeightService;
    
    @Autowired
    private ISecondaryIndicatorResultService secondaryIndicatorResultService;
    
    @Autowired
    private IPrimaryIndicatorResultService primaryIndicatorResultService;
    
    @Autowired
    private IReportService reportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> performEvaluation(Long surveyId, Long algorithmId, Long weightConfigId) {
        log.info("开始执行评估计算: surveyId={}, algorithmId={}, weightConfigId={}", surveyId, algorithmId, weightConfigId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 计算二级指标结果
            List<SecondaryIndicatorResult> secondaryResults = calculateSecondaryIndicators(surveyId, algorithmId, weightConfigId);
            result.put("secondaryResults", secondaryResults);
            
            // 2. 计算一级指标结果
            List<PrimaryIndicatorResult> primaryResults = calculatePrimaryIndicators(surveyId, algorithmId, weightConfigId, secondaryResults);
            result.put("primaryResults", primaryResults);
            
            // 3. 计算综合得分
            Double totalScore = calculateTotalScore(primaryResults);
            result.put("totalScore", totalScore);
            
            // 4. 确定评估等级
            String evaluationGrade = determineEvaluationGrade(totalScore);
            result.put("evaluationGrade", evaluationGrade);
            
            // 5. 生成评估报告
            Report report = generateEvaluationReport(surveyId, algorithmId, weightConfigId, primaryResults, "System");
            result.put("report", report);
            
            // 6. 获取算法过程数据
            Map<String, Object> processData = getAlgorithmProcessData(surveyId, algorithmId, weightConfigId);
            result.put("processData", processData);
            
            result.put("success", true);
            result.put("message", "评估计算完成");
            
            log.info("评估计算完成: 综合得分={}, 评估等级={}", totalScore, evaluationGrade);
            
        } catch (Exception e) {
            log.error("评估计算失败", e);
            result.put("success", false);
            result.put("message", "评估计算失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SecondaryIndicatorResult> calculateSecondaryIndicators(Long surveyId, Long algorithmId, Long weightConfigId) {
        log.info("开始计算二级指标结果");
        
        // 获取调查数据
        List<SurveyData> surveyDataList = surveyDataService.getBySurveyName("Survey_" + surveyId);
        
        // 获取权重配置
        List<IndicatorWeight> weights = indicatorWeightService.getByConfigIdAndLevel(weightConfigId, 2);
        
        List<SecondaryIndicatorResult> results = new ArrayList<>();
        
        for (IndicatorWeight weight : weights) {
            // 查找对应的调查数据 - 暂时跳过，因为新的数据结构不再有indicatorCode字段
            // 这里需要根据实际业务逻辑重新设计
            Optional<SurveyData> surveyDataOpt = surveyDataList.stream().findFirst();
            
            if (surveyDataOpt.isPresent()) {
                SurveyData surveyData = surveyDataOpt.get();
                
                SecondaryIndicatorResult result = new SecondaryIndicatorResult();
                result.setSurveyId(surveyId);
                result.setAlgorithmId(algorithmId);
                result.setWeightConfigId(weightConfigId);
                result.setIndicatorCode(weight.getIndicatorCode());
                result.setIndicatorName(weight.getIndicatorName());
                // 根据指标代码获取对应的数值 - 需要重新设计映射逻辑
                Double originalValue = getIndicatorValueByCode(surveyData, weight.getIndicatorCode());
                result.setOriginalValue(originalValue);
                
                // 数据归一化
                Double normalizedValue = normalizeValue(originalValue);
                result.setNormalizedValue(normalizedValue);
                
                // 设置权重值
                result.setWeightValue(weight.getWeight());
                
                // 计算加权值
                Double weightedValue = normalizedValue * weight.getWeight();
                result.setWeightedValue(weightedValue);
                
                // 生成过程数据
                Map<String, Object> processData = new HashMap<>();
                processData.put("step1_original", originalValue);
                processData.put("step2_normalized", normalizedValue);
                processData.put("step3_weight", weight.getWeight());
                processData.put("step4_weighted", weightedValue);
                result.setProcessData(processData.toString());
                
                result.setCreateTime(LocalDateTime.now());
                result.setUpdateTime(LocalDateTime.now());
                
                results.add(result);
            }
        }
        
        // 批量保存结果
        secondaryIndicatorResultService.batchSave(results);
        
        log.info("二级指标计算完成，共计算{}个指标", results.size());
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PrimaryIndicatorResult> calculatePrimaryIndicators(Long surveyId, Long algorithmId, Long weightConfigId, List<SecondaryIndicatorResult> secondaryResults) {
        log.info("开始计算一级指标结果");
        
        // 获取一级指标权重配置
        List<IndicatorWeight> primaryWeights = indicatorWeightService.getByConfigIdAndLevel(weightConfigId, 1);
        
        List<PrimaryIndicatorResult> results = new ArrayList<>();
        
        for (IndicatorWeight primaryWeight : primaryWeights) {
            // 获取该一级指标下的所有二级指标
            List<IndicatorWeight> childWeights = indicatorWeightService.getByParentId(primaryWeight.getId());
            
            // 计算一级指标值（二级指标加权平均）
            double totalWeightedValue = 0.0;
            double totalWeight = 0.0;
            
            Map<String, Object> processData = new HashMap<>();
            List<Map<String, Object>> childProcessData = new ArrayList<>();
            
            for (IndicatorWeight childWeight : childWeights) {
                Optional<SecondaryIndicatorResult> secondaryResultOpt = secondaryResults.stream()
                    .filter(result -> childWeight.getIndicatorCode().equals(result.getIndicatorCode()))
                    .findFirst();
                
                if (secondaryResultOpt.isPresent()) {
                    SecondaryIndicatorResult secondaryResult = secondaryResultOpt.get();
                    totalWeightedValue += secondaryResult.getWeightedValue();
                    totalWeight += childWeight.getWeight();
                    
                    Map<String, Object> childData = new HashMap<>();
                    childData.put("indicatorCode", childWeight.getIndicatorCode());
                    childData.put("indicatorName", childWeight.getIndicatorName());
                    childData.put("weightedValue", secondaryResult.getWeightedValue());
                    childData.put("weight", childWeight.getWeight());
                    childProcessData.add(childData);
                }
            }
            
            // 计算一级指标值
            Double calculatedValue = totalWeight > 0 ? totalWeightedValue / totalWeight : 0.0;
            
            PrimaryIndicatorResult result = new PrimaryIndicatorResult();
            result.setSurveyId(surveyId);
            result.setAlgorithmId(algorithmId);
            result.setWeightConfigId(weightConfigId);
            result.setIndicatorCode(primaryWeight.getIndicatorCode());
            result.setIndicatorName(primaryWeight.getIndicatorName());
            result.setCalculatedValue(calculatedValue);
            result.setWeightValue(primaryWeight.getWeight());
            result.setWeightedValue(calculatedValue * primaryWeight.getWeight());
            
            // 生成过程数据
            processData.put("primaryIndicator", primaryWeight.getIndicatorName());
            processData.put("childIndicators", childProcessData);
            processData.put("totalWeightedValue", totalWeightedValue);
            processData.put("totalWeight", totalWeight);
            processData.put("calculatedValue", calculatedValue);
            processData.put("finalWeightedValue", result.getWeightedValue());
            result.setProcessData(processData.toString());
            
            result.setCreateTime(LocalDateTime.now());
            result.setUpdateTime(LocalDateTime.now());
            
            results.add(result);
        }
        
        // 批量保存结果
        primaryIndicatorResultService.batchSave(results);
        
        log.info("一级指标计算完成，共计算{}个指标", results.size());
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report generateEvaluationReport(Long surveyId, Long algorithmId, Long weightConfigId, List<PrimaryIndicatorResult> primaryResults, String generator) {
        log.info("开始生成评估报告");
        
        Double totalScore = calculateTotalScore(primaryResults);
        String evaluationGrade = determineEvaluationGrade(totalScore);
        
        Report report = new Report();
        // 由于数据库表结构变更，使用新的字段结构
        report.setReportName("减灾能力评估报告_" + surveyId);
        report.setReportType("PDF");
        report.setGenerateTime(LocalDateTime.now());
        
        // 暂时不保存报告，因为需要primary_result_id字段
        // reportService.save(report);
        
        log.info("评估报告生成完成");
        return report;
    }

    @Override
    public List<Double> normalizeData(List<Double> originalValues, Long formulaId) {
        // 使用最大最小值归一化方法
        if (originalValues == null || originalValues.isEmpty()) {
            return new ArrayList<>();
        }
        
        double max = originalValues.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double min = originalValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        
        return originalValues.stream()
            .map(value -> (max - min) > 0 ? (value - min) / (max - min) : 0.0)
            .collect(Collectors.toList());
    }

    @Override
    public Double calculateTotalScore(List<PrimaryIndicatorResult> primaryResults) {
        if (primaryResults == null || primaryResults.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = primaryResults.stream()
            .mapToDouble(result -> result.getWeightedValue() != null ? result.getWeightedValue() : 0.0)
            .sum();
        
        // 转换为百分制
        return totalScore * 100;
    }

    @Override
    public String determineEvaluationGrade(Double totalScore) {
        if (totalScore == null) {
            return "E";
        }
        
        if (totalScore >= 90) {
            return "A";
        } else if (totalScore >= 80) {
            return "B";
        } else if (totalScore >= 70) {
            return "C";
        } else if (totalScore >= 60) {
            return "D";
        } else {
            return "E";
        }
    }

    @Override
    public Map<String, Object> getAlgorithmProcessData(Long surveyId, Long algorithmId, Long weightConfigId) {
        Map<String, Object> processData = new HashMap<>();
        
        // 获取二级指标过程数据
        List<SecondaryIndicatorResult> secondaryResults = secondaryIndicatorResultService.getBySurveyIdAndAlgorithmId(surveyId, algorithmId);
        processData.put("secondaryProcess", secondaryResults);
        
        // 获取一级指标过程数据
        List<PrimaryIndicatorResult> primaryResults = primaryIndicatorResultService.getBySurveyIdAndAlgorithmId(surveyId, algorithmId);
        processData.put("primaryProcess", primaryResults);
        
        // 添加算法步骤说明
        List<String> steps = Arrays.asList(
            "步骤1：数据收集与预处理",
            "步骤2：数据归一化处理",
            "步骤3：权重分配与计算",
            "步骤4：二级指标计算",
            "步骤5：一级指标聚合",
            "步骤6：综合评分与等级确定"
        );
        processData.put("algorithmSteps", steps);
        
        return processData;
    }

    @Override
    public boolean validateEvaluationParams(Long surveyId, Long algorithmId, Long weightConfigId) {
        // 验证调查数据是否存在
        List<SurveyData> surveyData = surveyDataService.getBySurveyName("Survey_" + surveyId);
        if (surveyData.isEmpty()) {
            log.error("调查数据不存在: surveyId={}", surveyId);
            return false;
        }
        
        // 验证权重配置是否存在
        WeightConfig weightConfig = weightConfigService.getById(weightConfigId);
        if (weightConfig == null) {
            log.error("权重配置不存在: weightConfigId={}", weightConfigId);
            return false;
        }
        
        // 验证权重配置完整性
        if (!indicatorWeightService.validateWeightIntegrity(weightConfigId)) {
            log.error("权重配置不完整: weightConfigId={}", weightConfigId);
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> recalculateEvaluation(Long surveyId, Long algorithmId, Long weightConfigId) {
        // 删除现有结果
        deleteEvaluationResults(surveyId, algorithmId, weightConfigId);
        
        // 重新计算
        return performEvaluation(surveyId, algorithmId, weightConfigId);
    }

    @Override
    public List<Map<String, Object>> batchEvaluation(List<Long> surveyIds, Long algorithmId, Long weightConfigId) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Long surveyId : surveyIds) {
            try {
                Map<String, Object> result = performEvaluation(surveyId, algorithmId, weightConfigId);
                results.add(result);
            } catch (Exception e) {
                log.error("批量评估失败: surveyId={}", surveyId, e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("surveyId", surveyId);
                errorResult.put("success", false);
                errorResult.put("message", e.getMessage());
                results.add(errorResult);
            }
        }
        
        return results;
    }

    @Override
    public Map<String, Object> compareEvaluationResults(List<Long> surveyIds, Long algorithmId, Long weightConfigId) {
        Map<String, Object> comparison = new HashMap<>();
        List<Map<String, Object>> evaluationResults = new ArrayList<>();
        
        for (Long surveyId : surveyIds) {
            List<PrimaryIndicatorResult> primaryResults = primaryIndicatorResultService.getBySurveyIdAndAlgorithmId(surveyId, algorithmId);
            Double totalScore = calculateTotalScore(primaryResults);
            String grade = determineEvaluationGrade(totalScore);
            
            Map<String, Object> result = new HashMap<>();
            result.put("surveyId", surveyId);
            result.put("totalScore", totalScore);
            result.put("grade", grade);
            result.put("primaryResults", primaryResults);
            
            evaluationResults.add(result);
        }
        
        comparison.put("results", evaluationResults);
        comparison.put("compareTime", LocalDateTime.now());
        
        return comparison;
    }

    @Override
    public List<Map<String, Object>> getEvaluationHistory(Long surveyId) {
        List<Report> reports = reportService.getBySurveyId(surveyId);
        
        return reports.stream().map(report -> {
            Map<String, Object> history = new HashMap<>();
            history.put("reportId", report.getId());
            history.put("reportName", report.getReportName());
            history.put("reportType", report.getReportType());
            history.put("generateTime", report.getGenerateTime());
            return history;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEvaluationResults(Long surveyId, Long algorithmId, Long weightConfigId) {
        try {
            // 删除二级指标结果
            secondaryIndicatorResultService.deleteByConditions(surveyId, algorithmId, weightConfigId);
            
            // 删除一级指标结果
            primaryIndicatorResultService.deleteByConditions(surveyId, algorithmId, weightConfigId);
            
            // 删除相关报告
            reportService.deleteBySurveyId(surveyId);
            
            return true;
        } catch (Exception e) {
            log.error("删除评估结果失败", e);
            return false;
        }
    }

    /**
     * 归一化单个值
     */
    private Double normalizeValue(Double value) {
        if (value == null) {
            return 0.0;
        }
        
        // 简单的归一化处理，实际项目中应该根据具体算法来实现
        // 这里假设原始值在0-100之间，归一化到0-1之间
        return Math.min(Math.max(value / 100.0, 0.0), 1.0);
    }

    /**
     * 根据指标代码获取对应的数值
     */
    private Double getIndicatorValueByCode(SurveyData surveyData, String indicatorCode) {
        if (surveyData == null || indicatorCode == null) {
            return 0.0;
        }
        
        // 根据指标代码映射到对应的字段值
        switch (indicatorCode) {
            case "L2_MANAGEMENT_CAPABILITY":
                // 队伍管理能力 = (管理人员数/人口数) * 10000
                return surveyData.getPopulation() > 0 ? 
                    (surveyData.getManagementStaff().doubleValue() / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            case "L2_RISK_ASSESSMENT":
                // 风险评估能力 = 是否开展风险评估(是=1,否=0)
                return "是".equals(surveyData.getRiskAssessment()) ? 1.0 : 0.0;
            case "L2_FUNDING":
                // 财政投入能力 = (资金投入/人口数) * 10000
                return surveyData.getPopulation() > 0 ? 
                    (surveyData.getFundingAmount() / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            case "L2_MATERIAL":
                // 物资储备能力 = (物资价值/人口数) * 10000
                return surveyData.getPopulation() > 0 ? 
                    (surveyData.getMaterialValue() / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            case "L2_MEDICAL":
                // 医疗保障能力 = (床位数/人口数) * 10000
                return surveyData.getPopulation() > 0 ? 
                    (surveyData.getHospitalBeds().doubleValue() / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            case "L2_SELF_RESCUE":
                // 自救互救能力 = ((消防员+志愿者+民兵)/人口数) * 10000
                int totalRescuePersonnel = surveyData.getFirefighters() + surveyData.getVolunteers() + surveyData.getMilitiaReserve();
                return surveyData.getPopulation() > 0 ? 
                    (totalRescuePersonnel / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            case "L2_PUBLIC_AVOIDANCE":
                // 公众避险能力 = (培训参与人次/人口数) * 10000
                return surveyData.getPopulation() > 0 ? 
                    (surveyData.getTrainingParticipants().doubleValue() / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            case "L2_RELOCATION":
                // 转移安置能力 = (避难场所容量/人口数) * 10000
                return surveyData.getPopulation() > 0 ? 
                    (surveyData.getShelterCapacity().doubleValue() / surveyData.getPopulation().doubleValue()) * 10000 : 0.0;
            default:
                return 0.0;
        }
    }

    /**
     * 生成建议措施
     */
    private String generateRecommendations(String grade, List<PrimaryIndicatorResult> primaryResults) {
        StringBuilder recommendations = new StringBuilder();
        
        switch (grade) {
            case "A":
                recommendations.append("评估结果优秀，请继续保持现有的减灾能力水平。");
                break;
            case "B":
                recommendations.append("评估结果良好，建议进一步完善薄弱环节。");
                break;
            case "C":
                recommendations.append("评估结果一般，需要重点加强减灾能力建设。");
                break;
            case "D":
                recommendations.append("评估结果较差，急需全面提升减灾能力。");
                break;
            case "E":
                recommendations.append("评估结果很差，必须立即采取措施改善减灾能力。");
                break;
        }
        
        // 针对得分较低的指标提出具体建议
        for (PrimaryIndicatorResult result : primaryResults) {
            if (result.getCalculatedValue() < 0.6) {
                recommendations.append("建议重点关注").append(result.getIndicatorName()).append("的改善。");
            }
        }
        
        return recommendations.toString();
    }
}