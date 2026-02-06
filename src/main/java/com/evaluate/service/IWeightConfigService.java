package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.WeightConfig;

import java.util.List;

/**
 * 权重配置服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface IWeightConfigService extends IService<WeightConfig> {

    /**
     * 获取默认权重配置
     * 
     * @return 默认权重配置
     */
    WeightConfig getDefaultConfig();

    /**
     * 根据配置名称查询
     * 
     * @param configName 配置名称
     * @return 权重配置
     */
    WeightConfig getByConfigName(String configName);

    /**
     * 获取启用状态的配置列表
     * 
     * @return 权重配置列表
     */
    List<WeightConfig> getEnabledConfigs();

    /**
     * 根据创建人查询配置列表
     * 
     * @param creator 创建人
     * @return 权重配置列表
     */
    List<WeightConfig> getByCreator(String creator);

    /**
     * 设置默认配置
     * 
     * @param id 配置ID
     * @return 设置结果
     */
    boolean setDefaultConfig(Long id);

    /**
     * 创建权重配置
     * 
     * @param weightConfig 权重配置
     * @return 创建结果
     */
    boolean createWeightConfig(WeightConfig weightConfig);

    /**
     * 更新权重配置
     * 
     * @param weightConfig 权重配置
     * @return 更新结果
     */
    boolean updateWeightConfig(WeightConfig weightConfig);

    /**
     * 删除权重配置及相关数据
     * 
     * @param id 配置ID
     * @return 删除结果
     */
    boolean deleteWeightConfigAndRelated(Long id);

    /**
     * 复制权重配置
     * 
     * @param sourceId 源配置ID
     * @param newConfigName 新配置名称
     * @param creator 创建人
     * @return 复制结果
     */
    boolean copyWeightConfig(Long sourceId, String newConfigName, String creator);

    /**
     * 验证权重配置
     * 
     * @param weightConfig 权重配置
     * @return 验证结果
     */
    boolean validateWeightConfig(WeightConfig weightConfig);

    /**
     * 启用/禁用权重配置
     *
     * @param id 配置ID
     * @param status 状态(0-禁用，1-启用)
     * @return 操作结果
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 根据组织机构编码查询配置列表
     *
     * @param orgcode 组织机构编码
     * @return 权重配置列表
     */
    List<WeightConfig> getByOrgcode(String orgcode);

    /**
     * 获取某组织机构某年度的默认模型权重配置列表（不存在则自动创建）
     *
     * @param orgcode 组织机构编码
     * @param year 年度
     * @return 权重配置列表（按模型顺序）
     */
    List<WeightConfig> getOrCreateModelYearConfigs(String orgcode, Integer year);

    /**
     * 获取某组织机构某年度的有效模型权重配置列表（不创建数据）
     * 当存在多套配置时，按“专家打分记录数优先，其次创建时间最新”选择一套返回。
     *
     * @param orgcode 组织机构编码
     * @param year 年度
     * @return 权重配置列表（按模型顺序）
     */
    List<WeightConfig> getEffectiveModelYearConfigs(String orgcode, Integer year);
}
