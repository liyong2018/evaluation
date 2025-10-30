package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.entity.ModelStep;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.mapper.ModelStepMapper;
import com.evaluate.mapper.StepAlgorithmMapper;
import com.evaluate.service.TOPSISParameterParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TOPSIS参数解析器实现类
 * 
 * 支持解析格式：
 * - @TOPSIS_POSITIVE:indicator1,indicator2,indicator3
 * - @TOPSIS_NEGATIVE:indicator1,indicator2,indicator3
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISParameterParserImpl implements TOPSISParameterParser {

    private static final Logger log = LoggerFactory.getLogger(TOPSISParameterParserImpl.class);

    
    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;
    
    @Autowired
    private ModelStepMapper modelStepMapper;
    
    /**
     * TOPSIS表达式正则模式
     * 匹配格式：@TOPSIS_POSITIVE:indicator1,indicator2,indicator3
     */
    private static final Pattern TOPSIS_PATTERN = Pattern.compile(
        "@(TOPSIS_POSITIVE|TOPSIS_NEGATIVE):([a-zA-Z0-9_,\\s]+)"
    );
    
    @Override
    public TOPSISAlgorithmConfig parseFromExpression(String qlExpression, StepAlgorithm stepAlgorithm) {
        log.debug("解析TOPSIS表达式: {}", qlExpression);
        
        if (qlExpression == null || qlExpression.trim().isEmpty()) {
            log.warn("ql_expression为空");
            return null;
        }
        
        if (stepAlgorithm == null) {
            log.warn("StepAlgorithm实体为空");
            return null;
        }
        
        try {
            Matcher matcher = TOPSIS_PATTERN.matcher(qlExpression.trim());
            
            if (!matcher.find()) {
                log.warn("ql_expression格式不匹配TOPSIS模式: {}", qlExpression);
                return null;
            }
            
            String algorithmType = matcher.group(1); // TOPSIS_POSITIVE 或 TOPSIS_NEGATIVE
            String indicatorsStr = matcher.group(2); // indicator1,indicator2,indicator3
            
            // 解析指标列表
            List<String> indicators = Arrays.stream(indicatorsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            if (indicators.isEmpty()) {
                log.warn("未找到有效的指标列名: {}", indicatorsStr);
                return null;
            }
            
            // 构建配置对象
            TOPSISAlgorithmConfig config = TOPSISAlgorithmConfig.builder()
                    .stepId(stepAlgorithm.getStepId())
                    .algorithmCode(stepAlgorithm.getAlgorithmCode())
                    .indicators(indicators)
                    .outputParam(stepAlgorithm.getOutputParam())
                    .isPositiveDistance("TOPSIS_POSITIVE".equals(algorithmType))
                    .originalExpression(qlExpression)
                    .algorithmType(algorithmType)
                    .enableSingleRegionHandling(true) // 默认启用单区域处理
                    .theoreticalBaselineRatio(0.2) // 默认20%作为理论基准
                    .build();
            
            log.info("成功解析TOPSIS配置 - 算法类型: {}, 指标数量: {}, 指标列表: {}", 
                    algorithmType, indicators.size(), indicators);
            
            return config;
            
        } catch (Exception e) {
            log.error("解析TOPSIS表达式时发生异常: {}", qlExpression, e);
            return null;
        }
    }
    
    @Override
    public boolean validateExpressionFormat(String qlExpression) {
        if (qlExpression == null || qlExpression.trim().isEmpty()) {
            return false;
        }
        
        try {
            Matcher matcher = TOPSIS_PATTERN.matcher(qlExpression.trim());
            
            if (!matcher.find()) {
                return false;
            }
            
            String indicatorsStr = matcher.group(2);
            
            // 验证指标列表不为空
            List<String> indicators = Arrays.stream(indicatorsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            return !indicators.isEmpty();
            
        } catch (Exception e) {
            log.error("验证TOPSIS表达式格式时发生异常: {}", qlExpression, e);
            return false;
        }
    }
    
    @Override
    public TOPSISAlgorithmConfig getTOPSISConfig(Long modelId, String stepCode) {
        log.debug("获取TOPSIS配置 - 模型ID: {}, 步骤代码: {}", modelId, stepCode);
        
        if (modelId == null || stepCode == null || stepCode.trim().isEmpty()) {
            log.warn("模型ID或步骤代码为空");
            return null;
        }
        
        try {
            // 1. 根据模型ID和步骤代码查找步骤
            QueryWrapper<ModelStep> stepQuery = new QueryWrapper<>();
            stepQuery.eq("model_id", modelId)
                    .eq("step_code", stepCode);
            
            ModelStep modelStep = modelStepMapper.selectOne(stepQuery);
            
            if (modelStep == null) {
                log.warn("未找到模型步骤 - 模型ID: {}, 步骤代码: {}", modelId, stepCode);
                return null;
            }
            
            // 2. 查找TOPSIS算法配置
            QueryWrapper<StepAlgorithm> algorithmQuery = new QueryWrapper<>();
            algorithmQuery.eq("step_id", modelStep.getId())
                    .and(wrapper -> wrapper
                            .like("algorithm_code", "TOPSIS")
                            .or()
                            .like("ql_expression", "@TOPSIS_")
                    )
                    .eq("status", 1) // 只查询启用的算法
                    .orderByAsc("algorithm_order");
            
            List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(algorithmQuery);
            
            if (algorithms.isEmpty()) {
                log.warn("未找到TOPSIS算法配置 - 步骤ID: {}", modelStep.getId());
                return null;
            }
            
            // 3. 解析第一个匹配的算法配置
            for (StepAlgorithm algorithm : algorithms) {
                TOPSISAlgorithmConfig config = parseFromExpression(algorithm.getQlExpression(), algorithm);
                
                if (config != null && config.isValid()) {
                    // 补充模型和步骤信息
                    config.setModelId(modelId);
                    config.setStepCode(stepCode);
                    
                    log.info("成功获取TOPSIS配置 - 模型ID: {}, 步骤代码: {}, 算法类型: {}", 
                            modelId, stepCode, config.getAlgorithmType());
                    
                    return config;
                }
            }
            
            log.warn("未找到有效的TOPSIS配置 - 模型ID: {}, 步骤代码: {}", modelId, stepCode);
            return null;
            
        } catch (Exception e) {
            log.error("获取TOPSIS配置时发生异常 - 模型ID: {}, 步骤代码: {}", modelId, stepCode, e);
            return null;
        }
    }
    
    @Override
    public String generateExpression(TOPSISAlgorithmConfig algorithmConfig) {
        if (algorithmConfig == null || !algorithmConfig.isValid()) {
            log.warn("TOPSIS算法配置无效");
            return null;
        }
        
        try {
            String algorithmType = algorithmConfig.isPositiveDistance() ? "TOPSIS_POSITIVE" : "TOPSIS_NEGATIVE";
            String indicators = String.join(",", algorithmConfig.getIndicators());
            
            String expression = "@" + algorithmType + ":" + indicators;
            
            log.debug("生成TOPSIS表达式: {}", expression);
            
            return expression;
            
        } catch (Exception e) {
            log.error("生成TOPSIS表达式时发生异常", e);
            return null;
        }
    }
}
