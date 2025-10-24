package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.mapper.StepAlgorithmMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.service.TOPSISConfigMigrationService;
import com.evaluate.service.TOPSISParameterParser;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * TOPSIS配置迁移服务实现类
 * 
 * 负责分析现有TOPSIS算法配置并迁移到新格式：
 * 1. 分析现有的TOPSIS算法配置
 * 2. 实现自动迁移现有配置到新格式
 * 3. 提供配置验证和修复功能
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISConfigMigrationServiceImpl implements TOPSISConfigMigrationService {

    private static final Logger log = LoggerFactory.getLogger(TOPSISConfigMigrationServiceImpl.class);

    
    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;
    
    @Autowired
    private TOPSISParameterParser parameterParser;
    

    
    // 迁移历史记录（简单实现，生产环境应使用数据库）
    private final List<Map<String, Object>> migrationHistory = new ArrayList<>();
    
    @Override
    public Map<String, Object> analyzeExistingConfigurations() {
        log.info("开始分析现有TOPSIS算法配置");
        
        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> configurations = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        
        try {
            // 查询所有TOPSIS相关的算法配置
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("algorithm_code", "TOPSIS");
            List<StepAlgorithm> topsisConfigs = stepAlgorithmMapper.selectList(queryWrapper);
            
            if (topsisConfigs == null || topsisConfigs.isEmpty()) {
                log.warn("未找到TOPSIS算法配置");
                analysis.put("totalConfigurations", 0);
                analysis.put("configurations", configurations);
                analysis.put("issues", Arrays.asList("未找到任何TOPSIS算法配置"));
                return analysis;
            }
            
            log.info("找到 {} 个TOPSIS配置", topsisConfigs.size());
            
            for (StepAlgorithm config : topsisConfigs) {
                Map<String, Object> configAnalysis = analyzeConfiguration(config);
                configurations.add(configAnalysis);
                
                @SuppressWarnings("unchecked")
                List<String> configIssues = (List<String>) configAnalysis.get("issues");
                if (configIssues != null && !configIssues.isEmpty()) {
                    issues.addAll(configIssues);
                }
            }
            
            // 统计分析结果
            long validConfigs = configurations.stream()
                    .mapToLong(config -> (Boolean) config.get("isValid") ? 1 : 0)
                    .sum();
            
            long needsMigration = configurations.stream()
                    .mapToLong(config -> (Boolean) config.get("needsMigration") ? 1 : 0)
                    .sum();
            
            analysis.put("totalConfigurations", topsisConfigs.size());
            analysis.put("validConfigurations", validConfigs);
            analysis.put("configurationsNeedingMigration", needsMigration);
            analysis.put("configurations", configurations);
            analysis.put("issues", issues);
            analysis.put("analysisTime", new Date());
            
            log.info("配置分析完成 - 总数: {}, 有效: {}, 需要迁移: {}", 
                    topsisConfigs.size(), validConfigs, needsMigration);
            
        } catch (Exception e) {
            log.error("分析TOPSIS配置时发生异常", e);
            analysis.put("error", "分析失败: " + e.getMessage());
            issues.add("分析过程中发生异常: " + e.getMessage());
            analysis.put("issues", issues);
        }
        
        return analysis;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> migrateConfiguration(Long modelId, String stepCode, List<String> indicators) {
        log.info("迁移TOPSIS配置 - 模型ID: {}, 步骤: {}, 指标: {}", modelId, stepCode, indicators);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找现有配置
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("step_id", modelId); // 假设step_id关联到模型
            queryWrapper.eq("algorithm_code", "TOPSIS");
            StepAlgorithm existingConfig = stepAlgorithmMapper.selectOne(queryWrapper);
            
            if (existingConfig == null) {
                log.warn("未找到模型 {} 步骤 {} 的算法配置", modelId, stepCode);
                result.put("success", false);
                result.put("error", "未找到现有配置");
                return result;
            }
            
            // 备份原始配置
            String originalExpression = existingConfig.getQlExpression();
            
            // 构建新的ql_expression
            String newExpression = buildNewExpression(indicators, existingConfig.getOutputParam());
            
            // 更新配置
            existingConfig.setQlExpression(newExpression);
            existingConfig.setCreateTime(LocalDateTime.now()); // 使用createTime作为更新时间
            
            int updateResult = stepAlgorithmMapper.updateById(existingConfig);
            
            if (updateResult > 0) {
                log.info("配置迁移成功 - 模型: {}, 步骤: {}", modelId, stepCode);
                
                // 记录迁移历史
                Map<String, Object> historyRecord = new HashMap<>();
                historyRecord.put("modelId", modelId);
                historyRecord.put("stepCode", stepCode);
                historyRecord.put("originalExpression", originalExpression);
                historyRecord.put("newExpression", newExpression);
                historyRecord.put("indicators", new ArrayList<>(indicators));
                historyRecord.put("migrationTime", new Date());
                historyRecord.put("success", true);
                migrationHistory.add(historyRecord);
                
                result.put("success", true);
                result.put("originalExpression", originalExpression);
                result.put("newExpression", newExpression);
                result.put("indicators", indicators);
                result.put("migrationTime", new Date());
                
            } else {
                log.error("配置更新失败 - 模型: {}, 步骤: {}", modelId, stepCode);
                result.put("success", false);
                result.put("error", "数据库更新失败");
            }
            
        } catch (Exception e) {
            log.error("迁移配置时发生异常 - 模型: {}, 步骤: {}", modelId, stepCode, e);
            result.put("success", false);
            result.put("error", "迁移失败: " + e.getMessage());
            
            // 记录失败的迁移历史
            Map<String, Object> historyRecord = new HashMap<>();
            historyRecord.put("modelId", modelId);
            historyRecord.put("stepCode", stepCode);
            historyRecord.put("indicators", indicators);
            historyRecord.put("migrationTime", new Date());
            historyRecord.put("success", false);
            historyRecord.put("error", e.getMessage());
            migrationHistory.add(historyRecord);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> migrateAllConfigurations() {
        log.info("开始批量迁移所有TOPSIS配置");
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> migrationResults = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        try {
            // 先分析现有配置
            Map<String, Object> analysis = analyzeExistingConfigurations();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> configurations = (List<Map<String, Object>>) analysis.get("configurations");
            
            if (configurations == null || configurations.isEmpty()) {
                result.put("success", false);
                result.put("error", "没有找到需要迁移的配置");
                return result;
            }
            
            // 逐个迁移配置
            for (Map<String, Object> config : configurations) {
                Boolean needsMigration = (Boolean) config.get("needsMigration");
                
                if (needsMigration != null && needsMigration) {
                    Long modelId = (Long) config.get("modelId");
                    String stepCode = (String) config.get("stepCode");
                    
                    // 尝试自动推断指标列表
                    List<String> suggestedIndicators = suggestIndicatorsForModel(modelId);
                    
                    if (!suggestedIndicators.isEmpty()) {
                        Map<String, Object> migrationResult = migrateConfiguration(modelId, stepCode, suggestedIndicators);
                        migrationResults.add(migrationResult);
                        
                        if ((Boolean) migrationResult.get("success")) {
                            successCount++;
                        } else {
                            failureCount++;
                        }
                    } else {
                        log.warn("无法为模型 {} 推断指标列表，跳过迁移", modelId);
                        Map<String, Object> skipResult = new HashMap<>();
                        skipResult.put("modelId", modelId);
                        skipResult.put("stepCode", stepCode);
                        skipResult.put("success", false);
                        skipResult.put("error", "无法推断指标列表");
                        migrationResults.add(skipResult);
                        failureCount++;
                    }
                }
            }
            
            result.put("success", successCount > 0);
            result.put("totalConfigurations", configurations.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("migrationResults", migrationResults);
            result.put("migrationTime", new Date());
            
            log.info("批量迁移完成 - 成功: {}, 失败: {}", successCount, failureCount);
            
        } catch (Exception e) {
            log.error("批量迁移配置时发生异常", e);
            result.put("success", false);
            result.put("error", "批量迁移失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateMigratedConfiguration(Long modelId, String stepCode) {
        log.info("验证迁移后的配置 - 模型: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> validation = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            // 查找配置
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("step_id", modelId);
            queryWrapper.eq("algorithm_code", "TOPSIS");
            StepAlgorithm config = stepAlgorithmMapper.selectOne(queryWrapper);
            
            if (config == null) {
                validation.put("valid", false);
                validation.put("error", "配置不存在");
                return validation;
            }
            
            // 解析配置
            TOPSISAlgorithmConfig algorithmConfig = parameterParser.parseFromExpression(
                    config.getQlExpression(), config);
            
            if (algorithmConfig == null || !algorithmConfig.isValid()) {
                issues.add("算法配置解析失败或无效");
            } else {
                // 验证指标是否存在于模型数据中
                List<String> availableIndicators = getAvailableIndicatorsForModel(modelId);
                
                for (String indicator : algorithmConfig.getIndicators()) {
                    if (!availableIndicators.contains(indicator)) {
                        issues.add("指标 " + indicator + " 在模型数据中不存在");
                    }
                }
                
                // 验证配置格式
                if (algorithmConfig.getIndicators().isEmpty()) {
                    issues.add("指标列表为空");
                }
                
                if (algorithmConfig.getOutputParam() == null || algorithmConfig.getOutputParam().trim().isEmpty()) {
                    issues.add("输出参数为空");
                }
            }
            
            boolean isValid = issues.isEmpty();
            validation.put("valid", isValid);
            validation.put("issues", issues);
            validation.put("algorithmConfig", algorithmConfig);
            validation.put("validationTime", new Date());
            
            if (isValid) {
                log.info("配置验证通过 - 模型: {}, 步骤: {}", modelId, stepCode);
            } else {
                log.warn("配置验证失败 - 模型: {}, 步骤: {}, 问题: {}", modelId, stepCode, issues);
            }
            
        } catch (Exception e) {
            log.error("验证配置时发生异常 - 模型: {}, 步骤: {}", modelId, stepCode, e);
            validation.put("valid", false);
            validation.put("error", "验证失败: " + e.getMessage());
        }
        
        return validation;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> repairConfiguration(Long modelId, String stepCode, List<String> issues) {
        log.info("修复配置 - 模型: {}, 步骤: {}, 问题: {}", modelId, stepCode, issues);
        
        Map<String, Object> result = new HashMap<>();
        List<String> repairActions = new ArrayList<>();
        
        try {
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("step_id", modelId);
            queryWrapper.eq("algorithm_code", "TOPSIS");
            StepAlgorithm config = stepAlgorithmMapper.selectOne(queryWrapper);
            
            if (config == null) {
                result.put("success", false);
                result.put("error", "配置不存在");
                return result;
            }
            
            boolean needsUpdate = false;
            String originalExpression = config.getQlExpression();
            
            // 根据问题类型进行修复
            for (String issue : issues) {
                if (issue.contains("指标") && issue.contains("不存在")) {
                    // 修复不存在的指标
                    List<String> availableIndicators = getAvailableIndicatorsForModel(modelId);
                    if (!availableIndicators.isEmpty()) {
                        String newExpression = buildNewExpression(availableIndicators, config.getOutputParam());
                        config.setQlExpression(newExpression);
                        needsUpdate = true;
                        repairActions.add("使用可用指标重新构建表达式");
                    }
                } else if (issue.contains("指标列表为空")) {
                    // 修复空指标列表
                    List<String> suggestedIndicators = suggestIndicatorsForModel(modelId);
                    if (!suggestedIndicators.isEmpty()) {
                        String newExpression = buildNewExpression(suggestedIndicators, config.getOutputParam());
                        config.setQlExpression(newExpression);
                        needsUpdate = true;
                        repairActions.add("添加推荐的指标列表");
                    }
                } else if (issue.contains("输出参数为空")) {
                    // 修复空输出参数
                    if (config.getOutputParam() == null || config.getOutputParam().trim().isEmpty()) {
                        config.setOutputParam("comprehensive");
                        needsUpdate = true;
                        repairActions.add("设置默认输出参数为 'comprehensive'");
                    }
                }
            }
            
            if (needsUpdate) {
                config.setCreateTime(LocalDateTime.now());
                int updateResult = stepAlgorithmMapper.updateById(config);
                
                if (updateResult > 0) {
                    result.put("success", true);
                    result.put("repairActions", repairActions);
                    result.put("originalExpression", originalExpression);
                    result.put("newExpression", config.getQlExpression());
                    result.put("repairTime", new Date());
                    
                    log.info("配置修复成功 - 模型: {}, 步骤: {}, 修复操作: {}", 
                            modelId, stepCode, repairActions);
                } else {
                    result.put("success", false);
                    result.put("error", "数据库更新失败");
                }
            } else {
                result.put("success", false);
                result.put("error", "无法自动修复这些问题");
                result.put("issues", issues);
            }
            
        } catch (Exception e) {
            log.error("修复配置时发生异常 - 模型: {}, 步骤: {}", modelId, stepCode, e);
            result.put("success", false);
            result.put("error", "修复失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rollbackMigration(Long modelId, String stepCode) {
        log.info("回滚配置迁移 - 模型: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找迁移历史记录
            Optional<Map<String, Object>> historyRecord = migrationHistory.stream()
                    .filter(record -> Objects.equals(record.get("modelId"), modelId) && 
                                    Objects.equals(record.get("stepCode"), stepCode))
                    .filter(record -> (Boolean) record.get("success"))
                    .max(Comparator.comparing(record -> (Date) record.get("migrationTime")));
            
            if (!historyRecord.isPresent()) {
                result.put("success", false);
                result.put("error", "未找到可回滚的迁移记录");
                return result;
            }
            
            Map<String, Object> history = historyRecord.get();
            String originalExpression = (String) history.get("originalExpression");
            
            // 恢复原始配置
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("step_id", modelId);
            queryWrapper.eq("algorithm_code", "TOPSIS");
            StepAlgorithm config = stepAlgorithmMapper.selectOne(queryWrapper);
            
            if (config == null) {
                result.put("success", false);
                result.put("error", "配置不存在");
                return result;
            }
            
            String currentExpression = config.getQlExpression();
            config.setQlExpression(originalExpression);
            config.setCreateTime(LocalDateTime.now());
            
            int updateResult = stepAlgorithmMapper.updateById(config);
            
            if (updateResult > 0) {
                result.put("success", true);
                result.put("originalExpression", originalExpression);
                result.put("rolledBackFrom", currentExpression);
                result.put("rollbackTime", new Date());
                
                log.info("配置回滚成功 - 模型: {}, 步骤: {}", modelId, stepCode);
            } else {
                result.put("success", false);
                result.put("error", "数据库更新失败");
            }
            
        } catch (Exception e) {
            log.error("回滚配置时发生异常 - 模型: {}, 步骤: {}", modelId, stepCode, e);
            result.put("success", false);
            result.put("error", "回滚失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getMigrationHistory() {
        return new ArrayList<>(migrationHistory);
    }
    
    /**
     * 分析单个配置
     */
    private Map<String, Object> analyzeConfiguration(StepAlgorithm config) {
        Map<String, Object> analysis = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        analysis.put("stepId", config.getStepId());
        analysis.put("modelId", config.getStepId()); // 使用stepId作为modelId
        analysis.put("stepCode", "step4"); // 默认步骤代码
        analysis.put("algorithmCode", config.getAlgorithmCode());
        analysis.put("qlExpression", config.getQlExpression());
        analysis.put("outputParam", config.getOutputParam());
        
        try {
            // 尝试解析现有配置
            TOPSISAlgorithmConfig algorithmConfig = parameterParser.parseFromExpression(
                    config.getQlExpression(), config);
            
            boolean isValid = algorithmConfig != null && algorithmConfig.isValid();
            boolean needsMigration = false;
            
            if (!isValid) {
                issues.add("配置解析失败或格式无效");
                needsMigration = true;
            } else if (algorithmConfig != null) {
                // 检查指标是否存在
                List<String> availableIndicators = getAvailableIndicatorsForModel(config.getStepId());
                
                if (algorithmConfig.getIndicators() != null) {
                    for (String indicator : algorithmConfig.getIndicators()) {
                        if (!availableIndicators.contains(indicator)) {
                            issues.add("指标 " + indicator + " 在模型数据中不存在");
                            needsMigration = true;
                        }
                    }
                }
                
                List<String> indicators = algorithmConfig.getIndicators();
                if (indicators == null || indicators.isEmpty()) {
                    issues.add("指标列表为空");
                    needsMigration = true;
                }
            }
            
            analysis.put("isValid", isValid);
            analysis.put("needsMigration", needsMigration);
            analysis.put("parsedConfig", algorithmConfig);
            
        } catch (Exception e) {
            log.error("分析配置时发生异常 - 步骤ID: {}", config.getStepId(), e);
            issues.add("分析过程中发生异常: " + e.getMessage());
            analysis.put("isValid", false);
            analysis.put("needsMigration", true);
        }
        
        analysis.put("issues", issues);
        return analysis;
    }
    
    /**
     * 构建新的ql_expression
     */
    private String buildNewExpression(List<String> indicators, String outputParam) {
        if (indicators == null || indicators.isEmpty()) {
            throw new IllegalArgumentException("指标列表不能为空");
        }
        
        String indicatorList = String.join(",", indicators);
        String algorithmType = outputParam != null && outputParam.contains("negative") ? 
                "TOPSIS_NEGATIVE" : "TOPSIS_POSITIVE";
        
        return "@" + algorithmType + ":" + indicatorList;
    }
    
    /**
     * 获取模型的可用指标列表
     */
    private List<String> getAvailableIndicatorsForModel(Long modelId) {
        // 根据模型ID返回预定义的指标列表
        if (modelId == 9L) { // 综合模型
            return Arrays.asList(
                "township_disasterMgmtScore", "township_disasterPrepScore", "township_selfRescueScore",
                "community_disasterMgmtScore", "community_disasterPrepScore", "community_selfRescueScore"
            );
        } else if (modelId == 3L) { // 标准模型
            return Arrays.asList("disasterMgmtScore", "disasterPrepScore", "selfRescueScore");
        } else if (modelId == 8L) { // 社区-乡镇模型
            return Arrays.asList("disasterMgmtScore", "disasterPrepScore", "selfRescueScore");
        }
        
        // 默认指标列表
        return Arrays.asList("score1", "score2", "score3");
    }
    
    /**
     * 为模型推荐指标列表
     */
    private List<String> suggestIndicatorsForModel(Long modelId) {
        try {
            List<String> availableIndicators = getAvailableIndicatorsForModel(modelId);
            
            // 根据模型类型推荐指标
            if (modelId == 9L) { // 综合模型
                return availableIndicators.stream()
                        .filter(indicator -> indicator.contains("Score") || indicator.contains("score"))
                        .collect(Collectors.toList());
            } else if (modelId == 3L) { // 标准模型
                return availableIndicators.stream()
                        .filter(indicator -> indicator.contains("disaster") || indicator.contains("rescue"))
                        .collect(Collectors.toList());
            } else if (modelId == 8L) { // 社区-乡镇模型
                return availableIndicators.stream()
                        .filter(indicator -> indicator.contains("community") || indicator.contains("township"))
                        .collect(Collectors.toList());
            }
            
            // 默认返回所有可用指标
            return availableIndicators;
            
        } catch (Exception e) {
            log.error("为模型 {} 推荐指标时发生异常", modelId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> analyzeMigration(Long modelId) {
        log.info("分析模型 {} 的迁移需求", modelId);
        
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // 查找该模型的TOPSIS配置
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("step_id", modelId);
            queryWrapper.eq("algorithm_code", "TOPSIS");
            StepAlgorithm config = stepAlgorithmMapper.selectOne(queryWrapper);
            
            if (config == null) {
                analysis.put("needsMigration", false);
                analysis.put("reason", "未找到TOPSIS配置");
                return analysis;
            }
            
            // 分析配置
            Map<String, Object> configAnalysis = analyzeConfiguration(config);
            
            analysis.put("modelId", modelId);
            analysis.put("needsMigration", configAnalysis.get("needsMigration"));
            analysis.put("isValid", configAnalysis.get("isValid"));
            analysis.put("issues", configAnalysis.get("issues"));
            analysis.put("currentConfig", configAnalysis);
            analysis.put("suggestedIndicators", suggestIndicatorsForModel(modelId));
            analysis.put("analysisTime", new Date());
            
        } catch (Exception e) {
            log.error("分析模型 {} 迁移需求时发生异常", modelId, e);
            analysis.put("error", "分析失败: " + e.getMessage());
        }
        
        return analysis;
    }
    
    @Override
    public Map<String, Object> executeMigration(Long modelId, boolean dryRun) {
        log.info("执行模型 {} 的迁移，试运行: {}", modelId, dryRun);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 先分析迁移需求
            Map<String, Object> analysis = analyzeMigration(modelId);
            Boolean needsMigration = (Boolean) analysis.get("needsMigration");
            
            if (needsMigration == null || !needsMigration) {
                result.put("success", false);
                result.put("reason", "不需要迁移");
                result.put("analysis", analysis);
                return result;
            }
            
            // 获取推荐的指标
            @SuppressWarnings("unchecked")
            List<String> suggestedIndicators = (List<String>) analysis.get("suggestedIndicators");
            
            if (suggestedIndicators == null || suggestedIndicators.isEmpty()) {
                result.put("success", false);
                result.put("reason", "无法推断指标列表");
                return result;
            }
            
            if (dryRun) {
                // 试运行模式，只返回将要执行的操作
                result.put("success", true);
                result.put("dryRun", true);
                result.put("plannedActions", Arrays.asList(
                    "更新TOPSIS配置",
                    "使用指标: " + String.join(", ", suggestedIndicators)
                ));
                result.put("suggestedIndicators", suggestedIndicators);
                result.put("analysis", analysis);
            } else {
                // 实际执行迁移
                Map<String, Object> migrationResult = migrateConfiguration(modelId, "step3", suggestedIndicators);
                result.putAll(migrationResult);
                result.put("dryRun", false);
                result.put("analysis", analysis);
            }
            
        } catch (Exception e) {
            log.error("执行模型 {} 迁移时发生异常", modelId, e);
            result.put("success", false);
            result.put("error", "迁移失败: " + e.getMessage());
        }
        
        return result;
    }
}
