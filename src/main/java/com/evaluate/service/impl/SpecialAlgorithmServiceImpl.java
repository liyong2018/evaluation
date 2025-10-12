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
        
        log.debug("归一化计算: indicator={}, region={}", indicatorName, currentRegionCode);
        
        // 1. 收集所有区域的指标值
        List<Double> allValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(indicatorName);
            if (value != null) {
                allValues.add(toDouble(value));
            }
        }
        
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
        
        log.debug("归一化结果: indicator={}, region={}, value={}, normalized={}", 
                indicatorName, currentRegionCode, currentValue, normalized);
        
        return normalized;
    }

    @Override
    public Double calculateTopsisPositive(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
        log.debug("TOPSIS优解计算: indicators={}, region={}", indicators, currentRegionCode);
        
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
                allScores.add(toDouble(value));
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
        
        // 3. 计算标准差 σ
        double variance = allScores.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        double stdev = Math.sqrt(variance);
        
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
        
        log.debug("分级结果: region={}, score={}, mean={}, stdev={}, grade={}", 
                currentRegionCode, score, mean, stdev, grade);
        
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
        if (mean <= 0.5 * stdev) {
            // 情况1：均值较小
            if (value >= mean + 1.5 * stdev) {
                return "强";
            } else if (value >= mean + 0.5 * stdev) {
                return "较强";
            } else {
                return "中等";
            }
        } else if (mean <= 1.5 * stdev) {
            // 情况2：均值中等
            if (value >= mean + 1.5 * stdev) {
                return "强";
            } else if (value >= mean + 0.5 * stdev) {
                return "较强";
            } else if (value >= mean - 0.5 * stdev) {
                return "中等";
            } else {
                return "较弱";
            }
        } else {
            // 情况3：均值较大
            if (value >= mean + 1.5 * stdev) {
                return "强";
            } else if (value >= mean + 0.5 * stdev) {
                return "较强";
            } else if (value >= mean - 0.5 * stdev) {
                return "中等";
            } else if (value >= mean - 1.5 * stdev) {
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
