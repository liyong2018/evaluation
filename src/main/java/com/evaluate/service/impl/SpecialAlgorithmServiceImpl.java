package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.EvaluationResult;
import com.evaluate.mapper.EvaluationResultMapper;
import com.evaluate.service.SpecialAlgorithmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;

    @Override
    public Object executeSpecialAlgorithm(
            String marker,
            String params,
            String currentRegionCode,
            Map<String, Object> regionContext,
            Map<String, Map<String, Object>> allRegionData) {


        switch (marker) {
            case "LOAD_EVAL_RESULT":
                return loadEvaluationResult(params, currentRegionCode, regionContext);

            case "NORMALIZE":
                return normalize(params, currentRegionCode, allRegionData);

            case "TOPSIS_POSITIVE":
                return calculateTopsisPositive(params, currentRegionCode, allRegionData);

            case "TOPSIS_NEGATIVE":
                return calculateTopsisNegative(params, currentRegionCode, allRegionData);

            case "TOPSIS_SCORE":
                return calculateTopsisScore(params, currentRegionCode, allRegionData);

            case "GRADE":
                return calculateGrade(params, currentRegionCode, allRegionData);

            default:
                log.warn("未知的特殊标记: {}", marker);
                return 0.0;
        }
    }

    /**
     * 从evaluation_result表加载评估结果
     * 参数格式：modelId=3,field=management_capability_score
     *
     * @param params 参数字符串
     * @param currentRegionCode 当前地区代码
     * @param regionContext 地区上下文
     * @return 字段值
     */
    private Double loadEvaluationResult(String params, String currentRegionCode, Map<String, Object> regionContext) {
        log.info("[LOAD_EVAL_RESULT] 加载评估结果: params={}, region={}", params, currentRegionCode);

        // 解析参数
        Map<String, String> paramMap = parseParams(params);
        String modelIdStr = paramMap.get("modelId");
        String fieldName = paramMap.get("field");

        if (modelIdStr == null || fieldName == null) {
            log.error("[LOAD_EVAL_RESULT] 参数不完整: modelId={}, field={}", modelIdStr, fieldName);
            return 0.0;
        }

        Long modelId = Long.parseLong(modelIdStr);

        // 从数据库查询评估结果
        QueryWrapper<EvaluationResult> query = new QueryWrapper<>();
        query.eq("evaluation_model_id", modelId)
             .eq("region_code", currentRegionCode)
             .orderByDesc("id")  // 获取最新记录
             .last("LIMIT 1");

        EvaluationResult result = evaluationResultMapper.selectOne(query);

        if (result == null) {
            log.warn("[LOAD_EVAL_RESULT] 未找到评估结果: modelId={}, regionCode={}", modelId, currentRegionCode);
            return 0.0;
        }

        // 根据字段名提取值
        Double value = extractFieldValue(result, fieldName);

        log.info("[LOAD_EVAL_RESULT] 加载成功: modelId={}, region={}, field={}, value={}",
                modelId, currentRegionCode, fieldName, value);

        return value;
    }

    /**
     * 解析参数字符串为Map
     * 例如：modelId=3,field=management_capability_score
     */
    private Map<String, String> parseParams(String params) {
        Map<String, String> paramMap = new HashMap<>();
        String[] pairs = params.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                paramMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        return paramMap;
    }

    /**
     * 从EvaluationResult对象中提取指定字段的值
     */
    private Double extractFieldValue(EvaluationResult result, String fieldName) {
        java.math.BigDecimal bdValue = null;

        switch (fieldName) {
            case "management_capability_score":
                bdValue = result.getManagementCapabilityScore();
                break;
            case "support_capability_score":
                bdValue = result.getSupportCapabilityScore();
                break;
            case "self_rescue_capability_score":
                bdValue = result.getSelfRescueCapabilityScore();
                break;
            case "comprehensive_capability_score":
                bdValue = result.getComprehensiveCapabilityScore();
                break;
            default:
                log.warn("未知的字段名: {}", fieldName);
                return 0.0;
        }

        return bdValue != null ? bdValue.doubleValue() : 0.0;
    }

    @Override
    public Double normalize(
            String indicatorName,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
   
        
        // 1. 收集所有区域的指标值
        List<Double> allValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allRegionData.entrySet()) {
            Object value = entry.getValue().get(indicatorName);
       
            
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
            log.warn("分母为0，所有值都是0或接近0，直接返回当前值: indicator={}", indicatorName);
            // 当分母为0时，说明所有地区的该指标值都是0或接近0
            // 这种情况下，归一化没有意义，直接返回当前区域的值
            Map<String, Object> currentData = allRegionData.get(currentRegionCode);
            if (currentData != null) {
                Object currentValue = currentData.get(indicatorName);
                if (currentValue != null) {
                    return toDouble(currentValue);
                }
            }
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
     
        return normalized;
    }

    @Override
    public Double calculateTopsisPositive(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
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

    /**
     * 计算TOPSIS得分
     * 公式：TOPSIS_SCORE = D- / (D+ + D-)
     * 
     * @param params 参数格式："POSITIVE_IDEAL_FIELD,NEGATIVE_IDEAL_FIELD"
     * @param currentRegionCode 当前区域代码
     * @param allRegionData 所有区域数据
     * @return TOPSIS得分（0-1之间）
     */
    public Double calculateTopsisScore(
            String params,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {
        
        
        // 1. 解析参数：正理想解字段名,负理想解字段名
        String[] fields = params.split(",");
        if (fields.length != 2) {
            return 0.0;
        }
        
        String positiveField = fields[0].trim();
        String negativeField = fields[1].trim();
        
        // 2. 获取当前区域数据
        Map<String, Object> currentData = allRegionData.get(currentRegionCode);
        if (currentData == null) {
            log.warn("未找到当前区域数据: {}", currentRegionCode);
            return 0.0;
        }
        
        // 3. 获取正理想解距离 D+
        Object positiveValue = currentData.get(positiveField);
        if (positiveValue == null) {
            log.warn("未找到正理想解距离: region={}, field={}", currentRegionCode, positiveField);
            return 0.0;
        }
        double dPositive = toDouble(positiveValue);
        
        // 4. 获取负理想解距离 D-
        Object negativeValue = currentData.get(negativeField);
        if (negativeValue == null) {
            log.warn("未找到负理想解距离: region={}, field={}", currentRegionCode, negativeField);
            return 0.0;
        }
        double dNegative = toDouble(negativeValue);
        
        // 5. 计算TOPSIS得分：D- / (D+ + D-)
        double denominator = dPositive + dNegative;
        if (denominator == 0) {
            return 0.0;
        }
        
        double score = dNegative / denominator;
        return score;
    }

    @Override
    public String calculateGrade(
            String scoreField,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData) {

        
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
        
  
        
        // 确保值不小于0（根据规则中的[0,...)区间)
        value = Math.max(0, value);
        
        if (mean <= halfStdev) {
            // 情况1：μ ≤ 0.5σ，分为3级
            if (value >= meanPlusOneAndHalf) {
                return "强";
            } else if (value >= meanPlusHalf) {
                return "较强";
            } else {
                return "中等";
            }
        } else if (mean <= oneAndHalfStdev) {
            // 情况2：0.5σ < μ ≤ 1.5σ，分为4级
            
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
