package com.evaluate.service.impl;

import java.lang.reflect.Method;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.*;
import com.evaluate.mapper.EvaluationMapper;
import com.evaluate.mapper.PrimaryIndicatorResultMapper;
import com.evaluate.mapper.SecondaryIndicatorResultMapper;
import com.evaluate.mapper.SurveyDataMapper;
import com.evaluate.service.IAlgorithmConfigService;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.IReportService;
import javax.annotation.Resource;
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

    @Resource
    private SurveyDataMapper surveyDataMapper;
    @Resource
    private IIndicatorWeightService indicatorWeightService;
    @Resource
    private IAlgorithmConfigService algorithmService;
    @Resource
    private IReportService reportService;
    @Resource
    private EvaluationMapper evaluationMapper;

    @Resource
    private SecondaryIndicatorResultMapper secondaryIndicatorResultMapper;
    @Resource
    private PrimaryIndicatorResultMapper primaryIndicatorResultMapper;
    @Resource
    private ISecondaryIndicatorResultService secondaryIndicatorResultService;
    @Resource
    private IPrimaryIndicatorResultService primaryIndicatorResultService;

    @Autowired
    private ISurveyDataService surveyDataService;

    @Autowired
    private IWeightConfigService weightConfigService;

    @Autowired
    private AlgorithmConfigServiceImpl algorithmConfigService;

    @Autowired
    private AlgorithmExecutionService algorithmExecutionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> performEvaluation(Long surveyId, Long algorithmId, Long weightConfigId) {
        log.info("开始执行评估计算: surveyId={}, algorithmId={}, weightConfigId={}", surveyId, algorithmId, weightConfigId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始执行评估流程，调查ID: {}", surveyId);

            log.info("步骤 1: 获取算法配置");
            AlgorithmConfig algorithmConfig = algorithmConfigService.getById(algorithmId);
            if (algorithmConfig == null) {
                throw new RuntimeException("算法配置不存在: " + algorithmId);
            }
            log.info("成功获取算法配置");

            log.info("步骤 2: 获取调查数据");
            SurveyData surveyData = surveyDataService.getById(surveyId);
            if (surveyData == null) {
                throw new RuntimeException("调查数据不存在: " + surveyId);
            }
            List<SurveyData> surveyDataList = Collections.singletonList(surveyData);
            log.info("成功获取调查数据，共 {} 条", surveyDataList.size());

            log.info("步骤 3: 获取权重配置");
            Map<String, Double> weightConfig = getWeightConfigMap(weightConfigId);
            log.info("成功获取权重配置，共 {} 个权重", weightConfig.size());

            // 4. 提取地区ID列表（按传入的surveyId）
            List<Long> regionIds = Collections.singletonList(surveyId);

            log.info("步骤 5: 开始执行算法流程...");
            Map<String, Object> algorithmResult = algorithmExecutionService.executeAlgorithm(
                algorithmConfig, surveyDataList, weightConfig, regionIds);
            log.info("算法流程执行完毕. Result: {}", algorithmResult);

            log.info("步骤 6: 开始保存评估结果...");
            saveEvaluationResults(surveyId, algorithmId, weightConfigId, algorithmResult);
            log.info("评估结果保存完毕.");

            log.info("步骤 7: 开始记录评估历史...");
            recordEvaluationHistory(surveyId, algorithmId, weightConfigId, algorithmResult);
            log.info("评估历史记录完毕.");

            result.put("success", true);
            result.put("message", "评估计算完成");
            result.put("algorithmResult", algorithmResult);
            result.put("executionId", algorithmResult.get("executionId"));

            log.info("评估计算完成: executionId={}", algorithmResult.get("executionId"));

        } catch (Exception e) {
            log.error("评估计算失败", e);
            throw new RuntimeException("评估计算失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SecondaryIndicatorResult> calculateSecondaryIndicators(Long surveyId, Long algorithmId, Long weightConfigId, Map<String, Object> algorithmResult) {
        log.info("开始计算二级指标，调查ID: {}, 算法ID: {}, 权重配置ID: {}", surveyId, algorithmId, weightConfigId);
        
        // 创建二级指标结果对象
        SecondaryIndicatorResult result = new SecondaryIndicatorResult();
        result.setSurveyDataId(surveyId);
        result.setConfigId(weightConfigId);
        
        // 从算法结果中提取二级指标值
        if (algorithmResult != null && algorithmResult.containsKey("table1Data")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> table1Data = (List<Map<String, Object>>) algorithmResult.get("table1Data");
            
            if (!table1Data.isEmpty()) {
                Map<String, Object> firstRow = table1Data.get(0);
                
                // 设置二级指标原始计算值
                result.setManagementCapability(parseDouble(firstRow.get("teamManagement")));
                result.setRiskAssessmentCapability(parseDouble(firstRow.get("riskAssessment")));
                result.setFundingCapability(parseDouble(firstRow.get("financialInput")));
                result.setMaterialCapability(parseDouble(firstRow.get("materialReserve")));
                result.setMedicalCapability(parseDouble(firstRow.get("medicalSupport")));
                result.setSelfRescueCapability(parseDouble(firstRow.get("selfRescue")));
                result.setPublicAvoidanceCapability(parseDouble(firstRow.get("publicAvoidance")));
                result.setRelocationCapability(parseDouble(firstRow.get("relocationCapacity")));
            }
        }
        
        // 从算法结果中提取归一化值
        if (algorithmResult != null && algorithmResult.containsKey("table2Data")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> table2Data = (List<Map<String, Object>>) algorithmResult.get("table2Data");
            
            if (!table2Data.isEmpty()) {
                Map<String, Object> firstRow = table2Data.get(0);
                
                // 设置二级指标归一化值
                result.setManagementNormalized(parseDouble(firstRow.get("teamManagement")));
                result.setRiskAssessmentNormalized(parseDouble(firstRow.get("riskAssessment")));
                result.setFundingNormalized(parseDouble(firstRow.get("financialInput")));
                result.setMaterialNormalized(parseDouble(firstRow.get("materialReserve")));
                result.setMedicalNormalized(parseDouble(firstRow.get("medicalSupport")));
                result.setSelfRescueNormalized(parseDouble(firstRow.get("selfRescue")));
                result.setPublicAvoidanceNormalized(parseDouble(firstRow.get("publicAvoidance")));
                result.setRelocationNormalized(parseDouble(firstRow.get("relocationCapacity")));
            }
        }
        
        result.setCalculateTime(LocalDateTime.now());
        
        // 保存结果
        List<SecondaryIndicatorResult> results = new ArrayList<>();
        results.add(result);
        secondaryIndicatorResultService.saveBatch(results);
        
        log.info("二级指标计算完成，保存了1条记录");
        return results;
    }
    
    /**
     * 解析 Double 值的辅助方法
     */
    private Double parseDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析数值: {}", value);
                return 0.0;
            }
        }
        return 0.0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PrimaryIndicatorResult> calculatePrimaryIndicators(Long surveyId, Long algorithmId, Long weightConfigId, List<SecondaryIndicatorResult> secondaryResults) {
        log.info("开始计算一级指标");
        
        if (secondaryResults == null || secondaryResults.isEmpty()) {
            log.warn("二级指标结果为空，无法计算一级指标");
            return new ArrayList<>();
        }
        
        List<PrimaryIndicatorResult> results = new ArrayList<>();
        
        // 从二级指标结果中获取第一个结果（假设每次评估只有一个结果）
        SecondaryIndicatorResult secondaryResult = secondaryResults.get(0);
        
        // 获取权重配置
        List<IndicatorWeight> primaryWeights = indicatorWeightService.getPrimaryWeights(weightConfigId);
        
        // 计算三个一级指标的能力值
        double managementCapability = 0.0;
        double preparationCapability = 0.0;
        double selfRescueCapability = 0.0;
        
        // 根据权重配置计算各一级指标
        for (IndicatorWeight primaryWeight : primaryWeights) {
            String indicatorCode = primaryWeight.getIndicatorCode();
            List<IndicatorWeight> childWeights = indicatorWeightService.getChildWeights(weightConfigId, primaryWeight.getId());
            
            double totalWeightedValue = 0.0;
            double totalWeight = 0.0;
            
            // 根据二级指标权重计算加权平均值
            for (IndicatorWeight childWeight : childWeights) {
                String childCode = childWeight.getIndicatorCode();
                Double normalizedValue = getSecondaryIndicatorNormalizedValue(secondaryResult, childCode);
                
                if (normalizedValue != null) {
                    totalWeightedValue += normalizedValue * childWeight.getWeight();
                    totalWeight += childWeight.getWeight();
                }
            }
            
            // 计算一级指标值
            double calculatedValue = totalWeight > 0 ? totalWeightedValue / totalWeight : 0.0;
            
            // 根据指标代码分配到对应的一级指标
            switch (indicatorCode) {
                case "A":
                    managementCapability = calculatedValue;
                    break;
                case "B":
                    preparationCapability = calculatedValue;
                    break;
                case "C":
                    selfRescueCapability = calculatedValue;
                    break;
            }
        }
        
        // 创建一个结果记录
        PrimaryIndicatorResult result = new PrimaryIndicatorResult();
        result.setSurveyId(surveyId);
        result.setAlgorithmId(algorithmId);
        result.setWeightConfigId(weightConfigId);
        
        // 设置三个一级指标的能力值
        result.setLevel1Management(managementCapability);
        result.setLevel1Preparation(preparationCapability);
        result.setLevel1SelfRescue(selfRescueCapability);
        
        // 计算综合减灾能力
        Double overallCapability = (result.getLevel1Management() + result.getLevel1Preparation() + result.getLevel1SelfRescue()) / 3.0;
        result.setOverallCapability(overallCapability);
        
        // 设置能力分级
        result.setManagementGrade(determineCapabilityGrade(result.getLevel1Management()));
        result.setPreparationGrade(determineCapabilityGrade(result.getLevel1Preparation()));
        result.setSelfRescueGrade(determineCapabilityGrade(result.getLevel1SelfRescue()));
        result.setOverallGrade(determineCapabilityGrade(overallCapability));
        
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());
        
        results.add(result);
        
        // 保存一级指标结果
        primaryIndicatorResultService.saveBatch(results);
        
        log.info("一级指标计算完成");
        return results;
    }
    
    /**
     * 根据指标代码获取二级指标的归一化值
     */
    private Double getSecondaryIndicatorNormalizedValue(SecondaryIndicatorResult result, String indicatorCode) {
        try {
            String fieldName = "a" + indicatorCode.substring(1) + "Normalized";
            Method method = result.getClass().getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            return (Double) method.invoke(result);
        } catch (Exception e) {
            log.error("获取二级指标归一化值失败，指标代码: {}", indicatorCode, e);
            return null;
        }
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
        
        // 使用综合减灾能力作为总分
        double totalScore = primaryResults.stream()
            .mapToDouble(result -> result.getOverallCapability() != null ? result.getOverallCapability() : 0.0)
            .average()
            .orElse(0.0);
        
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
        SurveyData surveyData = surveyDataService.getById(surveyId);
        if (surveyData == null) {
            log.error("调查数据不存在: surveyId={}", surveyId);
            return false;
        }
        
        // 验证权重配置是否存在
        WeightConfig weightConfig = weightConfigService.getById(weightConfigId);
        if (weightConfig == null) {
            log.error("权重配置不存在: weightConfigId={}", weightConfigId);
            return false;
        }
        
        // 若该权重配置下没有任何指标权重，初始化默认权重
        List<IndicatorWeight> existingWeights = indicatorWeightService.getByConfigId(weightConfigId);
        if (existingWeights == null || existingWeights.isEmpty()) {
            log.warn("当前权重配置下没有任何指标权重，初始化默认权重: weightConfigId={}", weightConfigId);
            try {
                indicatorWeightService.initDefaultWeights(weightConfigId);
            } catch (Exception initEx) {
                log.error("初始化默认权重失败", initEx);
                return false;
            }
        }
        
        // 验证权重配置完整性；若不通过则尝试自动归一化修复
        if (!indicatorWeightService.validateWeightIntegrity(weightConfigId)) {
            try {
                log.warn("权重配置完整性校验未通过，尝试自动归一化修复: weightConfigId={}", weightConfigId);
                List<IndicatorWeight> allWeights = indicatorWeightService.getByConfigId(weightConfigId);
                if (allWeights != null && !allWeights.isEmpty()) {
                    // 归一化一级指标权重
                    List<IndicatorWeight> primaryWeights = allWeights.stream()
                        .filter(w -> w.getIndicatorLevel() == 1)
                        .collect(Collectors.toList());
                    double primarySum = primaryWeights.stream().mapToDouble(IndicatorWeight::getWeight).sum();
                    if (primarySum > 0) {
                        for (IndicatorWeight w : primaryWeights) {
                            w.setWeight(w.getWeight() / primarySum);
                            indicatorWeightService.updateById(w);
                        }
                    } else if (!primaryWeights.isEmpty()) {
                        // 总和为0，均分
                        double equal = 1.0 / primaryWeights.size();
                        for (IndicatorWeight w : primaryWeights) {
                            w.setWeight(equal);
                            indicatorWeightService.updateById(w);
                        }
                    }
                    
                    // 归一化每个一级指标下的二级指标权重
                    Map<Long, List<IndicatorWeight>> secondaryGroups = allWeights.stream()
                        .filter(w -> w.getIndicatorLevel() == 2 && w.getParentId() != null)
                        .collect(Collectors.groupingBy(IndicatorWeight::getParentId));
                    for (Map.Entry<Long, List<IndicatorWeight>> entry : secondaryGroups.entrySet()) {
                        List<IndicatorWeight> group = entry.getValue();
                        double sum = group.stream().mapToDouble(IndicatorWeight::getWeight).sum();
                        if (sum > 0) {
                            for (IndicatorWeight w : group) {
                                w.setWeight(w.getWeight() / sum);
                                indicatorWeightService.updateById(w);
                            }
                        } else if (!group.isEmpty()) {
                            // 总和为0，均分
                            double equal = 1.0 / group.size();
                            for (IndicatorWeight w : group) {
                                w.setWeight(equal);
                                indicatorWeightService.updateById(w);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("自动归一化修复权重配置失败", ex);
                return false;
            }
            
            // 修复后重新校验
            if (!indicatorWeightService.validateWeightIntegrity(weightConfigId)) {
                log.error("权重配置完整性仍未通过: weightConfigId={}", weightConfigId);
                return false;
            }
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
            log.error("评估计算失败", e);
            throw new RuntimeException("评估计算失败", e);
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
     * 归一化单个值（带最小值和最大值参数）
     */
    private Double normalizeValue(Double value, Double minValue, Double maxValue) {
        if (value == null) {
            return 0.0;
        }
        
        if (minValue == null || maxValue == null || minValue.equals(maxValue)) {
            return normalizeValue(value);
        }
        
        // 使用最小-最大归一化
        return Math.min(Math.max((value - minValue) / (maxValue - minValue), 0.0), 1.0);
    }

    /**
     * 根据指标代码获取对应的数值
     */
    private Double getIndicatorValueByCode(Map<String, Object> surveyData, String indicatorCode) {
        if (surveyData == null || indicatorCode == null) {
            return 0.0;
        }
        
        // 根据指标代码映射到对应的字段值
        Object value = surveyData.get(indicatorCode);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        return 0.0;
    }

    /**
     * 根据指标代码获取对应的数值（兼容旧版本）
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
            if (result.getLevel1Management() != null && result.getLevel1Management() < 0.6) {
                recommendations.append("建议重点关注灾害管理能力的改善。");
            }
            if (result.getLevel1Preparation() != null && result.getLevel1Preparation() < 0.6) {
                recommendations.append("建议重点关注灾害备灾能力的改善。");
            }
            if (result.getLevel1SelfRescue() != null && result.getLevel1SelfRescue() < 0.6) {
                recommendations.append("建议重点关注自救转移能力的改善。");
            }
        }
        
        return recommendations.toString();
    }
    
    /**
     * 获取权重配置映射
     */
    private Map<String, Double> getWeightConfigMap(Long weightConfigId) {
        Map<String, Double> weightConfig = new HashMap<>();
        
        // 获取所有权重配置
        List<IndicatorWeight> weights = indicatorWeightService.getByConfigId(weightConfigId);
        
        for (IndicatorWeight weight : weights) {
            weightConfig.put(weight.getIndicatorCode(), weight.getWeight());
        }
        
        return weightConfig;
    }
    
    @Transactional(rollbackFor = Exception.class)
    private void saveEvaluationResults(Long surveyId, Long algorithmId, Long weightConfigId, Map<String, Object> algorithmResult) {
        log.info("开始保存评估结果到数据库");
        
        try {
            // 1. 计算二级指标结果
            List<SecondaryIndicatorResult> secondaryResults = calculateSecondaryIndicators(surveyId, algorithmId, weightConfigId, algorithmResult);
            
            // 2. 计算一级指标结果
            List<PrimaryIndicatorResult> primaryResults = calculatePrimaryIndicators(surveyId, algorithmId, weightConfigId, secondaryResults);
            
            // 3. 计算一级指标能力值和分级
            Map<String, Double> level1Capabilities = calculateLevel1Capabilities(secondaryResults);
            Map<String, String> grades = calculateGrades(level1Capabilities);
            
            // 4. 计算综合减灾能力
            double overallCapability = calculateOverallCapability(level1Capabilities);
            String overallGrade = calculateOverallGrade(overallCapability);
            
            // 5. 更新一级指标结果中的能力值和分级信息
            // 由于PrimaryIndicatorResult结构已更改，这部分代码不再需要
            
            // 6. 传递给专题图生成
            generateThematicMap(surveyId, primaryResults, overallCapability, overallGrade);
            
            log.info("评估结果保存完成，综合能力: {}, 综合分级: {}", overallCapability, overallGrade);
            
        } catch (Exception e) {
            log.error("保存评估结果失败", e);
            throw new RuntimeException("保存评估结果失败", e);
        }
    }
    
    /**
     * 计算一级指标能力值
     */
    private Map<String, Double> calculateLevel1Capabilities(List<SecondaryIndicatorResult> secondaryResults) {
        Map<String, Double> capabilities = new HashMap<>();
        
        if (secondaryResults == null || secondaryResults.isEmpty()) {
            capabilities.put("management", 0.0);
            capabilities.put("preparation", 0.0);
            capabilities.put("selfRescue", 0.0);
            return capabilities;
        }
        
        // 从第一个结果中计算各一级指标的平均值
        SecondaryIndicatorResult result = secondaryResults.get(0);
        
        // 计算管理能力 (A1-A33)
        double managementSum = 0.0;
        int managementCount = 0;
        for (int i = 1; i <= 33; i++) {
            Double value = getSecondaryIndicatorNormalizedValue(result, "A" + i);
            if (value != null) {
                managementSum += value;
                managementCount++;
            }
        }
        capabilities.put("management", managementCount > 0 ? managementSum / managementCount : 0.0);
        
        // 计算准备能力 (A34-A66)
        double preparationSum = 0.0;
        int preparationCount = 0;
        for (int i = 34; i <= 66; i++) {
            Double value = getSecondaryIndicatorNormalizedValue(result, "A" + i);
            if (value != null) {
                preparationSum += value;
                preparationCount++;
            }
        }
        capabilities.put("preparation", preparationCount > 0 ? preparationSum / preparationCount : 0.0);
        
        // 计算自救能力 (A67-A100)
        double selfRescueSum = 0.0;
        int selfRescueCount = 0;
        for (int i = 67; i <= 100; i++) {
            Double value = getSecondaryIndicatorNormalizedValue(result, "A" + i);
            if (value != null) {
                selfRescueSum += value;
                selfRescueCount++;
            }
        }
        capabilities.put("selfRescue", selfRescueCount > 0 ? selfRescueSum / selfRescueCount : 0.0);

        return capabilities;
    }

    /**
     * 计算分级
     */
    private Map<String, String> calculateGrades(Map<String, Double> capabilities) {
        Map<String, String> grades = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : capabilities.entrySet()) {
            String category = entry.getKey();
            Double value = entry.getValue();
            
            String grade;
            if (value >= 0.8) {
                grade = "极强";
            } else if (value >= 0.6) {
                grade = "较强";
            } else if (value >= 0.4) {
                grade = "中等";
            } else if (value >= 0.2) {
                grade = "较弱";
            } else {
                grade = "极弱";
            }
            
            grades.put(category, grade);
        }
        
        return grades;
    }

    /**
     * 计算综合减灾能力
     */
    private double calculateOverallCapability(Map<String, Double> capabilities) {
        // 使用等权重计算综合能力
        double sum = capabilities.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        return capabilities.size() > 0 ? sum / capabilities.size() : 0.0;
    }

    /**
     * 计算综合减灾能力分级
     */
    private String calculateOverallGrade(double overallCapability) {
        if (overallCapability >= 0.8) {
            return "极强";
        } else if (overallCapability >= 0.6) {
            return "较强";
        } else if (overallCapability >= 0.4) {
            return "中等";
        } else if (overallCapability >= 0.2) {
            return "较弱";
        } else {
            return "极弱";
        }
    }

    /**
     * 确定能力分级
     */
    private String determineCapabilityGrade(Double capability) {
        if (capability == null) {
            return "极弱";
        }
        
        if (capability >= 0.8) {
            return "极强";
        } else if (capability >= 0.6) {
            return "较强";
        } else if (capability >= 0.4) {
            return "中等";
        } else if (capability >= 0.2) {
            return "较弱";
        } else {
            return "极弱";
        }
    }

    /**
     * 生成专题图和报告
     */
    private void generateThematicMap(Long surveyId, List<PrimaryIndicatorResult> primaryResults, double overallCapability, String overallGrade) {
        try {
            // 这里可以调用专题图生成服务
            // 暂时记录日志
            log.info("开始生成专题图，调查ID: {}, 综合能力: {}, 综合分级: {}", 
                surveyId, overallCapability, overallGrade);
            
            // TODO: 实际的专题图生成逻辑
            
        } catch (Exception e) {
            log.error("生成专题图失败，调查ID: {}", surveyId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 保存评估结果到数据库（新版本）
     */
    @Override
    public void saveEvaluationResults(Long evaluationId) {
        try {
            // 获取评估记录
            Evaluation evaluation = evaluationMapper.selectById(evaluationId);
            if (evaluation == null) {
                throw new RuntimeException("评估记录不存在");
            }

            // 获取二级指标结果
            List<SecondaryIndicatorResult> secondaryResults = secondaryIndicatorResultMapper.selectList(
                new QueryWrapper<SecondaryIndicatorResult>()
                    .eq("evaluation_id", evaluationId)
            );

            if (secondaryResults.isEmpty()) {
                throw new RuntimeException("未找到二级指标结果数据");
            }

            // 计算一级指标能力值
            Map<String, Double> level1Capabilities = calculateLevel1CapabilitiesForEvaluation(secondaryResults);
            
            // 计算分级
            Map<String, String> grades = calculateGradesForEvaluation(level1Capabilities);
            
            // 计算综合减灾能力
            double overallCapability = calculateOverallCapabilityForEvaluation(level1Capabilities);
            String overallGrade = calculateOverallGradeForEvaluation(overallCapability);

            // 保存到primary_indicator_result表
            PrimaryIndicatorResult primaryResult = new PrimaryIndicatorResult();
            primaryResult.setEvaluationId(evaluationId);
            primaryResult.setSecondaryResultId(secondaryResults.get(0).getId()); // 关联第一个二级指标结果
            primaryResult.setLevel1Management(level1Capabilities.get("management"));
            primaryResult.setLevel1Preparation(level1Capabilities.get("preparation"));
            primaryResult.setLevel1SelfRescue(level1Capabilities.get("selfRescue"));
            primaryResult.setManagementGrade(grades.get("management"));
            primaryResult.setPreparationGrade(grades.get("preparation"));
            primaryResult.setSelfRescueGrade(grades.get("selfRescue"));
            primaryResult.setOverallCapability(overallCapability);
            primaryResult.setOverallGrade(overallGrade);
            primaryResult.setCreateTime(LocalDateTime.now());
            primaryResult.setUpdateTime(LocalDateTime.now());

            primaryIndicatorResultMapper.insert(primaryResult);

            // 传递给专题图生成
            generateThematicMapForEvaluation(evaluationId, primaryResult);

            log.info("评估结果保存成功，评估ID: {}", evaluationId);

        } catch (Exception e) {
            log.error("保存评估结果失败，评估ID: {}", evaluationId, e);
            throw new RuntimeException("保存评估结果失败: " + e.getMessage());
        }
    }

    /**
     * 计算一级指标能力值（用于评估）
     */
    private Map<String, Double> calculateLevel1CapabilitiesForEvaluation(List<SecondaryIndicatorResult> secondaryResults) {
        Map<String, Double> capabilities = new HashMap<>();
        
        if (secondaryResults == null || secondaryResults.isEmpty()) {
            capabilities.put("management", 0.0);
            capabilities.put("preparation", 0.0);
            capabilities.put("selfRescue", 0.0);
            return capabilities;
        }
        
        // 从第一个结果中计算各一级指标的平均值
        SecondaryIndicatorResult result = secondaryResults.get(0);
        
        // 计算管理能力 (A1-A33)
        double managementSum = 0.0;
        int managementCount = 0;
        for (int i = 1; i <= 33; i++) {
            Double value = getSecondaryIndicatorNormalizedValue(result, "A" + i);
            if (value != null) {
                managementSum += value;
                managementCount++;
            }
        }
        capabilities.put("management", managementCount > 0 ? managementSum / managementCount : 0.0);
        
        // 计算准备能力 (A34-A66)
        double preparationSum = 0.0;
        int preparationCount = 0;
        for (int i = 34; i <= 66; i++) {
            Double value = getSecondaryIndicatorNormalizedValue(result, "A" + i);
            if (value != null) {
                preparationSum += value;
                preparationCount++;
            }
        }
        capabilities.put("preparation", preparationCount > 0 ? preparationSum / preparationCount : 0.0);
        
        // 计算自救能力 (A67-A100)
        double selfRescueSum = 0.0;
        int selfRescueCount = 0;
        for (int i = 67; i <= 100; i++) {
            Double value = getSecondaryIndicatorNormalizedValue(result, "A" + i);
            if (value != null) {
                selfRescueSum += value;
                selfRescueCount++;
            }
        }
        capabilities.put("selfRescue", selfRescueCount > 0 ? selfRescueSum / selfRescueCount : 0.0);

        return capabilities;
    }

    /**
     * 计算分级（用于评估）
     */
    private Map<String, String> calculateGradesForEvaluation(Map<String, Double> capabilities) {
        Map<String, String> grades = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : capabilities.entrySet()) {
            String category = entry.getKey();
            Double value = entry.getValue();
            
            String grade;
            if (value >= 0.8) {
                grade = "极强";
            } else if (value >= 0.6) {
                grade = "较强";
            } else if (value >= 0.4) {
                grade = "中等";
            } else if (value >= 0.2) {
                grade = "较弱";
            } else {
                grade = "极弱";
            }
            
            grades.put(category, grade);
        }
        
        return grades;
    }

    /**
     * 计算综合减灾能力（用于评估）
     */
    private double calculateOverallCapabilityForEvaluation(Map<String, Double> capabilities) {
        // 使用等权重计算综合能力
        double sum = capabilities.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        return capabilities.size() > 0 ? sum / capabilities.size() : 0.0;
    }

    /**
     * 计算综合减灾能力分级（用于评估）
     */
    private String calculateOverallGradeForEvaluation(double overallCapability) {
        if (overallCapability >= 0.8) {
            return "极强";
        } else if (overallCapability >= 0.6) {
            return "较强";
        } else if (overallCapability >= 0.4) {
            return "中等";
        } else if (overallCapability >= 0.2) {
            return "较弱";
        } else {
            return "极弱";
        }
    }

    /**
     * 为评估生成专题图和报告
     */
    private void generateThematicMapForEvaluation(Long evaluationId, PrimaryIndicatorResult primaryResult) {
        try {
            // 这里可以调用专题图生成服务
            // 暂时记录日志
            log.info("开始生成专题图，评估ID: {}, 综合能力: {}, 综合分级: {}", 
                evaluationId, primaryResult.getOverallCapability(), primaryResult.getOverallGrade());
            
            // TODO: 实际的专题图生成逻辑
            
        } catch (Exception e) {
            log.error("生成专题图失败，评估ID: {}", evaluationId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 记录评估历史
     */
    @Transactional(rollbackFor = Exception.class)
    private void recordEvaluationHistory(Long surveyId, Long algorithmId, Long weightConfigId, Map<String, Object> algorithmResult) {
        log.info("开始记录评估历史");
        
        try {
            // 创建评估报告记录
            Report report = new Report();
            report.setReportName("减灾能力评估报告_" + surveyId + "_" + System.currentTimeMillis());
            report.setReportType("EVALUATION");
            report.setGenerateTime(LocalDateTime.now());
            
            // 保存报告到数据库
            reportService.save(report);
            
            log.info("评估历史记录完成: reportId={}, executionId={}", 
                    report.getId(), algorithmResult.get("executionId"));
            
        } catch (Exception e) {
            log.error("记录评估历史失败", e);
            throw new RuntimeException("记录评估历史失败: " + e.getMessage(), e);
        }
    }
}