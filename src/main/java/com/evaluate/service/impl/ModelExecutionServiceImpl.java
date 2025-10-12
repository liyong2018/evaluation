package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.*;
import com.evaluate.mapper.*;
import com.evaluate.service.ModelExecutionService;
import com.evaluate.service.QLExpressService;
import com.evaluate.service.SpecialAlgorithmService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private RegionMapper regionMapper;

    @Autowired
    private IndicatorWeightMapper indicatorWeightMapper;

    @Autowired
    private QLExpressService qlExpressService;

    @Autowired
    private SpecialAlgorithmService specialAlgorithmService;

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
        log.info("开始执行评估模型, modelId={}, regionCodes={}, weightConfigId={}", 
                modelId, regionCodes, weightConfigId);

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
        for (ModelStep step : steps) {
            log.info("执行步骤: {} - {}, order={}", step.getStepCode(), step.getStepName(), step.getStepOrder());
            
            try {
                // 执行单个步骤
                Map<String, Object> stepResult = executeStep(step.getId(), regionCodes, globalContext);
                stepResults.put(step.getStepCode(), stepResult);
                
                // 将步骤结果合并到全局上下文（供后续步骤使用）
                globalContext.put("step_" + step.getStepCode(), stepResult);
                
                log.info("步骤 {} 执行完成", step.getStepCode());
            } catch (Exception e) {
                log.error("步骤 {} 执行失败: {}", step.getStepCode(), e.getMessage(), e);
                throw new RuntimeException("步骤 " + step.getStepName() + " 执行失败: " + e.getMessage(), e);
            }
        }

        // 6. 构建最终结果
        Map<String, Object> result = new HashMap<>();
        result.put("modelId", modelId);
        result.put("modelName", model.getModelName());
        result.put("executionTime", new Date());
        result.put("stepResults", stepResults);
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
            throw new RuntimeException("步骤不存在或已禁用");
        }

        // 2. 获取该步骤的所有算法并按顺序排序
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .orderByAsc("algorithm_order");
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);

        if (algorithms == null || algorithms.isEmpty()) {
            log.warn("步骤 {} 没有配置算法", step.getStepCode());
            return new HashMap<>();
        }

        // 3. 初始化步骤结果
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", stepId);
        stepResult.put("stepName", step.getStepName());
        stepResult.put("stepCode", step.getStepCode());

        // 4. 第一遍：为所有地区准备上下文数据
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(inputData);
            regionContext.put("currentRegionCode", regionCode);
            
            // 加载前面步骤的输出结果到当前区域上下文
            loadPreviousStepOutputs(regionContext, regionCode, inputData);
            
            // 获取该地区的调查数据
            QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
            dataQuery.eq("region_code", regionCode);
            SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);
            
            if (surveyData != null) {
                addSurveyDataToContext(regionContext, surveyData);
            }
            
            allRegionContexts.put(regionCode, regionContext);
        }
        
        // 5. 第二遍：为每个地区执行算法（支持特殊标记）
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToAlgorithmName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            log.info("为地区 {} 执行算法", regionCode);
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> algorithmOutputs = new LinkedHashMap<>();
            
            // 按顺序执行每个算法
            for (StepAlgorithm algorithm : algorithms) {
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
                        
                        // 确保数值类型转换
                        if (result != null && result instanceof Number && !(result instanceof Double)) {
                            result = ((Number) result).doubleValue();
                        }
                    } else {
                        // 执行标准QLExpress表达式
                        result = qlExpressService.execute(qlExpression, regionContext);
                        
                        // 确保数值类型的结果转换为Double
                        if (result != null && result instanceof Number && !(result instanceof Double)) {
                            result = ((Number) result).doubleValue();
                        }
                    }
                    
                    // 保存算法输出到上下文（供后续算法使用）
                    String outputParam = algorithm.getOutputParam();
                    if (outputParam != null && !outputParam.isEmpty()) {
                        regionContext.put(outputParam, result);
                        allRegionContexts.put(regionCode, regionContext);  // 更新全局上下文
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
        log.info("生成结果二维表");

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

        // 为每个地区生成一行数据
        for (String regionCode : allRegions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regionCode", regionCode);
            
            // 获取地区名称
            Region region = regionMapper.selectByCode(regionCode);
            String regionName = region != null ? region.getName() : regionCode;
            row.put("regionName", regionName);
            log.debug("地区 {} 映射为: {}", regionCode, regionName);

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
                        String columnName;
                        
                        // 优先使用算法名称作为列名，如果没有则使用原始的 stepCode_outputParam 格式
                        if (globalOutputToAlgorithmName.containsKey(outputParam)) {
                            columnName = globalOutputToAlgorithmName.get(outputParam);
                        } else {
                            columnName = stepCode + "_" + outputParam;
                        }
                        
                        row.put(columnName, output.getValue());
                    }
                }
            }

            tableData.add(row);
        }

        log.info("生成结果二维表完成，共 {} 行数据", tableData.size());
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

        // 加载所有地区信息
        Map<String, Region> regionMap = new HashMap<>();
        for (String regionCode : regionCodes) {
            Region region = regionMapper.selectByCode(regionCode);
            if (region != null) {
                regionMap.put(regionCode, region);
            }
        }
        context.put("regions", regionMap);
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
                            log.debug("从前面步骤加载变量: {}={}", output.getKey(), output.getValue());
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
        context.put("riskAssessment", surveyData.getRiskAssessment());
        context.put("risk_assessment", surveyData.getRiskAssessment());
        
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
        log.info("执行算法步骤, algorithmId={}, stepOrder={}, regionCodes.size={}", algorithmId, stepOrder, regionCodes.size());

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

            // 6. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("stepId", targetStep.getId());
            result.put("stepName", targetStep.getStepName());
            result.put("stepOrder", stepOrder);
            result.put("stepCode", targetStep.getStepCode());
            result.put("description", targetStep.getStepDescription());
            result.put("executionResult", stepExecutionResult);
            result.put("tableData", tableData);
            result.put("success", true);
            result.put("executionTime", new Date());

            log.info("算法步骤 {} 执行完成，生成 {} 行表格数据", stepOrder, tableData.size());
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

            log.info("获取算法步骤信息完成，共 {} 个步骤", stepsInfo.size());
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
     * @param upToStepOrder 执行到第几步（包含该步骤）
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 所有已执行步骤的结果
     */
    @Override
    public Map<String, Object> executeAlgorithmStepsUpTo(Long algorithmId, Integer upToStepOrder, List<String> regionCodes, Long weightConfigId) {
        log.info("批量执行算法步骤到第{}步, algorithmId={}", upToStepOrder, algorithmId);

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

            log.info("批量执行算法步骤完成，执行到第{}步", upToStepOrder);
            return result;

        } catch (Exception e) {
            log.error("批量执行算法步骤失败", e);
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
            log.warn("算法步骤 {} 没有配置公式", algorithmStep.getStepCode());
            return new HashMap<>();
        }

        // 初始化步骤结果
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepId", algorithmStep.getId());
        stepResult.put("stepName", algorithmStep.getStepName());
        stepResult.put("stepCode", algorithmStep.getStepCode());

        // 第一遍：为所有地区准备上下文数据
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(globalContext);
            regionContext.put("currentRegionCode", regionCode);
            
            // 加载前面步骤的输出结果到当前区域上下文
            loadPreviousStepOutputs(regionContext, regionCode, globalContext);
            
            // 获取该地区的调查数据
            QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
            dataQuery.eq("region_code", regionCode);
            SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);
            
            if (surveyData != null) {
                addSurveyDataToContext(regionContext, surveyData);
            }
            
            allRegionContexts.put(regionCode, regionContext);
        }
        
        // 第二遍：为每个地区执行公式（支持特殊标记）
        Map<String, Map<String, Object>> regionResults = new LinkedHashMap<>();
        Map<String, String> outputToFormulaName = new LinkedHashMap<>();
        
        for (String regionCode : regionCodes) {
            log.debug("为地区 {} 执行公式", regionCode);
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> formulaOutputs = new LinkedHashMap<>();
            
            // 按顺序执行每个公式
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
                        
                        // 确保数值类型转换
                        if (result != null && result instanceof Number && !(result instanceof Double)) {
                            result = ((Number) result).doubleValue();
                        }
                    } else {
                        // 执行标准QLExpress表达式
                        result = qlExpressService.execute(expression, regionContext);
                        
                        // 确保数值类型的结果转换为Double
                        if (result != null && result instanceof Number && !(result instanceof Double)) {
                            result = ((Number) result).doubleValue();
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
            
            // 获取地区名称
            Region region = regionMapper.selectByCode(regionCode);
            String regionName = region != null ? region.getName() : regionCode;
            row.put("regionName", regionName);
            
            // 添加该地区的所有输出结果
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
                    
                    row.put(columnName, output.getValue());
                }
            }
            
            tableData.add(row);
        }
        
        return tableData;
    }

    @Autowired
    private AlgorithmStepMapper algorithmStepMapper;
    
    @Autowired
    private AlgorithmConfigMapper algorithmConfigMapper;
    
    @Autowired
    private FormulaConfigMapper formulaConfigMapper;
}
