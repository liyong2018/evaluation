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
 * 模型执行服务实现�?
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行评估模型
     * 
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 执行结果（包含每个步骤的输出�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId) {
        log.info("开始执行评估模�? modelId={}, regionCodes={}, weightConfigId={}", 
                modelId, regionCodes, weightConfigId);

        // 1. 验证模型是否存在且启�?
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        if (model == null || model.getStatus() == 0) {
            throw new RuntimeException("评估模型不存在或已禁�?);
        }

        // 2. 获取模型的所有步骤并按顺序排�?
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("status", 1)
                .orderByAsc("step_order");
        List<ModelStep> steps = modelStepMapper.selectList(stepQuery);
        
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("该模型没有配置步�?);
        }

        // 3. 初始化全局上下文（存储所有步骤的执行结果�?
        Map<String, Object> globalContext = new HashMap<>();
        globalContext.put("modelId", modelId);
        globalContext.put("modelName", model.getModelName());
        globalContext.put("regionCodes", regionCodes);
        globalContext.put("weightConfigId", weightConfigId);

        // 4. 加载基础数据到上下文
        loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

        // 5. 按顺序执行每个步�?
        Map<String, Object> stepResults = new HashMap<>();
        Map<Integer, List<String>> stepOutputParams = new LinkedHashMap<>();  // 记录每个步骤的输出参数名�?
        List<String> currentRegionCodes = new ArrayList<>(regionCodes);  // 当前使用的地区代码列�?
        
        for (ModelStep step : steps) {
            log.info("执行步骤: {} - {}, order={}", step.getStepCode(), step.getStepName(), step.getStepOrder());
            
            try {
                Map<String, Object> stepResult;
                
                // 特殊处理：如果是AGGREGATION类型且modelId=8，执行乡镇聚�?
                if ("AGGREGATION".equals(step.getStepType()) && modelId == 8) {
                    log.info("检测到乡镇聚合步骤，执行按乡镇分组聚合");
                    stepResult = executeTownshipAggregation(step.getId(), currentRegionCodes, globalContext);
                    
                    // 更新regionCodes为乡镇代码列表（用于后续步骤�?
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepResult.get("regionResults");
                    if (regionResults != null) {
                        currentRegionCodes = new ArrayList<>(regionResults.keySet());
                        log.info("乡镇聚合后，更新regionCodes为乡镇代码列�? {}", currentRegionCodes);
                    }
                } else {
                    // 执行单个步骤
                    stepResult = executeStep(step.getId(), currentRegionCodes, globalContext);
                }
                
                stepResults.put(step.getStepCode(), stepResult);
                
                // 记录该步骤的输出参数（用于后面生�?columns�?
                @SuppressWarnings("unchecked")
                Map<String, String> outputToAlgorithmName = 
                        (Map<String, String>) stepResult.get("outputToAlgorithmName");
                if (outputToAlgorithmName != null) {
                    stepOutputParams.put(step.getStepOrder(), new ArrayList<>(outputToAlgorithmName.values()));
                    log.debug("步骤{} 的输出参�? {}", step.getStepOrder(), outputToAlgorithmName.values());
                }
                
                // 将步骤结果合并到全局上下文（供后续步骤使用）
                globalContext.put("step_" + step.getStepCode(), stepResult);
                
                log.info("步骤 {} 执行完成", step.getStepCode());
            } catch (Exception e) {
                log.error("步骤 {} 执行失败: {}", step.getStepCode(), e.getMessage(), e);
                throw new RuntimeException("步骤 " + step.getStepName() + " 执行失败: " + e.getMessage(), e);
            }
        }

        // 生成二维表数�?
        List<Map<String, Object>> tableData = generateResultTable(
                Collections.singletonMap("stepResults", stepResults));
        
        // 生成 columns 数组（包含所有步骤的 stepOrder 信息�?
        List<Map<String, Object>> columns = generateColumnsWithAllStepsV2(tableData, stepOutputParams);

        // 6. 构建最终结�?
        Map<String, Object> result = new HashMap<>();
        result.put("modelId", modelId);
        result.put("modelName", model.getModelName());
        result.put("executionTime", new Date());
        result.put("stepResults", stepResults);
        result.put("tableData", tableData);
        result.put("columns", columns);
        result.put("success", true);

        log.info("评估模型执行完成");
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
        log.info("执行步骤, stepId={}", stepId);

        // 1. 获取步骤信息
        ModelStep step = modelStepMapper.selectById(stepId);
        if (step == null || step.getStatus() == 0) {
            throw new RuntimeException("步骤不存在或已禁�?);
        }

        // 2. 获取该步骤的所有算法并按顺序排�?
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);

        if (algorithms == null || algorithms.isEmpty()) {
            log.warn("步骤 {} 没有配置算法", step.getStepCode());
            return new HashMap<>();
        }

        // 3. 初始化步骤结�?
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());

        // 4. 第一遍：为所有地区准备上下文数据
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();

        // 获取modelId以决定使用哪个数据源
        Long modelId = (Long) inputData.get("modelId");

        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(inputData);
            regionContext.put("currentRegionCode", regionCode);

            // 根据modelId选择不同的数据源
            if (modelId != null && (modelId == 4 || modelId == 8)) {
                // 社区模型(modelId=4)和社�?乡镇模型(modelId=8)：从community_disaster_reduction_capacity表加载数�?
                // 使用selectMaps直接返回Map，key为数据库字段名，可直接匹配算法表达式中的变量�?
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                if (communityDataList != null && !communityDataList.isEmpty()) {
                    Map<String, Object> communityDataMap = communityDataList.get(0);
                    // 直接将数据库字段添加到上下文，同时处理数值类型转�?
                    addMapDataToContext(regionContext, communityDataMap);
                } else {
                    log.warn("未找到社区数�? regionCode={}", regionCode);
                }
            } else {
                // 乡镇模型(modelId=3)：从survey_data表加载数�?
                QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
                dataQuery.eq("region_code", regionCode);
                SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);

                if (surveyData != null) {
                    addSurveyDataToContext(regionContext, surveyData);
                } else {
                    log.warn("未找到调查数�? regionCode={}", regionCode);
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
        
        log.info("算法分组: 非GRADE算法={}, GRADE算法={}", nonGradeAlgorithms.size(), gradeAlgorithms.size());
        
        // 6. 第二遍：为每个地区执行非GRADE算法（支持特殊标记）
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            log.info("为地�?{} 执行非GRADE算法", regionCode);
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> algorithmOutputs = new LinkedHashMap<>();
            
            // 执行非GRADE算法
            for (StepAlgorithm algorithm : nonGradeAlgorithms) {
                try {
                    log.debug("执行算法: {} - {}", algorithm.getAlgorithmCode(), algorithm.getAlgorithmName());
                    
                    Object result;
                    String qlExpression = algorithm.getQlExpression();
                    
                    // 检查是否是特殊标记
                    if (qlExpression != null && qlExpression.startsWith("@")) {
                        // 解析特殊标记: @MARKER:params
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        log.info("执行特殊标记算法: marker={}, params={}", marker, params);
                        
                        // 调用特殊算法服务
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 确保数值类型转换并格式化为8位小�?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        // 执行标准QLExpress表达�?
                        result = qlExpressService.execute(qlExpression, regionContext);
                        
                        // 确保数值类型的结果转换为Double并格式化�?位小�?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    }
                    
                    // 保存算法输出到上下文（供后续算法使用�?
                    String outputParam = algorithm.getOutputParam();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        regionContext.put(outputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 更新全局上下�?
                        algorithmOutputs.put(outputParam, result);
                        outputToAlgorithmName.put(outputParam, algorithm.getAlgorithmName());
                    }
                    
                    log.debug("算法 {} 执行结果: {}", algorithm.getAlgorithmCode(), result);
                } catch (Exception e) {
                    log.error("算法 {} 执行失败: {}", algorithm.getAlgorithmCode(), e.getMessage(), e);
                    throw new RuntimeException("算法 " + algorithm.getAlgorithmName() + " 执行失败: " + e.getMessage(), e);
                }
            }
            
            regionResults.put(regionCode, algorithmOutputs);
        }
        
        // 7. 第三遍：为每个地区执行GRADE算法（此时所有地区的分数已计算完成）
        if (!gradeAlgorithms.isEmpty()) {
            log.info("开始执行GRADE算法，此时所有地区的分数已计算完�?);
            
            for (String regionCode : regionCodes) {
                log.info("为地�?{} 执行GRADE算法", regionCode);
                Map<String, Object> regionContext = allRegionContexts.get(regionCode);
                Map<String, Object> algorithmOutputs = regionResults.get(regionCode);
                
                for (StepAlgorithm algorithm : gradeAlgorithms) {
                    try {
                        log.debug("执行GRADE算法: {} - {}", algorithm.getAlgorithmCode(), algorithm.getAlgorithmName());
                        
                        String qlExpression = algorithm.getQlExpression();
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        log.info("执行特殊标记算法: marker={}, params={}", marker, params);
                        
                        // 调用特殊算法服务
                        Object result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 格式化GRADE算法结果�?位小�?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }

                        // 保存算法输出到上下文（供后续算法使用�?
                        String outputParam = algorithm.getOutputParam();
                        if (outputParam != null && !outputParam.isEmpty()) {
                            regionContext.put(outputParam, result);
                            allRegionContexts.put(regionCode, regionContext);  // 更新全局上下�?
                            algorithmOutputs.put(outputParam, result);
                            outputToAlgorithmName.put(outputParam, algorithm.getAlgorithmName());
                        }
                        
                        log.debug("GRADE算法 {} 执行结果: {}", algorithm.getAlgorithmCode(), result);
                    } catch (Exception e) {
                        log.error("GRADE算法 {} 执行失败: {}", algorithm.getAlgorithmCode(), e.getMessage(), e);
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
     * 生成结果二维�?
     * 
     * @param executionResults 执行结果
     * @return 二维表数�?
     */
    @Override
    public List<Map<String, Object>> generateResultTable(Map<String, Object> executionResults) {
        log.info("生成结果二维�?);

        List<Map<String, Object>> tableData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> stepResults = 
                (Map<String, Map<String, Object>>) executionResults.get("stepResults");

        if (stepResults == null || stepResults.isEmpty()) {
            return tableData;
        }

        // 收集所有地区代码和输出变量，以及输出参数到算法名称的映�?
        Set<String> allRegions = new LinkedHashSet<>();
        Set<String> allOutputs = new LinkedHashSet<>();
        Map<String, String> globalOutputToAlgorithmName = new LinkedHashMap<>();  // 全局的输出参数到算法名称映射

        for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> regionResults = 
                    (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");
            
            // 获取输出参数到算法名称的映射
            @SuppressWarnings("unchecked")
            Map<String, String> outputToAlgorithmName = 
                    (Map<String, String>) stepEntry.getValue().get("outputToAlgorithmName");
            if (outputToAlgorithmName != null) {
                globalOutputToAlgorithmName.putAll(outputToAlgorithmName);
            }
            
            if (regionResults != null) {
                allRegions.addAll(regionResults.keySet());
                
                for (Map<String, Object> outputs : regionResults.values()) {
                    allOutputs.addAll(outputs.keySet());
                }
            }
        }

        // 为每个地区生成一行数�?
        for (String regionCode : allRegions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("_rawRegionCode", regionCode);
            row.put("regionCode", regionCode);
            
            // 获取地区名称和乡镇名�?
            String regionName = regionCode;
            String townshipName = null;
            String communityName = null;
            
            // 检查是否是乡镇虚拟代码（以"TOWNSHIP_"开头）
            if (regionCode.startsWith("TOWNSHIP_")) {
                // 这是乡镇聚合后的虚拟代码
                townshipName = regionCode.substring("TOWNSHIP_".length());
                regionName = townshipName;
                // 乡镇行的 regionCode 直接展示中文名称，避免显�?TOWNSHIP_ 前缀
                row.put("regionCode", regionName);
                
                // 从步骤结果中获取保存的乡镇信�?
                for (Map.Entry<String, Map<String, Object>> stepEntry : stepResults.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> regionResults = 
                            (Map<String, Map<String, Object>>) stepEntry.getValue().get("regionResults");
                    if (regionResults != null && regionResults.containsKey(regionCode)) {
                        Map<String, Object> outputs = regionResults.get(regionCode);
                        if (outputs.containsKey("_townshipName")) {
                            townshipName = (String) outputs.get("_townshipName");
                            regionName = townshipName;
                        }
                        if (outputs.containsKey("_firstCommunityCode")) {
                            String firstCommunityCode = (String) outputs.get("_firstCommunityCode");
                            // 可以用第一个社区代码来获取更多信息
                            row.put("_firstCommunityCode", firstCommunityCode);
                        }
                        break;
                    }
                }
                
                log.debug("乡镇虚拟代码 {} 映射�? townshipName={}", regionCode, townshipName);
            } else {
                // 这是普通的社区代码
                // 首先尝试从community_disaster_reduction_capacity表获取社区和乡镇信息
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
                if (communityData != null) {
                    townshipName = communityData.getTownshipName();
                    communityName = communityData.getCommunityName();
                    regionName = communityName != null ? communityName : regionCode;
                } else {
                    // 如果community表中没有找到，尝试从survey_data表获取地区名�?
                    QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                    surveyQuery.eq("region_code", regionCode);
                    SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                    if (surveyData != null && surveyData.getTownship() != null) {
                        regionName = surveyData.getTownship();
                    } else {
                        // 如果都没有找到，使用regionCode作为regionName
                        regionName = regionCode;
                    }
                }
                
                log.debug("地区 {} 映射�? regionName={}, townshipName={}, communityName={}", 
                        regionCode, regionName, townshipName, communityName);
            }
            
            row.put("regionName", regionName);
            // 行级别：社区 or 乡镇，供前端按步骤过滤行
            if (regionCode.startsWith("TOWNSHIP_")) {
                row.put("_regionLevel", "township");
            } else {
                row.put("_regionLevel", "community");
            }
            if (townshipName != null) {
                row.put("townshipName", townshipName);
            }
            if (communityName != null) {
                row.put("communityName", communityName);
            }

            // 收集该地区在所有步骤中的输�?
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
                        
                        // 跳过内部使用的字段（�?_"开头）
                        if (outputParam.startsWith("_")) {
                            continue;
                        }
                        
                        String columnName;
                        
                        // 优先使用算法名称作为列名，如果没有则使用原始�?stepCode_outputParam 格式
                        if (globalOutputToAlgorithmName.containsKey(outputParam)) {
                            columnName = globalOutputToAlgorithmName.get(outputParam);
                        } else {
                            columnName = stepCode + "_" + outputParam;
                        }
                        
                        // 格式化数值为8位小�?
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
        }

        log.info("生成结果二维表完成，�?{} 行数�?, tableData.size());
        return tableData;
    }

    /**
     * 加载基础数据到上下文
     */
    private void loadBaseDataToContext(Map<String, Object> context, List<String> regionCodes, Long weightConfigId) {
        log.debug("加载基础数据到上下文");

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
                log.debug("加载权重: weight_{} = {}", weight.getIndicatorCode(), weightValue);
            }
        }
    }

    /**
     * 加载前面步骤的输出结果到当前区域上下�?
     * �?globalContext 中提取前面步骤的 regionResults，并将当前区域的输出值添加到上下�?
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
                    
                    // 获取当前区域的输�?
                    Map<String, Object> currentRegionOutputs = regionResults.get(regionCode);
                    if (currentRegionOutputs != null) {
                        // 将当前区域的所有输出变量添加到上下�?
                        for (Map.Entry<String, Object> output : currentRegionOutputs.entrySet()) {
                            regionContext.put(output.getKey(), output.getValue());
                            log.debug("从前面步骤加载变�? {}={}", output.getKey(), output.getValue());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 将调查数据添加到上下�?
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
        // 标准化风险评估值：如果值是"�?�?�?�?�?，转换为"�?，以匹配算法表达�?
        String normalizedRiskAssessment = riskAssessmentValue;
        if (riskAssessmentValue != null &&
            (riskAssessmentValue.equals("�?) ||
             riskAssessmentValue.equals("�?) ||
             riskAssessmentValue.equals("�?))) {
            normalizedRiskAssessment = "�?;
        }

        context.put("riskAssessment", normalizedRiskAssessment);
        context.put("risk_assessment", normalizedRiskAssessment);
        context.put("是否开展风险评�?, normalizedRiskAssessment);  // 中文变量�?
        
        // 资金投入（驼峰和下划线两种命名）
        context.put("fundingAmount", surveyData.getFundingAmount());
        context.put("funding_amount", surveyData.getFundingAmount());
        
        // 物资储备（驼峰和下划线两种命名）
        context.put("materialValue", surveyData.getMaterialValue());
        context.put("material_value", surveyData.getMaterialValue());
        
        // 医院床位（驼峰和下划线两种命名）
        context.put("hospitalBeds", surveyData.getHospitalBeds());
        context.put("hospital_beds", surveyData.getHospitalBeds());
        
        // 消防员（驼峰和下划线两种命名�?
        context.put("firefighters", surveyData.getFirefighters());
        
        // 志愿者（驼峰和下划线两种命名�?
        context.put("volunteers", surveyData.getVolunteers());
        
        // 民兵预备役（驼峰和下划线两种命名�?
        context.put("militiaReserve", surveyData.getMilitiaReserve());
        context.put("militia_reserve", surveyData.getMilitiaReserve());
        
        // 培训参与者（驼峰和下划线两种命名�?
        context.put("trainingParticipants", surveyData.getTrainingParticipants());
        context.put("training_participants", surveyData.getTrainingParticipants());
        
        // 避难所容量（驼峰和下划线两种命名）
        context.put("shelterCapacity", surveyData.getShelterCapacity());
        context.put("shelter_capacity", surveyData.getShelterCapacity());
    }

    /**
     * 通用方法：将Map数据添加到上下文
     * 数据库字段名直接作为变量名，无需手动映射
     * 所有数值类型转换为Double，避免整数除法精度丢�?
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

            // 转换数值类型为Double，避免整数除法精度丢�?
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

        log.debug("成功�?{} 个数据库字段添加到上下文", dataMap.size());
    }

    /**
     * 将社区数据添加到上下文（已废弃，使用addMapDataToContext替代�?
     * 所有数值类型转换为Double，避免整数除法精度丢�?
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

        // 人口数据（转换为Double�?
        context.put("population", communityData.getResidentPopulation() != null ? communityData.getResidentPopulation().doubleValue() : 0.0);
        context.put("residentPopulation", communityData.getResidentPopulation() != null ? communityData.getResidentPopulation().doubleValue() : 0.0);

        // 风险评估相关�?个是/否问题）
        context.put("hasEmergencyPlan", communityData.getHasEmergencyPlan());
        context.put("hasVulnerableGroupsList", communityData.getHasVulnerableGroupsList());
        context.put("hasDisasterPointsList", communityData.getHasDisasterPointsList());
        context.put("hasDisasterMap", communityData.getHasDisasterMap());

        // 资金投入（转换为Double�?
        Double fundingAmount = communityData.getLastYearFundingAmount() != null ? communityData.getLastYearFundingAmount().doubleValue() : 0.0;
        context.put("fundingAmount", fundingAmount);
        context.put("funding_amount", fundingAmount);
        context.put("lastYearFundingAmount", fundingAmount);

        // 物资储备（转换为Double�?
        Double materialValue = communityData.getMaterialsEquipmentValue() != null ? communityData.getMaterialsEquipmentValue().doubleValue() : 0.0;
        context.put("materialValue", materialValue);
        context.put("material_value", materialValue);
        context.put("materialsEquipmentValue", materialValue);

        // 医疗服务（转换为Double�?
        Double medicalServiceCount = communityData.getMedicalServiceCount() != null ? communityData.getMedicalServiceCount().doubleValue() : 0.0;
        context.put("medicalServiceCount", medicalServiceCount);
        context.put("medical_service_count", medicalServiceCount);

        // 民兵预备役（转换为Double�?
        Double militiaReserve = communityData.getMilitiaReserveCount() != null ? communityData.getMilitiaReserveCount().doubleValue() : 0.0;
        context.put("militiaReserve", militiaReserve);
        context.put("militia_reserve", militiaReserve);
        context.put("militiaReserveCount", militiaReserve);

        // 志愿者（转换为Double�?
        Double volunteers = communityData.getRegisteredVolunteerCount() != null ? communityData.getRegisteredVolunteerCount().doubleValue() : 0.0;
        context.put("volunteers", volunteers);
        context.put("registeredVolunteerCount", volunteers);

        // 培训参与者（转换为Double�?
        Double trainingParticipants = communityData.getLastYearTrainingParticipants() != null ? communityData.getLastYearTrainingParticipants().doubleValue() : 0.0;
        context.put("trainingParticipants", trainingParticipants);
        context.put("training_participants", trainingParticipants);
        context.put("lastYearTrainingParticipants", trainingParticipants);

        // 演练参与者（转换为Double�?
        Double drillParticipants = communityData.getLastYearDrillParticipants() != null ? communityData.getLastYearDrillParticipants().doubleValue() : 0.0;
        context.put("drillParticipants", drillParticipants);
        context.put("lastYearDrillParticipants", drillParticipants);

        // 避难所容量（转换为Double�?
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
     * @return 步骤执行结果，包�?D表格数据
     */
    @Override
    public Map<String, Object> executeAlgorithmStep(Long algorithmId, Integer stepOrder, List<String> regionCodes, Long weightConfigId) {
        log.info("执行算法步骤, algorithmId={}, stepOrder={}, regionCodes.size={}", algorithmId, stepOrder, regionCodes.size());

        try {
            // 1. 获取算法配置的所有步�?
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            if (algorithmSteps.isEmpty()) {
                throw new RuntimeException("算法配置没有找到任何步骤");
            }

            // 2. 找到指定顺序的步�?
            AlgorithmStep targetStep = algorithmSteps.stream()
                    .filter(step -> stepOrder.equals(step.getStepOrder()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到步骤顺序为 " + stepOrder + " 的算法步�?));

            // 3. 如果不是第一步，需要先执行前面的所有步骤来获取依赖数据
            Map<String, Object> globalContext = new HashMap<>();
            globalContext.put("algorithmId", algorithmId);
            globalContext.put("regionCodes", regionCodes);
            globalContext.put("weightConfigId", weightConfigId);

            // 加载基础数据
            loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

            // 如果不是第一步，执行前面的所有步�?
            if (stepOrder > 1) {
                executeAlgorithmStepsInternalUpTo(algorithmSteps, stepOrder - 1, regionCodes, globalContext);
            }

            // 4. 执行目标步骤
            Map<String, Object> stepExecutionResult = executeAlgorithmStepInternal(targetStep, regionCodes, globalContext);

        // 5. 生成该步骤的2D表格数据
        List<Map<String, Object>> tableData = generateStepResultTable(stepExecutionResult, regionCodes);

        // 生成 columns 数组（包�?stepOrder 信息�?
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

            log.info("算法步骤 {} 执行完成，生�?{} 行表格数�?, stepOrder, tableData.size());
            return result;

        } catch (Exception e) {
            log.error("执行算法步骤失败", e);
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
        log.info("获取算法步骤信息, algorithmId={}", algorithmId);

        try {
            // 获取算法配置
            AlgorithmConfig algorithmConfig = algorithmConfigMapper.selectById(algorithmId);
            if (algorithmConfig == null) {
                throw new RuntimeException("算法配置不存�?);
            }

            // 获取所有步�?
            QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("algorithm_config_id", algorithmId)
                    .eq("status", 1)
                    .orderByAsc("step_order");
            List<AlgorithmStep> algorithmSteps = algorithmStepMapper.selectList(stepQuery);

            // 转换为简化信�?
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

            log.info("获取算法步骤信息完成，共 {} 个步�?, stepsInfo.size());
            return result;

        } catch (Exception e) {
            log.error("获取算法步骤信息失败", e);
            throw new RuntimeException("获取算法步骤信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量执行算法步骤（直到指定步骤）
     *
     * @param algorithmId 算法ID
     * @param upToStepOrder 执行到第几步（包含该步骤�?
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 所有已执行步骤的结�?
     */
    @Override
    public Map<String, Object> executeAlgorithmStepsUpTo(Long algorithmId, Integer upToStepOrder, List<String> regionCodes, Long weightConfigId) {
        log.info("批量执行算法步骤到第{}�? algorithmId={}", upToStepOrder, algorithmId);

        try {
            // 1. 获取算法配置的所有步�?
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
                throw new RuntimeException("未找到步骤顺序为 " + upToStepOrder + " 的算法步�?);
            }

            // 3. 初始化上下文
            Map<String, Object> globalContext = new HashMap<>();
            globalContext.put("algorithmId", algorithmId);
            globalContext.put("regionCodes", regionCodes);
            globalContext.put("weightConfigId", weightConfigId);

            // 加载基础数据
            loadBaseDataToContext(globalContext, regionCodes, weightConfigId);

            // 4. 执行所有步骤直到指定步�?
            Map<String, Object> allStepResults = executeAlgorithmStepsInternalUpTo(algorithmSteps, upToStepOrder, regionCodes, globalContext);

            // 5. 为每个步骤生�?D表格
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

            log.info("批量执行算法步骤完成，执行到第{}�?, upToStepOrder);
            return result;

        } catch (Exception e) {
            log.error("批量执行算法步骤失败", e);
            throw new RuntimeException("批量执行算法步骤失败: " + e.getMessage(), e);
        }
    }

    /**
     * 内部方法：执行算法步骤直到指定顺�?
     */
    private Map<String, Object> executeAlgorithmStepsInternalUpTo(List<AlgorithmStep> algorithmSteps, Integer upToStepOrder, 
                                                                  List<String> regionCodes, Map<String, Object> globalContext) {
        Map<String, Object> stepResults = new HashMap<>();
        
        for (AlgorithmStep algorithmStep : algorithmSteps) {
            if (algorithmStep.getStepOrder() <= upToStepOrder) {
                log.info("执行算法步骤: {} - {}, order={}", algorithmStep.getStepCode(), algorithmStep.getStepName(), algorithmStep.getStepOrder());
                
                try {
                    Map<String, Object> stepResult = executeAlgorithmStepInternal(algorithmStep, regionCodes, globalContext);
                    stepResults.put("step_" + algorithmStep.getStepCode(), stepResult);
                    
                    // 将步骤结果合并到全局上下文（供后续步骤使用）
                    globalContext.put("step_" + algorithmStep.getStepCode(), stepResult);
                    
                    log.info("算法步骤 {} 执行完成", algorithmStep.getStepCode());
                } catch (Exception e) {
                    log.error("算法步骤 {} 执行失败: {}", algorithmStep.getStepCode(), e.getMessage(), e);
                    throw new RuntimeException("算法步骤 " + algorithmStep.getStepName() + " 执行失败: " + e.getMessage(), e);
                }
            }
        }
        
        return stepResults;
    }

    /**
     * 内部方法：执行单个算法步�?
     */
    private Map<String, Object> executeAlgorithmStepInternal(AlgorithmStep algorithmStep, List<String> regionCodes, Map<String, Object> globalContext) {
        // 获取该步骤的所有公式并按顺序排�?
        QueryWrapper<FormulaConfig> formulaQuery = new QueryWrapper<>();
        formulaQuery.eq("algorithm_step_id", algorithmStep.getId().toString())
                .eq("status", 1)
                .orderByAsc("id");
        List<FormulaConfig> formulas = formulaConfigMapper.selectList(formulaQuery);

        if (formulas.isEmpty()) {
            log.warn("算法步骤 {} 没有配置公式", algorithmStep.getStepCode());
            return new HashMap<>();
        }

        // 初始化步骤结�?
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
                // 社区模型(modelId=4)：从community_disaster_reduction_capacity表加载数�?
                // 使用selectMaps直接返回Map，key为数据库字段名，可直接匹配算法表达式中的变量�?
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                if (communityDataList != null && !communityDataList.isEmpty()) {
                    Map<String, Object> communityDataMap = communityDataList.get(0);
                    addMapDataToContext(regionContext, communityDataMap);
                }
            } else {
                // 乡镇模型(modelId=3)：从survey_data表加载数�?
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
        
        // 第二遍：为每个地区执行公式（支持特殊标记�?
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToFormulaName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            log.debug("为地�?{} 执行公式", regionCode);
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> formulaOutputs = new LinkedHashMap<>();
            
            // 按顺序执行每个公�?
            for (FormulaConfig formula : formulas) {
                try {
                    log.debug("执行公式: {} - {}", formula.getFormulaName(), formula.getFormulaExpression());
                    
                    Object result;
                    String expression = formula.getFormulaExpression();
                    
                    // 检查是否是特殊标记
                    if (expression != null && expression.startsWith("@")) {
                        // 解析特殊标记: @MARKER:params
                        String[] parts = expression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";
                        
                        log.info("执行特殊标记公式: marker={}, params={}", marker, params);
                        
                        // 调用特殊算法服务
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);
                        
                        // 确保数值类型转换并格式化为8位小�?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        // 执行标准QLExpress表达�?
                        result = qlExpressService.execute(expression, regionContext);
                        
                        // 确保数值类型的结果转换为Double并格式化�?位小�?
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    }
                    
                    // 保存公式输出到上下文（供后续公式使用�?
                    String outputParam = formula.getOutputVariable();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        regionContext.put(outputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 更新全局上下�?
                        formulaOutputs.put(outputParam, result);
                        outputToFormulaName.put(outputParam, formula.getFormulaName());
                    }
                    
                    log.debug("公式 {} 执行结果: {}", formula.getFormulaName(), result);
                } catch (Exception e) {
                    log.error("公式 {} 执行失败: {}", formula.getFormulaName(), e.getMessage(), e);
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
     * 为单个步骤生�?D表格数据
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
        
        // 为每个地区生成一行数�?
        for (String regionCode : regionCodes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regionCode", regionCode);
            
            // 获取地区名称 - 优先从community表，然后survey_data�?
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
            row.put("regionName", regionName);
            
            // 添加该地区的所有输出结�?
            Map<String, Object> outputs = regionResults.get(regionCode);
            if (outputs != null) {
                for (Map.Entry<String, Object> output : outputs.entrySet()) {
                    String outputParam = output.getKey();
                    String columnName;
                    
                    // 优先使用公式名称作为列名
                    if (outputToFormulaName != null && outputToFormulaName.containsKey(outputParam)) {
                        columnName = outputToFormulaName.get(outputParam);
                    } else {
                        columnName = outputParam;
                    }
                    
                    // 格式化数值为8位小�?
                    Object value = output.getValue();
                    if (value != null && value instanceof Number) {
                        double doubleValue = ((Number) value).doubleValue();
                        value = Double.parseDouble(String.format("%.8f", doubleValue));
                    }
                    row.put(columnName, value);
                }
            }
            
            tableData.add(row);
        }
        
        return tableData;
    }

    /**
     * 从表格数据和步骤输出参数生成 columns 数组，每列标记所属步�?
     * 
     * @param tableData 表格数据
     * @param stepOutputParams 步骤序号 -> 输出参数名称列表的映�?
     * @return columns 数组
     */
    private List<Map<String, Object>> generateColumnsWithAllSteps(
            List<Map<String, Object>> tableData, 
            Map<Integer, List<String>> stepOutputParams) {
        
        List<Map<String, Object>> columns = new ArrayList<>();
        
        if (tableData == null || tableData.isEmpty()) {
            log.debug("表格数据为空，返回空�?columns 数组");
            return columns;
        }
        
        // 从第一行数据提取所有列�?
        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
        
        // 创建反向映射：列�?-> 步骤序号
        Map<String, Integer> columnToStepOrder = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : stepOutputParams.entrySet()) {
            Integer stepOrder = entry.getKey();
            List<String> outputNames = entry.getValue();
            for (String outputName : outputNames) {
                columnToStepOrder.put(outputName, stepOrder);
            }
        }
        
        log.info("开始生�?columns 数组（全模型），总列�? {}", firstRow.size());
        log.debug("列名到步骤序号的映射: {}", columnToStepOrder);
        
        for (String columnName : firstRow.keySet()) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);
            column.put("label", columnName);
            
            // 设置列宽
            if ("regionCode".equals(columnName)) {
                column.put("width", 150);
            } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                column.put("width", 120);
            } else {
                column.put("width", 120);
                // 非基础列添�?stepOrder
                Integer stepOrder = columnToStepOrder.get(columnName);
                if (stepOrder != null) {
                    column.put("stepOrder", stepOrder);
                    log.debug("�?{} 标记为步�?{}", columnName, stepOrder);
                } else {
                    log.warn("�?{} 未找到对应的步骤序号", columnName);
                }
            }
            
            columns.add(column);
        }
        
        log.info("完成 columns 数组生成（全模型），�?{} 列，其中 {} 列包�?stepOrder", 
                columns.size(), columns.stream().filter(c -> c.containsKey("stepOrder")).count());
        
        return columns;
    }

    // 新版：扫描所有行，合并列，再根据 stepOutputParams 反标�?stepOrder，避免首行不包含全部步骤列导致缺�?    private List<Map<String, Object>> generateColumnsWithAllStepsV2(
            List<Map<String, Object>> tableData,
            Map<Integer, List<String>> stepOutputParams) {
        List<Map<String, Object>> columns = new ArrayList<>();
        if (tableData == null || tableData.isEmpty()) {
            return columns;
        }

        // 基础�?        Set<String> baseColumns = new LinkedHashSet<>(Arrays.asList("regionCode", "regionName", "region"));

        // 列到步骤序号
        Map<String, Integer> columnToStepOrder = new HashMap<>();
        for (Map.Entry<Integer, List<String>> e : stepOutputParams.entrySet()) {
            Integer stepOrder = e.getKey();
            for (String name : e.getValue()) {
                columnToStepOrder.put(name, stepOrder);
            }
        }

        // 收集所有列（保留首次出现顺序）
        LinkedHashSet<String> allColumnsOrdered = new LinkedHashSet<>();
        for (Map<String, Object> row : tableData) {
            allColumnsOrdered.addAll(row.keySet());
        }

        for (String columnName : allColumnsOrdered) {
            Map<String, Object> col = new LinkedHashMap<>();
            col.put("prop", columnName);
            col.put("label", columnName);

            if ("regionCode".equals(columnName)) {
                col.put("width", 150);
            } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                col.put("width", 120);
            } else {
                col.put("width", 120);
                Integer stepOrder = columnToStepOrder.get(columnName);
                if (stepOrder != null) {
                    col.put("stepOrder", stepOrder);
                }
            }

            columns.add(col);
        }

        return columns;
    }

    /**
     * 从表格数据生�?columns 数组，并为非基础列添�?stepOrder
     * 
     * @param tableData 表格数据
     * @param stepOrder 当前步骤序号
     * @return columns 数组
     */
    private List<Map<String, Object>> generateColumnsWithStepOrder(
            List<Map<String, Object>> tableData, Integer stepOrder) {
        
        List<Map<String, Object>> columns = new ArrayList<>();
        
        if (tableData == null || tableData.isEmpty()) {
            log.debug("表格数据为空，返回空�?columns 数组");
            return columns;
        }
        
        // 从第一行数据提取所有列�?
        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
        
        log.info("开始生�?columns 数组，步骤序�? {}, 列数: {}", stepOrder, firstRow.size());
        
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
                // 非基础列添�?stepOrder
                column.put("stepOrder", stepOrder);
                log.debug("�?{} 标记为步�?{}", columnName, stepOrder);
            }
            
            columns.add(column);
        }
        
        log.info("完成 columns 数组生成，共 {} 列，其中 {} 列包�?stepOrder", 
                columns.size(), columns.stream().filter(c -> c.containsKey("stepOrder")).count());
        
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
     * @param inputData 输入数据（包含步�?的社区级别计算结果）
     * @return 乡镇级别的聚合结�?
     */
    private Map<String, Object> executeTownshipAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("开始执行乡镇聚�? stepId={}, regionCodes.size={}", stepId, regionCodes.size());
        
        // 1. 获取步骤信息
        ModelStep step = modelStepMapper.selectById(stepId);
        if (step == null || step.getStatus() == 0) {
            throw new RuntimeException("步骤不存在或已禁�?);
        }
        
        // 2. 获取该步骤的所有算�?
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);
        
        if (algorithms == null || algorithms.isEmpty()) {
            log.warn("步骤 {} 没有配置算法", step.getStepCode());
            return new HashMap<>();
        }
        
        // 3. 按乡镇分组收集社区数�?
        Map<String, List<Map<String, Object>>> townshipGroups = new LinkedHashMap<>();
        Map<String, String> townshipToFirstRegionCode = new HashMap<>();  // 记录每个乡镇的第一个社区代码（用于后续步骤�?
        
        for (String regionCode : regionCodes) {
            // 获取社区的乡镇信�?
            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", regionCode);
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
            
            if (communityData == null) {
                log.warn("未找到社区数�? regionCode={}", regionCode);
                continue;
            }
            
            String townshipName = communityData.getTownshipName();
            if (townshipName == null || townshipName.isEmpty()) {
                log.warn("社区 {} 没有乡镇信息", regionCode);
                continue;
            }
            
            // 获取步骤1的输出结果（社区级别的能力值）
            Map<String, Object> communityContext = new HashMap<>();
            communityContext.put("currentRegionCode", regionCode);
            
            // 从inputData中获取步�?的结�?
            // inputData中包�?"step_XXX" 的键，其值是步骤的执行结�?
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
                        log.debug("社区 {} �?{} 加载�?{} 个输�?, regionCode, key, outputs.size());
                    }
                }
            }
            
            // 按乡镇分�?
            townshipGroups.computeIfAbsent(townshipName, k -> new ArrayList<>()).add(communityContext);
            
            // 记录每个乡镇的第一个社区代�?
            townshipToFirstRegionCode.putIfAbsent(townshipName, regionCode);
            
            log.debug("社区 {} 归属乡镇 {}", regionCode, townshipName);
        }
        
        log.info("按乡镇分组完成，�?{} 个乡�?, townshipGroups.size());
        
        // 4. 对每个乡镇执行聚合计�?
        Map<String, Map<String, Object>> townshipResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : townshipGroups.entrySet()) {
            String townshipName = entry.getKey();
            List<Map<String, Object>> communities = entry.getValue();
            int communityCount = communities.size();
            
            log.info("处理乡镇: {}, 社区数量: {}", townshipName, communityCount);
            
            Map<String, Object> townshipOutput = new LinkedHashMap<>();
            
            // 对每个算法执行聚�?
            for (StepAlgorithm algorithm : algorithms) {
                String qlExpression = algorithm.getQlExpression();
                String outputParam = algorithm.getOutputParam();
                
                if (outputParam == null || outputParam.isEmpty()) {
                    continue;
                }
                
                // 从表达式中提取输入字段名（例如：PLAN_CONSTRUCTION�?
                String inputField = qlExpression != null ? qlExpression.trim() : null;
                
                // 计算聚合值：求和后除以社区数�?
                double sum = 0.0;
                int validCount = 0;
                
                for (Map<String, Object> community : communities) {
                    Object value = (inputField != null && !inputField.isEmpty()) ? community.get(inputField) : null;
                    // 当表达式字段不存在时，回退使用该算法的输出参数�?                    if (value == null && outputParam != null && !outputParam.isEmpty()) {
                        value = community.get(outputParam);
                    }
                    if (value != null) {
                        sum += toDouble(value);
                        validCount++;
                    }
                }
                
                // 计算平均�?
                double average = validCount > 0 ? sum / validCount : 0.0;
                
                // 格式化为8位小�?
                average = Double.parseDouble(String.format("%.8f", average));
                
                townshipOutput.put(outputParam, average);
                outputToAlgorithmName.put(outputParam, algorithm.getAlgorithmName());
                
                log.debug("乡镇 {} �?{} 聚合结果: sum={}, count={}, avg={}", 
                        townshipName, outputParam, sum, communityCount, average);
            }
            
            // 使用"TOWNSHIP_"前缀 + 乡镇名称作为虚拟的regionCode
            // 这样可以确保每个乡镇有唯一的标识，且不会与社区代码冲突
            String townshipRegionCode = "TOWNSHIP_" + townshipName;
            townshipResults.put(townshipRegionCode, townshipOutput);
            
            // 同时在上下文中保存乡镇名称，供generateResultTable使用
            townshipOutput.put("_townshipName", townshipName);
            townshipOutput.put("_firstCommunityCode", townshipToFirstRegionCode.get(townshipName));
        }
        
        // 5. 构建步骤结果
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());
        stepResult.put("regionResults", townshipResults);
        stepResult.put("outputToAlgorithmName", outputToAlgorithmName);
        
        log.info("乡镇聚合完成，共 {} 个乡�?, townshipResults.size());
        
        return stepResult;
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
                log.warn("无法将字符串转换为数�? {}", value);
                return 0.0;
            }
        }
        log.warn("无法转换为Double的类�? {}", value.getClass());
        return 0.0;
    }\n    // ����������9������ָ�꣨Ӣ���ֶ����������Բ���1��������ݿ�ԭʼ��
    private Map<String, Double> deriveCommunityIndicators(Map<String, Object> c) {
        Map<String, Double> r = new HashMap<>();
        double pop = nz(number(c, "RESIDENT_POPULATION"), number(c, "resident_population"));
        if (pop <= 0) pop = 1.0;

        double hasPlan = nz(number(c, "HAS_EMERGENCY_PLAN"), number(c, "has_emergency_plan"));
        double hasVul = nz(number(c, "HAS_VULNERABLE_GROUPS_LIST"), number(c, "has_vulnerable_groups_list"));
        double hasHaz = nz(number(c, "HAS_DISASTER_POINTS_LIST"), number(c, "has_disaster_points_list"));
        double hasMap = nz(number(c, "HAS_DISASTER_MAP"), number(c, "has_disaster_map"));

        double fund = nz(number(c, "LAST_YEAR_FUNDING_AMOUNT"), number(c, "last_year_funding_amount"));
        double material = nz(number(c, "MATERIALS_EQUIPMENT_VALUE"), number(c, "materials_equipment_value"));
        double medical = nz(number(c, "MEDICAL_SERVICE_COUNT"), number(c, "medical_service_count"));
        double militia = nz(number(c, "MILITIA_RESERVE_COUNT"), number(c, "militia_reserve_count"));
        double volunteer = nz(number(c, "REGISTERED_VOLUNTEER_COUNT"), number(c, "registered_volunteer_count"));
        double train = nz(number(c, "LAST_YEAR_TRAINING_PARTICIPANTS"), number(c, "last_year_training_participants"));
        double drill = nz(number(c, "LAST_YEAR_DRILL_PARTICIPANTS"), number(c, "last_year_drill_participants"));
        double shelter = nz(number(c, "EMERGENCY_SHELTER_CAPACITY"), number(c, "emergency_shelter_capacity"));

        double PLAN_CONSTRUCTION = clamp01(hasPlan);
        double HAZARD_INSPECTION = clamp01((hasVul + hasHaz) / 2.0);
        double RISK_ASSESSMENT = clamp01(hasMap);
        double FINANCIAL_INPUT = (fund / pop) * 10000.0;
        double MATERIAL_RESERVE = (material / pop) * 10000.0;
        double MEDICAL_SUPPORT = (medical / pop) * 10000.0;
        double SELF_MUTUAL_AID = ((militia + volunteer) / pop) * 10000.0;
        double PUBLIC_EVACUATION = ((train + drill) / pop) * 100.0;
        double RELOCATION_SHELTER = (shelter / pop);

        r.put("PLAN_CONSTRUCTION", round8(PLAN_CONSTRUCTION));
        r.put("HAZARD_INSPECTION", round8(HAZARD_INSPECTION));
        r.put("RISK_ASSESSMENT", round8(RISK_ASSESSMENT));
        r.put("FINANCIAL_INPUT", round8(FINANCIAL_INPUT));
        r.put("MATERIAL_RESERVE", round8(MATERIAL_RESERVE));
        r.put("MEDICAL_SUPPORT", round8(MEDICAL_SUPPORT));
        r.put("SELF_MUTUAL_AID", round8(SELF_MUTUAL_AID));
        r.put("PUBLIC_EVACUATION", round8(PUBLIC_EVACUATION));
        r.put("RELOCATION_SHELTER", round8(RELOCATION_SHELTER));
        return r;
    }

    private Double number(Map<String, Object> c, String k) {
        Object v = c.get(k);
        return v instanceof Number ? ((Number) v).doubleValue() : null;
    }

    private double nz(Double v1, Double v2) {
        if (v1 != null) return v1;
        if (v2 != null) return v2;
        return 0.0;
    }

    private double clamp01(double v) {
        if (v < 0) return 0.0;
        if (v > 1) return 1.0;
        return v;
    }

    private double round8(double v) {
        return Double.parseDouble(String.format("%.8f", v));
    }\n}\n
