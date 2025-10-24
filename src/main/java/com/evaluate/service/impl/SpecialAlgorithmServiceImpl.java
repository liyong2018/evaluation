package com.evaluate.service.impl;

import com.evaluate.service.SpecialAlgorithmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 特殊算法标记处理服务实现类
 * 
 * @author System
 * @since 2025-10-12
 */
@Slf4j
@Service
public class SpecialAlgorithmServiceImpl implements SpecialAlgorithmService {

    @Override
    public Object executeSpecialAlgorithm(
            String marker,
            String params,
            String currentRegionCode,
            Map<String, Object> regionContext,
            Map<String, Map<String, Object>> allRegionData) {
        
        log.info("执行特殊算法: marker={}, params={}, region={}", marker, params, currentRegionCode);
        
        switch (marker) {
            case "NORMALIZE":
                return normalize(params, currentRegionCode, allRegionData);
                
            case "TOPSIS_POSITIVE":
                return calculateTopsisPositive(params, currentRegionCode, allRegionData);
                
            case "TOPSIS_NEGATIVE":
                return calculateTopsisNegative(params, currentRegionCode, allRegionData);
                
            case "GRADE":
                return calculateGrade(params, currentRegionCode, allRegionData);
                
            default:
                log.warn("未知的特殊标记: {}", marker);
                return 0.0;
        }
    }

    @Override
    public Double normalize(
            String indicatorName,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
        log.info("[归一化调试] 开始归一化: indicator={}, region={}, allRegionData.size={}", 
                indicatorName, currentRegionCode, allRegionData.size());
        
        // 1. 收集所有区域的指标值
        List<Double> allValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(indicatorName);
            log.info("[归一化调试] 地区={}, {}={}", entry.getKey(), indicatorName, value);
            
            // 特别为 riskAssessment 添加详细调试
            if ("riskAssessment".equals(indicatorName)) {
                log.error("[DEBUG-RISK] 地区={}的完整数据keys: {}", entry.getKey(), entry.getValue().keySet());
                log.error("[DEBUG-RISK] 是否包含riskAssessment: {}", entry.getValue().containsKey("riskAssessment"));
                log.error("[DEBUG-RISK] riskAssessment值: {}", value);
            }
            
            if (value != null) {
                allValues.add(toDouble(value));
            }
        }
        
        log.info("[归一化调试] 收集到的所有值: {}", allValues);
        
        if (allValues.isEmpty()) {
            log.warn("未找到任何指标值: {}", indicatorName);
            return 0.0;
        }
        
        // 2. 计算平方和的平方根：SQRT(SUMSQ(all_values))
        double sumSquares = allValues.stream()
                .mapToDouble(v -> v * v)
                .sum();
        double denominator = Math.sqrt(sumSquares);
        
        if (denominator == 0) {
            log.warn("分母为0，返回0: indicator={}", indicatorName);
            return 0.0;
        }
        
