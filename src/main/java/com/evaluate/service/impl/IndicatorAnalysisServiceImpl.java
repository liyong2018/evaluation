package com.evaluate.service.impl;

import com.evaluate.dto.topsis.IndicatorMetadata;
import com.evaluate.entity.PrimaryIndicatorResult;
import com.evaluate.entity.SecondaryIndicatorResult;
import com.evaluate.service.IndicatorAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标分析服务实现类
 * 
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class IndicatorAnalysisServiceImpl implements IndicatorAnalysisService {

    // 指标显示名称映射
    private static final Map<String, String> INDICATOR_DISPLAY_NAMES = new HashMap<>();
    static {
        // 一级指标
        INDICATOR_DISPLAY_NAMES.put("level1Management", "灾害管理能力");
        INDICATOR_DISPLAY_NAMES.put("level1Preparation", "灾害备灾能力");
        INDICATOR_DISPLAY_NAMES.put("level1SelfRescue", "自救转移能力");
        INDICATOR_DISPLAY_NAMES.put("overallCapability", "综合减灾能力");
        
        // 二级指标原始值
        INDICATOR_DISPLAY_NAMES.put("managementCapability", "管理能力");
        INDICATOR_DISPLAY_NAMES.put("riskAssessmentCapability", "风险评估能力");
        INDICATOR_DISPLAY_NAMES.put("fundingCapability", "资金保障能力");
        INDICATOR_DISPLAY_NAMES.put("materialCapability", "物资保障能力");
        INDICATOR_DISPLAY_NAMES.put("medicalCapability", "医疗救护能力");
        INDICATOR_DISPLAY_NAMES.put("selfRescueCapability", "自救互救能力");
        INDICATOR_DISPLAY_NAMES.put("publicAvoidanceCapability", "公众避险能力");
        INDICATOR_DISPLAY_NAMES.put("relocationCapability", "转移安置能力");
        
        // 二级指标归一化值
        INDICATOR_DISPLAY_NAMES.put("managementNormalized", "管理能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("riskAssessmentNormalized", "风险评估能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("fundingNormalized", "资金保障能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("materialNormalized", "物资保障能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("medicalNormalized", "医疗救护能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("selfRescueNormalized", "自救互救能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("publicAvoidanceNormalized", "公众避险能力(归一化)");
        INDICATOR_DISPLAY_NAMES.put("relocationNormalized", "转移安置能力(归一化)");
    }

    @Override
    public List<IndicatorMetadata> getIndicatorMetadata(Long modelId) {
        log.debug("获取模型指标元数据: modelId={}", modelId);
        
        List<IndicatorMetadata> metadataList = new ArrayList<>();
        
        try {
            // 获取一级指标元数据
            metadataList.addAll(getPrimaryIndicatorMetadata());
            
            // 获取二级指标元数据
            metadataList.addAll(getSecondaryIndicatorMetadata());
            
            // 分析每个指标的数据质量
            for (IndicatorMetadata metadata : metadataList) {
                enhanceMetadataWithQualityAnalysis(modelId, metadata);
            }
            
            return metadataList.stream()
                    .sorted(Comparator.comparing(IndicatorMetadata::getDisplayName))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取指标元数据失败: modelId={}", modelId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public IndicatorMetadata analyzeIndicatorQuality(Long modelId, String columnName) {
        log.debug("分析指标数据质量: modelId={}, columnName={}", modelId, columnName);
        
        try {
            IndicatorMetadata metadata = createBasicMetadata(columnName);
            enhanceMetadataWithQualityAnalysis(modelId, metadata);
            return metadata;
        } catch (Exception e) {
            log.error("分析指标质量失败: modelId={}, columnName={}", modelId, columnName, e);
            return null;
        }
    }

    @Override
    public List<IndicatorMetadata> getRecommendedIndicators(Long modelId, int maxCount) {
        log.debug("获取推荐指标: modelId={}, maxCount={}", modelId, maxCount);
        
        List<IndicatorMetadata> allIndicators = getIndicatorMetadata(modelId);
        
        return allIndicators.stream()
                .filter(IndicatorMetadata::isSuitableForTOPSIS)
                .sorted((a, b) -> {
                    // 按质量评分和完整性排序
                    double scoreA = a.getQualityScore() * 0.6 + a.getCompleteness() * 0.4;
                    double scoreB = b.getQualityScore() * 0.6 + b.getCompleteness() * 0.4;
                    return Double.compare(scoreB, scoreA);
                })
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> validateIndicatorCompleteness(Long modelId, List<String> indicators) {
        log.debug("验证指标完整性: modelId={}, indicators={}", modelId, indicators);
        
        Map<String, Double> completenessMap = new HashMap<>();
        
        for (String indicator : indicators) {
            try {
                StatisticalSummary summary = getStatisticalSummary(modelId, indicator);
                completenessMap.put(indicator, summary.getCompleteness());
            } catch (Exception e) {
                log.warn("计算指标完整性失败: indicator={}", indicator, e);
                completenessMap.put(indicator, 0.0);
            }
        }
        
        return completenessMap;
    }

    @Override
    public Map<String, Map<String, Double>> calculateCorrelationMatrix(Long modelId, List<String> indicators) {
        log.debug("计算指标相关性矩阵: modelId={}, indicators={}", modelId, indicators);
        
        // 简化实现，实际应该从数据库获取数据计算相关性
        Map<String, Map<String, Double>> correlationMatrix = new HashMap<>();
        
        for (String indicator1 : indicators) {
            Map<String, Double> correlations = new HashMap<>();
            for (String indicator2 : indicators) {
                if (indicator1.equals(indicator2)) {
                    correlations.put(indicator2, 1.0);
                } else {
                    // 模拟相关性计算，实际应该基于真实数据
                    correlations.put(indicator2, Math.random() * 0.8 - 0.4);
                }
            }
            correlationMatrix.put(indicator1, correlations);
        }
        
        return correlationMatrix;
    }

    @Override
    public OutlierDetectionResult detectOutliers(Long modelId, String columnName) {
        log.debug("检测指标异常值: modelId={}, columnName={}", modelId, columnName);
        
        try {
            StatisticalSummary summary = getStatisticalSummary(modelId, columnName);
            
            // 使用IQR方法检测异常值
            double iqr = summary.getQ3() - summary.getQ1();
            double lowerBound = summary.getQ1() - 1.5 * iqr;
            double upperBound = summary.getQ3() + 1.5 * iqr;
            
            // 简化实现，实际应该从数据库查询异常值
            List<Double> outliers = new ArrayList<>();
            double outlierPercentage = 0.05; // 假设5%的异常值
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("lowerBound", lowerBound);
            parameters.put("upperBound", upperBound);
            parameters.put("iqr", iqr);
            
            return new OutlierDetectionResult(outliers, outlierPercentage, "IQR", parameters);
            
        } catch (Exception e) {
            log.error("异常值检测失败: modelId={}, columnName={}", modelId, columnName, e);
            return new OutlierDetectionResult(Collections.emptyList(), 0.0, "ERROR", Collections.emptyMap());
        }
    }

    @Override
    public StatisticalSummary getStatisticalSummary(Long modelId, String columnName) {
        log.debug("获取指标统计摘要: modelId={}, columnName={}", modelId, columnName);
        
        try {
            // 简化实现，实际应该从数据库查询统计数据
            // 这里返回模拟数据，实际实现需要根据具体的数据表结构来查询
            
            Long count = 100L;
            Long nonNullCount = 95L;
            Double mean = 0.75;
            Double median = 0.73;
            Double mode = 0.70;
            Double standardDeviation = 0.15;
            Double variance = 0.0225;
            Double min = 0.20;
            Double max = 1.00;
            Double q1 = 0.65;
            Double q3 = 0.85;
            Double skewness = 0.1;
            Double kurtosis = -0.5;
            
            return new StatisticalSummary(count, nonNullCount, mean, median, mode,
                    standardDeviation, variance, min, max, q1, q3, skewness, kurtosis);
                    
        } catch (Exception e) {
            log.error("获取统计摘要失败: modelId={}, columnName={}", modelId, columnName, e);
            return new StatisticalSummary(0L, 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * 获取一级指标元数据
     */
    private List<IndicatorMetadata> getPrimaryIndicatorMetadata() {
        List<IndicatorMetadata> metadataList = new ArrayList<>();
        
        Field[] fields = PrimaryIndicatorResult.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (isNumericIndicatorField(fieldName)) {
                IndicatorMetadata metadata = createBasicMetadata(fieldName);
                metadata.setCategory("primary");
                metadataList.add(metadata);
            }
        }
        
        return metadataList;
    }

    /**
     * 获取二级指标元数据
     */
    private List<IndicatorMetadata> getSecondaryIndicatorMetadata() {
        List<IndicatorMetadata> metadataList = new ArrayList<>();
        
        Field[] fields = SecondaryIndicatorResult.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (isNumericIndicatorField(fieldName)) {
                IndicatorMetadata metadata = createBasicMetadata(fieldName);
                metadata.setCategory("secondary");
                metadataList.add(metadata);
            }
        }
        
        return metadataList;
    }

    /**
     * 创建基础元数据
     */
    private IndicatorMetadata createBasicMetadata(String columnName) {
        return IndicatorMetadata.builder()
                .columnName(columnName)
                .displayName(INDICATOR_DISPLAY_NAMES.getOrDefault(columnName, columnName))
                .description("指标: " + columnName)
                .dataType("Double")
                .isNumeric(true)
                .isBenefitType(true) // 默认为效益型指标
                .unit("分值")
                .build();
    }

    /**
     * 增强元数据的质量分析
     */
    private void enhanceMetadataWithQualityAnalysis(Long modelId, IndicatorMetadata metadata) {
        try {
            StatisticalSummary summary = getStatisticalSummary(modelId, metadata.getColumnName());
            
            metadata.setCompleteness(summary.getCompleteness());
            metadata.setMinValue(summary.getMin());
            metadata.setMaxValue(summary.getMax());
            metadata.setAvgValue(summary.getMean());
            metadata.setStdDev(summary.getStandardDeviation());
            metadata.setSampleCount(summary.getCount());
            metadata.setNonNullCount(summary.getNonNullCount());
            
            // 计算质量评分
            double qualityScore = calculateQualityScore(summary);
            metadata.setQualityScore(qualityScore);
            
            // 设置推荐标志
            metadata.setRecommended(metadata.isSuitableForTOPSIS());
            
        } catch (Exception e) {
            log.warn("增强元数据质量分析失败: columnName={}", metadata.getColumnName(), e);
            metadata.setCompleteness(0.0);
            metadata.setQualityScore(0.0);
            metadata.setRecommended(false);
        }
    }

    /**
     * 计算质量评分
     */
    private double calculateQualityScore(StatisticalSummary summary) {
        double score = 1.0;
        
        // 完整性权重 40%
        score *= 0.4 * summary.getCompleteness();
        
        // 数据分布权重 30%
        if (summary.getStandardDeviation() != null && summary.getMean() != null && summary.getMean() != 0) {
            double cv = summary.getStandardDeviation() / Math.abs(summary.getMean());
            score += 0.3 * Math.max(0, 1 - cv); // 变异系数越小质量越高
        } else {
            score += 0.15; // 部分分数
        }
        
        // 数据范围合理性权重 20%
        if (summary.getMin() != null && summary.getMax() != null) {
            double range = summary.getMax() - summary.getMin();
            if (range > 0 && range <= 10) { // 假设合理范围
                score += 0.2;
            } else {
                score += 0.1;
            }
        }
        
        // 异常值影响权重 10%
        if (summary.getSkewness() != null && Math.abs(summary.getSkewness()) < 2) {
            score += 0.1;
        } else {
            score += 0.05;
        }
        
        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * 判断是否为数值型指标字段
     */
    private boolean isNumericIndicatorField(String fieldName) {
        // 排除系统字段
        if (fieldName.equals("id") || fieldName.equals("surveyId") || fieldName.equals("surveyDataId") ||
            fieldName.equals("algorithmId") || fieldName.equals("weightConfigId") || fieldName.equals("configId") ||
            fieldName.equals("evaluationId") || fieldName.equals("createTime") || fieldName.equals("calculateTime") ||
            fieldName.equals("updateTime") || fieldName.equals("isDeleted") || fieldName.equals("secondaryResultId") ||
            fieldName.equals("serialVersionUID") || fieldName.contains("Grade") || fieldName.contains("grade")) {
            return false;
        }
        
        // 包含数值型指标字段
        return fieldName.contains("level") || fieldName.contains("capability") ||
               fieldName.contains("capacity") || fieldName.contains("Capability") ||
               fieldName.contains("Normalized") || fieldName.contains("normalized") ||
               fieldName.contains("overall") || fieldName.contains("Overall");
    }
    
    @Override
    public List<String> getAvailableIndicators(long modelId) {
        log.debug("获取模型 {} 的可用指标", modelId);
        
        // 根据模型ID返回可用指标列表
        List<String> indicators = new ArrayList<>();
        
        if (modelId == 9L) { // 综合模型
            indicators.addAll(Arrays.asList(
                "township_disasterMgmtScore", "township_disasterPrepScore", "township_selfRescueScore",
                "community_disasterMgmtScore", "community_disasterPrepScore", "community_selfRescueScore"
            ));
        } else if (modelId == 3L) { // 标准模型
            indicators.addAll(Arrays.asList(
                "disasterMgmtScore", "disasterPrepScore", "selfRescueScore"
            ));
        } else if (modelId == 8L) { // 社区-乡镇模型
            indicators.addAll(Arrays.asList(
                "disasterMgmtScore", "disasterPrepScore", "selfRescueScore"
            ));
        } else {
            // 默认指标
            indicators.addAll(Arrays.asList(
                "level1Management", "level1Preparation", "level1SelfRescue"
            ));
        }
        
        log.debug("模型 {} 可用指标: {}", modelId, indicators);
        return indicators;
    }
}