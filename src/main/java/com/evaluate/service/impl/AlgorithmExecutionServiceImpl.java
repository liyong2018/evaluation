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
    
    // 缓存步骤4的计算结果，避免步骤5重新计算
    private final Map<String, Map<String, Double>> step4ResultsCache = new ConcurrentHashMap<>();
    
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
        
        // 如果是步骤4，清理缓存以确保重新计算
        if (stepIndex == 3) {
            step4ResultsCache.clear();
        }
        
        try {
            if (stepIndex == 2) {
                // 步骤2：二级指标定权 - 返回两个表格的数据结构
                Map<String, Object> dualTableResult = calculateDualTableStepData(stepIndex, regionIds, formula);
                result.putAll(dualTableResult);
            } else {
                // 其他步骤：返回单表格数据结构
                List<Map<String, Object>> tableData = calculateRealStepData(stepIndex, regionIds, formula);
                List<Map<String, Object>> columns = generateStepColumns(stepIndex);
                // 统计信息已移除
                
                result.put("tableData", tableData);
                result.put("columns", columns);
                // summary字段已移除
            }
            
            result.put("stepId", stepId);
            result.put("stepIndex", stepIndex);
            result.put("formula", formula);
            result.put("calculationTime", System.currentTimeMillis());
            
            // 根据不同的步骤类型记录日志
            if (stepIndex == 2) {
                // 双表格情况
                List<Map<String, Object>> table1Data = (List<Map<String, Object>>) result.get("table1Data");
                List<Map<String, Object>> table2Data = (List<Map<String, Object>>) result.get("table2Data");
                int table1Size = table1Data != null ? table1Data.size() : 0;
                int table2Size = table2Data != null ? table2Data.size() : 0;
                log.info("步骤 {} 计算完成，生成双表格数据 - 表格1: {} 条，表格2: {} 条", stepIndex + 1, table1Size, table2Size);
            } else {
                // 单表格情况
                List<Map<String, Object>> tableData = (List<Map<String, Object>>) result.get("tableData");
                int dataSize = tableData != null ? tableData.size() : 0;
                log.info("步骤 {} 计算完成，生成 {} 条数据", stepIndex + 1, dataSize);
            }
            
        } catch (Exception e) {
            log.error("计算步骤结果失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 计算双表格步骤数据（步骤2专用）
     */
    private Map<String, Object> calculateDualTableStepData(Integer stepIndex, List<String> regionIds, String formula) {
        Map<String, Object> result = new HashMap<>();
        
        // 表格1：一级指标权重计算（8个指标的定权值）
        List<Map<String, Object>> table1Data = new ArrayList<>();
        List<Map<String, Object>> table1Columns = generateTable1Columns();
        
        // 表格2：乡镇减灾能力权重计算（8个指标乘以权重后的值）
        List<Map<String, Object>> table2Data = new ArrayList<>();
        List<Map<String, Object>> table2Columns = generateTable2Columns();
        
        // 获取权重配置
        Map<String, Double> primaryWeights = getPrimaryIndicatorWeights(1L);
        Map<String, Double> secondaryWeights = getSecondaryIndicatorWeights(1L);
        
        log.info("calculateDualTableStepData - 开始计算双表格数据");
        log.info("calculateDualTableStepData - 一级权重配置: {}", primaryWeights);
        log.info("calculateDualTableStepData - 二级权重配置: {}", secondaryWeights);
        
        // 获取所有地区的归一化值
        Map<String, List<Double>> allIndicatorValues = collectAllIndicatorValues(regionIds);
        
        // 为每个地区生成两个表格的数据
        for (String regionId : regionIds) {
            String regionName = extractRegionNameFromId(regionId);
            List<SurveyData> surveyDataList = surveyDataService.getBySurveyRegion(regionName);
            
            Map<String, Object> row1 = new HashMap<>();
            Map<String, Object> row2 = new HashMap<>();
            
            row1.put("region", regionName);
            row2.put("region", regionName);
            
            if (!surveyDataList.isEmpty()) {
                SurveyData surveyData = surveyDataList.get(0);
                
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
                
                // 计算归一化值
                double teamManagementNorm = normalizeIndicatorValue(teamManagement, allIndicatorValues.get("teamManagement"));
                double riskAssessmentNorm = normalizeIndicatorValue(riskAssessment, allIndicatorValues.get("riskAssessment"));
                double financialInputNorm = normalizeIndicatorValue(financialInput, allIndicatorValues.get("financialInput"));
                double materialReserveNorm = normalizeIndicatorValue(materialReserve, allIndicatorValues.get("materialReserve"));
                double medicalSupportNorm = normalizeIndicatorValue(medicalSupport, allIndicatorValues.get("medicalSupport"));
                double selfRescueNorm = normalizeIndicatorValue(selfRescue, allIndicatorValues.get("selfRescue"));
                double publicAvoidanceNorm = normalizeIndicatorValue(publicAvoidance, allIndicatorValues.get("publicAvoidance"));
                double relocationCapacityNorm = normalizeIndicatorValue(relocationCapacity, allIndicatorValues.get("relocationCapacity"));
                
                // 表格1：一级指标权重计算 - 定权公式：属性向量归一化 × 二级权重
                double table1TeamManagement = teamManagementNorm * secondaryWeights.getOrDefault("teamManagement", 0.37);
                double table1RiskAssessment = riskAssessmentNorm * secondaryWeights.getOrDefault("riskAssessment", 0.31);
                double table1FinancialInput = financialInputNorm * secondaryWeights.getOrDefault("financialInput", 0.32);
                double table1MaterialReserve = materialReserveNorm * secondaryWeights.getOrDefault("materialReserve", 0.51);
                double table1MedicalSupport = medicalSupportNorm * secondaryWeights.getOrDefault("medicalSupport", 0.49);
                double table1SelfRescue = selfRescueNorm * secondaryWeights.getOrDefault("selfRescue", 0.33);
                double table1PublicAvoidance = publicAvoidanceNorm * secondaryWeights.getOrDefault("publicAvoidance", 0.33);
                double table1RelocationCapacity = relocationCapacityNorm * secondaryWeights.getOrDefault("relocationCapacity", 0.34);
                
                row1.put("teamManagement", String.format("%.8f", table1TeamManagement));
                row1.put("riskAssessment", String.format("%.8f", table1RiskAssessment));
                row1.put("financialInput", String.format("%.8f", table1FinancialInput));
                row1.put("materialReserve", String.format("%.8f", table1MaterialReserve));
                row1.put("medicalSupport", String.format("%.8f", table1MedicalSupport));
                row1.put("selfRescue", String.format("%.8f", table1SelfRescue));
                row1.put("publicAvoidance", String.format("%.8f", table1PublicAvoidance));
                row1.put("relocationCapacity", String.format("%.8f", table1RelocationCapacity));
                
                // 表格2：乡镇减灾能力权重计算 - 定权公式：属性向量归一化 × 一级权重 × 二级权重
                double table2TeamManagement = teamManagementNorm * primaryWeights.getOrDefault("disasterManagement", 0.33) * secondaryWeights.getOrDefault("teamManagement", 0.37);
                double table2RiskAssessment = riskAssessmentNorm * primaryWeights.getOrDefault("disasterManagement", 0.33) * secondaryWeights.getOrDefault("riskAssessment", 0.31);
                double table2FinancialInput = financialInputNorm * primaryWeights.getOrDefault("disasterManagement", 0.33) * secondaryWeights.getOrDefault("financialInput", 0.32);
                double table2MaterialReserve = materialReserveNorm * primaryWeights.getOrDefault("disasterPreparedness", 0.32) * secondaryWeights.getOrDefault("materialReserve", 0.51);
                double table2MedicalSupport = medicalSupportNorm * primaryWeights.getOrDefault("disasterPreparedness", 0.32) * secondaryWeights.getOrDefault("medicalSupport", 0.49);
                double table2SelfRescue = selfRescueNorm * primaryWeights.getOrDefault("selfRescueTransfer", 0.35) * secondaryWeights.getOrDefault("selfRescue", 0.33);
                double table2PublicAvoidance = publicAvoidanceNorm * primaryWeights.getOrDefault("selfRescueTransfer", 0.35) * secondaryWeights.getOrDefault("publicAvoidance", 0.33);
                double table2RelocationCapacity = relocationCapacityNorm * primaryWeights.getOrDefault("selfRescueTransfer", 0.35) * secondaryWeights.getOrDefault("relocationCapacity", 0.34);
                
                row2.put("teamManagement", String.format("%.8f", table2TeamManagement));
                row2.put("riskAssessment", String.format("%.8f", table2RiskAssessment));
                row2.put("financialInput", String.format("%.8f", table2FinancialInput));
                row2.put("materialReserve", String.format("%.8f", table2MaterialReserve));
                row2.put("medicalSupport", String.format("%.8f", table2MedicalSupport));
                row2.put("selfRescue", String.format("%.8f", table2SelfRescue));
                row2.put("publicAvoidance", String.format("%.8f", table2PublicAvoidance));
                row2.put("relocationCapacity", String.format("%.8f", table2RelocationCapacity));
                
                log.info("地区 {} - 表格1计算完成，队伍管理能力: 归一化值={}, 二级权重={}, 结果={}", 
                        regionName, teamManagementNorm, secondaryWeights.getOrDefault("teamManagement", 0.37), table1TeamManagement);
                log.info("地区 {} - 表格2计算完成，队伍管理能力: 归一化值={}, 一级权重={}, 二级权重={}, 结果={}", 
                        regionName, teamManagementNorm, primaryWeights.getOrDefault("disasterManagement", 0.33), 
                        secondaryWeights.getOrDefault("teamManagement", 0.37), table2TeamManagement);
                        
            } else {
                // 默认值
                String[] indicators = {"teamManagement", "riskAssessment", "financialInput", "materialReserve", "medicalSupport", "selfRescue", "publicAvoidance", "relocationCapacity"};
                for (String indicator : indicators) {
                    row1.put(indicator, "0.00000000");
                    row2.put(indicator, "0.00000000");
                }
            }
            
            table1Data.add(row1);
            table2Data.add(row2);
        }
        
        // 统计信息已移除
        
        // 构建返回结果
        result.put("table1Data", table1Data);
        result.put("table1Columns", table1Columns);
        // table1Summary已移除
        result.put("table2Data", table2Data);
        result.put("table2Columns", table2Columns);
        // table2Summary已移除
        result.put("isDualTable", true);
        
        return result;
    }
    
    /**
     * 生成表格1的列配置（一级指标权重计算）
     */
    private List<Map<String, Object>> generateTable1Columns() {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        // 地区列
        columns.add(createColumn("region", "地区", 120));
        
        // 8个指标列
        columns.add(createColumn("teamManagement", "队伍管理能力", 120));
        columns.add(createColumn("riskAssessment", "风险评估能力", 120));
        columns.add(createColumn("financialInput", "财政投入能力", 120));
        columns.add(createColumn("materialReserve", "物资储备能力", 120));
        columns.add(createColumn("medicalSupport", "医疗保障能力", 120));
        columns.add(createColumn("selfRescue", "自救互救能力", 120));
        columns.add(createColumn("publicAvoidance", "公众避险能力", 120));
        columns.add(createColumn("relocationCapacity", "转移安置能力", 120));
        
        return columns;
    }
    
    /**
     * 生成表格2的列配置（乡镇减灾能力权重计算）
     */
    private List<Map<String, Object>> generateTable2Columns() {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        // 地区列
        columns.add(createColumn("region", "地区", 120));
        
        // 8个指标列（乘以权重后）
        columns.add(createColumn("teamManagement", "队伍管理能力", 120));
        columns.add(createColumn("riskAssessment", "风险评估能力", 120));
        columns.add(createColumn("financialInput", "财政投入能力", 120));
        columns.add(createColumn("materialReserve", "物资储备能力", 120));
        columns.add(createColumn("medicalSupport", "医疗保障能力", 120));
        columns.add(createColumn("selfRescue", "自救互救能力", 120));
        columns.add(createColumn("publicAvoidance", "公众避险能力", 120));
        columns.add(createColumn("relocationCapacity", "转移安置能力", 120));
        
        return columns;
    }
    
    /**
     * 获取指标权重配置
     */
    private Map<String, Double> getIndicatorWeights() {
        Map<String, Double> weights = new HashMap<>();
        
        // 设置默认权重（可以从数据库或配置文件读取）
        weights.put("teamManagement", 0.125);    // 队伍管理能力权重
        weights.put("riskAssessment", 0.125);    // 风险评估能力权重
        weights.put("financialInput", 0.125);    // 财政投入能力权重
        weights.put("materialReserve", 0.125);   // 物资储备能力权重
        weights.put("medicalSupport", 0.125);    // 医疗保障能力权重
        weights.put("selfRescue", 0.125);        // 自救互救能力权重
        weights.put("publicAvoidance", 0.125);   // 公众避险能力权重
        weights.put("relocationCapacity", 0.125); // 转移安置能力权重
        
        return weights;
    }
    
    /**
     * 生成表格1的汇总信息
     */
    private Map<String, Object> generateTable1Summary(List<Map<String, Object>> tableData) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("数据条数", tableData.size());
        summary.put("表格类型", "一级指标权重计算");
        summary.put("计算方法", "定权公式：属性向量归一化 × 二级权重");
        
        if (!tableData.isEmpty()) {
            double teamManagementSum = tableData.stream()
                .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                .sum();
            double avgTeamManagement = teamManagementSum / tableData.size();
            
            summary.put("平均定权值", String.format("%.8f", avgTeamManagement));
            summary.put("最高定权值", String.format("%.8f", tableData.stream()
                .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                .max().orElse(0.0)));
            summary.put("最低定权值", String.format("%.8f", tableData.stream()
                .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                .min().orElse(0.0)));
        } else {
            summary.put("平均定权值", "NaN");
            summary.put("最高定权值", "0");
            summary.put("最低定权值", "0");
        }
        
        return summary;
    }
    
    /**
     * 生成表格2的汇总信息
     */
    private Map<String, Object> generateTable2Summary(List<Map<String, Object>> tableData) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("数据条数", tableData.size());
        summary.put("表格类型", "乡镇减灾能力权重计算");
        summary.put("计算方法", "定权公式：属性向量归一化 × 一级权重 × 二级权重");
        
        if (!tableData.isEmpty()) {
            double teamManagementSum = tableData.stream()
                .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                .sum();
            double avgTeamManagement = teamManagementSum / tableData.size();
            
            summary.put("平均权重值", String.format("%.8f", avgTeamManagement));
            summary.put("最高权重值", String.format("%.8f", tableData.stream()
                .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                .max().orElse(0.0)));
            summary.put("最低权重值", String.format("%.8f", tableData.stream()
                .mapToDouble(row -> Double.parseDouble(row.get("teamManagement").toString()))
                .min().orElse(0.0)));
        } else {
            summary.put("平均权重值", "NaN");
            summary.put("最高权重值", "0");
            summary.put("最低权重值", "0");
        }
        
        return summary;
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
                } else if (stepIndex == 3) { // 步骤4：优劣解算法计算（显示4列：3个一级指标+1列综合减灾能力）
                    // 获取所有地区的定权结果（步骤3表2的数据）
                    Map<String, Map<String, Double>> allWeightedValues = collectAllWeightedValues(regionIds);
                    
                    // 计算当前地区的定权值
                    Map<String, Double> currentWeightedValues = calculateCurrentWeightedValues(surveyData, regionIds);
                    
                    // 使用TOPSIS算法计算3个一级指标
                    Map<String, Double> topsisResults = calculateTOPSIS(currentWeightedValues, allWeightedValues);
                    
                    // 使用TOPSIS算法计算综合减灾能力值
                    double comprehensiveCapability = calculateComprehensiveTOPSIS(currentWeightedValues, allWeightedValues);
                    
                    // 将步骤4的结果缓存起来，供步骤5使用
                    Map<String, Double> step4Data = new HashMap<>(topsisResults);
                    step4Data.put("comprehensiveCapability", comprehensiveCapability);
                    step4ResultsCache.put(regionName, step4Data);
                    
                    // 设置4列：3个一级指标 + 1列综合减灾能力
                    row.put("disasterManagement", String.format("%.8f", topsisResults.get("disasterManagement")));
                    row.put("disasterPreparedness", String.format("%.8f", topsisResults.get("disasterPreparedness")));
                    row.put("selfRescueTransfer", String.format("%.8f", topsisResults.get("selfRescueTransfer")));
                    row.put("comprehensiveCapability", String.format("%.8f", comprehensiveCapability));
                } else if (stepIndex == 4) { // 能力分级计算 - 直接使用步骤4的缓存数据进行分级
                    // 从缓存中获取步骤4的原始数据（避免重新计算）
                    Map<String, Double> step4Data = step4ResultsCache.get(regionName);
                    
                    // 如果缓存中没有数据，则先执行步骤4的计算
                    if (step4Data == null) {
                        // 获取所有地区的定权结果（步骤3表2的数据）
                        Map<String, Map<String, Double>> allWeightedValues = collectAllWeightedValues(regionIds);
                        
                        // 计算当前地区的定权值
                        Map<String, Double> currentWeightedValues = calculateCurrentWeightedValues(surveyData, regionIds);
                        
                        // 使用TOPSIS算法计算3个一级指标
                        Map<String, Double> topsisResults = calculateTOPSIS(currentWeightedValues, allWeightedValues);
                        
                        // 使用TOPSIS算法计算综合减灾能力值
                        double comprehensiveCapability = calculateComprehensiveTOPSIS(currentWeightedValues, allWeightedValues);
                        
                        // 将步骤4的结果缓存起来，供步骤5使用
                        step4Data = new HashMap<>(topsisResults);
                        step4Data.put("comprehensiveCapability", comprehensiveCapability);
                        step4ResultsCache.put(regionName, step4Data);
                    }
                    
                    // 获取所有地区的步骤4数据用于分级计算（从缓存中获取）
                    Map<String, List<Double>> allStep4Values = collectAllStep4ValuesFromCache(regionIds);
                    
                    // 基于步骤4的数据进行分级（不修改原始数值）
                    Map<String, String> grades = calculateCapabilityGrades(step4Data, allStep4Values);
                    
                    // 计算综合减灾能力分级（基于步骤4的综合减灾能力值）
                    double comprehensiveCapability = step4Data.get("comprehensiveCapability");
                    String comprehensiveGrade = calculateComprehensiveGrade(comprehensiveCapability, allStep4Values.get("comprehensiveCapability"));
                    
                    // 设置4列数据：保持步骤4的原始数值 + 添加分级信息
                    row.put("disasterManagement", String.format("%.8f", step4Data.get("disasterManagement")));
                    row.put("disasterPreparedness", String.format("%.8f", step4Data.get("disasterPreparedness")));
                    row.put("selfRescueTransfer", String.format("%.8f", step4Data.get("selfRescueTransfer")));
                    row.put("comprehensiveCapability", String.format("%.8f", comprehensiveCapability));
                    
                    // 添加分级列（如果需要在前端显示）
                    row.put("disasterManagementGrade", grades.get("disasterManagement"));
                    row.put("disasterPreparednessGrade", grades.get("disasterPreparedness"));
                    row.put("selfRescueTransferGrade", grades.get("selfRescueTransfer"));
                    row.put("comprehensiveCapabilityGrade", comprehensiveGrade);
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
                    if (stepIndex == 1) {
                        // 步骤2：属性向量归一化 - 8个指标默认值
                        row.put("teamManagement", "0.00000000");
                        row.put("riskAssessment", "0.00000000");
                        row.put("financialInput", "0.00000000");
                        row.put("materialReserve", "0.00000000");
                        row.put("medicalSupport", "0.00000000");
                        row.put("selfRescue", "0.00000000");
                        row.put("publicAvoidance", "0.00000000");
                        row.put("relocationCapacity", "0.00000000");
                    } else if (stepIndex == 2) {
                        // 步骤3：二级指标定权 - 8个指标默认值
                        row.put("teamManagement", "0.00000000");
                        row.put("riskAssessment", "0.00000000");
                        row.put("financialInput", "0.00000000");
                        row.put("materialReserve", "0.00000000");
                        row.put("medicalSupport", "0.00000000");
                        row.put("selfRescue", "0.00000000");
                        row.put("publicAvoidance", "0.00000000");
                        row.put("relocationCapacity", "0.00000000");
                    } else if (stepIndex == 3) {
                        // 步骤4：优劣解算法 - 4列默认值（3个一级指标 + 1列综合减灾能力）
                        row.put("disasterManagement", "0.00000000");
                        row.put("disasterPreparedness", "0.00000000");
                        row.put("selfRescueTransfer", "0.00000000");
                        row.put("comprehensiveCapability", "0.00000000");
                    } else if (stepIndex == 4) {
                        // 步骤5：能力分级计算 - 4列原始数值默认值（与步骤4相同）+ 分级信息
                        row.put("disasterManagement", "0.00000000");
                        row.put("disasterPreparedness", "0.00000000");
                        row.put("selfRescueTransfer", "0.00000000");
                        row.put("comprehensiveCapability", "0.00000000");
                        row.put("disasterManagementGrade", "中等");
                        row.put("disasterPreparednessGrade", "中等");
                        row.put("selfRescueTransferGrade", "中等");
                        row.put("comprehensiveCapabilityGrade", "中等");
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
     * 获取步骤4的原始数据（不重新计算）
     */
    private Map<String, Double> getStep4DataForRegion(SurveyData surveyData, List<String> regionIds) {
        // 获取所有地区的定权结果（步骤3表2的数据）
        Map<String, Map<String, Double>> allWeightedValues = collectAllWeightedValues(regionIds);
        
        // 计算当前地区的定权值
        Map<String, Double> currentWeightedValues = calculateCurrentWeightedValues(surveyData, regionIds);
        
        // 使用TOPSIS算法计算3个一级指标
        Map<String, Double> topsisResults = calculateTOPSIS(currentWeightedValues, allWeightedValues);
        
        // 使用TOPSIS算法计算综合减灾能力值
        double comprehensiveCapability = calculateComprehensiveTOPSIS(currentWeightedValues, allWeightedValues);
        
        // 返回步骤4的完整数据
        Map<String, Double> step4Data = new HashMap<>(topsisResults);
        step4Data.put("comprehensiveCapability", comprehensiveCapability);
        
        return step4Data;
    }
    
    /**
     * 收集所有地区的步骤4数据
     */
    private Map<String, List<Double>> collectAllStep4Values(List<String> regionIds) {
        Map<String, List<Double>> allValues = new HashMap<>();
        allValues.put("disasterManagement", new ArrayList<>());
        allValues.put("disasterPreparedness", new ArrayList<>());
        allValues.put("selfRescueTransfer", new ArrayList<>());
        allValues.put("comprehensiveCapability", new ArrayList<>());
        
        for (String regionId : regionIds) {
            String regionName = extractRegionNameFromId(regionId);
            List<SurveyData> surveyDataList = surveyDataService.getBySurveyRegion(regionName);
            
            if (!surveyDataList.isEmpty()) {
                SurveyData surveyData = surveyDataList.get(0);
                
                // 获取当前地区的步骤4数据
                Map<String, Double> step4Data = getStep4DataForRegion(surveyData, regionIds);
                
                // 添加到对应的列表中
                allValues.get("disasterManagement").add(step4Data.get("disasterManagement"));
                allValues.get("disasterPreparedness").add(step4Data.get("disasterPreparedness"));
                allValues.get("selfRescueTransfer").add(step4Data.get("selfRescueTransfer"));
                allValues.get("comprehensiveCapability").add(step4Data.get("comprehensiveCapability"));
            } else {
                // 如果没有数据，添加0值
                allValues.get("disasterManagement").add(0.0);
                allValues.get("disasterPreparedness").add(0.0);
                allValues.get("selfRescueTransfer").add(0.0);
                allValues.get("comprehensiveCapability").add(0.0);
            }
        }
        
        return allValues;
    }
    
    /**
     * 从缓存中收集所有地区的步骤4数据（避免重新计算）
     */
    private Map<String, List<Double>> collectAllStep4ValuesFromCache(List<String> regionIds) {
        Map<String, List<Double>> allValues = new HashMap<>();
        allValues.put("disasterManagement", new ArrayList<>());
        allValues.put("disasterPreparedness", new ArrayList<>());
        allValues.put("selfRescueTransfer", new ArrayList<>());
        allValues.put("comprehensiveCapability", new ArrayList<>());
        
        for (String regionId : regionIds) {
            String regionName = extractRegionNameFromId(regionId);
            
            // 从缓存中获取步骤4数据
            Map<String, Double> step4Data = step4ResultsCache.get(regionName);
            
            if (step4Data != null) {
                // 添加到对应的列表中
                allValues.get("disasterManagement").add(step4Data.get("disasterManagement"));
                allValues.get("disasterPreparedness").add(step4Data.get("disasterPreparedness"));
                allValues.get("selfRescueTransfer").add(step4Data.get("selfRescueTransfer"));
                allValues.get("comprehensiveCapability").add(step4Data.get("comprehensiveCapability"));
            } else {
                // 如果缓存中没有数据，添加0值
                allValues.get("disasterManagement").add(0.0);
                allValues.get("disasterPreparedness").add(0.0);
                allValues.get("selfRescueTransfer").add(0.0);
                allValues.get("comprehensiveCapability").add(0.0);
            }
        }
        
        return allValues;
    }
    
    /**
     * 计算综合减灾能力分级
     */
    private String calculateComprehensiveGrade(double value, List<Double> allValues) {
        if (allValues == null || allValues.isEmpty()) {
            return "中等";
        }
        
        // 过滤掉null值
        List<Double> validValues = allValues.stream()
            .filter(v -> v != null)
            .collect(Collectors.toList());
            
        if (validValues.isEmpty()) {
            return "中等";
        }
        
        // 计算均值和样本标准差（STDEV.S）
        double mean = validValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (validValues.size() <= 1) {
            // 样本数量不足，无法计算样本标准差
            return "中等";
        }
        // 样本标准差：除以(n-1)
        double variance = validValues.stream().mapToDouble(v -> Math.pow(v - mean, 2)).sum() / (validValues.size() - 1);
        double stdDev = Math.sqrt(variance);
        
        // 根据复杂的IF条件进行分级
        return calculateGrade(value, mean, stdDev);
    }
    
    /**
     * 计算能力分级
     */
    private Map<String, String> calculateCapabilityGrades(Map<String, Double> currentValues, Map<String, List<Double>> allValues) {
        Map<String, String> grades = new HashMap<>();
        
        // 为每个一级指标计算分级
        for (String indicator : Arrays.asList("disasterManagement", "disasterPreparedness", "selfRescueTransfer")) {
            Double currentValueObj = currentValues.get(indicator);
            double currentValue = currentValueObj != null ? currentValueObj : 0.0;
            List<Double> values = allValues.get(indicator);
            
            if (values == null || values.isEmpty()) {
                grades.put(indicator, "中等");
                continue;
            }
            
            // 过滤掉null值
            List<Double> validValues = values.stream()
                .filter(v -> v != null)
                .collect(Collectors.toList());
                
            if (validValues.size() <= 1) {
                // 样本数量不足，无法计算样本标准差
                grades.put(indicator, "中等");
                continue;
            }
            
            // 计算均值和样本标准差（STDEV.S）
            double mean = validValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            // 样本标准差：除以(n-1)
            double variance = validValues.stream().mapToDouble(v -> Math.pow(v - mean, 2)).sum() / (validValues.size() - 1);
            double stdDev = Math.sqrt(variance);
            
            // 根据复杂的IF条件进行分级
            String grade = calculateGrade(currentValue, mean, stdDev);
            grades.put(indicator, grade);
        }
        
        return grades;
    }
    
    /**
     * 根据均值和标准差计算分级
     * 严格按照用户提供的Excel公式实现：
     * IF(均值μ<=0.5*标准差σ,IF(BI3>=均值μ+1.5*标准差σ,"强",IF(BI3>=均值μ+0.5*标准差σ,"较强","中等")),
     * IF(均值μ<=1.5*标准差σ,IF(BI3>=均值μ+1.5*标准差σ,"强",IF(BI3>=均值μ+0.5*标准差σ,"较强",IF(BI3>=均值μ-0.5*标准差σ,"中等","较弱"))),
     * IF(BI3>=均值μ+1.5*标准差σ,"强",IF(BI3>=均值μ+0.5*标准差σ,"较强",IF(BI3>=均值μ-0.5*标准差σ,"中等",IF(BI3>=均值μ-1.5*标准差σ,"较弱","弱"))))))
     */
    private String calculateGrade(double value, double mean, double stdDev) {
        // 第一层IF：判断均值μ是否<=0.5*标准差σ
        if (mean <= 0.5 * stdDev) {
            // 均值μ<=0.5*标准差σ的情况
            if (value >= mean + 1.5 * stdDev) {
                return "强";
            } else if (value >= mean + 0.5 * stdDev) {
                return "较强";
            } else {
                return "中等";
            }
        } else {
            // 均值μ>0.5*标准差σ的情况，进入第二层IF：判断均值μ是否<=1.5*标准差σ
            if (mean <= 1.5 * stdDev) {
                // 均值μ<=1.5*标准差σ的情况
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
                // 均值μ>1.5*标准差σ的情况
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
    }
    
    /**
     * 获取一级指标权重配置
     */
    private Map<String, Double> getPrimaryIndicatorWeights(Long configId) {
        Map<String, Double> weights = new HashMap<>();
        
        try {
            // 获取一级指标权重配置
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.getByConfigIdAndLevel(configId, 1);
            
            log.info("getPrimaryIndicatorWeights - 从数据库获取到 {} 条一级权重配置记录", indicatorWeights.size());
            
            // 将指标权重映射到对应的字段名
            for (IndicatorWeight weight : indicatorWeights) {
                String indicatorCode = weight.getIndicatorCode();
                Double weightValue = weight.getWeight();
                
                log.info("getPrimaryIndicatorWeights - 处理权重配置: indicatorCode={}, weightValue={}", indicatorCode, weightValue);
                
                // 根据指标代码映射到对应的字段名
                switch (indicatorCode) {
                    case "L1_DISASTER_MANAGEMENT":
                        weights.put("disasterManagement", weightValue);
                        log.info("getPrimaryIndicatorWeights - 映射 L1_DISASTER_MANAGEMENT -> disasterManagement: {}", weightValue);
                        break;
                    case "L1_DISASTER_PREPAREDNESS":
                        weights.put("disasterPreparedness", weightValue);
                        log.info("getPrimaryIndicatorWeights - 映射 L1_DISASTER_PREPAREDNESS -> disasterPreparedness: {}", weightValue);
                        break;
                    case "L1_SELF_RESCUE_TRANSFER":
                        weights.put("selfRescueTransfer", weightValue);
                        log.info("getPrimaryIndicatorWeights - 映射 L1_SELF_RESCUE_TRANSFER -> selfRescueTransfer: {}", weightValue);
                        break;
                    default:
                        log.warn("getPrimaryIndicatorWeights - 未识别的一级指标代码: {}", indicatorCode);
                        break;
                }
            }
        } catch (Exception e) {
            log.warn("获取一级指标权重配置失败，使用默认权重: {}", e.getMessage());
        }
        
        // 如果没有获取到权重配置，使用默认权重
        if (weights.isEmpty()) {
            log.warn("getPrimaryIndicatorWeights - 未获取到任何一级权重配置，使用默认权重");
            weights.put("disasterManagement", 0.33);
            weights.put("disasterPreparedness", 0.32);
            weights.put("selfRescueTransfer", 0.35);
        }
        
        log.info("getPrimaryIndicatorWeights - 最终一级权重配置: {}", weights);
        return weights;
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
        } else if (stepIndex == 2) { // 二级指标定权 - 8个指标定权值（分为两个表格）
            columns.add(createColumn("teamManagement", "队伍管理能力", 120));
            columns.add(createColumn("riskAssessment", "风险评估能力", 120));
            columns.add(createColumn("financialInput", "财政投入能力", 120));
            columns.add(createColumn("materialReserve", "物资储备能力", 120));
            columns.add(createColumn("medicalSupport", "医疗保障能力", 120));
            columns.add(createColumn("selfRescue", "自救互救能力", 120));
            columns.add(createColumn("publicAvoidance", "公众避险能力", 120));
            columns.add(createColumn("relocationCapacity", "转移安置能力", 120));
        } else if (stepIndex == 3) { // 步骤4：优劣解算法 - 显示4列（3个一级指标 + 1列综合减灾能力）
            columns.add(createColumn("disasterManagement", "灾害管理能力", 150));
            columns.add(createColumn("disasterPreparedness", "灾害备灾能力", 150));
            columns.add(createColumn("selfRescueTransfer", "自救转移能力", 150));
            columns.add(createColumn("comprehensiveCapability", "乡镇（街道）减灾能力", 200));
        } else if (stepIndex == 4) { // 能力分级计算 - 显示8列（4列原始值+4列分级）
            // 前4列：原始数值
            columns.add(createColumn("disasterManagement", "灾害管理能力值", 130));
            columns.add(createColumn("disasterPreparedness", "灾害备灾能力值", 130));
            columns.add(createColumn("selfRescueTransfer", "自救转移能力值", 130));
            columns.add(createColumn("comprehensiveCapability", "综合减灾能力值", 130));
            // 后4列：分级数据
            columns.add(createColumn("disasterManagementGrade", "灾害管理分级", 120));
            columns.add(createColumn("disasterPreparednessGrade", "灾害备灾分级", 120));
            columns.add(createColumn("selfRescueTransferGrade", "自救转移分级", 120));
            columns.add(createColumn("comprehensiveCapabilityGrade", "综合能力分级", 120));
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
     * 生成步骤汇总信息 - 已移除所有统计信息
     */
    private Map<String, Object> generateStepSummary(List<Map<String, Object>> tableData, Integer stepIndex) {
        // 完全移除统计信息，返回null
        return null;
    }
    
    /**
     * 收集所有地区的定权结果
     */
    private Map<String, Map<String, Double>> collectAllWeightedValues(List<String> regionIds) {
        Map<String, Map<String, Double>> allWeightedValues = new HashMap<>();
        
        // 获取所有地区的归一化值
        Map<String, List<Double>> allIndicatorValues = collectAllIndicatorValues(regionIds);
        
        // 获取一级权重配置和二级权重配置
        Map<String, Double> primaryWeights = getPrimaryIndicatorWeights(1L);
        Map<String, Double> secondaryWeights = getSecondaryIndicatorWeights(1L);
        
        // 计算所有地区的定权值
        
        for (int i = 0; i < regionIds.size(); i++) {
            String regionId = regionIds.get(i);
            String regionName = extractRegionNameFromId(regionId);
            
            Map<String, Double> weightedValues = new HashMap<>();
            
            // 根据用户提供的正确公式计算定权值：属性向量归一化值 × 对应一级权重 × 对应二级权重
            
            // 获取当前地区的8个指标原始值
            List<Double> teamManagementValues = allIndicatorValues.get("teamManagement");
            List<Double> riskAssessmentValues = allIndicatorValues.get("riskAssessment");
            List<Double> financialInputValues = allIndicatorValues.get("financialInput");
            List<Double> materialReserveValues = allIndicatorValues.get("materialReserve");
            List<Double> medicalSupportValues = allIndicatorValues.get("medicalSupport");
            List<Double> selfRescueValues = allIndicatorValues.get("selfRescue");
            List<Double> publicAvoidanceValues = allIndicatorValues.get("publicAvoidance");
            List<Double> relocationCapacityValues = allIndicatorValues.get("relocationCapacity");
            
            if (i < teamManagementValues.size()) {
                // 1. 队伍管理能力(定权) = 队伍管理能力(归一化) × 灾害管理能力一级权重 × 队伍管理能力二级权重
                double teamManagementNorm = normalizeIndicatorValue(teamManagementValues.get(i), teamManagementValues);
                double teamManagementPrimaryWeight = primaryWeights.getOrDefault("disasterManagement", 0.33);
                double teamManagementSecondaryWeight = secondaryWeights.getOrDefault("teamManagement", 0.37);
                double teamManagementWeighted = teamManagementNorm * teamManagementPrimaryWeight * teamManagementSecondaryWeight;
                weightedValues.put("teamManagement", teamManagementWeighted);
                
                // 2. 风险评估能力(定权) = 风险评估能力(归一化) × 灾害管理能力一级权重 × 风险评估能力二级权重
                double riskAssessmentNorm = normalizeIndicatorValue(riskAssessmentValues.get(i), riskAssessmentValues);
                double riskAssessmentPrimaryWeight = primaryWeights.getOrDefault("disasterManagement", 0.33);
                double riskAssessmentSecondaryWeight = secondaryWeights.getOrDefault("riskAssessment", 0.31);
                double riskAssessmentWeighted = riskAssessmentNorm * riskAssessmentPrimaryWeight * riskAssessmentSecondaryWeight;
                weightedValues.put("riskAssessment", riskAssessmentWeighted);
                
                // 3. 财政投入能力(定权) = 财政投入能力(归一化) × 灾害管理能力一级权重 × 财政投入能力二级权重
                double financialInputNorm = normalizeIndicatorValue(financialInputValues.get(i), financialInputValues);
                double financialInputPrimaryWeight = primaryWeights.getOrDefault("disasterManagement", 0.33);
                double financialInputSecondaryWeight = secondaryWeights.getOrDefault("financialInput", 0.32);
                double financialInputWeighted = financialInputNorm * financialInputPrimaryWeight * financialInputSecondaryWeight;
                weightedValues.put("financialInput", financialInputWeighted);
                
                // 4. 物资储备能力(定权) = 物资储备能力(归一化) × 灾害备灾能力一级权重 × 物资储备能力二级权重
                double materialReserveNorm = normalizeIndicatorValue(materialReserveValues.get(i), materialReserveValues);
                double materialReservePrimaryWeight = primaryWeights.getOrDefault("disasterPreparedness", 0.32);
                double materialReserveSecondaryWeight = secondaryWeights.getOrDefault("materialReserve", 0.51);
                double materialReserveWeighted = materialReserveNorm * materialReservePrimaryWeight * materialReserveSecondaryWeight;
                weightedValues.put("materialReserve", materialReserveWeighted);
                
                // 物资储备能力定权计算完成
                
                // 5. 医疗保障能力(定权) = 医疗保障能力(归一化) × 灾害备灾能力一级权重 × 医疗保障能力二级权重
                double medicalSupportNorm = normalizeIndicatorValue(medicalSupportValues.get(i), medicalSupportValues);
                double medicalSupportPrimaryWeight = primaryWeights.getOrDefault("disasterPreparedness", 0.32);
                double medicalSupportSecondaryWeight = secondaryWeights.getOrDefault("medicalSupport", 0.49);
                double medicalSupportWeighted = medicalSupportNorm * medicalSupportPrimaryWeight * medicalSupportSecondaryWeight;
                weightedValues.put("medicalSupport", medicalSupportWeighted);
                
                // 6. 自救互救能力(定权) = 自救互救能力(归一化) × 自救转移能力一级权重 × 自救互救能力二级权重
                double selfRescueNorm = normalizeIndicatorValue(selfRescueValues.get(i), selfRescueValues);
                double selfRescuePrimaryWeight = primaryWeights.getOrDefault("selfRescueTransfer", 0.35);
                double selfRescueSecondaryWeight = secondaryWeights.getOrDefault("selfRescue", 0.33);
                double selfRescueWeighted = selfRescueNorm * selfRescuePrimaryWeight * selfRescueSecondaryWeight;
                weightedValues.put("selfRescue", selfRescueWeighted);
                
                // 7. 公众避险能力(定权) = 公众避险能力(归一化) × 自救转移能力一级权重 × 公众避险能力二级权重
                double publicAvoidanceNorm = normalizeIndicatorValue(publicAvoidanceValues.get(i), publicAvoidanceValues);
                double publicAvoidancePrimaryWeight = primaryWeights.getOrDefault("selfRescueTransfer", 0.35);
                double publicAvoidanceSecondaryWeight = secondaryWeights.getOrDefault("publicAvoidance", 0.33);
                double publicAvoidanceWeighted = publicAvoidanceNorm * publicAvoidancePrimaryWeight * publicAvoidanceSecondaryWeight;
                weightedValues.put("publicAvoidance", publicAvoidanceWeighted);
                
                // 8. 转移安置能力(定权) = 转移安置能力(归一化) × 自救转移能力一级权重 × 转移安置能力二级权重
                double relocationCapacityNorm = normalizeIndicatorValue(relocationCapacityValues.get(i), relocationCapacityValues);
                double relocationCapacityPrimaryWeight = primaryWeights.getOrDefault("selfRescueTransfer", 0.35);
                double relocationCapacitySecondaryWeight = secondaryWeights.getOrDefault("relocationCapacity", 0.34);
                double relocationCapacityWeighted = relocationCapacityNorm * relocationCapacityPrimaryWeight * relocationCapacitySecondaryWeight;
                weightedValues.put("relocationCapacity", relocationCapacityWeighted);
            } else {
                // 如果没有数据，设置默认值
                weightedValues.put("teamManagement", 0.0);
                weightedValues.put("riskAssessment", 0.0);
                weightedValues.put("financialInput", 0.0);
                weightedValues.put("materialReserve", 0.0);
                weightedValues.put("medicalSupport", 0.0);
                weightedValues.put("selfRescue", 0.0);
                weightedValues.put("publicAvoidance", 0.0);
                weightedValues.put("relocationCapacity", 0.0);
            }
            
            allWeightedValues.put(regionName, weightedValues);
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
        
        // 获取一级权重配置和二级权重配置
        Map<String, Double> primaryWeights = getPrimaryIndicatorWeights(1L);
        Map<String, Double> secondaryWeights = getSecondaryIndicatorWeights(1L);
        
        // 计算当前地区定权值
        
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
        
        // 当前地区原始指标值计算完成
        
        // 根据用户提供的正确公式计算定权值：属性向量归一化值 × 对应一级权重 × 对应二级权重
        
        // 1. 队伍管理能力(定权) = 队伍管理能力(归一化) × 灾害管理能力一级权重 × 队伍管理能力二级权重
        double teamManagementNorm = normalizeIndicatorValue(teamManagement, allIndicatorValues.get("teamManagement"));
        double teamManagementPrimaryWeight = primaryWeights.getOrDefault("disasterManagement", 0.33);
        double teamManagementSecondaryWeight = secondaryWeights.getOrDefault("teamManagement", 0.37);
        double teamManagementWeighted = teamManagementNorm * teamManagementPrimaryWeight * teamManagementSecondaryWeight;
        currentWeightedValues.put("teamManagement", teamManagementWeighted);
        
        // 2. 风险评估能力(定权) = 风险评估能力(归一化) × 灾害管理能力一级权重 × 风险评估能力二级权重
        double riskAssessmentNorm = normalizeIndicatorValue(riskAssessment, allIndicatorValues.get("riskAssessment"));
        double riskAssessmentPrimaryWeight = primaryWeights.getOrDefault("disasterManagement", 0.33);
        double riskAssessmentSecondaryWeight = secondaryWeights.getOrDefault("riskAssessment", 0.31);
        double riskAssessmentWeighted = riskAssessmentNorm * riskAssessmentPrimaryWeight * riskAssessmentSecondaryWeight;
        currentWeightedValues.put("riskAssessment", riskAssessmentWeighted);
        
        // 3. 财政投入能力(定权) = 财政投入能力(归一化) × 灾害管理能力一级权重 × 财政投入能力二级权重
        double financialInputNorm = normalizeIndicatorValue(financialInput, allIndicatorValues.get("financialInput"));
        double financialInputPrimaryWeight = primaryWeights.getOrDefault("disasterManagement", 0.33);
        double financialInputSecondaryWeight = secondaryWeights.getOrDefault("financialInput", 0.32);
        double financialInputWeighted = financialInputNorm * financialInputPrimaryWeight * financialInputSecondaryWeight;
        currentWeightedValues.put("financialInput", financialInputWeighted);
        
        // 4. 物资储备能力(定权) = 物资储备能力(归一化) × 灾害备灾能力一级权重 × 物资储备能力二级权重
        double materialReserveNorm = normalizeIndicatorValue(materialReserve, allIndicatorValues.get("materialReserve"));
        double materialReservePrimaryWeight = primaryWeights.getOrDefault("disasterPreparedness", 0.32);
        double materialReserveSecondaryWeight = secondaryWeights.getOrDefault("materialReserve", 0.51);
        double materialReserveWeighted = materialReserveNorm * materialReservePrimaryWeight * materialReserveSecondaryWeight;
        currentWeightedValues.put("materialReserve", materialReserveWeighted);
        
        // 5. 医疗保障能力(定权) = 医疗保障能力(归一化) × 灾害备灾能力一级权重 × 医疗保障能力二级权重
        double medicalSupportNorm = normalizeIndicatorValue(medicalSupport, allIndicatorValues.get("medicalSupport"));
        double medicalSupportPrimaryWeight = primaryWeights.getOrDefault("disasterPreparedness", 0.32);
        double medicalSupportSecondaryWeight = secondaryWeights.getOrDefault("medicalSupport", 0.49);
        double medicalSupportWeighted = medicalSupportNorm * medicalSupportPrimaryWeight * medicalSupportSecondaryWeight;
        currentWeightedValues.put("medicalSupport", medicalSupportWeighted);
        
        // 6. 自救互救能力(定权) = 自救互救能力(归一化) × 自救转移能力一级权重 × 自救互救能力二级权重
        double selfRescueNorm = normalizeIndicatorValue(selfRescue, allIndicatorValues.get("selfRescue"));
        double selfRescuePrimaryWeight = primaryWeights.getOrDefault("selfRescueTransfer", 0.35);
        double selfRescueSecondaryWeight = secondaryWeights.getOrDefault("selfRescue", 0.33);
        double selfRescueWeighted = selfRescueNorm * selfRescuePrimaryWeight * selfRescueSecondaryWeight;
        currentWeightedValues.put("selfRescue", selfRescueWeighted);
        
        // 7. 公众避险能力(定权) = 公众避险能力(归一化) × 自救转移能力一级权重 × 公众避险能力二级权重
        double publicAvoidanceNorm = normalizeIndicatorValue(publicAvoidance, allIndicatorValues.get("publicAvoidance"));
        double publicAvoidancePrimaryWeight = primaryWeights.getOrDefault("selfRescueTransfer", 0.35);
        double publicAvoidanceSecondaryWeight = secondaryWeights.getOrDefault("publicAvoidance", 0.33);
        double publicAvoidanceWeighted = publicAvoidanceNorm * publicAvoidancePrimaryWeight * publicAvoidanceSecondaryWeight;
        currentWeightedValues.put("publicAvoidance", publicAvoidanceWeighted);
        
        // 8. 转移安置能力(定权) = 转移安置能力(归一化) × 自救转移能力一级权重 × 转移安置能力二级权重
        double relocationCapacityNorm = normalizeIndicatorValue(relocationCapacity, allIndicatorValues.get("relocationCapacity"));
        double relocationCapacityPrimaryWeight = primaryWeights.getOrDefault("selfRescueTransfer", 0.35);
        double relocationCapacitySecondaryWeight = secondaryWeights.getOrDefault("relocationCapacity", 0.34);
        double relocationCapacityWeighted = relocationCapacityNorm * relocationCapacityPrimaryWeight * relocationCapacitySecondaryWeight;
        currentWeightedValues.put("relocationCapacity", relocationCapacityWeighted);
        
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
        
        // TOPSIS算法计算（移除调试日志）
        
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
        
        // 2. 灾害备灾能力（物资储备+医疗保障）
        double materialReserveCurrent = currentWeightedValues.get("materialReserve");
        double medicalSupportCurrent = currentWeightedValues.get("medicalSupport");
        double materialReserveMax = maxValues.get("materialReserve");
        double medicalSupportMax = maxValues.get("medicalSupport");
        double materialReserveMin = minValues.get("materialReserve");
        double medicalSupportMin = minValues.get("medicalSupport");
        
        double disasterPreparednessPositive = Math.sqrt(
            Math.pow(materialReserveMax - materialReserveCurrent, 2) +
            Math.pow(medicalSupportMax - medicalSupportCurrent, 2)
        );
        double disasterPreparednessNegative = Math.sqrt(
            Math.pow(materialReserveMin - materialReserveCurrent, 2) +
            Math.pow(medicalSupportMin - medicalSupportCurrent, 2)
        );
        
        // 计算灾害备灾能力的TOPSIS值
        
        // 防止除零错误
        double disasterPreparedness = 0.0;
        if (disasterPreparednessNegative + disasterPreparednessPositive > 0) {
            disasterPreparedness = disasterPreparednessNegative / (disasterPreparednessNegative + disasterPreparednessPositive);
        }
        
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
        
        primaryIndicators.put("disasterManagement", disasterManagement);
        primaryIndicators.put("disasterPreparedness", disasterPreparedness);
        primaryIndicators.put("selfRescueTransfer", selfRescueTransfer);
        
        return primaryIndicators;
    }
    
    /**
     * 使用TOPSIS算法计算综合减灾能力值（单一值）
     * 根据用户要求的计算逻辑：
     * 1. 计算乡镇（街道）减灾能力优（与最大值的欧氏距离）
     * 2. 计算乡镇（街道）减灾能力劣（与最小值的欧氏距离）
     * 3. 计算综合减灾能力值 = 劣距离 / (劣距离 + 优距离)
     */
    private double calculateComprehensiveTOPSIS(Map<String, Double> currentWeightedValues, 
                                               Map<String, Map<String, Double>> allWeightedValues) {
        
        // 计算各指标的最大值和最小值
        Map<String, Double> maxValues = new HashMap<>();
        Map<String, Double> minValues = new HashMap<>();
        
        String[] indicators = {"teamManagement", "riskAssessment", "financialInput", 
                              "materialReserve", "medicalSupport", "selfRescue", 
                              "publicAvoidance", "relocationCapacity"};
        
        for (String indicator : indicators) {
            double max = allWeightedValues.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .max().orElse(0.0);
            double min = allWeightedValues.values().stream()
                .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
                .min().orElse(0.0);
            
            maxValues.put(indicator, max);
            minValues.put(indicator, min);
        }
        
        // 综合TOPSIS算法计算（移除调试日志）
        
        // 1. 计算乡镇（街道）减灾能力优（与最大值的欧氏距离）
        double positiveDistance = 0.0;
        for (String indicator : indicators) {
            double currentValue = currentWeightedValues.getOrDefault(indicator, 0.0);
            double maxValue = maxValues.get(indicator);
            positiveDistance += Math.pow(maxValue - currentValue, 2);
        }
        positiveDistance = Math.sqrt(positiveDistance);
        
        // 2. 计算乡镇（街道）减灾能力劣（与最小值的欧氏距离）
        double negativeDistance = 0.0;
        for (String indicator : indicators) {
            double currentValue = currentWeightedValues.getOrDefault(indicator, 0.0);
            double minValue = minValues.get(indicator);
            negativeDistance += Math.pow(minValue - currentValue, 2);
        }
        negativeDistance = Math.sqrt(negativeDistance);
        
        // 3. 计算综合减灾能力值 = 劣距离 / (劣距离 + 优距离)
        double comprehensiveCapability = 0.0;
        if (negativeDistance + positiveDistance > 0) {
            comprehensiveCapability = negativeDistance / (negativeDistance + positiveDistance);
        }
        
        // 返回综合减灾能力值
        
        return comprehensiveCapability;
    }
}