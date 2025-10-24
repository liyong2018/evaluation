package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.service.TOPSISCompatibilityService;
import com.evaluate.service.UnifiedTOPSISCalculator;
import com.evaluate.service.TOPSISConfigService;
import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * TOPSIS兼容性服务实现类
 * 
 * 提供新旧TOPSIS计算器的切换机制和向后兼容性支持：
 * 1. 保持现有TOPSISCalculator接口的兼容性
 * 2. 提供新旧计算器的切换机制
 * 3. 确保现有功能不受影响
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISCompatibilityServiceImpl implements TOPSISCompatibilityService {

    private static final Logger log = LoggerFactory.getLogger(TOPSISCompatibilityServiceImpl.class);

    
    @Autowired
    private UnifiedTOPSISCalculator unifiedCalculator;
    
    @Autowired
    private TOPSISConfigService configService;
    
    @Value("${topsis.calculator.strategy:AUTO}")
    private String defaultStrategy;
    
    private CalculatorStrategy currentStrategy = CalculatorStrategy.AUTO;
    
    @Override
    public Map<String, Map<String, Double>> calculateDistances(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode) {
        
        log.debug("计算TOPSIS距离 - 模型: {}, 步骤: {}, 策略: {}", modelId, stepCode, currentStrategy);
        
        try {
            switch (currentStrategy) {
                case LEGACY:
                    log.info("使用原始TOPSIS计算器（强制模式）");
                    return calculateDistancesLegacy(weightedData);
                    
                case UNIFIED:
                    log.info("使用统一TOPSIS计算器（强制模式）");
                    return calculateDistancesUnified(weightedData, modelId, stepCode);
                    
                case AUTO:
                default:
                    // 自动选择计算器
                    if (shouldUseUnifiedCalculator(modelId, stepCode)) {
                        log.info("自动选择：使用统一TOPSIS计算器");
                        return calculateDistancesUnified(weightedData, modelId, stepCode);
                    } else {
                        log.info("自动选择：使用原始TOPSIS计算器");
                        return calculateDistancesLegacy(weightedData);
                    }
            }
            
        } catch (Exception e) {
            log.error("TOPSIS计算失败，尝试降级到原始计算器", e);
            
            // 发生异常时降级到原始计算器
            try {
                return calculateDistancesLegacy(weightedData);
            } catch (Exception fallbackException) {
                log.error("原始计算器也失败了", fallbackException);
                throw fallbackException;
            }
        }
    }
    
    @Override
    public Map<String, Map<String, Double>> calculateDistancesLegacy(
            Map<String, Map<String, Double>> weightedData) {
        
        log.debug("使用原始TOPSIS计算器");
        
        try {
            return unifiedCalculator.calculateDistances(weightedData, null);
        } catch (Exception e) {
            log.error("原始TOPSIS计算器执行失败", e);
            throw e;
        }
    }
    
    @Override
    public Map<String, Map<String, Double>> calculateDistancesUnified(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode) {
        
        log.debug("使用统一TOPSIS计算器 - 模型: {}, 步骤: {}", modelId, stepCode);
        
        try {
            // 获取算法配置
            TOPSISAlgorithmConfig algorithmConfig = configService.getTOPSISConfig(modelId, stepCode);
            
            if (algorithmConfig == null || !algorithmConfig.isValid()) {
                log.warn("统一TOPSIS配置无效，降级到原始计算器");
                return calculateDistancesLegacy(weightedData);
            }
            
            return unifiedCalculator.calculateDistances(weightedData, algorithmConfig);
            
        } catch (Exception e) {
            log.error("统一TOPSIS计算器执行失败，降级到原始计算器", e);
            return unifiedCalculator.calculateDistances(weightedData, null);
        }
    }
    
    @Override
    public boolean shouldUseUnifiedCalculator(Long modelId, String stepCode) {
        try {
            // 检查是否有有效的统一TOPSIS配置
            TOPSISAlgorithmConfig algorithmConfig = configService.getTOPSISConfig(modelId, stepCode);
            
            if (algorithmConfig == null || !algorithmConfig.isValid()) {
                log.debug("模型 {} 步骤 {} 没有有效的统一TOPSIS配置", modelId, stepCode);
                return false;
            }
            
            // 检查指标是否有效
            if (algorithmConfig.getIndicators() == null || algorithmConfig.getIndicators().isEmpty()) {
                log.debug("模型 {} 步骤 {} 的TOPSIS配置指标列表为空", modelId, stepCode);
                return false;
            }
            
            log.debug("模型 {} 步骤 {} 可以使用统一TOPSIS计算器", modelId, stepCode);
            return true;
            
        } catch (Exception e) {
            log.error("检查是否应该使用统一计算器时发生异常 - 模型: {}, 步骤: {}", modelId, stepCode, e);
            return false;
        }
    }
    
    @Override
    public void setCalculatorStrategy(CalculatorStrategy strategy) {
        log.info("设置TOPSIS计算器策略: {} -> {}", this.currentStrategy, strategy);
        this.currentStrategy = strategy != null ? strategy : CalculatorStrategy.AUTO;
    }
    
    @Override
    public CalculatorStrategy getCalculatorStrategy() {
        return this.currentStrategy;
    }
    
    @Override
    public Map<String, Object> validateCalculatorConsistency(
            Map<String, Map<String, Double>> weightedData, 
            Long modelId, 
            String stepCode) {
        
        log.info("验证计算器一致性 - 模型: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> validation = new HashMap<>();
        
        try {
            // 使用原始计算器计算
            Map<String, Map<String, Double>> legacyResults = calculateDistancesLegacy(weightedData);
            
            // 使用统一计算器计算
            Map<String, Map<String, Double>> unifiedResults = calculateDistancesUnified(weightedData, modelId, stepCode);
            
            // 比较结果
            Map<String, Object> comparison = compareResults(legacyResults, unifiedResults);
            
            validation.put("legacyResults", legacyResults);
            validation.put("unifiedResults", unifiedResults);
            validation.put("comparison", comparison);
            validation.put("consistent", (Boolean) comparison.get("consistent"));
            validation.put("validationTime", new Date());
            
            if ((Boolean) comparison.get("consistent")) {
                log.info("计算器一致性验证通过");
            } else {
                log.warn("计算器一致性验证失败: {}", comparison.get("differences"));
            }
            
        } catch (Exception e) {
            log.error("验证计算器一致性时发生异常", e);
            validation.put("error", "验证失败: " + e.getMessage());
            validation.put("consistent", false);
        }
        
        return validation;
    }
    
    /**
     * 比较两个计算结果
     */
    private Map<String, Object> compareResults(
            Map<String, Map<String, Double>> legacyResults,
            Map<String, Map<String, Double>> unifiedResults) {
        
        Map<String, Object> comparison = new HashMap<>();
        List<String> differences = new ArrayList<>();
        boolean consistent = true;
        
        // 检查地区数量
        if (legacyResults.size() != unifiedResults.size()) {
            differences.add("地区数量不一致: 原始=" + legacyResults.size() + ", 统一=" + unifiedResults.size());
            consistent = false;
        }
        
        // 比较每个地区的结果
        for (String regionCode : legacyResults.keySet()) {
            if (!unifiedResults.containsKey(regionCode)) {
                differences.add("地区 " + regionCode + " 在统一计算器结果中缺失");
                consistent = false;
                continue;
            }
            
            Map<String, Double> legacyRegionResult = legacyResults.get(regionCode);
            Map<String, Double> unifiedRegionResult = unifiedResults.get(regionCode);
            
            // 比较距离值
            Double legacyPositive = legacyRegionResult.get("comprehensive_positive");
            Double legacyNegative = legacyRegionResult.get("comprehensive_negative");
            
            // 统一计算器可能使用不同的键名，需要灵活匹配
            Double unifiedPositive = findPositiveDistance(unifiedRegionResult);
            Double unifiedNegative = findNegativeDistance(unifiedRegionResult);
            
            if (legacyPositive != null && unifiedPositive != null) {
                double diff = Math.abs(legacyPositive - unifiedPositive);
                if (diff > 0.001) { // 允许小的数值误差
                    differences.add(String.format("地区 %s 正理想解距离差异: %.6f vs %.6f (差值: %.6f)", 
                            regionCode, legacyPositive, unifiedPositive, diff));
                    consistent = false;
                }
            }
            
            if (legacyNegative != null && unifiedNegative != null) {
                double diff = Math.abs(legacyNegative - unifiedNegative);
                if (diff > 0.001) { // 允许小的数值误差
                    differences.add(String.format("地区 %s 负理想解距离差异: %.6f vs %.6f (差值: %.6f)", 
                            regionCode, legacyNegative, unifiedNegative, diff));
                    consistent = false;
                }
            }
        }
        
        // 检查统一计算器是否有额外的地区
        for (String regionCode : unifiedResults.keySet()) {
            if (!legacyResults.containsKey(regionCode)) {
                differences.add("地区 " + regionCode + " 在原始计算器结果中缺失");
                consistent = false;
            }
        }
        
        comparison.put("consistent", consistent);
        comparison.put("differences", differences);
        comparison.put("totalDifferences", differences.size());
        
        return comparison;
    }
    
    /**
     * 在结果中查找正理想解距离
     */
    private Double findPositiveDistance(Map<String, Double> result) {
        // 尝试不同的可能键名
        String[] possibleKeys = {"comprehensive_positive", "positive", "positiveDistance"};
        
        for (String key : possibleKeys) {
            if (result.containsKey(key)) {
                return result.get(key);
            }
        }
        
        // 查找包含"positive"的键
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            if (entry.getKey().toLowerCase().contains("positive")) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 在结果中查找负理想解距离
     */
    private Double findNegativeDistance(Map<String, Double> result) {
        // 尝试不同的可能键名
        String[] possibleKeys = {"comprehensive_negative", "negative", "negativeDistance"};
        
        for (String key : possibleKeys) {
            if (result.containsKey(key)) {
                return result.get(key);
            }
        }
        
        // 查找包含"negative"的键
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            if (entry.getKey().toLowerCase().contains("negative")) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> checkCompatibility(Long modelId) {
        log.debug("检查模型 {} 的兼容性", modelId);
        
        Map<String, Object> compatibility = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            // 检查是否有TOPSIS配置
            TOPSISAlgorithmConfig config = configService.getTOPSISConfig(modelId, "step3");
            
            if (config == null) {
                issues.add("缺少TOPSIS算法配置");
            } else {
                if (!config.isValid()) {
                    issues.add("TOPSIS配置无效");
                }
                if (config.getIndicators() == null || config.getIndicators().isEmpty()) {
                    issues.add("指标列表为空");
                }
            }
            
            compatibility.put("compatible", issues.isEmpty());
            compatibility.put("issues", issues);
            compatibility.put("modelId", modelId);
            compatibility.put("checkTime", new Date());
            
        } catch (Exception e) {
            log.error("检查兼容性时发生异常", e);
            issues.add("检查过程中发生异常: " + e.getMessage());
            compatibility.put("compatible", false);
            compatibility.put("issues", issues);
        }
        
        return compatibility;
    }
    
    @Override
    public List<String> getCompatibilityIssues(Long modelId) {
        log.debug("获取模型 {} 的兼容性问题", modelId);
        
        Map<String, Object> compatibility = checkCompatibility(modelId);
        return (List<String>) compatibility.get("issues");
    }
}
