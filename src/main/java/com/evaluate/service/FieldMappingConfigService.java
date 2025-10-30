package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.FieldMappingConfig;

import java.util.List;
import java.util.Map;

/**
 * 字段映射配置服务接口
 *
 * @author system
 * @since 2025-10-29
 */
public interface FieldMappingConfigService extends IService<FieldMappingConfig> {

    /**
     * 根据模型ID获取字段映射配置
     *
     * @param modelId 模型ID
     * @return 字段映射配置列表
     */
    List<FieldMappingConfig> getByModelId(Long modelId);

    /**
     * 根据模型ID获取字段映射（算法输出字段 -> 数据库字段）
     *
     * @param modelId 模型ID
     * @return 字段映射Map
     */
    Map<String, String> getFieldMappingByModelId(Long modelId);

    /**
     * 根据模型ID和字段类型获取字段映射
     *
     * @param modelId 模型ID
     * @param fieldType 字段类型
     * @return 字段映射配置列表
     */
    List<FieldMappingConfig> getByModelIdAndFieldType(Long modelId, String fieldType);

    /**
     * 使用数据库配置映射字段值
     *
     * @param modelId 模型ID
     * @param algorithmData 算法输出数据
     * @param evaluationResult 评估结果对象
     */
    void mapFieldsUsingDatabaseConfig(Long modelId, Map<String, Object> algorithmData,
                                     com.evaluate.entity.EvaluationResult evaluationResult);

    /**
     * 刷新字段映射缓存
     *
     * @param modelId 模型ID
     */
    void refreshFieldMappingCache(Long modelId);
}