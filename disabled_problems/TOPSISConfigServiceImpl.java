package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.entity.ModelStep;
import com.evaluate.mapper.StepAlgorithmMapper;
import com.evaluate.mapper.ModelStepMapper;
import com.evaluate.service.TOPSISConfigService;
import com.evaluate.service.TOPSISParameterParser;
import com.evaluate.service.IndicatorAnalysisService;
import com.evaluate.service.TOPSISConfigValidationService;
import com.evaluate.dto.topsis.IndicatorMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TOPSIS配置服务实现类
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISConfigServiceImpl implements TOPSISConfigService {

    private static final Logger log = LoggerFactory.getLogger(TOPSISConfigServiceImpl.class);


    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;
    
    @Autowired
    private ModelStepMapper modelStepMapper;
    
    @Autowired
    private TOPSISParameterParser topsisParameterParser;
    
    @Autowired
    private IndicatorAnalysisService indicatorAnalysisService;
    
    @Autowired
    private TOPSISConfigValidationService validationService;

    @Override
    public TOPSISAlgorithmConfig getTOPSISConfig(Long modelId, String stepCode) {
        log.debug("获取TOPSIS配置: modelId={}, stepCode={}", modelId, stepCode);
        
        // 1. 根据模型ID和步骤代码查找步骤
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("step_code", stepCode)
                .eq("status", 1);
        ModelStep modelStep = modelStepMapper.selectOne(stepQuery);
        
        if (modelStep == null) {
            log.warn("未找到模型步骤: modelId={}, stepCode={}", modelId, stepCode);
            return null;
        }
        
        return getTOPSISConfigByStepId(modelStep.getId());
    }

    @Override
    public TOPSISAlgorithmConfig getTOPSISConfigByStepId(Long stepId) {
        log.debug("根据步骤ID获取TOPSIS配置: stepId={}", stepId);
        
        // 查找TOPSIS相关的算法配置
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", stepId)
                .eq("status", 1)
                .and(wrapper -> wrapper.like("algorithm_code", "TOPSIS")
                        .or().like("ql_expression", "@TOPSIS"));
        
        List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);
        
        if (algorithms.isEmpty()) {
            log.warn("未找到TOPSIS算法配置: stepId={}", stepId);
            return null;
        }
        
        // 取第一个TOPSIS算法配置
        StepAlgorithm algorithm = algorithms.get(0);
        
        try {
            return topsisParameterParser.parseFromExpression(algorithm.getQlExpression(), algorithm);
        } catch (Exception e) {
            log.error("解析TOPSIS配置失败: stepId={}, algorithmId={}", stepId, algorithm.getId(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTOPSISConfig(Long modelId, String stepCode, List<String> indicators, String algorithmType) {
        log.info("更新TOPSIS配置: modelId={}, stepCode={}, indicators={}, algorithmType={}", 
                modelId, stepCode, indicators, algorithmType);
        
        // 1. 查找模型步骤
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("step_code", stepCode)
                .eq("status", 1);
        ModelStep modelStep = modelStepMapper.selectOne(stepQuery);
        
        if (modelStep == null) {
            log.error("未找到模型步骤: modelId={}, stepCode={}", modelId, stepCode);
            return false;
        }
        
        return updateTOPSISConfigByStepId(modelStep.getId(), indicators, algorithmType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTOPSISConfigByStepId(Long stepId, List<String> indicators, String algorithmType) {
        log.info("根据步骤ID更新TOPSIS配置: stepId={}, indicators={}, algorithmType={}", 
                stepId, indicators, algorithmType);
        
        try {
            // 1. 查找现有的TOPSIS算法配置
            QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
            algorithmQuery.eq("step_id", stepId)
                    .eq("status", 1)
                    .and(wrapper -> wrapper.like("algorithm_code", "TOPSIS")
                            .or().like("ql_expression", "@TOPSIS"));
            
            List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);
            
            // 2. 构建新的ql_expression
            String newExpression = buildTOPSISExpression(indicators, algorithmType);
            
            if (algorithms.isEmpty()) {
                // 创建新的算法配置
                StepAlgorithm newAlgorithm = new StepAlgorithm();
                newAlgorithm.setStepId(stepId);
                newAlgorithm.setAlgorithmName("TOPSIS优劣解算");
                newAlgorithm.setAlgorithmCode(algorithmType);
                newAlgorithm.setAlgorithmOrder(1);
                newAlgorithm.setQlExpression(newExpression);
                newAlgorithm.setOutputParam(algorithmType.equals("TOPSIS_POSITIVE") ? "positive_distance" : "negative_distance");
                newAlgorithm.setDescription("TOPSIS优劣解算法配置");
                newAlgorithm.setStatus(1);
                
                return stepAlgorithmMapper.insert(newAlgorithm) > 0;
            } else {
                // 更新现有配置
                StepAlgorithm algorithm = algorithms.get(0);
                algorithm.setQlExpression(newExpression);
                algorithm.setAlgorithmCode(algorithmType);
                algorithm.setOutputParam(algorithmType.equals("TOPSIS_POSITIVE") ? "positive_distance" : "negative_distance");
                
                return stepAlgorithmMapper.updateById(algorithm) > 0;
            }
        } catch (Exception e) {
            log.error("更新TOPSIS配置失败: stepId={}", stepId, e);
            return false;
        }
    }

    @Override
    public List<String> getAvailableIndicators(Long modelId) {
        log.debug("获取模型可用指标列: modelId={}", modelId);
        
        try {
            // 使用指标分析服务获取元数据
            List<IndicatorMetadata> metadataList = indicatorAnalysisService.getIndicatorMetadata(modelId);
            
            return metadataList.stream()
                    .filter(metadata -> metadata.isNumeric() && metadata.getCompleteness() > 0.5)
                    .map(IndicatorMetadata::getColumnName)
                    .sorted()
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取可用指标列失败: modelId={}", modelId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public IndicatorValidationResult validateIndicators(Long modelId, List<String> indicators) {
        log.debug("验证指标列: modelId={}, indicators={}", modelId, indicators);
        
        if (indicators == null || indicators.isEmpty()) {
            return new IndicatorValidationResult(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Arrays.asList("指标列表不能为空")
            );
        }
        
        List<String> validIndicators = new ArrayList<>();
        List<String> invalidIndicators = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // 获取指标元数据进行详细验证
            List<IndicatorMetadata> metadataList = indicatorAnalysisService.getIndicatorMetadata(modelId);
            Map<String, IndicatorMetadata> metadataMap = metadataList.stream()
                    .collect(Collectors.toMap(IndicatorMetadata::getColumnName, metadata -> metadata));
            
            for (String indicator : indicators) {
                if (StringUtils.hasText(indicator)) {
                    IndicatorMetadata metadata = metadataMap.get(indicator);
                    if (metadata != null && metadata.isNumeric()) {
                        validIndicators.add(indicator);
                        
                        // 添加数据质量警告
                        if (metadata.getCompleteness() < 0.8) {
                            warnings.add(String.format("指标 %s 数据完整性较低 (%.1f%%)", 
                                    metadata.getDisplayName(), metadata.getCompletenessPercentage()));
                        }
                        
                        if (metadata.getQualityScore() < 0.7) {
                            warnings.add(String.format("指标 %s 数据质量评分较低 (%.1f%%)", 
                                    metadata.getDisplayName(), metadata.getQualityScorePercentage()));
                        }
                    } else {
                        invalidIndicators.add(indicator);
                    }
                } else {
                    warnings.add("发现空的指标名称");
                }
            }
            
            // 添加通用警告信息
            if (validIndicators.size() < 2) {
                warnings.add("TOPSIS算法建议至少使用2个指标");
            }
            
            if (validIndicators.size() > 10) {
                warnings.add("使用过多指标可能影响计算性能");
            }
            
            // 检查指标相关性
            if (validIndicators.size() >= 2) {
                Map<String, Map<String, Double>> correlationMatrix = 
                        indicatorAnalysisService.calculateCorrelationMatrix(modelId, validIndicators);
                checkHighCorrelation(correlationMatrix, warnings);
            }
            
        } catch (Exception e) {
            log.error("验证指标时发生错误: modelId={}", modelId, e);
            warnings.add("指标验证过程中发生错误，请检查系统日志");
        }
        
        return new IndicatorValidationResult(validIndicators, invalidIndicators, warnings);
    }

    @Override
    public List<TOPSISAlgorithmConfig> getAllTOPSISConfigs(Long modelId) {
        log.debug("获取所有TOPSIS配置: modelId={}", modelId);
        
        // 1. 查找模型的所有步骤
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("status", 1);
        List<ModelStep> steps = modelStepMapper.selectList(stepQuery);
        
        List<TOPSISAlgorithmConfig> configs = new ArrayList<>();
        
        for (ModelStep step : steps) {
            TOPSISAlgorithmConfig config = getTOPSISConfigByStepId(step.getId());
            if (config != null) {
                configs.add(config);
            }
        }
        
        return configs;
    }

    @Override
    public boolean hasTOPSISConfig(Long modelId, String stepCode) {
        TOPSISAlgorithmConfig config = getTOPSISConfig(modelId, stepCode);
        return config != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTOPSISConfig(TOPSISAlgorithmConfig config) {
        log.info("创建TOPSIS配置: {}", config);
        
        if (!config.isValid()) {
            log.error("TOPSIS配置无效: {}", config);
            return false;
        }
        
        try {
            StepAlgorithm algorithm = new StepAlgorithm();
            algorithm.setStepId(config.getStepId());
            algorithm.setAlgorithmName("TOPSIS优劣解算");
            algorithm.setAlgorithmCode(config.getAlgorithmCode());
            algorithm.setAlgorithmOrder(1);
            algorithm.setQlExpression(buildTOPSISExpression(config.getIndicators(), config.getAlgorithmType()));
            algorithm.setOutputParam(config.getOutputParam());
            algorithm.setDescription("TOPSIS优劣解算法配置");
            algorithm.setStatus(1);
            
            return stepAlgorithmMapper.insert(algorithm) > 0;
        } catch (Exception e) {
            log.error("创建TOPSIS配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTOPSISConfig(Long modelId, String stepCode) {
        log.info("删除TOPSIS配置: modelId={}, stepCode={}", modelId, stepCode);
        
        // 1. 查找模型步骤
        QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("model_id", modelId)
                .eq("step_code", stepCode)
                .eq("status", 1);
        ModelStep modelStep = modelStepMapper.selectOne(stepQuery);
        
        if (modelStep == null) {
            log.warn("未找到模型步骤: modelId={}, stepCode={}", modelId, stepCode);
            return false;
        }
        
        // 2. 删除TOPSIS算法配置
        QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
        algorithmQuery.eq("step_id", modelStep.getId())
                .and(wrapper -> wrapper.like("algorithm_code", "TOPSIS")
                        .or().like("ql_expression", "@TOPSIS"));
        
        return stepAlgorithmMapper.delete(algorithmQuery) > 0;
    }

    @Override
    public ValidationResult validateTOPSISConfig(TOPSISAlgorithmConfig config) {
        log.info("验证TOPSIS配置: {}", config);
        
        try {
            // 使用专门的验证服务进行综合验证
            TOPSISConfigValidationService.ComprehensiveValidationResult result = 
                    validationService.comprehensiveValidation(config);
            
            // 收集所有错误、警告和建议
            List<String> allErrors = new ArrayList<>();
            List<String> allWarnings = new ArrayList<>();
            List<String> allSuggestions = new ArrayList<>();
            
            // 从各个验证结果中收集信息
            if (result.getConfigValidation() != null) {
                if (result.getConfigValidation().getErrors() != null) {
                    allErrors.addAll(result.getConfigValidation().getErrors());
                }
                if (result.getConfigValidation().getWarnings() != null) {
                    allWarnings.addAll(result.getConfigValidation().getWarnings());
                }
                if (result.getConfigValidation().getSuggestions() != null) {
                    allSuggestions.addAll(result.getConfigValidation().getSuggestions());
                }
            }
            
            // 添加数据相关的问题
            if (result.getExistenceValidation() != null && !result.getExistenceValidation().isAllExist()) {
                allErrors.add(String.format("有%d个指标在数据中不存在: %s", 
                        result.getExistenceValidation().getMissingIndicators().size(),
                        String.join(", ", result.getExistenceValidation().getMissingIndicators())));
            }
            
            if (result.getQualityValidation() != null && !result.getQualityValidation().isQualityAcceptable()) {
                allWarnings.add(String.format("数据质量评分较低: %.1f%%", 
                        result.getQualityValidation().getAverageQualityScore() * 100));
            }
            
            if (result.getCompletenessValidation() != null && !result.getCompletenessValidation().isMeetsThreshold()) {
                allWarnings.add(String.format("数据完整性不足: %.1f%%", 
                        result.getCompletenessValidation().getAverageCompleteness() * 100));
            }
            
            // 添加执行相关的问题
            if (result.getExecutabilityValidation() != null) {
                if (result.getExecutabilityValidation().getExecutionBlockers() != null) {
                    allErrors.addAll(result.getExecutabilityValidation().getExecutionBlockers());
                }
                if (result.getExecutabilityValidation().getExecutionWarnings() != null) {
                    allWarnings.addAll(result.getExecutabilityValidation().getExecutionWarnings());
                }
            }
            
            // 添加综合建议
            if (result.getRecommendations() != null) {
                allSuggestions.addAll(result.getRecommendations());
            }
            
            return new ValidationResult(result.isOverallValid(), allErrors, allWarnings, 
                    allSuggestions, result.getConfidenceScore());
                    
        } catch (Exception e) {
            log.error("验证TOPSIS配置时发生错误", e);
            return new ValidationResult(false, 
                    Arrays.asList("验证过程中发生系统错误: " + e.getMessage()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    0.0);
        }
    }

    /**
     * 构建TOPSIS表达式
     */
    private String buildTOPSISExpression(List<String> indicators, String algorithmType) {
        if (indicators == null || indicators.isEmpty()) {
            throw new IllegalArgumentException("指标列表不能为空");
        }
        
        String indicatorList = String.join(",", indicators);
        return String.format("@%s:%s", algorithmType, indicatorList);
    }

    /**
     * 检查指标间的高相关性
     */
    private void checkHighCorrelation(Map<String, Map<String, Double>> correlationMatrix, List<String> warnings) {
        double highCorrelationThreshold = 0.8;
        
        for (Map.Entry<String, Map<String, Double>> entry1 : correlationMatrix.entrySet()) {
            String indicator1 = entry1.getKey();
            for (Map.Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
                String indicator2 = entry2.getKey();
                Double correlation = entry2.getValue();
                
                if (!indicator1.equals(indicator2) && 
                    correlation != null && 
                    Math.abs(correlation) > highCorrelationThreshold) {
                    warnings.add(String.format("指标 %s 和 %s 相关性较高 (%.2f)，可能存在冗余", 
                            indicator1, indicator2, correlation));
                }
            }
        }
    }
}