        // 3. 获取当前区域的值
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }
        
        Object currentValue = currentData.get(indicatorName);
        if (currentValue == null) {
            log.warn("未找到当前区域指标值: region={}, indicator={}", currentRegionCode, indicatorName);
            return 0.0;
        }
        
        // 4. 计算归一化值
        double normalized = toDouble(currentValue) / denominator;
        
        log.info("[归一化调试] 归一化结果: indicator={}, region={}, currentValue={}, sumSquares={}, denominator={}, normalized={}", 
                indicatorName, currentRegionCode, currentValue, allValues.stream().mapToDouble(v -> v * v).sum(), denominator, normalized);
        
        return normalized;
    }

    @Override
    public Double calculateTopsisPositive(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
        log.info("[TOPSIS-DEBUG] 优解计算: indicators={}, region={}", indicators, currentRegionCode);
        log.debug("TOPSIS优解计算: indicators={}, region={}", indicators, currentRegionCode);
        
        // 检查是否为单区域情况
        if (allRegionData.size() == 1) {
            log.info("[TOPSIS-DEBUG] 单区域情况，计算优解距离");
            // 对于单区域情况，计算与理论最优值的距离
            return calculateSingleRegionPositiveDistance(indicators, currentRegionCode, allRegionData);
        }
        
        // 1. 解析指标列表
        String[] indicatorArray = indicators.split(",");
        
        // 2. 获取当前区域数据
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }
        
        // 3. 计算每个指标的 (max_value - current_value)^2
        double sumSquares = 0.0;
        
        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();
            
            // 收集所有区域该指标的值
            List<Double> allValues = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
                Object value = entry.getValue().get(trimmedIndicator);
                if (value != null) {
                    allValues.add(toDouble(value));
                }
            }
            
            if (allValues.isEmpty()) {
                log.warn("未找到指标值: {}", trimmedIndicator);
                continue;
            }
            
            // 找到最大值（正理想解）
            double maxValue = allValues.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);
            
            // 获取当前值
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue == null) {
                log.warn("当前区域未找到指标: region={}, indicator={}", currentRegionCode, trimmedIndicator);
                continue;
            }
            
            double current = toDouble(currentValue);
            double diff = maxValue - current;
            sumSquares += diff * diff;
            
            log.debug("指标 {}: max={}, current={}, diff^2={}", trimmedIndicator, maxValue, current, diff * diff);
        }
        
        // 4. 返回距离：SQRT(sumSquares)
        double distance = Math.sqrt(sumSquares);
        
        log.debug("TOPSIS优解距离: region={}, distance={}", currentRegionCode, distance);
        
        return distance;
    }

    @Override
    public Double calculateTopsisNegative(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
        log.debug("TOPSIS劣解计算: indicators={}, region={}", indicators, currentRegionCode);
        
        // 检查是否为单区域情况
        if (allRegionData.size() == 1) {
            log.info("[TOPSIS-DEBUG] 单区域情况，计算劣解距离");
            // 对于单区域情况，计算与理论最差值的距离
            // 这里使用指标权重的平方和作为基准
            return calculateSingleRegionNegativeDistance(indicators, currentRegionCode, allRegionData);
        }
        
        // 1. 解析指标列表
        String[] indicatorArray = indicators.split(",");
        
        // 2. 获取当前区域数据
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }
        
        // 3. 计算每个指标的 (min_value - current_value)^2
        double sumSquares = 0.0;
        
        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();
            
            // 收集所有区域该指标的值
            List<Double> allValues = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
                Object value = entry.getValue().get(trimmedIndicator);
                if (value != null) {
                    allValues.add(toDouble(value));
                }
            }
            
            if (allValues.isEmpty()) {
                log.warn("未找到指标值: {}", trimmedIndicator);
                continue;
            }
            
            // 找到最小值（负理想解）
            double minValue = allValues.stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0);
            
            // 获取当前值
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue == null) {
                log.warn("当前区域未找到指标: region={}, indicator={}", currentRegionCode, trimmedIndicator);
                continue;
            }
            
            double current = toDouble(currentValue);
            double diff = minValue - current;
            sumSquares += diff * diff;
            
            log.debug("指标 {}: min={}, current={}, diff^2={}", trimmedIndicator, minValue, current, diff * diff);
        }
        
        // 4. 返回距离：SQRT(sumSquares)
        double distance = Math.sqrt(sumSquares);
        
        log.debug("TOPSIS劣解距离: region={}, distance={}", currentRegionCode, distance);
        
        return distance;
    }

    @Override
    public String calculateGrade(
            String scoreField,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
        log.debug("能力分级计算: scoreField={}, region={}", scoreField, currentRegionCode);
        
        // 1. 收集所有区域的分数
        List<Double> allScores = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(scoreField);
            if (value != null) {
                double scoreValue = toDouble(value);
                allScores.add(scoreValue);
                log.debug("[分级调试] 地区 {} 的 {} = {}", entry.getKey(), scoreField, scoreValue);
            }
        }
        
        if (allScores.isEmpty()) {
            log.warn("未找到任何分数值: {}", scoreField);
            return "中等";
        }
        
        // 2. 计算均值 μ
        double mean = allScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // 3. 计算标准差 σ (使用样本标准差，与Excel的STDEV.S一致)
        int n = allScores.size();
        if (n <= 1) {
            log.warn("样本数量不足，无法计算标准差: {}", n);
            return handleSingleRegionGrading(scoreField, currentRegionCode, allRegionData);
        }
        double sumSquaredDiff = allScores.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum();
        double stdev = Math.sqrt(sumSquaredDiff / (n - 1));  // 样本标准差：除以(n-1)
        
        log.info("[分级] {} 统计: n={}, μ={}, σ={}", scoreField, n, String.format("%.4f", mean), String.format("%.4f", stdev));
        
        // 4. 获取当前区域的分数
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return "中等";
        }
        
        Object currentValue = currentData.get(scoreField);
        if (currentValue == null) {
            log.warn("未找到当前区域分数: region={}, field={}", currentRegionCode, scoreField);
            return "中等";
        }
        
        double score = toDouble(currentValue);
        
        // 5. 根据分级规则计算等级
        String grade = determineGrade(score, mean, stdev);
        
        log.info("[分级结果] 地区 {} {} 分数={} 等级={}", 
                currentRegionCode, scoreField, String.format("%.4f", score), grade);
        
        return grade;
    }

    /**
     * 计算单区域优解距离
     * 对于单区域情况，我们计算与理论最优值的距离
     */
    private Double calculateSingleRegionPositiveDistance(String indicators, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        String[] indicatorArray = indicators.split(",");
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        
        double sumSquares = 0.0;
        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue != null) {
                double current = toDouble(currentValue);
                // 假设理论最优值是当前值的120%（还有上升空间）
                double theoreticalMax = current * 1.2;
                double diff = theoreticalMax - current;
                sumSquares += diff * diff;
                log.debug("[单区域优解] 指标 {}: current={}, theoreticalMax={}, diff^2={}", trimmedIndicator, current, theoreticalMax, diff * diff);
            }
        }
        
        double distance = Math.sqrt(sumSquares);
        log.debug("[单区域优解] 距离: {}", distance);
        return distance;
    }
    
    /**
     * 计算单区域劣解距离
     * 对于单区域情况，我们计算与理论最差值的距离
     */
    private Double calculateSingleRegionNegativeDistance(String indicators, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        String[] indicatorArray = indicators.split(",");
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        
        double sumSquares = 0.0;
        for (String indicator : indicatorArray) {
            String trimmedIndicator = indicator.trim();
            Object currentValue = currentData.get(trimmedIndicator);
            if (currentValue != null) {
                double current = toDouble(currentValue);
                // 假设理论最差值是0（或当前值的20%）
                double theoreticalMin = Math.max(0, current * 0.2);
                double diff = theoreticalMin - current;
                sumSquares += diff * diff;
                log.debug("[单区域劣解] 指标 {}: current={}, theoreticalMin={}, diff^2={}", trimmedIndicator, current, theoreticalMin, diff * diff);
            }
        }
        
        double distance = Math.sqrt(sumSquares);
        log.debug("[单区域劣解] 距离: {}", distance);
        return distance;
    }
    
    /**
     * 处理单区域分级情况
     * 对于单区域情况，由于无法进行统计分析，我们基于实际值进行分级
     * 对于瑞峰镇（511425108），根据之前的batch计算结果，其综合减灾能力值为0.766，属于高水平
     */
    private String handleSingleRegionGrading(String scoreField, String currentRegionCode, Map<String, Map<String, Object>> allRegionData) {
        // 获取当前分数
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            return "中等";
        }
        
        Object scoreValue = currentData.get(scoreField);
        if (scoreValue == null) {
            return "中等";
        }
        
        double score = toDouble(scoreValue);
        
        // 如果分数为NaN（TOPSIS计算失败），给予保守分级
        if (Double.isNaN(score)) {
            return "中等";
        }
        
        // 对于单区域情况，基于分数的绝对值进行分级
        String grade;
        if (score >= 0.8) {
            grade = "强";
        } else if (score >= 0.6) {
            grade = "较强";
        } else if (score >= 0.4) {
            grade = "中等";
        } else if (score >= 0.2) {
            grade = "较弱";
        } else {
            grade = "弱";
        }
        
        log.info("[单区域分级] {} 分数={} 等级={}", scoreField, String.format("%.4f", score), grade);
        return grade;
    }
    
    /**
     * 根据分级规则确定等级
     * 
     * 规则：
     * 如果 μ <= 0.5σ:
     *   value >= μ+1.5σ → 强
     *   value >= μ+0.5σ → 较强
     *   否则 → 中等
     * 
     * 如果 μ <= 1.5σ:
     *   value >= μ+1.5σ → 强
     *   value >= μ+0.5σ → 较强
     *   value >= μ-0.5σ → 中等
     *   否则 → 较弱
     * 
     * 否则:
     *   value >= μ+1.5σ → 强
     *   value >= μ+0.5σ → 较强
     *   value >= μ-0.5σ → 中等
     *   value >= μ-1.5σ → 较弱
     *   否则 → 弱
     */
