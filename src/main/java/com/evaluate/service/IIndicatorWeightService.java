package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.IndicatorWeight;

import java.util.List;
import java.util.Map;

/**
 * 指标权重服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface IIndicatorWeightService extends IService<IndicatorWeight> {

    /**
     * 根据配置ID查询权重列表
     * 
     * @param configId 配置ID
     * @return 指标权重列表
     */
    List<IndicatorWeight> getByConfigId(Long configId);

    /**
     * 根据配置ID和指标级别查询权重列表
     * 
     * @param configId 配置ID
     * @param indicatorLevel 指标级别
     * @return 指标权重列表
     */
    List<IndicatorWeight> getByConfigIdAndLevel(Long configId, Integer indicatorLevel);

    /**
     * 根据父指标ID查询子指标权重列表
     * 
     * @param parentId 父指标ID
     * @return 指标权重列表
     */
    List<IndicatorWeight> getByParentId(Long parentId);

    /**
     * 根据配置ID和指标代码查询权重
     * 
     * @param configId 配置ID
     * @param indicatorCode 指标代码
     * @return 指标权重
     */
    IndicatorWeight getByConfigIdAndCode(Long configId, String indicatorCode);

    /**
     * 获取树形结构的权重数据
     * 
     * @param configId 配置ID
     * @return 树形权重列表
     */
    List<IndicatorWeight> getTreeByConfigId(Long configId);

    /**
     * 批量保存指标权重
     * 
     * @param weightList 权重列表
     * @return 保存结果
     */
    boolean batchSave(List<IndicatorWeight> weightList);

    /**
     * 批量更新权重值
     * 
     * @param weightList 权重列表
     * @return 更新结果
     */
    boolean batchUpdateWeight(List<IndicatorWeight> weightList);

    /**
     * 根据配置ID删除所有权重
     * 
     * @param configId 配置ID
     * @return 删除结果
     */
    boolean deleteByConfigId(Long configId);

    /**
     * 复制权重配置
     * 
     * @param sourceConfigId 源配置ID
     * @param targetConfigId 目标配置ID
     * @return 复制结果
     */
    boolean copyWeightsByConfigId(Long sourceConfigId, Long targetConfigId);

    /**
     * 初始化默认权重配置
     * 
     * @param configId 配置ID
     * @return 初始化结果
     */
    boolean initDefaultWeights(Long configId);

    /**
     * 验证权重配置的完整性
     * 
     * @param configId 配置ID
     * @return 验证结果
     */
    boolean validateWeightIntegrity(Long configId);

    /**
     * 获取权重配置的统计信息
     * 
     * @param configId 配置ID
     * @return 统计信息
     */
    Map<String, Object> getWeightStatistics(Long configId);

    /**
     * 更新指标权重
     * 
     * @param indicatorWeight 指标权重
     * @return 更新结果
     */
    boolean updateIndicatorWeight(IndicatorWeight indicatorWeight);

    /**
     * 添加新指标权重
     * 
     * @param indicatorWeight 指标权重
     * @return 添加结果
     */
    boolean addIndicatorWeight(IndicatorWeight indicatorWeight);

    /**
     * 删除指标权重
     * 
     * @param id 权重ID
     * @return 删除结果
     */
    boolean deleteIndicatorWeight(Long id);

    /**
     * 验证指标权重数据
     * 
     * @param indicatorWeight 指标权重
     * @return 验证结果
     */
    boolean validateIndicatorWeight(IndicatorWeight indicatorWeight);

    List<IndicatorWeight> getSecondaryWeights(Long weightConfigId);

    List<IndicatorWeight> getPrimaryWeights(Long weightConfigId);

    List<IndicatorWeight> getChildWeights(Long weightConfigId, Long parentId);
}