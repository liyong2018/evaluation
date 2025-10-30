package com.evaluate.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.evaluate.service.TOPSISCalculationLogger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TOPSIS计算日志记录器实现类
 * 
 * 实现详细的TOPSIS计算过程日志记录功能
 * 
 * @author System
 * @since 2025-01-01
 */
@Service
public class TOPSISCalculationLoggerImpl implements TOPSISCalculationLogger {

    private static final Logger log = LoggerFactory.getLogger(TOPSISCalculationLoggerImpl.class);

    
    // 使用ThreadLocal存储当前线程的日志会话
    private final ThreadLocal<Map<String, Object>> currentSessionLog = new ThreadLocal<>();
    
    // 全局日志存储（可选，用于持久化）
    private final Map<String, Map<String, Object>> globalLogStorage = new ConcurrentHashMap<>();
    
    @Override
    public void logCalculationStart(Long modelId, String stepCode, Map<String, Map<String, Double>> inputData) {
        log.info("=== TOPSIS计算开始 === 模型ID: {}, 步骤: {}", modelId, stepCode);
        
        Map<String, Object> sessionLog = initializeSessionLog();
        sessionLog.put("modelId", modelId);
        sessionLog.put("stepCode", stepCode);
        sessionLog.put("startTime", System.currentTimeMillis());
        sessionLog.put("inputDataSize", inputData != null ? inputData.size() : 0);
        
        // 记录输入数据摘要
        Map<String, Object> inputSummary = generateInputDataSummary(inputData);
        sessionLog.put("inputSummary", inputSummary);
        
        log.info("输入数据摘要: 区域数量={}, 指标数量={}", 
                inputSummary.get("regionCount"), inputSummary.get("indicatorCount"));
        
        // 详细记录输入数据
        if (inputData != null && log.isDebugEnabled()) {
            for (Map.Entry<String, Map<String, Double>> entry : inputData.entrySet()) {
                log.debug("区域 {} 数据: {}", entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
    public void logIdealSolutionCalculation(Map<String, Double> positiveIdeal, Map<String, Double> negativeIdeal) {
        log.info("=== 理想解计算 ===");
        
        Map<String, Object> sessionLog = getCurrentSessionLog();
        Map<String, Object> idealSolutionLog = new HashMap<>();
        idealSolutionLog.put("timestamp", System.currentTimeMillis());
        idealSolutionLog.put("positiveIdeal", new HashMap<>(positiveIdeal));
        idealSolutionLog.put("negativeIdeal", new HashMap<>(negativeIdeal));
        
        sessionLog.put("idealSolutionCalculation", idealSolutionLog);
        
        log.info("正理想解: {}", positiveIdeal);
        log.info("负理想解: {}", negativeIdeal);
        
        // 分析理想解的特征
        analyzeIdealSolutions(positiveIdeal, negativeIdeal);
    }
    
    @Override
    public void logDistanceCalculation(String regionCode, Map<String, Double> regionData, 
                                     double positiveDistance, double negativeDistance) {
        
        Map<String, Object> sessionLog = getCurrentSessionLog();
        
        // 获取或创建距离计算日志列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> distanceCalculations = 
                (List<Map<String, Object>>) sessionLog.computeIfAbsent("distanceCalculations", k -> new ArrayList<>());
        
        Map<String, Object> distanceLog = new HashMap<>();
        distanceLog.put("regionCode", regionCode);
        distanceLog.put("regionData", new HashMap<>(regionData));
        distanceLog.put("positiveDistance", positiveDistance);
        distanceLog.put("negativeDistance", negativeDistance);
        distanceLog.put("timestamp", System.currentTimeMillis());
        
        // 计算综合能力值
        double comprehensiveScore = 0.0;
        if (positiveDistance + negativeDistance > 0) {
            comprehensiveScore = negativeDistance / (negativeDistance + positiveDistance);
        }
        distanceLog.put("comprehensiveScore", comprehensiveScore);
        
        distanceCalculations.add(distanceLog);
        
        log.debug("区域 {} 距离计算: 正理想解距离={}, 负理想解距离={}, 综合能力值={}", 
                regionCode, positiveDistance, negativeDistance, comprehensiveScore);
        
        // 检查异常情况
        validateDistanceCalculation(regionCode, positiveDistance, negativeDistance);
    }
    
    @Override
    public void logDataValidation(Map<String, Object> validationResult) {
        log.info("=== 数据验证 ===");
        
        Map<String, Object> sessionLog = getCurrentSessionLog();
        sessionLog.put("dataValidation", validationResult);
        
        Boolean isValid = (Boolean) validationResult.get("valid");
        log.info("数据验证结果: {}", isValid ? "通过" : "失败");
        
        if (!isValid) {
            @SuppressWarnings("unchecked")
            List<String> issues = (List<String>) validationResult.get("issues");
            if (issues != null) {
                log.warn("数据验证问题:");
                for (String issue : issues) {
                    log.warn("  - {}", issue);
                }
            }
        }
        
        // 记录数据质量统计
        logDataQualityStatistics(validationResult);
    }
    
    @Override
    public void logException(String step, Exception exception, Map<String, Object> context) {
        log.error("=== 计算异常 === 步骤: {}", step, exception);
        
        Map<String, Object> sessionLog = getCurrentSessionLog();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> exceptions = 
                (List<Map<String, Object>>) sessionLog.computeIfAbsent("exceptions", k -> new ArrayList<>());
        
        Map<String, Object> exceptionLog = new HashMap<>();
        exceptionLog.put("step", step);
        exceptionLog.put("exceptionType", exception.getClass().getSimpleName());
        exceptionLog.put("message", exception.getMessage());
        exceptionLog.put("timestamp", System.currentTimeMillis());
        exceptionLog.put("context", context != null ? new HashMap<>(context) : new HashMap<>());
        
        exceptions.add(exceptionLog);
        
        log.error("异常详情: 步骤={}, 类型={}, 消息={}", step, exception.getClass().getSimpleName(), exception.getMessage());
        if (context != null && !context.isEmpty()) {
            log.error("异常上下文: {}", context);
        }
    }
    
    @Override
    public void logCalculationComplete(Map<String, Map<String, Double>> results, long calculationTimeMs) {
        log.info("=== TOPSIS计算完成 ===");
        
        Map<String, Object> sessionLog = getCurrentSessionLog();
        sessionLog.put("endTime", System.currentTimeMillis());
        sessionLog.put("calculationTimeMs", calculationTimeMs);
        sessionLog.put("resultSize", results != null ? results.size() : 0);
        
        log.info("计算耗时: {}ms, 结果数量: {}", calculationTimeMs, results != null ? results.size() : 0);
        
        // 分析计算结果
        if (results != null && !results.isEmpty()) {
            analyzeCalculationResults(results, sessionLog);
        }
        
        // 生成计算摘要
        generateCalculationSummary(sessionLog);
        
        // 保存到全局存储（可选）
        String sessionId = generateSessionId(sessionLog);
        globalLogStorage.put(sessionId, new HashMap<>(sessionLog));
        
        log.info("TOPSIS计算会话完成，会话ID: {}", sessionId);
    }
    
    @Override
    public Map<String, Object> getCurrentSessionLog() {
        Map<String, Object> sessionLog = currentSessionLog.get();
        if (sessionLog == null) {
            sessionLog = initializeSessionLog();
        }
        return sessionLog;
    }
    
    @Override
    public void clearCurrentSessionLog() {
        currentSessionLog.remove();
        log.debug("当前会话日志已清理");
    }
    
    /**
     * 初始化会话日志
     */
    private Map<String, Object> initializeSessionLog() {
        Map<String, Object> sessionLog = new HashMap<>();
        sessionLog.put("sessionId", UUID.randomUUID().toString());
        sessionLog.put("threadId", Thread.currentThread().getId());
        sessionLog.put("threadName", Thread.currentThread().getName());
        currentSessionLog.set(sessionLog);
        return sessionLog;
    }
    
    /**
     * 生成输入数据摘要
     */
    private Map<String, Object> generateInputDataSummary(Map<String, Map<String, Double>> inputData) {
        Map<String, Object> summary = new HashMap<>();
        
        if (inputData == null || inputData.isEmpty()) {
            summary.put("regionCount", 0);
            summary.put("indicatorCount", 0);
            summary.put("empty", true);
            return summary;
        }
        
        Set<String> allIndicators = new HashSet<>();
        int totalValues = 0;
        int validValues = 0;
        
        for (Map<String, Double> regionData : inputData.values()) {
            if (regionData != null) {
                allIndicators.addAll(regionData.keySet());
                for (Double value : regionData.values()) {
                    totalValues++;
                    if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                        validValues++;
                    }
                }
            }
        }
        
        summary.put("regionCount", inputData.size());
        summary.put("indicatorCount", allIndicators.size());
        summary.put("totalValues", totalValues);
        summary.put("validValues", validValues);
        summary.put("dataCompleteness", totalValues > 0 ? (double) validValues / totalValues : 0.0);
        summary.put("indicators", new ArrayList<>(allIndicators));
        summary.put("regions", new ArrayList<>(inputData.keySet()));
        
        return summary;
    }
    
    /**
     * 分析理想解
     */
    private void analyzeIdealSolutions(Map<String, Double> positiveIdeal, Map<String, Double> negativeIdeal) {
        int sameValueCount = 0;
        int zeroRangeCount = 0;
        
        for (String indicator : positiveIdeal.keySet()) {
            Double maxValue = positiveIdeal.get(indicator);
            Double minValue = negativeIdeal.get(indicator);
            
            if (maxValue != null && minValue != null) {
                if (maxValue.equals(minValue)) {
                    sameValueCount++;
                    log.warn("指标 {} 的最大值和最小值相同: {}", indicator, maxValue);
                }
                
                if (Math.abs(maxValue - minValue) < 1e-10) {
                    zeroRangeCount++;
                    log.warn("指标 {} 的取值范围接近0: max={}, min={}", indicator, maxValue, minValue);
                }
            }
        }
        
        if (sameValueCount > 0) {
            log.warn("发现 {} 个指标的最大值和最小值相同，可能影响TOPSIS计算效果", sameValueCount);
        }
        
        if (zeroRangeCount > 0) {
            log.warn("发现 {} 个指标的取值范围接近0，可能导致计算不稳定", zeroRangeCount);
        }
    }
    
    /**
     * 验证距离计算
     */
    private void validateDistanceCalculation(String regionCode, double positiveDistance, double negativeDistance) {
        if (Double.isNaN(positiveDistance) || Double.isNaN(negativeDistance)) {
            log.error("区域 {} 的距离计算结果为NaN: 正理想解距离={}, 负理想解距离={}", 
                    regionCode, positiveDistance, negativeDistance);
        }
        
        if (Double.isInfinite(positiveDistance) || Double.isInfinite(negativeDistance)) {
            log.error("区域 {} 的距离计算结果为无穷: 正理想解距离={}, 负理想解距离={}", 
                    regionCode, positiveDistance, negativeDistance);
        }
        
        if (positiveDistance < 0 || negativeDistance < 0) {
            log.error("区域 {} 的距离计算结果为负数: 正理想解距离={}, 负理想解距离={}", 
                    regionCode, positiveDistance, negativeDistance);
        }
        
        if (positiveDistance == 0.0 && negativeDistance == 0.0) {
            log.warn("区域 {} 到正负理想解的距离都为0，可能所有指标值都相同", regionCode);
        }
    }
    
    /**
     * 记录数据质量统计
     */
    private void logDataQualityStatistics(Map<String, Object> validationResult) {
        Integer totalValues = (Integer) validationResult.get("totalValues");
        Integer nanCount = (Integer) validationResult.get("nanCount");
        Integer infiniteCount = (Integer) validationResult.get("infiniteCount");
        Integer zeroCount = (Integer) validationResult.get("zeroCount");
        
        if (totalValues != null && totalValues > 0) {
            log.info("数据质量统计: 总值数={}, NaN数={}, 无穷值数={}, 零值数={}", 
                    totalValues, nanCount, infiniteCount, zeroCount);
            
            if (nanCount != null && nanCount > 0) {
                double nanRate = (double) nanCount / totalValues;
                log.warn("NaN值比例: {:.2%}", nanRate);
            }
            
            if (infiniteCount != null && infiniteCount > 0) {
                double infiniteRate = (double) infiniteCount / totalValues;
                log.warn("无穷值比例: {:.2%}", infiniteRate);
            }
            
            if (zeroCount != null && zeroCount > 0) {
                double zeroRate = (double) zeroCount / totalValues;
                log.info("零值比例: {:.2%}", zeroRate);
            }
        }
    }
    
    /**
     * 分析计算结果
     */
    private void analyzeCalculationResults(Map<String, Map<String, Double>> results, Map<String, Object> sessionLog) {
        List<Double> positiveDistances = new ArrayList<>();
        List<Double> negativeDistances = new ArrayList<>();
        List<Double> comprehensiveScores = new ArrayList<>();
        
        int validResults = 0;
        int invalidResults = 0;
        
        for (Map.Entry<String, Map<String, Double>> entry : results.entrySet()) {
            Map<String, Double> distances = entry.getValue();
            Double pos = distances.get("comprehensive_positive");
            Double neg = distances.get("comprehensive_negative");
            
            if (pos != null && neg != null && 
                !Double.isNaN(pos) && !Double.isNaN(neg) && 
                !Double.isInfinite(pos) && !Double.isInfinite(neg)) {
                
                positiveDistances.add(pos);
                negativeDistances.add(neg);
                
                if (pos + neg > 0) {
                    comprehensiveScores.add(neg / (neg + pos));
                }
                validResults++;
            } else {
                invalidResults++;
            }
        }
        
        // 记录结果统计
        Map<String, Object> resultAnalysis = new HashMap<>();
        resultAnalysis.put("validResults", validResults);
        resultAnalysis.put("invalidResults", invalidResults);
        
        if (!positiveDistances.isEmpty()) {
            resultAnalysis.put("positiveDistanceStats", calculateStatistics(positiveDistances));
            resultAnalysis.put("negativeDistanceStats", calculateStatistics(negativeDistances));
        }
        
        if (!comprehensiveScores.isEmpty()) {
            resultAnalysis.put("comprehensiveScoreStats", calculateStatistics(comprehensiveScores));
            
            // 检查分布特征
            long distinctScores = comprehensiveScores.stream().distinct().count();
            if (distinctScores == 1) {
                log.warn("所有区域的综合能力值都相同: {}", comprehensiveScores.get(0));
            } else if (distinctScores < comprehensiveScores.size() * 0.5) {
                log.warn("综合能力值缺乏区分度，不同值的数量: {}/{}", distinctScores, comprehensiveScores.size());
            }
        }
        
        sessionLog.put("resultAnalysis", resultAnalysis);
        
        log.info("结果分析: 有效结果={}, 无效结果={}", validResults, invalidResults);
    }
    
    /**
     * 计算统计信息
     */
    private Map<String, Double> calculateStatistics(List<Double> values) {
        Map<String, Double> stats = new HashMap<>();
        
        if (values.isEmpty()) {
            return stats;
        }
        
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / values.size();
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        
        // 计算标准差
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        stats.put("count", (double) values.size());
        stats.put("sum", sum);
        stats.put("mean", mean);
        stats.put("min", min);
        stats.put("max", max);
        stats.put("range", max - min);
        stats.put("stdDev", stdDev);
        
        return stats;
    }
    
    /**
     * 生成计算摘要
     */
    private void generateCalculationSummary(Map<String, Object> sessionLog) {
        Map<String, Object> summary = new HashMap<>();
        
        Long startTime = (Long) sessionLog.get("startTime");
        Long endTime = (Long) sessionLog.get("endTime");
        if (startTime != null && endTime != null) {
            summary.put("totalTimeMs", endTime - startTime);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> inputSummary = (Map<String, Object>) sessionLog.get("inputSummary");
        if (inputSummary != null) {
            summary.put("processedRegions", inputSummary.get("regionCount"));
            summary.put("processedIndicators", inputSummary.get("indicatorCount"));
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> exceptions = (List<Map<String, Object>>) sessionLog.get("exceptions");
        summary.put("exceptionCount", exceptions != null ? exceptions.size() : 0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultAnalysis = (Map<String, Object>) sessionLog.get("resultAnalysis");
        if (resultAnalysis != null) {
            summary.put("validResults", resultAnalysis.get("validResults"));
            summary.put("invalidResults", resultAnalysis.get("invalidResults"));
        }
        
        sessionLog.put("summary", summary);
        
        log.info("计算摘要: {}", summary);
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId(Map<String, Object> sessionLog) {
        Long modelId = (Long) sessionLog.get("modelId");
        String stepCode = (String) sessionLog.get("stepCode");
        Long startTime = (Long) sessionLog.get("startTime");
        
        return String.format("TOPSIS_%s_%s_%s", 
                modelId != null ? modelId : "unknown",
                stepCode != null ? stepCode : "unknown",
                startTime != null ? startTime : System.currentTimeMillis());
    }
}