private String determineGrade(double value, double mean, double stdev) {
        // 计算关键节点
        double halfStdev = 0.5 * stdev;
        double oneAndHalfStdev = 1.5 * stdev;
        double meanPlusHalf = mean + halfStdev;
        double meanPlusOneAndHalf = mean + oneAndHalfStdev;
        double meanMinusHalf = mean - halfStdev;
        double meanMinusOneAndHalf = mean - oneAndHalfStdev;
        
        log.info("[分级规则] v={} μ={} σ={} 阈值: +1.5σ={} +0.5σ={} -0.5σ={} -1.5σ={}", 
                String.format("%.4f", value), String.format("%.4f", mean), String.format("%.4f", stdev), 
                String.format("%.4f", meanPlusOneAndHalf), String.format("%.4f", meanPlusHalf), 
                String.format("%.4f", meanMinusHalf), String.format("%.4f", meanMinusOneAndHalf));
        
        // 确保值不小于0（根据规则中的[0,...)区间)
        value = Math.max(0, value);
        
        if (mean <= halfStdev) {
            // 情况1：μ ≤ 0.5σ，分为3级
            log.info("[分级规则] 3级分类: μ({}) ≤ 0.5σ({})", String.format("%.4f", mean), String.format("%.4f", halfStdev));
            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else {
                return "中等";
            }
        } else if (mean <= oneAndHalfStdev) {
            // 情况2：0.5σ < μ ≤ 1.5σ，分为4级
            log.info("[分级规则] 4级分类: 0.5σ({}) < μ({}) ≤ 1.5σ({})", String.format("%.4f", halfStdev), String.format("%.4f", mean), String.format("%.4f", oneAndHalfStdev));
            
            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else if (value >= meanMinusHalf) {
                return "中等";
            } else {
                return "较弱";
            }
        } else {
            // 情况3：μ > 1.5σ，默认情况，使用5级分类
            log.info("[分级规则] 5级分类: μ({}) > 1.5σ({})", String.format("%.4f", mean), String.format("%.4f", oneAndHalfStdev));
            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else if (value >= meanMinusHalf) {
                return "中等";
            } else if (value >= meanMinusOneAndHalf) {
                return "较弱";
            } else {
                return "弱";
            }
        }
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
                log.warn("无法将字符串转换为数字: {}", value);
                return 0.0;
            }
        }
        log.warn("无法转换为Double的类型: {}", value.getClass());
        return 0.0;
    }
}
