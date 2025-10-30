package com.evaluate.service.adapter;

import java.util.List;
import java.util.Map;

/**
 * 数据源适配器接口
 * 为不同类型的数据源提供统一的数据访问接口
 *
 * @author System
 * @since 2025-01-01
 */
public interface DataSourceAdapter {

    /**
     * 获取适配器类型
     * @return 数据源类型标识
     */
    String getAdapterType();

    /**
     * 获取指定地区的原始数据
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 原始数据映射
     */
    Map<String, Object> getRawData(List<String> regionCodes, Long weightConfigId);

    /**
     * 获取指定地区的指标数据
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 指标数据映射，格式为 Map<地区代码, Map<指标代码, 数值>>
     */
    Map<String, Map<String, Double>> getIndicatorData(List<String> regionCodes, Long weightConfigId);

    /**
     * 获取地区名称映射
     * @param regionCodes 地区代码列表
     * @return 地区代码到名称的映射
     */
    Map<String, String> getRegionNames(List<String> regionCodes);

    /**
     * 验证数据源是否可用
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 验证结果
     */
    boolean validateDataSource(List<String> regionCodes, Long weightConfigId);

    /**
     * 获取数据源的元数据信息
     * @return 元数据信息
     */
    Map<String, Object> getMetadata();

    /**
     * 获取支持的所有指标代码
     * @param weightConfigId 权重配置ID
     * @return 指标代码列表
     */
    List<String> getSupportedIndicators(Long weightConfigId);

    /**
     * 检查地区是否存在于数据源中
     * @param regionCode 地区代码
     * @return 是否存在
     */
    boolean isRegionSupported(String regionCode);

    /**
     * 获取数据源的描述信息
     * @return 描述信息
     */
    String getDescription();
}