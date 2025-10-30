package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.mapper.StepAlgorithmMapper;
import com.evaluate.service.TOPSISConfigurationAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TOPSIS配置分析器实现类
 * 
 * 实现TOPSIS配置的分析和对比功能
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISConfigurationAnalyzerImpl implements TOPSISConfigurationAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TOPSISConfigurationAnalyzerImpl.class);

    
    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;
    
    // TOPSIS相关的算法代码
    private static final Set<String> TOPSIS_ALGORITHM_CODES = new HashSet<>(Arrays.asList(
            "COMPREHENSIVE_POSITIVE", "COMPREHENSIVE_NEGATIVE", 
            "TOPSIS_POSITIVE", "TOPSIS_NEGATIVE",
            "IDEAL_SOLUTION", "DISTANCE_CALCULATION"
    ));
    
    @Override
    public Map<String, Object> compareModelConfigurations(Long modelId1, Long modelId2, String stepCode) {
        log.info("对比模型TOPSIS配置 - 模型1: {}, 模型2: {}, 步骤: {}", modelId1, modelId2, stepCode);
        
        Map<String, Object> comparison = new HashMap<>();
        
        try {
            // 获取两个模型的配置
            Map<String, Object> config1 = analyzeModelConfiguration(modelId1, stepCode);
            Map<String, Object> config2 = analyzeModelConfiguration(modelId2, stepCode);
            
            // 基本信息对比
            comparison.put("model1Id", modelId1);
            comparison.put("model2Id", modelId2);
            comparison.put("stepCode", stepCode);
            comparison.put("comparisonTime", System.currentTimeMillis());
            
            // 配置存在性对比
            boolean config1Exists = (Boolean) config1.getOrDefault("exists", false);
            boolean config2Exists = (Boolean) config2.getOrDefault("exists", false);
            
            comparison.put("model1ConfigExists", config1Exists);
            comparison.put("model2ConfigExists", config2Exists);
            
            if (!config1Exists && !config2Exists) {
                comparison.put("result", "both_missing");
                comparison.put("message", "两个模型都没有TOPSIS配置");
                return comparison;
            }
            
            if (!config1Exists) {
                comparison.put("result", "model1_missing");
                comparison.put("message", "模型1缺少TOPSIS配置");
                return comparison;
            }
            
            if (!config2Exists) {
                comparison.put("result", "model2_missing");
                comparison.put("message", "模型2缺少TOPSIS配置");
                return comparison;
            }
            
            // 详细配置对比
            List<String> differences = new ArrayList<>();
            List<String> similarities = new ArrayList<>();
            
            // 对比算法数量
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> algorithms1 = (List<Map<String, Object>>) config1.get("algorithms");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> algorithms2 = (List<Map<String, Object>>) config2.get("algorithms");
            
            if (algorithms1.size() != algorithms2.size()) {
                differences.add("算法数量不同: 模型1有" + algorithms1.size() + "个，模型2有" + algorithms2.size() + "个");
            } else {
                similarities.add("算法数量相同: " + algorithms1.size() + "个");
            }
            
            // 对比具体算法配置
            compareAlgorithmConfigurations(algorithms1, algorithms2, differences, similarities);
            
            // 对比指标配置
            compareIndicatorConfigurations(config1, config2, differences, similarities);
            
            // 生成对比结果
            comparison.put("differences", differences);
            comparison.put("similarities", similarities);
            comparison.put("config1", config1);
            comparison.put("config2", config2);
            
            // 生成建议
            List<String> recommendations = generateComparisonRecommendations(differences, similarities);
            comparison.put("recommendations", recommendations);
            
            // 计算相似度
            double similarity = calculateConfigurationSimilarity(differences, similarities);
            comparison.put("similarityScore", similarity);
            
            comparison.put("result", "success");
            comparison.put("message", "配置对比完成");
            
            log.info("模型配置对比完成 - 差异: {}, 相似: {}, 相似度: {:.2f}", 
                    differences.size(), similarities.size(), similarity);
            
        } catch (Exception e) {
            log.error("对比模型配置时发生异常", e);
            comparison.put("result", "error");
            comparison.put("message", "配置对比失败: " + e.getMessage());
        }
        
        return comparison;
    }
    
    @Override
    public Map<String, Object> analyzeModelConfiguration(Long modelId, String stepCode) {
        log.debug("分析模型TOPSIS配置 - 模型ID: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // 获取模型的TOPSIS相关算法
            List<StepAlgorithm> algorithms = getTOPSISAlgorithmsForModel(modelId, stepCode);
            
            analysis.put("modelId", modelId);
            analysis.put("stepCode", stepCode);
            analysis.put("exists", !algorithms.isEmpty());
            analysis.put("algorithmCount", algorithms.size());
            
            if (algorithms.isEmpty()) {
                analysis.put("message", "未找到TOPSIS相关配置");
                return analysis;
            }
            
            // 分析算法配置
            List<Map<String, Object>> algorithmAnalysis = new ArrayList<>();
            Set<String> allIndicators = new HashSet<>();
            
            for (StepAlgorithm algorithm : algorithms) {
                Map<String, Object> algInfo = analyzeAlgorithmConfiguration(algorithm);
                algorithmAnalysis.add(algInfo);
                
                // 提取指标信息
                @SuppressWarnings("unchecked")
                List<String> indicators = (List<String>) algInfo.get("indicators");
                if (indicators != null) {
                    allIndicators.addAll(indicators);
                }
            }
            
            analysis.put("algorithms", algorithmAnalysis);
            analysis.put("totalIndicators", allIndicators.size());
            analysis.put("indicators", new ArrayList<>(allIndicators));
            
            // 配置质量分析
            Map<String, Object> qualityAnalysis = analyzeConfigurationQuality(algorithms);
            analysis.put("qualityAnalysis", qualityAnalysis);
            
            // 生成配置摘要
            Map<String, Object> summary = generateConfigurationSummary(algorithms, allIndicators);
            analysis.put("summary", summary);
            
            log.debug("模型配置分析完成 - 算法数: {}, 指标数: {}", algorithms.size(), allIndicators.size());
            
        } catch (Exception e) {
            log.error("分析模型配置时发生异常", e);
            analysis.put("error", e.getMessage());
        }
        
        return analysis;
    }
    
    @Override
    public List<Map<String, Object>> getTOPSISConfigurations(Long modelId) {
        log.debug("获取模型的所有TOPSIS配置 - 模型ID: {}", modelId);
        
        List<Map<String, Object>> configurations = new ArrayList<>();
        
        try {
            // 查询所有TOPSIS相关算法
            QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("algorithm_code", TOPSIS_ALGORITHM_CODES)
                       .eq("status", 1)
                       .orderBy(true, true, "algorithm_order");
            
            List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(queryWrapper);
            
            // 按步骤分组
            Map<Long, List<StepAlgorithm>> stepGroups = algorithms.stream()
                    .collect(Collectors.groupingBy(StepAlgorithm::getStepId));
            
            for (Map.Entry<Long, List<StepAlgorithm>> entry : stepGroups.entrySet()) {
                Long stepId = entry.getKey();
                List<StepAlgorithm> stepAlgorithms = entry.getValue();
                
                Map<String, Object> stepConfig = new HashMap<>();
                stepConfig.put("stepId", stepId);
                stepConfig.put("algorithmCount", stepAlgorithms.size());
                
                List<Map<String, Object>> algorithmConfigs = new ArrayList<>();
                for (StepAlgorithm algorithm : stepAlgorithms) {
                    Map<String, Object> algConfig = new HashMap<>();
                    algConfig.put("id", algorithm.getId());
                    algConfig.put("algorithmCode", algorithm.getAlgorithmCode());
                    algConfig.put("algorithmName", algorithm.getAlgorithmName());
                    algConfig.put("qlExpression", algorithm.getQlExpression());
                    algConfig.put("outputParam", algorithm.getOutputParam());
                    algConfig.put("description", algorithm.getDescription());
                    
                    algorithmConfigs.add(algConfig);
                }
                
                stepConfig.put("algorithms", algorithmConfigs);
                configurations.add(stepConfig);
            }
            
            log.debug("获取TOPSIS配置完成 - 步骤数: {}", configurations.size());
            
        } catch (Exception e) {
            log.error("获取TOPSIS配置时发生异常", e);
        }
        
        return configurations;
    }
    
    @Override
    public Map<String, Object> validateConfiguration(Long modelId, String stepCode) {
        log.debug("验证TOPSIS配置 - 模型ID: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            List<StepAlgorithm> algorithms = getTOPSISAlgorithmsForModel(modelId, stepCode);
            
            if (algorithms.isEmpty()) {
                errors.add("未找到TOPSIS相关算法配置");
                validation.put("valid", false);
                validation.put("errors", errors);
                return validation;
            }
            
            // 验证算法完整性
            validateAlgorithmCompleteness(algorithms, errors, warnings);
            
            // 验证表达式格式
            validateExpressionFormat(algorithms, errors, warnings);
            
            // 验证输出参数
            validateOutputParameters(algorithms, errors, warnings);
            
            // 验证算法顺序
            validateAlgorithmOrder(algorithms, errors, warnings);
            
            boolean isValid = errors.isEmpty();
            
            validation.put("valid", isValid);
            validation.put("errors", errors);
            validation.put("warnings", warnings);
            validation.put("algorithmCount", algorithms.size());
            validation.put("message", isValid ? "配置验证通过" : "配置验证失败");
            
            log.debug("配置验证完成 - 有效: {}, 错误: {}, 警告: {}", isValid, errors.size(), warnings.size());
            
        } catch (Exception e) {
            log.error("验证配置时发生异常", e);
            errors.add("验证过程中发生异常: " + e.getMessage());
            validation.put("valid", false);
            validation.put("errors", errors);
        }
        
        return validation;
    }
    
    @Override
    public Map<String, Object> generateDetailedComparisonReport(List<Long> modelIds, String stepCode) {
        log.info("生成详细配置对比报告 - 模型数量: {}, 步骤: {}", modelIds.size(), stepCode);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            report.put("modelIds", modelIds);
            report.put("stepCode", stepCode);
            report.put("reportTime", System.currentTimeMillis());
            
            // 获取所有模型的配置
            Map<Long, Map<String, Object>> modelConfigs = new HashMap<>();
            for (Long modelId : modelIds) {
                Map<String, Object> config = analyzeModelConfiguration(modelId, stepCode);
                modelConfigs.put(modelId, config);
            }
            
            report.put("modelConfigurations", modelConfigs);
            
            // 生成对比矩阵
            Map<String, Object> comparisonMatrix = generateComparisonMatrix(modelConfigs);
            report.put("comparisonMatrix", comparisonMatrix);
            
            // 分析配置一致性
            Map<String, Object> consistencyAnalysis = analyzeConfigurationConsistency(modelConfigs);
            report.put("consistencyAnalysis", consistencyAnalysis);
            
            // 生成统一建议
            List<String> unifiedRecommendations = generateUnifiedRecommendations(modelConfigs);
            report.put("recommendations", unifiedRecommendations);
            
            log.info("详细对比报告生成完成");
            
        } catch (Exception e) {
            log.error("生成详细对比报告时发生异常", e);
            report.put("error", e.getMessage());
        }
        
        return report;
    }
    
    @Override
    public Map<String, Object> analyzeConfigurationHistory(Long modelId, String stepCode) {
        log.debug("分析配置历史变更 - 模型ID: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> history = new HashMap<>();
        
        // TODO: 实现配置历史分析
        // 这需要额外的历史记录表或审计日志功能
        
        history.put("modelId", modelId);
        history.put("stepCode", stepCode);
        history.put("message", "配置历史分析功能待实现");
        history.put("implemented", false);
        
        return history;
    }
    
    @Override
    public List<String> recommendConfigurationOptimizations(Long modelId, String stepCode) {
        log.debug("推荐配置优化建议 - 模型ID: {}, 步骤: {}", modelId, stepCode);
        
        List<String> recommendations = new ArrayList<>();
        
        try {
            Map<String, Object> analysis = analyzeModelConfiguration(modelId, stepCode);
            
            if (!(Boolean) analysis.getOrDefault("exists", false)) {
                recommendations.add("建议创建TOPSIS算法配置");
                return recommendations;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> qualityAnalysis = (Map<String, Object>) analysis.get("qualityAnalysis");
            
            if (qualityAnalysis != null) {
                Double completeness = (Double) qualityAnalysis.get("completeness");
                if (completeness != null && completeness < 0.8) {
                    recommendations.add("配置完整性较低，建议补充缺失的算法配置");
                }
                
                @SuppressWarnings("unchecked")
                List<String> issues = (List<String>) qualityAnalysis.get("issues");
                if (issues != null && !issues.isEmpty()) {
                    recommendations.add("发现配置问题，建议修复: " + String.join(", ", issues));
                }
            }
            
            // 基于最佳实践的建议
            recommendations.addAll(generateBestPracticeRecommendations(analysis));
            
        } catch (Exception e) {
            log.error("生成优化建议时发生异常", e);
            recommendations.add("生成建议时发生异常，请检查配置");
        }
        
        return recommendations;
    }
    
    /**
     * 获取模型的TOPSIS相关算法
     */
    private List<StepAlgorithm> getTOPSISAlgorithmsForModel(Long modelId, String stepCode) {
        // TODO: 需要关联查询model_step表来获取step_id
        // 这里先返回所有TOPSIS相关算法作为示例
        
        QueryWrapper<StepAlgorithm> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("algorithm_code", TOPSIS_ALGORITHM_CODES)
                   .eq("status", 1)
                   .orderBy(true, true, "algorithm_order");
        
        return stepAlgorithmMapper.selectList(queryWrapper);
    }
    
    /**
     * 分析单个算法配置
     */
    private Map<String, Object> analyzeAlgorithmConfiguration(StepAlgorithm algorithm) {
        Map<String, Object> analysis = new HashMap<>();
        
        analysis.put("id", algorithm.getId());
        analysis.put("algorithmCode", algorithm.getAlgorithmCode());
        analysis.put("algorithmName", algorithm.getAlgorithmName());
        analysis.put("qlExpression", algorithm.getQlExpression());
        analysis.put("outputParam", algorithm.getOutputParam());
        analysis.put("description", algorithm.getDescription());
        
        // 解析QL表达式中的指标
        List<String> indicators = parseIndicatorsFromExpression(algorithm.getQlExpression());
        analysis.put("indicators", indicators);
        analysis.put("indicatorCount", indicators.size());
        
        // 分析表达式复杂度
        int complexity = calculateExpressionComplexity(algorithm.getQlExpression());
        analysis.put("complexity", complexity);
        
        return analysis;
    }
    
    /**
     * 从QL表达式中解析指标
     */
    private List<String> parseIndicatorsFromExpression(String qlExpression) {
        List<String> indicators = new ArrayList<>();
        
        if (qlExpression == null || qlExpression.trim().isEmpty()) {
            return indicators;
        }
        
        // 简单的指标解析逻辑
        // 查找@TOPSIS_POSITIVE:indicator1,indicator2格式
        if (qlExpression.contains("@TOPSIS_POSITIVE:") || qlExpression.contains("@TOPSIS_NEGATIVE:")) {
            String[] parts = qlExpression.split(":");
            if (parts.length > 1) {
                String indicatorPart = parts[1];
                String[] indicatorArray = indicatorPart.split(",");
                for (String indicator : indicatorArray) {
                    indicators.add(indicator.trim());
                }
            }
        }
        
        return indicators;
    }
    
    /**
     * 计算表达式复杂度
     */
    private int calculateExpressionComplexity(String qlExpression) {
        if (qlExpression == null || qlExpression.trim().isEmpty()) {
            return 0;
        }
        
        int complexity = 0;
        
        // 基于操作符数量计算复杂度
        String[] operators = {"+", "-", "*", "/", "==", "!=", ">", "<", ">=", "<=", "&&", "||"};
        for (String operator : operators) {
            complexity += countOccurrences(qlExpression, operator);
        }
        
        // 基于函数调用数量
        complexity += countOccurrences(qlExpression, "(");
        
        return complexity;
    }
    
    /**
     * 计算字符串中子串出现次数
     */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
    
    /**
     * 对比算法配置
     */
    private void compareAlgorithmConfigurations(List<Map<String, Object>> algorithms1, 
                                              List<Map<String, Object>> algorithms2,
                                              List<String> differences, 
                                              List<String> similarities) {
        
        // 创建算法代码映射
        Map<String, Map<String, Object>> alg1Map = algorithms1.stream()
                .collect(Collectors.toMap(
                        alg -> (String) alg.get("algorithmCode"),
                        alg -> alg,
                        (existing, replacement) -> existing
                ));
        
        Map<String, Map<String, Object>> alg2Map = algorithms2.stream()
                .collect(Collectors.toMap(
                        alg -> (String) alg.get("algorithmCode"),
                        alg -> alg,
                        (existing, replacement) -> existing
                ));
        
        // 找出共同的算法
        Set<String> commonAlgorithms = new HashSet<>(alg1Map.keySet());
        commonAlgorithms.retainAll(alg2Map.keySet());
        
        // 找出独有的算法
        Set<String> onlyInModel1 = new HashSet<>(alg1Map.keySet());
        onlyInModel1.removeAll(alg2Map.keySet());
        
        Set<String> onlyInModel2 = new HashSet<>(alg2Map.keySet());
        onlyInModel2.removeAll(alg1Map.keySet());
        
        // 记录差异
        if (!onlyInModel1.isEmpty()) {
            differences.add("模型1独有算法: " + onlyInModel1);
        }
        
        if (!onlyInModel2.isEmpty()) {
            differences.add("模型2独有算法: " + onlyInModel2);
        }
        
        // 对比共同算法的配置
        for (String algorithmCode : commonAlgorithms) {
            Map<String, Object> alg1 = alg1Map.get(algorithmCode);
            Map<String, Object> alg2 = alg2Map.get(algorithmCode);
            
            String expr1 = (String) alg1.get("qlExpression");
            String expr2 = (String) alg2.get("qlExpression");
            
            if (!Objects.equals(expr1, expr2)) {
                differences.add("算法 " + algorithmCode + " 表达式不同");
            } else {
                similarities.add("算法 " + algorithmCode + " 表达式相同");
            }
            
            String output1 = (String) alg1.get("outputParam");
            String output2 = (String) alg2.get("outputParam");
            
            if (!Objects.equals(output1, output2)) {
                differences.add("算法 " + algorithmCode + " 输出参数不同");
            } else {
                similarities.add("算法 " + algorithmCode + " 输出参数相同");
            }
        }
    }
    
    /**
     * 对比指标配置
     */
    private void compareIndicatorConfigurations(Map<String, Object> config1, 
                                              Map<String, Object> config2,
                                              List<String> differences, 
                                              List<String> similarities) {
        
        @SuppressWarnings("unchecked")
        List<String> indicators1 = (List<String>) config1.get("indicators");
        @SuppressWarnings("unchecked")
        List<String> indicators2 = (List<String>) config2.get("indicators");
        
        if (indicators1 == null) indicators1 = new ArrayList<>();
        if (indicators2 == null) indicators2 = new ArrayList<>();
        
        Set<String> set1 = new HashSet<>(indicators1);
        Set<String> set2 = new HashSet<>(indicators2);
        
        Set<String> common = new HashSet<>(set1);
        common.retainAll(set2);
        
        Set<String> onlyIn1 = new HashSet<>(set1);
        onlyIn1.removeAll(set2);
        
        Set<String> onlyIn2 = new HashSet<>(set2);
        onlyIn2.removeAll(set1);
        
        if (!onlyIn1.isEmpty()) {
            differences.add("模型1独有指标: " + onlyIn1);
        }
        
        if (!onlyIn2.isEmpty()) {
            differences.add("模型2独有指标: " + onlyIn2);
        }
        
        if (!common.isEmpty()) {
            similarities.add("共同指标: " + common);
        }
    }
    
    /**
     * 生成对比建议
     */
    private List<String> generateComparisonRecommendations(List<String> differences, List<String> similarities) {
        List<String> recommendations = new ArrayList<>();
        
        if (differences.isEmpty()) {
            recommendations.add("两个模型的TOPSIS配置完全相同");
        } else {
            recommendations.add("发现 " + differences.size() + " 个配置差异，建议检查是否需要统一");
            
            if (differences.stream().anyMatch(d -> d.contains("独有算法"))) {
                recommendations.add("存在独有算法，建议确认是否为模型特定需求");
            }
            
            if (differences.stream().anyMatch(d -> d.contains("表达式不同"))) {
                recommendations.add("算法表达式存在差异，建议验证计算逻辑的正确性");
            }
            
            if (differences.stream().anyMatch(d -> d.contains("独有指标"))) {
                recommendations.add("指标配置存在差异，建议统一指标体系");
            }
        }
        
        return recommendations;
    }
    
    /**
     * 计算配置相似度
     */
    private double calculateConfigurationSimilarity(List<String> differences, List<String> similarities) {
        int totalComparisons = differences.size() + similarities.size();
        if (totalComparisons == 0) {
            return 1.0; // 如果没有可比较的项，认为完全相似
        }
        
        return (double) similarities.size() / totalComparisons;
    }
    
    /**
     * 分析配置质量
     */
    private Map<String, Object> analyzeConfigurationQuality(List<StepAlgorithm> algorithms) {
        Map<String, Object> quality = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        // 检查配置完整性
        boolean hasPositive = algorithms.stream()
                .anyMatch(alg -> alg.getAlgorithmCode().contains("POSITIVE"));
        boolean hasNegative = algorithms.stream()
                .anyMatch(alg -> alg.getAlgorithmCode().contains("NEGATIVE"));
        
        if (!hasPositive) {
            issues.add("缺少正理想解算法配置");
        }
        
        if (!hasNegative) {
            issues.add("缺少负理想解算法配置");
        }
        
        // 检查表达式有效性
        for (StepAlgorithm algorithm : algorithms) {
            if (algorithm.getQlExpression() == null || algorithm.getQlExpression().trim().isEmpty()) {
                issues.add("算法 " + algorithm.getAlgorithmCode() + " 缺少表达式");
            }
            
            if (algorithm.getOutputParam() == null || algorithm.getOutputParam().trim().isEmpty()) {
                issues.add("算法 " + algorithm.getAlgorithmCode() + " 缺少输出参数");
            }
        }
        
        double completeness = hasPositive && hasNegative ? 1.0 : 0.5;
        
        quality.put("completeness", completeness);
        quality.put("issues", issues);
        quality.put("issueCount", issues.size());
        quality.put("hasPositiveAlgorithm", hasPositive);
        quality.put("hasNegativeAlgorithm", hasNegative);
        
        return quality;
    }
    
    /**
     * 生成配置摘要
     */
    private Map<String, Object> generateConfigurationSummary(List<StepAlgorithm> algorithms, Set<String> indicators) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalAlgorithms", algorithms.size());
        summary.put("totalIndicators", indicators.size());
        
        Map<String, Long> algorithmTypes = algorithms.stream()
                .collect(Collectors.groupingBy(
                        alg -> alg.getAlgorithmCode().contains("POSITIVE") ? "POSITIVE" : 
                               alg.getAlgorithmCode().contains("NEGATIVE") ? "NEGATIVE" : "OTHER",
                        Collectors.counting()
                ));
        
        summary.put("algorithmTypes", algorithmTypes);
        
        // 计算平均复杂度
        double avgComplexity = algorithms.stream()
                .mapToInt(alg -> calculateExpressionComplexity(alg.getQlExpression()))
                .average()
                .orElse(0.0);
        
        summary.put("averageComplexity", avgComplexity);
        
        return summary;
    }
    
    /**
     * 验证算法完整性
     */
    private void validateAlgorithmCompleteness(List<StepAlgorithm> algorithms, 
                                             List<String> errors, 
                                             List<String> warnings) {
        
        boolean hasPositive = algorithms.stream()
                .anyMatch(alg -> alg.getAlgorithmCode().contains("POSITIVE"));
        boolean hasNegative = algorithms.stream()
                .anyMatch(alg -> alg.getAlgorithmCode().contains("NEGATIVE"));
        
        if (!hasPositive) {
            errors.add("缺少正理想解算法配置");
        }
        
        if (!hasNegative) {
            errors.add("缺少负理想解算法配置");
        }
        
        if (algorithms.size() < 2) {
            warnings.add("TOPSIS算法配置数量较少，可能影响计算效果");
        }
    }
    
    /**
     * 验证表达式格式
     */
    private void validateExpressionFormat(List<StepAlgorithm> algorithms, 
                                        List<String> errors, 
                                        List<String> warnings) {
        
        for (StepAlgorithm algorithm : algorithms) {
            String expression = algorithm.getQlExpression();
            
            if (expression == null || expression.trim().isEmpty()) {
                errors.add("算法 " + algorithm.getAlgorithmCode() + " 缺少QL表达式");
                continue;
            }
            
            // 简单的格式验证
            if (!expression.contains("@TOPSIS") && !expression.contains("comprehensive")) {
                warnings.add("算法 " + algorithm.getAlgorithmCode() + " 表达式格式可能不正确");
            }
        }
    }
    
    /**
     * 验证输出参数
     */
    private void validateOutputParameters(List<StepAlgorithm> algorithms, 
                                        List<String> errors, 
                                        List<String> warnings) {
        
        Set<String> outputParams = new HashSet<>();
        
        for (StepAlgorithm algorithm : algorithms) {
            String outputParam = algorithm.getOutputParam();
            
            if (outputParam == null || outputParam.trim().isEmpty()) {
                errors.add("算法 " + algorithm.getAlgorithmCode() + " 缺少输出参数");
                continue;
            }
            
            if (outputParams.contains(outputParam)) {
                warnings.add("输出参数 " + outputParam + " 被多个算法使用");
            }
            
            outputParams.add(outputParam);
        }
    }
    
    /**
     * 验证算法顺序
     */
    private void validateAlgorithmOrder(List<StepAlgorithm> algorithms, 
                                      List<String> errors, 
                                      List<String> warnings) {
        
        Set<Integer> orders = new HashSet<>();
        
        for (StepAlgorithm algorithm : algorithms) {
            Integer order = algorithm.getAlgorithmOrder();
            
            if (order == null) {
                warnings.add("算法 " + algorithm.getAlgorithmCode() + " 缺少执行顺序");
                continue;
            }
            
            if (orders.contains(order)) {
                warnings.add("执行顺序 " + order + " 被多个算法使用");
            }
            
            orders.add(order);
        }
    }
    
    /**
     * 生成对比矩阵
     */
    private Map<String, Object> generateComparisonMatrix(Map<Long, Map<String, Object>> modelConfigs) {
        Map<String, Object> matrix = new HashMap<>();
        
        // TODO: 实现详细的对比矩阵生成逻辑
        matrix.put("implemented", false);
        matrix.put("message", "对比矩阵功能待实现");
        
        return matrix;
    }
    
    /**
     * 分析配置一致性
     */
    private Map<String, Object> analyzeConfigurationConsistency(Map<Long, Map<String, Object>> modelConfigs) {
        Map<String, Object> consistency = new HashMap<>();
        
        // TODO: 实现配置一致性分析逻辑
        consistency.put("implemented", false);
        consistency.put("message", "一致性分析功能待实现");
        
        return consistency;
    }
    
    /**
     * 生成统一建议
     */
    private List<String> generateUnifiedRecommendations(Map<Long, Map<String, Object>> modelConfigs) {
        List<String> recommendations = new ArrayList<>();
        
        // TODO: 基于多模型配置生成统一建议
        recommendations.add("统一建议功能待实现");
        
        return recommendations;
    }
    
    /**
     * 生成最佳实践建议
     */
    private List<String> generateBestPracticeRecommendations(Map<String, Object> analysis) {
        List<String> recommendations = new ArrayList<>();
        
        Integer algorithmCount = (Integer) analysis.get("algorithmCount");
        if (algorithmCount != null && algorithmCount < 2) {
            recommendations.add("建议配置完整的正负理想解算法");
        }
        
        Integer totalIndicators = (Integer) analysis.get("totalIndicators");
        if (totalIndicators != null && totalIndicators < 3) {
            recommendations.add("建议增加更多指标以提高评估准确性");
        }
        
        return recommendations;
    }
}
