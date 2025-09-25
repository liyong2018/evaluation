package com.evaluate.service.impl;

import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.AlgorithmStep;
import com.evaluate.entity.FormulaConfig;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.AlgorithmStepMapper;
import com.evaluate.mapper.FormulaConfigMapper;
import com.evaluate.service.AlgorithmExecutionService;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.ISurveyDataService;
import com.evaluate.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 算法执行服务实现类
 */
@Slf4j
@Service
public class AlgorithmExecutionServiceImpl implements AlgorithmExecutionService {
    
    @Autowired
    private AlgorithmStepMapper algorithmStepMapper;
    
    @Autowired
    private FormulaConfigMapper formulaConfigMapper;
    
    @Autowired
    private RegionService regionService;
    
    @Autowired
    private ISurveyDataService surveyDataService;
    
    @Autowired
    private IIndicatorWeightService indicatorWeightService;
    
    // 存储执行进度的Map
    private final Map<String, Map<String, Object>> executionProgressMap = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, Object> executeAlgorithm(
            AlgorithmConfig algorithmConfig, 
            List<SurveyData> surveyDataList, 
            Map<String, Double> weightConfig,
            List<Long> regionIds) {
        
        String executionId = UUID.randomUUID().toString();
        log.info("开始执行算法: {}, 执行ID: {}", algorithmConfig.getConfigName(), executionId);
        
        try {
            // 初始化进度
            Map<String, Object> progress = new HashMap<>();
            progress.put("status", "RUNNING");
            progress.put("percentage", 0);
            progress.put("message", "算法执行中...");
            progress.put("startTime", System.currentTimeMillis());
            executionProgressMap.put(executionId, progress);
            
            // 获取算法步骤
            List<AlgorithmStep> steps = algorithmStepMapper.selectByAlgorithmId(algorithmConfig.getId());
            if (steps.isEmpty()) {
                throw new RuntimeException("算法配置中没有找到执行步骤");
            }
            
            // 按步骤顺序排序
            steps.sort(Comparator.comparing(AlgorithmStep::getStepOrder));
            
            Map<String, Object> result = new HashMap<>();
            result.put("executionId", executionId);
            result.put("algorithmName", algorithmConfig.getConfigName());
            result.put("dataCount", surveyDataList.size());
            result.put("regionCount", regionIds != null ? regionIds.size() : 0);
            
            // 执行各个步骤
            Map<String, Object> stepResults = new HashMap<>();
            for (int i = 0; i < steps.size(); i++) {
                AlgorithmStep step = steps.get(i);
                
                // 更新进度
                int percentage = (i + 1) * 100 / steps.size();
                progress.put("percentage", percentage);
                progress.put("message", "执行步骤: " + step.getStepName());
                
                log.info("执行算法步骤: {}", step.getStepName());
                
                // 根据步骤类型执行相应的处理
                Map<String, Object> stepResult = executeAlgorithmStep(step, surveyDataList, weightConfig, stepResults);
                stepResults.put(step.getStepCode(), stepResult);
            }
            
            // 生成最终结果
            result.put("steps", stepResults);
            result.put("finalScores", calculateFinalScores(surveyDataList, weightConfig, stepResults));
            result.put("rankings", calculateRankings(surveyDataList, stepResults));
            result.put("summary", generateSummary(stepResults));
            
            // 更新完成状态
            progress.put("status", "SUCCESS");
            progress.put("percentage", 100);
            progress.put("message", "算法执行完成");
            progress.put("endTime", System.currentTimeMillis());
            
            log.info("算法执行完成: {}, 执行ID: {}", algorithmConfig.getConfigName(), executionId);
            return result;
            
        } catch (Exception e) {
            log.error("算法执行失败: {}, 执行ID: {}", algorithmConfig.getConfigName(), executionId, e);
            
            // 更新失败状态
            Map<String, Object> progress = executionProgressMap.get(executionId);
            if (progress != null) {
                progress.put("status", "FAILED");
                progress.put("message", "算法执行失败: " + e.getMessage());
                progress.put("endTime", System.currentTimeMillis());
            }
            
            throw new RuntimeException("算法执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行单个算法步骤
     */
    private Map<String, Object> executeAlgorithmStep(
            AlgorithmStep step, 
            List<SurveyData> surveyDataList, 
            Map<String, Double> weightConfig,
            Map<String, Object> previousResults) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 根据步骤编码执行相应处理
        switch (step.getStepCode()) {
            case "SECONDARY_CALCULATION":
                result = processData(surveyDataList, null);
                break;
            case "NORMALIZATION":
                result = performNormalization(surveyDataList, null, previousResults);
                break;
            case "SECONDARY_WEIGHTING":
                result = performWeighting(surveyDataList, weightConfig, null, previousResults);
                break;
            case "TOPSIS_CALCULATION":
                result = performRanking(surveyDataList, null, previousResults);
                break;
            case "GRADING_CALCULATION":
                result = performGrading(surveyDataList, null, previousResults);
                break;
            default:
                log.warn("未知的步骤编码: {}", step.getStepCode());
                result.put("message", "跳过未知步骤编码: " + step.getStepCode());
        }
        
        result.put("stepName", step.getStepName());
        result.put("stepCode", step.getStepCode());
        result.put("executionTime", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 数据处理步骤
     */
    private Map<String, Object> processData(List<SurveyData> surveyDataList, String params) {
        Map<String, Object> result = new HashMap<>();
        
        // 数据清洗和预处理
        List<SurveyData> processedData = surveyDataList.stream()
                .filter(data -> data != null && data.getRegionCode() != null)
                .collect(Collectors.toList());
        
        result.put("originalCount", surveyDataList.size());
        result.put("processedCount", processedData.size());
        result.put("processedData", processedData);
        
        return result;
    }
    
    /**
     * 计算步骤
     */
    private Map<String, Object> performCalculation(
            List<SurveyData> surveyDataList, 
            Map<String, Double> weightConfig, 
            String params,
            Map<String, Object> previousResults) {
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> scores = new HashMap<>();
        
        // 模拟计算过程
        for (SurveyData data : surveyDataList) {
            double score = calculateScore(data, weightConfig);
            String key = data.getRegionCode() + "_" + (data.getTownship() != null ? data.getTownship() : data.getCounty());
            scores.put(key, score);
        }
        
        result.put("scores", scores);
        result.put("averageScore", scores.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        result.put("maxScore", scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        result.put("minScore", scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        
        return result;
    }
    
    /**
     * 归一化步骤
     */
    private Map<String, Object> performNormalization(
            List<SurveyData> surveyDataList, 
            String params,
            Map<String, Object> previousResults) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 从前一步获取分数
        @SuppressWarnings("unchecked")
        Map<String, Double> scores = (Map<String, Double>) previousResults.get("calculation");
        if (scores == null) {
            scores = new HashMap<>();
            for (SurveyData data : surveyDataList) {
                String key = data.getRegionCode() + "_" + (data.getTownship() != null ? data.getTownship() : data.getCounty());
                scores.put(key, Math.random() * 100);
            }
        }
        
        // 执行归一化
        double maxScore = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double minScore = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        
        Map<String, Double> normalizedScores = new HashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            double normalized = (entry.getValue() - minScore) / (maxScore - minScore);
            normalizedScores.put(entry.getKey(), normalized);
        }
        
        result.put("normalizedScores", normalizedScores);
        result.put("normalizationMethod", "min-max");
        
        return result;
    }
    
    /**
     * 加权步骤
     */
    private Map<String, Object> performWeighting(
            List<SurveyData> surveyDataList, 
            Map<String, Double> weightConfig, 
            String params,
            Map<String, Object> previousResults) {
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> weightedScores = new HashMap<>();
        
        // 应用权重
        for (SurveyData data : surveyDataList) {
            double weightedScore = calculateWeightedScore(data, weightConfig);
            String key = data.getRegionCode() + "_" + (data.getTownship() != null ? data.getTownship() : data.getCounty());
            weightedScores.put(key, weightedScore);
        }
        
        result.put("weightedScores", weightedScores);
        result.put("appliedWeights", weightConfig);
        
        return result;
    }
    
    /**
     * 排序步骤
     */
    private Map<String, Object> performRanking(
            List<SurveyData> surveyDataList, 
            String params,
            Map<String, Object> previousResults) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取最终分数进行排序
        Map<String, Double> finalScores = getFinalScores(previousResults);
        
        List<Map.Entry<String, Double>> sortedEntries = finalScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        Map<String, Integer> rankings = new HashMap<>();
        for (int i = 0; i < sortedEntries.size(); i++) {
            rankings.put(sortedEntries.get(i).getKey(), i + 1);
        }
        
        result.put("rankings", rankings);
        result.put("sortedList", sortedEntries);
        
        return result;
    }
    
    /**
     * 分级步骤
     */
    private Map<String, Object> performGrading(
            List<SurveyData> surveyDataList, 
            String params,
            Map<String, Object> previousResults) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取最终分数进行分级
        Map<String, Double> finalScores = getFinalScores(previousResults);
        Map<String, String> grades = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : finalScores.entrySet()) {
            String grade = calculateGrade(entry.getValue());
            grades.put(entry.getKey(), grade);
        }
        
        result.put("grades", grades);
        result.put("gradingCriteria", getGradingCriteria());
        
        return result;
    }
    
    /**
     * 计算单个数据的分数
     */
    private double calculateScore(SurveyData data, Map<String, Double> weightConfig) {
        // 模拟计算逻辑
        return Math.random() * 100;
    }
    
    /**
     * 计算加权分数
     */
    private double calculateWeightedScore(SurveyData data, Map<String, Double> weightConfig) {
        // 模拟加权计算
        return Math.random() * 100;
    }
    
    /**
     * 获取最终分数
     */
    private Map<String, Double> getFinalScores(Map<String, Object> previousResults) {
        // 尝试从加权结果获取
        @SuppressWarnings("unchecked")
        Map<String, Double> weightedScores = (Map<String, Double>) previousResults.get("weighting");
        if (weightedScores != null) {
            return weightedScores;
        }
        
        // 尝试从归一化结果获取
        @SuppressWarnings("unchecked")
        Map<String, Double> normalizedScores = (Map<String, Double>) previousResults.get("normalization");
        if (normalizedScores != null) {
            return normalizedScores;
        }
        
        // 尝试从计算结果获取
        @SuppressWarnings("unchecked")
        Map<String, Double> calculationScores = (Map<String, Double>) previousResults.get("calculation");
        if (calculationScores != null) {
            return calculationScores;
        }
        
        return new HashMap<>();
    }
    
    /**
     * 计算等级
     */
    private String calculateGrade(double score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "中等";
        if (score >= 60) return "及格";
        return "不及格";
    }
    
    /**
     * 获取分级标准
     */
    private Map<String, String> getGradingCriteria() {
        Map<String, String> criteria = new HashMap<>();
        criteria.put("优秀", "90-100分");
        criteria.put("良好", "80-89分");
        criteria.put("中等", "70-79分");
        criteria.put("及格", "60-69分");
        criteria.put("不及格", "0-59分");
        return criteria;
    }
    
    /**
     * 计算最终分数
     */
    private Map<String, Double> calculateFinalScores(
            List<SurveyData> surveyDataList, 
            Map<String, Double> weightConfig,
            Map<String, Object> stepResults) {
        
        return getFinalScores(stepResults);
    }
    
    /**
     * 计算排名
     */
    private Map<String, Integer> calculateRankings(
            List<SurveyData> surveyDataList,
            Map<String, Object> stepResults) {
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> rankings = (Map<String, Integer>) stepResults.get("ranking");
        return rankings != null ? rankings : new HashMap<>();
    }
    
    /**
     * 生成摘要
     */
    private Map<String, Object> generateSummary(Map<String, Object> stepResults) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("executedSteps", stepResults.keySet());
        summary.put("totalSteps", stepResults.size());
        summary.put("executionTime", System.currentTimeMillis());
        return summary;
    }
    
    @Override
    public boolean validateAlgorithmParams(AlgorithmConfig algorithmConfig, Map<String, Object> parameters) {
        // 参数验证逻辑
        return true;
    }
    
    @Override
    public Map<String, Object> getExecutionProgress(String executionId) {
        return executionProgressMap.get(executionId);
    }
    
    @Override
    public boolean stopExecution(String executionId) {
        Map<String, Object> progress = executionProgressMap.get(executionId);
        if (progress != null) {
            progress.put("status", "STOPPED");
            progress.put("message", "执行已停止");
            progress.put("endTime", System.currentTimeMillis());
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> getSupportedAlgorithmTypes() {
        return Arrays.asList(
            "AHP", "层次分析法",
            "FUZZY", "模糊综合评价",
            "GREY", "灰色关联分析",
            "ENTROPY", "熵权法",
            "TOPSIS", "逼近理想解排序法",
            "COMPREHENSIVE", "综合评价法"
        );
    }
    
    @Override
    public Map<String, Object> calculateStepResult(
            AlgorithmConfig algorithmConfig,
            Long stepId,
            Integer stepIndex,
            String formula,
            List<String> regionIds,
            Map<String, Object> parameters) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取真实的调查数据并进行计算
            List<Map<String, Object>> tableData = calculateRealStepData(stepIndex, regionIds, formula);
            List<Map<String, Object>> columns = generateStepColumns(stepIndex);
            Map<String, Object> summary = generateStepSummary(tableData, stepIndex);
            
            result.put("tableData", tableData);
            result.put("columns", columns);
            result.put("summary", summary);
            result.put("stepId", stepId);
            result.put("stepIndex", stepIndex);
            result.put("formula", formula);
            result.put("calculationTime", System.currentTimeMillis());
            
            log.info("步骤 {} 计算完成，生成 {} 条数据", stepIndex + 1, tableData.size());
            
        } catch (Exception e) {
            log.error("计算步骤结果失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 计算真实的步骤数据
     */
    private List<Map<String, Object>> calculateRealStepData(Integer stepIndex, List<String> regionIds, String formula) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        // 直接处理字符串格式的地区ID
        for (String regionId : regionIds) {
            Map<String, Object> row = new HashMap<>();
            
            // 从字符串ID中提取地区名称
            String regionName = extractRegionNameFromId(regionId);
            row.put("region", regionName);
            
            // 根据地区名称获取调查数据
            List<SurveyData> surveyDataList = surveyDataService.getBySurveyRegion(regionName);
            
            if (!surveyDataList.isEmpty()) {
                SurveyData surveyData = surveyDataList.get(0); // 取第一条数据
                
                if (stepIndex == 0) { // 二级指标计算 - 实现8个指标
                    // 1. 队伍管理能力=(本级灾害管理工作人员总数/常住人口数量)*10000
                    double teamManagement = calculateIndicatorValue(surveyData.getManagementStaff(), surveyData.getPopulation());
                    
                    // 2. 风险评估能力=IF(是否开展乡镇灾害风险评估="是",1,0)
                    double riskAssessment = "是".equals(surveyData.getRiskAssessment()) ? 1.0 : 0.0;
                    
                    // 3. 财政投入能力=(上一年度防灾减灾救灾资金投入总金额/常住人口数量)*10000
                    double financialInput = calculateIndicatorValueFromDouble(surveyData.getFundingAmount(), surveyData.getPopulation());
                    
                    // 4. 物资储备能力=(现有储备物资装备折合金额/常住人口数量)*10000
                    double materialReserve = calculateIndicatorValueFromDouble(surveyData.getMaterialValue(), surveyData.getPopulation());
                    
                    // 5. 医疗保障能力=(实有住院床位数/常住人口数量)*10000
                    double medicalSupport = calculateIndicatorValue(surveyData.getHospitalBeds(), surveyData.getPopulation());
                    
                    // 6. 自救互救能力=(消防员数量+志愿者人数+民兵预备役人数)/常住人口数量)*10000
                    int totalRescuePersonnel = (surveyData.getFirefighters() != null ? surveyData.getFirefighters() : 0) +
                                              (surveyData.getVolunteers() != null ? surveyData.getVolunteers() : 0) +
                                              (surveyData.getMilitiaReserve() != null ? surveyData.getMilitiaReserve() : 0);
                    double selfRescue = calculateIndicatorValue(totalRescuePersonnel, surveyData.getPopulation());
                    
                    // 7. 公众避险能力=(上一年度组织的应急管理培训和演练参与人次/常住人口数量)*100
                    double publicAvoidance = calculateIndicatorValue(surveyData.getTrainingParticipants(), surveyData.getPopulation())/100;
                    
                    // 8. 转移安置能力=(本级灾害应急避难场所容量/常住人口数量)
                    double relocationCapacity = calculateIndicatorValue(surveyData.getShelterCapacity(), surveyData.getPopulation())/10000;
                    
                    // 设置8个指标值
                    row.put("teamManagement", String.format("%.8f", teamManagement));
                    row.put("riskAssessment", String.format("%.8f", riskAssessment));
                    row.put("financialInput", String.format("%.8f", financialInput));
                    row.put("materialReserve", String.format("%.8f", materialReserve));
                    row.put("medicalSupport", String.format("%.8f", medicalSupport));
                    row.put("selfRescue", String.format("%.8f", selfRescue));
                    row.put("publicAvoidance", String.format("%.8f", publicAvoidance));
                    row.put("relocationCapacity", String.format("%.8f", relocationCapacity));
                    
                } else if (stepIndex == 1) { // 属性向量归一化
                    // 先收集所有地区的8个指标原始值
                    Map<String, List<Double>> allIndicatorValues = collectAllIndicatorValues(regionIds);
                    
                    // 计算当前地区的8个指标原始值
                    double teamManagement = calculateIndicatorValue(surveyData.getManagementStaff(), surveyData.getPopulation());
                    double riskAssessment = "是".equals(surveyData.getRiskAssessment()) ? 1.0 : 0.0;
                    double financialInput = calculateIndicatorValueFromDouble(surveyData.getFundingAmount(), surveyData.getPopulation());
                    double materialReserve = calculateIndicatorValueFromDouble(surveyData.getMaterialValue(), surveyData.getPopulation());
                    double medicalSupport = calculateIndicatorValue(surveyData.getHospitalBeds(), surveyData.getPopulation());
                    int totalRescuePersonnel = (surveyData.getFirefighters() != null ? surveyData.getFirefighters() : 0) +
                                              (surveyData.getVolunteers() != null ? surveyData.getVolunteers() : 0) +
                                              (surveyData.getMilitiaReserve() != null ? surveyData.getMilitiaReserve() : 0);
                    double selfRescue = calculateIndicatorValue(totalRescuePersonnel, surveyData.getPopulation());
                    double publicAvoidance = calculateIndicatorValue(surveyData.getTrainingParticipants(), surveyData.getPopulation())/100;
                    double relocationCapacity = calculateIndicatorValue(surveyData.getShelterCapacity(), surveyData.getPopulation())/10000;
                    
                    // 计算属性向量归一化值
                    double teamManagementNorm = normalizeIndicatorValue(teamManagement, allIndicatorValues.get("teamManagement"));
                    double riskAssessmentNorm = normalizeIndicatorValue(riskAssessment, allIndicatorValues.get("riskAssessment"));
                    double financialInputNorm = normalizeIndicatorValue(financialInput, allIndicatorValues.get("financialInput"));
                    double materialReserveNorm = normalizeIndicatorValue(materialReserve, allIndicatorValues.get("materialReserve"));
                    double medicalSupportNorm = normalizeIndicatorValue(medicalSupport, allIndicatorValues.get("medicalSupport"));
                    double selfRescueNorm = normalizeIndicatorValue(selfRescue, allIndicatorValues.get("selfRescue"));
                    double publicAvoidanceNorm = normalizeIndicatorValue(publicAvoidance, allIndicatorValues.get("publicAvoidance"));
                    double relocationCapacityNorm = normalizeIndicatorValue(relocationCapacity, allIndicatorValues.get("relocationCapacity"));
                    
                    // 设置8个指标的归一化值
                    row.put("teamManagement", String.format("%.8f", teamManagementNorm));
                    row.put("riskAssessment", String.format("%.8f", riskAssessmentNorm));
                    row.put("financialInput", String.format("%.8f", financialInputNorm));
                    row.put("materialReserve", String.format("%.8f", materialReserveNorm));
                    row.put("medicalSupport", String.format("%.8f", medicalSupportNorm));
                    row.put("selfRescue", String.format("%.8f", selfRescueNorm));
                    row.put("publicAvoidance", String.format("%.8f", publicAvoidanceNorm));
                    row.put("relocationCapacity", String.format("%.8f", relocationCapacityNorm));
                } else if (stepIndex == 2) { // 二级指标定权
                    // 使用与TOPSIS算法相同的计算逻辑
                    Map<String, Double> currentWeightedValues = calculateCurrentWeightedValues(surveyData, regionIds);
                    
                    // 设置8个指标的定权值（与TOPSIS算法保持一致）
                    row.put("teamManagement", String.format("%.8f", currentWeightedValues.get("teamManagement")));
                    row.put("riskAssessment", String.format("%.8f", currentWeightedValues.get("riskAssessment")));
                    row.put("financialInput", String.format("%.8f", currentWeightedValues.get("financialInput")));
                    row.put("materialReserve", String.format("%.8f", currentWeightedValues.get("materialReserve")));
                    row.put("medicalSupport", String.format("%.8f", currentWeightedValues.get("medicalSupport")));
                    row.put("selfRescue", String.format("%.8f", currentWeightedValues.get("selfRescue")));
                    row.put("publicAvoidance", String.format("%.8f", currentWeightedValues.get("publicAvoidance")));
                    row.put("relocationCapacity", String.format("%.8f", currentWeightedValues.get("relocationCapacity")));
                } else if (stepIndex == 3) { // 优劣解算法计算一级指标
                    // 获取所有地区的定权结果
                    Map<String, Map<String, Double>> allWeightedValues = collectAllWeightedValues(regionIds);
                    
                    // 计算当前地区的定权值
                    Map<String, Double> currentWeightedValues = calculateCurrentWeightedValues(surveyData, regionIds);
                    
                    // 使用TOPSIS算法计算3个一级指标
                    Map<String, Double> primaryIndicators = calculateTOPSIS(currentWeightedValues, allWeightedValues);
                    
                    // 设置3个一级指标值
                    row.put("disasterManagement", String.format("%.8f", primaryIndicators.get("disasterManagement")));
                    row.put("disasterPreparedness", String.format("%.8f", primaryIndicators.get("disasterPreparedness")));
                    row.put("selfRescueTransfer", String.format("%.8f", primaryIndicators.get("selfRescueTransfer")));
                } else if (stepIndex == 4) { // 能力分级计算
                    // 获取所有地区的一级指标结果
                    Map<String, List<Double>> allPrimaryIndicators = collectAllPrimaryIndicators(regionIds);
                    
                    // 计算当前地区的一级指标值
                    Map<String, Double> currentPrimaryIndicators = calculateCurrentPrimaryIndicators(surveyData, regionIds);
                    
                    // 计算分级结果
                    Map<String, String> grades = calculateCapabilityGrades(currentPrimaryIndicators, allPrimaryIndicators);
                    
                    // 设置3个一级指标的分级结果
                    row.put("disasterManagement", grades.get("disasterManagement"));
                    row.put("disasterPreparedness", grades.get("disasterPreparedness"));
                    row.put("selfRescueTransfer", grades.get("selfRescueTransfer"));
                } else { // 其他步骤
                    double value = calculateIndicatorValue(surveyData.getManagementStaff(), surveyData.getPopulation());
                    double weight = 0.25; // 固定权重
                    double score = value * weight;
                    
                    row.put("value", String.format("%.2f", value));
                    row.put("weight", String.format("%.3f", weight));
                    row.put("score", String.format("%.2f", score));
                    row.put("rank", tableData.size() + 1);
                }
            } else {
                // 如果没有调查数据，使用默认值
                if (stepIndex == 0) {
                    row.put("teamManagement", "0.00000000");
                    row.put("riskAssessment", "0.00000000");
                    row.put("financialInput", "0.00000000");
                    row.put("materialReserve", "0.00000000");
                    row.put("medicalSupport", "0.00000000");
                    row.put("selfRescue", "0.00000000");
                    row.put("publicAvoidance", "0.00000000");
                    row.put("relocationCapacity", "0.00000000");
                } else {
                    if (stepIndex == 1 || stepIndex == 2) {
                        // 步骤2：属性向量归一化 和 步骤3：二级指标定权 - 8个指标默认值
                        row.put("teamManagement", "0.00000000");
                        row.put("riskAssessment", "0.00000000");
                        row.put("financialInput", "0.00000000");
                        row.put("materialReserve", "0.00000000");
                        row.put("medicalSupport", "0.00000000");
                        row.put("selfRescue", "0.00000000");
                        row.put("publicAvoidance", "0.00000000");
                        row.put("relocationCapacity", "0.00000000");
                    } else if (stepIndex == 3) {
                        // 步骤4：优劣解算法 - 3个一级指标默认值
                        row.put("disasterManagement", "0.00000000");
                        row.put("disasterPreparedness", "0.00000000");
                        row.put("selfRescueTransfer", "0.00000000");
                    } else if (stepIndex == 4) {
                        // 步骤5：能力分级计算 - 3个一级指标分级默认值
                        row.put("disasterManagement", "中等");
                        row.put("disasterPreparedness", "中等");
                        row.put("selfRescueTransfer", "中等");
                    } else {
                        // 其他步骤的默认值
                        row.put("value", "0.00");
                        row.put("weight", "0.000");
                        row.put("score", "0.00");
                        row.put("rank", tableData.size() + 1);
                    }
                }
            }
            
            tableData.add(row);
        }
        
        return tableData;
    }
    
    /**
     * 计算指标值：(staff/population)*10000
     */
    private double calculateIndicatorValue(Integer staff, Long population) {
        if (staff == null || population == null || population == 0) {
            return 0.0;
        }
        return (staff.doubleValue() / population.doubleValue()) * 10000;
    }
    
    /**
     * 计算指标值：(amount/population)*10000 (处理Double类型)
     */
    private double calculateIndicatorValueFromDouble(Double amount, Long population) {
        if (amount == null || population == null || population == 0) {
            return 0.0;
        }
        return (amount / population.doubleValue()) * 10000;
    }
    
    /**
     * 从字符串ID中提取地区名称
     * 例如：township_四川省_眉山市_青神县_青竹街道 -> 青竹街道
     */
    private String extractRegionNameFromId(String regionId) {
        if (regionId == null || regionId.isEmpty()) {
            return "未知地区";
        }
        
        // 如果是township_开头的格式
        if (regionId.startsWith("township_")) {
            String[] parts = regionId.split("_");
            if (parts.length >= 5) {
                // 返回最后一部分（乡镇名称）
                return parts[parts.length - 1];
            }
        }
        
        // 如果不是预期格式，直接返回原字符串
        return regionId;
    }
    
    /**
     * 收集所有地区的8个指标原始值
     */
    private Map<String, List<Double>> collectAllIndicatorValues(List<String> regionIds) {
        Map<String, List<Double>> allValues = new HashMap<>();
        allValues.put("teamManagement", new ArrayList<>());
        allValues.put("riskAssessment", new ArrayList<>());
        allValues.put("financialInput", new ArrayList<>());
        allValues.put("materialReserve", new ArrayList<>());
        allValues.put("medicalSupport", new ArrayList<>());
        allValues.put("selfRescue", new ArrayList<>());
        allValues.put("publicAvoidance", new ArrayList<>());
        allValues.put("relocationCapacity", new ArrayList<>());
        
        for (String regionId : regionIds) {
            String regionName = extractRegionNameFromId(regionId);
            List<SurveyData> surveyDataList = surveyDataService.getBySurveyRegion(regionName);
            
            if (!surveyDataList.isEmpty()) {
                SurveyData surveyData = surveyDataList.get(0);
                
                // 计算8个指标原始值
                double teamManagement = calculateIndicatorValue(surveyData.getManagementStaff(), surveyData.getPopulation());
                double riskAssessment = "是".equals(surveyData.getRiskAssessment()) ? 1.0 : 0.0;
                double financialInput = calculateIndicatorValueFromDouble(surveyData.getFundingAmount(), surveyData.getPopulation());
                double materialReserve = calculateIndicatorValueFromDouble(surveyData.getMaterialValue(), surveyData.getPopulation());
                double medicalSupport = calculateIndicatorValue(surveyData.getHospitalBeds(), surveyData.getPopulation());
                int totalRescuePersonnel = (surveyData.getFirefighters() != null ? surveyData.getFirefighters() : 0) +
                                          (surveyData.getVolunteers() != null ? surveyData.getVolunteers() : 0) +
                                          (surveyData.getMilitiaReserve() != null ? surveyData.getMilitiaReserve() : 0);
                double selfRescue = calculateIndicatorValue(totalRescuePersonnel, surveyData.getPopulation());
                double publicAvoidance = calculateIndicatorValue(surveyData.getTrainingParticipants(), surveyData.getPopulation())/100;
                double relocationCapacity = calculateIndicatorValue(surveyData.getShelterCapacity(), surveyData.getPopulation())/10000;
                
                // 添加到对应的列表中
                allValues.get("teamManagement").add(teamManagement);
                allValues.get("riskAssessment").add(riskAssessment);
                allValues.get("financialInput").add(financialInput);
                allValues.get("materialReserve").add(materialReserve);
                allValues.get("medicalSupport").add(medicalSupport);
                allValues.get("selfRescue").add(selfRescue);
                allValues.get("publicAvoidance").add(publicAvoidance);
                allValues.get("relocationCapacity").add(relocationCapacity);
            } else {
                // 如果没有数据，添加0值
                allValues.get("teamManagement").add(0.0);
                allValues.get("riskAssessment").add(0.0);
                allValues.get("financialInput").add(0.0);
                allValues.get("materialReserve").add(0.0);
                allValues.get("medicalSupport").add(0.0);
                allValues.get("selfRescue").add(0.0);
                allValues.get("publicAvoidance").add(0.0);
                allValues.get("relocationCapacity").add(0.0);
            }
        }
        
        return allValues;
    }
    
    /**
     * 计算属性向量归一化值
     * 公式：本乡镇指标值 / SQRT(SUMSQ(全部乡镇指标值))
     */
    private double normalizeIndicatorValue(double currentValue, List<Double> allValues) {
        if (allValues == null || allValues.isEmpty()) {
            return 0.0;
        }
        
        // 计算平方和
        double sumOfSquares = allValues.stream()
            .mapToDouble(value -> value * value)
            .sum();
        
        // 计算平方根
        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);
        
        // 避免除零
        if (sqrtSumOfSquares == 0.0) {
            return 0.0;
        }
        
        // 返回归一化值
        return currentValue / sqrtSumOfSquares;
    }
    
    /**
     * 收集所有地区的一级指标值
     */
    private Map<String, List<Double>> collectAllPrimaryIndicators(List<String> regionIds) {
        Map<String, List<Double>> allValues = new HashMap<>();
        allValues.put("disasterManagement", new ArrayList<>());
        allValues.put("disasterPreparedness", new ArrayList<>());
        allValues.put("selfRescueTransfer", new ArrayList<>());
        
        for (String regionId : regionIds) {
            String regionName = extractRegionNameFromId(regionId);
            List<SurveyData> surveyDataList = surveyDataService.getBySurveyRegion(regionName);
            
            if (!surveyDataList.isEmpty()) {
                SurveyData surveyData = surveyDataList.get(0);
                
                // 计算当前地区的一级指标值
                Map<String, Double> primaryIndicators = calculateCurrentPrimaryIndicators(surveyData, regionIds);
                
                allValues.get("disasterManagement").add(primaryIndicators.get("disasterManagement"));
                allValues.get("disasterPreparedness").add(primaryIndicators.get("disasterPreparedness"));
                allValues.get("selfRescueTransfer").add(primaryIndicators.get("selfRescueTransfer"));
            } else {
                // 如果没有数据，添加0值
                allValues.get("disasterManagement").add(0.0);
                allValues.get("disasterPreparedness").add(0.0);
                allValues.get("selfRescueTransfer").add(0.0);
            }
        }
        
        return allValues;
    }
    
    /**
     * 计算当前地区的一级指标值
     */
    private Map<String, Double> calculateCurrentPrimaryIndicators(SurveyData surveyData, List<String> regionIds) {
        // 获取所有地区的定权结果
        Map<String, Map<String, Double>> allWeightedValues = collectAllWeightedValues(regionIds);
        
        // 计算当前地区的定权值
        Map<String, Double> currentWeightedValues = calculateCurrentWeightedValues(surveyData, regionIds);
        
        // 使用TOPSIS算法计算3个一级指标
        return calculateTOPSIS(currentWeightedValues, allWeightedValues);
    }
    
    /**
     * 计算能力分级
     */
    private Map<String, String> calculateCapabilityGrades(Map<String, Double> currentValues, Map<String, List<Double>> allValues) {
        Map<String, String> grades = new HashMap<>();
        
        // 为每个一级指标计算分级
        for (String indicator : Arrays.asList("disasterManagement", "disasterPreparedness", "selfRescueTransfer")) {
            double currentValue = currentValues.get(indicator);
            List<Double> values = allValues.get(indicator);
            
            // 计算均值和标准差
            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            // 根据复杂的IF条件进行分级
            String grade = calculateGrade(currentValue, mean, stdDev);
            grades.put(indicator, grade);
        }
        
        return grades;
    }
    
    /**
     * 根据均值和标准差计算分级
     */
    private String calculateGrade(double value, double mean, double stdDev) {
        if (mean <= 0.5 * stdDev) {
            if (value >= mean + 1.5 * stdDev) {
                return "强";
            } else if (value >= mean + 0.5 * stdDev) {
                return "较强";
            } else {
                return "中等";
            }
        } else if (mean <= 1.5 * stdDev) {
            if (value >= mean + 1.5 * stdDev) {
                return "强";
            } else if (value >= mean + 0.5 * stdDev) {
                return "较强";
            } else if (value >= mean - 0.5 * stdDev) {
                return "中等";
            } else {
                return "较弱";
            }
        } else {
            if (value >= mean + 1.5 * stdDev) {
                return "强";
            } else if (value >= mean + 0.5 * stdDev) {
                return "较强";
            } else if (value >= mean - 0.5 * stdDev) {
                return "中等";
            } else if (value >= mean - 1.5 * stdDev) {
                return "较弱";
            } else {
                return "弱";
            }
        }
    }
    
    /**
     * 获取二级指标权重配置
     */
    private Map<String, Double> getSecondaryIndicatorWeights(Long configId) {
        Map<String, Double> weights = new HashMap<>();
        
        try {
            // 获取二级指标权重配置
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigIdAndLevel(configId, 2);
            
            log.info("getSecondaryIndicatorWeights - 从数据库获取到 {} 条权重配置记录", indicatorWeights.size());
            
            // 将指标权重映射到对应的字段名
            for (IndicatorWeight weight : indicatorWeights) {
                String indicatorCode = weight.getIndicatorCode();
                Double weightValue = weight.getWeight();
                
                log.info("getSecondaryIndicatorWeights - 处理权重配置: indicatorCode={}, weightValue={}", indicatorCode, weightValue);
                
                // 根据指标代码映射到对应的字段名
                switch (indicatorCode) {
                    case "L2_MANAGEMENT_CAPABILITY":
                        weights.put("teamManagement", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_MANAGEMENT_CAPABILITY -> teamManagement: {}", weightValue);
                        break;
                    case "L2_RISK_ASSESSMENT":
                        weights.put("riskAssessment", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_RISK_ASSESSMENT -> riskAssessment: {}", weightValue);
                        break;
                    case "L2_FUNDING":
                        weights.put("financialInput", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_FUNDING -> financialInput: {}", weightValue);
                        break;
                    case "L2_MATERIAL":
                        weights.put("materialReserve", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_MATERIAL -> materialReserve: {}", weightValue);
                        break;
                    case "L2_MEDICAL":
                        weights.put("medicalSupport", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_MEDICAL -> medicalSupport: {}", weightValue);
                        break;
                    case "L2_SELF_RESCUE":
                        weights.put("selfRescue", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_SELF_RESCUE -> selfRescue: {}", weightValue);
                        break;
                    case "L2_PUBLIC_AVOIDANCE":
                        weights.put("publicAvoidance", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_PUBLIC_AVOIDANCE -> publicAvoidance: {}", weightValue);
                        break;
                    case "L2_RELOCATION":
                        weights.put("relocationCapacity", weightValue);
                        log.info("getSecondaryIndicatorWeights - 映射 L2_RELOCATION -> relocationCapacity: {}", weightValue);
                        break;
                    default:
                        log.warn("getSecondaryIndicatorWeights - 未识别的指标代码: {}", indicatorCode);
                        break;
                }
            }
        } catch (Exception e) {
            log.warn("获取二级指标权重配置失败，使用默认权重: {}", e.getMessage());
        }
        
        // 如果没有获取到权重配置，使用默认权重
        if (weights.isEmpty()) {
            log.warn("getSecondaryIndicatorWeights - 未获取到任何权重配置，使用默认权重");
            weights.put("teamManagement", 0.37);
            weights.put("riskAssessment", 0.31);
            weights.put("financialInput", 0.32);
            weights.put("materialReserve", 0.51);
            weights.put("medicalSupport", 0.49);
            weights.put("selfRescue", 0.33);
            weights.put("publicAvoidance", 0.33);
            weights.put("relocationCapacity", 0.34);
        }
        
        log.info("getSecondaryIndicatorWeights - 最终权重配置: {}", weights);
        return weights;
    }
    
    /**
     * 生成步骤表格列定义
     */
    private List<Map<String, Object>> generateStepColumns(Integer stepIndex) {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        // 地区列（所有步骤都有）
        Map<String, Object> regionColumn = new HashMap<>();
        regionColumn.put("prop", "region");
        regionColumn.put("label", "地区");
        regionColumn.put("width", 120);
        columns.add(regionColumn);
        
        if (stepIndex == 0) { // 二级指标计算 - 8个指标
            columns.add(createColumn("teamManagement", "队伍管理能力", 120));
            columns.add(createColumn("riskAssessment", "风险评估能力", 120));
            columns.add(createColumn("financialInput", "财政投入能力", 120));
            columns.add(createColumn("materialReserve", "物资储备能力", 120));
            columns.add(createColumn("medicalSupport", "医疗保障能力", 120));
            columns.add(createColumn("selfRescue", "自救互救能力", 120));
            columns.add(createColumn("publicAvoidance", "公众避险能力", 120));
            columns.add(createColumn("relocationCapacity", "转移安置能力", 120));
        } else if (stepIndex == 1) { // 属性向量归一化 - 8个指标归一化值
            columns.add(createColumn("teamManagement", "队伍管理能力", 120));
            columns.add(createColumn("riskAssessment", "风险评估能力", 120));
            columns.add(createColumn("financialInput", "财政投入能力", 120));
            columns.add(createColumn("materialReserve", "物资储备能力", 120));
            columns.add(createColumn("medicalSupport", "医疗保障能力", 120));
            columns.add(createColumn("selfRescue", "自救互救能力", 120));
            columns.add(createColumn("publicAvoidance", "公众避险能力", 120));
            columns.add(createColumn("relocationCapacity", "转移安置能力", 120));
        } else if (stepIndex == 2) { // 二级指标定权 - 8个指标定权值
            columns.add(createColumn("teamManagement", "队伍管理能力", 120));
            columns.add(createColumn("riskAssessment", "风险评估能力", 120));
            columns.add(createColumn("financialInput", "财政投入能力", 120));
            columns.add(createColumn("materialReserve", "物资储备能力", 120));
            columns.add(createColumn("medicalSupport", "医疗保障能力", 120));
            columns.add(createColumn("selfRescue", "自救互救能力", 120));
            columns.add(createColumn("publicAvoidance", "公众避险能力", 120));
            columns.add(createColumn("relocationCapacity", "转移安置能力", 120));
        } else if (stepIndex == 3) { // 优劣解算法 - 3个一级指标
            columns.add(createColumn("disasterManagement", "灾害管理能力", 150));
            columns.add(createColumn("disasterPreparedness", "灾害备灾能力", 150));
            columns.add(createColumn("selfRescueTransfer", "自救转移能力", 150));
        } else if (stepIndex == 4) { // 能力分级计算 - 3个一级指标分级
            columns.add(createColumn("disasterManagement", "灾害管理能力", 150));
            columns.add(createColumn("disasterPreparedness", "灾害备灾能力", 150));
            columns.add(createColumn("selfRescueTransfer", "自救转移能力", 150));
        } else { // 其他步骤
            columns.add(createColumn("value", "数值", 100));
            columns.add(createColumn("weight", "权重", 100));
            columns.add(createColumn("score", "得分", 100));
            columns.add(createColumn("rank", "排名", 80));
        }
        
        return columns;
    }
    
    /**
     * 创建列定义
     */
    private Map<String, Object> createColumn(String prop, String label, Integer width) {
        Map<String, Object> column = new HashMap<>();
        column.put("prop", prop);
        column.put("label", label);
        column.put("width", width);
        return column;
    }
    
    /**
     * 生成步骤汇总信息
     */
    private Map<String, Object> generateStepSummary(List<Map<String, Object>> tableData, Integer stepIndex) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("数据条数", tableData.size());
        
        if (stepIndex == 0 || stepIndex == 1 || stepIndex == 2) {
            // 步骤0、步骤1和步骤2都是8个指标的统计信息
            if (!tableData.isEmpty()) {
                // 计算队伍管理能力的统计信息
                double teamManagementSum = tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                    .sum();
                double avgTeamManagement = teamManagementSum / tableData.size();
                
                if (stepIndex == 0) {
                    summary.put("平均分", String.format("%.8f", avgTeamManagement));
                    summary.put("最高分", String.format("%.8f", tableData.stream()
                        .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                        .max().orElse(0.0)));
                    summary.put("最低分", String.format("%.8f", tableData.stream()
                        .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                        .min().orElse(0.0)));
                } else if (stepIndex == 1) {
                    // stepIndex == 1: 属性向量归一化
                    summary.put("平均归一化值", String.format("%.8f", avgTeamManagement));
                    summary.put("最高归一化值", String.format("%.8f", tableData.stream()
                        .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                        .max().orElse(0.0)));
                    summary.put("最低归一化值", String.format("%.8f", tableData.stream()
                        .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                        .min().orElse(0.0)));
                } else {
                    // stepIndex == 2: 二级指标定权
                    summary.put("平均定权值", String.format("%.8f", avgTeamManagement));
                    summary.put("最高定权值", String.format("%.8f", tableData.stream()
                        .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                        .max().orElse(0.0)));
                    summary.put("最低定权值", String.format("%.8f", tableData.stream()
                        .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                        .min().orElse(0.0)));
                }
            } else {
                if (stepIndex == 0) {
                    summary.put("平均分", "NaN");
                    summary.put("最高分", "0");
                    summary.put("最低分", "0");
                } else if (stepIndex == 1) {
                    summary.put("平均归一化值", "NaN");
                    summary.put("最高归一化值", "0");
                    summary.put("最低归一化值", "0");
                } else {
                    summary.put("平均定权值", "NaN");
                    summary.put("最高定权值", "0");
                    summary.put("最低定权值", "0");
                }
            }
        } else if (stepIndex == 3) {
            // 步骤3：优劣解算法 - 3个一级指标统计信息
            if (!tableData.isEmpty()) {
                // 计算灾害管理能力的统计信息
                double disasterManagementSum = tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("disasterManagement").toString()))
                    .sum();
                double avgDisasterManagement = disasterManagementSum / tableData.size();
                
                summary.put("平均一级指标值", String.format("%.8f", avgDisasterManagement));
                summary.put("最高一级指标值", String.format("%.8f", tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("disasterManagement").toString()))
                    .max().orElse(0.0)));
                summary.put("最低一级指标值", String.format("%.8f", tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("disasterManagement").toString()))
                    .min().orElse(0.0)));
            } else {
                summary.put("平均一级指标值", "NaN");
                summary.put("最高一级指标值", "0");
                summary.put("最低一级指标值", "0");
            }
        } else if (stepIndex == 4) {
            // 步骤4：能力分级计算 - 分级统计信息
            if (!tableData.isEmpty()) {
                // 统计各个等级的数量
                Map<String, Long> gradeCount = new HashMap<>();
                
                // 统计灾害管理能力分级
                Map<String, Long> disasterManagementGrades = tableData.stream()
                    .collect(Collectors.groupingBy(
                        row -> row.get("disasterManagement").toString(),
                        Collectors.counting()
                    ));
                
                summary.put("灾害管理能力分级统计", disasterManagementGrades);
                summary.put("强等级数量", disasterManagementGrades.getOrDefault("强", 0L));
                summary.put("较强等级数量", disasterManagementGrades.getOrDefault("较强", 0L));
                summary.put("中等等级数量", disasterManagementGrades.getOrDefault("中等", 0L));
                summary.put("较弱等级数量", disasterManagementGrades.getOrDefault("较弱", 0L));
                summary.put("弱等级数量", disasterManagementGrades.getOrDefault("弱", 0L));
            } else {
                summary.put("灾害管理能力分级统计", new HashMap<>());
                summary.put("强等级数量", 0L);
                summary.put("较强等级数量", 0L);
                summary.put("中等等级数量", 0L);
                summary.put("较弱等级数量", 0L);
                summary.put("弱等级数量", 0L);
            }
        } else {
            // 其他步骤：计算得分的统计信息
            if (!tableData.isEmpty()) {
                double scoreSum = tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("score").toString()))
                    .sum();
                double avgScore = scoreSum / tableData.size();
                
                summary.put("平均得分", String.format("%.2f", avgScore));
                summary.put("最高得分", String.format("%.2f", tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("score").toString()))
                    .max().orElse(0.0)));
                summary.put("最低得分", String.format("%.2f", tableData.stream()
                    .mapToDouble(row -> Double.parseDouble(row.get("score").toString()))
                    .min().orElse(0.0)));
            } else {
                summary.put("平均得分", "NaN");
                summary.put("最高得分", "0");
                summary.put("最低得分", "0");
            }
        }
        
        return summary;
    }
    
    /**
     * 收集所有地区的定权结果
     */
    private Map<String, Map<String, Double>> collectAllWeightedValues(List<String> regionIds) {
        Map<String, Map<String, Double>> allWeightedValues = new HashMap<>();
        
        // 获取所有地区的归一化值
        Map<String, List<Double>> allIndicatorValues = collectAllIndicatorValues(regionIds);
        
        // 获取二级权重配置
        Map<String, Double> indicatorWeights = getSecondaryIndicatorWeights(1L);
        
        log.info("collectAllWeightedValues - 开始计算所有地区的定权值，地区数量: {}", regionIds.size());
        log.info("collectAllWeightedValues - 二级权重配置: {}", indicatorWeights);
        
        for (int i = 0; i < regionIds.size(); i++) {
            String regionId = regionIds.get(i);
            String regionName = extractRegionNameFromId(regionId);
            
            Map<String, Double> weightedValues = new HashMap<>();
            
            // 计算每个指标的定权值：归一化值 × 二级权重指标
            for (String indicator : Arrays.asList("teamManagement", "riskAssessment", "financialInput", 
                    "materialReserve", "medicalSupport", "selfRescue", "publicAvoidance", "relocationCapacity")) {
                
                List<Double> values = allIndicatorValues.get(indicator);
                if (values != null && i < values.size()) {
                    double originalValue = values.get(i);
                    double normalizedValue = normalizeIndicatorValue(originalValue, values);
                    double weight = indicatorWeights.getOrDefault(indicator, 0.33);
                    double weightedValue = normalizedValue * weight;
                    weightedValues.put(indicator, weightedValue);
                    
                    // 为物资储备能力添加详细日志
                    if ("materialReserve".equals(indicator)) {
                        log.info("collectAllWeightedValues - 地区: {}, 物资储备能力: 原始值={}, 归一化值={}, 权重={}, 定权值={}", 
                                regionName, originalValue, normalizedValue, weight, weightedValue);
                    }
                } else {
                    weightedValues.put(indicator, 0.0);
                }
            }
            
            allWeightedValues.put(regionName, weightedValues);
            log.info("collectAllWeightedValues - 地区: {} 的所有定权值: {}", regionName, weightedValues);
        }
        
        // 输出物资储备能力的所有值用于调试
        log.info("collectAllWeightedValues - 所有地区物资储备能力定权值:");
        for (Map.Entry<String, Map<String, Double>> entry : allWeightedValues.entrySet()) {
            log.info("  地区: {}, 物资储备能力定权值: {}", entry.getKey(), entry.getValue().get("materialReserve"));
        }
        
        return allWeightedValues;
    }
    
    /**
     * 计算当前地区的定权值
     */
    private Map<String, Double> calculateCurrentWeightedValues(SurveyData surveyData, List<String> regionIds) {
        Map<String, Double> currentWeightedValues = new HashMap<>();
        
        // 获取所有地区的归一化值
        Map<String, List<Double>> allIndicatorValues = collectAllIndicatorValues(regionIds);
        
        // 获取二级权重配置
        Map<String, Double> indicatorWeights = getSecondaryIndicatorWeights(1L);
        
        log.info("calculateCurrentWeightedValues - 开始计算当前地区定权值");
        log.info("calculateCurrentWeightedValues - 二级权重配置: {}", indicatorWeights);
        
        // 计算当前地区的8个指标原始值
        double teamManagement = calculateIndicatorValue(surveyData.getManagementStaff(), surveyData.getPopulation());
        double riskAssessment = "是".equals(surveyData.getRiskAssessment()) ? 1.0 : 0.0;
        double financialInput = calculateIndicatorValueFromDouble(surveyData.getFundingAmount(), surveyData.getPopulation());
        double materialReserve = calculateIndicatorValueFromDouble(surveyData.getMaterialValue(), surveyData.getPopulation());
        double medicalSupport = calculateIndicatorValue(surveyData.getHospitalBeds(), surveyData.getPopulation());
        int totalRescuePersonnel = (surveyData.getFirefighters() != null ? surveyData.getFirefighters() : 0) +
                                  (surveyData.getVolunteers() != null ? surveyData.getVolunteers() : 0) +
                                  (surveyData.getMilitiaReserve() != null ? surveyData.getMilitiaReserve() : 0);
        double selfRescue = calculateIndicatorValue(totalRescuePersonnel, surveyData.getPopulation());
        double publicAvoidance = calculateIndicatorValue(surveyData.getTrainingParticipants(), surveyData.getPopulation())/100;
        double relocationCapacity = calculateIndicatorValue(surveyData.getShelterCapacity(), surveyData.getPopulation())/10000;
        
        log.info("calculateCurrentWeightedValues - 当前地区原始指标值: 物资储备能力={}, 医疗保障能力={}", materialReserve, medicalSupport);
        
        // 计算归一化值并乘以权重
        double materialReserveNorm = normalizeIndicatorValue(materialReserve, allIndicatorValues.get("materialReserve"));
        double materialReserveWeight = indicatorWeights.getOrDefault("materialReserve", 0.51);
        double materialReserveWeighted = materialReserveNorm * materialReserveWeight;
        
        log.info("calculateCurrentWeightedValues - 物资储备能力计算: 原始值={}, 归一化值={}, 权重={}, 定权值={}", 
                materialReserve, materialReserveNorm, materialReserveWeight, materialReserveWeighted);
        
        currentWeightedValues.put("teamManagement", 
            normalizeIndicatorValue(teamManagement, allIndicatorValues.get("teamManagement")) * 
            indicatorWeights.getOrDefault("teamManagement", 0.37));
        currentWeightedValues.put("riskAssessment", 
            normalizeIndicatorValue(riskAssessment, allIndicatorValues.get("riskAssessment")) * 
            indicatorWeights.getOrDefault("riskAssessment", 0.31));
        currentWeightedValues.put("financialInput", 
            normalizeIndicatorValue(financialInput, allIndicatorValues.get("financialInput")) * 
            indicatorWeights.getOrDefault("financialInput", 0.32));
        currentWeightedValues.put("materialReserve", materialReserveWeighted);
        currentWeightedValues.put("medicalSupport", 
            normalizeIndicatorValue(medicalSupport, allIndicatorValues.get("medicalSupport")) * 
            indicatorWeights.getOrDefault("medicalSupport", 0.49));
        currentWeightedValues.put("selfRescue", 
            normalizeIndicatorValue(selfRescue, allIndicatorValues.get("selfRescue")) * 
            indicatorWeights.getOrDefault("selfRescue", 0.33));
        currentWeightedValues.put("publicAvoidance", 
            normalizeIndicatorValue(publicAvoidance, allIndicatorValues.get("publicAvoidance")) * 
            indicatorWeights.getOrDefault("publicAvoidance", 0.33));
        currentWeightedValues.put("relocationCapacity", 
            normalizeIndicatorValue(relocationCapacity, allIndicatorValues.get("relocationCapacity")) * 
            indicatorWeights.getOrDefault("relocationCapacity", 0.34));
        
        log.info("calculateCurrentWeightedValues - 当前地区所有定权值: {}", currentWeightedValues);
        
        return currentWeightedValues;
    }
    
    /**
     * 使用TOPSIS算法计算3个一级指标
     */
    private Map<String, Double> calculateTOPSIS(Map<String, Double> currentWeightedValues, 
                                               Map<String, Map<String, Double>> allWeightedValues) {
        Map<String, Double> primaryIndicators = new HashMap<>();
        
        // 计算各指标的最大值和最小值
        Map<String, Double> maxValues = new HashMap<>();
        Map<String, Double> minValues = new HashMap<>();
        
        for (String indicator : Arrays.asList("teamManagement", "riskAssessment", "financialInput", 
                "materialReserve", "medicalSupport", "selfRescue", "publicAvoidance", "relocationCapacity")) {
            
            double max = allWeightedValues.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .max().orElse(0.0);
            double min = allWeightedValues.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .min().orElse(0.0);
            
            maxValues.put(indicator, max);
            minValues.put(indicator, min);
        }
        
        // 添加详细日志输出
        log.info("TOPSIS算法计算 - 当前地区定权值: {}", currentWeightedValues);
        log.info("TOPSIS算法计算 - 各指标最大值: {}", maxValues);
        log.info("TOPSIS算法计算 - 各指标最小值: {}", minValues);
        
        // 详细输出所有地区的物资储备能力定权值用于调试
        log.info("TOPSIS算法计算 - 所有地区的物资储备能力定权值详情:");
        for (Map.Entry<String, Map<String, Double>> entry : allWeightedValues.entrySet()) {
            String regionName = entry.getKey();
            Double materialReserveValue = entry.getValue().get("materialReserve");
            log.info("  地区: {}, 物资储备能力定权值: {}", regionName, materialReserveValue);
        }
        
        // 验证物资储备能力最大值计算
        double calculatedMaxMaterialReserve = allWeightedValues.values().stream()
            .mapToDouble(values -> values.getOrDefault("materialReserve", 0.0))
            .max().orElse(0.0);
        log.info("TOPSIS算法计算 - 计算得出的物资储备能力最大值: {}", calculatedMaxMaterialReserve);
        log.info("TOPSIS算法计算 - 与前端显示值0.4431的差异: {}", Math.abs(calculatedMaxMaterialReserve - 0.4431));
        
        // 计算3个一级指标的TOPSIS值
        
        // 1. 灾害管理能力（队伍管理+风险评估+财政投入）
        double disasterManagementPositive = Math.sqrt(
            Math.pow(maxValues.get("teamManagement") - currentWeightedValues.get("teamManagement"), 2) +
            Math.pow(maxValues.get("riskAssessment") - currentWeightedValues.get("riskAssessment"), 2) +
            Math.pow(maxValues.get("financialInput") - currentWeightedValues.get("financialInput"), 2)
        );
        double disasterManagementNegative = Math.sqrt(
            Math.pow(minValues.get("teamManagement") - currentWeightedValues.get("teamManagement"), 2) +
            Math.pow(minValues.get("riskAssessment") - currentWeightedValues.get("riskAssessment"), 2) +
            Math.pow(minValues.get("financialInput") - currentWeightedValues.get("financialInput"), 2)
        );
        
        // 防止除零错误
        double disasterManagement = 0.0;
        if (disasterManagementNegative + disasterManagementPositive > 0) {
            disasterManagement = disasterManagementNegative / (disasterManagementNegative + disasterManagementPositive);
        }
        
        log.info("灾害管理能力计算 - 正理想解距离: {}, 负理想解距离: {}, 相对贴近度: {}", 
                disasterManagementPositive, disasterManagementNegative, disasterManagement);
        
        // 2. 灾害备灾能力（物资储备+医疗保障）
        double materialReserveCurrent = currentWeightedValues.get("materialReserve");
        double medicalSupportCurrent = currentWeightedValues.get("medicalSupport");
        double materialReserveMax = maxValues.get("materialReserve");
        double medicalSupportMax = maxValues.get("medicalSupport");
        double materialReserveMin = minValues.get("materialReserve");
        double medicalSupportMin = minValues.get("medicalSupport");
        
        log.info("灾害备灾能力计算 - 物资储备能力: 当前值={}, 最大值={}, 最小值={}", 
                materialReserveCurrent, materialReserveMax, materialReserveMin);
        log.info("灾害备灾能力计算 - 医疗保障能力: 当前值={}, 最大值={}, 最小值={}", 
                medicalSupportCurrent, medicalSupportMax, medicalSupportMin);
        
        double disasterPreparednessPositive = Math.sqrt(
            Math.pow(materialReserveMax - materialReserveCurrent, 2) +
            Math.pow(medicalSupportMax - medicalSupportCurrent, 2)
        );
        double disasterPreparednessNegative = Math.sqrt(
            Math.pow(materialReserveMin - materialReserveCurrent, 2) +
            Math.pow(medicalSupportMin - medicalSupportCurrent, 2)
        );
        
        log.info("灾害备灾能力计算 - 正理想解距离计算: sqrt(({}-{})^2 + ({}-{})^2) = sqrt({} + {}) = {}", 
                materialReserveMax, materialReserveCurrent, medicalSupportMax, medicalSupportCurrent,
                Math.pow(materialReserveMax - materialReserveCurrent, 2),
                Math.pow(medicalSupportMax - medicalSupportCurrent, 2),
                disasterPreparednessPositive);
        
        log.info("灾害备灾能力计算 - 负理想解距离计算: sqrt(({}-{})^2 + ({}-{})^2) = sqrt({} + {}) = {}", 
                materialReserveMin, materialReserveCurrent, medicalSupportMin, medicalSupportCurrent,
                Math.pow(materialReserveMin - materialReserveCurrent, 2),
                Math.pow(medicalSupportMin - medicalSupportCurrent, 2),
                disasterPreparednessNegative);
        
        // 防止除零错误
        double disasterPreparedness = 0.0;
        if (disasterPreparednessNegative + disasterPreparednessPositive > 0) {
            disasterPreparedness = disasterPreparednessNegative / (disasterPreparednessNegative + disasterPreparednessPositive);
        }
        
        log.info("灾害备灾能力计算 - 正理想解距离: {}, 负理想解距离: {}, 相对贴近度: {} / ({} + {}) = {}", 
                disasterPreparednessPositive, disasterPreparednessNegative, 
                disasterPreparednessNegative, disasterPreparednessNegative, disasterPreparednessPositive, disasterPreparedness);
        
        // 3. 自救转移能力（自救互救+公众避险+转移安置）
        double selfRescueTransferPositive = Math.sqrt(
            Math.pow(maxValues.get("selfRescue") - currentWeightedValues.get("selfRescue"), 2) +
            Math.pow(maxValues.get("publicAvoidance") - currentWeightedValues.get("publicAvoidance"), 2) +
            Math.pow(maxValues.get("relocationCapacity") - currentWeightedValues.get("relocationCapacity"), 2)
        );
        double selfRescueTransferNegative = Math.sqrt(
            Math.pow(minValues.get("selfRescue") - currentWeightedValues.get("selfRescue"), 2) +
            Math.pow(minValues.get("publicAvoidance") - currentWeightedValues.get("publicAvoidance"), 2) +
            Math.pow(minValues.get("relocationCapacity") - currentWeightedValues.get("relocationCapacity"), 2)
        );
        
        // 防止除零错误
        double selfRescueTransfer = 0.0;
        if (selfRescueTransferNegative + selfRescueTransferPositive > 0) {
            selfRescueTransfer = selfRescueTransferNegative / (selfRescueTransferNegative + selfRescueTransferPositive);
        }
        
        log.info("自救转移能力计算 - 正理想解距离: {}, 负理想解距离: {}, 相对贴近度: {}", 
                selfRescueTransferPositive, selfRescueTransferNegative, selfRescueTransfer);
        
        primaryIndicators.put("disasterManagement", disasterManagement);
        primaryIndicators.put("disasterPreparedness", disasterPreparedness);
        primaryIndicators.put("selfRescueTransfer", selfRescueTransfer);
        
        log.info("TOPSIS算法计算结果 - 灾害管理能力: {}, 灾害备灾能力: {}, 自救转移能力: {}", 
                disasterManagement, disasterPreparedness, selfRescueTransfer);
        
        return primaryIndicators;
    }
}