package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.entity.*;
import com.evaluate.mapper.*;
import com.evaluate.service.IWeightConfigService;
import com.evaluate.service.ModelExecutionService;
import com.evaluate.service.QLExpressService;
import com.evaluate.service.SpecialAlgorithmService;
import com.evaluate.service.ISurveyDataService;
import com.evaluate.service.EvaluationResultService;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private IWeightConfigService weightConfigService;

    @Autowired
    private ModelExecutionRecordMapper modelExecutionRecordMapper;

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;

    @Autowired
    private EvaluationResultService evaluationResultService;

    @Autowired(required = false)
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    @Autowired
    @Qualifier("evaluationTaskExecutor")
    private Executor evaluationTaskExecutor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GOVERNMENT_MODEL_KEYWORD = "政府减灾能力";
    private static final String ENTERPRISE_MODEL_KEYWORD = "企业减灾能力";
    private static final String SOCIAL_ORGANIZATION_MODEL_KEYWORD = "社会组织减灾能力";
    private static final String GOVERNMENT_CAPACITY_TABLE = "government_disaster_reduction_capacity_2020";
    private static final String ENTERPRISE_CAPACITY_TABLE = "enterprise_disaster_reduction_capacity_2020";
    private static final String SOCIAL_ORGANIZATION_CAPACITY_TABLE = "social_organization_disaster_reduction_capacity_2020";
    private static final Pattern TRAILING_NUMERIC_MULTIPLIER = Pattern.compile("(?s)^(.*)\\*\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$");
    private static final Pattern WEIGHT_VAR_PATTERN = Pattern.compile("\\bweight_[A-Z0-9_]+\\b");
    private static final Pattern NORM_VAR_PATTERN = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*(?:Norm|Normalized)\\b");
    private static final String[] DEFAULT_WEIGHT_CODES = new String[] {
            "L1_DISASTER_MANAGEMENT",
            "L1_DISASTER_PREPAREDNESS",
            "L1_SELF_RESCUE_TRANSFER",
            "L1_MANAGEMENT",
            "L1_PREPARATION",
            "L1_SELF_RESCUE",
            "L2_MANAGEMENT_CAPABILITY",
            "L2_RISK_ASSESSMENT",
            "L2_FUNDING",
            "L2_MATERIAL",
            "L2_MEDICAL",
            "L2_SELF_RESCUE",
            "L2_PUBLIC_AVOIDANCE",
            "L2_RELOCATION",
            "L1_TOWNSHIP",
            "L1_COMMUNITY",
            "L2_TOWNSHIP_DISASTER_MANAGEMENT",
            "L2_TOWNSHIP_DISASTER_PREPAREDNESS",
            "L2_TOWNSHIP_SELF_RESCUE_TRANSFER",
            "L2_COMMUNITY_DISASTER_MANAGEMENT",
            "L2_COMMUNITY_DISASTER_PREPAREDNESS",
            "L2_COMMUNITY_SELF_RESCUE_TRANSFER"
    };

    private static final Duration WEIGHT_CACHE_TTL = Duration.ofMinutes(15);
    private static final int RESOLVED_WEIGHT_CONFIG_CACHE_MAX = 500;
    private static final int WEIGHT_MAP_CACHE_MAX = 500;

    private final Object resolvedWeightConfigCacheLock = new Object();
    private final LinkedHashMap<String, TimedValue<Long>> resolvedWeightConfigIdCache =
            new LinkedHashMap<String, TimedValue<Long>>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, TimedValue<Long>> eldest) {
                    return size() > RESOLVED_WEIGHT_CONFIG_CACHE_MAX;
                }
            };

    private final Object weightMapCacheLock = new Object();
    private final LinkedHashMap<Long, TimedValue<Map<String, Double>>> weightMapCache =
            new LinkedHashMap<Long, TimedValue<Map<String, Double>>>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, TimedValue<Map<String, Double>>> eldest) {
                    return size() > WEIGHT_MAP_CACHE_MAX;
                }
            };

    /**
     * 执行评估模型
     *
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param year 评估年份
     * @param orgCode 机构代码
     * @param createBy 操作人
     * @return 执行结果（包含每个步骤的输出）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId, Integer year, String orgCode, String createBy) {
        // 执行评估模型（核心逻辑）
        Map<String, Object> result = executeModelInternal(modelId, regionCodes, weightConfigId, year, orgCode, createBy);
        Long resolvedWeightConfigId = result.get("weightConfigId") instanceof Number
                ? ((Number) result.get("weightConfigId")).longValue()
                : weightConfigId;
        String resolvedOrgCode = result.get("orgCode") instanceof String ? (String) result.get("orgCode") : orgCode;

        // 保存执行记录和评估结果
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tableData = (List<Map<String, Object>>) result.get("tableData");
        @SuppressWarnings("unchecked")
        Map<String, Object> stepResults = (Map<String, Object>) result.get("stepResults");
        List<String> currentRegionCodes = (List<String>) result.get("currentRegionCodes");

        Long executionRecordId = saveExecutionRecordAndResults(
                modelId,
                (String) result.get("modelName"),
                currentRegionCodes,
                resolvedWeightConfigId,
                stepResults,
                tableData,
                year, resolvedOrgCode, createBy);

        result.put("executionRecordId", executionRecordId);
        return result;
    }

    /**
     * 执行评估模型的核心逻辑（不保存执行记录）
     * 用于异步执行，避免重复创建执行记录
     *
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param year 评估年份
     * @param orgCode 机构代码
     * @param createBy 操作人
     * @return 执行结果（包含每个步骤的输出）
     */
    private Map<String, Object> executeModelInternal(Long modelId, List<String> regionCodes, Long weightConfigId, Integer year, String orgCode, String createBy) {
        // 1. 验证模型是否存在且启用
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        if (model == null || model.getStatus() == 0) {
            throw new RuntimeException("评估模型不存在或已禁用");
        }

        String resolvedOrgCode = normalizeOrgCode(orgCode, regionCodes);
        Long resolvedWeightConfigId = resolveWeightConfigIdIfNeeded(modelId, weightConfigId, year, resolvedOrgCode);

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
        boolean governmentModel = isGovernmentModel(modelId, model.getModelName());
        boolean enterpriseModel = isEnterpriseModel(modelId, model.getModelName());
        boolean socialOrganizationModel = isSocialOrganizationModel(modelId, model.getModelName());
        List<String> effectiveRegionCodes;
        if (governmentModel) {
            effectiveRegionCodes = resolveGovernmentRegionCodes(regionCodes, year);
        } else if (enterpriseModel) {
            effectiveRegionCodes = resolveEnterpriseRegionCodes(regionCodes, year);
        } else if (socialOrganizationModel) {
            effectiveRegionCodes = resolveSocialOrganizationRegionCodes(regionCodes, year);
        } else {
            effectiveRegionCodes = new ArrayList<>(regionCodes);
        }
        globalContext.put("regionCodes", effectiveRegionCodes);
        globalContext.put("weightConfigId", resolvedWeightConfigId);
        if (year != null) {
            globalContext.put("year", year);
        }

        // 如果指定了年份，则严格校验该年份是否存在数据，避免错误回退到其它年份
        // 根据评估模型类型检查所需的数据：
        // - Model 3: 乡镇减灾能力评估模型 - 需要乡镇数据
        // - Model 4: 社区-行政村减灾能力评估模型 - 需要社区数据
        // - Model 8: 社区-乡镇减灾能力评估模型 - 需要社区数据
        // - Model 11: 综合减灾能力评估模型 - 需要乡镇减灾能力评估结果(Model 3)和社区-乡镇减灾能力评估结果(Model 8)
        if (year != null) {
            for (String regionCode : effectiveRegionCodes) {
                if (governmentModel) {
                    if (!hasGovernmentData(regionCode, year)) {
                        throw new RuntimeException("所选年份无政府减灾能力数据，无法进行政府减灾能力评估");
                    }
                } else if (enterpriseModel) {
                    if (!hasEnterpriseData(regionCode, year)) {
                        throw new RuntimeException("所选年份无企业减灾能力数据，无法进行企业减灾能力评估");
                    }
                } else if (socialOrganizationModel) {
                    if (!hasSocialOrganizationData(regionCode, year)) {
                        throw new RuntimeException("所选年份无社会组织减灾能力数据，无法进行社会组织减灾能力评估");
                    }
                } else if (modelId == 4 || modelId == 8) {
                    // 社区-行政村/乡镇减灾能力评估模型：检查社区数据
                    QueryWrapper<CommunityDisasterReductionCapacity> q = new QueryWrapper<>();
                    q.eq("region_code", regionCode).eq("year", year);
                    CommunityDisasterReductionCapacity exists = communityDataMapper.selectOne(q);
                    if (exists == null) {
                        throw new RuntimeException("所选年份无社区数据，无法进行社区-行政村/乡镇减灾能力评估");
                    }
                } else if (modelId == 11) {
                    // 综合减灾能力评估模型：需要检查评估历史表中是否存在Model 3和Model 8的评估结果
                    // 检查是否存在乡镇减灾能力评估结果（Model 3）
                    String queryOrgCode = StringUtils.hasText(resolvedOrgCode) ? resolvedOrgCode : "511425";
                    List<EvaluationResult> townshipEvalResults = evaluationResultMapper.selectByModelIdAndYearAndOrgCode(3L, year, queryOrgCode);
                    boolean hasTownshipResult = townshipEvalResults != null && townshipEvalResults.stream()
                            .anyMatch(r -> regionCode.equals(r.getRegionCode()));
                    if (!hasTownshipResult) {
                        throw new RuntimeException("所选年份无乡镇减灾能力评估结果（请先执行乡镇减灾能力评估模型），综合减灾能力评估需要乡镇减灾能力评估结果和社区-乡镇减灾能力评估结果");
                    }

                    // 检查是否存在社区-乡镇减灾能力评估结果（Model 8）
                    List<EvaluationResult> communityEvalResults = evaluationResultMapper.selectByModelIdAndYearAndOrgCode(8L, year, queryOrgCode);
                    boolean hasCommunityResult = communityEvalResults != null && communityEvalResults.stream()
                            .anyMatch(r -> regionCode.equals(r.getRegionCode()));
                    if (!hasCommunityResult) {
                        throw new RuntimeException("所选年份无社区-乡镇减灾能力评估结果（请先执行社区-乡镇减灾能力评估模型），综合减灾能力评估需要乡镇减灾能力评估结果和社区-乡镇减灾能力评估结果");
                    }
                } else {
                    // 默认（Model 3 乡镇减灾能力评估模型等）：检查乡镇数据
                    QueryWrapper<SurveyData> q = new QueryWrapper<>();
                    q.eq("region_code", regionCode).eq("year", year);
                    SurveyData exists = surveyDataMapper.selectOne(q);
                    if (exists == null) {
                        throw new RuntimeException("所选年份无乡镇数据，无法进行乡镇减灾能力评估");
                    }
                }
            }
        }

        // 4. 加载基础数据到上下文
        loadBaseDataToContext(globalContext, effectiveRegionCodes, resolvedWeightConfigId);

        // 5. 按顺序执行每个步骤
        Map<String, Object> stepResults = new HashMap<>();
        Map<Integer, Set<String>> stepOutputParams = new LinkedHashMap<>();  // 记录每个步骤的可能列名称
        List<String> currentRegionCodes = new ArrayList<>(effectiveRegionCodes);  // 当前使用的地区代码列表
        
        for (ModelStep step : steps) {
            
            try {
                Map<String, Object> stepResult;
                
                // 特殊处理：如果是AGGREGATION类型，执行数据聚合
                if ("AGGREGATION".equals(step.getStepType())) {
                    stepResult = executeDataAggregation(step.getId(), currentRegionCodes, globalContext, modelId);
                    
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
                stepResult.put("modelId", modelId);
                
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

            List<Map<String, Object>> stepTableData = generateStepResultTable(rawStepResult, stepRegionCodes, year);
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

        // 6. 构建最终结果（不保存执行记录）
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
        result.put("currentRegionCodes", currentRegionCodes);  // 添加当前地区代码列表
        result.put("weightConfigId", resolvedWeightConfigId);
        if (resolvedOrgCode != null) {
            result.put("orgCode", resolvedOrgCode);
        }

        normalizeGovernmentExecutionResult(result, year);

        return result;
    }

    /**
     * 异步执行评估模型
     * 立即返回执行记录ID，实际计算在后台进行
     *
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param year 评估年份
     * @param orgCode 机构代码
     * @param createBy 操作人
     * @return 执行记录ID
     */
    @Override
    public Long executeModelAsync(Long modelId, List<String> regionCodes, Long weightConfigId, Integer year, String orgCode, String createBy) {
        log.info("开始异步执行评估模型, modelId={}, regionCodes={}, year={}", modelId, regionCodes, year);

        // 1. 验证模型是否存在且启用
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        if (model == null || model.getStatus() == 0) {
            throw new RuntimeException("评估模型不存在或已禁用");
        }

        String resolvedOrgCode = normalizeOrgCode(orgCode, regionCodes);
        Long resolvedWeightConfigId = resolveWeightConfigIdIfNeeded(modelId, weightConfigId, year, resolvedOrgCode);

        // 2. 创建执行记录，状态为 RUNNING
        ModelExecutionRecord executionRecord = new ModelExecutionRecord();
        executionRecord.setModelId(modelId);
        executionRecord.setExecutionCode("EXEC_" + System.currentTimeMillis());
        executionRecord.setRegionIds(String.join(",", regionCodes));
        executionRecord.setWeightConfigId(resolvedWeightConfigId);
        executionRecord.setExecutionStatus(com.evaluate.enums.ExecutionStatus.RUNNING.getCode());
        executionRecord.setStartTime(java.time.LocalDateTime.now());
        if (year != null) {
            executionRecord.setYear(year);
        }
        if (resolvedOrgCode != null && !resolvedOrgCode.trim().isEmpty()) {
            executionRecord.setOrgCode(resolvedOrgCode.trim());
        }
        if (createBy != null && !createBy.trim().isEmpty()) {
            executionRecord.setCreateBy(createBy.trim());
        }

        // 保存执行记录
        modelExecutionRecordMapper.insert(executionRecord);
        Long executionRecordId = executionRecord.getId();

        log.info("创建执行记录成功, executionRecordId={}, status=RUNNING", executionRecordId);

        // 3. 启动异步任务执行评估
        evaluationTaskExecutor.execute(() -> {
            try {
                executeModelAsyncTask(executionRecordId, modelId, model.getModelName(), regionCodes, resolvedWeightConfigId, year, resolvedOrgCode, createBy);
            } catch (Throwable e) {
                // 捕获所有异常，防止导致JVM崩溃
                log.error("评估任务执行异常（已捕获）: executionRecordId={}, error={}", executionRecordId, e.getMessage(), e);
                try {
                    // 更新执行记录状态为 FAILED
                    ModelExecutionRecord record = new ModelExecutionRecord();
                    record.setId(executionRecordId);
                    record.setExecutionStatus("FAILED");
                    record.setErrorMessage(e.getMessage());
                    modelExecutionRecordMapper.updateById(record);
                } catch (Exception ex) {
                    log.error("更新失败状态时出错: executionRecordId={}", executionRecordId, ex);
                }
            }
        });

        return executionRecordId;
    }

    /**
     * 异步执行评估任务
     * 移除 @Async 注解，避免双重异步提交
     *
     * @param executionRecordId 执行记录ID
     * @param modelId 模型ID
     * @param modelName 模型名称
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @param year 评估年份
     * @param orgCode 机构代码
     * @param createBy 操作人
     */
    public void executeModelAsyncTask(Long executionRecordId, Long modelId, String modelName,
                                       List<String> regionCodes, Long weightConfigId,
                                       Integer year, String orgCode, String createBy) {
        log.info("异步任务开始执行, executionRecordId={}, modelId={}", executionRecordId, modelId);

        Map<String, Object> result = null;
        try {
            // 调用内部执行方法（不创建新的执行记录）
            result = executeModelInternal(modelId, regionCodes, weightConfigId, year, orgCode, createBy);

            // 保存评估结果到数据库
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableData = (List<Map<String, Object>>) result.get("tableData");
            @SuppressWarnings("unchecked")
            Map<String, Object> stepResults = (Map<String, Object>) result.get("stepResults");
            List<String> currentRegionCodes = (List<String>) result.get("currentRegionCodes");

            saveEvaluationResults(executionRecordId, modelId, modelName, stepResults, tableData, year, orgCode);

            // 更新执行记录状态为 SUCCESS
            updateExecutionRecordStatus(executionRecordId, com.evaluate.enums.ExecutionStatus.SUCCESS, null, result);
            log.info("异步任务执行成功, executionRecordId={}", executionRecordId);

        } catch (Exception e) {
            log.error("异步任务执行失败, executionRecordId={}, error={}", executionRecordId, e.getMessage(), e);

            // 更新执行记录状态为 FAILED
            updateExecutionRecordStatus(executionRecordId, com.evaluate.enums.ExecutionStatus.FAILED, e.getMessage(), null);
        }
    }

    /**
     * 保存评估结果到数据库（不创建新的执行记录）
     *
     * @param executionRecordId 执行记录ID
     * @param modelId 模型ID
     * @param modelName 模型名称
     * @param stepResults 步骤结果
     * @param tableData 表格数据
     * @param year 年份
     * @param orgCode 机构代码
     */
    private void saveEvaluationResults(Long executionRecordId, Long modelId, String modelName,
                                       Map<String, Object> stepResults, List<Map<String, Object>> tableData,
                                       Integer year, String orgCode) {
        try {
            // 从stepResults中提取评估结果
            List<EvaluationResult> evaluationResults = extractEvaluationResults(
                    modelId, executionRecordId, stepResults, tableData, year, orgCode);

            // 批量保存评估结果
            if (!evaluationResults.isEmpty()) {
                for (EvaluationResult result : evaluationResults) {
                    evaluationResultMapper.insert(result);
                }
            }

            log.info("保存评估结果成功, executionRecordId={}, resultsCount={}", executionRecordId, evaluationResults.size());
        } catch (Exception e) {
            log.error("保存评估结果失败, executionRecordId={}, error={}", executionRecordId, e.getMessage(), e);
            throw new RuntimeException("保存评估结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新执行记录状态
     *
     * @param executionRecordId 执行记录ID
     * @param status 执行状态
     * @param errorMessage 错误信息（失败时）
     * @param result 执行结果（成功时）
     */
    private void updateExecutionRecordStatus(Long executionRecordId, com.evaluate.enums.ExecutionStatus status,
                                             String errorMessage, Map<String, Object> result) {
        try {
            ModelExecutionRecord executionRecord = modelExecutionRecordMapper.selectById(executionRecordId);
            if (executionRecord != null) {
                executionRecord.setExecutionStatus(status.getCode());
                executionRecord.setEndTime(java.time.LocalDateTime.now());

                if (status == com.evaluate.enums.ExecutionStatus.FAILED) {
                    executionRecord.setErrorMessage(errorMessage);
                } else if (status == com.evaluate.enums.ExecutionStatus.SUCCESS && result != null) {
                    // 生成结果摘要
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> tableData = (List<Map<String, Object>>) result.get("tableData");
                    if (tableData != null) {
                        StringBuilder summary = new StringBuilder();
                        summary.append("模型: ").append(result.get("modelName")).append("; ");
                        summary.append("地区数: ").append(executionRecord.getRegionIds().split(",").length).append("; ");
                        summary.append("评估结果数: ").append(tableData.size());
                        executionRecord.setResultSummary(summary.toString());
                    }

                    Map<String, Object> resultDetail = new LinkedHashMap<>();
                    resultDetail.put("modelId", result.get("modelId"));
                    resultDetail.put("modelName", result.get("modelName"));
                    resultDetail.put("isMultiStep", result.get("isMultiStep"));
                    resultDetail.put("stepResultsList", result.get("stepResultsList"));
                    resultDetail.put("columns", result.get("columns"));
                    resultDetail.put("tableData", result.get("tableData"));
                    executionRecord.setResultDetail(objectMapper.writeValueAsString(resultDetail));
                }

                modelExecutionRecordMapper.updateById(executionRecord);
                log.info("更新执行记录状态成功, executionRecordId={}, status={}", executionRecordId, status.getCode());
            }
        } catch (Exception e) {
            log.error("更新执行记录状态失败, executionRecordId={}, error={}", executionRecordId, e.getMessage(), e);
        }
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

        // 年份（如提供则严格匹配该年）
        Integer ctxYear = null;
        Object yearObj = inputData.get("year");
        if (yearObj != null) {
            try { ctxYear = Integer.valueOf(yearObj.toString()); } catch (Exception ignore) {}
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> communityDataMap =
                (Map<String, Map<String, Object>>) inputData.get("communityDataMap");
        @SuppressWarnings("unchecked")
        Map<String, SurveyData> surveyDataMap =
                (Map<String, SurveyData>) inputData.get("surveyDataMap");
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> governmentDataMap =
                (Map<String, Map<String, Object>>) inputData.get("governmentDataMap");
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> enterpriseDataMap =
                (Map<String, Map<String, Object>>) inputData.get("enterpriseDataMap");
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> socialOrganizationDataMap =
                (Map<String, Map<String, Object>>) inputData.get("socialOrganizationDataMap");
        boolean governmentModel = isGovernmentModel(modelId, (String) inputData.get("modelName"));
        boolean enterpriseModel = isEnterpriseModel(modelId, (String) inputData.get("modelName"));
        boolean socialOrganizationModel = isSocialOrganizationModel(modelId, (String) inputData.get("modelName"));

        for (String regionCode : regionCodes) {
            Map<String, Object> regionContext = new HashMap<>(inputData);
            regionContext.put("currentRegionCode", regionCode);

            // 根据modelId选择不同的数据源
            if (governmentModel || enterpriseModel || socialOrganizationModel) {
                Map<String, Map<String, Object>> locationDataMap;
                if (governmentModel) {
                    locationDataMap = governmentDataMap;
                } else if (enterpriseModel) {
                    locationDataMap = enterpriseDataMap;
                } else {
                    locationDataMap = socialOrganizationDataMap;
                }
                Map<String, Object> cachedLocation = locationDataMap != null ? locationDataMap.get(regionCode) : null;
                if (cachedLocation != null) {
                    addMapDataToContext(regionContext, cachedLocation);
                } else {
                    List<Map<String, Object>> locationRows;
                    if (governmentModel) {
                        locationRows = queryGovernmentRows(Collections.singletonList(regionCode), ctxYear);
                    } else if (enterpriseModel) {
                        locationRows = queryEnterpriseRows(Collections.singletonList(regionCode), ctxYear);
                    } else {
                        locationRows = querySocialOrganizationRows(Collections.singletonList(regionCode), ctxYear);
                    }
                    if (locationRows != null && !locationRows.isEmpty()) {
                        addMapDataToContext(regionContext, locationRows.get(0));
                    }
                }
            } else if (modelId != null && (modelId == 4 || modelId == 8)) {
                // 社区模型(modelId=4)和社区-乡镇模型(modelId=8)：从community_disaster_reduction_capacity表加载数据
                // 使用selectMaps直接返回Map，key为数据库字段名，可直接匹配算法表达式中的变量名
                Map<String, Object> cachedCommunity = communityDataMap != null ? communityDataMap.get(regionCode) : null;
                if (cachedCommunity != null) {
                    addMapDataToContext(regionContext, cachedCommunity);
                } else {
                    QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                    communityQuery.eq("region_code", regionCode);
                    if (ctxYear != null) {
                        communityQuery.eq("year", ctxYear);
                    } else {
                        communityQuery.orderByDesc("year");
                    }
                    communityQuery.last("LIMIT 1");
                    List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(communityQuery);

                    if (communityDataList != null && !communityDataList.isEmpty()) {
                        Map<String, Object> communityDataRow = communityDataList.get(0);
                        addMapDataToContext(regionContext, communityDataRow);
                    }
                }
            } else {
                // 乡镇模型(modelId=3)：从survey_data表加载数据
                SurveyData cachedSurvey = surveyDataMap != null ? surveyDataMap.get(regionCode) : null;
                if (cachedSurvey != null) {
                    addSurveyDataToContext(regionContext, cachedSurvey);
                } else {
                    QueryWrapper<SurveyData> dataQuery = new QueryWrapper<>();
                    dataQuery.eq("region_code", regionCode);
                    if (ctxYear != null) {
                        dataQuery.eq("year", ctxYear);
                    } else {
                        dataQuery.orderByDesc("year").last("LIMIT 1");
                    }
                    SurveyData surveyData = surveyDataMapper.selectOne(dataQuery);

                    if (surveyData != null) {
                        addSurveyDataToContext(regionContext, surveyData);
                    }
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

        Map<String, Map<String, Object>> immutableAllRegionContexts = cloneRegionContexts(allRegionContexts);

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
                        Map<String, Map<String, Object>> allRegionData = "NORMALIZE".equalsIgnoreCase(marker)
                                ? immutableAllRegionContexts
                                : allRegionContexts;
                        result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionData);
                        
                        // 确保数值类型转换并格式化为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                                doubleValue = 0.0;
                            }
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                        }
                    } else {
                        if (shouldApplyGovernmentSecondaryWeighting(step, algorithm, qlExpression, governmentModel)) {
                            result = applyGovernmentSecondaryWeighting(algorithm, regionContext);
                        } else {
                            String rewrittenExpression = rewriteLegacyWeightExpressionIfNeeded(algorithm, qlExpression, regionContext);
                            String normalizedExpression = normalizeWeightVarCodes(rewrittenExpression);
                            prepareNullVariablesForExpression(normalizedExpression, regionContext);
                            result = qlExpressService.execute(normalizedExpression, regionContext);
                        }
                        
                        // 确保数值类型的结果转换为Double并格式化为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                                doubleValue = 0.0;
                            }
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
            Set<String> gradeScoreFields = new LinkedHashSet<>();
            for (StepAlgorithm algorithm : gradeAlgorithms) {
                String qlExpression = algorithm.getQlExpression();
                if (qlExpression != null) {
                    String[] parts = qlExpression.substring(1).split(":", 2);
                    String params = parts.length > 1 ? parts[1] : "";
                    if (params != null && !params.trim().isEmpty()) {
                        gradeScoreFields.add(params.trim());
                    }
                }
            }

            if (!gradeScoreFields.isEmpty()) {
                Map<String, double[]> gradeStats = buildGradeStats(gradeScoreFields, allRegionContexts);
                if (!gradeStats.isEmpty()) {
                    for (Map<String, Object> regionContext : allRegionContexts.values()) {
                        regionContext.put("gradeStats", gradeStats);
                    }
                }
            }

            for (String regionCode : regionCodes) {
                Map<String, Object> regionContext = allRegionContexts.get(regionCode);
                Map<String, Object> algorithmOutputs = regionResults.get(regionCode);

                for (StepAlgorithm algorithm : gradeAlgorithms) {
                    try {
                        String qlExpression = algorithm.getQlExpression();
                        String[] parts = qlExpression.substring(1).split(":", 2);
                        String marker = parts[0];
                        String params = parts.length > 1 ? parts[1] : "";

                        log.info("[执行GRADE算法] regionCode={}, marker={}, params={}", regionCode, marker, params);

                        // 调用特殊算法服务
                        Object result = specialAlgorithmService.executeSpecialAlgorithm(
                                marker, params, regionCode, regionContext, allRegionContexts);

                        log.info("[执行GRADE算法] regionCode={}, result={}, resultType={}",
                                regionCode, result, result != null ? result.getClass().getName() : "null");

                        // 格式化GRADE算法结果为8位小数
                        if (result != null && result instanceof Number) {
                            double doubleValue = ((Number) result).doubleValue();
                            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                                doubleValue = 0.0;
                            }
                            result = Double.parseDouble(String.format("%.8f", doubleValue));
                            log.warn("[执行GRADE算法] result是Number类型，已格式化: {}", result);
                        }

                        // 保存算法输出到上下文（供后续算法使用）
                        String outputParam = algorithm.getOutputParam();
                        if (outputParam != null && !outputParam.isEmpty()) {
                            String cleanedOutputParam = outputParam.trim();
                            log.info("[执行GRADE算法] 存储结果: key={}, value={}", cleanedOutputParam, result);
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

    private Map<String, Map<String, Object>> cloneRegionContexts(Map<String, Map<String, Object>> source) {
        Map<String, Map<String, Object>> clone = new LinkedHashMap<>();
        if (source == null || source.isEmpty()) {
            return clone;
        }
        for (Map.Entry<String, Map<String, Object>> entry : source.entrySet()) {
            Map<String, Object> context = entry.getValue();
            clone.put(entry.getKey(), context == null ? new HashMap<>() : new HashMap<>(context));
        }
        return clone;
    }

    private boolean shouldApplyGovernmentSecondaryWeighting(ModelStep step, StepAlgorithm algorithm, String expression, boolean governmentModel) {
        if (!governmentModel || step == null || algorithm == null) {
            return false;
        }
        if (!Objects.equals(step.getStepOrder(), 4)) {
            return false;
        }
        if (expression == null) {
            return false;
        }
        String normalized = expression.replaceAll("\\s+", "");
        String outputParam = algorithm.getOutputParam();
        if (!StringUtils.hasText(outputParam)) {
            return false;
        }
        String output = outputParam.trim();
        return normalized.equals("(" + output + "*1.0)");
    }

    private Double applyGovernmentSecondaryWeighting(StepAlgorithm algorithm, Map<String, Object> regionContext) {
        String outputParam = algorithm.getOutputParam() == null ? null : algorithm.getOutputParam().trim();
        if (!StringUtils.hasText(outputParam)) {
            return 0.0;
        }
        double baseValue = toDouble(regionContext.get(outputParam));
        double indicatorWeight = resolveIndicatorWeightFromContext(regionContext, outputParam);
        if (indicatorWeight <= 0.0) {
            return baseValue;
        }
        return baseValue * indicatorWeight;
    }

    private double resolveIndicatorWeightFromContext(Map<String, Object> regionContext, String indicatorCode) {
        if (regionContext == null || !StringUtils.hasText(indicatorCode)) {
            return 0.0;
        }
        String trimmed = indicatorCode.trim();
        List<String> candidateKeys = Arrays.asList(
                "weight_" + trimmed,
                "weight_" + trimmed.toUpperCase(Locale.ROOT),
                "weight_" + trimmed.toLowerCase(Locale.ROOT)
        );
        for (String key : candidateKeys) {
            Object value = regionContext.get(key);
            double resolved = toDouble(value);
            if (resolved > 0.0) {
                return resolved;
            }
        }

        Object weightsObj = regionContext.get("weights");
        if (weightsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> weights = (Map<String, Object>) weightsObj;
            for (String key : Arrays.asList(trimmed, trimmed.toUpperCase(Locale.ROOT), trimmed.toLowerCase(Locale.ROOT))) {
                double resolved = toDouble(weights.get(key));
                if (resolved > 0.0) {
                    return resolved;
                }
            }
            for (Map.Entry<String, Object> entry : weights.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(trimmed)) {
                    double resolved = toDouble(entry.getValue());
                    if (resolved > 0.0) {
                        return resolved;
                    }
                }
            }
        }
        return 0.0;
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
                    // 优先使用survey_data中的乡镇名称（优先最新年份）
                    QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                    surveyQuery.eq("region_code", regionCode);
                    surveyQuery.orderByDesc("year").last("LIMIT 1");
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
                communityQuery.orderByDesc("year");
                communityQuery.last("LIMIT 1");
                CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
                if (communityData != null) {
                    townshipName = communityData.getTownshipName();
                    communityName = communityData.getCommunityName();
                    regionName = communityName != null ? communityName : regionCode;
                } else {
                    QueryWrapper<SurveyData> surveyQuery = new QueryWrapper<>();
                    surveyQuery.eq("region_code", regionCode);
                    surveyQuery.orderByDesc("year").last("LIMIT 1");
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
            Map<String, Double> weightMap = getWeightMapCached(weightConfigId);
            context.put("weights", weightMap);

            // 同时将每个权重作为独立变量存储（便于表达式直接引用）
            for (Map.Entry<String, Double> entry : weightMap.entrySet()) {
                String code = entry.getKey();
                Double weightValue = entry.getValue();
                context.put("weight_" + code, weightValue == null ? 0.0 : weightValue);
            }
            for (String code : DEFAULT_WEIGHT_CODES) {
                context.putIfAbsent("weight_" + code, 0.0);
            }
        }

        // 加载基础数据到上下文（根据年份、数据类型和评估区域类型筛选）
        Integer year = (Integer) context.get("year");
        Long modelId = (Long) context.get("modelId");

        // 根据模型ID判断数据类型和评估区域类型
        if (modelId != null) {
            if (isGovernmentModel(modelId, (String) context.get("modelName"))) {
                loadGovernmentBaseData(context, regionCodes, year);
            } else if (isEnterpriseModel(modelId, (String) context.get("modelName"))) {
                loadEnterpriseBaseData(context, regionCodes, year);
            } else if (isSocialOrganizationModel(modelId, (String) context.get("modelName"))) {
                loadSocialOrganizationBaseData(context, regionCodes, year);
            } else if (modelId == 4 || modelId == 8) {
                // 社区级评估模型：加载社区数据并按年份筛选
                loadCommunityBaseData(context, regionCodes, year);
            } else {
                // 乡镇级评估模型：加载乡镇数据并按年份筛选
                loadTownshipBaseData(context, regionCodes, year);
            }
        }
    }

    private Long resolveWeightConfigIdIfNeeded(Long modelId, Long weightConfigId, Integer year, String orgCode) {
        if (weightConfigId != null) {
            return weightConfigId;
        }
        if (modelId == null || year == null || !StringUtils.hasText(orgCode)) {
            return null;
        }

        String trimmedOrgCode = orgCode.trim();
        String cacheKey = trimmedOrgCode + ":" + year + ":" + modelId;
        Long cached = getResolvedWeightConfigIdFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        String desiredConfigName = resolveModelNameForWeightConfig(modelId);
        List<WeightConfig> configs = weightConfigService.getEffectiveModelYearConfigs(trimmedOrgCode, year);
        WeightConfig matched = findWeightConfig(configs, modelId, desiredConfigName);

        if (matched == null) {
            configs = weightConfigService.getOrCreateModelYearConfigs(trimmedOrgCode, year);
            matched = findWeightConfig(configs, modelId, desiredConfigName);
        }

        if (matched == null || matched.getId() == null) {
            return null;
        }

        putResolvedWeightConfigIdToCache(cacheKey, matched.getId());
        return matched.getId();
    }

    private String resolveModelNameForWeightConfig(Long modelId) {
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        if (model != null && StringUtils.hasText(model.getModelName())) {
            return model.getModelName().trim();
        }
        return null;
    }

    private WeightConfig findWeightConfig(List<WeightConfig> configs, Long modelId, String desiredConfigName) {
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        if (modelId != null) {
            for (WeightConfig cfg : configs) {
                if (cfg == null || cfg.getId() == null || cfg.getModelId() == null) {
                    continue;
                }
                if (Objects.equals(cfg.getModelId(), modelId)) {
                    return cfg;
                }
            }
        }
        if (!StringUtils.hasText(desiredConfigName)) {
            return null;
        }
        String desired = desiredConfigName.trim();
        for (WeightConfig cfg : configs) {
            if (cfg == null || !StringUtils.hasText(cfg.getConfigName())) {
                continue;
            }
            if (cfg.getConfigName().trim().equals(desired)) {
                return cfg;
            }
        }
        return null;
    }

    private Long getResolvedWeightConfigIdFromCache(String key) {
        Instant now = Instant.now();
        synchronized (resolvedWeightConfigCacheLock) {
            TimedValue<Long> tv = resolvedWeightConfigIdCache.get(key);
            if (tv == null) {
                return null;
            }
            if (tv.isExpired(now, WEIGHT_CACHE_TTL)) {
                resolvedWeightConfigIdCache.remove(key);
                return null;
            }
            return tv.value;
        }
    }

    private void putResolvedWeightConfigIdToCache(String key, Long value) {
        synchronized (resolvedWeightConfigCacheLock) {
            resolvedWeightConfigIdCache.put(key, new TimedValue<>(value, Instant.now()));
        }
    }

    private Map<String, Double> getWeightMapCached(Long weightConfigId) {
        Instant now = Instant.now();
        synchronized (weightMapCacheLock) {
            TimedValue<Map<String, Double>> tv = weightMapCache.get(weightConfigId);
            if (tv != null && !tv.isExpired(now, WEIGHT_CACHE_TTL)) {
                return tv.value;
            }
            if (tv != null) {
                weightMapCache.remove(weightConfigId);
            }
        }

        Map<String, Double> weightMap = new HashMap<>();
        QueryWrapper<IndicatorWeight> weightQuery = new QueryWrapper<>();
        weightQuery.eq("config_id", weightConfigId);
        List<IndicatorWeight> weights = indicatorWeightMapper.selectList(weightQuery);
        for (IndicatorWeight w : weights) {
            if (w == null || w.getIndicatorCode() == null) {
                continue;
            }
            String code = w.getIndicatorCode().trim();
            if (code.isEmpty()) {
                continue;
            }
            Double v = w.getWeight();
            weightMap.put(code, v == null ? 0.0 : v);
        }

        if (indicatorWeightScoreService != null) {
            Map<String, Double> averageWeights = indicatorWeightScoreService.calculateAverageWeights(weightConfigId);
            if (averageWeights != null && !averageWeights.isEmpty()) {
                for (Map.Entry<String, Double> entry : averageWeights.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        String code = entry.getKey().trim();
                        if (!code.isEmpty()) {
                            weightMap.put(code, entry.getValue());
                        }
                    }
                }
            }
        }

        boolean hasExplicitL1 = weightMap.containsKey("L1_DISASTER_MANAGEMENT")
                || weightMap.containsKey("L1_DISASTER_PREPAREDNESS")
                || weightMap.containsKey("L1_SELF_RESCUE_TRANSFER")
                || weightMap.containsKey("L1_MANAGEMENT")
                || weightMap.containsKey("L1_PREPARATION")
                || weightMap.containsKey("L1_SELF_RESCUE");

        Double legacyL1Management = weightMap.get("L1_MANAGEMENT");
        Double legacyL1Preparation = weightMap.get("L1_PREPARATION");
        Double legacyL1SelfRescue = weightMap.get("L1_SELF_RESCUE");
        if (legacyL1Management != null && legacyL1Management != 0.0) {
            weightMap.putIfAbsent("L1_DISASTER_MANAGEMENT", legacyL1Management);
            weightMap.putIfAbsent("L1_MANAGEMENT", legacyL1Management);
        }
        if (legacyL1Preparation != null && legacyL1Preparation != 0.0) {
            weightMap.putIfAbsent("L1_DISASTER_PREPAREDNESS", legacyL1Preparation);
            weightMap.putIfAbsent("L1_PREPARATION", legacyL1Preparation);
        }
        if (legacyL1SelfRescue != null && legacyL1SelfRescue != 0.0) {
            weightMap.putIfAbsent("L1_SELF_RESCUE_TRANSFER", legacyL1SelfRescue);
            weightMap.putIfAbsent("L1_SELF_RESCUE", legacyL1SelfRescue);
        }

        Double newL1Management = weightMap.get("L1_DISASTER_MANAGEMENT");
        Double newL1Preparation = weightMap.get("L1_DISASTER_PREPAREDNESS");
        Double newL1SelfRescue = weightMap.get("L1_SELF_RESCUE_TRANSFER");
        if (newL1Management != null && newL1Management != 0.0) {
            weightMap.putIfAbsent("L1_MANAGEMENT", newL1Management);
        }
        if (newL1Preparation != null && newL1Preparation != 0.0) {
            weightMap.putIfAbsent("L1_PREPARATION", newL1Preparation);
        }
        if (newL1SelfRescue != null && newL1SelfRescue != 0.0) {
            weightMap.putIfAbsent("L1_SELF_RESCUE", newL1SelfRescue);
        }

        double l1m = weightMap.getOrDefault("L1_DISASTER_MANAGEMENT", 0.0);
        double l1p = weightMap.getOrDefault("L1_DISASTER_PREPAREDNESS", 0.0);
        double l1s = weightMap.getOrDefault("L1_SELF_RESCUE_TRANSFER", 0.0);
        if (l1m == 0.0 && l1p == 0.0 && l1s == 0.0) {
            double s1 = weightMap.getOrDefault("L2_MANAGEMENT_CAPABILITY", 0.0)
                    + weightMap.getOrDefault("L2_RISK_ASSESSMENT", 0.0)
                    + weightMap.getOrDefault("L2_FUNDING", 0.0);
            double s2 = weightMap.getOrDefault("L2_MATERIAL", 0.0)
                    + weightMap.getOrDefault("L2_MEDICAL", 0.0);
            double s3 = weightMap.getOrDefault("L2_SELF_RESCUE", 0.0)
                    + weightMap.getOrDefault("L2_PUBLIC_AVOIDANCE", 0.0)
                    + weightMap.getOrDefault("L2_RELOCATION", 0.0);
            double total = s1 + s2 + s3;
            if (total > 0.0) {
                weightMap.put("L1_DISASTER_MANAGEMENT", s1 / total);
                weightMap.put("L1_DISASTER_PREPAREDNESS", s2 / total);
                weightMap.put("L1_SELF_RESCUE_TRANSFER", s3 / total);
                weightMap.putIfAbsent("L1_MANAGEMENT", weightMap.get("L1_DISASTER_MANAGEMENT"));
                weightMap.putIfAbsent("L1_PREPARATION", weightMap.get("L1_DISASTER_PREPAREDNESS"));
                weightMap.putIfAbsent("L1_SELF_RESCUE", weightMap.get("L1_SELF_RESCUE_TRANSFER"));
            }
        }

        if (!hasExplicitL1) {
            double s1 = weightMap.getOrDefault("L2_MANAGEMENT_CAPABILITY", 0.0)
                    + weightMap.getOrDefault("L2_RISK_ASSESSMENT", 0.0)
                    + weightMap.getOrDefault("L2_FUNDING", 0.0);
            double s2 = weightMap.getOrDefault("L2_MATERIAL", 0.0)
                    + weightMap.getOrDefault("L2_MEDICAL", 0.0);
            double s3 = weightMap.getOrDefault("L2_SELF_RESCUE", 0.0)
                    + weightMap.getOrDefault("L2_PUBLIC_AVOIDANCE", 0.0)
                    + weightMap.getOrDefault("L2_RELOCATION", 0.0);
            double total = s1 + s2 + s3;
            if (total > 0.0) {
                weightMap.put("L1_DISASTER_MANAGEMENT", s1 / total);
                weightMap.put("L1_DISASTER_PREPAREDNESS", s2 / total);
                weightMap.put("L1_SELF_RESCUE_TRANSFER", s3 / total);
                weightMap.put("L1_MANAGEMENT", weightMap.get("L1_DISASTER_MANAGEMENT"));
                weightMap.put("L1_PREPARATION", weightMap.get("L1_DISASTER_PREPAREDNESS"));
                weightMap.put("L1_SELF_RESCUE", weightMap.get("L1_SELF_RESCUE_TRANSFER"));
            }
        }

        Map<String, Double> immutable = Collections.unmodifiableMap(weightMap);
        synchronized (weightMapCacheLock) {
            weightMapCache.put(weightConfigId, new TimedValue<>(immutable, Instant.now()));
        }
        return immutable;
    }

    private static final class TimedValue<T> {
        private final T value;
        private final Instant createdAt;

        private TimedValue(T value, Instant createdAt) {
            this.value = value;
            this.createdAt = createdAt;
        }

        private boolean isExpired(Instant now, Duration ttl) {
            if (ttl == null) {
                return false;
            }
            return createdAt.plus(ttl).isBefore(now);
        }
    }

    private String normalizeOrgCode(String orgCode, List<String> regionCodes) {
        if (StringUtils.hasText(orgCode)) {
            String trimmed = orgCode.trim();
            if (trimmed.length() >= 6) {
                return trimmed.substring(0, 6);
            }
            return trimmed;
        }
        if (regionCodes == null || regionCodes.isEmpty()) {
            return null;
        }
        String first = regionCodes.get(0);
        if (!StringUtils.hasText(first)) {
            return null;
        }
        String code = first.trim();
        if (code.length() >= 6) {
            return code.substring(0, 6);
        }
        return code;
    }

    private void prepareNullVariablesForExpression(String expression, Map<String, Object> context) {
        if (expression == null || expression.trim().isEmpty() || context == null) {
            return;
        }
        Matcher weightMatcher = WEIGHT_VAR_PATTERN.matcher(expression);
        while (weightMatcher.find()) {
            String var = weightMatcher.group();
            Object v = context.get(var);
            if (v == null) {
                context.put(var, 0.0);
            }
        }
        Matcher normMatcher = NORM_VAR_PATTERN.matcher(expression);
        while (normMatcher.find()) {
            String var = normMatcher.group();
            Object v = context.get(var);
            if (v == null) {
                context.put(var, 0.0);
            }
        }
    }

    private String normalizeWeightVarCodes(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return expression;
        }
        String normalized = expression;
        normalized = normalized.replaceAll("(?<![A-Za-z0-9_])weight_L1_MANAGEMENT(?![A-Za-z0-9_])", "weight_L1_DISASTER_MANAGEMENT");
        normalized = normalized.replaceAll("(?<![A-Za-z0-9_])weight_L1_PREPARATION(?![A-Za-z0-9_])", "weight_L1_DISASTER_PREPAREDNESS");
        normalized = normalized.replaceAll("(?<![A-Za-z0-9_])weight_L1_SELF_RESCUE(?![A-Za-z0-9_])", "weight_L1_SELF_RESCUE_TRANSFER");
        return normalized;
    }

    private String rewriteLegacyWeightExpressionIfNeeded(StepAlgorithm algorithm, String expression, Map<String, Object> context) {
        if (expression == null || expression.trim().isEmpty()) {
            return expression;
        }
        if (expression.contains("weight_")) {
            return expression;
        }
        String algorithmCode = algorithm == null ? null : algorithm.getAlgorithmCode();
        if (algorithmCode == null) {
            return expression;
        }
        String code = algorithmCode.trim();
        if (code.isEmpty()) {
            return expression;
        }

        boolean isWeighted = code.endsWith("_WEIGHTED");
        boolean isSecondary = code.endsWith("_SECONDARY");
        if (!isWeighted && !isSecondary) {
            return expression;
        }

        String baseCode = isWeighted ? code.substring(0, code.length() - "_WEIGHTED".length())
                : code.substring(0, code.length() - "_SECONDARY".length());
        String[] codes = resolveWeightIndicatorCodes(baseCode, context);
        if (codes == null) {
            return expression;
        }

        String stripped = stripTrailingNumericMultipliers(expression, isWeighted ? 2 : 1);
        if (stripped == null || stripped.trim().isEmpty()) {
            return expression;
        }

        if (isWeighted) {
            return stripped.trim() + " * weight_" + codes[0] + " * weight_" + codes[1];
        }
        return stripped.trim() + " * weight_" + codes[1];
    }

    private String stripTrailingNumericMultipliers(String expression, int count) {
        String current = expression == null ? null : expression.trim();
        if (current == null || current.isEmpty()) {
            return current;
        }
        for (int i = 0; i < count; i++) {
            Matcher m = TRAILING_NUMERIC_MULTIPLIER.matcher(current);
            if (!m.matches()) {
                return null;
            }
            current = m.group(1);
            if (current == null) {
                return null;
            }
            current = current.trim();
        }
        return current;
    }

    private String[] resolveWeightIndicatorCodes(String baseAlgorithmCode, Map<String, Object> context) {
        if (baseAlgorithmCode == null) {
            return null;
        }
        String base = baseAlgorithmCode.trim().toUpperCase(Locale.ROOT);
        if (base.isEmpty()) {
            return null;
        }
        List<String> availableWeightCodes = extractWeightCodesFromContext(context);
        if (availableWeightCodes.isEmpty()) {
            return null;
        }
        List<String> tokens = extractMatchTokens(base);
        String l2 = findBestIndicatorCode(availableWeightCodes, "L2_", tokens);
        if (!StringUtils.hasText(l2)) {
            return null;
        }
        String l1 = findBestIndicatorCode(availableWeightCodes, "L1_", tokens);
        if (!StringUtils.hasText(l1)) {
            List<String> l2Tokens = extractMatchTokens(l2);
            l1 = findBestIndicatorCode(availableWeightCodes, "L1_", l2Tokens);
        }
        if (!StringUtils.hasText(l1)) {
            return null;
        }
        return new String[]{l1, l2};
    }

    private List<String> extractWeightCodesFromContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> codes = new ArrayList<>();
        for (String key : context.keySet()) {
            if (!StringUtils.hasText(key) || !key.startsWith("weight_")) {
                continue;
            }
            String code = key.substring("weight_".length()).trim().toUpperCase(Locale.ROOT);
            if (StringUtils.hasText(code)) {
                codes.add(code);
            }
        }
        return codes;
    }

    private List<String> extractMatchTokens(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        String[] parts = normalized.split("_");
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "L1", "L2", "WEIGHTED", "SECONDARY", "CAPABILITY", "SCORE", "TOTAL", "INDEX"
        ));
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            String token = part.trim();
            if (stopWords.contains(token) || token.length() <= 1) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private String findBestIndicatorCode(List<String> codes, String prefix, List<String> tokens) {
        if (codes == null || codes.isEmpty() || !StringUtils.hasText(prefix)) {
            return null;
        }
        List<String> candidates = codes.stream()
                .filter(code -> StringUtils.hasText(code) && code.startsWith(prefix))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return null;
        }
        if (tokens == null || tokens.isEmpty()) {
            return candidates.get(0);
        }
        String best = null;
        int bestScore = -1;
        for (String candidate : candidates) {
            int score = 0;
            for (String token : tokens) {
                if (candidate.contains(token)) {
                    score++;
                }
            }
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return bestScore > 0 ? best : null;
    }

    /**
     * 加载乡镇基础数据
     */
    private void loadTownshipBaseData(Map<String, Object> context, List<String> regionCodes, Integer year) {
        QueryWrapper<SurveyData> queryWrapper = new QueryWrapper<>();
        if (year != null) {
            queryWrapper.eq("year", year);
        } else {
            queryWrapper.orderByDesc("year");
        }
        if (regionCodes != null && !regionCodes.isEmpty()) {
            queryWrapper.in("region_code", regionCodes);
        }

        List<SurveyData> surveyDataList = surveyDataMapper.selectList(queryWrapper);

        // 将数据转换为Map，以region_code为key
        Map<String, SurveyData> surveyDataMap = surveyDataList.stream()
                .collect(Collectors.toMap(
                        SurveyData::getRegionCode,
                        data -> data,
                        (existing, replacement) -> existing // 保留第一个
                ));

        context.put("surveyDataMap", surveyDataMap);
        // 注意：不添加 surveyDataList 到上下文，避免大量数据复制导致性能问题
        // 如需访问所有乡镇数据，请使用 surveyDataMap

        log.info("加载乡镇基础数据：{} 条记录，年份：{}", surveyDataList.size(), year);
    }

    /**
     * 加载社区基础数据
     */
    private void loadCommunityBaseData(Map<String, Object> context, List<String> regionCodes, Integer year) {
        QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();
        if (year != null) {
            queryWrapper.eq("year", year);
        } else {
            queryWrapper.orderByDesc("year");
        }
        if (regionCodes != null && !regionCodes.isEmpty()) {
            queryWrapper.in("region_code", regionCodes);
        }

        List<Map<String, Object>> communityDataList = communityDataMapper.selectMaps(queryWrapper);
        Map<String, Map<String, Object>> communityDataMap = new LinkedHashMap<>();
        for (Map<String, Object> row : communityDataList) {
            if (row == null) {
                continue;
            }
            Object regionCodeObj = row.get("region_code");
            if (regionCodeObj == null) {
                continue;
            }
            String regionCode = regionCodeObj.toString();
            if (!communityDataMap.containsKey(regionCode)) {
                communityDataMap.put(regionCode, row);
            }
        }

        context.put("communityDataMap", communityDataMap);
        // 注意：不添加 communityDataList 到上下文，避免大量数据复制导致性能问题
        // 如需访问所有社区数据，请使用 communityDataMap

        log.info("加载社区基础数据：{} 条记录，年份：{}", communityDataList.size(), year);
    }

    private void loadGovernmentBaseData(Map<String, Object> context, List<String> regionCodes, Integer year) {
        List<Map<String, Object>> governmentDataList = queryGovernmentRows(regionCodes, year);
        Map<String, Map<String, Object>> governmentDataMap = new LinkedHashMap<>();
        for (Map<String, Object> row : governmentDataList) {
            if (row == null) {
                continue;
            }
            Object regionCodeObj = row.get("region_code");
            if (regionCodeObj == null) {
                continue;
            }
            String regionCode = String.valueOf(regionCodeObj).trim();
            if (!regionCode.isEmpty() && !governmentDataMap.containsKey(regionCode)) {
                governmentDataMap.put(regionCode, row);
            }
        }
        context.put("governmentDataMap", governmentDataMap);
        log.info("加载政府基础数据：{} 条记录，年份：{}", governmentDataList.size(), year);
    }

    private void loadEnterpriseBaseData(Map<String, Object> context, List<String> regionCodes, Integer year) {
        List<Map<String, Object>> enterpriseDataList = queryEnterpriseRows(regionCodes, year);
        Map<String, Map<String, Object>> enterpriseDataMap = new LinkedHashMap<>();
        for (Map<String, Object> row : enterpriseDataList) {
            if (row == null) {
                continue;
            }
            Object regionCodeObj = row.get("region_code");
            if (regionCodeObj == null) {
                continue;
            }
            String regionCode = String.valueOf(regionCodeObj).trim();
            if (!regionCode.isEmpty() && !enterpriseDataMap.containsKey(regionCode)) {
                enterpriseDataMap.put(regionCode, row);
            }
        }
        context.put("enterpriseDataMap", enterpriseDataMap);
        log.info("加载企业基础数据：{} 条记录，年份：{}", enterpriseDataList.size(), year);
    }

    private void loadSocialOrganizationBaseData(Map<String, Object> context, List<String> regionCodes, Integer year) {
        List<Map<String, Object>> socialOrganizationDataList = querySocialOrganizationRows(regionCodes, year);
        Map<String, Map<String, Object>> socialOrganizationDataMap = new LinkedHashMap<>();
        for (Map<String, Object> row : socialOrganizationDataList) {
            if (row == null) {
                continue;
            }
            Object regionCodeObj = row.get("region_code");
            if (regionCodeObj == null) {
                continue;
            }
            String regionCode = String.valueOf(regionCodeObj).trim();
            if (!regionCode.isEmpty() && !socialOrganizationDataMap.containsKey(regionCode)) {
                socialOrganizationDataMap.put(regionCode, row);
            }
        }
        context.put("socialOrganizationDataMap", socialOrganizationDataMap);
        log.info("加载社会组织基础数据：{} 条记录，年份：{}", socialOrganizationDataList.size(), year);
    }

    private List<Map<String, Object>> queryGovernmentRows(List<String> regionCodes, Integer year) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM " + GOVERNMENT_CAPACITY_TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        List<String> validCodes = regionCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (validCodes.isEmpty()) {
            return Collections.emptyList();
        }
        sql.append(" AND (");
        for (int i = 0; i < validCodes.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("region_code LIKE ?");
            params.add(validCodes.get(i) + "%");
        }
        sql.append(")");
        sql.append(" ORDER BY region_code ASC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    private List<String> resolveGovernmentRegionCodes(List<String> regionCodes, Integer year) {
        List<Map<String, Object>> rows = queryGovernmentRows(regionCodes, year);
        List<String> resolved = rows.stream()
                .map(row -> row.get("region_code"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (!resolved.isEmpty()) {
            return resolved;
        }
        return regionCodes == null ? Collections.emptyList() : new ArrayList<>(regionCodes);
    }

    private List<Map<String, Object>> queryEnterpriseRows(List<String> regionCodes, Integer year) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM " + ENTERPRISE_CAPACITY_TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        List<String> validCodes = regionCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (validCodes.isEmpty()) {
            return Collections.emptyList();
        }
        sql.append(" AND (");
        for (int i = 0; i < validCodes.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("region_code LIKE ?");
            params.add(validCodes.get(i) + "%");
        }
        sql.append(")");
        sql.append(" ORDER BY region_code ASC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    private List<String> resolveEnterpriseRegionCodes(List<String> regionCodes, Integer year) {
        List<Map<String, Object>> rows = queryEnterpriseRows(regionCodes, year);
        List<String> resolved = rows.stream()
                .map(row -> row.get("region_code"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (!resolved.isEmpty()) {
            return resolved;
        }
        return regionCodes == null ? Collections.emptyList() : new ArrayList<>(regionCodes);
    }

    private List<Map<String, Object>> querySocialOrganizationRows(List<String> regionCodes, Integer year) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM " + SOCIAL_ORGANIZATION_CAPACITY_TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        List<String> validCodes = regionCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (validCodes.isEmpty()) {
            return Collections.emptyList();
        }
        sql.append(" AND (");
        for (int i = 0; i < validCodes.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("region_code LIKE ?");
            params.add(validCodes.get(i) + "%");
        }
        sql.append(")");
        sql.append(" ORDER BY region_code ASC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    private List<String> resolveSocialOrganizationRegionCodes(List<String> regionCodes, Integer year) {
        List<Map<String, Object>> rows = querySocialOrganizationRows(regionCodes, year);
        List<String> resolved = rows.stream()
                .map(row -> row.get("region_code"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (!resolved.isEmpty()) {
            return resolved;
        }
        return regionCodes == null ? Collections.emptyList() : new ArrayList<>(regionCodes);
    }

    private boolean hasGovernmentData(String regionCode, Integer year) {
        if (!StringUtils.hasText(regionCode)) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM " + GOVERNMENT_CAPACITY_TABLE + " WHERE region_code LIKE ?");
        List<Object> params = new ArrayList<>();
        params.add(regionCode.trim() + "%");
        if (year != null) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null && count > 0;
    }

    private boolean hasEnterpriseData(String regionCode, Integer year) {
        if (!StringUtils.hasText(regionCode)) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM " + ENTERPRISE_CAPACITY_TABLE + " WHERE region_code LIKE ?");
        List<Object> params = new ArrayList<>();
        params.add(regionCode.trim() + "%");
        if (year != null) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null && count > 0;
    }

    private boolean hasSocialOrganizationData(String regionCode, Integer year) {
        if (!StringUtils.hasText(regionCode)) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM " + SOCIAL_ORGANIZATION_CAPACITY_TABLE + " WHERE region_code LIKE ?");
        List<Object> params = new ArrayList<>();
        params.add(regionCode.trim() + "%");
        if (year != null) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null && count > 0;
    }

    private boolean isGovernmentModel(Long modelId, String modelName) {
        if (StringUtils.hasText(modelName) && modelName.contains(GOVERNMENT_MODEL_KEYWORD)) {
            return true;
        }
        if (modelId == null) {
            return false;
        }
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        return model != null && StringUtils.hasText(model.getModelName()) && model.getModelName().contains(GOVERNMENT_MODEL_KEYWORD);
    }

    private boolean isEnterpriseModel(Long modelId, String modelName) {
        if (StringUtils.hasText(modelName) && modelName.contains(ENTERPRISE_MODEL_KEYWORD)) {
            return true;
        }
        if (modelId == null) {
            return false;
        }
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        return model != null && StringUtils.hasText(model.getModelName()) && model.getModelName().contains(ENTERPRISE_MODEL_KEYWORD);
    }

    private boolean isSocialOrganizationModel(Long modelId, String modelName) {
        if (StringUtils.hasText(modelName) && modelName.contains(SOCIAL_ORGANIZATION_MODEL_KEYWORD)) {
            return true;
        }
        if (modelId == null) {
            return false;
        }
        EvaluationModel model = evaluationModelMapper.selectById(modelId);
        return model != null && StringUtils.hasText(model.getModelName()) && model.getModelName().contains(SOCIAL_ORGANIZATION_MODEL_KEYWORD);
    }

    /**
     * 加载前面步骤的输出结果到当前区域上下文
     * 从 globalContext 中提取前面步骤的 regionResults，并将当前区域的输出值添加到上下文
     */
    private void loadPreviousStepOutputs(Map<String, Object> regionContext, String regionCode, Map<String, Object> globalContext) {
        List<Map<String, Object>> orderedStepResults = new ArrayList<>();
        for (Map.Entry<String, Object> entry : globalContext.entrySet()) {
            if (!entry.getKey().startsWith("step_") || !(entry.getValue() instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
            orderedStepResults.add(stepResult);
        }

        orderedStepResults.sort(Comparator.comparingInt(stepResult -> {
            Object stepOrderObj = stepResult.get("stepOrder");
            if (stepOrderObj instanceof Number) {
                return ((Number) stepOrderObj).intValue();
            }
            if (stepOrderObj instanceof String) {
                try {
                    return Integer.parseInt((String) stepOrderObj);
                } catch (NumberFormatException ignore) {
                    return Integer.MAX_VALUE;
                }
            }
            return Integer.MAX_VALUE;
        }));

        for (Map<String, Object> stepResult : orderedStepResults) {
            Object regionResultsObj = stepResult.get("regionResults");
            if (!(regionResultsObj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) regionResultsObj;
            Map<String, Object> currentRegionOutputs = regionResults.get(regionCode);
            if (currentRegionOutputs == null) {
                continue;
            }
            for (Map.Entry<String, Object> output : currentRegionOutputs.entrySet()) {
                regionContext.put(output.getKey(), output.getValue());
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
        context.put("fundingAmount", surveyData.getFundingAmount() != null ? surveyData.getFundingAmount().doubleValue() : 0.0);
        context.put("funding_amount", surveyData.getFundingAmount() != null ? surveyData.getFundingAmount().doubleValue() : 0.0);

        // 物资储备（驼峰和下划线两种命名）- 处理null值
        context.put("materialValue", surveyData.getMaterialValue() != null ? surveyData.getMaterialValue().doubleValue() : 0.0);
        context.put("material_value", surveyData.getMaterialValue() != null ? surveyData.getMaterialValue().doubleValue() : 0.0);

        // 医院床位（驼峰和下划线两种命名）- 处理null值
        Integer hospitalBeds = surveyData.getHospitalBeds();
        context.put("hospitalBeds", hospitalBeds != null ? hospitalBeds : 0);
        context.put("hospital_beds", hospitalBeds != null ? hospitalBeds : 0);

        // 消防员（驼峰和下划线两种命名）- 处理null值
        Integer firefighters = surveyData.getFirefighters();
        context.put("firefighters", firefighters != null ? firefighters : 0);
        context.put(" firefighter_count", firefighters != null ? firefighters : 0);

        // 志愿者（驼峰和下划线两种命名）- 处理null值
        Integer volunteers = surveyData.getVolunteers();
        context.put("volunteers", volunteers != null ? volunteers : 0);
        context.put("volunteer_count", volunteers != null ? volunteers : 0);

        // 民兵预备役（驼峰和下划线两种命名）- 处理null值
        Integer militiaReserve = surveyData.getMilitiaReserve();
        context.put("militiaReserve", militiaReserve != null ? militiaReserve : 0);
        context.put("militia_reserve", militiaReserve != null ? militiaReserve : 0);

        // 培训参与者（驼峰和下划线两种命名）- 处理null值
        Integer trainingParticipants = surveyData.getTrainingParticipants();
        context.put("trainingParticipants", trainingParticipants != null ? trainingParticipants : 0);
        context.put("training_participants", trainingParticipants != null ? trainingParticipants : 0);

        // 避难所容量（驼峰和下划线两种命名）- 处理null值
        Integer shelterCapacity = surveyData.getShelterCapacity();
        context.put("shelterCapacity", shelterCapacity != null ? shelterCapacity : 0);
        context.put("shelter_capacity", shelterCapacity != null ? shelterCapacity : 0);
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

            context.put(key, contextValue);
            String camelKey = toCamelCase(key);
            if (camelKey != null && !camelKey.equals(key) && !context.containsKey(camelKey)) {
                context.put(camelKey, contextValue);
            }
        }

    }
    
    private String toCamelCase(String value) {
        if (value == null) {
            return null;
        }
        String[] parts = value.split("_");
        if (parts.length <= 1) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                builder.append(parts[i].substring(1));
            }
        }
        return builder.toString();
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
     * 为单个步骤生成2D表格数据
     */
    private List<Map<String, Object>> generateStepResultTable(Map<String, Object> stepResult, List<String> regionCodes, Integer year) {
        // 从stepResult中获取modelId
        Long modelId = null;
        Object modelIdObj = stepResult.get("modelId");
        if (modelIdObj instanceof Number) {
            modelId = ((Number) modelIdObj).longValue();
        } else if (modelIdObj instanceof String) {
            try {
                modelId = Long.parseLong((String) modelIdObj);
            } catch (NumberFormatException ignore) {}
        }
        boolean governmentModel = isGovernmentModel(modelId, null);
        boolean enterpriseModel = isEnterpriseModel(modelId, null);
        boolean socialOrganizationModel = isSocialOrganizationModel(modelId, null);
        boolean locationModel = governmentModel || enterpriseModel || socialOrganizationModel;
        Map<String, Map<String, String>> governmentLocationByRegion = new HashMap<>();
        if (locationModel && regionCodes != null && !regionCodes.isEmpty()) {
            List<Map<String, Object>> locationRows;
            if (governmentModel) {
                locationRows = queryGovernmentRows(regionCodes, year);
            } else if (enterpriseModel) {
                locationRows = queryEnterpriseRows(regionCodes, year);
            } else {
                locationRows = querySocialOrganizationRows(regionCodes, year);
            }
            for (Map<String, Object> row : locationRows) {
                Object regionObj = row.get("region_code");
                if (regionObj == null) {
                    continue;
                }
                String code = String.valueOf(regionObj).trim();
                if (code.isEmpty()) {
                    continue;
                }
                Map<String, String> location = governmentLocationByRegion.computeIfAbsent(code, k -> new HashMap<>());
                location.put("provinceName", toString(row.get("province_name")));
                location.put("cityName", toString(row.get("city_name")));
                location.put("countyName", toString(row.get("county_name")));
            }
        }
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
            if (locationModel) {
                Map<String, String> location = governmentLocationByRegion.get(regionCode);
                if (location != null) {
                    String provinceName = location.get("provinceName");
                    String cityName = location.get("cityName");
                    String countyName = location.get("countyName");
                    if (!isEmptyString(provinceName)) {
                        row.put("provinceName", provinceName);
                    }
                    if (!isEmptyString(cityName)) {
                        row.put("cityName", cityName);
                    }
                    if (!isEmptyString(countyName)) {
                        regionName = countyName;
                    }
                }
            } else {
                QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
                communityQuery.eq("region_code", regionCode);
                if (year != null) {
                    communityQuery.eq("year", year);
                } else {
                    communityQuery.orderByDesc("year");
                }
                communityQuery.orderByDesc("create_time");
                communityQuery.last("LIMIT 1");
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
                    surveyQuery.eq("is_deleted", 0);
                    if (year != null) {
                        surveyQuery.eq("year", year);
                    } else {
                        surveyQuery.orderByDesc("year");
                    }
                    surveyQuery.orderByDesc("create_time");
                    surveyQuery.last("LIMIT 1");
                    SurveyData surveyData = surveyDataMapper.selectOne(surveyQuery);
                    if (surveyData != null && surveyData.getTownship() != null) {
                        regionName = surveyData.getTownship();
                    }
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

                if (!locationModel) {
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

            // 对于社区-行政村能力评估模型(modelId=4)，不要覆盖已经设置的社区名称
            if (!locationModel && isCodeLike(regionName) && (modelId == null || modelId != 4)) {
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
            if (!locationModel) {
                if (!isEmptyString(townshipNameMeta)) {
                    row.put("townshipName", townshipNameMeta);
                }
                if (!isEmptyString(communityNameMeta)) {
                    row.put("communityName", communityNameMeta);
                }
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

        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region", "provinceName", "cityName", "townshipName", "communityName"));

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
            column.put("label", getBaseColumnLabel(columnName));

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
        Long modelId = null;
        Object modelIdObj = rawStepResult != null ? rawStepResult.get("modelId") : null;
        if (modelIdObj instanceof Number) {
            modelId = ((Number) modelIdObj).longValue();
        } else if (modelIdObj instanceof String) {
            try {
                modelId = Long.parseLong((String) modelIdObj);
            } catch (NumberFormatException ignore) {
                modelId = null;
            }
        }
        boolean governmentModel = isGovernmentModel(modelId, null);
        boolean enterpriseModel = isEnterpriseModel(modelId, null);
        boolean socialOrganizationModel = isSocialOrganizationModel(modelId, null);
        boolean locationModel = governmentModel || enterpriseModel || socialOrganizationModel;

        String stepCode = rawStepResult != null ? toString(rawStepResult.get("stepCode")) : null;

        Map<String, Object> firstRow = tableData.get(0);
        Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region", "provinceName", "cityName", "townshipName", "communityName"));

        for (String columnName : firstRow.keySet()) {
            if (columnName.startsWith("_")) {
                continue;
            }

            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", columnName);

            String label;
            if (baseColumns.contains(columnName)) {
                if (locationModel) {
                    if ("regionCode".equals(columnName)) {
                        label = "行政区代码";
                    } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
                        label = "县名称";
                    } else {
                        label = getBaseColumnLabel(columnName);
                    }
                } else {
                    label = getBaseColumnLabel(columnName);
                }
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

    /**
     * 数据聚合步骤（支持多种模型类型）
     * 根据模型ID执行不同的聚合逻辑：
     * - modelId=4: 社区评估模型，将社区数据聚合到乡镇级别
     * - modelId=8: 综合评估模型，融合乡镇评估结果和社区聚合结果
     *
     * @param stepId 步骤ID
     * @param regionCodes 地区代码列表
     * @param inputData 输入数据（包含前面步骤的计算结果）
     * @param modelId 模型ID
     * @return 聚合结果
     */
    private Map<String, Object> executeDataAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData, Long modelId) {
        log.info("开始执行数据聚合, stepId={}, regionCodes.size={}, modelId={}", stepId, regionCodes.size(), modelId);

        if (modelId == 4) {
            // 社区-行政村能力评估模型：不进行聚合，直接处理社区数据
            return executeDirectCommunityProcessing(stepId, regionCodes, inputData);
        } else if (modelId == 8) {
            // 社区-乡镇评估模型：将社区数据聚合到乡镇级别
            return executeTownshipAggregation(stepId, regionCodes, inputData);
        } else {
            log.warn("不支持的模型ID进行聚合: {}", modelId);
            return new HashMap<>();
        }
    }

    /**
     * 社区评估模型聚合
     * 将社区数据聚合到乡镇级别
     *
     * @param stepId 步骤ID
     * @param regionCodes 社区代码列表
     * @param inputData 输入数据
     * @return 乡镇级别的聚合结果
     */
    private Map<String, Object> executeCommunityAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        // 直接调用现有的乡镇聚合逻辑
        return executeTownshipAggregation(stepId, regionCodes, inputData);
    }

    /**
     * 执行综合评估数据聚合
     */
    private Map<String, Object> executeComprehensiveAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("执行综合评估数据聚合，步骤ID: {}, 区域数量: {}", stepId, regionCodes.size());

        // 使用标准权重
        double townshipWeight = 0.6;
        double communityWeight = 0.4;

        // 从输入数据中提取乡镇和社区评估结果
        Map<String, Map<String, Object>> townshipResults = extractStepResults(inputData, "step_");
        Map<String, Map<String, Object>> communityResults = new HashMap<>(); // 社区数据通常来自相同的步骤

        // 标准综合聚合逻辑
        return performStandardComprehensiveAggregation(townshipResults, communityResults, townshipWeight, communityWeight);
    }

    /**
     * 执行标准综合聚合
     */
    private Map<String, Object> performStandardComprehensiveAggregation(
            Map<String, Map<String, Object>> townshipResults,
            Map<String, Map<String, Object>> communityResults,
            double townshipWeight,
            double communityWeight) {

        Map<String, Map<String, Object>> fusedResults = new LinkedHashMap<>();

        // 获取所有地区
        Set<String> allRegions = new HashSet<>();
        allRegions.addAll(townshipResults.keySet());
        allRegions.addAll(communityResults.keySet());

        // 对每个地区进行融合
        for (String regionName : allRegions) {
            Map<String, Object> townshipData = townshipResults.get(regionName);
            Map<String, Object> communityData = communityResults.get(regionName);

            Map<String, Object> fusedData = new LinkedHashMap<>();

            // 融合综合能力分数
            double townshipComprehensive = getScoreFromData(townshipData, "comprehensiveCapabilityScore");
            double communityComprehensive = getScoreFromData(communityData, "comprehensiveCapabilityScore");
            double finalComprehensive = townshipComprehensive * townshipWeight + communityComprehensive * communityWeight;

            fusedData.put("regionName", regionName);
            fusedData.put("comprehensiveCapabilityScore", finalComprehensive);
            fusedData.put("comprehensiveCapabilityLevel", getCapabilityLevel(finalComprehensive));

            fusedResults.put(regionName, fusedData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("regionResults", fusedResults);
        result.put("aggregationType", "comprehensive");
        result.put("regionCount", fusedResults.size());

        return result;
    }

    /**
     * 从数据中获取分数
     */
    private double getScoreFromData(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return 0.0;
        }
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    
    /**
     * 根据能力值计算能力等级（重载方法）
     */
    private String getCapabilityLevel(double score) {
        if (score >= 80) {
            return "较强";
        } else if (score >= 60) {
            return "一般";
        } else {
            return "较弱";
        }
    }

    /**
     * 从输入数据中提取指定前缀的步骤结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> extractStepResults(Map<String, Object> inputData, String prefix) {
        Map<String, Map<String, Object>> results = new HashMap<>();

        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) stepResult.get("regionResults");

                if (regionResults != null) {
                    results.putAll(regionResults);
                }
            }
        }

        return results;
    }

    /**
     * 按乡镇聚合社区数据
     */
    private Map<String, Map<String, Object>> aggregateCommunityDataByTownship(
            List<String> regionCodes, Map<String, Object> inputData, List<StepAlgorithm> algorithms) {

        Integer year = (Integer) inputData.get("year");
        Map<String, List<Map<String, Object>>> townshipGroups = new LinkedHashMap<>();

        // 按乡镇分组社区数据
        for (String regionCode : regionCodes) {
            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", regionCode);
            if (year != null) {
                communityQuery.eq("year", year);
            }
            communityQuery.orderByDesc("create_time");
            communityQuery.last("LIMIT 1");

            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);
            if (communityData == null) continue;

            String townshipName = communityData.getTownshipName();
            if (townshipName == null || townshipName.isEmpty()) continue;

            // 创建社区上下文数据
            Map<String, Object> communityContext = new HashMap<>();
            communityContext.put("currentRegionCode", regionCode);

            // 从inputData中获取社区级别的计算结果
            for (Map.Entry<String, Object> entry : inputData.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("step_")) {
                    Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
                    Map<String, Map<String, Object>> regionResults =
                            (Map<String, Map<String, Object>>) stepResult.get("regionResults");

                    if (regionResults != null && regionResults.containsKey(regionCode)) {
                        Map<String, Object> outputs = regionResults.get(regionCode);
                        communityContext.putAll(outputs);
                    }
                }
            }

            townshipGroups.computeIfAbsent(townshipName, k -> new ArrayList<>()).add(communityContext);
        }

        // 对每个乡镇执行聚合计算
        Map<String, Map<String, Object>> aggregatedResults = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : townshipGroups.entrySet()) {
            String townshipName = entry.getKey();
            List<Map<String, Object>> communities = entry.getValue();

            Map<String, Object> townshipOutput = new LinkedHashMap<>();

            // 对每个算法执行聚合（通常求平均值）
            for (StepAlgorithm algorithm : algorithms) {
                String outputParam = algorithm.getOutputParam();
                if (outputParam == null || outputParam.isEmpty()) continue;

                String cleanedOutputParam = outputParam.trim();
                double sum = 0.0;
                int count = 0;

                for (Map<String, Object> community : communities) {
                    Object value = community.get(cleanedOutputParam);
                    if (value instanceof Number) {
                        sum += ((Number) value).doubleValue();
                        count++;
                    }
                }

                double result = count > 0 ? sum / count : 0.0;
                townshipOutput.put(cleanedOutputParam, result);
            }

            aggregatedResults.put(townshipName, townshipOutput);
        }

        return aggregatedResults;
    }

    /**
     * 融合乡镇评估结果和社区聚合结果
     */
    private Map<String, Map<String, Object>> fuseTownshipAndCommunityResults(
            Map<String, Map<String, Object>> townshipResults,
            Map<String, Map<String, Object>> communityAggregatedResults,
            List<StepAlgorithm> algorithms) {

        Map<String, Map<String, Object>> fusedResults = new HashMap<>();

        // 收集所有乡镇名称
        Set<String> allTownships = new HashSet<>();
        allTownships.addAll(townshipResults.keySet());
        allTownships.addAll(communityAggregatedResults.keySet());

        for (String townshipName : allTownships) {
            Map<String, Object> townshipData = townshipResults.get(townshipName);
            Map<String, Object> communityData = communityAggregatedResults.get(townshipName);

            Map<String, Object> fusedData = new HashMap<>();

            // 融合两个数据源的结果
            for (StepAlgorithm algorithm : algorithms) {
                String outputParam = algorithm.getOutputParam();
                if (outputParam == null || outputParam.isEmpty()) continue;

                String cleanedOutputParam = outputParam.trim();

                // 优先使用乡镇评估结果，如果没有则使用社区聚合结果
                if (townshipData != null && townshipData.containsKey(cleanedOutputParam)) {
                    fusedData.put(cleanedOutputParam, townshipData.get(cleanedOutputParam));
                } else if (communityData != null && communityData.containsKey(cleanedOutputParam)) {
                    fusedData.put(cleanedOutputParam, communityData.get(cleanedOutputParam));
                } else {
                    fusedData.put(cleanedOutputParam, 0.0);
                }
            }

            fusedResults.put(townshipName, fusedData);
        }

        return fusedResults;
    }

    /**
     * 执行乡镇聚合（原方法保留）
     * 按乡镇分组，对社区数据进行聚合计算（求和后除以社区数量）
     *
     * @param stepId 步骤ID
     * @param regionCodes 社区代码列表
     * @param inputData 输入数据（包含步骤1的社区级别计算结果）
     * @return 乡镇级别的聚合结果
     */
    private Map<String, Object> executeTownshipAggregation(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("开始执行乡镇聚合, stepId={}, regionCodes.size={}", stepId, regionCodes.size());

        // 提取年份数据
        Integer year = (Integer) inputData.get("year");

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
            if (year != null) {
                communityQuery.eq("year", year);
            } else {
                communityQuery.orderByDesc("year");
            }
            communityQuery.orderByDesc("create_time");
            communityQuery.last("LIMIT 1");
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
                log.debug("计算字段 {} 在 {} 个社区中的总和", fieldName, communities.size());

                // 调试：检查第一个社区包含的所有字段
                if (!communities.isEmpty()) {
                    log.debug("第一个社区包含的字段: {}", communities.get(0).keySet());
                }

                for (Map<String, Object> community : communities) {
                    Object value = community.get(fieldName);
                    log.debug("社区字段值: {} = {}", fieldName, value);
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
     * 社区-行政村能力评估模型：直接处理社区数据，不进行聚合
     *
     * @param stepId 步骤ID
     * @param regionCodes 社区代码列表
     * @param inputData 输入数据
     * @return 处理结果
     */
    private Map<String, Object> executeDirectCommunityProcessing(Long stepId, List<String> regionCodes, Map<String, Object> inputData) {
        log.info("开始直接处理社区数据, stepId={}, regionCodes.size={}", stepId, regionCodes.size());

        Map<String, Object> stepResult = new HashMap<>();
        Map<String, Object> regionResults = new HashMap<>();

        // 获取年份信息
        Integer year = null;
        if (inputData.containsKey("year")) {
            year = (Integer) inputData.get("year");
        }

        // 为每个社区创建独立的输出结果，不进行聚合
        for (String communityCode : regionCodes) {
            Map<String, Object> communityOutput = new HashMap<>();

            // 查询社区数据
            QueryWrapper<CommunityDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("region_code", communityCode);
            if (year != null) {
                queryWrapper.eq("year", year);
            } else {
                queryWrapper.orderByDesc("year");
            }
            queryWrapper.last("LIMIT 1");

            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(queryWrapper);

            if (communityData != null) {
                // 设置社区特定的元数据，用于结果提取
                communityOutput.put("_firstCommunityCode", communityCode); // 社区代码作为地区代码
                communityOutput.put("_communityName", communityData.getCommunityName()); // 社区名称
                communityOutput.put("_isTownship", false); // 标记为非乡镇数据

                // 传递原有的所有指标数据
                for (Map.Entry<String, Object> entry : inputData.entrySet()) {
                    if (!entry.getKey().equals("communityDataList")) {
                        communityOutput.put(entry.getKey(), entry.getValue());
                    }
                }

                // 为当前社区设置特定的上下文数据
                communityOutput.put("region_code", communityCode);
                if (communityData.getCommunityName() != null) {
                    communityOutput.put("community_name", communityData.getCommunityName());
                }
                communityOutput.put("township_name", communityData.getTownshipName());

                log.debug("处理社区数据: communityCode={}, communityName={}",
                         communityCode, communityData.getCommunityName());
            }

            // 使用社区代码作为结果键，确保每个社区独立评估
            regionResults.put(communityCode, communityOutput);
        }

        // 构建标准的步骤结果结构
        stepResult.put("regionResults", regionResults);
        stepResult.put("modelId", inputData.get("modelId"));
        stepResult.put("stepId", stepId);
        stepResult.put("stepType", "AGGREGATION"); // 标记为聚合步骤，以便后续查找

        log.info("社区数据处理完成，处理了 {} 个社区", regionResults.size());
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
            double v = ((Number) value).doubleValue();
            return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v;
        }
        if (value instanceof String) {
            try {
                double v = Double.parseDouble((String) value);
                return (Double.isNaN(v) || Double.isInfinite(v)) ? 0.0 : v;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private Map<String, double[]> buildGradeStats(Set<String> scoreFields, Map<String, Map<String, Object>> allRegionContexts) {
        Map<String, double[]> stats = new HashMap<>();
        if (scoreFields == null || scoreFields.isEmpty() || allRegionContexts == null || allRegionContexts.isEmpty()) {
            return stats;
        }

        for (String scoreField : scoreFields) {
            double sum = 0.0;
            int count = 0;
            for (Map<String, Object> context : allRegionContexts.values()) {
                Object value = context.get(scoreField);
                if (value != null) {
                    sum += toDouble(value);
                    count++;
                }
            }
            if (count == 0) {
                continue;
            }
            double mean = sum / count;
            double sumSquaredDiff = 0.0;
            if (count > 1) {
                for (Map<String, Object> context : allRegionContexts.values()) {
                    Object value = context.get(scoreField);
                    if (value != null) {
                        double v = toDouble(value);
                        double diff = v - mean;
                        sumSquaredDiff += diff * diff;
                    }
                }
            }
            double stdev = count > 1 ? Math.sqrt(sumSquaredDiff / (count - 1)) : 0.0;
            stats.put(scoreField, new double[]{mean, stdev, count});
        }

        return stats;
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
     * @param createBy 操作人
     * @return 执行记录ID
     */
    private Long saveExecutionRecordAndResults(
            Long modelId,
            String modelName,
            List<String> regionCodes,
            Long weightConfigId,
            Map<String, Object> stepResults,
            List<Map<String, Object>> tableData,
            Integer year,
            String orgCode,
            String createBy
            ) {

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
            if (year != null) {
                executionRecord.setYear(year);
            }
            if (orgCode != null && !orgCode.trim().isEmpty()) {
                executionRecord.setOrgCode(orgCode.trim());
            }
            if (year != null) {
                executionRecord.setYear(year);
            }
            // 设置操作人
            if (createBy != null && !createBy.trim().isEmpty()) {
                executionRecord.setCreateBy(createBy.trim());
            }

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
                    modelId, executionRecordId, stepResults, tableData,year,orgCode);

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
            List<Map<String, Object>> tableData,
            Integer year,
            String orgCode) {

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

            // 对于社区评估模型(modelId=4)，如果当前步骤的输出中没有firstCommunityCode，
            // 使用stepRegionCode作为社区代码
            if (firstCommunityCode == null && modelId != null && modelId == 4) {
                log.info("社区评估模型 - 使用stepRegionCode作为社区代码: stepRegionCode={}", stepRegionCode);
                // 社区评估模型中，stepRegionCode本身就是社区代码
                firstCommunityCode = stepRegionCode;
            }

            String lookupRegionCode = firstCommunityCode != null ? firstCommunityCode : stepRegionCode;

            // 添加调试日志
            log.info("提取评估结果 - modelId={}, stepRegionCode={}, firstCommunityCode={}",
                    modelId, stepRegionCode, firstCommunityCode);

            // 详细调试：对于社区评估模型，显示所有stepResults的键
            if (modelId != null && modelId == 4 && firstCommunityCode == null) {
                log.info("社区评估模型调试 - stepResults包含的步骤: {}", stepResults.keySet());
                for (Map.Entry<String, Object> stepEntry : stepResults.entrySet()) {
                    Object stepResultObj = stepEntry.getValue();
                    if (stepResultObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> stepResult = (Map<String, Object>) stepResultObj;
                        Object regionResultsObj = stepResult.get("regionResults");
                        if (regionResultsObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) regionResultsObj;
                            log.info("步骤 {} 的regionResults包含的地区: {}", stepEntry.getKey(), regionResults.keySet());
                            // 检查当前地区是否在这个步骤的结果中
                            Map<String, Object> currentRegionResult = regionResults.get(stepRegionCode);
                            if (currentRegionResult != null) {
                                String stepFirstCommunityCode = toString(currentRegionResult.get("_firstCommunityCode"));
                                log.info("步骤 {} 中地区 {} 的_firstCommunityCode: {}", stepEntry.getKey(), stepRegionCode, stepFirstCommunityCode);
                            }
                        }
                    }
                }
            }

            QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
            communityQuery.eq("region_code", lookupRegionCode);
            if (year != null) {
                communityQuery.eq("year", year);
            } else {
                communityQuery.orderByDesc("year");
            }
            communityQuery.orderByDesc("create_time");
            communityQuery.last("LIMIT 1");
            CommunityDisasterReductionCapacity communityData = communityDataMapper.selectOne(communityQuery);

            String townshipName = communityData != null ? communityData.getTownshipName() : null;
            String communityName = communityData != null ? communityData.getCommunityName() : null;

            String storedRegionCode;
            String storedRegionName;
            String dataSource;

            if (firstCommunityCode != null) {
                storedRegionCode = firstCommunityCode; // 对于社区评估，保存社区代码
                // 对于社区-行政村能力评估模型(modelId=4)，应该显示社区名称
                if (modelId != null && modelId == 4) {
                    log.info("社区评估模型 - modelId=4, 使用社区代码: {}, 社区名称: {}", storedRegionCode, communityName);
                    // 社区-行政村能力评估模型：显示社区名称
                    if (!isEmptyString(communityName)) {
                        storedRegionName = communityName;
                    } else {
                        storedRegionName = resolveCommunityNameByPrefix(firstCommunityCode);
                        if (isEmptyString(storedRegionName)) {
                            storedRegionName = getRegionName(firstCommunityCode, year);
                        }
                    }
                    dataSource = "community";
                } else {
                    // 其他模型：显示乡镇名称
                    storedRegionCode = deriveTownshipCodeForStorage(firstCommunityCode);
                    if (!isEmptyString(townshipName)) {
                        storedRegionName = townshipName;
                    } else {
                        storedRegionName = getTownshipNameByCommunityCode(firstCommunityCode);
                        if (isEmptyString(storedRegionName)) {
                            storedRegionName = getRegionName(stepRegionCode, year);
                        }
                    }
                    dataSource = "township";
                }
            } else {
                log.info("firstCommunityCode为null，使用乡镇代码: stepRegionCode={}", stepRegionCode);
                storedRegionCode = deriveTownshipCodeForStorage(stepRegionCode);
                if (townshipName != null && !townshipName.isEmpty()) {
                    storedRegionName = townshipName;
                } else if (communityName != null && !communityName.isEmpty()) {
                    storedRegionName = communityName;
                } else {
                    storedRegionName = getRegionName(stepRegionCode, year);
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
            // 对于社区-行政村能力评估模型(modelId=4)，不要覆盖已经设置的社区名称
            if (modelId == null || modelId != 4) {
                if (isCodeLike(storedRegionName)) {
                    String fetched = getTownshipNameByCommunityCode(firstCommunityCode != null ? firstCommunityCode : storedRegionCode);
                    if (!isEmptyString(fetched)) {
                        storedRegionName = fetched;
                    }
                }
            }

            EvaluationResult result = new EvaluationResult();
            result.setRegionCode(storedRegionCode);
            result.setRegionName(storedRegionName);
            result.setEvaluationModelId(modelId);
            result.setDataSource(dataSource);

            log.info("保存评估结果 - modelId={}, storedRegionCode={}, storedRegionName={}, dataSource={}",
                    modelId, storedRegionCode, storedRegionName, dataSource);
            result.setExecutionRecordId(executionRecordId);
            if (orgCode != null && !orgCode.trim().isEmpty()) {
                result.setOrgCode(orgCode.trim());
            }

            // 设置分数
            BigDecimal managementScore = getDecimalValueFromMap(outputs,
                    "management_capability_score",
                    "managementCapabilityScore",
                    "disasterMgmtScore",
                    "disaster_mgmt_score",
                    "engineering_rescue_capacity");
            BigDecimal supportScore = getDecimalValueFromMap(outputs,
                    "support_capability_score",
                    "supportCapabilityScore",
                    "disasterPrepScore",
                    "disaster_prep_score",
                    "insurance_reinsurance_capacity");
            BigDecimal selfRescueScore = getDecimalValueFromMap(outputs,
                    "self_rescue_capability_score",
                    "selfRescueCapabilityScore",
                    "selfRescueScore",
                    "self_rescue_score",
                    "insurance_reinsurance_capacity");
            BigDecimal comprehensiveScore = getDecimalValueFromMap(outputs,
                    "comprehensive_capability_score",
                    "comprehensiveCapabilityScore",
                    "comprehensiveScore",
                    "comprehensive_score");
            if (comprehensiveScore == null && managementScore != null && supportScore != null) {
                comprehensiveScore = managementScore.add(supportScore).divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);
            }

            result.setManagementCapabilityScore(managementScore);
            result.setSupportCapabilityScore(supportScore);
            result.setSelfRescueCapabilityScore(selfRescueScore);
            result.setComprehensiveCapabilityScore(comprehensiveScore);

            // 设置等级 - 如果算法输出中没有等级信息，基于分数计算默认等级
            String managementLevel = getStringValueFromMap(outputs,
                    "management_capability_level",
                    "managementCapabilityLevel",
                    "disasterMgmtGrade",
                    "disaster_mgmt_grade",
                    "engineering_rescue_capacity_level");
            if (managementLevel == null && managementScore != null) {
                managementLevel = calculateLevelFromScore(managementScore);
            }
            result.setManagementCapabilityLevel(managementLevel);

            String supportLevel = getStringValueFromMap(outputs,
                    "support_capability_level",
                    "supportCapabilityLevel",
                    "disasterPrepGrade",
                    "disaster_prep_grade",
                    "insurance_reinsurance_capacity_level");
            if (supportLevel == null && supportScore != null) {
                supportLevel = calculateLevelFromScore(supportScore);
            }
            result.setSupportCapabilityLevel(supportLevel);

            String selfRescueLevel = getStringValueFromMap(outputs,
                    "self_rescue_capability_level",
                    "selfRescueCapabilityLevel",
                    "selfRescueGrade",
                    "self_rescue_grade",
                    "insurance_reinsurance_capacity_level");
            if (selfRescueLevel == null && selfRescueScore != null) {
                selfRescueLevel = calculateLevelFromScore(selfRescueScore);
            }
            result.setSelfRescueCapabilityLevel(selfRescueLevel);

            String comprehensiveLevel = getStringValueFromMap(outputs,
                    "comprehensive_capability_level",
                    "comprehensiveCapabilityLevel",
                    "comprehensiveGrade",
                    "comprehensive_grade");
            if (comprehensiveLevel == null && comprehensiveScore != null) {
                comprehensiveLevel = calculateLevelFromScore(comprehensiveScore);
            }
            if (comprehensiveLevel == null) {
                comprehensiveLevel = supportLevel;
            }
            result.setComprehensiveCapabilityLevel(comprehensiveLevel);

            results.add(result);
        }

        return results;
    }

    /**
     * 获取地区名称
     */
    private String getRegionName(String regionCode, Integer year) {
        // 检查是否是乡镇虚拟代码
        if (regionCode.startsWith("TOWNSHIP_")) {
            return regionCode.substring("TOWNSHIP_".length());
        }

        // 尝试从community表获取
        QueryWrapper<CommunityDisasterReductionCapacity> communityQuery = new QueryWrapper<>();
        communityQuery.eq("region_code", regionCode);
        communityQuery.last("LIMIT 1");
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
        surveyQuery.eq("is_deleted", 0);
        if (year != null) {
            surveyQuery.eq("year", year);
        } else {
            surveyQuery.orderByDesc("year");
        }
        surveyQuery.orderByDesc("create_time");
        surveyQuery.last("LIMIT 1");
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
     * 根据分数计算等级（默认分级标准）
     * 当算法输出中没有等级信息时使用
     * @param score 能力分数
     * @return 等级字符串
     */
    private String calculateLevelFromScore(java.math.BigDecimal score) {
        if (score == null) {
            return "中等";
        }
        double value = score.doubleValue();
        if (value >= 0.8) {
            return "强";
        } else if (value >= 0.6) {
            return "较强";
        } else if (value >= 0.4) {
            return "中等";
        } else if (value >= 0.2) {
            return "较弱";
        } else {
            return "弱";
        }
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
            double doubleValue = ((Number) value).doubleValue();
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                return null;
            }
            return java.math.BigDecimal.valueOf(doubleValue);
        }
        if (value instanceof String) {
            String stringValue = ((String) value).trim();
            if (stringValue.isEmpty() || "null".equalsIgnoreCase(stringValue)) {
                return null;
            }
            try {
                return new java.math.BigDecimal(stringValue);
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

    /**
     * 解析特殊标记表达式，支持两种格式：
     * 1. @MARKER:params (冒号格式)
     * 2. @MARKER(params) (括号格式)
     *
     * @param expression 表达式（如 "GRADE:comprehensive_capability_score" 或 "GRADE(comprehensive_capability_score)"）
     * @return 包含marker和params的Map
     */
    private Map<String, String> parseSpecialMarker(String expression) {
        Map<String, String> result = new LinkedHashMap<>();

        if (expression == null || !expression.startsWith("@")) {
            log.warn("[parseSpecialMarker] 表达式不是特殊标记: {}", expression);
            return result;
        }

        // 去掉@符号
        String content = expression.substring(1);

        // 检查是否是括号格式 @MARKER(params)
        if (content.contains("(")) {
            int parenIndex = content.indexOf("(");
            String marker = content.substring(0, parenIndex);
            String params = "";

            // 提取括号内的内容
            if (content.endsWith(")")) {
                params = content.substring(parenIndex + 1, content.length() - 1);
            } else {
                // 括号没有闭合，提取到末尾
                params = content.substring(parenIndex + 1);
            }

            result.put("marker", marker.trim());
            result.put("params", params.trim());
        } else {
            // 冒号格式 @MARKER:params
            String[] parts = content.split(":", 2);
            String marker = parts[0];
            String params = parts.length > 1 ? parts[1] : "";

            result.put("marker", marker.trim());
            result.put("params", params.trim());
        }

        log.info("[parseSpecialMarker] 解析特殊标记: expression={}, marker={}, params={}",
                expression, result.get("marker"), result.get("params"));

        return result;
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
            case "provinceName":
                return "省名称";
            case "cityName":
                return "市名称";
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

    private void normalizeGovernmentExecutionResult(Map<String, Object> executionResult, Integer year) {
        if (executionResult == null) {
            return;
        }
        Long modelId = null;
        Object modelIdObj = executionResult.get("modelId");
        if (modelIdObj instanceof Number) {
            modelId = ((Number) modelIdObj).longValue();
        } else if (modelIdObj instanceof String) {
            try {
                modelId = Long.parseLong((String) modelIdObj);
            } catch (NumberFormatException ignore) {
                modelId = null;
            }
        }
        String modelName = toString(executionResult.get("modelName"));
        boolean governmentModel = isGovernmentModel(modelId, modelName);
        boolean enterpriseModel = isEnterpriseModel(modelId, modelName);
        if (!governmentModel && !enterpriseModel) {
            return;
        }
        Set<String> regionCodes = new LinkedHashSet<>();
        Object tableDataObj = executionResult.get("tableData");
        if (tableDataObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableData = (List<Map<String, Object>>) tableDataObj;
            for (Map<String, Object> row : tableData) {
                if (row == null) {
                    continue;
                }
                String code = toString(row.get("regionCode"));
                if (!isEmptyString(code)) {
                    regionCodes.add(code);
                }
            }
        }
        Object stepResultsObj = executionResult.get("stepResultsList");
        if (stepResultsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepResultsList = (List<Map<String, Object>>) stepResultsObj;
            for (Map<String, Object> stepResult : stepResultsList) {
                if (stepResult == null) {
                    continue;
                }
                Object stepTableObj = stepResult.get("tableData");
                if (!(stepTableObj instanceof List)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stepTable = (List<Map<String, Object>>) stepTableObj;
                for (Map<String, Object> row : stepTable) {
                    if (row == null) {
                        continue;
                    }
                    String code = toString(row.get("regionCode"));
                    if (!isEmptyString(code)) {
                        regionCodes.add(code);
                    }
                }
            }
        }
        if (regionCodes.isEmpty()) {
            return;
        }
        Map<String, Map<String, String>> locationByCode = new HashMap<>();
        List<Map<String, Object>> locationRows = governmentModel
                ? queryGovernmentRows(new ArrayList<>(regionCodes), year)
                : queryEnterpriseRows(new ArrayList<>(regionCodes), year);
        for (Map<String, Object> row : locationRows) {
            if (row == null) {
                continue;
            }
            String code = toString(row.get("region_code"));
            if (isEmptyString(code)) {
                continue;
            }
            Map<String, String> location = new HashMap<>();
            location.put("provinceName", toString(row.get("province_name")));
            location.put("cityName", toString(row.get("city_name")));
            location.put("countyName", toString(row.get("county_name")));
            locationByCode.put(code, location);
        }
        if (tableDataObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableData = (List<Map<String, Object>>) tableDataObj;
            normalizeGovernmentTable(tableData, locationByCode);
        }
        Object columnsObj = executionResult.get("columns");
        if (columnsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> columns = (List<Map<String, Object>>) columnsObj;
            normalizeGovernmentColumns(columns);
        }
        if (stepResultsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepResultsList = (List<Map<String, Object>>) stepResultsObj;
            for (Map<String, Object> stepResult : stepResultsList) {
                if (stepResult == null) {
                    continue;
                }
                Object stepTableObj = stepResult.get("tableData");
                if (stepTableObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> stepTable = (List<Map<String, Object>>) stepTableObj;
                    normalizeGovernmentTable(stepTable, locationByCode);
                }
                Object stepColumnsObj = stepResult.get("columns");
                if (stepColumnsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> stepColumns = (List<Map<String, Object>>) stepColumnsObj;
                    normalizeGovernmentColumns(stepColumns);
                }
            }
            if (governmentModel) {
                recomputeGovernmentStepsByConfiguration(stepResultsList);
            }
        }
    }

    private void normalizeGovernmentTable(List<Map<String, Object>> tableData, Map<String, Map<String, String>> locationByCode) {
        if (tableData == null) {
            return;
        }
        boolean hasLocationMapping = locationByCode != null && !locationByCode.isEmpty();
        for (Map<String, Object> row : tableData) {
            if (row == null) {
                continue;
            }
            if (hasLocationMapping) {
                String code = toString(row.get("regionCode"));
                if (!isEmptyString(code)) {
                    Map<String, String> location = locationByCode.get(code);
                    if (location != null) {
                        String provinceName = location.get("provinceName");
                        String cityName = location.get("cityName");
                        String countyName = location.get("countyName");
                        if (!isEmptyString(provinceName)) {
                            row.put("provinceName", provinceName);
                        }
                        if (!isEmptyString(cityName)) {
                            row.put("cityName", cityName);
                        }
                        if (!isEmptyString(countyName)) {
                            row.put("regionName", countyName);
                        }
                    }
                }
            }
            row.remove("townshipName");
            row.remove("communityName");
            sanitizeNonFiniteValues(row);
        }
    }

    private void sanitizeNonFiniteValues(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            if ("regionCode".equals(key) || "provinceName".equals(key) || "cityName".equals(key)
                    || "regionName".equals(key) || "townshipName".equals(key) || "communityName".equals(key)) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof Number) {
                double numericValue = ((Number) value).doubleValue();
                if (Double.isNaN(numericValue) || Double.isInfinite(numericValue)) {
                    entry.setValue(0.0);
                }
                continue;
            }
            if (value instanceof String) {
                String stringValue = ((String) value).trim();
                if (stringValue.isEmpty()) {
                    continue;
                }
                if ("#DIV/0!".equalsIgnoreCase(stringValue) || "NaN".equalsIgnoreCase(stringValue)
                        || "Infinity".equalsIgnoreCase(stringValue) || "-Infinity".equalsIgnoreCase(stringValue)) {
                    entry.setValue(0.0);
                }
            }
        }
    }

    private void recomputeGovernmentStepsByConfiguration(List<Map<String, Object>> stepResultsList) {
        if (stepResultsList == null || stepResultsList.isEmpty()) {
            return;
        }
        Map<Integer, Map<String, Object>> stepByOrder = new LinkedHashMap<>();
        for (Map<String, Object> stepResult : stepResultsList) {
            if (stepResult == null) {
                continue;
            }
            Integer stepOrder = parseStepOrder(stepResult.get("stepOrder"));
            if (stepOrder == null) {
                continue;
            }
            stepByOrder.put(stepOrder, stepResult);
        }
        List<Integer> sortedStepOrders = stepByOrder.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        for (Integer targetStepOrder : sortedStepOrders) {
            if (targetStepOrder == null || targetStepOrder <= 4) {
                continue;
            }
            Map<String, Object> sourceStep = stepByOrder.get(targetStepOrder - 1);
            Map<String, Object> targetStep = stepByOrder.get(targetStepOrder);
            if (sourceStep == null || targetStep == null) {
                continue;
            }
            Long stepId = parseLongObject(targetStep.get("stepId"));
            if (stepId == null) {
                continue;
            }
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("step_id", stepId)
                    .eq("status", 1)
                    .orderByAsc("algorithm_order");
            List<StepAlgorithm> stepAlgorithms = stepAlgorithmMapper.selectList(queryWrapper);
            if (stepAlgorithms == null || stepAlgorithms.isEmpty()) {
                continue;
            }
            recomputeGovernmentStepWithAlgorithms(sourceStep, targetStep, stepAlgorithms, targetStepOrder);
        }
    }

    private void recomputeGovernmentStepWithAlgorithms(
            Map<String, Object> sourceStep,
            Map<String, Object> targetStep,
            List<StepAlgorithm> algorithms,
            Integer targetStepOrder) {
        if (sourceStep == null || targetStep == null || algorithms == null || algorithms.isEmpty()) {
            return;
        }
        Object sourceTableObj = sourceStep.get("tableData");
        Object targetTableObj = targetStep.get("tableData");
        if (!(sourceTableObj instanceof List) || !(targetTableObj instanceof List)) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sourceTable = (List<Map<String, Object>>) sourceTableObj;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> targetTable = (List<Map<String, Object>>) targetTableObj;
        if (sourceTable.isEmpty() || targetTable.isEmpty()) {
            return;
        }
        Map<String, Map<String, Object>> sourceByCode = new LinkedHashMap<>();
        for (Map<String, Object> row : sourceTable) {
            if (row == null) {
                continue;
            }
            String regionCode = toString(row.get("regionCode"));
            if (isEmptyString(regionCode)) {
                continue;
            }
            sourceByCode.put(regionCode, row);
        }
        if (sourceByCode.isEmpty()) {
            return;
        }
        Map<String, Map<String, Object>> targetByCode = new LinkedHashMap<>();
        Map<String, Map<String, Object>> allRegionContexts = new LinkedHashMap<>();
        for (Map<String, Object> targetRow : targetTable) {
            if (targetRow == null) {
                continue;
            }
            String regionCode = toString(targetRow.get("regionCode"));
            if (isEmptyString(regionCode)) {
                continue;
            }
            Map<String, Object> sourceRow = sourceByCode.get(regionCode);
            if (sourceRow == null) {
                continue;
            }
            targetByCode.put(regionCode, targetRow);
            allRegionContexts.put(regionCode, new LinkedHashMap<>(sourceRow));
        }
        if (targetByCode.isEmpty() || allRegionContexts.isEmpty()) {
            return;
        }
        List<StepAlgorithm> nonGradeAlgorithms = new ArrayList<>();
        List<StepAlgorithm> gradeAlgorithms = new ArrayList<>();
        for (StepAlgorithm algorithm : algorithms) {
            if (algorithm == null || isEmptyString(algorithm.getOutputParam()) || isEmptyString(algorithm.getQlExpression())) {
                continue;
            }
            Map<String, String> markerInfo = parseSpecialMarker(algorithm.getQlExpression().trim());
            if ("GRADE".equalsIgnoreCase(markerInfo.get("marker"))) {
                gradeAlgorithms.add(algorithm);
            } else {
                nonGradeAlgorithms.add(algorithm);
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, String> outputToAlgorithmName = (Map<String, String>) targetStep.get("outputToAlgorithmName");
        if (outputToAlgorithmName == null) {
            outputToAlgorithmName = new LinkedHashMap<>();
            targetStep.put("outputToAlgorithmName", outputToAlgorithmName);
        }

        for (String regionCode : targetByCode.keySet()) {
            Map<String, Object> regionContext = allRegionContexts.get(regionCode);
            Map<String, Object> targetRow = targetByCode.get(regionCode);
            for (StepAlgorithm algorithm : nonGradeAlgorithms) {
                Object result = executeConfiguredAlgorithm(regionCode, regionContext, allRegionContexts, algorithm);
                String outputParam = algorithm.getOutputParam().trim();
                regionContext.put(outputParam, result);
                targetRow.put(outputParam, result);
                outputToAlgorithmName.put(outputParam, toString(algorithm.getAlgorithmName()));
            }
            sanitizeNonFiniteValues(targetRow);
        }

        if (!gradeAlgorithms.isEmpty()) {
            Set<String> gradeScoreFields = new LinkedHashSet<>();
            for (StepAlgorithm algorithm : gradeAlgorithms) {
                Map<String, String> markerInfo = parseSpecialMarker(algorithm.getQlExpression().trim());
                String params = markerInfo.get("params");
                if (!isEmptyString(params)) {
                    gradeScoreFields.add(params.trim());
                }
            }
            if (!gradeScoreFields.isEmpty()) {
                Map<String, double[]> gradeStats = buildGradeStats(gradeScoreFields, allRegionContexts);
                if (!gradeStats.isEmpty()) {
                    for (Map<String, Object> regionContext : allRegionContexts.values()) {
                        regionContext.put("gradeStats", gradeStats);
                    }
                }
            }
            for (String regionCode : targetByCode.keySet()) {
                Map<String, Object> regionContext = allRegionContexts.get(regionCode);
                Map<String, Object> targetRow = targetByCode.get(regionCode);
                for (StepAlgorithm algorithm : gradeAlgorithms) {
                    Object result = executeConfiguredAlgorithm(regionCode, regionContext, allRegionContexts, algorithm);
                    String outputParam = algorithm.getOutputParam().trim();
                    regionContext.put(outputParam, result);
                    targetRow.put(outputParam, result);
                    outputToAlgorithmName.put(outputParam, toString(algorithm.getAlgorithmName()));
                }
                sanitizeNonFiniteValues(targetRow);
            }
        }

        List<Map<String, Object>> refreshedColumns = generateColumnsForStep(targetStep, targetTable, targetStepOrder);
        targetStep.put("columns", refreshedColumns);
    }

    private Object executeConfiguredAlgorithm(
            String regionCode,
            Map<String, Object> regionContext,
            Map<String, Map<String, Object>> allRegionContexts,
            StepAlgorithm algorithm) {
        String qlExpression = algorithm.getQlExpression();
        if (isEmptyString(qlExpression)) {
            return null;
        }
        qlExpression = qlExpression.trim();
        try {
            Object result;
            if (qlExpression.startsWith("@")) {
                Map<String, String> markerInfo = parseSpecialMarker(qlExpression);
                String marker = markerInfo.getOrDefault("marker", "");
                String params = markerInfo.getOrDefault("params", "");
                result = specialAlgorithmService.executeSpecialAlgorithm(
                        marker, params, regionCode, regionContext, allRegionContexts);
            } else {
                String rewrittenExpression = rewriteLegacyWeightExpressionIfNeeded(algorithm, qlExpression, regionContext);
                String normalizedExpression = normalizeWeightVarCodes(rewrittenExpression);
                prepareNullVariablesForExpression(normalizedExpression, regionContext);
                result = qlExpressService.execute(normalizedExpression, regionContext);
            }
            if (result instanceof Number) {
                double numeric = ((Number) result).doubleValue();
                if (Double.isNaN(numeric) || Double.isInfinite(numeric)) {
                    numeric = 0.0;
                }
                return Double.parseDouble(String.format("%.8f", numeric));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("算法 " + algorithm.getAlgorithmName() + " 执行失败: " + e.getMessage(), e);
        }
    }

    private Long parseLongObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong(((String) value).trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private Integer parseStepOrder(Object stepOrderObj) {
        if (stepOrderObj == null) {
            return null;
        }
        if (stepOrderObj instanceof Number) {
            return ((Number) stepOrderObj).intValue();
        }
        if (stepOrderObj instanceof String) {
            try {
                return Integer.parseInt(((String) stepOrderObj).trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private void normalizeGovernmentColumns(List<Map<String, Object>> columns) {
        if (columns == null || columns.isEmpty()) {
            return;
        }
        Set<String> seen = new LinkedHashSet<>();
        List<Map<String, Object>> normalized = new ArrayList<>();
        Map<String, Object> regionCodeColumn = null;
        Map<String, Object> regionNameColumn = null;
        for (Map<String, Object> column : columns) {
            if (column == null) {
                continue;
            }
            String prop = toString(column.get("prop"));
            if (isEmptyString(prop)) {
                continue;
            }
            if ("townshipName".equals(prop) || "communityName".equals(prop)) {
                continue;
            }
            if ("regionCode".equals(prop)) {
                column.put("label", "行政区代码");
                regionCodeColumn = column;
            } else if ("regionName".equals(prop) || "region".equals(prop)) {
                column.put("label", "县名称");
                regionNameColumn = column;
            } else if ("provinceName".equals(prop)) {
                column.put("label", "省名称");
            } else if ("cityName".equals(prop)) {
                column.put("label", "市名称");
            }
            if (seen.add(prop)) {
                normalized.add(column);
            }
        }
        if (!seen.contains("provinceName")) {
            Map<String, Object> provinceColumn = new LinkedHashMap<>();
            provinceColumn.put("prop", "provinceName");
            provinceColumn.put("label", "省名称");
            provinceColumn.put("width", 120);
            normalized.add(provinceColumn);
        }
        if (!seen.contains("cityName")) {
            Map<String, Object> cityColumn = new LinkedHashMap<>();
            cityColumn.put("prop", "cityName");
            cityColumn.put("label", "市名称");
            cityColumn.put("width", 120);
            normalized.add(cityColumn);
        }
        if (regionCodeColumn == null) {
            Map<String, Object> codeColumn = new LinkedHashMap<>();
            codeColumn.put("prop", "regionCode");
            codeColumn.put("label", "行政区代码");
            codeColumn.put("width", 150);
            normalized.add(0, codeColumn);
        }
        if (regionNameColumn == null) {
            Map<String, Object> countyColumn = new LinkedHashMap<>();
            countyColumn.put("prop", "regionName");
            countyColumn.put("label", "县名称");
            countyColumn.put("width", 120);
            normalized.add(countyColumn);
        }
        List<Map<String, Object>> ordered = new ArrayList<>();
        for (String baseProp : Arrays.asList("regionCode", "provinceName", "cityName", "regionName")) {
            for (Map<String, Object> column : normalized) {
                if (baseProp.equals(toString(column.get("prop")))) {
                    ordered.add(column);
                    break;
                }
            }
        }
        for (Map<String, Object> column : normalized) {
            String prop = toString(column.get("prop"));
            if (!"regionCode".equals(prop) && !"provinceName".equals(prop) && !"cityName".equals(prop) && !"regionName".equals(prop)) {
                ordered.add(column);
            }
        }
        columns.clear();
        columns.addAll(ordered);
    }

    /**
     * 获取执行记录详情
     *
     * @param executionRecordId 执行记录ID
     * @return 执行记录详情
     */
    @Override
    public Map<String, Object> getExecutionRecordDetail(Long executionRecordId) {
        try {
            // 获取执行记录
            ModelExecutionRecord executionRecord = modelExecutionRecordMapper.selectById(executionRecordId);
            if (executionRecord == null) {
                throw new RuntimeException("执行记录不存在");
            }

            // 获取该执行记录的所有评估结果
            QueryWrapper<EvaluationResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("execution_record_id", executionRecordId);
            List<EvaluationResult> evaluationResults = evaluationResultMapper.selectList(queryWrapper);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("executionRecord", executionRecord);
            result.put("evaluationResults", evaluationResults);
            result.put("totalResults", evaluationResults.size());

            String resultDetailJson = executionRecord.getResultDetail();
            if (resultDetailJson != null && !resultDetailJson.trim().isEmpty()) {
                try {
                    Map<String, Object> executionResult = objectMapper.readValue(
                            resultDetailJson, new TypeReference<Map<String, Object>>() {});
                    normalizeGovernmentExecutionResult(executionResult, executionRecord.getYear());
                    result.put("executionResult", executionResult);
                } catch (Exception ignore) {
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取执行记录详情失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取评估历史列表（分页）
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param modelId 模型ID（可选）
     * @param executionStatus 执行状态（可选）
     * @param year 评估年份（可选）
     * @param county 区县名称（可选）
     * @return 分页的评估历史列表
     */
    @Override
    public Map<String, Object> getEvaluationHistoryList(Integer page, Integer size, Long modelId, String executionStatus) {
        return getEvaluationHistoryList(page, size, modelId, executionStatus, null, null);
    }

    /**
     * 获取评估历史列表（分页，支持更多筛选条件）
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param modelId 模型ID（可选）
     * @param executionStatus 执行状态（可选）
     * @param year 评估年份（可选）
     * @param orgCode 机构代码（可选，如区县代码511425）
     * @return 分页的评估历史列表
     */
    @Override
    public Map<String, Object> getEvaluationHistoryList(Integer page, Integer size, Long modelId, String executionStatus, Integer year, String orgCode) {
        try {
            // 创建分页对象
            Page<ModelExecutionRecord> pageRequest = new Page<>(page, size);

            // 构建查询条件
            QueryWrapper<ModelExecutionRecord> queryWrapper = new QueryWrapper<>();

            if (modelId != null) {
                queryWrapper.eq("model_id", modelId);
            }

            if (executionStatus != null && !executionStatus.isEmpty()) {
                queryWrapper.eq("execution_status", executionStatus);
            }

            if (year != null) {
                queryWrapper.eq("year", year);
            }

            if (orgCode != null && !orgCode.isEmpty()) {
                queryWrapper.eq("org_code", orgCode);
            }

            // 按开始时间倒序排列
            queryWrapper.orderByDesc("start_time");

            // 执行分页查询
            Page<ModelExecutionRecord> resultPage = modelExecutionRecordMapper.selectPage(pageRequest, queryWrapper);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", resultPage.getRecords());
            result.put("total", resultPage.getTotal());
            result.put("current", resultPage.getCurrent());
            result.put("size", resultPage.getSize());
            result.put("pages", resultPage.getPages());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取评估历史列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> checkEvaluationData(Long modelId, List<String> regionCodes, Integer year, String orgCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("exists", true);
        result.put("message", "");

        try {
            // 根据评估模型类型检查所需的数据：
            // - Model 3: 乡镇减灾能力评估模型 - 需要乡镇数据
            // - Model 4: 社区-行政村减灾能力评估模型 - 需要社区数据
            // - Model 8: 社区-乡镇减灾能力评估模型 - 需要社区数据
            // - Model 11: 综合减灾能力评估模型 - 需要乡镇减灾能力评估结果(Model 3)和社区-乡镇减灾能力评估结果(Model 8)
            if (year != null) {
                EvaluationModel model = modelId == null ? null : evaluationModelMapper.selectById(modelId);
                boolean governmentModel = isGovernmentModel(modelId, model != null ? model.getModelName() : null);
                boolean enterpriseModel = isEnterpriseModel(modelId, model != null ? model.getModelName() : null);
                boolean socialOrganizationModel = isSocialOrganizationModel(modelId, model != null ? model.getModelName() : null);
                for (String regionCode : regionCodes) {
                    if (governmentModel) {
                        if (!hasGovernmentData(regionCode, year)) {
                            result.put("exists", false);
                            result.put("message", "所选年份无政府减灾能力数据，无法进行政府减灾能力评估");
                            return result;
                        }
                    } else if (enterpriseModel) {
                        if (!hasEnterpriseData(regionCode, year)) {
                            result.put("exists", false);
                            result.put("message", "所选年份无企业减灾能力数据，无法进行企业减灾能力评估");
                            return result;
                        }
                    } else if (socialOrganizationModel) {
                        if (!hasSocialOrganizationData(regionCode, year)) {
                            result.put("exists", false);
                            result.put("message", "所选年份无社会组织减灾能力数据，无法进行社会组织减灾能力评估");
                            return result;
                        }
                    } else if (modelId == 4 || modelId == 8) {
                        // 社区-行政村/乡镇减灾能力评估模型：检查社区数据
                        QueryWrapper<CommunityDisasterReductionCapacity> q = new QueryWrapper<>();
                        q.eq("region_code", regionCode).eq("year", year);
                        CommunityDisasterReductionCapacity exists = communityDataMapper.selectOne(q);
                        if (exists == null) {
                            result.put("exists", false);
                            result.put("message", "所选年份无社区数据，无法进行社区-行政村/乡镇减灾能力评估");
                            return result;
                        }
                    } else if (modelId == 11) {
                        // 综合减灾能力评估模型：需要检查评估历史表中是否存在Model 3和Model 8的评估结果
                        // 检查是否存在乡镇减灾能力评估结果（Model 3）
                        List<EvaluationResult> townshipEvalResults = evaluationResultMapper.selectByModelIdAndYearAndOrgCode(3L, year, orgCode != null ? orgCode : "511425");
                        boolean hasTownshipResult = townshipEvalResults != null && townshipEvalResults.stream()
                                .anyMatch(r -> regionCode.equals(r.getRegionCode()));
                        if (!hasTownshipResult) {
                            result.put("exists", false);
                            result.put("message", "所选年份无乡镇减灾能力评估结果（请先执行乡镇减灾能力评估模型），综合减灾能力评估需要乡镇减灾能力评估结果和社区-乡镇减灾能力评估结果");
                            return result;
                        }

                        // 检查是否存在社区-乡镇减灾能力评估结果（Model 8）
                        List<EvaluationResult> communityEvalResults = evaluationResultMapper.selectByModelIdAndYearAndOrgCode(8L, year, orgCode != null ? orgCode : "511425");
                        boolean hasCommunityResult = communityEvalResults != null && communityEvalResults.stream()
                                .anyMatch(r -> regionCode.equals(r.getRegionCode()));
                        if (!hasCommunityResult) {
                            result.put("exists", false);
                            result.put("message", "所选年份无社区-乡镇减灾能力评估结果（请先执行社区-乡镇减灾能力评估模型），综合减灾能力评估需要乡镇减灾能力评估结果和社区-乡镇减灾能力评估结果");
                            return result;
                        }
                    } else {
                        // 默认（Model 3 乡镇减灾能力评估模型等）：检查乡镇数据
                        QueryWrapper<SurveyData> q = new QueryWrapper<>();
                        q.eq("region_code", regionCode).eq("year", year);
                        SurveyData exists = surveyDataMapper.selectOne(q);
                        if (exists == null) {
                            result.put("exists", false);
                            result.put("message", "所选年份无乡镇数据，无法进行乡镇减灾能力评估");
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            result.put("exists", false);
            result.put("message", "检查数据时发生错误: " + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEvaluationHistory(Long executionRecordId) {
        try {
            log.info("开始删除评估历史记录，executionRecordId={}", executionRecordId);

            // 1. 先检查记录是否存在
            ModelExecutionRecord record = modelExecutionRecordMapper.selectById(executionRecordId);
            if (record == null) {
                log.warn("评估历史记录不存在，executionRecordId={}", executionRecordId);
                return false;
            }

            // 2. 删除关联的 evaluation_result 记录
            if (evaluationResultService != null) {
                evaluationResultService.deleteByExecutionRecordId(executionRecordId);
                log.info("已删除关联的 evaluation_result 记录");
            } else {
                // 直接使用 mapper 删除
                QueryWrapper<EvaluationResult> resultQueryWrapper = new QueryWrapper<>();
                resultQueryWrapper.eq("execution_record_id", executionRecordId);
                int deletedResults = evaluationResultMapper.delete(resultQueryWrapper);
                log.info("直接删除了 {} 条 evaluation_result 记录", deletedResults);
            }

            // 3. 删除 model_execution_record 记录
            int deletedRecords = modelExecutionRecordMapper.deleteById(executionRecordId);

            log.info("成功删除评估历史记录，executionRecordId={}, 删除记录数={}", executionRecordId, deletedRecords);
            return true;
        } catch (Exception e) {
            log.error("删除评估历史记录失败，executionRecordId={}", executionRecordId, e);
            throw new RuntimeException("删除评估历史记录失败: " + e.getMessage(), e);
        }
    }
}
