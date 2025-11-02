package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.*;
import com.evaluate.mapper.*;
import com.evaluate.service.ModelExecutionService;
import com.evaluate.service.QLExpressService;
import com.evaluate.service.SpecialAlgorithmService;
import com.evaluate.service.ISurveyDataService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;

/**
 * 模型执行服务实现类
 * 负责按步骤执行QLExpress表达式并生成评估结果
 * 
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class ModelExecutionServiceImpl implements ModelExecutionService {

    @Autowired
    private EvaluationModelMapper evaluationModelMapper;

    @Autowired
    private ModelStepMapper modelStepMapper;

    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;

    @Autowired
    private SurveyDataMapper surveyDataMapper;

    @Autowired
    private CommunityDisasterReductionCapacityMapper communityDataMapper;

    @Autowired
    private IndicatorWeightMapper indicatorWeightMapper;

    @Autowired
    private QLExpressService qlExpressService;

    @Autowired
    private SpecialAlgorithmService specialAlgorithmService;

    @Autowired
    private ISurveyDataService surveyDataService;

    @Autowired
    private ModelExecutionRecordMapper modelExecutionRecordMapper;

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行评估模型
     * 
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 执行结果（包含每个步骤的输出）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId) {
        // 1. 验证模型是否存在且启用
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        if (model == null || model.getStatus() == 0) {
            throw new RuntimeException("评估模型不存在或已禁用");
        }

        // 2. 获取模型的所有步骤并按顺序排序
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("status", 1)
                .orderByAsc("step_order");
        List<ModelStep> steps = modelStepMapper.selectList(stepQuery);
        
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("该模型没有配置步骤");
        }

        // 3. 初始化全局上下文（存储所有步骤的执行结果）
        Map<String, Object> globalContext = new HashMap<>();
        globalContext.put("modelId", modelId);
        globalContext.put("modelName", model.getModelName());
        globalContext.put("regionCodes", regionCodes);
        globalContext.put("weightConfigId", weightConfigId);

        // 4. 加载基础数据到上下文
        loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

        // 5. 按顺序执行每个步骤
        Map<String, Object> stepResults = new HashMap<>();
        Map<Integer, Set<String>> stepOutputParams = new LinkedHashMap<>();  // 记录每个步骤的可能列名称
        List<String> currentRegionCodes = new ArrayList<>(regionCodes);  // 当前使用的地区代码列表
        
        for (ModelStep step : steps) {
            
            try {
                Map<String, Object> stepResult;
                
                // 特殊处理：如果是AGGREGATION类型且modelId=8，执行乡镇聚合
                if ("AGGREGATION".equals(step.getStepType()) && modelId == 8) {
                    stepResult = executeTownshipAggregation(step.getId(), currentRegionCodes, globalContext);
                    
                    // 更新regionCodes为乡镇代码列表（用于后续步骤）
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepResult.get("regionResults");
                    if (regionResults != null) {
                        currentRegionCodes = new ArrayList<>(regionResults.keySet());
                    }
                } else {
                    // 执行单个步骤
                    stepResult = executeStep(step.getId(), currentRegionCodes, globalContext);
                }
                
                stepResults.put(step.getStepCode(), stepResult);
                
                // 记录该步骤的输出参数（用于后面生成 columns）
                @SuppressWarnings("unchecked")
                Map<String, String> outputToAlgorithmName = 
                        (Map<String, String>) stepResult.get("outputToAlgorithmName");
                if (outputToAlgorithmName != null && !outputToAlgorithmName.isEmpty()) {
                    Set<String> columnNames = stepOutputParams.computeIfAbsent(
                            step.getStepOrder(), k -> new LinkedHashSet<>());
                    outputToAlgorithmName.forEach((outputParam, algorithmName) -> {
                        if (algorithmName != null && !algorithmName.trim().isEmpty()) {
                            columnNames.add(algorithmName.trim());
                        }
                        if (outputParam != null && !outputParam.trim().isEmpty() && !outputParam.startsWith("_")) {
                            String cleanedOutputParam = outputParam.trim();
                            columnNames.add(cleanedOutputParam);
                            if (step.getStepCode() != null && !step.getStepCode().trim().isEmpty()) {
                                columnNames.add(step.getStepCode().trim() + "_" + cleanedOutputParam);
                            }
                        }
                    });
                }
                
                // 将步骤结果合并到全局上下文（供后续步骤使用）
                globalContext.put("step_" + step.getStepCode(), stepResult);
                
            } catch (Exception e) {
                throw new RuntimeException("步骤 " + step.getStepName() + " 执行失败: " + e.getMessage(), e);
            }
        }

        // 生成二维表数据
        List<Map<String, Object>> tableData = generateResultTable(
                Collections.singletonMap("stepResults", stepResults));

        // 生成 columns 数组（包含所有步骤的 stepOrder 信息）
        List<Map<String, Object>> columns = generateColumnsWithAllSteps(tableData, stepOutputParams);

        // 构建有序的多步骤结果列表，便于前端逐步展示
        List<Map<String, Object>> orderedStepResults = new ArrayList<>();
        for (ModelStep step : steps) {
            Map<String, Object> rawStepResult = (Map<String, Object>) stepResults.get(step.getStepCode());
            if (rawStepResult == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> regionResults =
                    (Map<String, Map<String, Object>>) rawStepResult.get("regionResults");
            List<String> stepRegionCodes = new ArrayList<>();
            if (regionResults != null) {
                stepRegionCodes.addAll(regionResults.keySet());
            }

            List<Map<String, Object>> stepTableData = generateStepResultTable(rawStepResult, stepRegionCodes);
            List<Map<String, Object>> stepColumns = generateColumnsForStep(rawStepResult, stepTableData, step.getStepOrder());

            Map<String, Object> packagedStep = new LinkedHashMap<>();
            packagedStep.put("stepId", step.getId());
            packagedStep.put("stepName", step.getStepName());
            packagedStep.put("stepOrder", step.getStepOrder());
            packagedStep.put("stepCode", step.getStepCode());
            packagedStep.put("description", step.getDescription());
            packagedStep.put("regionResults", regionResults);
            packagedStep.put("tableData", stepTableData);
            packagedStep.put("columns", stepColumns);
            packagedStep.put("outputToAlgorithmName", rawStepResult.get("outputToAlgorithmName"));
            packagedStep.put("success", true);

            orderedStepResults.add(packagedStep);
        }

        // 6. 保存执行记录和评估结果
        Long executionRecordId = saveExecutionRecordAndResults(
                modelId,
                model.getModelName(),
                currentRegionCodes,
                weightConfigId,
                stepResults,
                tableData);

        // 7. 构建最终结果
        Map<String, Object> result = new HashMap<>();
        result.put("modelId", modelId);
        result.put("modelName", model.getModelName());
        result.put("executionTime", new Date());
        result.put("stepResults", stepResults);
        result.put("stepResultsList", orderedStepResults);
        result.put("isMultiStep", true);
        result.put("tableData", tableData);
        result.put("columns", columns);
        result.put("success", true);
        result.put("executionRecordId", executionRecordId);

        return result;
    }

    /**
     * 执行单个步骤
     * 
     * @param stepId 步骤ID
     * @param regionCodes 地区代码列表
     * @param inputData 输入数据（全局上下文）
     * @return 步骤执行结果
     */
    @Override
    public Map<String, Object> executeStep(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        // 1. 获取步骤信息
        ModelStep step = modelStepMapper.selectById(stepId);
        if (step == null || step.getStatus() == 0) {
            throw new RuntimeException("步骤不存在或已禁用");
        }

        // 2. 获取该步骤的所有算法并按顺序排序
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);

        if (algorithms == null || algorithms.isEmpty()) {
            return new HashMap<>();
        }

        // 3. 初始化步骤结果
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());
        stepResult.put("stepOrder", step.getStepOrder());

        // 4. 第一遍：为所有地区准备上下文数据
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();

        // 获取modelId以决定使用哪个数据源
        Long modelId = (Long) inputData.get("modelId");

        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(inputData);
            regionContext.put("currentRegionCode", regionCode);

            // 根据modelId选择不同的数据源
            if (modelId != null && (modelId == 4 || modelId == 8)) {
                // 社区模型(modelId=4)和社区-乡镇模型(modelId=8)：从community_disaster_reduction_capacity表加载数据
                // 使用selectMaps直接返回Map，key为数据库字段名，可直接匹配算法表达式中的变量名
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                if (communityDataList != null && !communityDataList.isEmpty()) {
                    Map<String, Object> communityDataMap = communityDataList.get(0);
                    // 直接将数据库字段添加到上下文，同时处理数值类型转换
                    addMapDataToContext(regionContext, communityDataMap);
                }
            } else {
                // 乡镇模型(modelId=3)：从survey_data表加载数据
                QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
                dataQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);

                if (surveyData != null) {
                    addSurveyDataToContext(regionContext, surveyData);
                }
            }

            // 再加载前面步骤的输出结果（计算结果），这样会覆盖原始数据中的同名字段
            loadPreviousStepOutputs(regionContext, regionCode, inputData);

            allRegionContexts.put(regionCode, regionContext);
        }
        
        // 5. 分离GRADE算法和非GRADE算法
        List<StepAlgorithm> nonGradeAlgorithms = new ArrayList<>();
        List<StepAlgorithm> gradeAlgorithms = new ArrayList<>();
        
        for (StepAlgorithm algorithm : algorithms) {
            String qlExpression = algorithm.getQlExpression();
            if (qlExpression != null && qlExpression.startsWith("@GRADE")) {
                gradeAlgorithms.add(algorithm);
            } else {
                nonGradeAlgorithms.add(algorithm);
            }
        }

        // 6. 第二遍：为每个地区执行非GRADE算法（支持特殊标记）
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> algorithmOutputs = new LinkedHashMap<>();

            // 执行非GRADE算法
            for (StepAlgorithm algorithm : nonGradeAlgorithms) {
                try {
                    Object result;
                    String qlExpression = algorithm.getQlExpression();
                    
                    // 检查是否是特殊标记
                    if (qlExpression != null && qlExpression.startsWith("@")) {
                        // 解析特殊标记: @MARKER:params
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";

                        // 调用特殊算法服务
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 确保数值类型转换并格式化为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        // 执行标准QLExpress表达式
                        result = qlExpressService.execute(qlExpression, regionContext);
                        
                        // 确保数值类型的结果转换为Double并格式化为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    }
                    
                    // 保存算法输出到上下文（供后续算法使用）
                    String outputParam = algorithm.getOutputParam();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        String cleanedOutputParam = outputParam.trim();
                        regionContext.put(cleanedOutputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 更新全局上下文
                        algorithmOutputs.put(cleanedOutputParam, result);
                        String algorithmName = algorithm.getAlgorithmName();
                        outputToAlgorithmName.put(cleanedOutputParam, algorithmName != null ? algorithmName.trim() : null);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("算法 " + algorithm.getAlgorithmName() + " 执行失败: " + e.getMessage(), e);
                }
            }
            
            regionResults.put(regionCode, algorithmOutputs);
        }
        
        // 7. 第三遍：为每个地区执行GRADE算法（此时所有地区的分数已计算完成）
        if (!gradeAlgorithms.isEmpty()) {
            for (String regionCode : regionCodes) {
                Map<String, Object> regionContext = allRegionContexts.get(regionCode);
                Map<String, Object> algorithmOutputs = regionResults.get(regionCode);

                for (StepAlgorithm algorithm : gradeAlgorithms) {
                    try {
                        String qlExpression = algorithm.getQlExpression();
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";

                        // 调用特殊算法服务
                        Object result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 格式化GRADE算法结果为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }

                        // 保存算法输出到上下文（供后续算法使用）
                        String outputParam = algorithm.getOutputParam();
                        if (outputParam != null && !outputParam.isEmpty()) {
                            String cleanedOutputParam = outputParam.trim();
                            regionContext.put(cleanedOutputParam, result);
                            allRegionContexts.put(regionCode, regionContext);  // 更新全局上下文
                            algorithmOutputs.put(cleanedOutputParam, result);
                            String algorithmName = algorithm.getAlgorithmName();
                            outputToAlgorithmName.put(cleanedOutputParam, algorithmName != null ? algorithmName.trim() : null);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("GRADE算法 " + algorithm.getAlgorithmName() + " 执行失败: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        // 保存输出参数到算法名称的映射
        if (!outputToAlgorithmName.isEmpty()) {
            stepResult.put("outputToAlgorithmName", outputToAlgorithmName);
        }

        stepResult.put("regionResults", regionResults);
        return stepResult;
    }

    /**
     * 生成结果二维表
     * 
     * @param executionResults 执行结果
     * @return 二维表数据
     */
    @Override
    public List<Map<String, Object>> generateResultTable(Map<String, Object> executionResults) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> stepResults = 
                (Map<String, Map<String, Object>>) executionResults.get("stepResults");

        if (stepResults == null || stepResults.isEmpty()) {
            return tableData;
        }

        // 收集所有地区代码和输出变量，以及输出参数到算法名称的映射
        Set<String> allRegions = new LinkedHashSet<>();
        Set<String> allOutputs = new LinkedHashSet<>();
        Map<String, String> globalOutputToAlgorithmName = new LinkedHashMap<>();  // 全局的输出参数到算法名称映射
        Map<String, String> resolvedColumnNames = new LinkedHashMap<>(); // 输出参数 -> 最终列名
        Set<String> usedColumnNames = new LinkedHashSet<>(); // 已使用列名，避免重复
        Map<String, String> townshipNameByRegion = new LinkedHashMap<>();
        Map<String, String> communityNameByRegion = new LinkedHashMap<>();
        Map<String, String> firstCommunityCodeByRegion = new LinkedHashMap<>();
        Map<String, Boolean> isTownshipByRegion = new LinkedHashMap<>();

        log.info("开始生成结果表，共 {} 个步骤", stepResults.size());

        for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
            String stepCode = stepEntry.getKey();

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> regionResults =
                    (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");

            // 获取输出参数到算法名称的映射
            @SuppressWarnings("unchecked")
            Map<String, String> outputToAlgorithmName =
                    (Map<String, String>) stepEntry.getValue().get("outputToAlgorithmName");
            if (outputToAlgorithmName != null) {
                log.info("步骤 {} 有 {} 个输出参数映射", stepCode, outputToAlgorithmName.size());
                outputToAlgorithmName.forEach((key, value) -> {
                    String cleanedKey = key != null ? key.trim() : null;
                    String cleanedValue = value != null ? value.trim() : null;
                    if (cleanedKey != null && !cleanedKey.isEmpty()) {
                        globalOutputToAlgorithmName.put(cleanedKey, cleanedValue);
                    }
                });
            } else {
                log.warn("步骤 {} 没有outputToAlgorithmName", stepCode);
            }

            if (regionResults != null) {
                log.info("步骤 {} 有 {} 个地区结果", stepCode, regionResults.size());
                allRegions.addAll(regionResults.keySet());

                for (Map<String, Object> outputs : regionResults.values()) {
                    allOutputs.addAll(outputs.keySet());
                }

                for (Map.Entry<String, Map<String, Object>> regionEntry : regionResults.entrySet()) {
                    String regionCode = regionEntry.getKey();
                    Map<String, Object> outputs = regionEntry.getValue();
                    if (outputs == null) {
                        continue;
                    }
                    String townshipNameMeta = toString(outputs.get("_townshipName"));
                    String communityNameMeta = toString(outputs.get("_communityName"));
                    String firstCommunityCodeMeta = toString(outputs.get("_firstCommunityCode"));
                    String townshipRegionCodeMeta = toString(outputs.get("_townshipRegionCode"));

                    if (townshipNameMeta != null) {
                        townshipNameByRegion.put(regionCode, townshipNameMeta);
                    }
                    if (communityNameMeta != null) {
                        communityNameByRegion.put(regionCode, communityNameMeta);
                    }
                    if (firstCommunityCodeMeta != null) {
                        firstCommunityCodeByRegion.put(regionCode, firstCommunityCodeMeta);
                        String derivedCode = deriveTownshipCodeForStorage(firstCommunityCodeMeta);
                        if (derivedCode != null) {
                            townshipNameByRegion.putIfAbsent(derivedCode, townshipNameMeta);
                            communityNameByRegion.putIfAbsent(derivedCode, communityNameMeta);
                            firstCommunityCodeByRegion.putIfAbsent(derivedCode, firstCommunityCodeMeta);
                            isTownshipByRegion.put(derivedCode, true);
                        }
                    }
                    if (outputs.containsKey("_isTownship")) {
                        Object flag = outputs.get("_isTownship");
                        boolean isTownship = false;
                        if (flag instanceof Boolean) {
                            isTownship = (Boolean) flag;
                        } else if (flag != null) {
                            isTownship = "true".equalsIgnoreCase(flag.toString());
                        }
                        isTownshipByRegion.put(regionCode, isTownship);
                    } else if (outputs.containsKey("_townshipRegionCode")) {
                        isTownshipByRegion.put(regionCode, true);
                    }

                    if (townshipRegionCodeMeta != null) {
                        String normalized = townshipRegionCodeMeta;
                        if (normalized.startsWith("TOWNSHIP_")) {
                            normalized = normalized.substring("TOWNSHIP_".length());
                        }
                        if (normalized.matches("\\d+")) {
                            if (normalized.length() >= 9) {
                                normalized = normalized.substring(0, 9);
                            }
                            townshipNameByRegion.putIfAbsent(normalized, townshipNameMeta);
                            communityNameByRegion.putIfAbsent(normalized, communityNameMeta);
                            firstCommunityCodeByRegion.putIfAbsent(normalized, firstCommunityCodeMeta);
                            isTownshipByRegion.put(normalized, true);
                        }
                    }

                    if (regionCode != null && regionCode.matches("\\d+") && regionCode.length() >= 9) {
                        String prefix = regionCode.substring(0, 9);
                        townshipNameByRegion.putIfAbsent(prefix, townshipNameMeta);
                        communityNameByRegion.putIfAbsent(prefix, communityNameMeta);
                        firstCommunityCodeByRegion.putIfAbsent(prefix, firstCommunityCodeMeta);
                        if (Boolean.TRUE.equals(isTownshipByRegion.get(regionCode))) {
                            isTownshipByRegion.put(prefix, true);
                        }
                    }
                }
            } else {
                log.warn("步骤 {} 没有regionResults", stepCode);
            }
        }

        log.info("收集完成：{} 个地区，{} 个输出字段，{} 个算法名称映射",
                allRegions.size(), allOutputs.size(), globalOutputToAlgorithmName.size());

        // 为每个地区生成一行数据
        for (String regionCode : allRegions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regionCode", regionCode);
            
            // 获取地区名称和乡镇名称
            String regionName = regionCode;
            String townshipName = townshipNameByRegion.get(regionCode);
            String communityName = communityNameByRegion.get(regionCode);
            String firstCommunityCodeMeta = firstCommunityCodeByRegion.get(regionCode);
            boolean isTownshipAggregated = Boolean.TRUE.equals(isTownshipByRegion.get(regionCode));

            if (isTownshipAggregated) {
                // 乡镇聚合数据
                if (townshipName == null || townshipName.isEmpty()) {
                    // 优先使用survey_data中的乡镇名称
                    QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                    surveyQuery.eq("region_code", regionCode);
                    SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                    if (surveyData != null && surveyData.getTownship() != null) {
                        townshipName = surveyData.getTownship();
                    } else if (firstCommunityCodeMeta != null) {
                        // 回退到首个社区记录
                        QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                        communityQuery.eq("region_code", firstCommunityCodeMeta);
                        CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
                        if (communityData != null && communityData.getTownshipName() != null) {
                            townshipName = communityData.getTownshipName();
                        }
                    }
                }
                if (isCodeLike(townshipName)) {
                    String fetched = getTownshipNameByCommunityCode(firstCommunityCodeMeta != null ? firstCommunityCodeMeta : regionCode);
                    if (fetched != null && !fetched.isEmpty()) {
                        townshipName = fetched;
                    }
                }

                if (townshipName != null && !townshipName.isEmpty()) {
                    regionName = townshipName;
                    row.put("townshipName", townshipName);
                    townshipNameByRegion.put(regionCode, townshipName);
                }
                if (communityName != null && !communityName.isEmpty()) {
                    row.put("communityName", communityName);
                }
                if (firstCommunityCodeMeta != null) {
                    row.put("_firstCommunityCode", firstCommunityCodeMeta);
                }
                row.put("_isTownship", true);
            } else {
                // 社区级数据
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
                if (communityData != null) {
                    townshipName = communityData.getTownshipName();
                    communityName = communityData.getCommunityName();
                    regionName = communityName != null ? communityName : regionCode;
                } else {
                    QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                    surveyQuery.eq("region_code", regionCode);
                    SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                    if (surveyData != null && surveyData.getTownship() != null) {
                        regionName = surveyData.getTownship();
                    } else {
                        regionName = regionCode;
                    }
                }
            }
            
            row.put("regionName", regionName);
            if (townshipName != null) {
                row.put("townshipName", townshipName);
            }
            if (communityName != null && !communityName.isEmpty()) {
                row.put("communityName", communityName);
            }

            // 收集该地区在所有步骤中的输出
            for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
                String stepCode = stepEntry.getKey();
                
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> regionResults = 
                        (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");
                
                    if (regionResults != null && regionResults.containsKey(regionCode)) {
                        Map<String, Object> outputs = regionResults.get(regionCode);
                        
                        // 将输出变量添加到行数据，使用算法中文名称作为列名
                        for (Map.Entry<String, Object> output : outputs.entrySet()) {
                            String outputParam = output.getKey();
                            
                            // 跳过内部使用的字段（以"_"开头）
                            if (outputParam.startsWith("_")) {
                                continue;
                            }
                            
                            String columnName = resolvedColumnNames.get(outputParam);
                            if (columnName == null) {
                                String preferredName = globalOutputToAlgorithmName.getOrDefault(outputParam, null);
                                if (preferredName != null) {
                                    preferredName = preferredName.trim();
                                }
                                String fallbackName = stepCode + "_" + outputParam;
                                
                                boolean preferredUsable = preferredName != null && !preferredName.isEmpty() && !usedColumnNames.contains(preferredName);
                                String uniqueName = preferredUsable ? preferredName : fallbackName;
                                
                                // 如果回退名称已被使用（理论上不应发生），保持回退名称以保证稳定
                                if (usedColumnNames.contains(uniqueName)) {
                                    uniqueName = fallbackName;
                                }
                                
                                columnName = uniqueName;
                                resolvedColumnNames.put(outputParam, columnName);
                                usedColumnNames.add(columnName);
                            }
                            
                            // 格式化数值为8位小数
                            Object value = output.getValue();
                            if (value != null && value instanceof Number) {
                                double doubleValue = ((Number) value).doubleValue();
                                value = Double.parseDouble(String.format("%.8f", doubleValue));
                            }
                            row.put(columnName, value);
                        }
                    }
                }

            tableData.add(row);

            // 记录第一行数据的字段数量（用于诊断）
            if (tableData.size() == 1) {
                log.info("第一行数据有 {} 个字段: {}", row.size(), row.keySet());
            }
        }

        log.info("生成结果表完成，共 {} 行数据", tableData.size());
        return tableData;
    }

    /**
     * 加载基础数据到上下文
     */
    private void loadBaseDataToContext(Map<String, Object> context, List<String> regionCodes, Long weightConfigId) {
        // 加载权重配置
        if (weightConfigId != null) {
            QueryWrapper<IndicatorWeight> weightQuery = new QueryWrapper<>();
            weightQuery.eq("config_id", weightConfigId);
            List<IndicatorWeight> weights = indicatorWeightMapper.selectList(weightQuery);
            
            // 将权重转换为Map便于查找
            Map<String, Double> weightMap = weights.stream()
                    .collect(Collectors.toMap(
                            IndicatorWeight::getIndicatorCode,
                            IndicatorWeight::getWeight,
                            (v1, v2) -> v1
                    ));
            context.put("weights", weightMap);
            
            // 同时将每个权重作为独立变量存储（便于表达式直接引用）
            for (IndicatorWeight weight : weights) {
                // 确保权重值为Double类型
                Double weightValue = weight.getWeight();
                if (weightValue == null) {
                    weightValue = 0.0;
                }
                context.put("weight_" + weight.getIndicatorCode(), weightValue);
            }
        }
    }

    /**
     * 加载前面步骤的输出结果到当前区域上下文
     * 从 globalContext 中提取前面步骤的 regionResults，并将当前区域的输出值添加到上下文
     */
    private void loadPreviousStepOutputs(Map<String, Object> regionContext, String regionCode, Map<String, Object> globalContext) {
        // 遍历 globalContext 中所有以 "step_" 开头的条目
        for (Map.Entry<String, Object> entry : globalContext.entrySet()) {
            if (entry.getKey().startsWith("step_") && entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                
                // 获取该步骤的 regionResults
                Object regionResultsObj = stepResult.get("regionResults");
                if (regionResultsObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) regionResultsObj;
                    
                    // 获取当前区域的输出
                    Map<String, Object> currentRegionOutputs = regionResults.get(regionCode);
                    if (currentRegionOutputs != null) {
                        // 将当前区域的所有输出变量添加到上下文
                        for (Map.Entry<String, Object> output : currentRegionOutputs.entrySet()) {
                            regionContext.put(output.getKey(), output.getValue());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 将调查数据添加到上下文
     * 同时添加驼峰命名和下划线命名，以支持不同的表达式风格
     */
    private void addSurveyDataToContext(Map<String, Object> context, SurveyData surveyData) {
        // 地区信息
        context.put("regionCode", surveyData.getRegionCode());
        context.put("region_code", surveyData.getRegionCode());
        context.put("province", surveyData.getProvince());
        context.put("city", surveyData.getCity());
        context.put("county", surveyData.getCounty());
        context.put("township", surveyData.getTownship());
        
        // 人口数据（驼峰和下划线两种命名）
        context.put("population", surveyData.getPopulation());
        
        // 管理人员（驼峰和下划线两种命名）
        context.put("managementStaff", surveyData.getManagementStaff());
        context.put("management_staff", surveyData.getManagementStaff());
        
        // 风险评估（驼峰和下划线两种命名）
        String riskAssessmentValue = surveyData.getRiskAssessment();
        // 标准化风险评估值：如果值是"低"、"中"、"高"，转换为"是"，以匹配算法表达式
        String normalizedRiskAssessment = riskAssessmentValue;
        if (riskAssessmentValue != null &&
            (riskAssessmentValue.equals("低") ||
             riskAssessmentValue.equals("中") ||
             riskAssessmentValue.equals("高"))) {
            normalizedRiskAssessment = "是";
        }

        context.put("riskAssessment", normalizedRiskAssessment);
        context.put("risk_assessment", normalizedRiskAssessment);
        context.put("是否开展风险评估", normalizedRiskAssessment);  // 中文变量名
        
        // 资金投入（驼峰和下划线两种命名）
        context.put("fundingAmount", surveyData.getFundingAmount());
        context.put("funding_amount", surveyData.getFundingAmount());
        
        // 物资储备（驼峰和下划线两种命名）
        context.put("materialValue", surveyData.getMaterialValue());
        context.put("material_value", surveyData.getMaterialValue());
        
        // 医院床位（驼峰和下划线两种命名）
        context.put("hospitalBeds", surveyData.getHospitalBeds());
        context.put("hospital_beds", surveyData.getHospitalBeds());
        
        // 消防员（驼峰和下划线两种命名）
        context.put("firefighters", surveyData.getFirefighters());
        
        // 志愿者（驼峰和下划线两种命名）
        context.put("volunteers", surveyData.getVolunteers());
        
        // 民兵预备役（驼峰和下划线两种命名）
        context.put("militiaReserve", surveyData.getMilitiaReserve());
        context.put("militia_reserve", surveyData.getMilitiaReserve());
        
        // 培训参与者（驼峰和下划线两种命名）
        context.put("trainingParticipants", surveyData.getTrainingParticipants());
        context.put("training_participants", surveyData.getTrainingParticipants());
        
        // 避难所容量（驼峰和下划线两种命名）
        context.put("shelterCapacity", surveyData.getShelterCapacity());
        context.put("shelter_capacity", surveyData.getShelterCapacity());
    }

    /**
     * 通用方法：将Map数据添加到上下文
     * 数据库字段名直接作为变量名，无需手动映射
     * 所有数值类型转换为Double，避免整数除法精度丢失
     */
    private void addMapDataToContext(Map<String, Object> context, Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 跳过时间字段和ID字段
            if ("create_time".equals(key) || "update_time".equals(key) || "id".equals(key)) {
                continue;
            }

            // 转换数值类型为Double，避免整数除法精度丢失
            Object contextValue = value;
            if (value != null) {
                if (value instanceof Integer) {
                    contextValue = ((Integer) value).doubleValue();
                } else if (value instanceof Long) {
                    contextValue = ((Long) value).doubleValue();
                } else if (value instanceof java.math.BigDecimal) {
                    contextValue = ((java.math.BigDecimal) value).doubleValue();
                } else if (value instanceof Float) {
                    contextValue = ((Float) value).doubleValue();
                }
            }

            // 直接使用数据库字段名作为上下文变量名
            context.put(key, contextValue);
        }

    }

    /**
     * 将社区数据添加到上下文（已废弃，使用addMapDataToContext替代）
     * 所有数值类型转换为Double，避免整数除法精度丢失
     * @deprecated 使用selectMaps查询和addMapDataToContext方法替代
     */
    @Deprecated
    private void addCommunityDataToContext(Map<String, Object> context, CommunityDisasterReductionCapacity communityData) {
        // 地区信息
        context.put("regionCode", communityData.getRegionCode());
        context.put("region_code", communityData.getRegionCode());
        context.put("province", communityData.getProvinceName());
        context.put("city", communityData.getCityName());
        context.put("county", communityData.getCountyName());
        context.put("township", communityData.getTownshipName());
        context.put("community", communityData.getCommunityName());

        // 人口数据（转换为Double）
        context.put("population", communityData.getResidentPopulation() != null ? communityData.getResidentPopulation().doubleValue() : 0.0);
        context.put("residentPopulation", communityData.getResidentPopulation() != null ? communityData.getResidentPopulation().doubleValue() : 0.0);

        // 风险评估相关（4个是/否问题）
        context.put("hasEmergencyPlan", communityData.getHasEmergencyPlan());
        context.put("hasVulnerableGroupsList", communityData.getHasVulnerableGroupsList());
        context.put("hasDisasterPointsList", communityData.getHasDisasterPointsList());
        context.put("hasDisasterMap", communityData.getHasDisasterMap());

        // 资金投入（转换为Double）
        Double fundingAmount = communityData.getLastYearFundingAmount() != null ? communityData.getLastYearFundingAmount().doubleValue() : 0.0;
        context.put("fundingAmount", fundingAmount);
        context.put("funding_amount", fundingAmount);
        context.put("lastYearFundingAmount", fundingAmount);

        // 物资储备（转换为Double）
        Double materialValue = communityData.getMaterialsEquipmentValue() != null ? communityData.getMaterialsEquipmentValue().doubleValue() : 0.0;
        context.put("materialValue", materialValue);
        context.put("material_value", materialValue);
        context.put("materialsEquipmentValue", materialValue);

        // 医疗服务（转换为Double）
        Double medicalServiceCount = communityData.getMedicalServiceCount() != null ? communityData.getMedicalServiceCount().doubleValue() : 0.0;
        context.put("medicalServiceCount", medicalServiceCount);
        context.put("medical_service_count", medicalServiceCount);

        // 民兵预备役（转换为Double）
        Double militiaReserve = communityData.getMilitiaReserveCount() != null ? communityData.getMilitiaReserveCount().doubleValue() : 0.0;
        context.put("militiaReserve", militiaReserve);
        context.put("militia_reserve", militiaReserve);
        context.put("militiaReserveCount", militiaReserve);

        // 志愿者（转换为Double）
        Double volunteers = communityData.getRegisteredVolunteerCount() != null ? communityData.getRegisteredVolunteerCount().doubleValue() : 0.0;
        context.put("volunteers", volunteers);
        context.put("registeredVolunteerCount", volunteers);

        // 培训参与者（转换为Double）
        Double trainingParticipants = communityData.getLastYearTrainingParticipants() != null ? communityData.getLastYearTrainingParticipants().doubleValue() : 0.0;
        context.put("trainingParticipants", trainingParticipants);
        context.put("training_participants", trainingParticipants);
        context.put("lastYearTrainingParticipants", trainingParticipants);

        // 演练参与者（转换为Double）
        Double drillParticipants = communityData.getLastYearDrillParticipants() != null ? communityData.getLastYearDrillParticipants().doubleValue() : 0.0;
        context.put("drillParticipants", drillParticipants);
        context.put("lastYearDrillParticipants", drillParticipants);

        // 避难所容量（转换为Double）
        Double shelterCapacity = communityData.getEmergencyShelterCapacity() != null ? communityData.getEmergencyShelterCapacity().doubleValue() : 0.0;
        context.put("shelterCapacity", shelterCapacity);
        context.put("shelter_capacity", shelterCapacity);
        context.put("emergencyShelterCapacity", shelterCapacity);
    }

    /**
     * 执行算法的单个步骤并返回2D表格结果
     *
     * @param algorithmId 算法ID（对应algorithm_config表）
     * @param stepOrder 步骤顺序（从1开始）
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 步骤执行结果，包含2D表格数据
     */
    @Override
    public Map<String, Object> executeAlgorithmStep(Long algorithmId, Integer stepOrder, List<String> regionCodes, Long weightConfigId) {

        try {
            // 1. 获取算法配置的所有步骤
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            if (algorithmSteps.isEmpty()) {
                throw new RuntimeException("算法配置没有找到任何步骤");
            }

            // 2. 找到指定顺序的步骤
            AlgorithmStep targetStep = algorithmSteps.stream()
                    .filter(step -> stepOrder.equals(step.getStepOrder()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到步骤顺序为 " + stepOrder + " 的算法步骤"));

            // 3. 如果不是第一步，需要先执行前面的所有步骤来获取依赖数据
            Map<String, Object> globalContext = new HashMap<>();
            globalContext.put("algorithmId", algorithmId);
            globalContext.put("regionCodes", regionCodes);
            globalContext.put("weightConfigId", weightConfigId);

            // 加载基础数据
            loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

            // 如果不是第一步，执行前面的所有步骤
            if (stepOrder > 1) {
                executeAlgorithmStepsInternalUpTo(algorithmSteps, stepOrder - 1, regionCodes, globalContext);
            }

            // 4. 执行目标步骤
            Map<String, Object> stepExecutionResult = executeAlgorithmStepInternal(targetStep, regionCodes, globalContext);

        // 5. 生成该步骤的2D表格数据
        List<Map<String, Object>> tableData = generateStepResultTable(stepExecutionResult, regionCodes);

        // 生成 columns 数组（包含 stepOrder 信息）
        List<Map<String, Object>> columns = generateColumnsWithStepOrder(tableData, stepOrder);

        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("stepId", targetStep.getId());
        result.put("stepName", targetStep.getStepName());
        result.put("stepOrder", stepOrder);
        result.put("stepCode", targetStep.getStepCode());
        result.put("description", targetStep.getStepDescription());
        result.put("executionResult", stepExecutionResult);
        result.put("tableData", tableData);
        result.put("columns", columns);
        result.put("success", true);
        result.put("executionTime", new Date());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("执行算法步骤失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取算法所有步骤的基本信息
     *
     * @param algorithmId 算法ID
     * @return 算法步骤列表信息
     */
    @Override
    public Map<String, Object> getAlgorithmStepsInfo(Long algorithmId) {

        try {
            // 获取算法配置
            AlgorithmConfig algorithmConfig = algorithmConfigMapper.selectById(algorithmId);
            if (algorithmConfig == null) {
                throw new RuntimeException("算法配置不存在");
            }

            // 获取所有步骤
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            // 转换为简化信息
            List<Map<String, Object>> stepsInfo = algorithmSteps.stream().map(step -> {
                Map<String, Object> stepInfo = new HashMap<>();
                stepInfo.put("stepId", step.getId());
                stepInfo.put("stepName", step.getStepName());
                stepInfo.put("stepOrder", step.getStepOrder());
                stepInfo.put("stepCode", step.getStepCode());
                stepInfo.put("description", step.getStepDescription());
                stepInfo.put("status", step.getStatus());
                return stepInfo;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("algorithmId", algorithmId);
            result.put("algorithmName", algorithmConfig.getConfigName());
            result.put("algorithmDescription", algorithmConfig.getDescription());
            result.put("totalSteps", stepsInfo.size());
            result.put("steps", stepsInfo);
            result.put("success", true);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("获取算法步骤信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量执行算法步骤（直到指定步骤）
     *
     * @param algorithmId 算法ID
     * @param upToStepOrder 执行到第几步（包含该步骤）
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 所有已执行步骤的结果
     */
    @Override
    public Map<String, Object> executeAlgorithmStepsUpTo(Long algorithmId, Integer upToStepOrder, List<String> regionCodes, Long weightConfigId) {

        try {
            // 1. 获取算法配置的所有步骤
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            if (algorithmSteps.isEmpty()) {
                throw new RuntimeException("算法配置没有找到任何步骤");
            }

            // 2. 验证步骤顺序
            boolean hasTargetStep = algorithmSteps.stream()
                    .anyMatch(step -> upToStepOrder.equals(step.getStepOrder()));
            if (!hasTargetStep) {
                throw new RuntimeException("未找到步骤顺序为 " + upToStepOrder + " 的算法步骤");
            }

            // 3. 初始化上下文
            Map<String, Object> globalContext = new HashMap<>();
            globalContext.put("algorithmId", algorithmId);
            globalContext.put("regionCodes", regionCodes);
            globalContext.put("weightConfigId", weightConfigId);

            // 加载基础数据
            loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

            // 4. 执行所有步骤直到指定步骤
            Map<String, Object> allStepResults = executeAlgorithmStepsInternalUpTo(algorithmSteps, upToStepOrder, regionCodes, globalContext);

            // 5. 为每个步骤生成2D表格
            Map<String, List<Map<String, Object>>> allTableData = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : allStepResults.entrySet()) {
                String stepKey = entry.getKey();
                if (stepKey.startsWith("step_")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                    List<Map<String, Object>> tableData = generateStepResultTable(stepResult, regionCodes);
                    allTableData.put(stepKey, tableData);
                }
            }

            // 6. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("algorithmId", algorithmId);
            result.put("executedUpToStep", upToStepOrder);
            result.put("stepResults", allStepResults);
            result.put("tableData", allTableData);
            result.put("success", true);
            result.put("executionTime", new Date());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("批量执行算法步骤失败: " + e.getMessage(), e);
        }
    }

    /**
     * 内部方法：执行算法步骤直到指定顺序
     */
    private Map<String, Object> executeAlgorithmStepsInternalUpTo(List<AlgorithmStep> algorithmSteps, Integer upToStepOrder, 
                                                                  List<String> regionCodes, Map<String, Object> globalContext) {
        Map<String, Object> stepResults = new HashMap<>();
        
        for (AlgorithmStep algorithmStep : algorithmSteps) {
            if (algorithmStep.getStepOrder() <= upToStepOrder) {
                
                try {
                    Map<String, Object> stepResult = executeAlgorithmStepInternal(algorithmStep, regionCodes, globalContext);
                    stepResults.put("step_" + algorithmStep.getStepCode(), stepResult);
                    
                    // 将步骤结果合并到全局上下文（供后续步骤使用）
                    globalContext.put("step_" + algorithmStep.getStepCode(), stepResult);
                    
                } catch (Exception e) {
                    throw new RuntimeException("算法步骤 " + algorithmStep.getStepName() + " 执行失败: " + e.getMessage(), e);
                }
            }
        }
        
        return stepResults;
    }

    /**
     * 内部方法：执行单个算法步骤
     */
    private Map<String, Object> executeAlgorithmStepInternal(AlgorithmStep algorithmStep, List<String> regionCodes, Map<String, Object> globalContext) {
        // 获取该步骤的所有公式并按顺序排序
        QueryWrapper<FormulaConfig> formulaQuery = new QueryWrapper<>();
        formulaQuery.eq("algorithm_step_id", algorithmStep.getId().toString())
                .eq("status", 1)
                .orderByAsc("id");
        List<FormulaConfig> formulas = formulaConfigMapper.selectList(formulaQuery);

        if (formulas.isEmpty()) {
            return new HashMap<>();
        }

        // 初始化步骤结果
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", algorithmStep.getId());
        stepResult.put("stepName", algorithmStep.getStepName());
        stepResult.put("stepCode", algorithmStep.getStepCode());

        // 第一遍：为所有地区准备上下文数据
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();

        // 获取modelId以决定使用哪个数据源
        Long modelId = (Long) globalContext.get("modelId");

        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(globalContext);
            regionContext.put("currentRegionCode", regionCode);

            // 根据modelId选择不同的数据源
            if (modelId != null && modelId == 4) {
                // 社区模型(modelId=4)：从community_disaster_reduction_capacity表加载数据
                // 使用selectMaps直接返回Map，key为数据库字段名，可直接匹配算法表达式中的变量名
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                if (communityDataList != null && !communityDataList.isEmpty()) {
                    Map<String, Object> communityDataMap = communityDataList.get(0);
                    addMapDataToContext(regionContext, communityDataMap);
                }
            } else {
                // 乡镇模型(modelId=3)：从survey_data表加载数据
                QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
                dataQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);

                if (surveyData != null) {
                    addSurveyDataToContext(regionContext, surveyData);
                }
            }

            // 再加载前面步骤的输出结果（计算结果），这样会覆盖原始数据中的同名字段
            loadPreviousStepOutputs(regionContext, regionCode, globalContext);

            allRegionContexts.put(regionCode, regionContext);
        }
        
        // 第二遍：为每个地区执行公式（支持特殊标记）
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToFormulaName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> formulaOutputs = new LinkedHashMap<>();
            
            // 按顺序执行每个公式
            for (FormulaConfig formula : formulas) {
                try {
                    
                    Object result;
                    String expression = formula.getFormulaExpression();
                    
                    // 检查是否是特殊标记
                    if (expression != null && expression.startsWith("@")) {
                        // 解析特殊标记: @MARKER:params
                        String[] parts = expression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        
                        // 调用特殊算法服务
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 确保数值类型转换并格式化为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        // 执行标准QLExpress表达式
                        result = qlExpressService.execute(expression, regionContext);
                        
                        // 确保数值类型的结果转换为Double并格式化为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    }
                    
                    // 保存公式输出到上下文（供后续公式使用）
                    String outputParam = formula.getOutputVariable();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        regionContext.put(outputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 更新全局上下文
                        formulaOutputs.put(outputParam, result);
                        outputToFormulaName.put(outputParam, formula.getFormulaName());
                    }
                    
                } catch (Exception e) {
                    throw new RuntimeException("公式 " + formula.getFormulaName() + " 执行失败: " + e.getMessage(), e);
                }
            }
            
            regionResults.put(regionCode, formulaOutputs);
        }
        
        // 保存输出参数到公式名称的映射
        if (!outputToFormulaName.isEmpty()) {
            stepResult.put("outputToFormulaName", outputToFormulaName);
        }

        stepResult.put("regionResults", regionResults);
        return stepResult;
    }

    /**
     * 为单个步骤生成2D表格数据
     */
    private List<Map<String, Object>> generateStepResultTable(Map<String, Object> stepResult, List<String> regionCodes) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> regionResults = 
                (Map<String, Map<String, Object>>) stepResult.get("regionResults");
        
        @SuppressWarnings("unchecked")
        Map<String, String> outputToFormulaName = 
                (Map<String, String>) stepResult.get("outputToFormulaName");
        
        if (regionResults == null) {
            return tableData;
        }
        
        // 为每个地区生成一行数据
        for (String regionCode : regionCodes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regionCode", regionCode);

            String regionName = regionCode;
            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", regionCode);
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
            if (communityData != null) {
                if (communityData.getCommunityName() != null) {
                    regionName = communityData.getCommunityName();
                } else if (communityData.getTownshipName() != null) {
                    regionName = communityData.getTownshipName();
                }
            } else {
                QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                surveyQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                if (surveyData != null && surveyData.getTownship() != null) {
                    regionName = surveyData.getTownship();
                }
            }

            Map<String, Object> outputs = regionResults.get(regionCode);
            String townshipNameMeta = null;
            String communityNameMeta = null;
            String firstCommunityCodeMeta = null;
            boolean isTownship = false;

            if (outputs != null) {
                townshipNameMeta = toString(outputs.get("_townshipName"));
                communityNameMeta = toString(outputs.get("_communityName"));
                firstCommunityCodeMeta = toString(outputs.get("_firstCommunityCode"));
                Object flag = outputs.get("_isTownship");
                if (flag instanceof Boolean) {
                    isTownship = (Boolean) flag;
                } else if (flag != null) {
                    isTownship = "true".equalsIgnoreCase(flag.toString());
                }
                if (!isTownship && outputs.containsKey("_townshipRegionCode")) {
                    isTownship = true;
                }

                if (isTownship) {
                    if (!isEmptyString(townshipNameMeta)) {
                        regionName = townshipNameMeta;
                    } else if (!isEmptyString(firstCommunityCodeMeta)) {
                        String name = getTownshipNameByCommunityCode(firstCommunityCodeMeta);
                        if (!isEmptyString(name)) {
                            regionName = name;
                        }
                    }
                    if (!isEmptyString(regionName)) {
                        row.put("townshipName", regionName);
                    }
                } else {
                    if (!isEmptyString(communityNameMeta)) {
                        regionName = communityNameMeta;
                    } else if (!isEmptyString(townshipNameMeta)) {
                        regionName = townshipNameMeta;
                    }
                    if (!isEmptyString(townshipNameMeta)) {
                        row.put("townshipName", townshipNameMeta);
                    }
                    if (!isEmptyString(communityNameMeta)) {
                        row.put("communityName", communityNameMeta);
                    }
                }

                for (Map.Entry<String, Object> output : outputs.entrySet()) {
                    String outputParam = output.getKey();
                    if (outputParam.startsWith("_")) {
                        continue;
                    }
                    String columnName;
                    
                    // 优先使用公式名称作为列名
                    if (outputToFormulaName != null && outputToFormulaName.containsKey(outputParam)) {
                        columnName = outputToFormulaName.get(outputParam);
                    } else {
                        columnName = outputParam;
                    }
                    
                    // 格式化数值为8位小数
                    Object value = output.getValue();
                    if (value != null && value instanceof Number) {
                        double doubleValue = ((Number) value).doubleValue();
                        value = Double.parseDouble(String.format("%.8f", doubleValue));
                    }
                    row.put(columnName, value);
                }
            }

            if (isCodeLike(regionName)) {
                String lookupCode = !isEmptyString(firstCommunityCodeMeta) ? firstCommunityCodeMeta : regionCode;
                String resolved = getTownshipNameByCommunityCode(lookupCode);
                if (isEmptyString(resolved)) {
                    String derived = deriveTownshipCodeForStorage(lookupCode);
                    resolved = resolveTownshipNameByPrefix(derived);
                }
                if (!isEmptyString(resolved)) {
                    regionName = resolved;
                }
            }

            row.put("regionName", regionName);
            if (!isEmptyString(townshipNameMeta)) {
                row.put("townshipName", townshipNameMeta);
            }
            if (!isEmptyString(communityNameMeta)) {
                row.put("communityName", communityNameMeta);
            }
            if (!isEmptyString(firstCommunityCodeMeta)) {
                row.put("_firstCommunityCode", firstCommunityCodeMeta);
            }
            
            tableData.add(row);
        }

        Integer currentStepOrder = null;
        Object orderObj = stepResult.get("stepOrder");
        if (orderObj instanceof Number) {
            currentStepOrder = ((Number) orderObj).intValue();
        } else if (orderObj instanceof String) {
            try {
                currentStepOrder = Integer.parseInt((String) orderObj);
            } catch (NumberFormatException ignore) {
                currentStepOrder = null;
            }
        }

        if (currentStepOrder != null && currentStepOrder >= 6 && !tableData.isEmpty()) {
            int sampleCount = Math.min(3, tableData.size());
            for (int i = 0; i < sampleCount; i++) {
                Map<String, Object> sample = tableData.get(i);
                log.info("[Step {} sample] regionCode={}, regionName={}",
                        currentStepOrder,
                        sample.get("regionCode"),
                        sample.get("regionName"));
            }
        }
        
        return tableData;
    }

    /**
     * 从表格数据和步骤输出参数生成 columns 数组，每列标记所属步骤
     * 
     * @param tableData 表格数据
     * @param stepOutputParams 步骤序号 -> 输出参数名称列表的映射
     * @return columns 数组
     */
    private List<Map<String, Object>> generateColumnsWithAllSteps(
            List<Map<String, Object>> tableData,
            Map<Integer, Set<String>> stepOutputParams) {

        List<Map<String, Object>> columns = new ArrayList<>();

        if (tableData == null || tableData.isEmpty()) {
            return columns;
        }

        // 从所有行数据提取所有可能的列名（不只是第一行）
        Set<String> allColumnNames = new LinkedHashSet<>();
        for (Map<String, Object> row : tableData) {
            allColumnNames.addAll(row.keySet());
        }

        log.info("从 {} 行数据中收集到 {} 个唯一列名", tableData.size(), allColumnNames.size());

        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region", "townshipName", "communityName"));

        // 创建反向映射：列名 -> 步骤序号
        Map<String, Integer> columnToStepOrder = new HashMap<>();
        for (Map.Entry<Integer, Set<String>> entry : stepOutputParams.entrySet()) {
            Integer stepOrder = entry.getKey();
            Set<String> outputNames = entry.getValue();
            for (String outputName : outputNames) {
                if (outputName == null) {
                    continue;
                }
                String normalized = outputName.trim();
                if (normalized.isEmpty()) {
                    continue;
                }
                columnToStepOrder.putIfAbsent(normalized, stepOrder);
            }
        }

        // 生成columns
        for (String columnName : allColumnNames) {
            // 跳过内部字段
            if (columnName.startsWith("_")) {
                continue;
            }

            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);
            column.put("label", columnName);

            // 设置列宽
            if ("regionCode".equals(columnName)) {
                column.put("width", 150);
            } else if (baseColumns.contains(columnName)) {
                column.put("width", 120);
            } else {
                column.put("width", 120);
                // 非基础列添加 stepOrder
                Integer stepOrder = columnToStepOrder.get(columnName);
                if (stepOrder == null) {
                    stepOrder = columnToStepOrder.get(columnName.trim());
                }
                if (stepOrder != null) {
                    column.put("stepOrder", stepOrder);
                }
            }

            columns.add(column);
        }

        log.info("生成 {} 个columns，其中 {} 个有stepOrder",
                columns.size(),
                columns.stream().filter(c -> c.containsKey("stepOrder")).count());

        return columns;
    }

    /**
     * 从表格数据生成 columns 数组，并为非基础列添加 stepOrder
     * 
     * @param tableData 表格数据
     * @param stepOrder 当前步骤序号
     * @return columns 数组
     */
    private List<Map<String, Object>> generateColumnsWithStepOrder(
            List<Map<String, Object>> tableData, Integer stepOrder) {
        
        List<Map<String, Object>> columns = new ArrayList<>();
        
        if (tableData == null || tableData.isEmpty()) {
            return columns;
        }
        
        // 从第一行数据提取所有列名
        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
        
        
        for (String columnName : firstRow.keySet()) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);
            column.put("label", columnName);  // 使用中文名称作为 label
            
            // 设置列宽
            if ("regionCode".equals(columnName)) {
                column.put("width", 150);
            } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                column.put("width", 120);
            } else {
                column.put("width", 120);
                // 非基础列添加 stepOrder
                column.put("stepOrder", stepOrder);
            }
            
            columns.add(column);
        }
        
        
        return columns;
    }

    private List<Map<String, Object>> generateColumnsForStep(
            Map<String, Object> rawStepResult,
            List<Map<String, Object>> tableData,
            Integer stepOrder) {

        List<Map<String, Object>> columns = new ArrayList<>();
        if (tableData == null || tableData.isEmpty()) {
            return columns;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> outputNameMap = rawStepResult != null
                ? (Map<String, String>) rawStepResult.getOrDefault("outputToAlgorithmName",
                rawStepResult.get("outputToFormulaName"))
                : null;

        String stepCode = rawStepResult != null ? toString(rawStepResult.get("stepCode")) : null;

        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region", "townshipName", "communityName"));

        for (String columnName : firstRow.keySet()) {
            if (columnName.startsWith("_")) {
                continue;
            }

            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);

            String label;
            if (baseColumns.contains(columnName)) {
                label = getBaseColumnLabel(columnName);
            } else {
                label = resolveColumnLabel(columnName, stepCode, outputNameMap);
            }

            column.put("label", label);

            if ("regionCode".equals(columnName)) {
                column.put("width", 150);
            } else if (baseColumns.contains(columnName)) {
                column.put("width", 120);
            } else {
                column.put("width", 120);
                column.put("stepOrder", stepOrder);
            }

            columns.add(column);
        }

        return columns;
    }

    @Autowired
    private AlgorithmStepMapper algorithmStepMapper;
    
    @Autowired
    private AlgorithmConfigMapper algorithmConfigMapper;
    
    @Autowired
    private FormulaConfigMapper formulaConfigMapper;

    /**
     * 执行乡镇聚合
     * 按乡镇分组，对社区数据进行聚合计算（求和后除以社区数量）
     * 
     * @param stepId 步骤ID
     * @param regionCodes 社区代码列表
     * @param inputData 输入数据（包含步骤1的社区级别计算结果）
     * @return 乡镇级别的聚合结果
     */
    private Map<String, Object> executeTownshipAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("开始执行乡镇聚合, stepId={}, regionCodes.size={}", stepId, regionCodes.size());
        
        // 1. 获取步骤信息
        ModelStep step = modelStepMapper.selectById(stepId);
        if (step == null || step.getStatus() == 0) {
            throw new RuntimeException("步骤不存在或已禁用");
        }
        
        // 2. 获取该步骤的所有算法
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);

        if (algorithms == null || algorithms.isEmpty()) {
            return new HashMap<>();
        }
        
        // 3. 按乡镇分组收集社区数据
        Map<String, List<Map<String, Object>>> townshipGroups = new LinkedHashMap<>();
        Map<String, String> townshipToFirstRegionCode = new HashMap<>();  // 记录每个乡镇的第一个社区代码（用于后续步骤）
        
        for (String regionCode : regionCodes) {
            // 获取社区的乡镇信息
            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", regionCode);
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);

            if (communityData == null) {
                continue;
            }

            String townshipName = communityData.getTownshipName();
            if (townshipName == null || townshipName.isEmpty()) {
                continue;
            }
            
            // 获取步骤1的输出结果（社区级别的能力值）
            Map<String, Object> communityContext = new HashMap<>();
            communityContext.put("currentRegionCode", regionCode);
            
            // 从inputData中获取步骤1的结果
            // inputData中包含 "step_XXX" 的键，其值是步骤的执行结果
            for (Map.Entry<String, Object> entry : inputData.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("step_")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepResult.get("regionResults");
                    
                    if (regionResults != null && regionResults.containsKey(regionCode)) {
                        // 将该社区在这个步骤的输出添加到上下文
                        Map<String, Object> outputs = regionResults.get(regionCode);
                        communityContext.putAll(outputs);
                    }
                }
            }
            
            // 按乡镇分组
            townshipGroups.computeIfAbsent(townshipName, k -> new ArrayList<>()).add(communityContext);

            // 记录每个乡镇的第一个社区代码
            townshipToFirstRegionCode.putIfAbsent(townshipName, regionCode);
        }
        
        log.info("按乡镇分组完成，共 {} 个乡镇", townshipGroups.size());
        
        // 4. 对每个乡镇执行聚合计算
        Map<String, Map<String, Object>> townshipResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : townshipGroups.entrySet()) {
            String townshipName = entry.getKey();
            List<Map<String, Object>> communities = entry.getValue();
            int communityCount = communities.size();
            
            log.info("处理乡镇: {}, 社区数量: {}", townshipName, communityCount);

            Map<String, Object> townshipOutput = new LinkedHashMap<>();

            // 对每个算法执行聚合
            for (StepAlgorithm algorithm : algorithms) {
                String qlExpression = algorithm.getQlExpression();
                String outputParam = algorithm.getOutputParam();
                String inputParams = algorithm.getInputParams();

                if (outputParam == null || outputParam.isEmpty()) {
                    continue;
                }
                String cleanedOutputParam = outputParam.trim();

                // 检查表达式是否包含SUM()函数
                double result;
                if (qlExpression != null && qlExpression.contains("SUM(")) {
                    // 使用新的SUM表达式计算
                    result = calculateAggregationExpression(qlExpression, communities, communityCount);
                } else {
                    // 兼容旧逻辑：简单的字段求和平均
                    String inputField = null;
                    if (inputParams != null && !inputParams.isEmpty()) {
                        inputField = inputParams.split(",")[0].trim();
                    } else {
                        inputField = qlExpression != null ? qlExpression.trim() : null;
                    }

                    if (inputField == null || inputField.isEmpty()) {
                        continue;
                    }

                    double sum = 0.0;
                    int validCount = 0;
                    for (Map<String, Object> community : communities) {
                        Object value = community.get(inputField);
                        if (value != null) {
                            double doubleValue = toDouble(value);
                            sum += doubleValue;
                            validCount++;
                        }
                    }
                    result = validCount > 0 ? sum / communityCount : 0.0;
                }

                // 格式化为8位小数
                result = Double.parseDouble(String.format("%.8f", result));

                townshipOutput.put(cleanedOutputParam, result);
                String algorithmName = algorithm.getAlgorithmName();
                outputToAlgorithmName.put(cleanedOutputParam, algorithmName != null ? algorithmName.trim() : null);
            }
            
            // 使用"TOWNSHIP_"前缀 + 乡镇名称作为虚拟的regionCode
            // 这样可以确保每个乡镇有唯一的标识，且不会与社区代码冲突
            String firstCommunityCode = townshipToFirstRegionCode.get(townshipName);
            String townshipRegionCode = deriveTownshipRegionCode(firstCommunityCode, townshipName);
            townshipResults.put(townshipRegionCode, townshipOutput);
            
            // 同时在上下文中保存乡镇名称，供generateResultTable使用
            townshipOutput.put("_townshipName", townshipName);
            townshipOutput.put("_firstCommunityCode", firstCommunityCode);
            townshipOutput.put("_townshipRegionCode", townshipRegionCode);
            townshipOutput.put("_isTownship", true);
        }
        
        // 5. 构建步骤结果
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());
        stepResult.put("stepOrder", step.getStepOrder());
        stepResult.put("regionResults", townshipResults);
        stepResult.put("outputToAlgorithmName", outputToAlgorithmName);
        
        log.info("乡镇聚合完成，共 {} 个乡镇", townshipResults.size());
        
        return stepResult;
    }
    
    /**
     * 计算包含SUM()函数的聚合表达式
     * 支持的格式：
     * - SUM(fieldName) - 对字段求和
     * - SUM(fieldA)+SUM(fieldB) - 多个字段求和
     * - SUM(fieldA)/SUM(fieldB)*10000 - 复杂表达式
     * - (SUM(fieldA)+SUM(fieldB))/(2*communityCount) - 包含社区数量
     *
     * @param expression 表达式，如 "SUM(A)+SUM(B)" 或 "SUM(A)/communityCount"
     * @param communities 社区数据列表
     * @param communityCount 社区数量
     * @return 计算结果
     */
    private double calculateAggregationExpression(String expression, List<Map<String, Object>> communities, int communityCount) {
        try {
            // 1. 使用正则表达式找出所有SUM(字段名)
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("SUM\\(([^)]+)\\)");
            java.util.regex.Matcher matcher = pattern.matcher(expression);

            // 2. 先找出所有SUM并计算，保存到Map中（避免重复计算）
            Map<String, Double> sumResults = new java.util.LinkedHashMap<>();

            while (matcher.find()) {
                String fullMatch = matcher.group(0);  // 完整的 SUM(fieldName)
                String fieldName = matcher.group(1);  // 字段名

                // 如果已经计算过这个SUM，跳过
                if (sumResults.containsKey(fullMatch)) {
                    continue;
                }

                // 计算该字段在所有社区的总和
                double sum = 0.0;
                for (Map<String, Object> community : communities) {
                    Object value = community.get(fieldName);
                    if (value != null) {
                        sum += toDouble(value);
                    }
                }

                sumResults.put(fullMatch, sum);
            }

            // 3. 替换表达式中的所有SUM(...)为计算结果
            String processedExpression = expression;
            for (Map.Entry<String, Double> entry : sumResults.entrySet()) {
                processedExpression = processedExpression.replace(entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 4. 替换communityCount为实际的社区数量
            processedExpression = processedExpression.replace("communityCount", String.valueOf(communityCount));

            // 5. 使用QLExpress计算最终结果
            Object result = qlExpressService.execute(processedExpression, new HashMap<>());

            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }

            return 0.0;
        } catch (Exception e) {
            log.error("计算聚合表达式失败: expression={}, error={}", expression, e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * 根据首个社区的区划代码推导乡镇区划代码。
     *  - 若社区代码为12位数字，则取前9位并补“000”得到乡镇12位代码。
     *  - 若长度不足12但为纯数字，则返回原值。
     *  - 其它情况返回以“TOWNSHIP_”前缀的备用值，避免 Null。
     *
     * @param firstCommunityCode 社区行政区划代码
     * @param townshipName       乡镇名称（用于回退）
     * @return 乡镇级区划代码或带前缀的备用值
     */
    private String deriveTownshipRegionCode(String firstCommunityCode, String townshipName) {
        if (firstCommunityCode != null) {
            String digitsOnly = firstCommunityCode.trim();
            if (digitsOnly.matches("\\d+")) {
                if (digitsOnly.length() >= 12) {
                    // 取前9位 + 000 形成乡镇层级的12位代码
                    String prefix = digitsOnly.substring(0, 9);
                    return prefix + "000";
                }
                // 非12位但全部为数字，直接返回
                return digitsOnly;
            }
        }
        return "TOWNSHIP_" + (townshipName == null ? "UNKNOWN" : townshipName);
    }

    /**
     * 将对象转换为Double
     */
    private Double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * 保存执行记录和评估结果
     *
     * @param modelId 模型ID
     * @param modelName 模型名称
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param stepResults 步骤执行结果
     * @param tableData 二维表数据
     * @return 执行记录ID
     */
    private Long saveExecutionRecordAndResults(
            Long modelId,
            String modelName,
            List<String> regionCodes,
            Long weightConfigId,
            Map<String, Object> stepResults,
            List<Map<String, Object>> tableData) {

        try {
            java.time.LocalDateTime startTime = java.time.LocalDateTime.now();

            // 1. 创建执行记录
            ModelExecutionRecord executionRecord = new ModelExecutionRecord();
            executionRecord.setModelId(modelId);
            executionRecord.setExecutionCode("EXEC_" + System.currentTimeMillis());
            executionRecord.setRegionIds(String.join(",", regionCodes));
            executionRecord.setWeightConfigId(weightConfigId);
            executionRecord.setExecutionStatus("SUCCESS");
            executionRecord.setStartTime(startTime);
            executionRecord.setEndTime(java.time.LocalDateTime.now());

            // 生成结果摘要
            StringBuilder summary = new StringBuilder();
            summary.append("模型: ").append(modelName).append("; ");
            summary.append("地区数: ").append(regionCodes.size()).append("; ");
            summary.append("评估结果数: ").append(tableData.size());
            executionRecord.setResultSummary(summary.toString());

            // 保存执行记录
            modelExecutionRecordMapper.insert(executionRecord);
            Long executionRecordId = executionRecord.getId();

            // 2. 从stepResults的最后一步提取评估结果（8个字段：4个评分+4个级别）
            List<EvaluationResult> evaluationResults = extractEvaluationResults(
                    modelId, executionRecordId, stepResults, tableData);

            // 批量保存评估结果
            if (!evaluationResults.isEmpty()) {
                for (EvaluationResult result : evaluationResults) {
                    evaluationResultMapper.insert(result);
                }
            }

            return executionRecordId;

        } catch (Exception e) {
            throw new RuntimeException("保存执行记录和评估结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从stepResults中提取评估结果
     * 直接从stepResults的最后一步中提取数据，使用输出参数名（output_param）
     */
    private List<EvaluationResult> extractEvaluationResults(
            Long modelId,
            Long executionRecordId,
            Map<String, Object> stepResults,
            List<Map<String, Object>> tableData) {

        Map<String, Object> finalStepResult = null;
        Integer finalStepOrder = null;

        for (Map.Entry<String, Object> entry : stepResults.entrySet()) {
            Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
            Integer stepOrder = null;
            Object orderObj = stepResult.get("stepOrder");
            if (orderObj instanceof Number) {
                stepOrder = ((Number) orderObj).intValue();
            } else if (orderObj instanceof String) {
                try {
                    stepOrder = Integer.parseInt((String) orderObj);
                } catch (NumberFormatException ignore) {
                }
            }

            if (stepOrder == null) {
                continue;
            }

            if (finalStepResult == null || (finalStepOrder != null && stepOrder > finalStepOrder)) {
                finalStepResult = stepResult;
                finalStepOrder = stepOrder;
            }
        }

        if (finalStepResult == null) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> regionOutputs =
                (Map<String, Map<String, Object>>) finalStepResult.get("regionResults");
        if (regionOutputs == null || regionOutputs.isEmpty()) {
            return Collections.emptyList();
        }

        List<EvaluationResult> results = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : regionOutputs.entrySet()) {
            String stepRegionCode = entry.getKey();
            Map<String, Object> outputs = entry.getValue();
            if (outputs == null) {
                continue;
            }

            String firstCommunityCode = toString(outputs.get("_firstCommunityCode"));
            String lookupRegionCode = firstCommunityCode != null ? firstCommunityCode : stepRegionCode;

            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", lookupRegionCode);
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);

            String townshipName = communityData != null ? communityData.getTownshipName() : null;
            String communityName = communityData != null ? communityData.getCommunityName() : null;

            String storedRegionCode;
            String storedRegionName;
            String dataSource;

            if (firstCommunityCode != null) {
                storedRegionCode = deriveTownshipCodeForStorage(firstCommunityCode);
                if (!isEmptyString(townshipName)) {
                    storedRegionName = townshipName;
                } else {
                    storedRegionName = getTownshipNameByCommunityCode(firstCommunityCode);
                    if (isEmptyString(storedRegionName)) {
                        storedRegionName = getRegionName(stepRegionCode);
                    }
                }
                dataSource = "township";
            } else {
                storedRegionCode = deriveTownshipCodeForStorage(stepRegionCode);
                if (townshipName != null && !townshipName.isEmpty()) {
                    storedRegionName = townshipName;
                } else if (communityName != null && !communityName.isEmpty()) {
                    storedRegionName = communityName;
                } else {
                    storedRegionName = getRegionName(stepRegionCode);
                }
                dataSource = "township";
            }

            if ((storedRegionName == null || storedRegionName.equals(storedRegionCode)) && storedRegionCode != null) {
                String resolvedCommunity = resolveCommunityNameByPrefix(storedRegionCode);
                if (resolvedCommunity != null && !resolvedCommunity.isEmpty()) {
                    storedRegionName = resolvedCommunity;
                } else {
                    String resolvedTownship = resolveTownshipNameByPrefix(storedRegionCode);
                    if (resolvedTownship != null && !resolvedTownship.isEmpty()) {
                        storedRegionName = resolvedTownship;
                    }
                }
            }
            if (isCodeLike(storedRegionName)) {
                String fetched = getTownshipNameByCommunityCode(firstCommunityCode != null ? firstCommunityCode : storedRegionCode);
                if (!isEmptyString(fetched)) {
                    storedRegionName = fetched;
                }
            }

            EvaluationResult result = new EvaluationResult();
            result.setRegionCode(storedRegionCode);
            result.setRegionName(storedRegionName);
            result.setEvaluationModelId(modelId);
            result.setDataSource(dataSource);
            result.setExecutionRecordId(executionRecordId);

            result.setManagementCapabilityScore(
                    getDecimalValueFromMap(outputs,
                            "management_capability_score",
                            "managementCapabilityScore",
                            "disasterMgmtScore",
                            "disaster_mgmt_score"));
            result.setSupportCapabilityScore(
                    getDecimalValueFromMap(outputs,
                            "support_capability_score",
                            "supportCapabilityScore",
                            "disasterPrepScore",
                            "disaster_prep_score"));
            result.setSelfRescueCapabilityScore(
                    getDecimalValueFromMap(outputs,
                            "self_rescue_capability_score",
                            "selfRescueCapabilityScore",
                            "selfRescueScore",
                            "self_rescue_score"));
            result.setComprehensiveCapabilityScore(
                    getDecimalValueFromMap(outputs,
                            "comprehensive_capability_score",
                            "comprehensiveCapabilityScore",
                            "comprehensiveScore",
                            "comprehensive_score"));

            result.setManagementCapabilityLevel(
                    getStringValueFromMap(outputs,
                            "management_capability_level",
                            "managementCapabilityLevel",
                            "disasterMgmtGrade",
                            "disaster_mgmt_grade"));
            result.setSupportCapabilityLevel(
                    getStringValueFromMap(outputs,
                            "support_capability_level",
                            "supportCapabilityLevel",
                            "disasterPrepGrade",
                            "disaster_prep_grade"));
            result.setSelfRescueCapabilityLevel(
                    getStringValueFromMap(outputs,
                            "self_rescue_capability_level",
                            "selfRescueCapabilityLevel",
                            "selfRescueGrade",
                            "self_rescue_grade"));
            result.setComprehensiveCapabilityLevel(
                    getStringValueFromMap(outputs,
                            "comprehensive_capability_level",
                            "comprehensiveCapabilityLevel",
                            "comprehensiveGrade",
                            "comprehensive_grade"));

            results.add(result);
        }

        return results;
    }

    /**
     * 获取地区名称
     */
    private String getRegionName(String regionCode) {
        // 检查是否是乡镇虚拟代码
        if (regionCode.startsWith("TOWNSHIP_")) {
            return regionCode.substring("TOWNSHIP_".length());
        }

        // 尝试从community表获取
        QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
        communityQuery.eq("region_code", regionCode);
        CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
        if (communityData != null) {
            if (communityData.getCommunityName() != null) {
                return communityData.getCommunityName();
            } else if (communityData.getTownshipName() != null) {
                return communityData.getTownshipName();
            }
        }

        // 尝试从survey_data表获取
        QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
        surveyQuery.eq("region_code", regionCode);
        SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
        if (surveyData != null && surveyData.getTownship() != null) {
            return surveyData.getTownship();
        }

        return regionCode;
    }

    private String resolveTownshipNameByPrefix(String regionCodePrefix) {
        if (regionCodePrefix == null) {
            return null;
        }
        String digits = regionCodePrefix.trim();
        if (!digits.matches("\\d+")) {
            return null;
        }

        QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
        communityQuery.likeRight("region_code", digits);
        communityQuery.last("LIMIT 1");
        CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
        if (communityData != null && communityData.getTownshipName() != null) {
            return communityData.getTownshipName();
        }

        QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
        surveyQuery.likeRight("region_code", digits);
        surveyQuery.last("LIMIT 1");
        SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
        if (surveyData != null && surveyData.getTownship() != null) {
            return surveyData.getTownship();
        }

        return null;
    }

    private String resolveCommunityNameByPrefix(String regionCodePrefix) {
        if (regionCodePrefix == null) {
            return null;
        }
        String digits = regionCodePrefix.trim();
        if (!digits.matches("\\d+")) {
            return null;
        }

        QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
        communityQuery.likeRight("region_code", digits);
        communityQuery.last("LIMIT 1");
        CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
        if (communityData != null && communityData.getCommunityName() != null) {
            return communityData.getCommunityName();
        }

        return null;
    }

    private String getTownshipNameByCommunityCode(String communityRegionCode) {
        if (communityRegionCode == null || communityRegionCode.trim().isEmpty()) {
            return null;
        }
        String normalized = communityRegionCode.trim();

        QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
        communityQuery.eq("region_code", normalized);
        communityQuery.last("LIMIT 1");
        CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
        if (communityData == null && normalized.matches("\\d{1,9}")) {
            communityQuery = new QueryWrapper<>();
            communityQuery.likeRight("region_code", normalized);
            communityQuery.last("LIMIT 1");
            communityData = communityDataMapper.selectOne(communityQuery);
        }
        if (communityData != null && !isEmptyString(communityData.getTownshipName())) {
            return communityData.getTownshipName();
        }

        QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
        surveyQuery.eq("region_code", normalized);
        surveyQuery.last("LIMIT 1");
        SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
        if (surveyData == null && normalized.matches("\\d{1,9}")) {
            surveyQuery = new QueryWrapper<>();
            surveyQuery.likeRight("region_code", normalized);
            surveyQuery.last("LIMIT 1");
            surveyData = surveyDataMapper.selectOne(surveyQuery);
        }
        if (surveyData != null && !isEmptyString(surveyData.getTownship())) {
            return surveyData.getTownship();
        }
        return null;
    }

    private boolean isCodeLike(String value) {
        return value != null && value.matches("\\d+");
    }

    private boolean isEmptyString(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 从Map中获取BigDecimal值
     */
    private java.math.BigDecimal getDecimalValueFromMap(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return toBigDecimal(value);
            }
        }
        return null;
    }

    /**
     * 从Map中获取String值
     */
    private String getStringValueFromMap(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return toString(value);
            }
        }
        return null;
    }


    /**
     * 将对象转换为BigDecimal
     */
    private java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.math.BigDecimal) {
            return (java.math.BigDecimal) value;
        }
        if (value instanceof Number) {
            return java.math.BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new java.math.BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 将对象转换为String
     */
    private String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private String deriveTownshipCodeForStorage(String communityRegionCode) {
        if (communityRegionCode == null) {
            return null;
        }
        String digitsOnly = communityRegionCode.trim();
        if (digitsOnly.matches("\\d+")) {
            if (digitsOnly.length() >= 9) {
                return digitsOnly.substring(0, 9);
            }
            return digitsOnly;
        }
        return digitsOnly;
    }

    private String getBaseColumnLabel(String columnName) {
        switch (columnName) {
            case "regionCode":
                return "地区代码";
            case "regionName":
            case "region":
                return "地区名称";
            case "townshipName":
                return "乡镇名称";
            case "communityName":
                return "社区名称";
            default:
                return columnName;
        }
    }

    private String resolveColumnLabel(String columnName, String stepCode, Map<String, String> outputNameMap) {
        if (outputNameMap == null || outputNameMap.isEmpty()) {
            return columnName;
        }

        String label = outputNameMap.get(columnName);
        if (label != null && !label.isEmpty()) {
            return label;
        }

        for (Map.Entry<String, String> entry : outputNameMap.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            if (columnName.equalsIgnoreCase(key)) {
                return entry.getValue();
            }
            if (stepCode != null && columnName.equalsIgnoreCase(stepCode + "_" + key)) {
                return entry.getValue();
            }
        }

        return columnName;
    }
}
