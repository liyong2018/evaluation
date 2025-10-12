package com.evaluate.service;

import java.util.List;
import java.util.Map;

/**
 * 特殊算法标记处理服务接口
 * 用于处理QLExpress无法直接执行的跨区域聚合算法
 * 
 * @author System
 * @since 2025-10-12
 */
public interface SpecialAlgorithmService {

    /**
     * 执行特殊标记算法
     * 
     * @param marker 标记类型 (NORMALIZE, TOPSIS_POSITIVE, TOPSIS_NEGATIVE, GRADE)
     * @param params 参数字符串
     * @param currentRegionCode 当前区域代码
     * @param regionContext 当前区域上下文数据
     * @param allRegionData 所有区域的数据（Map<regionCode, contextData>）
     * @return 计算结果
     */
    Object executeSpecialAlgorithm(
            String marker,
            String params,
            String currentRegionCode,
            Map<String, Object> regionContext,
            Map<String, Map<String, Object>> allRegionData
    );

    /**
     * 归一化算法：value / SQRT(SUMSQ(all_values))
     * 
     * @param indicatorName 指标名称
     * @param currentRegionCode 当前区域代码
     * @param allRegionData 所有区域的数据
     * @return 归一化值
     */
    Double normalize(
            String indicatorName,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData
    );

    /**
     * TOPSIS优解距离：SQRT(SUM((max_value - current_value)^2))
     * 
     * @param indicators 指标名称列表（逗号分隔）
     * @param currentRegionCode 当前区域代码
     * @param allRegionData 所有区域的数据
     * @return 优解距离
     */
    Double calculateTopsisPositive(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData
    );

    /**
     * TOPSIS劣解距离：SQRT(SUM((min_value - current_value)^2))
     * 
     * @param indicators 指标名称列表（逗号分隔）
     * @param currentRegionCode 当前区域代码
     * @param allRegionData 所有区域的数据
     * @return 劣解距离
     */
    Double calculateTopsisNegative(
            String indicators,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData
    );

    /**
     * 能力分级算法：基于均值和标准差进行五级分类
     * 
     * @param scoreField 分数字段名
     * @param currentRegionCode 当前区域代码
     * @param allRegionData 所有区域的数据
     * @return 分级结果（强/较强/中等/较弱/弱）
     */
    String calculateGrade(
            String scoreField,
            String currentRegionCode,
            Map<String, Map<String, Object>> allRegionData
    );
}
