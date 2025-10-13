package com.evaluate.service;

import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.AlgorithmRuleMapping;
import com.evaluate.entity.DynamicRule;

import java.util.List;
import java.util.Map;

/**
 * 算法配置服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface AlgorithmConfigService {

    /**
     * 创建算法配置
     * 
     * @param algorithmConfig 算法配置信息
     * @return 创建的算法配置
     */
    AlgorithmConfig createAlgorithmConfig(AlgorithmConfig algorithmConfig);

    /**
     * 更新算法配置
     * 
     * @param algorithmConfig 算法配置信息
     * @return 更新后的算法配置
     */
    AlgorithmConfig updateAlgorithmConfig(AlgorithmConfig algorithmConfig);

    /**
     * 删除算法配置
     * 
     * @param configId 配置ID
     * @return 是否删除成功
     */
    boolean deleteAlgorithmConfig(Long configId);

    /**
     * 根据ID查询算法配置
     * 
     * @param configId 配置ID
     * @return 算法配置
     */
    AlgorithmConfig getAlgorithmConfigById(Long configId);

    /**
     * 查询所有算法配置
     * 
     * @return 算法配置列表
     */
    List<AlgorithmConfig> getAllAlgorithmConfigs();

    /**
     * 为算法配置添加规则映射
     * 
     * @param mapping 规则映射信息
     * @return 创建的规则映射
     */
    AlgorithmRuleMapping addRuleMapping(AlgorithmRuleMapping mapping);

    /**
     * 更新规则映射
     * 
     * @param mapping 规则映射信息
     * @return 更新后的规则映射
     */
    AlgorithmRuleMapping updateRuleMapping(AlgorithmRuleMapping mapping);

    /**
     * 删除规则映射
     * 
     * @param mappingId 映射ID
     * @return 是否删除成功
     */
    boolean deleteRuleMapping(Long mappingId);

    /**
     * 根据算法配置ID查询规则映射
     * 
     * @param algorithmConfigId 算法配置ID
     * @return 规则映射列表
     */
    List<AlgorithmRuleMapping> getRuleMappingsByConfigId(Long algorithmConfigId);

    /**
     * 根据算法步骤ID查询规则映射
     * 
     * @param algorithmStepId 算法步骤ID
     * @return 规则映射列表
     */
    List<AlgorithmRuleMapping> getRuleMappingsByStepId(Long algorithmStepId);

    /**
     * 执行算法配置
     * 
     * @param configId 算法配置ID
     * @param inputData 输入数据
     * @return 执行结果
     */
    Map<String, Object> executeAlgorithmConfig(Long configId, Map<String, Object> inputData);

    /**
     * 验证算法配置
     * 
     * @param configId 算法配置ID
     * @return 验证结果
     */
    Map<String, Object> validateAlgorithmConfig(Long configId);

    /**
     * 复制算法配置
     * 
     * @param sourceConfigId 源配置ID
     * @param newConfigName 新配置名称
     * @return 复制的算法配置
     */
    AlgorithmConfig copyAlgorithmConfig(Long sourceConfigId, String newConfigName);

    /**
     * 获取算法配置的执行统计
     * 
     * @param configId 算法配置ID
     * @return 执行统计信息
     */
    Map<String, Object> getAlgorithmConfigStats(Long configId);
}